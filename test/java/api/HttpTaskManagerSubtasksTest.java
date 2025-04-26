package java.api;

import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerSubtasksTest extends HttpTaskServerTestBase {

    @Test
    public void testCreateSubtaskWithTiming() throws Exception {
        System.out.println("Запуск теста: проверка создания подзадачи с временными параметрами...");

        // Создаем эпик
        Epic epic = manager.createEpic(new Epic("Ремонт квартиры", "Полный цикл ремонтных работ"));
        System.out.println("Создан эпик ID: " + epic.getId());

        // Подготовка подзадачи с временем
        Subtask subtask = new Subtask("Закупка материалов", "Покупка стройматериалов", epic.getId());
        subtask.setStartTime(LocalDateTime.of(2024, 10, 15, 10, 0));
        subtask.setDuration(Duration.ofHours(2));

        // Сериализация с использованием кастомных адаптеров
        String subtaskJson = gson.toJson(subtask);
        System.out.println("JSON подзадачи: " + subtaskJson);

        // Отправка POST-запроса
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Статус ответа должен быть 201 Created");

        // Проверка данных в менеджере
        Subtask createdSubtask = manager.getAllSubtasks().getFirst();
        assertAll("Проверка полей подзадачи",
                () -> assertEquals("Закупка материалов", createdSubtask.getTitle()),
                () -> assertEquals(epic.getId(), createdSubtask.getEpicId()),
                () -> assertEquals(LocalDateTime.of(2024, 10, 15, 10, 0), createdSubtask.getStartTime()),
                () -> assertEquals(Duration.ofHours(2), createdSubtask.getDuration())
        );

        // Проверка обновления времени эпика
        Epic updatedEpic = manager.getEpicById(epic.getId());
        assertAll("Проверка времени эпика",
                () -> assertEquals(createdSubtask.getStartTime(), updatedEpic.getStartTime()),
                () -> assertEquals(createdSubtask.getEndTime(), updatedEpic.getEndTime()),
                () -> assertEquals(Duration.ofHours(2), updatedEpic.getDuration())
        );

        System.out.println("Тест пройден: подзадача и время эпика корректны ");
    }
}
