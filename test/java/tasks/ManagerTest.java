package tasks;

import managers.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ManagerTest {

    @Test
    public void testManagersInitialization() {
        // Создаем TaskManager и HistoryManager через фабрику Managers
        TaskManager taskManager = Managers.getDefault();
        HistoryManager historyManager = Managers.getDefaultHistory();

        // Проверяем, что оба менеджера были успешно инициализированы
        assertNotNull(taskManager, "TaskManager должен быть проинициализирован.");
        assertNotNull(historyManager, "HistoryManager должен быть проинициализирован.");
    }

    @Test
    public void testAddAndFindTasksById() {
        // Создаем менеджер задач, задачу, эпик и подзадачу
        TaskManager manager = Managers.getDefault();
        Task task = manager.createTask(new Task("Task 1", "Description 1"));
        Epic epic = manager.createEpic(new Epic("Epic 1", "Description 1"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask 1", "Description 1", epic.getId()));

        // Проверяем, что задачи, эпик и подзадача находятся по их id
        assertEquals(task, manager.getTaskById(task.getId()), "Задача должна быть найдена по id.");
        assertEquals(epic, manager.getEpicById(epic.getId()), "Эпик должен быть найден по id.");
        assertEquals(subtask, manager.getSubTaskById(subtask.getId()), "Подзадача должна быть найдена по id.");
    }

    @Test
    public void testDeleteAllTasks() {
        // Создаем менеджер задач и две задачи
        TaskManager manager = Managers.getDefault();
        Task task1 = manager.createTask(new Task("Task 1", "Description 1"));
        Task task2 = manager.createTask(new Task("Task 2", "Description 2"));

        // Проверяем, что в менеджере две задачи
        assertEquals(2, manager.getAllTasks().size(), "Должно быть 2 задачи.");

        // Удаляем все задачи
        manager.deleteAllTasks();

        // Проверяем, что список задач пуст
        assertEquals(0, manager.getAllTasks().size(), "Все задачи должны быть удалены.");
    }

    @Test
    public void testHistoryManagerPreservesTaskState() {
        // Создаем менеджер задач и менеджер истории
        InMemoryTaskManager manager = new InMemoryTaskManager();
        HistoryManager historyManager = Managers.getDefaultHistory();

        // Создаем задачу и добавляем её в историю
        Task task = manager.createTask(new Task("Task 1", "Description 1"));
        historyManager.add(task);

        // Получаем задачу из менеджера и изменяем её статус
        Task retrievedTask = manager.getTaskById(task.getId());
        assertNotNull(retrievedTask, "Задача должна быть найдена в TaskManager.");
        retrievedTask.setStatus(Status.IN_PROGRESS);

        // Получаем историю и проверяем, что она не пуста
        List<Task> history = historyManager.getHistory();
        assertFalse(history.isEmpty(), "История не должна быть пустой.");

        // Проверяем, что история сохранила исходное состояние задачи (NEW)
        Task historyTask = history.get(0);
        assertEquals(Status.NEW, historyTask.getStatus(), "История должна сохранять предыдущую версию задачи.");
    }

    @Test
    public void testHistoryManagerRemovesDuplicates() {
        // Создаем менеджер истории и задачу
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task = new Task("Task 1", "Description 1");
        task.setId(1);

        // Добавляем задачу в историю дважды
        historyManager.add(task);
        historyManager.add(task);

        // Проверяем, что история содержит только одну запись
        assertEquals(1, historyManager.getHistory().size(), "История должна содержать только одну запись.");
    }

    @Test
    public void testHistoryManagerRemovesTask() {
        // Создаем менеджер истории и задачу
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task = new Task("Task 1", "Description 1");
        task.setId(1);

        // Добавляем задачу в историю и удаляем её
        historyManager.add(task);
        historyManager.remove(task.getId());

        // Проверяем, что история пуста
        assertTrue(historyManager.getHistory().isEmpty(), "История должна быть пустой после удаления задачи.");
    }

    @Test
    void testAddAndGetHistory() {
        // Создаем менеджер задач и менеджер истории
        InMemoryTaskManager manager = new InMemoryTaskManager();
        HistoryManager historyManager = Managers.getDefaultHistory();

        // Создаем две задачи и добавляем их в историю
        Task task1 = manager.createTask(new Task("Task 1", "Description 1"));
        Task task2 = manager.createTask(new Task("Task 2", "Description 2"));
        historyManager.add(task1);
        historyManager.add(task2);

        // Проверяем, что история содержит две задачи в правильном порядке
        assertEquals(2, historyManager.getHistory().size());
        assertEquals(task1, historyManager.getHistory().get(0));
        assertEquals(task2, historyManager.getHistory().get(1));
    }

    @Test
    void testRemove() {
        // Создаем менеджер истории и две задачи
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task1 = new Task("Task 1", "Description 1");
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description 2");
        task2.setId(2);

        // Добавляем обе задачи в историю и удаляем первую
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(1);

        // Проверяем, что в истории осталась только вторая задача
        assertEquals(1, historyManager.getHistory().size());
        assertEquals(task2, historyManager.getHistory().get(0));
    }

    @Test
    void testNoDuplicates() {
        // Создаем менеджер истории и задачу
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task1 = new Task("Task 1", "Description 1");
        task1.setId(1);

        // Добавляем задачу в историю дважды
        historyManager.add(task1);
        historyManager.add(task1);

        // Проверяем, что история содержит только одну запись
        assertEquals(1, historyManager.getHistory().size());
    }

    @Test
    void test_removeEpic_itAndItsSubtasksRemovedFromHistory() {
        TaskManager taskManager = Managers.getDefault();

        // Создаем эпик и две подзадачи
        Epic epic = taskManager.createEpic(new Epic("Epic 1", "Description 1"));
        Subtask subtask1 = taskManager.createSubtask(new Subtask("Subtask 1", "Description 1", epic.getId()));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Subtask 2", "Description 2", epic.getId()));

        // Добавляем эпик и подзадачи в историю
        taskManager.getEpicById(epic.getId());
        taskManager.getSubTaskById(subtask1.getId());
        taskManager.getSubTaskById(subtask2.getId());

        // Удаляем эпик
        taskManager.deleteEpic(epic.getId());

        // Проверяем, что эпик и его подзадачи удалены из истории
        List<Task> history = taskManager.getHistory();
        assertFalse(history.contains(epic), "Эпик должен быть удален из истории.");
        assertFalse(history.contains(subtask1), "Подзадача 1 должна быть удалена из истории.");
        assertFalse(history.contains(subtask2), "Подзадача 2 должна быть удалена из истории.");
    }
}