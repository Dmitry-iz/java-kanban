package managers;

import tasks.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Утилитарный класс для преобразования задач в строковый формат (сериализация)
 * и обратного преобразования из строки в объекты задач (десериализация).
 * Используется для сохранения и загрузки задач в файл.
 */
public class FormatterUtil {
    // Форматтер для даты и времени (ISO 8601)
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Преобразует строку в объект задачи.
     * Формат строки: id,type,name,status,description,epic,start_time,duration
     *
     * @param value строка с данными задачи
     * @return объект задачи (Task, Subtask или Epic)
     */
    static Task fromString(String value) {
        // Разбиваем строку на части по запятым (максимум 8 частей)
        String[] fields = value.split(",", 8);

        // Парсим основные поля
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String title = fields[2];
        Status status = Status.valueOf(fields[3]);
        // Описание может быть пустым
        String description = fields[4].isEmpty() ? "" : fields[4];

        // Парсим временные параметры
        LocalDateTime startTime = parseDateTime(fields[6]);
        Duration duration = parseDuration(fields[7]);

        // Создаем объект задачи в зависимости от типа
        switch (type) {
            case TASK:
                Task task = new Task(title, description);
                task.setId(id);
                task.setStatus(status);
                task.setStartTime(startTime);
                task.setDuration(duration);
                return task;
            case EPIC:
                // Для эпика не парсим время, так как оно рассчитывается на основе подзадач
                Epic epic = new Epic(title, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                // Для подзадачи парсим ID эпика
                int epicId = Integer.parseInt(fields[5]);
                Subtask subtask = new Subtask(title, description, epicId);
                subtask.setId(id);
                subtask.setStatus(status);
                subtask.setStartTime(startTime);
                subtask.setDuration(duration);
                return subtask;
            default:
                return null; // На случай добавления новых типов
        }
    }

    /**
     * Преобразует задачу в строку для сохранения.
     *
     * @param task задача для сериализации
     * @return строка в формате: id,type,name,status,description,epic,start_time,duration
     */
    static String toString(Task task) {
        // Определяем тип задачи
        String type = task instanceof Epic ? TaskType.EPIC.name() :
                task instanceof Subtask ? TaskType.SUBTASK.name() : TaskType.TASK.name();

        // Для подзадачи получаем ID эпика, для остальных - пустую строку
        String epicId = task instanceof Subtask ?
                String.valueOf(((Subtask) task).getEpicId()) : "";

        // Форматируем время начала (если задано)
        String startTime = task.getStartTime() != null ?
                task.getStartTime().format(DATE_FORMATTER) : "";

        // Получаем продолжительность в минутах (если задана)
        String duration = task.getDuration() != null ?
                String.valueOf(task.getDuration().toMinutes()) : "";

        // Собираем все поля в строку через запятую
        return String.join(",",
                String.valueOf(task.getId()),
                type,
                task.getTitle(),
                task.getStatus().name(),
                task.getDescription(),
                epicId,
                startTime,
                duration);
    }

    /**
     * Парсит строку с датой и временем.
     *
     * @param value строка с датой в формате ISO_LOCAL_DATE_TIME
     * @return LocalDateTime или null, если строка пустая
     */
    private static LocalDateTime parseDateTime(String value) {
        return value.isEmpty() ? null : LocalDateTime.parse(value, DATE_FORMATTER);
    }

    /**
     * Парсит строку с продолжительностью.
     *
     * @param value строка с количеством минут
     * @return Duration или null, если строка пустая
     */
    private static Duration parseDuration(String value) {
        return value.isEmpty() ? null : Duration.ofMinutes(Long.parseLong(value));
    }
}
