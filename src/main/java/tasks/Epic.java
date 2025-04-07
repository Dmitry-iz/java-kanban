package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private List<Integer> subtaskIds = new ArrayList<>(); // Список id подзадач
    private LocalDateTime endTime;

    public Epic(String title, String description) {
        super(title, description);
    }

    public Epic(Epic epic) {
        super(epic); // Копируем поля задачи
        this.subtaskIds = new ArrayList<>(epic.subtaskIds); // Копируем список подзадач
    }

    public void updateTiming(List<Subtask> subtasks) {
        LocalDateTime earliest = subtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        setStartTime(earliest);

        this.endTime = subtasks.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        Duration total = subtasks.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);

        setDuration(total);
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
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
        return "Epic{" +
                "title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus() +
                ", subtaskIds=" + subtaskIds +
                ", startTime=" + getStartTime() +
                ", endTime=" + endTime +
                ", duration=" + getDuration() +
                '}';
    }
}