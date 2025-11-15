package api.handlers;

import api.BaseHttpHandler;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.NotFoundException;
import manager.task.TaskManager;
import task.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public SubtaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET" -> handleGet(exchange, path);
                case "POST" -> handlePost(exchange, path);
                case "DELETE" -> handleDelete(exchange, path);
                default -> sendMethodNotAllowed(exchange);
            }
        } catch (Exception e) {
            sendInternalError(exchange, "Внутренняя ошибка сервера: " + e.getMessage());
        } finally {
            exchange.close();
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/subtasks")) {
            List<Subtask> subtasks = taskManager.getSubtasks();
            sendJson(exchange, subtasks, 200);
        } else if (path.startsWith("/subtasks/")) {
            try {
                int id = Integer.parseInt(path.substring(10));
                Subtask subtask = taskManager.getSubtaskByID(id);

                if (subtask == null) {
                    sendNotFound(exchange);
                    return;
                }

                sendJson(exchange, subtask, 200);
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Неверный ID");
            }
        } else {
            sendBadRequest(exchange, "Неверный URL");
        }
    }

    private void handlePost(HttpExchange exchange, String path) throws IOException {
        if (!path.equals("/subtasks")) {
            sendBadRequest(exchange, "Неверный URL для POST");
            return;
        }

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (requestBody.isEmpty()) {
            sendBadRequest(exchange, "Пустое тело запроса");
            return;
        }

        try {
            Subtask subtask = this.gson.fromJson(requestBody, Subtask.class);
            if (subtask == null) {
                sendBadRequest(exchange, "Некорректный JSON");
                return;
            }
            if (subtask.getName() == null || subtask.getStartTime() == null) {
                sendBadRequest(exchange, "Обязательные поля: name, startTime");
                return;
            }
            taskManager.addSubTask(subtask);
            sendJson(exchange, Map.of("status", "created", "id", subtask.getId()), 201);
        } catch (IllegalArgumentException e) {
            sendBadRequest(exchange, e.getMessage());
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Некорректный JSON: " + e.getMessage());
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/subtasks")) {
            taskManager.deleteSubtasks();
            sendJson(exchange, Map.of("status", "deleted"), 200);
        } else if (path.startsWith("/subtasks/")) {
            try {
                int id = Integer.parseInt(path.substring(10));
                taskManager.deleteSubtaskByID(id);
                sendJson(exchange, Map.of("status", "deleted"), 200);
            } catch (NotFoundException e) {
                sendNotFound(exchange);
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Неверный ID");
            }
        } else {
            sendBadRequest(exchange, "Неверный URL для DELETE");
        }
    }
}
