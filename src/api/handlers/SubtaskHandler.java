package api.handlers;

import api.BaseHttpHandler;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import exceptions.NotFoundException;
import manager.task.TaskManager;
import task.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class SubtaskHandler extends BaseHttpHandler {

    public SubtaskHandler(TaskManager taskManager) {
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
        if (path.equals("/subtasks")) {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if (requestBody.isEmpty()) {
                sendBadRequest(exchange, "Пустое тело запроса");
                return;
            }

            try {
                Subtask subtask = gson.fromJson(requestBody, Subtask.class);
                if (subtask == null) {
                    sendBadRequest(exchange, "Некорректный JSON");
                    return;
                }

                if (subtask.getName() == null || subtask.getStartTime() == null) {
                    sendBadRequest(exchange, "Обязательные поля: name, startTime");
                    return;
                }
                if (subtask.getEpicId() <= 0) {
                    sendBadRequest(exchange, "Поле epicId обязательно");
                    return;
                }

                Subtask created = taskManager.addSubTask(subtask);
                if (created == null) {
                    sendInternalError(exchange, "Не удалось создать подзадачу");
                    return;
                }
                sendJson(exchange, Map.of("status", "created", "id", created.getId()), 201);
            } catch (JsonSyntaxException e) {
                sendBadRequest(exchange, "Некорректный JSON: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                sendBadRequest(exchange, e.getMessage());
            } catch (Exception e) {
                sendInternalError(exchange, "Внутренняя ошибка");
            }
        } else if (path.startsWith("/subtasks/")) {
            try {
                int id = Integer.parseInt(path.substring(10));
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

                if (requestBody.isEmpty()) {
                    sendBadRequest(exchange, "Пустое тело запроса");
                    return;
                }

                Subtask subtask = gson.fromJson(requestBody, Subtask.class);
                if (subtask == null) {
                    sendBadRequest(exchange, "Некорректный JSON");
                    return;
                }

                subtask.setId(id);

                Subtask updated = taskManager.updateSubtask(subtask);
                if (updated == null) {
                    sendNotFound(exchange);
                } else {
                    sendJson(exchange, Map.of("status", "updated", "id", updated.getId()), 200);
                }
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Неверный ID в URL. Должно быть число");
            } catch (JsonSyntaxException e) {
                sendBadRequest(exchange, "Ошибка JSON: " + e.getMessage());
            } catch (Exception e) {
                sendInternalError(exchange, "Ошибка при обновлении подзадачи: " + e.getMessage());
            }
        } else {
            sendBadRequest(exchange, "Неверный URL для POST. Используйте /subtasks или /subtasks/{id}");
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
