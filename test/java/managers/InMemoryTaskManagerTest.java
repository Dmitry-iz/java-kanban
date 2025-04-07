package managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @BeforeEach
    public void setUp() {
        manager = new InMemoryTaskManager();
    }

    @Test
    void testAddAndFindTasksById() {
        Task task = manager.createTask(new Task("Task 1", "Description 1"));
        Epic epic = manager.createEpic(new Epic("Epic 1", "Description 1"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask 1", "Description 1", epic.getId()));

        assertEquals(task, manager.getTaskById(task.getId()), "Задача должна быть найдена по id.");
        assertEquals(epic, manager.getEpicById(epic.getId()), "Эпик должен быть найден по id.");
        assertEquals(subtask, manager.getSubTaskById(subtask.getId()), "Подзадача должна быть найдена по id.");
    }

    @Test
    void testDeleteAllTasks() {
        Task task1 = manager.createTask(new Task("Task 1", "Description 1"));
        Task task2 = manager.createTask(new Task("Task 2", "Description 2"));

        assertEquals(2, manager.getAllTasks().size(), "Должно быть 2 задачи.");
        manager.deleteAllTasks();
        assertEquals(0, manager.getAllTasks().size(), "Все задачи должны быть удалены.");
    }

    @Test
    void testDeleteAllTasksEndAllEpicsEndAllSubTasks() {
        // Создаем задачи разных типов
        Task task = manager.createTask(new Task("Task", "Description"));
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Description", epic.getId()));

        // Удаляем только обычные задачи
        manager.deleteAllTasks();
        manager.deleteAllEpics();
        manager.deleteAllSubTasks();

        // Проверяем что именно должно удалиться
        assertEquals(0, manager.getAllTasks().size(), "Обычные задачи должны быть удалены");
        assertEquals(0, manager.getAllEpics().size(), "Эпики должны быть удалены");
        assertEquals(0, manager.getAllSubtasks().size(), "Подзадачи должны быть удалены");
    }
}