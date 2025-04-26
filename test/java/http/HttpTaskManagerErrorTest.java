package http;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerErrorTest extends HttpTaskServerTestBase {

    @Test
    public void testInvalidTaskCreation() throws Exception {
        System.out.println("Запуск теста: проверка обработки некорректного JSON...");

        // 1. Подготовка некорректного JSON (незакрытая кавычка)
        String invalidJson = "{'title': 'Invalid}";

        // 2. Создание HTTP-клиента
        HttpClient client = HttpClient.newHttpClient();

        // 3. Формирование POST-запроса
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .build();

        // 4. Отправка запроса и получение ответа
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 5. Проверка статуса ответа (ожидаем 400 Bad Request)
        assertEquals(400, response.statusCode());

        System.out.println("Тест пройден: сервер вернул 400 на некорректный JSON");
    }

    @Test
    public void testTaskNotFound() throws Exception {
        System.out.println("Запуск теста: проверка запроса несуществующей задачи...");

        // 1. Создание HTTP-клиента
        HttpClient client = HttpClient.newHttpClient();

        // 2. Формирование GET-запроса к несуществующему ID (666)
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/666"))
                .GET()
                .build();

        // 3. Отправка запроса и получение ответа
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 4. Проверка статуса ответа (ожидаем 404 Not Found)
        assertEquals(404, response.statusCode());

        System.out.println("Тест пройден: сервер вернул 404 на несуществующую задачу ");
    }
}
