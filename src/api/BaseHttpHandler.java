package api;

import api.adapter.DurationTypeAdapter;
import api.adapter.LocalDateTimeTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.task.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

public abstract class BaseHttpHandler implements HttpHandler {
    protected final Gson gson;
    protected final TaskManager taskManager;

    public BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = createGsonWithJavaTimeSupport();
    }

    private Gson createGsonWithJavaTimeSupport() {
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        builder.registerTypeAdapter(Duration.class, new DurationTypeAdapter());
        builder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter());
        return builder.create();
    }

    @Override
    public final void handle(HttpExchange exchange) throws IOException {
        try {
            processRequest(exchange);
        } catch (Exception e) {
            sendInternalError(exchange, "Внутренняя ошибка сервера: " + e.getMessage());
        }
    }

    protected abstract void processRequest(HttpExchange exchange) throws IOException;

    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        try (var outputStream = exchange.getResponseBody()) {
            outputStream.write(response);
        }
        exchange.close();
    }

    protected void sendJson(HttpExchange exchange, Object obj, int statusCode) throws IOException {
        String json = gson.toJson(obj);
        sendText(exchange, json, statusCode);
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendJson(exchange, Map.of("error", "Ресурс не найден"), 404);
    }

    protected void sendHasInteractions(HttpExchange exchange, String message) throws IOException {
        sendJson(exchange, Map.of("error", message), 406);
    }

    protected void sendInternalError(HttpExchange exchange, String message) throws IOException {
        sendJson(exchange, Map.of("error", message), 500);
    }

    protected void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        sendJson(exchange, Map.of("error", message), 400);
    }

    protected void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        sendJson(exchange, Map.of("error", "Метод не поддерживается"), 405);
    }
}