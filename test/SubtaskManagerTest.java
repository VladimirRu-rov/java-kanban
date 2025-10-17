import manage.Managers;
import manage.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Subtask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class SubtaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void setup() {
        taskManager = Managers.getDefault();
    }

    @Test
    void invalidEpicIdShouldThrowException() {
        try {
            Subtask invalidSubtask = new Subtask("Некорректная", "Ошибка", -1);
            taskManager.addSubTask(invalidSubtask);
            fail("Должно было выбросить исключение");
        } catch (IllegalArgumentException e) {
            assertEquals("Указанный эпик не найден.", e.getMessage());
        }
    }
}
