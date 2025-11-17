package manager;

import manager.history.HistoryManager;
import manager.history.InMemoryHistoryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Status;
import task.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    public void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    public void testEmptyHistory() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }

    @Test
    public void testAddingTaskToHistory() {
        Task task = new Task(1, "Задача №1", "Описание", Status.NEW, null, null);
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertSame(task, history.get(0));
    }

    @Test
    public void testRemovingFirstItem() {
        Task task1 = new Task(1, "Задача №1", "Описание", Status.NEW, null, null);
        Task task2 = new Task(2, "Задача №2", "Описание", Status.NEW, null, null);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(task1.getId());
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertSame(task2, history.get(0));
    }

    @Test
    public void testRemovingLastItem() {
        Task task1 = new Task(1, "Задача №1", "Описание", Status.NEW, null, null);
        Task task2 = new Task(2, "Задача №2", "Описание", Status.NEW, null, null);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(task2.getId());
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertSame(task1, history.get(0));
    }

    @Test
    public void testDuplicateItems() {
        Task task = new Task(1, "Задача №1", "Описание", Status.NEW, null, null);
        historyManager.add(task);
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
    }

    @Test
    public void testClearingHistory() {
        Task task1 = new Task(1, "Задача №1", "Описание", Status.NEW, null, null);
        Task task2 = new Task(2, "Задача №2", "Описание", Status.NEW, null, null);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.removeAll();
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }

    @Test
    public void testRemoveMiddleElementFromHistory() {
        Task task1 = new Task(1, "Задача #1", "Описание", Status.NEW, null, null);
        Task task2 = new Task(2, "Задача #2", "Описание", Status.NEW, null, null);
        Task task3 = new Task(3, "Задача #3", "Описание", Status.NEW, null, null);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<Task> initialHistory = historyManager.getHistory();
        assertEquals(3, initialHistory.size(), "Изначально в истории должно быть 3 задачи");
        assertSame(task3, initialHistory.get(0), "Первый элемент должен быть task3 (последняя добавленная)");
        assertSame(task2, initialHistory.get(1), "Второй элемент должен быть task2");
        assertSame(task1, initialHistory.get(2), "Третий элемент должен быть task1");

        historyManager.remove(task2.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "После удаления должно остаться 2 задачи");

        assertSame(task3, history.get(0), "Первый элемент должен быть task3");
        assertSame(task1, history.get(1), "Второй элемент должен быть task1 (task2 удалён)");
    }
}