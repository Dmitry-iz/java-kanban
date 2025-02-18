package managers;

import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private HistoryManager historyManager = Managers.getDefaultHistory();

    private int taskIdCounter = 1;
    private int subTaskIdCounter = 1;
    private int epicIdCounter = 1;

    @Override
    public Task createTask(Task task) {
        task.setId(taskIdCounter++);
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (epics.containsKey(subtask.getEpicId())) {
            if (subtask.getEpicId() == subtask.getId()) {
                return null; //возврат null, Нельзя добавить подзадачу в себя
            }
            subtask.setId(subTaskIdCounter++);
            subtasks.put(subtask.getId(), subtask);
            epics.get(subtask.getEpicId()).addSubtask(subtask.getId());
            updateEpicStatus(subtask.getEpicId());
            return subtask;
        }
        return null; // Возврат null, если эпик не найден
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(epicIdCounter++);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            return null; // Возврат null, если задача не найдена
        }
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return null; // Возврат null, если эпик не найден
        }
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubTaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            return null; // Возврат null, если сабтаск не найден
        }
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
            updateEpicStatus(subtask.getEpicId());
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
        }
    }

    @Override
    public int deleteTask(int id) {
        return tasks.remove(id) == null ? -1 : id; // Возврат -1, если задача не найдена
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public void deleteAllSubTasks() {
        subtasks.clear();
        for (Map.Entry<Integer, Epic> epic : epics.entrySet()) {
            epic.getValue().getSubtaskIds().clear();
        }
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
        deleteAllSubTasks();
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            updateEpicStatus(subtask.getEpicId());
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                List<Integer> ids = epic.getSubtaskIds();
                ids.remove(Integer.valueOf(id));
            }
        }
    }

    @Override
    public int deleteEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            for (int subId : epic.getSubtaskIds()) {
                subtasks.remove(subId);
            }
            epics.remove(id);
            return id; // Возврат ID эпика, если он удален
        }
        return -1; // Возврат -1, если эпик не найден
    }

    @Override
    public void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            List<Integer> subtaskIds = epic.getSubtaskIds();
            boolean allDone = true;
            boolean anyInProgress = false;

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

            if (subtaskIds.isEmpty() || allDone) {
                epic.setStatus(Status.DONE);
            } else if (anyInProgress) {
                epic.setStatus(Status.IN_PROGRESS);
            } else {
                epic.setStatus(Status.NEW);
            }
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}