import manage.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.*;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;

    @BeforeEach
    public void setUp() {
        taskManager = createTaskManagerInstance();
    }

    protected abstract T createTaskManagerInstance();

    @Test
    public void testAddingAndGettingTask() {
        Task task = new Task(1, "Задача 1", "Описание", Status.NEW, Duration.ZERO, null);
        taskManager.addTask(task);
        Task retrievedTask = taskManager.getTaskByID(task.getId());
        assertEquals(task, retrievedTask);
    }

    @Test
    public void testUpdatingTask() {
        Task task = new Task(1, "Задача 1", "Описание", Status.NEW, Duration.ZERO, null);
        taskManager.addTask(task);
        Task updatedTask = new Task(task.getId(), "Изменённая задача", "Изменённое описание", Status.IN_PROGRESS, Duration.ZERO, null);
        taskManager.updateTask(updatedTask);
        Task result = taskManager.getTaskByID(task.getId());
        assertEquals(updatedTask, result);
    }

    @Test
    public void testConflictDetection() {
        Task task1 = new Task(1, "Задача 1", "Описание", Status.NEW, Duration.ofHours(2), LocalDateTime.now());
        Task task2 = new Task(2, "Задача 2", "Описание", Status.NEW, Duration.ofHours(2), LocalDateTime.now().plusHours(1));
        taskManager.addTask(task1);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> taskManager.addTask(task2));
        assertTrue(ex.getMessage().contains("пересекается по времени"));
    }

    @Test
    public void testAddNonOverlappingTask_NoException() {
        assertDoesNotThrow(() -> {
            LocalDateTime now = LocalDateTime.of(2025, 10, 29, 12, 0);
            Task t1 = new Task(1, "Задача 1", "Описание", Status.NEW, Duration.ofHours(1), now);
            Task t2 = new Task(2, "Задача 1", "Описание", Status.NEW, Duration.ofHours(1), now.plusHours(2));

            taskManager.addTask(t1);
            taskManager.addTask(t2);
        }, "Добавление непересекающихся задач не должно вызывать исключений");
    }

    @Test
    public void testUpdateTask_NoOverlapException() {
        LocalDateTime now = LocalDateTime.of(2025, 10, 29, 12, 0);
        Task original = new Task(1, "Исходная", "Описание", Status.NEW, Duration.ofHours(1), now);
        taskManager.addTask(original);

        Task updated = new Task(1, "Обновлённая", "Описание", Status.IN_PROGRESS, Duration.ofHours(1), now.plusHours(3));

        assertDoesNotThrow(() -> taskManager.updateTask(updated),
                "Обновление задачи без пересечения не должно бросать исключение");
    }
}
