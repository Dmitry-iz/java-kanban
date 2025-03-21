package tasks;

import managers.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    public void testTaskEqualityById() {
        // Создаем менеджер задач и две задачи
        TaskManager manager = Managers.getDefault();
        Task task1 = manager.createTask(new Task("Task 1", "Description 1"));
        Task task2 = manager.createTask(new Task("Task 2", "Description 2"));

        // Проверяем, что задачи имеют уникальные идентификаторы
        assertNotEquals(task1.getId(), task2.getId(), "Задачи должны иметь уникальные ID.");
    }

    @Test
    public void testTaskImmutabilityWhenAdded() {
        // Создаем менеджер задач и задачу
        TaskManager manager = Managers.getDefault();
        Task task = manager.createTask(new Task("Task 1", "Description 1"));

        // Получаем задачу из менеджера и проверяем, что её свойства не изменились
        Task retrievedTask = manager.getTaskById(task.getId());
        assertEquals(task.getTitle(), retrievedTask.getTitle(), "Название задачи не должно изменяться.");
        assertEquals(task.getDescription(), retrievedTask.getDescription(), "Описание задачи не должно изменяться.");
        assertEquals(task.getStatus(), retrievedTask.getStatus(), "Статус задачи не должен изменяться.");
    }

    @Test
    public void testTaskIdConflict() {
        // Создаем менеджер задач и две задачи
        TaskManager manager = Managers.getDefault();
        Task task1 = manager.createTask(new Task("Task 1", "Description 1"));
        Task task2 = manager.createTask(new Task("Task 2", "Description 2"));

        // Проверяем, что задачи имеют уникальные идентификаторы
        assertNotEquals(task1.getId(), task2.getId(), "Задачи должны иметь уникальные ID.");
    }
}
