package manager;

import exceptions.NotFoundException;
import manager.task.InMemoryTaskManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createTaskManagerInstance() {
        return new InMemoryTaskManager();
    }

    @Test
    public void testAddTask_Standard() {
        Task task = new Task(1, "Задача", "Описание", Status.NEW, Duration.ofHours(1), LocalDateTime.now());
        Task added = taskManager.addTask(task);
        Assertions.assertEquals(task, taskManager.getTaskByID(task.getId()));
    }

    @Test
    public void testGetEpicByID_Standard() {
        Epic epic = new Epic(1, "Эпик", "Описание", Status.NEW, Duration.ZERO, null);
        taskManager.addEpic(epic);
        Assertions.assertEquals(epic, taskManager.getEpicByID(epic.getId()));
    }

    @Test
    public void testGetTasks_Empty() {
        Assertions.assertTrue(taskManager.getTasks().isEmpty(), "Список задач должен быть пуст");
    }

    @Test
    public void testGetEpics_Empty() {
        Assertions.assertTrue(taskManager.getEpics().isEmpty(), "Список эпиков должен быть пуст");
    }

    @Test
    public void testGetTaskByID_Nonexistent() {
        assertThrows(
                NotFoundException.class,
                () -> taskManager.getTaskByID(999),
                "Ожидалось исключение NotFoundException для несуществующего ID"
        );
    }

    @Test
    public void testDeleteTaskByID_Nonexistent() {
        Assertions.assertNull(taskManager.deleteTaskByID(999), "Удаление несуществующей задачи должно возвращать null");
    }

    @Test
    public void testUpdateTask_NonexistentID() {
        Task task = new Task(999, "Не существует", "", Status.NEW, null, null);
        Assertions.assertNull(taskManager.updateTask(task), "Обновление несуществующей задачи должно возвращать null");
    }

    @Test
    public void testAddSubtask_Standard() {
        Epic epic = new Epic(1, "Эпик", "Описание", Status.NEW, Duration.ZERO, null);
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask(10, "Подзадача", "Описание", Status.NEW, Duration.ZERO, null, epic.getId());
        taskManager.addSubTask(subtask);

        assertEquals(1, epic.getSubTaskList().size());
        assertEquals(subtask, epic.getSubTaskList().get(0));
    }

    @Test
    public void testGetSubtaskByID_Nonexistent() {
        Assertions.assertNull(taskManager.getSubtaskByID(999), "Несуществующая подзадача должна возвращать null");
    }

    @Test
    public void testDeleteSubtaskByID_Nonexistent() {
        Assertions.assertNull(taskManager.deleteSubtaskByID(999), "Удаление несуществующей подзадачи должно возвращать null");
    }

    @Test
    public void testGetTasks_AfterAddition() {
        Task task = new Task(1, "Задача", "Описание", Status.NEW, Duration.ofHours(1), LocalDateTime.now());
        taskManager.addTask(task);
        Assertions.assertEquals(1, taskManager.getTasks().size());
    }

    @Test
    public void testGetEpics_AfterAddition() {
        Epic epic = new Epic(1, "Эпик", "Описание", Status.NEW, Duration.ZERO, null);
        taskManager.addEpic(epic);
        Assertions.assertEquals(1, taskManager.getEpics().size());
    }

    @Test
    public void testGetSubtasks_Empty() {
        Assertions.assertTrue(taskManager.getSubtasks().isEmpty(), "Список подзадач должен быть пуст");
    }

    @Test
    public void testUpdateTask_Standard() {
        Task task = new Task(1, "Задача", "Описание", Status.NEW, Duration.ofHours(1), LocalDateTime.now());
        taskManager.addTask(task);

        Task updated = new Task(1, "Обновлённая", "Новое описание", Status.IN_PROGRESS, Duration.ofHours(2), task.getStartTime());
        Task result = taskManager.updateTask(updated);

        assertEquals(updated, result);
        Assertions.assertEquals(Status.IN_PROGRESS, taskManager.getTaskByID(1).getStatus());
    }

    @Test
    public void testDeleteEpicByID_Standard() {
        Epic epic = new Epic(1, "Эпик", "Описание", Status.NEW, Duration.ZERO, null);
        taskManager.addEpic(epic);

        Epic deleted = taskManager.deleteEpicByID(epic.getId());
        assertSame(epic, deleted);
        assertNull(taskManager.getEpicByID(epic.getId()));
    }

    @Test
    public void testDeleteSubtaskByID_Standard() {
        Epic epic = new Epic(1, "Эпик", "Описание", Status.NEW, Duration.ZERO, null);
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask(10, "Подзадача", "Описание", Status.NEW, Duration.ZERO, null, epic.getId());
        taskManager.addSubTask(subtask);

        Subtask deleted = taskManager.deleteSubtaskByID(subtask.getId());
        assertSame(subtask, deleted);
        assertTrue(epic.getSubTaskList().isEmpty());
    }
}