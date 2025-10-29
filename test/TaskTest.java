import manage.FileBackedTaskManager;
import manage.InMemoryTaskManager;
import manage.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {

    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new FileBackedTaskManager();
    }

    @Test
    public void testTaskIntersection() {
        Task task1 = new Task(1, "Задача №1", "Описание", Status.NEW, Duration.ofHours(2), LocalDateTime.now());
        Task task2 = new Task(2, "Задача №2", "Описание", Status.NEW, Duration.ofHours(2), LocalDateTime.now().plusHours(1));

        assertTrue(task1.isOverlapping(task2));
    }

    @Test
    public void testOverlappingTasks() {
        LocalDateTime now = LocalDateTime.of(2025, 10, 29, 12, 0);
        Task task1 = new Task(1, "Задача 1", "Описание", Status.NEW, Duration.ofHours(2), now);
        Task task2 = new Task(2, "Задача 2", "Описание", Status.NEW, Duration.ofHours(2), now.plusHours(1));

        assertTrue(task1.isOverlapping(task2));
        assertTrue(task2.isOverlapping(task1));  // Симметрия
    }

    @Test
    public void testNoOverlap_TouchingBoundaries() {
        LocalDateTime t1Start = LocalDateTime.of(2025, 10, 29, 10, 0);
        LocalDateTime t2Start = t1Start.plusHours(2); // 12:00

        Task task1 = new Task(1, "Задача 1", "Описание", Status.NEW, Duration.ofHours(2), t1Start);
        Task task2 = new Task(2, "Задача 2", "Описание", Status.NEW, Duration.ofHours(2), t2Start);

        assertFalse(task1.isOverlapping(task2), "task1 не должен пересекаться с task2");
        assertFalse(task2.isOverlapping(task1), "task2 не должен пересекаться с task1");
    }

    @Test
    public void testZeroDurationTask() {
        LocalDateTime now = LocalDateTime.of(2025, 10, 29, 12, 0);
        Task task1 = new Task(1, "Задача 1", "Описание", Status.NEW, Duration.ZERO, now);
        Task task2 = new Task(2, "Задача 2", "Описание", Status.NEW, Duration.ofHours(1), now);

        assertFalse(task1.isOverlapping(task2));
        assertFalse(task2.isOverlapping(task1));
    }

    @Test
    public void testFullContainment() {
        LocalDateTime now = LocalDateTime.of(2025, 10, 29, 12, 0);
        Task outer = new Task(1, "Задача 1", "Описание", Status.NEW, Duration.ofHours(4), now);
        Task inner = new Task(2, "Задача 2", "Описание", Status.NEW, Duration.ofHours(2), now.plusHours(1));


        assertTrue(outer.isOverlapping(inner));
        assertTrue(inner.isOverlapping(outer));
    }

    @Test
    public void testEpicStatus_AllSubtasksAreNew() {

        Epic epic = new Epic(1, "Эпик1", "Разработать новую версию продукта", Status.NEW, Duration.ZERO, null);

        Subtask subtask1 = new Subtask(10, "Проектирование", "", Status.NEW, Duration.ofHours(10), null, epic.getId());
        Subtask subtask2 = new Subtask(11, "Написание", "", Status.NEW, Duration.ofHours(10), null, epic.getId());
        epic.addSubTask(subtask1);
        epic.addSubTask(subtask2);

        taskManager.updateEpic(epic);

        assertEquals(Status.NEW, epic.getStatus(), "Статус эпика должен быть NEW");
    }

    @Test
    public void testEpicStatus_MixedStatuses() {
        Epic epic = new Epic(1, "Эпик1", "Разработать новую версию продукта", Status.NEW, Duration.ZERO, null);
        Subtask subtask1 = new Subtask(10, "Проектирование", "", Status.NEW, Duration.ofHours(10), null, epic.getId());
        Subtask subtask2 = new Subtask(11, "Написание", "", Status.DONE, Duration.ofHours(10), null, epic.getId());
        epic.addSubTask(subtask1);
        epic.addSubTask(subtask2);
        taskManager.addEpic(epic);  // Сохраняет в хранилище
        taskManager.updateEpic(epic);
        Epic updatedEpic = taskManager.getEpicByID(epic.getId());
        assertEquals(Status.IN_PROGRESS, updatedEpic.getStatus());
    }
}
