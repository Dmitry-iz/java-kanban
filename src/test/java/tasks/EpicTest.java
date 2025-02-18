package tasks;

import static org.junit.jupiter.api.Assertions.*;

import managers.HistoryManager;
import managers.Managers;
import managers.TaskManager;
import org.junit.jupiter.api.Test;

import java.util.List;

class TaskManagerTest {

    @Test
    public void testTaskEqualityById() {
        // Проверяем, что экземпляры класса Task равны друг другу, если равен их id
        Task task1 = new Task("Task 1", "Description 1");
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description 2");
        task2.setId(1);

        assertEquals(task1.getId(), task2.getId(), "Задачи с одинаковым id должны быть равны.");
    }

    @Test
    public void testSubtaskEqualityById() {
        // Проверяем, что наследники класса Task (Subtask) равны друг другу, если равен их id
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", 1);
        subtask1.setId(1);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", 1);
        subtask2.setId(1);

        assertEquals(subtask1.getId(), subtask2.getId(), "Сабтаски с одинаковым id должны быть равны.");
    }

    @Test
    public void testEpicCannotAddItselfAsSubtask() {
        // Проверяем, что объект Epic нельзя добавить в самого себя в виде подзадачи
        TaskManager manager = Managers.getDefault();
        Epic epic = new Epic("Epic 1", "Description 1");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", "Description 1", epic.getId());
        subtask.setId(epic.getId()); // Пытаемся добавить эпик в самого себя

        assertNull(manager.createSubtask(subtask), "Эпик не должен быть добавлен в самого себя как подзадача.");
    }

    @Test
    public void testSubtaskCannotBeItsOwnEpic() {
        // Проверяем, что объект Subtask нельзя сделать своим же эпиком
        TaskManager manager = Managers.getDefault();
        Epic epic = new Epic("Epic 1", "Description 1");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", "Description 1", epic.getId());
        subtask.setEpicId(subtask.getId()); // Пытаемся сделать сабтаск своим же эпиком

        assertNull(manager.createSubtask(subtask), "Сабтаск не должен быть своим же эпиком.");
    }

    @Test
    public void testManagersInitialization() {
        // Убеждаемся, что утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры менеджеров
        TaskManager taskManager = Managers.getDefault();
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertNotNull(taskManager, "TaskManager должен быть проинициализирован.");
        assertNotNull(historyManager, "HistoryManager должен быть проинициализирован.");
    }

    @Test
    public void testAddAndFindTasksById() {
        // Проверяем, что InMemoryTaskManager действительно добавляет задачи разного типа и может найти их по id
        TaskManager manager = Managers.getDefault();
        Task task = new Task("Task 1", "Description 1");
        manager.createTask(task);

        Epic epic = new Epic("Epic 1", "Description 1");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", "Description 1", epic.getId());
        manager.createSubtask(subtask);

        assertEquals(task, manager.getTaskById(task.getId()), "Задача должна быть найдена по id.");
        assertEquals(epic, manager.getEpicById(epic.getId()), "Эпик должен быть найден по id.");
        assertEquals(subtask, manager.getSubTaskById(subtask.getId()), "Сабтаск должен быть найден по id.");
    }

    @Test
    public void testTaskIdConflict() {
        // Проверяем, что задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера
        TaskManager manager = Managers.getDefault();
        Task task1 = new Task("Task 1", "Description 1");
        task1.setId(1); // Задаем id вручную
        manager.createTask(task1);

        Task task2 = new Task("Task 2", "Description 2");
        manager.createTask(task2); // Генерируем id автоматически

        assertNotEquals(task1.getId(), task2.getId(), "Задачи с заданным и сгенерированным id не должны конфликтовать.");
    }

    @Test
    public void testTaskImmutabilityWhenAdded() {
        // Проверяем неизменность задачи (по всем полям) при добавлении задачи в менеджер
        TaskManager manager = Managers.getDefault();
        Task task = new Task("Task 1", "Description 1");
        manager.createTask(task);

        Task retrievedTask = manager.getTaskById(task.getId());
        assertEquals(task.getTitle(), retrievedTask.getTitle(), "Название задачи не должно изменяться.");
        assertEquals(task.getDescription(), retrievedTask.getDescription(), "Описание задачи не должно изменяться.");
        assertEquals(task.getStatus(), retrievedTask.getStatus(), "Статус задачи не должен изменяться.");
    }

