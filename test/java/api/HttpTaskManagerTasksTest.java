package java.api;

import org.junit.jupiter.api.Test;
import tasks.Status;
import tasks.Task;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTasksTest extends HttpTaskServerTestBase {

    @Test
    public void testCreateTask() throws Exception {
        System.out.println("Запуск теста: проверка создания задачи через API...");

        // 1. Подготовка задачи с временными параметрами
        Task task = new Task(
                "Тестовая задача",
                "Описание задачи",
                Status.NEW,
                LocalDateTime.now(),
                Duration.ofHours(2)
        );
        String taskJson = gson.toJson(task);
        System.out.println("Сериализованная задача в JSON: " + taskJson);

        // 2. Настройка HTTP-клиента и запроса
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        System.out.println("Отправка POST-запроса на /tasks...");

        // 3. Отправка запроса и получение ответа
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Получен ответ: статус " + response.statusCode());

        // 4. Проверка кода ответа и наличия задачи в менеджере
        assertEquals(201, response.statusCode(), "Сервер должен вернуть 201 Created");
        assertEquals(1, manager.getAllTasks().size(), "В менеджере должна быть одна задача");

        System.out.println("Тест пройден: задача успешно создана ");
    }

    @Test
    public void testGetTaskById() throws Exception {
        System.out.println("Запуск теста: проверка получения задачи по ID...");
        Task testTask = new Task(
                "Тестовая задача",
                "Описание задачи",
                Status.NEW,
                LocalDateTime.now(),
                Duration.ofHours(2)
        );

        // 1. Создание тестовой задачи
        Task task = manager.createTask(testTask);
        int taskId = task.getId();
        System.out.println("Создана задача с ID: " + taskId);

        // 2. Формирование GET-запроса
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + taskId))
                .GET()
                .build();
        System.out.println("Отправка GET-запроса на /tasks/" + taskId + "...");

        // 3. Получение и проверка ответа
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Получен ответ: статус " + response.statusCode());

        assertEquals(200, response.statusCode(), "Ожидался статус 200 OK");
        Task receivedTask = gson.fromJson(response.body(), Task.class);
        assertEquals(taskId, receivedTask.getId(), "ID задачи не совпадает");

        System.out.println("Тест пройден: задача успешно получена ");
    }

    @Test
    public void testDeleteTask() throws Exception {
        System.out.println("Запуск теста: проверка удаления задачи...");

        // 1. Создание и проверка задачи
        Task task = manager.createTask(new Task("Тест", "Описание"));
        int taskId = task.getId();
        System.out.println("Создана задача с ID: " + taskId);
        assertEquals(1, manager.getAllTasks().size(), "Задача не добавлена в менеджер");

        // 2. Формирование DELETE-запроса
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + taskId))
                .DELETE()
                .build();
        System.out.println("Отправка DELETE-запроса на /tasks/" + taskId + "...");

        // 3. Проверка ответа и состояния менеджера
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Получен ответ: статус " + response.statusCode());

        assertEquals(200, response.statusCode(), "Ожидался статус 200 OK");
        assertTrue(manager.getAllTasks().isEmpty(), "Задача не удалена из менеджера");

        System.out.println("Тест пройден: задача успешно удалена ");
    }
}
