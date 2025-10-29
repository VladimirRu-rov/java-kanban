package manage;

import task.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private Node head; // Начало списка
    private Node tail; // Конец списка
    private final Map<Integer, Node> nodeMap = new HashMap<>();

    @Override
    public void add(Task task) {
        Node existingNode = nodeMap.get(task.getId());
        removeNode(existingNode);
        Node newNode = new Node(task);
        linkLast(newNode);
        nodeMap.put(task.getId(), newNode);
    }

    private List<Task> getTasks(Node node) {
        List<Task> tasks = new ArrayList<>();
        while (node != null) {
            tasks.add(node.task);
            node = node.next;
        }
        return tasks;
    }

    @Override
    public List<Task> getHistory() {
        return getTasks(head);
    }

    @Override
    public void remove(int id) {
        Node nodeToRemove = nodeMap.get(id);
        if (nodeToRemove != null) {
            removeNode(nodeToRemove);
            nodeMap.remove(id);
        }
    }

    @Override
    public void removeAll() {
        head = null;
        tail = null;
        nodeMap.clear();
    }

    private void linkLast(Node newNode) {
        if (tail == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
    }

    private void removeNode(Node node) {
        if (node == null) return;

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