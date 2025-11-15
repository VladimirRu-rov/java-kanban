package task;

import manager.task.FileBackedTaskManager;
import manager.task.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {

    @TempDir
    private Path tempDir;

    private TaskManager taskManager;
    private Path testFilePath;

    @BeforeEach
    protected void setUp() throws Exception {
        testFilePath = tempDir.resolve("test_tasks.csv");
        taskManager = new FileBackedTaskManager(testFilePath.toFile());
    }

    @Test
    public void testTaskIntersection() {
        Task task1 = new Task(1, "Задача №1", "Описание", Status.NEW, Duration.ofHours(2), LocalDateTime.now());
        Task task2 = new Task(2, "Задача №2", "Описание", Status.NEW, Duration.ofHours(2), LocalDateTime.now().plusHours(1));

        assertTrue(taskManager.isOverlapping(task1, task2));
    }

    @Test
    public void testOverlappingTasks() {
        LocalDateTime now = LocalDateTime.of(2025, 10, 29, 12, 0);
        Task task1 = new Task(1, "Задача 1", "Описание", Status.NEW, Duration.ofHours(2), now);
        Task task2 = new Task(2, "Задача 2", "Описание", Status.NEW, Duration.ofHours(2), now.plusHours(1));

        assertTrue(taskManager.isOverlapping(task1, task2));
        assertTrue(taskManager.isOverlapping(task2, task1));  // Симметрия
    }

    @Test
    public void testNoOverlap_TouchingBoundaries() {
        LocalDateTime t1Start = LocalDateTime.of(2025, 10, 29, 10, 0);
        LocalDateTime t2Start = t1Start.plusHours(2); // 12:00

        Task task1 = new Task(1, "Задача 1", "Описание", Status.NEW, Duration.ofHours(2), t1Start);
        Task task2 = new Task(2, "Задача 2", "Описание", Status.NEW, Duration.ofHours(2), t2Start);

        assertFalse(taskManager.isOverlapping(task1, task2));
        assertFalse(taskManager.isOverlapping(task2, task1));
    }

    @Test
    public void testZeroDurationTask() {
        LocalDateTime now = LocalDateTime.of(2025, 10, 29, 12, 0);
        Task task1 = new Task(1, "Задача 1", "Описание", Status.NEW, Duration.ZERO, now);
        Task task2 = new Task(2, "Задача 2", "Описание", Status.NEW, Duration.ofHours(1), now);

        assertFalse(taskManager.isOverlapping(task1, task2));
        assertFalse(taskManager.isOverlapping(task2, task1));
    }

    @Test
    public void testFullContainment() {
        LocalDateTime now = LocalDateTime.of(2025, 10, 29, 12, 0);
        Task outer = new Task(1, "Задача 1", "Описание", Status.NEW, Duration.ofHours(4), now);
        Task inner = new Task(2, "Задача 2", "Описание", Status.NEW, Duration.ofHours(2), now.plusHours(1));


        assertTrue(taskManager.isOverlapping(outer, inner));
        assertTrue(taskManager.isOverlapping(inner, outer));
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
        taskManager.addEpic(epic);
        taskManager.updateEpic(epic);
        Epic updatedEpic = taskManager.getEpicByID(epic.getId());
        assertEquals(Status.IN_PROGRESS, updatedEpic.getStatus());
    }

    @Test
    public void testEpicStatus_EmptySubtasks() {
        Epic epic = new Epic(1, "Эпик без подзадач", "Описание", Status.NEW, Duration.ZERO, null);
        taskManager.addEpic(epic);
        taskManager.updateEpic(epic);

        assertEquals(Status.NEW, epic.getStatus(),
                "Эпик без подзадач должен иметь статус NEW");
    }

    @Test
    public void testEpicStatus_AllNew() {
        Epic epic = new Epic(1, "Эпик", "Описание", Status.NEW, Duration.ZERO, null);
        epic.addSubTask(new Subtask(10, "Подзадача 1", "", Status.NEW, Duration.ZERO, null, epic.getId()));
        epic.addSubTask(new Subtask(11, "Подзадача 2", "", Status.NEW, Duration.ZERO, null, epic.getId()));

        taskManager.addEpic(epic);
        taskManager.updateEpic(epic);

        assertEquals(Status.NEW, epic.getStatus(), "Если все подзадачи NEW — статус эпика NEW");
    }

    @Test
    public void testEpicStatus_AllDone() {
        Epic epic = new Epic(1, "Эпик", "Описание", Status.NEW, Duration.ZERO, null);
        epic.addSubTask(new Subtask(10, "Подзадача 1", "", Status.DONE, Duration.ZERO, null, epic.getId()));
        epic.addSubTask(new Subtask(11, "Подзадача 2", "", Status.DONE, Duration.ZERO, null, epic.getId()));

        taskManager.addEpic(epic);
        taskManager.updateEpic(epic);

        assertEquals(Status.DONE, epic.getStatus(),
                "Если все подзадачи DONE — статус эпика DONE");
    }

    @Test
    public void testEpicStatus_MixedNewAndDone() {
        Epic epic = new Epic(1, "Эпик", "Описание", Status.NEW, Duration.ZERO, null);
        epic.addSubTask(new Subtask(10, "Подзадача 1", "", Status.NEW, Duration.ZERO, null, epic.getId()));
        epic.addSubTask(new Subtask(11, "Подзадача 2", "", Status.DONE, Duration.ZERO, null, epic.getId()));

        taskManager.addEpic(epic);
        taskManager.updateEpic(epic);

        assertEquals(Status.IN_PROGRESS, epic.getStatus(),
                "Если есть NEW и DONE — статус эпика IN_PROGRESS");
    }

    @Test
    public void testEpicStatus_WithInProgress() {
        Epic epic = new Epic(1, "Эпик", "Описание", Status.NEW, Duration.ZERO, null);
        epic.addSubTask(new Subtask(10, "Подзадача 1", "", Status.IN_PROGRESS, Duration.ZERO, null, epic.getId()));
        epic.addSubTask(new Subtask(11, "Подзадача 2", "", Status.NEW, Duration.ZERO, null, epic.getId()));

        taskManager.addEpic(epic);
        taskManager.updateEpic(epic);

        assertEquals(Status.IN_PROGRESS, epic.getStatus(),
                "Если есть IN_PROGRESS — статус эпика IN_PROGRESS");
    }
}