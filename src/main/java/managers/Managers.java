package managers;

public class Managers {
    // Возвращает реализацию TaskManager
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    // Возвращает реализацию HistoryManager
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}