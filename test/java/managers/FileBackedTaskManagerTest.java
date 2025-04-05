package managers;

import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {
    private FileBackedTaskManager manager; // Менеджер задач, который будет тестироваться
    private File tempFile; // Временный файл для хранения задач в формате CSV

    @BeforeEach
    public void setUp() throws IOException {
        // Создаем временный файл для тестирования, который будет использоваться для сохранения и загрузки задач
        tempFile = File.createTempFile("tasks", ".csv");
        manager = new FileBackedTaskManager(tempFile); // Инициализируем менеджер с временным файлом
    }

    @AfterEach
    public void tearDown() {
        // Удаляем временный файл после завершения каждого теста, чтобы избежать накопления ненужных файлов
        tempFile.delete();
    }

    @Test
    public void testSaveAndLoadEmptyFile() {
        // Проверяем, что менеджер задач пустой (не содержит задач, эпиков и подзадач)
        assertEquals(0, manager.getAllTasks().size());
        assertEquals(0, manager.getAllEpics().size());
        assertEquals(0, manager.getAllSubtasks().size());

        // Загружаем из файла, который должен быть пустым
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        // Проверяем, что загруженный менеджер также пустой
        assertEquals(0, loadedManager.getAllTasks().size());
        assertEquals(0, loadedManager.getAllEpics().size());
        assertEquals(0, loadedManager.getAllSubtasks().size());
    }

    @Test
    public void testSaveAndLoadMultipleTasks() throws IOException {
        // Создаем временный файл для хранения задач
        File file = File.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file); // Инициализируем новый менеджер

        // Создаем задачи и эпики
        Task task1 = manager.createTask(new Task("Task 1", "Description 1")); // Создаем первую задачу
        Task task2 = manager.createTask(new Task("Task 2", "Description 2")); // Создаем вторую задачу
        Epic epic1 = manager.createEpic(new Epic("Epic 1", "Description Epic 1")); // Создаем эпик
        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask 1", "Description Subtask 1", epic1.getId())); // Создаем подзадачу

        // Сохраняем состояние менеджера в файл
        manager.save();

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Проверяем, что загруженный менеджер содержит те же задачи
        assertEquals(2, loadedManager.getAllTasks().size()); // Проверяем количество задач
        assertEquals(1, loadedManager.getAllEpics().size()); // Проверяем количество эпиков
        assertEquals(1, loadedManager.getAllSubtasks().size()); // Проверяем количество подзадач

        // Проверяем, что задачи совпадают по содержимому
        Task loadedTask1 = loadedManager.getTaskById(task1.getId()); // Загружаем первую задачу
        assertNotNull(loadedTask1); // Проверяем, что задача не равна null
        assertEquals(task1.getTitle(), loadedTask1.getTitle()); // Проверяем заголовок
        assertEquals(task1.getDescription(), loadedTask1.getDescription()); // Проверяем описание

        Task loadedTask2 = loadedManager.getTaskById(task2.getId()); // Загружаем вторую задачу
        assertNotNull(loadedTask2); // Проверяем, что задача не равна null
        assertEquals(task2.getTitle(), loadedTask2.getTitle()); // Проверяем заголовок
        assertEquals(task2.getDescription(), loadedTask2.getDescription()); // Проверяем описание

        Epic loadedEpic1 = loadedManager.getEpicById(epic1.getId()); // Загружаем эпик
        assertNotNull(loadedEpic1); // Проверяем, что эпик не равен null
        assertEquals(epic1.getTitle(), loadedEpic1.getTitle()); // Проверяем заголовок
        assertEquals(epic1.getDescription(), loadedEpic1.getDescription()); // Проверяем описание

        Subtask loadedSubtask1 = loadedManager.getSubTaskById(subtask1.getId()); // Загружаем подзадачу
        assertNotNull(loadedSubtask1); // Проверяем, что подзадача не равна null
        assertEquals(subtask1.getTitle(), loadedSubtask1.getTitle()); // Проверяем заголовок
        assertEquals(subtask1.getDescription(), loadedSubtask1.getDescription()); // Проверяем описание
        assertEquals(subtask1.getEpicId(), loadedSubtask1.getEpicId()); // Проверяем ID эпика, к которому принадлежит подзадача
    }

    @Test
    public void testSaveAndLoadWithStatusChange() {
        // Создаем задачи и эпики
        Task task1 = manager.createTask(new Task("Task 1", "Description 1")); // Создаем первую задачу
        Epic epic1 = manager.createEpic(new Epic("Epic 1", "Description Epic 1")); // Создаем эпик
        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask 1", "Description Subtask 1", epic1.getId())); // Создаем подзадачу

        // Меняем статусы задач
        task1.setStatus(Status.IN_PROGRESS); // Устанавливаем статус "В процессе" для задачи
        manager.updateTask(task1); // Обновляем задачу в менеджере
        subtask1.setStatus(Status.DONE); // Устанавливаем статус "Выполнено" для подзадачи
        manager.updateSubtask(subtask1); // Обновляем подзадачу в менеджере

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // Проверяем, что статусы задач загружены корректно
        assertEquals(Status.IN_PROGRESS, loadedManager.getTaskById(task1.getId()).getStatus()); // Проверяем статус задачи
        assertEquals(Status.DONE, loadedManager.getSubTaskById(subtask1.getId()).getStatus()); // Проверяем статус подзадачи
    }

    @Test
    public void testLoadFromNonExistentFile() {
        // Проверяем, что при попытке загрузки из несуществующего файла выбрасывается исключение
        assertThrows(ManagerSaveException.class, () -> {
            FileBackedTaskManager.loadFromFile(new File("non_existent_file.csv")); // Пытаемся загрузить из несуществующего файла
        });
    }
}