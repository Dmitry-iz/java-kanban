import managers.Managers;
import managers.TaskManager;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

public class Main {
    public static void main(String[] args) {
        System.out.println("Поехали!");

        TaskManager taskManager = Managers.getDefault();

        // Создаем задачи
        Task task1 = taskManager.createTask(new Task("Task 1", "Description 1"));
        Task task2 = taskManager.createTask(new Task("Task 2", "Description 2"));

        // Создаем эпик с тремя подзадачами
        Epic epic1 = taskManager.createEpic(new Epic("Epic 1", "Description 1"));
        Subtask subtask1 = taskManager.createSubtask(new Subtask("Subtask 1", "Description 1", epic1.getId()));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Subtask 2", "Description 2", epic1.getId()));
        Subtask subtask3 = taskManager.createSubtask(new Subtask("Subtask 3", "Description 3", epic1.getId()));

        // Создаем эпик без подзадач
        Epic epic2 = taskManager.createEpic(new Epic("Epic 2", "Description 2"));

        // Запрашиваем задачи несколько раз
        taskManager.getTaskById(task1.getId()); // Задача 1
        taskManager.getTaskById(task2.getId());  // Задача 2

        taskManager.getEpicById(epic1.getId()); // Эпик 1
        taskManager.getEpicById(epic2.getId()); // Эпик 2

        taskManager.getSubTaskById(subtask1.getId()); // Подзадача 1

        taskManager.getTaskById(task1.getId()); // Повторный запрос задачи 1

        // Выводим историю
        System.out.println("История после запросов:");
        taskManager.getHistory().forEach(System.out::println);

        // Удаляем задачу 1 и проверяем историю
        taskManager.deleteTask(task1.getId());
        System.out.println("История после удаления задачи 1:");
        taskManager.getHistory().forEach(System.out::println);

        // Удаляем эпик 1 и проверяем историю
        taskManager.deleteEpic(epic1.getId());
        System.out.println("История после удаления эпика 1:");
        taskManager.getHistory().forEach(System.out::println);
    }
}