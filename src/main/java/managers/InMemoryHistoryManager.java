package managers;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private List<Task> history = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (history.size() >= 10) {
            history.remove(0);
        }
        if (task instanceof Epic) {
            history.add(new Epic((Epic) task));
        } else if (task instanceof Subtask) {
            history.add(new Subtask((Subtask) task));
        } else {
            history.add(new Task(task));
        }
    }

    @Override
    public List<Task> getHistory() {
        return history;
    }
}