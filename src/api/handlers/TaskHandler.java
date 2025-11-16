package api.handlers;

import api.BaseHttpHandler;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import exceptions.NotFoundException;
import exceptions.TaskOverlapException;
import manager.task.TaskManager;
import task.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class TaskHandler extends BaseHttpHandler {

    public TaskHandler(TaskManager taskManager) {
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

    private void handlePost(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/tasks")) {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if (requestBody.isEmpty()) {
                sendBadRequest(exchange, "Пустое тело запроса");
                return;
            }

            try {
                Task task = gson.fromJson(requestBody, Task.class);
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
            } catch (JsonSyntaxException e) {
                sendBadRequest(exchange, "Ошибка синтаксиса JSON: " + e.getMessage());
            }
        } else if (path.startsWith("/tasks/")) {
            try {
                int id = Integer.parseInt(path.substring(7));
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                if (requestBody.isEmpty()) {
                    sendBadRequest(exchange, "Пустое тело запроса");
                    return;
                }

                Task task = gson.fromJson(requestBody, Task.class);

                task.setId(id);

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
            sendBadRequest(exchange, "Неверный URL для POST. Используйте /tasks или /tasks/{id}");
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
