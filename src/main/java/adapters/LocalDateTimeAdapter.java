package adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
 * LocalDateTimeAdapter: Адаптер для корректной работы Gson с LocalDateTime.
 * Использует ISO-8601 формат (например: "2023-10-05T12:30:45").
 */

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    private final DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            // Для ответа пользователю: "dd.MM.yyyy HH:mm"
            out.value(value.format(inputFormatter));
        }
    }

    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
        // Принимаем дату от пользователя: "15.10.2024 18:00"
        String dateStr = in.nextString();
        return LocalDateTime.parse(dateStr, inputFormatter);
    }
}
