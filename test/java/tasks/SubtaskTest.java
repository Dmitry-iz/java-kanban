package tasks;

import managers.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    @Test
    public void testSubtaskEqualityById() {
        // Создаем менеджер задач, эпик и две подзадачи
        TaskManager manager = Managers.getDefault();
        Epic epic = manager.createEpic(new Epic("Epic 1", "Description 1"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask 1", "Description 1", epic.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Subtask 2", "Description 2", epic.getId()));

        // Проверяем, что подзадачи имеют уникальные идентификаторы
        assertNotEquals(subtask1.getId(), subtask2.getId(), "Подзадачи должны иметь уникальные ID.");
    }

    @Test
    public void testSubtaskCannotBeItsOwnEpic() {
        // Создаем менеджер задач и эпик
        TaskManager manager = Managers.getDefault();
        Epic epic = manager.createEpic(new Epic("Epic 1", "Description 1"));

        // Пытаемся создать подзадачу, где эпик является своим же подзадачей
        Subtask subtask = new Subtask("Subtask 1", "Description 1", epic.getId());
        subtask.setEpicId(subtask.getId()); // Устанавливаем epicId равным id подзадачи

        // Проверяем, что подзадача не создается (должен вернуться null)
        assertNull(manager.createSubtask(subtask), "Подзадача не должна быть своим же эпиком.");
    }
}