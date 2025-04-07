package tasks;

import managers.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    @Test
    public void testSubtaskEqualityById() {
        TaskManager manager = Managers.getDefault();
        Epic epic = manager.createEpic(new Epic("Epic 1", "Description 1"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask 1", "Description 1", epic.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Subtask 2", "Description 2", epic.getId()));

        assertNotEquals(subtask1.getId(), subtask2.getId(), "Подзадачи должны иметь уникальные ID.");
    }

    @Test
    public void testSubtaskCannotBeItsOwnEpic() {
        TaskManager manager = Managers.getDefault();
        Epic epic = manager.createEpic(new Epic("Epic 1", "Description 1"));

        Subtask subtask = new Subtask("Subtask 1", "Description 1", epic.getId());
        subtask.setEpicId(subtask.getId());

        assertNull(manager.createSubtask(subtask), "Подзадача не должна быть своим же эпиком.");
    }
}