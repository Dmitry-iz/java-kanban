package managers;

import org.junit.jupiter.api.Test;
import tasks.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @Test
    void testEmptyHistory() {
        historyManager = new InMemoryHistoryManager();
        assertTrue(historyManager.getHistory().isEmpty(), "История должна быть пустой.");
    }

    @Test
    void testAddToHistory() {
        historyManager = new InMemoryHistoryManager();
        Task task = new Task("Task 1", "Description 1");
        task.setId(1);

        historyManager.add(task);
        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "История должна содержать 1 задачу.");
        assertEquals(task, history.get(0), "Задачи должны совпадать.");
    }

    @Test
    void testRemoveFromBeginning() {
        historyManager = new InMemoryHistoryManager();
        Task task1 = new Task("Task 1", "Description 1");
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description 2");
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать 1 задачу.");
        assertEquals(task2, history.get(0), "Оставшаяся задача должна быть task2.");
    }

    @Test
    void testRemoveFromMiddle() {
        historyManager = new InMemoryHistoryManager();
        Task task1 = new Task("Task 1", "Description 1");
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description 2");
        task2.setId(2);
        Task task3 = new Task("Task 3", "Description 3");
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "История должна содержать 2 задачи.");
        assertEquals(task1, history.get(0), "Первая задача должна быть task1.");
        assertEquals(task3, history.get(1), "Вторая задача должна быть task3.");
    }

    @Test
    void testRemoveFromEnd() {
        historyManager = new InMemoryHistoryManager();
        Task task1 = new Task("Task 1", "Description 1");
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description 2");
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать 1 задачу.");
        assertEquals(task1, history.get(0), "Оставшаяся задача должна быть task1.");
    }

    @Test
    void testNoDuplicatesInHistory() {
        historyManager = new InMemoryHistoryManager();
        Task task = new Task("Task 1", "Description 1");
        task.setId(1);

        historyManager.add(task);
        historyManager.add(task);

        assertEquals(1, historyManager.getHistory().size(), "История должна содержать только одну запись.");
    }
}