package http;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import task.Task;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HistoryEndpointTest extends HttpTaskServerTestBase {

    @Test
    public void testGetHistory_OrderAndNoDuplicates() throws Exception {
        initGson();
        Task t1 = manager.addTask(new Task("Задача 1", "Описание", Duration.ofMinutes(10), LocalDateTime.now()));
        Task t2 = manager.addTask(new Task("Задача 2", "Описание", Duration.ofHours(1), LocalDateTime.now().plusHours(1)));

        HttpClient client = HttpClient.newHttpClient();

        client.send(HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + t2.getId()))
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString());

        Thread.sleep(100);
        client.send(HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + t1.getId()))
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString());

        Thread.sleep(100);
        client.send(HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + t2.getId()))
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString());

        HttpRequest historyRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(historyRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> history = gson.fromJson(response.body(),
                new TypeToken<List<Task>>() {
                }.getType());

        assertEquals(2, history.size(), "В истории должно быть 2 уникальные задачи");
        assertEquals(t2.getId(), history.get(0).getId(), "Последняя запрошенная задача — первая в истории");
        assertEquals(t1.getId(), history.get(1).getId(), "Ранее запрошенная задача — вторая");

        String contentType = response.headers().firstValue("Content-Type").orElse("");
        assertTrue(contentType.startsWith("application/json"));
    }

    @Test
    public void testClearHistory() throws Exception {
        Task task = manager.addTask(new Task("Задача", "Описание", Duration.ZERO, LocalDateTime.now()));
        manager.getTaskByID(task.getId());

        assertEquals(1, manager.getHistory().size());

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertTrue(manager.getHistory().isEmpty(), "История должна быть очищена");

        String contentType = response.headers().firstValue("Content-Type").orElse("");
        assertTrue(contentType.startsWith("application/json"));
    }

    @Test
    public void testHistory_AfterTaskDeletion() throws Exception {
        initGson();
        Task task = manager.addTask(new Task("Удалить задачу", "Описание", Duration.ZERO, LocalDateTime.now()));
        manager.getTaskByID(task.getId());

        manager.deleteTaskByID(task.getId());

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> history = gson.fromJson(response.body(),
                new TypeToken<List<Task>>() {
                }.getType());
        assertTrue(history.isEmpty(), "После удаления задачи история не должна её содержать");

        String contentType = response.headers().firstValue("Content-Type").orElse("");
        assertTrue(contentType.startsWith("application/json"));
    }

    @Test
    public void testGetHistory_Empty() throws Exception {
        initGson();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> history = gson.fromJson(response.body(),
                new TypeToken<List<Task>>() {
                }.getType());
        assertTrue(history.isEmpty(), "При пустой истории должен возвращаться пустой массив");

        String contentType = response.headers().firstValue("Content-Type").orElse("");
        assertTrue(contentType.startsWith("application/json"));
    }
}