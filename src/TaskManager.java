import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;


class TaskManager {
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private int taskIdCounter = 1;
    private int subTaskIdCounter = 1;
    private int epicIdCounter = 1;

    // метод для создания задачи
    public Task createTask(Task task) {
        task.setId(taskIdCounter ++);
        tasks.put(task.getId(), task);
        return task;
    }

    // метод для создания подзадачи
    public Subtask createSubtask(Subtask subtask) {
        subtask.setId(subTaskIdCounter++);
        subtasks.put(subtask.getId(), subtask);
        if (epics.containsKey(subtask.getEpicId())) {
            epics.get(subtask.getEpicId()).addSubtask(subtask.getId());
        }
        return subtask;
    }

    // Метод для создания эпика
    public Epic createEpic(Epic epic) {
        epic.setId(epicIdCounter++);
        epics.put(epic.getId(), epic);
        return epic;
    }

    // метод получения таски по id
    public Task getTaskById(int id){
        return tasks.get(id);
    }

    // метод получения эпика по id
    public Epic getEpicById(int id){
        return epics.get(id);
    }

    // метод получения сабтаски по id
    public Subtask getSubTaskById(int id){
        return subtasks.get(id);
    }


    // Метод для получения списка всех задач
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    // Метод для получения списка всех подзадач
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    // Метод для получения списка всех эпиков
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    // Метод для обновления задачи
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    // Метод для обновления подзадачи
    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(subtask.getEpicId());
    }

    // Метод для обновления эпика
    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    // Метод для удаления задачи по идентификатору
    public int deleteTask(int id) {
        tasks.remove(id);
        return id;
    }

    // метод для удаления всех задач
    public void deleteAllTasks() {
        tasks.clear();
    }

    // метод для удаления всех подзадач
    public void deleteAllSubTasks() {
        subtasks.clear();
    }

    // метод для удаления всех эпиков
    public void deleteAllEpics() {
        epics.clear();
    }

    // Метод для удаления подзадачи по идентификатору
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            updateEpicStatus(subtask.getEpicId());
        }
    }

    // Метод для удаления эпика по идентификатору
    public int deleteEpic(int id) {
        epics.remove(id);
        return id;
    }



    // Метод для обновления статуса эпика на основе его подзадач
    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId); // Получаем эпик по идентификатору
        if (epic != null) { // Проверяем, существует ли эпик
            List<Integer> subtaskIds = epic.getSubtaskIds(); // Получаем список подзадач
            boolean allDone = true; // Предполагаем, что все подзадачи завершены
            boolean anyInProgress = false; // Предполагаем, что нет подзадач в процессе выполнения

            // Проверяем статус каждой подзадачи
            for (int subtaskId : subtaskIds) {
                Subtask subtask = subtasks.get(subtaskId); // Получаем подзадачу по идентификатору
                if (subtask != null) { // Проверяем, существует ли подзадача
                    if (subtask.getStatus() != Status.DONE) {
                        allDone = false; // Если подзадача не завершена, меняем флаг
                    }
                    if (subtask.getStatus() == Status.IN_PROGRESS) {
                        anyInProgress = true; // Если есть подзадача в процессе, меняем флаг
                    }
                }
            }

            // Устанавливаем статус эпика в зависимости от статуса подзадач
            if (subtaskIds.isEmpty() || allDone) {
                epic.setStatus(Status.DONE); // Если подзадач нет или все завершены, эпик завершен
            } else if (anyInProgress) {
                epic.setStatus(Status.IN_PROGRESS); // Если есть подзадачи в процессе, эпик в процессе
            } else {
                epic.setStatus(Status.NEW); // Если все подзадачи новые, эпик новый
            }
        }
    }
}