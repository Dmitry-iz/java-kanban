package managers;


import managers.FileBackedTaskManager;
import managers.ManagerSaveException;
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

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File tempFile;

    @BeforeEach
    public void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        manager = new FileBackedTaskManager(tempFile);
    }

    @AfterEach
    public void tearDown() {
        tempFile.delete();
    }

    @Test
    public void testSaveAndLoadEmptyFile() {
        assertEquals(0, manager.getAllTasks().size());
        assertEquals(0, manager.getAllEpics().size());
        assertEquals(0, manager.getAllSubtasks().size());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(0, loadedManager.getAllTasks().size());
        assertEquals(0, loadedManager.getAllEpics().size());
        assertEquals(0, loadedManager.getAllSubtasks().size());
    }

    @Test
    public void testSaveAndLoadMultipleTasks() throws IOException {
        Task task1 = manager.createTask(new Task("Task 1", "Description 1"));
        Task task2 = manager.createTask(new Task("Task 2", "Description 2"));
        Epic epic1 = manager.createEpic(new Epic("Epic 1", "Description Epic 1"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask 1", "Description Subtask 1", epic1.getId()));

        manager.save();
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(2, loadedManager.getAllTasks().size());
        assertEquals(1, loadedManager.getAllEpics().size());
        assertEquals(1, loadedManager.getAllSubtasks().size());

        Task loadedTask1 = loadedManager.getTaskById(task1.getId());
        assertNotNull(loadedTask1);
        assertEquals(task1.getTitle(), loadedTask1.getTitle());
        assertEquals(task1.getDescription(), loadedTask1.getDescription());

        Task loadedTask2 = loadedManager.getTaskById(task2.getId());
        assertNotNull(loadedTask2);
        assertEquals(task2.getTitle(), loadedTask2.getTitle());
        assertEquals(task2.getDescription(), loadedTask2.getDescription());

        Epic loadedEpic1 = loadedManager.getEpicById(epic1.getId());
        assertNotNull(loadedEpic1);
        assertEquals(epic1.getTitle(), loadedEpic1.getTitle());
        assertEquals(epic1.getDescription(), loadedEpic1.getDescription());

        Subtask loadedSubtask1 = loadedManager.getSubTaskById(subtask1.getId());
        assertNotNull(loadedSubtask1);
        assertEquals(subtask1.getTitle(), loadedSubtask1.getTitle());
        assertEquals(subtask1.getDescription(), loadedSubtask1.getDescription());
        assertEquals(subtask1.getEpicId(), loadedSubtask1.getEpicId());
    }

    @Test
    public void testSaveAndLoadWithStatusChange() {
        Task task1 = manager.createTask(new Task("Task 1", "Description 1"));
        Epic epic1 = manager.createEpic(new Epic("Epic 1", "Description Epic 1"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask 1", "Description Subtask 1", epic1.getId()));

        task1.setStatus(Status.IN_PROGRESS);
        manager.updateTask(task1);
        subtask1.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(Status.IN_PROGRESS, loadedManager.getTaskById(task1.getId()).getStatus());
        assertEquals(Status.DONE, loadedManager.getSubTaskById(subtask1.getId()).getStatus());
    }

    @Test
    public void testLoadFromNonExistentFile() {
        assertThrows(ManagerSaveException.class, () -> {
            FileBackedTaskManager.loadFromFile(new File("non_existent_file.csv"));
        });
    }
}