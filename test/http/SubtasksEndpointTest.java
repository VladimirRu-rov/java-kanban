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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SubtasksEndpointTest extends HttpTaskServerTestBase {

    @Test
    public void testAddSubtask_ToNonExistingEpic() throws Exception {
        initGson();
        Subtask subtask = new Subtask(0, "Неправильный Subtask", "Описание", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now(), 999);
        String json = gson.toJson(subtask);


        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode(),
                "Ожидался статус 400 (Bad Request) при несуществующем epicId");

        List<Subtask> allSubtasks = manager.getSubtasks();
        assertFalse(allSubtasks.stream().anyMatch(s -> s.getName().equals("Неправильный Subtask")),
                "Подзадача с некорректным epicId не должна быть добавлена");
    }

    @Test
    public void testAddSubtask_MissingFields() throws Exception {
        initGson();
        Subtask subtask = new Subtask(0, "ПодПодЗадача", "Описание", Status.NEW, Duration.ZERO, null, 1);
        String json = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode(), "Некорректные поля должны возвращать 400");


        String contentType = response.headers().firstValue("Content-Type").orElse("");
        assertTrue(contentType.startsWith("application/json"));
    }

    @Test
    public void testGetSubtask_ById() throws Exception {
        initGson();
        Epic epic = manager.addEpic(new Epic(0, "Эпик", "Описание", Status.NEW, null, null));
        Subtask subtask = new Subtask(0, "подподзадача", "Описание", Status.NEW,
                Duration.ofMinutes(20), LocalDateTime.now(), epic.getId());
        subtask = manager.addSubTask(subtask);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtask.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Subtask result = gson.fromJson(response.body(), Subtask.class);
        assertEquals(subtask.getId(), result.getId());
        assertEquals("подподзадача", result.getName());

        Subtask storedSubtask = manager.getSubtaskByID(subtask.getId());
        assertNotNull(storedSubtask, "Подзадача должна быть в хранилище");
        assertEquals(subtask.getId(), storedSubtask.getId());
        assertEquals("подподзадача", storedSubtask.getName());
    }

    @Test
    public void testGetSubtask_NotFound() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Несуществующая подзадача — 404");
        String contentType = response.headers().firstValue("Content-Type").orElse("");
        assertTrue(contentType.startsWith("application/json"));
    }

    @Test
    public void testDeleteSubtask_Success() throws Exception {
        initGson();
        Epic epic = manager.addEpic(new Epic(0, "Эпик", "Описание", Status.NEW, null, null));
        Subtask subtask = new Subtask(0, "Для удаления", "Описание", Status.NEW,
                Duration.ofMinutes(15), LocalDateTime.now(), epic.getId());
        subtask = manager.addSubTask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtask.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Удаление должно возвращать 200 No Content");

        Subtask deleted = manager.getSubtaskByID(subtask.getId());
        assertNull(deleted, "Подзадача не должна быть доступна после удаления");

        List<Subtask> allSubtasks = manager.getSubtasks();
        assertFalse(allSubtasks.contains(subtask), "Подзадача не должна присутствовать в списке всех подзадач");
    }
}