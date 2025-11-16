package http;

import org.junit.jupiter.api.Test;
import task.Epic;
import task.Status;
import task.Subtask;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class EpicsEndpointTest extends HttpTaskServerTestBase {

    @Test
    public void testAddEpic_Success() throws Exception {
        initGson();
        Epic epic = new Epic(0, "Новый эпик", "Описание", Status.NEW, null, null);
        String json = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Статус должен быть 201 Created");

        Epic savedEpic = gson.fromJson(response.body(), Epic.class);
        assertNotNull(savedEpic.getId(), "ID должен быть назначен сервером");
        assertEquals("Новый эпик", savedEpic.getName());
    }

    @Test
    public void testAddEpic_EmptyBody() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode(), "Пустое тело должно возвращать 400");

        String contentType = response.headers().firstValue("Content-Type").orElse("");
        assertTrue(contentType.startsWith("application/json"));
    }

    @Test
    public void testAddEpic_InvalidJson() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":}"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode(), "Невалидный JSON должен возвращать 400");
    }

    @Test
    public void testGetEpicById_NotFound() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Несуществующий эпик — 404");

        String contentType = response.headers().firstValue("Content-Type").orElse("");
        assertTrue(contentType.startsWith("application/json"));
    }

    @Test
    public void testUpdateEpic_Success() throws Exception {
        initGson();
        Epic epic = manager.addEpic(new Epic(0, "Старое имя", "Описание", Status.NEW, null, null));
        Subtask subtask = new Subtask(0, "Subtask", "Описание", Status.IN_PROGRESS, Duration.ofMinutes(10), LocalDateTime.now(), epic.getId());
        manager.addSubTask(subtask);

        Epic updatedEpic = new Epic(epic.getId(), "Новое имя", "Новое описание", Status.IN_PROGRESS, null, null);
        String json = gson.toJson(updatedEpic);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epic.getId()))  // ← Добавляем ID!
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Обновление должно возвращать 200");

        Epic result = gson.fromJson(response.body(), Epic.class);
        assertEquals("Новое имя", result.getName());
        assertEquals(Status.IN_PROGRESS, result.getStatus());
    }

    @Test
    public void testDeleteEpic_Success() throws Exception {
        Epic epic = manager.addEpic(new Epic(0, "Удалить", "Описание", Status.NEW, null, null));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epic.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertNull(manager.getEpicByID(epic.getId()));
    }
}