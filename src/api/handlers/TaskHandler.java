package api.handlers;

import api.BaseHttpHandler;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.NotFoundException;
import exceptions.TaskOverlapException;
import manager.task.TaskManager;
import task.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public TaskHandler(TaskManager taskManager) {
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
        if (path.equals("/tasks")) {
            List<Task> tasks = taskManager.getTasks();
            sendJson(exchange, tasks, 200);
        } else if (path.startsWith("/tasks/")) {
            try {
                int id = Integer.parseInt(path.substring(7));
                Task task = taskManager.getTaskByID(id);
                sendJson(exchange, task, 200);
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
        try {
            if (path.equals("/tasks")) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                if (requestBody.isEmpty()) {
                    sendBadRequest(exchange, "Пустое тело запроса");
                    return;
                }

                Task task;
                try {
                    task = this.gson.fromJson(requestBody, Task.class);
                } catch (JsonSyntaxException e) {
                    sendBadRequest(exchange, "Ошибка синтаксиса JSON: " + e.getMessage());
                    return;
                }

                if (task == null) {
                    sendBadRequest(exchange, "Некорректный JSON");
                    return;
                }
                if (task.getName() == null || task.getName().trim().isEmpty()) {
                    sendBadRequest(exchange, "Имя задачи не может быть пустым");
                    return;
                }
                if (task.getDuration() != null && task.getDuration().isNegative()) {
                    sendBadRequest(exchange, "Длительность задачи не может быть отрицательной");
                    return;
                }

                try {
                    Task savedTask = taskManager.addTask(task);
                    sendJson(exchange, savedTask, 201);
                } catch (TaskOverlapException e) {
                    sendHasInteractions(exchange, e.getMessage());
                }
            } else if (path.startsWith("/tasks/")) {
                try {
                    int id = Integer.parseInt(path.substring(7));
                    String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    if (requestBody.isEmpty()) {
                        sendBadRequest(exchange, "Пустое тело запроса");
                        return;
                    }

                    Task task = this.gson.fromJson(requestBody, Task.class);

                    if (task.getId() != id) {
                        sendBadRequest(exchange, "ID в URL и в теле запроса не совпадают");
                        return;
                    }

                    Task updatedTask = taskManager.updateTask(task);
                    if (updatedTask != null) {
                        sendJson(exchange, task, 200);
                    } else {
                        sendNotFound(exchange);
                    }
                } catch (NumberFormatException e) {
                    sendBadRequest(exchange, "Неверный ID в URL");
                } catch (JsonSyntaxException e) {
                    sendBadRequest(exchange, "Ошибка JSON: " + e.getMessage());
                }
            } else {
                sendBadRequest(exchange, "Неверный URL для POST");
            }
        } catch (Exception e) {
            sendInternalError(exchange, "Ошибка обработки POST: " + e.getMessage());
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/tasks")) {
            taskManager.deleteTasks();
            sendJson(exchange, Map.of("status", "deleted"), 200);
        } else if (path.startsWith("/tasks/")) {
            try {
                int id = Integer.parseInt(path.substring(7));
                taskManager.deleteTaskByID(id);
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