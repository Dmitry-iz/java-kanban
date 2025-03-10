package managers;

import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    // Хранилище задач, подзадач и эпиков
    private Map<Integer, Task> tasks = new HashMap<>();
    private Map<Integer, Subtask> subtasks = new HashMap<>();
    private Map<Integer, Epic> epics = new HashMap<>();

    // Менеджер истории для отслеживания просмотренных задач
    private HistoryManager historyManager = Managers.getDefaultHistory();

    // Счетчики для генерации уникальных id
    private int idCounter = 1;

    @Override
    public Task createTask(Task task) {
        task.setId(++idCounter); // Увеличиваем счетчик и устанавливаем уникальный id
        tasks.put(task.getId(), task); // Добавляем задачу в хранилище
        return task;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (epics.containsKey(subtask.getEpicId())) {
            if (subtask.getEpicId() == subtask.getId()) {
                return null; // Подзадача не может быть своим эпиком
            }
            subtask.setId(++idCounter); // Увеличиваем счетчик и устанавливаем уникальный id
            subtasks.put(subtask.getId(), subtask); // Добавляем подзадачу в хранилище
            epics.get(subtask.getEpicId()).addSubtask(subtask.getId()); // Добавляем id подзадачи в эпик
            updateEpicStatus(subtask.getEpicId()); // Обновляем статус эпика
            return subtask;
        }
        return null; // Если эпик не найден
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(++idCounter); // Увеличиваем счетчик и устанавливаем уникальный id
        epics.put(epic.getId(), epic); // Добавляем эпик в хранилище
        return epic;
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(new Task(task)); // Добавляем клон задачи в историю
        }
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(new Epic(epic)); // Добавляем клон эпика в историю
        }
        return epic;
    }

    @Override
    public Subtask getSubTaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(new Subtask(subtask)); // Добавляем клон подзадачи в историю
        }
        return subtask;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values()); // Возвращаем список всех задач
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values()); // Возвращаем список всех подзадач
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values()); // Возвращаем список всех эпиков
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory(); // Возвращаем историю просмотров
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task); // Обновляем задачу
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask); // Обновляем подзадачу
            updateEpicStatus(subtask.getEpicId()); // Обновляем статус эпика
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic); // Обновляем эпик
        }
    }

    @Override
    public int deleteTask(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            historyManager.remove(id); // Удаляем задачу из истории
            return id;
        }
        return -1; // Если задача не найдена
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear(); // Очищаем хранилище задач
    }

    @Override
    public void deleteAllSubTasks() {
        subtasks.clear(); // Очищаем хранилище подзадач
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear(); // Очищаем списки подзадач у всех эпиков
        }
    }

    @Override
    public void deleteAllEpics() {
        epics.clear(); // Очищаем хранилище эпиков
        deleteAllSubTasks(); // Удаляем все подзадачи
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            historyManager.remove(id); // Удаляем подзадачу из истории
            updateEpicStatus(subtask.getEpicId()); // Обновляем статус эпика
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtaskIds().remove(Integer.valueOf(id)); // Удаляем id подзадачи из эпика
            }
        }
    }

    @Override
    public int deleteEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            for (int subId : epic.getSubtaskIds()) {
                subtasks.remove(subId); // Удаляем все подзадачи эпика
                historyManager.remove(subId); // Удаляем подзадачи из истории
            }
            epics.remove(id); // Удаляем эпик
            historyManager.remove(id); // Удаляем эпик из истории
            return id;
        }
        return -1; // Если эпик не найден
    }

    @Override
    public void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            List<Integer> subtaskIds = epic.getSubtaskIds();
            boolean allDone = true;
            boolean anyInProgress = false;

            // Проверяем статусы всех подзадач
            for (int subtaskId : subtaskIds) {
                Subtask subtask = subtasks.get(subtaskId);
                if (subtask != null) {
                    if (subtask.getStatus() != Status.DONE) {
                        allDone = false;
                    }
                    if (subtask.getStatus() == Status.IN_PROGRESS) {
                        anyInProgress = true;
                    }
                }
            }

            // Обновляем статус эпика
            if (subtaskIds.isEmpty() || allDone) {
                epic.setStatus(Status.DONE);
            } else if (anyInProgress) {
                epic.setStatus(Status.IN_PROGRESS);
            } else {
                epic.setStatus(Status.NEW);
            }
        }
    }
}