package managers;

import tasks.Task;

import java.util.List;

public interface HistoryManager {
    // Добавляет задачу в историю просмотров
    void add(Task task);

    // Удаляет задачу из истории просмотров по её id
    void remove(int id);

    // Возвращает список задач из истории просмотров
    List<Task> getHistory();
}