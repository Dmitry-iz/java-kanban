package managers;

import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    // Хранилище задач, подзадач и эпиков
    protected Map<Integer, Task> tasks = new HashMap<>();
    protected Map<Integer, Subtask> subtasks = new HashMap<>();
    protected Map<Integer, Epic> epics = new HashMap<>();
    protected final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(
                    Task::getStartTime,
                    Comparator.nullsLast(Comparator.naturalOrder())
            ));

    // Менеджер истории для отслеживания просмотренных задач
    private HistoryManager historyManager = Managers.getDefaultHistory();

    // Счетчики для генерации уникальных id
    protected int idCounter = 0;

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private void validateSubtaskTime(Subtask newSubtask) {
        Epic epic = epics.get(newSubtask.getEpicId());
        if (epic == null) return;

        List<Subtask> subtasksValidate = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .filter(s -> s.getId() != newSubtask.getId()) // Исключаем текущую задачу при обновлении
                .collect(Collectors.toList());

        boolean hasConflict = subtasksValidate.stream().anyMatch(existing -> isOverlap(existing, newSubtask));
        if (hasConflict) {
            throw new ManagerValidationException("Задача пересекается с существующей");
        }
    }

    private void validateTaskTime(Task newTask) {
        boolean hasConflict = getAllTasks().stream()
                .filter(task -> task.getId() != newTask.getId())
                .anyMatch(existing -> isOverlap(existing, newTask));

        if (hasConflict) {
            throw new ManagerValidationException("Задачи пересекаются по времени");
        }
    }

    private boolean isOverlap(Task a, Task b) {
        LocalDateTime aStart = a.getStartTime();
        LocalDateTime aEnd = a.getEndTime();
        LocalDateTime bStart = b.getStartTime();
        LocalDateTime bEnd = b.getEndTime();

        return aStart != null && bStart != null &&
                !aEnd.isBefore(bStart) &&
                !aStart.isAfter(bEnd);
    }

    @Override
    public Task createTask(Task task) {
        validateTaskTime(task);
        task.setId(++idCounter);
        tasks.put(task.getId(), task);

        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
        return task;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        validateSubtaskTime(subtask);
        if (epics.containsKey(subtask.getEpicId())) {
            if (subtask.getEpicId() == subtask.getId()) {
                return null; // Подзадача не может быть своим эпиком
            }
            subtask.setId(++idCounter);

            subtasks.put(subtask.getId(), subtask);

            Epic epic = epics.get(subtask.getEpicId());
            epic.addSubtask(subtask.getId()); // Добавляем ID в эпик

            updateEpicStatus(subtask.getEpicId()); // Старая логика - обновление статуса
            updateEpicTiming(subtask.getEpicId()); // Новая логика - обновление времени

            if (subtask.getStartTime() != null) {
                prioritizedTasks.add(subtask);
            }
            return subtask;
        }
        return null;
    }

    private void updateEpicTiming(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            List<Subtask> subs = epic.getSubtaskIds().stream()
                    .map(subtasks::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            epic.updateTiming(subs);
        }
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(++idCounter);// Увеличиваем счетчик и устанавливаем уникальный id
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
        validateTaskTime(task);
        if (tasks.containsKey(task.getId())) {
            Task oldTask = tasks.get(task.getId());
            prioritizedTasks.remove(oldTask); // Удаляем старую версию
            tasks.put(task.getId(), task);
            if (task.getStartTime() != null) {
                prioritizedTasks.add(task); // Добавляем обновленную
            }
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        validateSubtaskTime(subtask);
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask); // Обновляем подзадачу
            updateEpicStatus(subtask.getEpicId()); // Обновляем статус эпика
        }
        if (subtask.getStartTime() != null) {
            prioritizedTasks.remove(subtask); // Сначала удаляем старую версию
            prioritizedTasks.add(subtask);
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
            prioritizedTasks.remove(task); // Синхронизируем удаление
            historyManager.remove(id); // Удаляем задачу из истории
            return id;
        }
        return -1; // Если задача не найдена
    }

    @Override
    public void deleteAllTasks() {
        subtasks.values().forEach(prioritizedTasks::remove); // Очищаем подзадачи
        subtasks.clear();
        tasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
        }
        idCounter = 1;
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
        idCounter = 1;
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            prioritizedTasks.remove(subtask); // Удаляем из приоритетного списка
            historyManager.remove(id);

            int epicId = subtask.getEpicId();
            updateEpicStatus(epicId);
            updateEpicTiming(epicId); // Добавляем вызов

            Epic epic = epics.get(epicId);
            if (epic != null) {
                epic.getSubtaskIds().remove(Integer.valueOf(id));
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
            boolean allNew = true;
            boolean allDone = true;
            boolean anyInProgress = false;

            // Проверяем статусы всех подзадач
            for (int subtaskId : subtaskIds) {
                Subtask subtask = subtasks.get(subtaskId);
                if (subtask != null) {
                    if (subtask.getStatus() != Status.NEW) {
                        allNew = false;
                    }
                    if (subtask.getStatus() != Status.DONE) {
                        allDone = false;
                    }
                    if (subtask.getStatus() == Status.IN_PROGRESS) {
                        anyInProgress = true;
                    }
                }
            }

            // Обновляем статус эпика
            if (subtaskIds.isEmpty()) {
                epic.setStatus(Status.NEW);
            } else if (allDone) {
                epic.setStatus(Status.DONE);
            } else if (allNew) {
                epic.setStatus(Status.NEW);
            } else {
                epic.setStatus(Status.IN_PROGRESS);
            }
        }
    }
}