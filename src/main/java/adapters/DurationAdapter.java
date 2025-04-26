package adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

/*
 * DurationAdapter: Кастомный сериализатор/десериализатор для работы с Duration через Gson.
 * Решает проблему преобразования между Duration и строковым представлением (ISO-8601).
 */

public class DurationAdapter extends TypeAdapter<Duration> {
    @Override
    public void write(JsonWriter out, Duration duration) throws IOException {
        if (duration == null) {
            out.nullValue();
            return;
        }
        // Для ответа пользователю: "X ч. Y мин."
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        out.value(hours + " ч. " + minutes + " мин.");
    }

    @Override
    public Duration read(JsonReader in) throws IOException {
        // Принимаем от пользователя: "0 ч. 30 мин."
        String value = in.nextString();
        String[] parts = value.split(" ");
        long hours = Long.parseLong(parts[0]);
        long minutes = Long.parseLong(parts[2]);
        return Duration.ofHours(hours).plusMinutes(minutes);
    }
}
