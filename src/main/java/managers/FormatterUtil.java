package managers;

import tasks.*;

public class FormatterUtil {

    /**
     * //     * Создает задачу из строки CSV.
     * //     *
     * //     * @param value Строка CSV, содержащая данные задачи.
     * //     * @return Задача, созданная из строки.
     * //
     */
    static Task fromString(String value) {
        // Разделяем строку на поля
        String[] fields = value.split(",", 6); // Указываем limit=6, чтобы избежать лишних разделений
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String title = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4].isEmpty() ? "" : fields[4]; // Пустое описание
        int epicId = fields.length > 5 && !fields[5].isEmpty() ? Integer.parseInt(fields[5]) : -1;

        // В зависимости от типа задачи создаем соответствующий объект
        switch (type) {
            case TASK:
                Task task = new Task(title, description);
                task.setId(id);
                task.setStatus(status);
                return task;
            case EPIC:
                Epic epic = new Epic(title, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                Subtask subtask = new Subtask(title, description, epicId);
                subtask.setId(id);
                subtask.setStatus(status);
                return subtask;
            default:
                return null;
        }
    }

    /**
     * Преобразует задачу в строку CSV.
     *
     * @param task Задача, которую нужно преобразовать.
     * @return Строка в формате CSV.
     */
    static String toString(Task task) {
        // Определяем тип задачи
        String type = task instanceof Epic ? TaskType.EPIC.name() :
                task instanceof Subtask ? TaskType.SUBTASK.name() : TaskType.TASK.name();

        // Получаем ID эпика для подзадачи (если это подзадача)
        String epicId = task instanceof Subtask ? String.valueOf(((Subtask) task).getEpicId()) : "";

        // Собираем строку CSV
        return String.join(",",
                String.valueOf(task.getId()),
                type,
                task.getTitle(),
                task.getStatus().name(),
                task.getDescription(),
                epicId);
    }

}
