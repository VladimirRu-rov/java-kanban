package manage;

import task.Task;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Task> taskById = new HashMap<>();
    private Node head; // Начало списка
    private Node tail; // Конец списка
    private final Map<Integer, Node> nodeMap = new HashMap<>();

    @Override
    public void add(Task task) {
        // Если задача уже была просмотрена, удаляем её прежнюю позицию
        if (nodeMap.containsKey(task.getId())) {
            Node existingNode = nodeMap.get(task.getId());
            removeNode(existingNode); // удаляем старую позицию
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

    @Override
    public void remove(int id) {
        Node nodeToRemove = nodeMap.get(id);
        if (nodeToRemove != null) {
            removeNode(nodeToRemove); // удаляем узел
            nodeMap.remove(id); // удаляем из карты
        }
    }

    // Приватный метод для добавления узла в конец списка
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