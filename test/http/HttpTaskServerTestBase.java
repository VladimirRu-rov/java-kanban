package http;

import api.HttpTaskServer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import manager.task.InMemoryTaskManager;
import manager.task.TaskManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServerTestBase {
    protected static TaskManager manager;
    protected static HttpTaskServer server;
    protected Gson gson;

    @BeforeAll
    public static void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        server.start();
    }

    @AfterAll
    public static void shutDown() {
        if (server != null) {
            try {
                server.stop();
                Thread.sleep(1000);
            } catch (Exception e) {
                System.err.println("Ошибка при остановке сервера: " + e.getMessage());
            }
        }
    }

    protected void initGson() {
        gson = new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .create();
    }

    private static class DurationTypeAdapter extends TypeAdapter<Duration> {
        @Override
        public void write(JsonWriter out, Duration value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.getSeconds());
            }
        }

        @Override
        public Duration read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            try {
                long seconds = in.nextLong();
                return Duration.ofSeconds(seconds);
            } catch (NumberFormatException e) {
                throw new JsonParseException("Неверный формат Duration: ожидалось число секунд", e);
            }
        }
    }

    private static class LocalDateTimeTypeAdapter extends TypeAdapter<LocalDateTime> {
        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.toString());
            }
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            try {
                return LocalDateTime.parse(in.nextString());
            } catch (Exception e) {
                throw new JsonParseException("Неверный формат LocalDateTime: " + in.nextString(), e);
            }
        }
    }
}