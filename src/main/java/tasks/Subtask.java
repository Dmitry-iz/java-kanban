package tasks;

public class Subtask extends Task {
    private int epicId; // Id эпика, к которому относится подзадача

    public Subtask(String title, String description, int epicId) {
        super(title, description);
        this.epicId = epicId;
    }

    public Subtask(Subtask subtask) {
        super(subtask); // Копируем поля задачи
        this.epicId = subtask.epicId; // Копируем id эпика
    }

    // Возвращает id эпика
    public int getEpicId() {
        return epicId;
    }

    // Устанавливает id эпика
    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "tasks.Subtask{" +
                "title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus() +
                ", epicId=" + epicId +
                '}';
    }
}