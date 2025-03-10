package managers;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    // Внутренний класс для узла двусвязного списка
    private static class Node {
        Task task; // Задача, хранящаяся в узле
        Node prev; // Ссылка на предыдущий узел
        Node next; // Ссылка на следующий узел

        Node(Task task, Node prev, Node next) {
            this.task = task;
            this.prev = prev;
            this.next = next;
        }
    }

    // Хранит узлы по id задач для быстрого доступа
    private final Map<Integer, Node> historyMap = new HashMap<>();
    private Node head; // Начало списка
    private Node tail; // Конец списка


    @Override
    public void add(Task task) {
        if (task == null) {
            return; // Игнорируем null-задачи
        }
        // Удаляем задачу, если она уже есть в истории
        remove(task.getId());

        // Создаем клон задачи в зависимости от её типа
        Task clonedTask;
        if (task instanceof Epic) {
            clonedTask = new Epic((Epic) task); // Клонируем Epic
        } else if (task instanceof Subtask) {
            clonedTask = new Subtask((Subtask) task); // Клонируем Subtask
        } else {
            clonedTask = new Task(task); // Клонируем обычную Task
        }

        // Добавляем клон в историю
        linkLast(clonedTask);
    }

    @Override
    public void remove(int id) {
        Node node = historyMap.get(id);
        if (node != null) {
            // Удаляем узел из списка
            removeNode(node);
            // Удаляем узел из мапы
            historyMap.remove(id);
        }
    }

    @Override
    public List<Task> getHistory() {
        List<Task> history = new ArrayList<>();
        Node current = head;
        // Проходим по списку и собираем задачи
        while (current != null) {
            history.add(current.task);
            current = current.next;
        }
        return history;
    }

    // Добавляет задачу в конец двусвязного списка
    private void linkLast(Task task) {
        final Node newNode = new Node(task, tail, null);
        if (tail == null) {
            head = newNode; // Если список пуст, новый узел становится головой
        } else {
            tail.next = newNode; // Иначе добавляем узел в конец
        }
        tail = newNode; // Обновляем хвост списка
        historyMap.put(task.getId(), newNode); // Сохраняем узел в мапе
    }

    // Удаляет узел из двусвязного списка
    private void removeNode(Node node) {
        if (node.prev != null) {
            node.prev.next = node.next; // Обновляем ссылку у предыдущего узла
        } else {
            head = node.next; // Если удаляем голову, обновляем head
        }

        if (node.next != null) {
            node.next.prev = node.prev; // Обновляем ссылку у следующего узла
        } else {
            tail = node.prev; // Если удаляем хвост, обновляем tail
        }
    }
}