package api.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

public class DurationTypeAdapter extends TypeAdapter<Duration> {
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
        if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        String value = in.nextString();
        try {
            long seconds = Long.parseLong(value);
            return Duration.ofSeconds(seconds);
        } catch (NumberFormatException e) {
            try {
                return Duration.parse(value);
            } catch (Exception ex) {
                throw new com.google.gson.JsonParseException(
                        "Неверный формат Duration: ожидалось число секунд или строка в формате ISO-8601 (PT30M)", ex);
            }
        }
    }
}

