import managers.Managers;
import managers.TaskManager;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;
public class Main {
    public static void main(String[] args) {
        System.out.println("Поехали!");


        TaskManager taskManager = Managers.getDefault();

        // Создаем задачи
        Task task1 = new Task("Task 1", "Description 1");
        taskManager.createTask(task1);
        Task task2 = new Task("Task 2", "Description 2");
        taskManager.createTask(task2);

        // Создаем эпик
        Epic epic1 = new Epic("Epic 1", "Description 1");
        taskManager.createEpic(epic1);

        //Обновим эпик
        System.out.println(epic1 + " эпик до обновления");
        epic1.setTitle("updated title");
        taskManager.updateEpic(epic1);
        System.out.println(epic1 + " эпик после обновления");

        // Создаем подзадачи для эпика
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", epic1.getId());
        taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", epic1.getId());
        taskManager.createSubtask(subtask2);

        // Выводим все задачи, эпики и подзадачи
        System.out.println("Все задачи:");
        taskManager.getAllTasks().forEach(System.out::println);

        System.out.println("Все эпики:");
        taskManager.getAllEpics().forEach(System.out::println);

        System.out.println("Все подзадачи:");
        taskManager.getAllSubtasks().forEach(System.out::println);

        // Обновляем статусы задач и подзадач
        task1.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(task1);

        subtask1.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);

        // Проверяем обновление статуса эпика
        System.out.println("Статус эпика после обновления подзадачи:");
        System.out.println(taskManager.getEpicById(epic1.getId()));

        // Удаляем одну задачу и одну подзадачу
        taskManager.deleteTask(task1.getId());
        taskManager.deleteSubtask(subtask2.getId());

        // Проверяем историю просмотров
        System.out.println("История просмотров:");
        taskManager.getHistory().forEach(System.out::println);

        // удалим 1 эпик
        taskManager.deleteEpic(1);

        // Удаляем все задачи, эпики и подзадачи
        taskManager.deleteAllTasks();
        taskManager.deleteAllEpics();
        taskManager.deleteAllSubTasks();

        // Проверяем, что все задачи удалены
        System.out.println("Все задачи после удаления:");
        taskManager.getAllTasks().forEach(System.out::println);
    }
}
