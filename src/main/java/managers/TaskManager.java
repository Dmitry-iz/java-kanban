package managers;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.List;

public interface TaskManager {
    Task createTask(Task task);

    Subtask createSubtask(Subtask subtask);

    Epic createEpic(Epic epic);

    Task getTaskById(int id);

    Epic getEpicById(int id);

    Subtask getSubTaskById(int id);

    List<Task> getAllTasks();

    List<Subtask> getAllSubtasks();

    List<Epic> getAllEpics();

    List<Task> getHistory();

    void updateTask(Task task);

    void updateEpicStatus(int epicId);

    void updateSubtask(Subtask subtask);

    void updateEpic(Epic epic);

    int deleteTask(int id);

    void deleteAllTasks();

    void deleteAllSubTasks();

    void deleteAllEpics();

    void deleteSubtask(int id);

    int deleteEpic(int id);
}
