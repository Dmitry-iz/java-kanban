package tasks;

import static org.junit.jupiter.api.Assertions.*;

import managers.*;
import org.junit.jupiter.api.Test;

import java.util.List;

class EpicTest {

    @Test
    public void testTaskEqualityById() {
        TaskManager manager = Managers.getDefault();
        Task task1 = manager.createTask(new Task("Task 1", "Description 1"));
        Task task2 = manager.createTask(new Task("Task 2", "Description 2"));

        assertNotEquals(task1.getId(), task2.getId(), "Задачи должны иметь уникальные ID.");
    }

    @Test
    public void testSubtaskEqualityById() {
        TaskManager manager = Managers.getDefault();
        Epic epic = manager.createEpic(new Epic("Epic 1", "Description 1"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask 1", "Description 1", epic.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Subtask 2", "Description 2", epic.getId()));

        assertNotEquals(subtask1.getId(), subtask2.getId(), "Подзадачи должны иметь уникальные ID.");
    }

    @Test
    public void testEpicCannotAddItselfAsSubtask() {
        TaskManager manager = Managers.getDefault();
        Epic epic = manager.createEpic(new Epic("Epic 1", "Description 1"));

        Subtask subtask = new Subtask("Subtask 1", "Description 1", epic.getId());
        subtask.setId(epic.getId()); // Пытаемся добавить эпик в самого себя

        assertNull(manager.createSubtask(subtask), "Эпик не должен быть добавлен в самого себя как подзадача.");
    }

    @Test
    public void testSubtaskCannotBeItsOwnEpic() {
        TaskManager manager = Managers.getDefault();
        Epic epic = manager.createEpic(new Epic("Epic 1", "Description 1"));

        Subtask subtask = new Subtask("Subtask 1", "Description 1", epic.getId());
        subtask.setEpicId(subtask.getId()); // Пытаемся сделать подзадачу своим же эпиком

        assertNull(manager.createSubtask(subtask), "Подзадача не должна быть своим же эпиком.");
    }

    @Test
    public void testManagersInitialization() {
        TaskManager taskManager = Managers.getDefault();
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertNotNull(taskManager, "TaskManager должен быть проинициализирован.");
        assertNotNull(historyManager, "HistoryManager должен быть проинициализирован.");
    }

    @Test
    public void testAddAndFindTasksById() {
        TaskManager manager = Managers.getDefault();
        Task task = manager.createTask(new Task("Task 1", "Description 1"));
        Epic epic = manager.createEpic(new Epic("Epic 1", "Description 1"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask 1", "Description 1", epic.getId()));

        assertEquals(task, manager.getTaskById(task.getId()), "Задача должна быть найдена по id.");
        assertEquals(epic, manager.getEpicById(epic.getId()), "Эпик должен быть найден по id.");
        assertEquals(subtask, manager.getSubTaskById(subtask.getId()), "Подзадача должна быть найдена по id.");
    }

    @Test
    public void testTaskIdConflict() {
        TaskManager manager = Managers.getDefault();
        Task task1 = manager.createTask(new Task("Task 1", "Description 1"));
        Task task2 = manager.createTask(new Task("Task 2", "Description 2"));

        assertNotEquals(task1.getId(), task2.getId(), "Задачи должны иметь уникальные ID.");
    }

    @Test
    public void testTaskImmutabilityWhenAdded() {
        TaskManager manager = Managers.getDefault();
        Task task = manager.createTask(new Task("Task 1", "Description 1"));

        Task retrievedTask = manager.getTaskById(task.getId());
        assertEquals(task.getTitle(), retrievedTask.getTitle(), "Название задачи не должно изменяться.");
        assertEquals(task.getDescription(), retrievedTask.getDescription(), "Описание задачи не должно изменяться.");
        assertEquals(task.getStatus(), retrievedTask.getStatus(), "Статус задачи не должен изменяться.");
    }

    @Test
    public void testHistoryManagerPreservesTaskState() {
        InMemoryTaskManager manager = new InMemoryTaskManager();
        HistoryManager historyManager = Managers.getDefaultHistory();

        Task task = manager.createTask(new Task("Task 1", "Description 1"));
        historyManager.add(task);

        Task retrievedTask = manager.getTaskById(task.getId());
        assertNotNull(retrievedTask, "Задача должна быть найдена в TaskManager.");

        retrievedTask.setStatus(Status.IN_PROGRESS); // Изменяем статус задачи

        List<Task> history = historyManager.getHistory();
        assertFalse(history.isEmpty(), "История не должна быть пустой.");

        Task historyTask = history.get(0);
        assertEquals(Status.NEW, historyTask.getStatus(), "История должна сохранять предыдущую версию задачи.");
    }

    @Test
    public void testEpicStatusUpdate() {
        TaskManager manager = Managers.getDefault();
        Epic epic = manager.createEpic(new Epic("Epic 1", "Description 1"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask 1", "Description 1", epic.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Subtask 2", "Description 2", epic.getId()));

        assertEquals(Status.NEW, epic.getStatus(), "Статус эпика должен быть NEW.");

        subtask1.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask1);

        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Статус эпика должен быть IN_PROGRESS.");

        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);
        manager.updateSubtask(subtask2);

        assertEquals(Status.DONE, epic.getStatus(), "Статус эпика должен быть DONE.");
    }

    @Test
    public void testDeleteAllTasks() {
        TaskManager manager = Managers.getDefault();
        Task task1 = manager.createTask(new Task("Task 1", "Description 1"));
        Task task2 = manager.createTask(new Task("Task 2", "Description 2"));

        assertEquals(2, manager.getAllTasks().size(), "Должно быть 2 задачи.");

        manager.deleteAllTasks(); // Удаляем все задачи

        assertEquals(0, manager.getAllTasks().size(), "Все задачи должны быть удалены.");
    }

    @Test
    public void testHistoryManagerRemovesDuplicates() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task = new Task("Task 1", "Description 1");
        task.setId(1);

        historyManager.add(task);
        historyManager.add(task);

        assertEquals(1, historyManager.getHistory().size(), "История должна содержать только одну запись.");
    }

    @Test
    public void testHistoryManagerRemovesTask() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task = new Task("Task 1", "Description 1");
        task.setId(1);

        historyManager.add(task);
        historyManager.remove(task.getId());

        assertTrue(historyManager.getHistory().isEmpty(), "История должна быть пустой после удаления задачи.");
    }

    @Test
    void testAddAndGetHistory() {
        InMemoryTaskManager manager = new InMemoryTaskManager();
        HistoryManager historyManager = Managers.getDefaultHistory();

        Task task1 = manager.createTask(new Task("Task 1", "Description 1"));
        Task task2 = manager.createTask(new Task("Task 2", "Description 2"));

        historyManager.add(task1);
        historyManager.add(task2);

        assertEquals(2, historyManager.getHistory().size());
        assertEquals(task1, historyManager.getHistory().get(0));
        assertEquals(task2, historyManager.getHistory().get(1));
    }

    @Test
    void testRemove() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task1 = new Task("Task 1", "Description 1");
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description 2");
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(1);

        assertEquals(1, historyManager.getHistory().size());
        assertEquals(task2, historyManager.getHistory().get(0));
    }

    @Test
    void testNoDuplicates() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task1 = new Task("Task 1", "Description 1");
        task1.setId(1);

        historyManager.add(task1);
        historyManager.add(task1);

        assertEquals(1, historyManager.getHistory().size());
    }
}
