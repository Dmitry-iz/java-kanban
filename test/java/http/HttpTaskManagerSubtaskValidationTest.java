package http;

import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerSubtaskValidationTest extends HttpTaskServerTestBase {

    @Test
    public void testSubtaskTimeConflict() throws Exception {
        // Создаем эпик
        Epic epic = manager.createEpic(new Epic("Test Epic", "Description"));
        int epicId = epic.getId();

        // Создаем первую подзадачу
        Subtask subtask1 = new Subtask("Subtask 1", "Description", epicId);
        subtask1.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        subtask1.setDuration(Duration.ofHours(2));
        manager.createSubtask(subtask1);

        // Пытаемся создать вторую подзадачу с пересекающимся временем
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epicId);
        subtask2.setStartTime(LocalDateTime.of(2024, 1, 1, 11, 0)); // Пересекается с 10:00-12:00
        subtask2.setDuration(Duration.ofHours(1));

        String subtaskJson = gson.toJson(subtask2);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем, что сервер вернул 406
        assertEquals(406, response.statusCode(), "Ожидался статус 406 Not Acceptable");
    }
}