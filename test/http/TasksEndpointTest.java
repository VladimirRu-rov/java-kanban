package http;

import com.google.gson.JsonParseException;
import org.junit.jupiter.api.Test;
import task.Status;
import task.Task;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TasksEndpointTest extends HttpTaskServerTestBase {

    @Test
    public void testAddTask_Success() throws Exception {
        initGson();
        Task task = new Task("Задача", "Описание", Duration.ofMinutes(30), LocalDateTime.now());
        String json = gson.toJson(task);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Статус ответа должен быть 201 Created");

        try {
            Task savedTask = gson.fromJson(response.body(), Task.class);
            assertEquals("Задача", savedTask.getName(), "Имя задачи должно совпадать");
            assertEquals(1, manager.getTasks().size(), "В хранилище должна быть 1 задача");
        } catch (JsonParseException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testAddTask_InvalidDuration_Negative() throws Exception {
        initGson();

        LocalDateTime now = LocalDateTime.now();
        Task task = new Task("Неправильная задача", "Описание", Duration.ofMinutes(-10), now);

        String json = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode(), "Отрицательная длительность должна вызывать ошибку 400");
    }

    @Test
    public void testGetTaskById_NotFound() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "При запросе несуществующей задачи должен возвращаться 404");
    }

    @Test
    public void testAddTask_EmptyBody() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode(), "Пустое тело должно возвращать 400");

        String contentType = response.headers().firstValue("Content-Type").orElse("");
        assertTrue(contentType.startsWith("application/json"));
    }

    @Test
    public void testAddTask_InvalidJson() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":}"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode(), "Невалидный JSON должен возвращать 400");
    }

    @Test
    public void testAddTask_MissingName() throws Exception {
        initGson();

        Task task = new Task("", "Описание", Duration.ofMinutes(10), LocalDateTime.now());
        String json = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode(), "Отсутствие name должно возвращать 400");
    }

    @Test
    public void testAddTask_Overlap_Returns406() throws Exception {
        initGson();

        // Фиксированное время для первой задачи
        LocalDateTime start1 = LocalDateTime.of(2025, 11, 15, 10, 0); // 15.11.2025 10:00
        Task t1 = new Task("Задача 1", "Описание", Duration.ofHours(2), start1);
        manager.addTask(t1);

        // Вторая задача пересекается: начинается в 10:30, а первая идёт до 12:00
        LocalDateTime start2 = LocalDateTime.of(2025, 11, 15, 10, 30);
        Task t2 = new Task("Задача 2", "Описание", Duration.ofMinutes(45), start2);
        String json = gson.toJson(t2);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode(), "Пересечение должно возвращать 406");

        String contentType = response.headers().firstValue("Content-Type").orElse("");
        assertTrue(contentType.startsWith("application/json"));
    }

    @Test
    public void testUpdateTask_Success() throws Exception {
        initGson();

        LocalDateTime start = LocalDateTime.of(2025, 11, 15, 8, 0); // 15.11.2025 08:00
        Task task = manager.addTask(new Task("старая задача", "Описание", Duration.ofMinutes(10), start));

        // Новое время — гарантированно вне пересечения (например, следующий день)
        LocalDateTime newStartTime = LocalDateTime.of(2025, 11, 16, 9, 0); // 16.11.2025 09:00
        Task updatedTask = new Task(task.getId(), "Обновленное имя", "Новое описание", Status.IN_PROGRESS,
                Duration.ofMinutes(20), newStartTime);

        String json = gson.toJson(updatedTask);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + task.getId()))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Обновление должно возвращать 200");

        Task result = gson.fromJson(response.body(), Task.class);
        assertEquals("Обновленное имя", result.getName());
        assertEquals(Status.IN_PROGRESS, result.getStatus());
        assertEquals(newStartTime, result.getStartTime());
    }

    @Test
    public void testUpdateTask_NotFound() throws Exception {
        initGson();

        Task task = new Task(999, "Name", "Desc", Status.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        String json = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/999"))
                .POST(HttpRequest.BodyPublishers.ofString(json))  // POST вместо PUT
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }
}
