package handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;

import managers.TaskManager;
import tasks.Epic;
import tasks.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public EpicsHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            System.out.println("\n=== New Epics Handler Request ===");
            System.out.println("Method: " + exchange.getRequestMethod());
            System.out.println("Path: " + exchange.getRequestURI().getPath());

            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET" -> {
                    if ("/epics".equals(path)) {
                        System.out.println("GET all epics");
                        sendSuccess(exchange, gson.toJson(taskManager.getAllEpics()));

                    } else if (path.startsWith("/epics/")) {
                        if (path.endsWith("/subtasks")) {
                            int epicId = Integer.parseInt(path.split("/")[2]);
                            System.out.println("GET subtasks for epic ID: " + epicId);

                            Epic epic = taskManager.getEpicById(epicId);
                            if (epic != null) {
                                List<Subtask> subs = epic.getSubtaskIds().stream()
                                        .map(taskManager::getSubTaskById)
                                        .toList();
                                sendSuccess(exchange, gson.toJson(subs));
                            } else {
                                System.out.println("Epic not found: " + epicId);
                                sendNotFound(exchange);
                            }
                        } else {
                            int id = Integer.parseInt(path.substring(7));
                            System.out.println("GET epic by ID: " + id);

                            Epic epic = taskManager.getEpicById(id);
                            if (epic != null) {
                                sendSuccess(exchange, gson.toJson(epic));
                            } else {
                                System.out.println("Epic not found: " + id);
                                sendNotFound(exchange);
                            }
                        }
                    }
                }
                case "POST" -> {
                    String body = new String(
                            exchange.getRequestBody().readAllBytes(),
                            StandardCharsets.UTF_8
                    );
                    System.out.println("POST body: " + body); // Логируем тело запроса

                    try {
                        Epic epic = gson.fromJson(body, Epic.class);
                        System.out.println("Parsed epic: " + epic);

                        if (epic.getId() == 0) {
                            Epic created = taskManager.createEpic(epic);
                            System.out.println("Created epic: " + created.getId());
                            sendCreated(exchange, gson.toJson(created));
                        } else {
                            taskManager.updateEpic(epic);
                            System.out.println("Updated epic: " + epic.getId());
                            sendCreated(exchange, gson.toJson(epic));
                        }
                    } catch (JsonSyntaxException e) {
                        System.out.println("JSON parsing error: " + e.getMessage());
                        throw e;
                    }
                }
                case "DELETE" -> {
                    if ("/epics".equals(path)) {
                        System.out.println("DELETE all epics");
                        taskManager.deleteAllEpics();
                        sendSuccess(exchange, "All epics deleted");

                    } else if (path.startsWith("/epics/")) {
                        int id = Integer.parseInt(path.substring(7));
                        System.out.println("DELETE epic ID: " + id);

                        int deletedId = taskManager.deleteEpic(id);
                        if (deletedId != -1) {
                            sendSuccess(exchange, "Epic " + deletedId + " deleted");
                        } else {
                            sendNotFound(exchange);
                        }
                    }
                }
                case null, default -> sendNotFound(exchange);
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format: " + e.getMessage());
            sendNotFound(exchange);
        } catch (JsonSyntaxException e) {
            System.out.println("JSON Syntax Error: " + e.getMessage());
            sendBadRequest(exchange);
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            sendInternalError(exchange);
        } finally {
            System.out.println("=== Request handling completed ===\n");
        }
    }
}

