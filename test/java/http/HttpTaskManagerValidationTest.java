package http;

import org.junit.jupiter.api.Test;
import tasks.Task;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerValidationTest extends HttpTaskServerTestBase {

    @Test
    public void testTaskTimeConflict() throws Exception {
        // Создаем первую задачу
        Task task1 = new Task("Task 1", "Description");
        task1.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setDuration(Duration.ofHours(2));
        manager.createTask(task1);

        // Создаем вторую задачу с пересекающимся временем
        Task task2 = new Task("Task 2", "Description");
        task2.setStartTime(LocalDateTime.of(2024, 1, 1, 11, 0));
        task2.setDuration(Duration.ofHours(1));

        String taskJson = gson.toJson(task2);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode(), "Ожидался статус 406 Not Acceptable");
    }
}