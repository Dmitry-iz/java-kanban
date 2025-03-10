package tasks;

import managers.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    @Test
    public void testEpicCannotAddItselfAsSubtask() {
        // Создаем менеджер задач и эпик
        TaskManager manager = Managers.getDefault();
        Epic epic = manager.createEpic(new Epic("Epic 1", "Description 1"));

        // Пытаемся создать подзадачу, где эпик является своей же подзадачей
        Subtask subtask = new Subtask("Subtask 1", "Description 1", epic.getId());
        subtask.setId(epic.getId()); // Устанавливаем id подзадачи равным id эпика

        // Проверяем, что подзадача не создается (должен вернуться null)
        assertNull(manager.createSubtask(subtask), "Эпик не должен быть добавлен в самого себя как подзадача.");
    }

    @Test
    public void testEpicStatusUpdate() {
        // Создаем менеджер задач, эпик и две подзадачи
        TaskManager manager = Managers.getDefault();
        Epic epic = manager.createEpic(new Epic("Epic 1", "Description 1"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask 1", "Description 1", epic.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Subtask 2", "Description 2", epic.getId()));

        // Проверяем, что статус эпика изначально NEW
        assertEquals(Status.NEW, epic.getStatus(), "Статус эпика должен быть NEW.");

        // Меняем статус первой подзадачи на IN_PROGRESS и обновляем её
        subtask1.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask1);

        // Проверяем, что статус эпика изменился на IN_PROGRESS
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Статус эпика должен быть IN_PROGRESS.");

        // Меняем статусы обеих подзадач на DONE и обновляем их
        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);
        manager.updateSubtask(subtask2);

        // Проверяем, что статус эпика изменился на DONE
        assertEquals(Status.DONE, epic.getStatus(), "Статус эпика должен быть DONE.");
    }
}
