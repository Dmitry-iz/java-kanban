package handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import managers.TaskManager;
import tasks.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SubtasksHandler extends BaseHttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public SubtasksHandler(TaskManager taskManager, Gson gson) {
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
                    if ("/subtasks".equals(path)) {
                        sendSuccess(exchange, gson.toJson(taskManager.getAllSubtasks()));
                    } else if (path.startsWith("/subtasks/")) {
                        int id = Integer.parseInt(path.substring(10));
                        Subtask subtask = taskManager.getSubTaskById(id);
                        if (subtask != null) {
                            sendSuccess(exchange, gson.toJson(subtask));
                        } else {
                            sendNotFound(exchange);
                        }
                    }
                }
                case "POST" -> {
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    Subtask subtask = gson.fromJson(body, Subtask.class);
                    if (subtask.getId() == 0) {
                        try {
                            Subtask created = taskManager.createSubtask(subtask);
                            sendCreated(exchange, gson.toJson(created));
                        } catch (Exception e) {
                            sendBadRequest(exchange);
                        }
                    } else {
                        taskManager.updateSubtask(subtask);
                        sendCreated(exchange, gson.toJson(subtask));
                    }
                }
                case "DELETE" -> {
                    if ("/subtasks".equals(path)) {
                        taskManager.deleteAllSubTasks();
                        sendSuccess(exchange, "All subtasks deleted");
                    } else if (path.startsWith("/subtasks/")) {
                        int id = Integer.parseInt(path.substring(10));
                        taskManager.deleteSubtask(id);
                        sendSuccess(exchange, "Subtask " + id + " deleted");
                    }
                }
                case null, default -> sendNotFound(exchange);
            }
        } catch (NumberFormatException e) {
            sendNotFound(exchange);
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }
}

