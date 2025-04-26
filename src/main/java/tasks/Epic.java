package tasks;

import com.google.gson.annotations.SerializedName;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    @SerializedName("subtaskIds")
    private List<Integer> subtaskIds; // Список id подзадач
    @SerializedName("endTime")
    private LocalDateTime endTime;

    public Epic(String title, String description) {
        super(title, description);
        this.subtaskIds = new ArrayList<>();
    }

    public Epic(Epic epic) {
        super(epic); // Копируем поля задачи
        this.subtaskIds = new ArrayList<>(epic.subtaskIds); // Копируем список подзадач
    }

    public Epic() {
        super();
        this.subtaskIds = new ArrayList<>();
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void updateTiming(List<Subtask> subtasks) {
        if (subtasks.isEmpty()) {
            // Если подзадач нет, сбрасываем время
            super.setStartTime(null);
            super.setDuration(Duration.ZERO);
            setEndTime(null);
            return;
        }

        // Находим самое раннее время начала
        LocalDateTime earliestStart = subtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        // Находим самое позднее время окончания
        LocalDateTime latestEnd = subtasks.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        // Суммируем продолжительность всех подзадач
        Duration totalDuration = subtasks.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);

        // Устанавливаем значения через сеттеры
        super.setStartTime(earliestStart);
        super.setDuration(totalDuration);
        setEndTime(latestEnd);
    }


    public void addSubtask(int subtaskId) {
        subtaskIds.add(subtaskId);
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