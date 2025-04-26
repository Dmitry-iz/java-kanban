package handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;

import managers.ManagerValidationException;
import managers.TaskManager;
import tasks.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TasksHandler extends BaseHttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public TasksHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET" -> {
                    if ("/tasks".equals(path)) {
                        sendSuccess(exchange, gson.toJson(taskManager.getAllTasks()));
                    } else if (path.startsWith("/tasks/")) {
                        int id = Integer.parseInt(path.substring(7));
                        Task task = taskManager.getTaskById(id);
                        if (task != null) {
                            sendSuccess(exchange, gson.toJson(task));
                        } else {
                            sendNotFound(exchange);
                        }
                    } else {
                        sendNotFound(exchange);
                    }
                }
                case "POST" -> {
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    try {
                        Task task = gson.fromJson(body, Task.class);
                        try {
                            if (task.getId() == 0) {
                                Task created = taskManager.createTask(task);
                                sendCreated(exchange, gson.toJson(created));
                            } else {
                                taskManager.updateTask(task);
                                sendCreated(exchange, gson.toJson(task));
                            }
                        } catch (ManagerValidationException e) {
                            sendHasInteractions(exchange); // Отправляем 406 при пересечении
                        }
                    } catch (JsonSyntaxException e) {
                        sendBadRequest(exchange);
                    }
                }
                case "DELETE" -> {
                    if ("/tasks".equals(path)) {
                        taskManager.deleteAllTasks();
                        sendSuccess(exchange, "Все задачи удалены");
                    } else if (path.startsWith("/tasks/")) {
                        int id = Integer.parseInt(path.substring(7));
                        taskManager.deleteTask(id);
                        sendSuccess(exchange, "Задача " + id + " удалена");
                    } else {
                        sendNotFound(exchange);
                    }
                }
                case null, default -> sendNotFound(exchange);
            }
        } catch (JsonSyntaxException | NumberFormatException e) {
            sendBadRequest(exchange);
        } catch (Exception e) {
            sendInternalError(exchange);
        } finally {
            exchange.close();
        }
    }
}

