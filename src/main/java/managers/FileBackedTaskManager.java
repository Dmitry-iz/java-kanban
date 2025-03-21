package managers;

import tasks.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskType;

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
            // Записываем заголовок CSV
            writer.write("id,type,name,status,description,epic\n");

            // Сохраняем задачи
            for (Task task : getAllTasks()) {
                writer.write(toString(task) + "\n");
            }

            // Сохраняем эпики
            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic) + "\n");
            }

            // Сохраняем подзадачи
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(toString(subtask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении файла", e);
        }
    }

    /**
     * Преобразует задачу в строку CSV.
     *
     * @param task Задача, которую нужно преобразовать.
     * @return Строка в формате CSV.
     */
    private String toString(Task task) {
        // Определяем тип задачи
        String type = task instanceof Epic ? TaskType.EPIC.name() :
                task instanceof Subtask ? TaskType.SUBTASK.name() : TaskType.TASK.name();

        // Получаем ID эпика для подзадачи (если это подзадача)
        String epicId = task instanceof Subtask ? String.valueOf(((Subtask) task).getEpicId()) : "";

        // Собираем строку CSV
        return String.join(",",
                String.valueOf(task.getId()),
                type,
                task.getTitle(),
                task.getStatus().name(),
                task.getDescription(),
                epicId);
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
            // Читаем содержимое файла
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");

            // Пропускаем заголовок и обрабатываем каждую строку
            for (int i = 1; i < lines.length; i++) {
                Task task = fromString(lines[i]);
                if (task != null) {
                    // В зависимости от типа задачи добавляем её в соответствующее хранилище
                    if (task instanceof Epic) {
                        manager.epics.put(task.getId(), (Epic) task);
                    } else if (task instanceof Subtask) {
                        manager.subtasks.put(task.getId(), (Subtask) task);
                    } else {
                        manager.tasks.put(task.getId(), task);
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке файла", e);
        }
        return manager;
    }

    /**
     * Создает задачу из строки CSV.
     *
     * @param value Строка CSV, содержащая данные задачи.
     * @return Задача, созданная из строки.
     */
    private static Task fromString(String value) {
        // Разделяем строку на поля
        String[] fields = value.split(",", 6); // Указываем limit=6, чтобы избежать лишних разделений
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String title = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4].isEmpty() ? "" : fields[4]; // Пустое описание
        int epicId = fields.length > 5 && !fields[5].isEmpty() ? Integer.parseInt(fields[5]) : -1;

        // В зависимости от типа задачи создаем соответствующий объект
        switch (type) {
            case TASK:
                Task task = new Task(title, description);
                task.setId(id);
                task.setStatus(status);
                return task;
            case EPIC:
                Epic epic = new Epic(title, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                Subtask subtask = new Subtask(title, description, epicId);
                subtask.setId(id);
                subtask.setStatus(status);
                return subtask;
            default:
                return null;
        }
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
        // Создаем временный файл для тестирования
        File file;
        try {
            file = File.createTempFile("tasks", ".csv");
        } catch (IOException e) {
            System.err.println("Ошибка при создании временного файла: " + e.getMessage());
            return;
        }

        // Создаем менеджер с автосохранением в файл
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        // Создаем задачи
        Task task1 = manager.createTask(new Task("Task 1", "Описание задачи 1"));
        Task task2 = manager.createTask(new Task("Task 2", "Описание задачи 2"));

        // Создаем эпик с подзадачами
        Epic epic1 = manager.createEpic(new Epic("Epic 1", "Описание эпика 1"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask 1", "Описание подзадачи 1", epic1.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Subtask 2", "Описание подзадачи 2", epic1.getId()));

        // Просматриваем задачи, чтобы добавить их в историю
        manager.getTaskById(task1.getId()); // Просмотр задачи 1
        manager.getEpicById(epic1.getId()); // Просмотр эпика 1
        manager.getSubTaskById(subtask1.getId()); // Просмотр подзадачи 1

        // Меняем статусы задач
        task1.setStatus(Status.IN_PROGRESS);
        manager.updateTask(task1);

        subtask1.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);

        subtask2.setStatus(Status.DONE);
        manager.updateSubtask(subtask2);

        // Обновляем статус эпика 1 (должен стать DONE, так как все подзадачи выполнены)
        manager.updateEpicStatus(epic1.getId());

        // Выводим список задач, эпиков и подзадач
        System.out.println("Созданные задачи:");
        System.out.println(manager.getAllTasks());
        System.out.println("Созданные эпики:");
        System.out.println(manager.getAllEpics());
        System.out.println("Созданные подзадачи:");
        System.out.println(manager.getAllSubtasks());

        // Выводим содержимое файла для наглядности
        System.out.println("\nСодержимое файла:");
        try {
            String content = Files.readString(file.toPath());
            System.out.println(content);
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }

        // Восстанавливаем менеджер из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Проверяем, что задачи, эпики и подзадачи восстановлены
        System.out.println("\nЗадачи после восстановления:");
        System.out.println(loadedManager.getAllTasks());
        System.out.println("Эпики после восстановления:");
        System.out.println(loadedManager.getAllEpics());
        System.out.println("Подзадачи после восстановления:");
        System.out.println(loadedManager.getAllSubtasks());

        // Удаляем временный файл после завершения работы
        file.deleteOnExit();
    }
}

