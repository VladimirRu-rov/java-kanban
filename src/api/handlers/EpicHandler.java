package api.handlers;

import api.BaseHttpHandler;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.NotFoundException;
import manager.task.TaskManager;
import task.Epic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public EpicHandler(TaskManager taskManager) {
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
        if (path.equals("/epics")) {
            List<Epic> epics = taskManager.getEpics();
            sendJson(exchange, epics, 200);
        } else if (path.startsWith("/epics/")) {
            try {
                int id = Integer.parseInt(path.substring(7));
                Epic epic = taskManager.getEpicByID(id);
                if (epic == null) {
                    sendNotFound(exchange);
                    return;
                }
                sendJson(exchange, epic, 200);
            } catch (NotFoundException e) {
                sendNotFound(exchange);
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Неверный ID");
            }
        } else {
            sendBadRequest(exchange, "Неверный URL");
        }
    }

    protected void handlePost(HttpExchange exchange, String path) throws IOException {
        if (!path.equals("/epics")) {
            sendBadRequest(exchange, "Неверный URL для POST. Используйте /epics");
            return;
        }

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (requestBody.isEmpty()) {
            sendBadRequest(exchange, "Пустое тело запроса");
            return;
        }

        try {
            Epic epic = this.gson.fromJson(requestBody, Epic.class);
            if (epic == null) {
                sendBadRequest(exchange, "Некорректный JSON");
                return;
            }
            if (epic.getName() == null || epic.getName().trim().isEmpty()) {
                sendBadRequest(exchange, "Имя эпика не может быть пустым");
                return;
            }

            if (epic.getId() > 0) {
                Epic existingEpic = taskManager.getEpicByID(epic.getId());
                if (existingEpic != null) {
                    Epic updatedEpic = taskManager.updateEpic(epic);
                    sendJson(exchange, updatedEpic, 200);
                    return;
                } else {
                    sendNotFound(exchange);
                    return;
                }
            }

            Epic savedEpic = taskManager.addEpic(epic);
            sendJson(exchange, savedEpic, 201);

        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Ошибка синтаксиса JSON: " + e.getMessage());
        } catch (Exception e) {
            sendInternalError(exchange, "Ошибка при обработке запроса: " + e.getMessage());
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/epics")) {
            taskManager.deleteEpics();
            sendJson(exchange, Map.of("status", "deleted"), 200);
        } else if (path.startsWith("/epics/")) {
            try {
                int id = Integer.parseInt(path.substring(7));
                taskManager.deleteEpicByID(id);
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

