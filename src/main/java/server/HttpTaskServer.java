package server;

import adapters.DurationAdapter;
import adapters.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import handler.*;
import managers.FileBackedTaskManager;
import managers.TaskManager;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private Gson gson;

    public Gson getGson() {
        return gson;
    }

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .serializeNulls()
                .create();

        server = HttpServer.create(new InetSocketAddress(PORT), 0); // Инициализация HTTP сервера с привязкой к порту
        server.createContext("/tasks", new TasksHandler(taskManager, gson)); // Обработчик задач
        server.createContext("/subtasks", new SubtasksHandler(taskManager, gson)); // Обработчик подзадач
        server.createContext("/epics", new EpicsHandler(taskManager, gson)); // Обработчик эпиков
        server.createContext("/history", new HistoryHandler(taskManager, gson)); // История просмотров
        server.createContext("/prioritized", new PrioritizedHandler(taskManager, gson)); // Приоритетные задачи
    }

    // Метод запуска сервера с выводом статуса в консоль
    public void start() {
        server.start();
        System.out.println("HTTP-сервер запущен на порту " + PORT);
    }

    // Метод остановки сервера с задержкой 0 (немедленная остановка)
    public void stop() {
        server.stop(0);
        System.out.println("HTTP-сервер остановлен");
    }

    public static void main(String[] args) {
        try {
            File file = new File("tasks.csv");
            FileBackedTaskManager manager = new FileBackedTaskManager(file);
            HttpTaskServer server = new HttpTaskServer(manager);
            server.start();
        } catch (IOException e) {
            System.out.println("Ошибка при создании сервера: " + e.getMessage());
        }
    }
}
