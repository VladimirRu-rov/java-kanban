package api;

import api.adapter.DurationTypeAdapter;
import api.adapter.LocalDateTimeTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

public class BaseHttpHandler {
    protected final Gson gson;

    public BaseHttpHandler() {
        this.gson = createGsonWithJavaTimeSupport();
    }

    private Gson createGsonWithJavaTimeSupport() {
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        builder.registerTypeAdapter(Duration.class, new DurationTypeAdapter());
        builder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter());
        return builder.create();
    }

    protected void sendText(HttpExchange h, String text, int statusCode) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        h.sendResponseHeaders(statusCode, resp.length);
        try (var os = h.getResponseBody()) {
            os.write(resp);
        }
        h.close();
    }

    protected void sendJson(HttpExchange h, Object obj, int statusCode) throws IOException {
        String json = gson.toJson(obj);
        sendText(h, json, statusCode);
    }

    protected void sendNotFound(HttpExchange h) throws IOException {
        sendJson(h, Map.of("error", "Ресурс не найден"), 404);
    }

    protected void sendHasInteractions(HttpExchange h, String message) throws IOException {
        sendJson(h, Map.of("error", message), 406);
    }

    protected void sendInternalError(HttpExchange h, String message) throws IOException {
        sendJson(h, Map.of("error", message), 500);
    }

    protected void sendBadRequest(HttpExchange h, String message) throws IOException {
        sendJson(h, Map.of("error", message), 400);
    }

    protected void sendMethodNotAllowed(HttpExchange h) throws IOException {
        sendJson(h, Map.of("error", "Метод не поддерживается"), 405);
    }
}