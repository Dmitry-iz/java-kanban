package managers;

import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.util.*;

public class TaskManager {
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private int taskIdCounter = 1;
    private int subTaskIdCounter = 1;
    private int epicIdCounter = 1;

    public Task createTask(Task task) {
        task.setId(taskIdCounter++);
        tasks.put(task.getId(), task);
        return task;
    }

    public Subtask createSubtask(Subtask subtask) {
        if (epics.containsKey(subtask.getEpicId())) {
            subtask.setId(subTaskIdCounter++);
            subtasks.put(subtask.getId(), subtask);
            epics.get(subtask.getEpicId()).addSubtask(subtask.getId());
            updateEpicStatus(subtask.getEpicId());
            return subtask;
        }
        System.out.println("Ошибка! Эпик с ID " + subtask.getEpicId() + " не найден.");
        return null;
    }

    public Epic createEpic(Epic epic) {
        epic.setId(epicIdCounter++);
        epics.put(epic.getId(), epic);
        return epic;
    }

    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            System.out.println("Ошибка! Задача с ID " + id + " не найдена.");
        }
        return task;
    }

    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            System.out.println("Ошибка! Эпик с ID " + id + " не найден.");
        }
        return epic;
    }

    public Subtask getSubTaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            System.out.println("Ошибка! Сабтаска с ID " + id + " не найдена.");
        }
        return subtask;
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        } else {
            System.out.println("Ошибка! Нельзя обновить задачу с ID " + task.getId() + ", так как она не существует.");
        }
    }

    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
            updateEpicStatus(subtask.getEpicId());
        } else {
            System.out.println("Ошибка! Нельзя обновить сабтаску с ID " + subtask.getId() + ", так как она не существует.");
        }
    }

    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
        } else {
            System.out.println("Ошибка! Нельзя обновить эпик с ID " + epic.getId() + ", так как он не существует.");
        }
    }

    public int deleteTask(int id) {
        if (tasks.remove(id) == null) {
            System.out.println("Ошибка! Задача с ID " + id + " не найдена и не может быть удалена.");
        }
        return id;
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllSubTasks() {
        subtasks.clear();
        for (Map.Entry<Integer, Epic> epic : epics.entrySet()) {
            epic.getValue().getSubtaskIds().clear();
        }
    }

    public void deleteAllEpics() {
        epics.clear();
        deleteAllSubTasks();
    }

    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            updateEpicStatus(subtask.getEpicId());
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                List<Integer> ids = epic.getSubtaskIds();
                ids.remove(Integer.valueOf(id));
            }
        } else {
            System.out.println("Ошибка! Сабтаска с ID " + id + " не найдена и не может быть удалена.");
        }
    }

    public int deleteEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            for (int subId : epic.getSubtaskIds()) {
                subtasks.remove(subId);
            }
            epics.remove(id);
        } else {
            System.out.println("Ошибка! Эпик с ID " + id + " не найден и не может быть удален.");
        }
        return id;
    }

    private void updateEpicStatus(int epicId) {
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
}