import manage.TaskManager;
import manage.Managers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Subtask;
import task.Task;
import task.Status;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void setup() {
        taskManager = Managers.getDefault();
    }

    @Test
    void shouldStoreOldVersionAfterUpdate() {
        Task initialTask = new Task("Первоначальная задача", "Описание");
        taskManager.addTask(initialTask);
        taskManager.getTaskByID(initialTask.getId());

        Task updatedTask = new Task(initialTask.getId(), "Новое название", "Новое описание", Status.IN_PROGRESS);
        taskManager.updateTask(updatedTask);

        List<Task> history = taskManager.getHistory();
        assertTrue(history.stream().anyMatch(t -> t.getName().equals("Первоначальная задача")));
    }

    @Test
    void removedTaskShouldDisappearFromHistory() {
        Task task = new Task("Моя задача", "Пример");
        taskManager.addTask(task);
        taskManager.getTaskByID(task.getId());

        taskManager.deleteTaskByID(task.getId());
        assertFalse(taskManager.getHistory().contains(task));
    }

    @Test
    void deletedSubtaskShouldNotHaveOldIds() {
        Epic epic = new Epic("Отдых", "Планирую отдых");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Купание", "Идем купаться", epic.getId());
        taskManager.addSubTask(subtask);

        taskManager.deleteSubtaskByID(subtask.getId());
        assertNull(taskManager.getSubtaskByID(subtask.getId()));
    }

    @Test
    void deletedEpicShouldClearSubtasks() {
        Epic epic = new Epic("Отдых", "Планирую отдых");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Купание", "Идем купаться", epic.getId());
        taskManager.addSubTask(subtask);

        taskManager.deleteEpicByID(epic.getId());
        assertNull(taskManager.getSubtaskByID(subtask.getId()));
    }
}
