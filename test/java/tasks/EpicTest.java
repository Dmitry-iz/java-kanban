package tasks;

import managers.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    @Test
    public void testEpicCannotAddItselfAsSubtask() {
        TaskManager manager = Managers.getDefault();
        Epic epic = manager.createEpic(new Epic("Epic 1", "Description 1"));

        Subtask subtask = new Subtask("Subtask 1", "Description 1", epic.getId());
        subtask.setId(epic.getId());

        assertNull(manager.createSubtask(subtask), "Эпик не должен быть добавлен в самого себя как подзадача.");
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
}
