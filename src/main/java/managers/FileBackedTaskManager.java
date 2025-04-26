package managers;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


import tasks.Epic;
import tasks.Subtask;
import tasks.Task;


/**
 * Менеджер задач, который автоматически сохраняет состояние в файл.
 * Наследует функциональность InMemoryTaskManager и добавляет возможность сохранения и загрузки данных из файла.
 */
public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file; // Файл для автосохранения

    /**
     * Конструктор, принимающий файл для сохранения данных.
     *
     * @param file Файл, в который будут сохраняться данные.
     */
    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    /**
     * Сохраняет текущее состояние менеджера в файл.
     * Данные сохраняются в формате CSV.
     */
    public void save() {
        try (FileWriter writer = new FileWriter(file)) {
            // Обновленный заголовок с новыми полями
            writer.write("id,type,name,status,description,epic,start_time,duration\n");

            // Сохраняем все типы задач
            for (Task task : getAllTasks()) {
                writer.write(FormatterUtil.toString(task) + "\n");
            }
            for (Epic epic : getAllEpics()) {
                writer.write(FormatterUtil.toString(epic) + "\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(FormatterUtil.toString(subtask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении файла", e);
        }
    }

    /**
     * Восстанавливает состояние менеджера из файла.
     *
     * @param file Файл, из которого будут загружены данные.
     * @return Новый экземпляр FileBackedTaskManager с восстановленными данными.
     */
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");

            for (int i = 1; i < lines.length; i++) {
                Task task = FormatterUtil.fromString(lines[i]);
                if (task != null) {
                    if (task instanceof Epic) {
                        manager.epics.put(task.getId(), (Epic) task);
                    } else if (task instanceof Subtask) {
                        manager.subtasks.put(task.getId(), (Subtask) task);
                        Epic epic = manager.epics.get(((Subtask) task).getEpicId());
                        if (epic != null) {
                            epic.addSubtask(task.getId());
                        }
                    } else {
                        manager.tasks.put(task.getId(), task);
                    }
                }
            }

            // Обновляем время эпиков после загрузки
            for (Epic epic : manager.epics.values()) {
                List<Subtask> subs = epic.getSubtaskIds().stream()
                        .map(manager.subtasks::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                epic.updateTiming(subs);
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке файла", e);
        }
        return manager;
    }

    // Переопределение методов для добавления автосохранения
    @Override
    public Task createTask(Task task) {
        Task newTask = super.createTask(task);
        save(); // Сохраняем состояние после создания задачи
        return newTask;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask newSubtask = super.createSubtask(subtask);
        save(); // Сохраняем состояние после создания подзадачи
        return newSubtask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic newEpic = super.createEpic(epic);
        save(); // Сохраняем состояние после создания эпика
        return newEpic;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save(); // Сохраняем состояние после обновления задачи
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save(); // Сохраняем состояние после обновления подзадачи
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save(); // Сохраняем состояние после обновления эпика
    }

    @Override
    public int deleteTask(int id) {
        int result = super.deleteTask(id);
        save(); // Сохраняем состояние после удаления задачи
        return result;
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save(); // Сохраняем состояние после удаления всех задач
    }

    @Override
    public void deleteAllSubTasks() {
        super.deleteAllSubTasks();
        save(); // Сохраняем состояние после удаления всех подзадач
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save(); // Сохраняем состояние после удаления всех эпиков
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save(); // Сохраняем состояние после удаления подзадачи
    }

    @Override
    public int deleteEpic(int id) {
        int result = super.deleteEpic(id);
        save(); // Сохраняем состояние после удаления эпика
        return result;
    }

    //Основной метод для тестирования функциональности FileBackedTaskManager.
    public static void main(String[] args) {
        File file = new File("tasks.csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try {
            // Создаем задачи с временем
            Task task1 = new Task("Task 1", "Description 1");
            task1.setStartTime(LocalDateTime.of(2024, 3, 1, 9, 0));
            task1.setDuration(Duration.ofHours(2));
            manager.createTask(task1);

            Task task2 = new Task("Task 2", "Description 2");
            task2.setStartTime(LocalDateTime.of(2024, 4, 1, 11, 0));
            task2.setDuration(Duration.ofMinutes(30));
            manager.createTask(task2);

            // Создаем эпик с подзадачами
            Epic epic1 = manager.createEpic(new Epic("Epic 1", "Epic description"));

            Subtask subtask1 = new Subtask("Subtask 1", "Sub 1", epic1.getId());
            subtask1.setStartTime(LocalDateTime.of(2024, 5, 2, 10, 0));
            subtask1.setDuration(Duration.ofHours(1));
            manager.createSubtask(subtask1);

            Subtask subtask2 = new Subtask("Subtask 2", "Sub 2", epic1.getId());
            subtask2.setStartTime(LocalDateTime.of(2024, 6, 2, 12, 0));
            subtask2.setDuration(Duration.ofHours(2));
            manager.createSubtask(subtask2);

            // Тестируем приоритетный список
            System.out.println("\nPrioritized tasks:");
            manager.getPrioritizedTasks().forEach(task ->
                    System.out.printf("%s: %s - %s (Duration: %d min)%n",
                            task.getStartTime(),
                            task.getEndTime(),
                            task.getTitle(),
                            task.getDuration().toMinutes())
            );

            // Тестируем время эпика
            Epic loadedEpic = manager.getEpicById(epic1.getId());
            System.out.println("\nEpic timing:");
            System.out.printf("Start: %s%nEnd: %s%nDuration: %d min%n",
                    loadedEpic.getStartTime(),
                    loadedEpic.getEndTime(),
                    loadedEpic.getDuration().toMinutes());

            // Тестируем пересечение задач
            try {
                Task invalidTask = new Task("Invalid", "Should overlap");
                invalidTask.setStartTime(LocalDateTime.of(2024, 3, 1, 10, 30));
                invalidTask.setDuration(Duration.ofHours(1));
                manager.createTask(invalidTask);
            } catch (ManagerValidationException e) {
                System.out.println("\nValidation test passed: " + e.getMessage());
            }

            // Сохраняем и загружаем из файла
            FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

            // Проверяем восстановленные данные
            System.out.println("\nAfter loading from file:");
            System.out.println("Tasks: " + loadedManager.getAllTasks().size());
            System.out.println("Epics: " + loadedManager.getAllEpics().size());
            System.out.println("Subtasks: " + loadedManager.getAllSubtasks().size());

        } finally {
            file.delete();
        }
    }

    public void clearAllData() {       // использовал для собственных тестов и отладки
        deleteAllTasks();    // Удалить все задачи
        deleteAllEpics();    // Удалить все эпики (включая подзадачи)
        idCounter = 0; // Сбрасываем счетчик
        save();              // Перезаписать файл с заголовком
    }
}

