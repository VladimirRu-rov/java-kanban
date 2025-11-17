package api.handlers;

import api.BaseHttpHandler;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import exceptions.NotFoundException;
import manager.task.TaskManager;
import task.Epic;
import task.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class EpicHandler extends BaseHttpHandler {

    public EpicHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void processRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        switch (method) {
            case "GET" -> handleGet(exchange, path);
            case "POST" -> handlePost(exchange, path);
            case "DELETE" -> handleDelete(exchange, path);
            default -> sendMethodNotAllowed(exchange);
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/epics")) {
            List<Epic> epics = taskManager.getEpics();
            sendJson(exchange, epics, 200);
        } else if (path.startsWith("/epics/") && !path.contains("/subtasks")) {
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
        } else if (path.matches("/epics/\\d+/subtasks")) {
            try {
                String[] parts = path.split("/");
                int epicId = Integer.parseInt(parts[2]);

                Epic epic = taskManager.getEpicByID(epicId);
                if (epic == null) {
                    sendNotFound(exchange);
                    return;
                }

                List<Subtask> subtasks = taskManager.getEpicSubtasks(epic);
                sendJson(exchange, subtasks, 200);
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Неверный ID эпика");
            } catch (NotFoundException e) {
                sendNotFound(exchange);
            }
        } else {
            sendBadRequest(exchange, "Неверный URL");
        }
    }

    private void handlePost(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/epics")) {
            // Создание (как было)
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if (requestBody.isEmpty()) {
                sendBadRequest(exchange, "Пустое тело запроса");
                return;
            }

            try {
                Epic epic = gson.fromJson(requestBody, Epic.class);
                if (epic == null) {
                    sendBadRequest(exchange, "Некорректный JSON");
                    return;
                }
                if (epic.getName() == null || epic.getName().trim().isEmpty()) {
                    sendBadRequest(exchange, "Имя эпика не может быть пустым");
                    return;
                }

                Epic savedEpic = taskManager.addEpic(epic);
                sendJson(exchange, savedEpic, 201);

            } catch (JsonSyntaxException e) {
                sendBadRequest(exchange, "Ошибка синтаксиса JSON: " + e.getMessage());
            } catch (Exception e) {
                sendInternalError(exchange, "Ошибка при обработке запроса: " + e.getMessage());
            }
        } else if (path.startsWith("/epics/")) {
            try {
                int id = Integer.parseInt(path.substring(7)); // Извлекаем id из URL
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

                if (requestBody.isEmpty()) {
                    sendBadRequest(exchange, "Пустое тело запроса");
                    return;
                }

                Epic epic = gson.fromJson(requestBody, Epic.class);
                if (epic == null) {
                    sendBadRequest(exchange, "Некорректный JSON");
                    return;
                }

                epic.setId(id);

                Epic updatedEpic = taskManager.updateEpic(epic);
                if (updatedEpic != null) {
                    sendJson(exchange, updatedEpic, 200);
                } else {
                    sendNotFound(exchange);
                }
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Неверный ID в URL. Должно быть число");
            } catch (JsonSyntaxException e) {
                sendBadRequest(exchange, "Ошибка JSON: " + e.getMessage());
            } catch (NotFoundException e) {
                sendNotFound(exchange);
            } catch (Exception e) {
                sendInternalError(exchange, "Ошибка при обновлении эпика: " + e.getMessage());
            }
        } else {
            sendBadRequest(exchange, "Неверный URL для POST. Используйте /epics или /epics/{id}");
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

