package tasks;

import com.google.gson.annotations.SerializedName;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    // Поля класса
    @SerializedName("title")
    private String title;       // Название задачи
    @SerializedName("description")
    private String description; // Описание задачи
    @SerializedName("id")
    private int id;             // Уникальный идентификатор задачи
    @SerializedName("status")
    private Status status; // Status.NEW; // Значение по умолчанию
    @SerializedName("startTime")
    private LocalDateTime startTime;
    @SerializedName("duration")
    private Duration duration;

    // Конструктор для создания новой задачи
    public Task(String title, String description) {
        this.title = title;
        this.description = description;
        this.setStatus(Status.NEW); // По умолчанию статус NEW
    }

    // Конструктор для копирования задачи
    public Task(Task task) {
        this.title = task.title;
        this.description = task.description;
        this.id = task.id;
        this.status = task.status;
        this.startTime = task.startTime;
        this.duration = task.duration;
    }

    // Конструктор для Gson, иначе он не сможет десериализовать объект с полями
    public Task(
            String title,
            String description,
            Status status,
            LocalDateTime startTime,
            Duration duration
    ) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task() {
        this.setStatus(Status.NEW);
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    // Геттер для названия задачи
    public String getTitle() {
        return title;
    }

    // Геттер для описания задачи
    public String getDescription() {
        return description;
    }

    // Геттер для идентификатора задачи
    public int getId() {
        return id;
    }

    // Геттер для статуса задачи
    public Status getStatus() {
        return status;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    // Сеттер для идентификатора задачи
    public void setId(int id) {
        this.id = id;
    }

    // Сеттер для названия задачи
    public void setTitle(String title) {
        this.title = title;
    }

    // Сеттер для описания задачи
    public void setDescription(String description) {
        this.description = description;
    }

    // Сеттер для статуса задачи
    public void setStatus(Status status) {
        //this.status = status;
        this.status = (status != null) ? status : Status.NEW;
    }

    // Переопределение метода equals для сравнения задач
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Проверка на ссылочное равенство
        if (o == null || getClass() != o.getClass()) return false; // Проверка на null и класс

        Task task = (Task) o;
        // Сравнение по id, названию, описанию и статусу
        return id == task.id && Objects.equals(title, task.title)
                && Objects.equals(description, task.description)
                && status == task.status;
    }

    // Переопределение метода hashCode для корректной работы с коллекциями
    @Override
    public int hashCode() {
        int result = Objects.hashCode(title);
        result = 31 * result + Objects.hashCode(description);
        result = 31 * result + id;
        result = 31 * result + Objects.hashCode(status);
        return result;
    }

    // Переопределение метода toString для удобного вывода информации о задаче
    @Override
    public String toString() {
        return "Task{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                ", startTime=" + startTime +
                ", duration=" + duration +
                '}';
    }
}