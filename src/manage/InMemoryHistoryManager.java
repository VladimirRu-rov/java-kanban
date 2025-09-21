package manage;

import task.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Task> taskById = new HashMap<>();

    // Двусвязный список для хранения истории просмотров
    private Node head; // Начало списка
    private Node tail; // Конец списка

    // Вспомогательная структура для быстрого доступа к узлам по идентификатору задачи
    private final Map<Integer, Node> nodeMap = new HashMap<>();

    @Override
    public void add(Task task) {
        // Проверяем, присутствует ли задача в истории раньше, если задача уже есть, удаляем предыдущее её положение
        if (nodeMap.containsKey(task.getId())) {
            Node existingNode = nodeMap.get(task.getId());
            removeNode(existingNode);
        }

        // Создаем новый узел и добавляем его в конец списка
        Node newNode = new Node(task);
        linkLast(newNode);
        nodeMap.put(task.getId(), newNode); // Обновляем карту
    }

    @Override
    public List<Task> getHistory() {
        List<Task> result = new ArrayList<>();
        Node current = head;
        while (current != null) {
            result.add(current.task);
            current = current.next;
        }
        return result;
    }

    // Удаляет задачу из истории просмотров по её идентификатору
    @Override
    public void remove(int id) {
        Node nodeToRemove = nodeMap.get(id);
        if (nodeToRemove != null) {
            removeNode(nodeToRemove); // Удаляем узел
            nodeMap.remove(id); // Удаляем из карты
        }
    }

    // Метод для добавления узла в конец списка
    private void linkLast(Node newNode) {
        if (tail == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
    }

    // Приватный метод для удаления узла из списка
    private void removeNode(Node node) {
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
    }
}