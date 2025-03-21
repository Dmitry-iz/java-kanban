package tasks;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds = new ArrayList<>(); // Список id подзадач

    public Epic(String title, String description) {
        super(title, description);
    }

    public Epic(Epic epic) {
        super(epic); // Копируем поля задачи
        this.subtaskIds = new ArrayList<>(epic.subtaskIds); // Копируем список подзадач
    }

    // Добавляет id подзадачи в список
    public void addSubtask(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    // Возвращает список id подзадач
    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    @Override
    public String toString() {
        return "tasks.Epic{" +
                "title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus() +
                ", subtaskIds=" + subtaskIds +
                '}';
    }
}