import java.util.ArrayList;
import java.util.List;

class Epic extends Task {
    private List<Integer> subtaskIds = new ArrayList<>();


    public Epic(String title, String description) {
        super(title, description);
    }

    // Метод для добавления подзадачи
    public void addSubtask(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    // Метод для получения списка идентификаторов подзадач
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
                '}';
    }
}