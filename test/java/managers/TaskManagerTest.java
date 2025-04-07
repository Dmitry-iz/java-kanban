package managers;

import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;

    @Test
    void testCreateAndGetTask() {
        Task task = manager.createTask(new Task("Test task", "Test description"));
        Task savedTask = manager.getTaskById(task.getId());

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");
    }

    @Test
    void testCreateAndGetEpic() {
        Epic epic = manager.createEpic(new Epic("Test epic", "Test description"));
        Epic savedEpic = manager.getEpicById(epic.getId());

        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");
    }

    @Test
    void testCreateAndGetSubtask() {
        Epic epic = manager.createEpic(new Epic("Test epic", "Test description"));
        Subtask subtask = manager.createSubtask(new Subtask("Test subtask", "Test description", epic.getId()));
        Subtask savedSubtask = manager.getSubTaskById(subtask.getId());

        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают.");
        assertEquals(epic.getId(), savedSubtask.getEpicId(), "ID эпика не совпадает.");
    }

    @Test
    void testUpdateTask() {
        Task task = manager.createTask(new Task("Test task", "Test description"));
        task.setStatus(Status.IN_PROGRESS);
        manager.updateTask(task);

        assertEquals(Status.IN_PROGRESS, manager.getTaskById(task.getId()).getStatus(), "Статус задачи не обновлен.");
    }

    @Test
    void testDeleteTask() {
        Task task = manager.createTask(new Task("Test task", "Test description"));
        int taskId = task.getId();
        manager.deleteTask(taskId);

        assertNull(manager.getTaskById(taskId), "Задача не удалена.");
    }

    @Test
    void testEpicStatusAllNew() {
        Epic epic = manager.createEpic(new Epic("Test epic", "Test description"));
        manager.createSubtask(new Subtask("Subtask 1", "Description 1", epic.getId()));
        manager.createSubtask(new Subtask("Subtask 2", "Description 2", epic.getId()));

        assertEquals(Status.NEW, epic.getStatus(), "Статус эпика должен быть NEW.");
    }

    @Test
    void testEpicStatusAllDone() {
        Epic epic = manager.createEpic(new Epic("Test epic", "Test description"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask 1", "Description 1", epic.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Subtask 2", "Description 2", epic.getId()));

        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);
        manager.updateSubtask(subtask2);

        assertEquals(Status.DONE, epic.getStatus(), "Статус эпика должен быть DONE.");
    }


    @Test
    void testEpicStatusNewAndDone() {
        Epic epic = manager.createEpic(new Epic("Test epic", "Test description"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask 1", "Description 1", epic.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Subtask 2", "Description 2", epic.getId()));

        subtask1.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);

        // Получаем актуальный эпик из менеджера
        Epic updatedEpic = manager.getEpicById(epic.getId());
        assertEquals(Status.IN_PROGRESS, updatedEpic.getStatus(),
                "Статус эпика должен быть IN_PROGRESS, когда одна подзадача DONE, а другая NEW");
    }


    @Test
    void testEpicStatusInProgress() {
        Epic epic = manager.createEpic(new Epic("Test epic", "Test description"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask 1", "Description 1", epic.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Subtask 2", "Description 2", epic.getId()));

        subtask1.setStatus(Status.IN_PROGRESS);
        subtask2.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask1);
        manager.updateSubtask(subtask2);

        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Статус эпика должен быть IN_PROGRESS.");
    }

    @Test
    void testTaskTimeIntersection() {
        Task task1 = new Task("Task 1", "Description 1");
        task1.setStartTime(LocalDateTime.now());
        task1.setDuration(Duration.ofHours(1));
        manager.createTask(task1);

        Task task2 = new Task("Task 2", "Description 2");
        task2.setStartTime(task1.getStartTime().plusMinutes(30));
        task2.setDuration(Duration.ofHours(1));

        assertThrows(ManagerValidationException.class, () -> manager.createTask(task2),
                "Должно быть исключение при пересечении времени задач.");
    }
}