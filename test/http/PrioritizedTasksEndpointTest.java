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

public class PrioritizedTasksEndpointTest extends HttpTaskServerTestBase {

    @Test
    public void testGetPrioritizedTasks_NoOverlap() throws Exception {
        initGson();
        Task t1 = manager.addTask(new Task("Задача 1", "Описание", Duration.ofMinutes(30), LocalDateTime.now()));
        Task t2 = manager.addTask(new Task("Задача 2", "Описание", Duration.ofHours(1), LocalDateTime.now().plusHours(2)));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> prioritized = gson.fromJson(response.body(),
                new TypeToken<List<Task>>() {
                }.getType());

        assertEquals(2, prioritized.size());
        assertEquals(t1.getId(), prioritized.get(0).getId());
        assertEquals(t2.getId(), prioritized.get(1).getId());

        String contentType = response.headers().firstValue("Content-Type").orElse("");
        assertTrue(contentType.startsWith("application/json"));

        List<Task> managerPrioritized = manager.getPrioritizedTasks();
        assertEquals(prioritized.size(), managerPrioritized.size(),
                "Количество задач в ответе и в менеджере должно совпадать");

        for (int i = 0; i < prioritized.size(); i++) {
            Task responseTask = prioritized.get(i);
            Task managerTask = managerPrioritized.get(i);
            assertEquals(responseTask.getId(), managerTask.getId(),
                    "ID задачи в ответе должен совпадать с ID в менеджере на позиции " + i);
            assertEquals(responseTask.getName(), managerTask.getName(),
                    "Имя задачи в ответе должно совпадать с именем в менеджере на позиции " + i);
        }
    }
}