    @Test
    public void testHistoryManagerPreservesTaskState() {
        // Убеждаемся, что задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных
        TaskManager manager = Managers.getDefault();
        Task task = new Task("Task 1", "Description 1");
        manager.createTask(task);

        Task retrievedTask = manager.getTaskById(task.getId());
        retrievedTask.setStatus(Status.IN_PROGRESS); // Изменяем статус задачи

        List<Task> history = manager.getHistory();
        Task historyTask = history.get(0);

        assertEquals(Status.NEW, historyTask.getStatus(), "История должна сохранять предыдущую версию задачи.");
    }

    @Test
    public void testEpicStatusUpdate() {
        // для проверки обновления статуса эпика
        TaskManager manager = Managers.getDefault();
        Epic epic = new Epic("Epic 1", "Description 1");
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", epic.getId());
        manager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", epic.getId());
        manager.createSubtask(subtask2);

        // Проверяем, что статус эпика NEW, так как все подзадачи NEW
        assertEquals(Status.NEW, epic.getStatus(), "Статус эпика должен быть NEW.");

        // Меняем статус одной подзадачи на IN_PROGRESS
        subtask1.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask1);

        // Проверяем, что статус эпика IN_PROGRESS
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Статус эпика должен быть IN_PROGRESS.");

        // Меняем статус обеих подзадач на DONE
        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);
        manager.updateSubtask(subtask2);

        // Проверяем, что статус эпика DONE
        assertEquals(Status.DONE, epic.getStatus(), "Статус эпика должен быть DONE.");
    }


    @Test
    public void testDeleteAllTasks() {
        //для проверки удаления всех задач
        TaskManager manager = Managers.getDefault();
        Task task1 = new Task("Task 1", "Description 1");
        manager.createTask(task1);
        Task task2 = new Task("Task 2", "Description 2");
        manager.createTask(task2);

        // Проверяем, что задачи добавлены
        assertEquals(2, manager.getAllTasks().size(), "Должно быть 2 задачи.");

        // Удаляем все задачи
        manager.deleteAllTasks();

        // Проверяем, что задачи удалены
        assertEquals(0, manager.getAllTasks().size(), "Все задачи должны быть удалены.");
    }

    @Test
    public void testHistoryManager() {
        TaskManager manager = Managers.getDefault();
        Task task1 = new Task("Task 1", "Description 1");
        manager.createTask(task1);
        Epic epic1 = new Epic("Epic 1", "Description 1");
        manager.createEpic(epic1);
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", epic1.getId());
        manager.createSubtask(subtask1);

        // Просматриваем задачи
        manager.getTaskById(task1.getId());
        manager.getEpicById(epic1.getId());
        manager.getSubTaskById(subtask1.getId());

        // Проверяем историю просмотров
        List<Task> history = manager.getHistory();
        assertEquals(3, history.size(), "В истории должно быть 3 задачи.");
        assertEquals(task1, history.get(0), "Первая задача в истории должна быть Task 1.");
        assertEquals(epic1, history.get(1), "Вторая задача в истории должна быть Epic 1.");
        assertEquals(subtask1, history.get(2), "Третья задача в истории должна быть Subtask 1.");
    }

    @Test
    public void testHistoryManager2() {
        TaskManager manager = Managers.getDefault();

        // Создаем задачи
        Task task1 = new Task("Task 1", "Description 1");
        manager.createTask(task1);
        Epic epic1 = new Epic("Epic 1", "Description 1");
        manager.createEpic(epic1);
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", epic1.getId());
        manager.createSubtask(subtask1);

        // Просматриваем задачи
        manager.getTaskById(task1.getId());
        manager.getEpicById(epic1.getId());
        manager.getSubTaskById(subtask1.getId());

        // Проверяем историю
        List<Task> history = manager.getHistory();
        assertEquals(3, history.size(), "В истории должно быть 3 задачи.");
        assertEquals(task1, history.get(0), "Первая задача в истории должна быть Task 1.");
        assertEquals(epic1, history.get(1), "Вторая задача в истории должна быть Epic 1.");
        assertEquals(subtask1, history.get(2), "Третья задача в истории должна быть Subtask 1.");

        // Проверяем, что история сохраняет состояние задачи на момент просмотра
        task1.setStatus(Status.IN_PROGRESS);
        assertEquals(Status.NEW, history.get(0).getStatus(), "История должна сохранять состояние задачи на момент просмотра.");

        // Проверяем ограничение истории 10 элементами
        for (int i = 0; i < 10; i++) {
            Task task = new Task("Task " + i, "Description " + i);
            manager.createTask(task);
            manager.getTaskById(task.getId());
        }

        history = manager.getHistory();
        assertEquals(10, history.size(), "История должна содержать не более 10 задач.");
        assertEquals("Task 0", history.get(0).getTitle(), "Первая задача в истории должна быть Task 0.");
        assertEquals("Task 9", history.get(9).getTitle(), "Последняя задача в истории должна быть Task 9.");
    }
}