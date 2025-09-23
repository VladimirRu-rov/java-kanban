package test;

import manage.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Subtask;
import task.Task;
import task.Status;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AllTest {

    private TaskManager taskManager;

    @BeforeEach
    void setup() {
        taskManager = Managers.getDefault(); // инициализация менеджера задач
    }

    // Проверка сохранения старой версии задачи после обновления
    @Test
    void shouldStoreOldVersionAfterUpdate() {
        Task initialTask = new Task("Первоначальная задача", "Описание");
        taskManager.addTask(initialTask);
        taskManager.getTaskByID(initialTask.getId()); // смотреть задачу

        Task updatedTask = new Task(initialTask.getId(), "Новое название", "Новое описание", Status.IN_PROGRESS);
        taskManager.updateTask(updatedTask);

        List<Task> history = taskManager.getHistory();
        assertTrue(history.stream().anyMatch(t -> t.getName().equals("Первоначальная задача"))); // проверяем старую версию
    }

    // Проверка удаления задачи и её исчезновения из истории
    @Test
    void removedTaskShouldDisappearFromHistory() {
        Task task = new Task("Моя задача", "Пример");
        taskManager.addTask(task);
        taskManager.getTaskByID(task.getId()); // просмотр задачи

        taskManager.deleteTaskByID(task.getId()); // удаляем задачу

        List<Task> history = taskManager.getHistory();
        assertFalse(history.contains(task)); // проверяем, что задача исчезла из истории
    }

    // Проверка очистки связей подзадач после удаления
    @Test
    void deletedSubtaskShouldNotHaveOldIds() {
        Epic epic = new Epic("Отдых", "Планирую отдых");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Купание", "Идем купаться", epic.getId());
        taskManager.addSubTask(subtask);

        taskManager.deleteSubtaskByID(subtask.getId()); // удаляем подзадачу

        assertNull(taskManager.getSubtaskByID(subtask.getId())); // проверяем, что подзадачи нет
    }

    // Проверка очистки ссылок на подзадачи после удаления эпика
    @Test
    void deletedEpicShouldClearSubtasks() {
        Epic epic = new Epic("Отдых", "Планирую отдых");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Купание", "Идем купаться", epic.getId());
        taskManager.addSubTask(subtask);

        taskManager.deleteEpicByID(epic.getId()); // удаляем эпик

        assertNull(taskManager.getSubtaskByID(subtask.getId())); // проверяем, что подзадача удалена
    }

    // Проверка невозможности задания некорректного идентификатора эпика
    @Test
    void invalidEpicIdShouldThrowException() {
        try {
            Subtask invalidSubtask = new Subtask("Некорректная", "Ошибка", -1); // неправильный ID эпика
            taskManager.addSubTask(invalidSubtask); // добавляем подзадачу
            fail("Должно было выбросить исключение");
        } catch (IllegalArgumentException e) {
            assertEquals("Неправильный идентификатор эпика", e.getMessage());
        }
    }

    // Проверка неизменности данных при изменениях через сеттеры
    @Test
    void changedFieldsThroughSettersDoNotAffectManagerData() {
        Task task = new Task("Покупка хлеба", "Нужно купить хлеб");
        taskManager.addTask(task);

        task.setName("Новый хлеб"); // изменяем имя через сеттер

        Task retrievedTask = taskManager.getTaskByID(task.getId());
        assertEquals("Покупка хлеба", retrievedTask.getName()); // проверяем, что изменения не затронули задачу в менеджере
    }

    // Проверка правильности возврата типа менеджера по умолчанию
    @Test
    void getDefaultReturnsCorrectManagerType() {
        TaskManager defaultManager = Managers.getDefault();
        assertTrue(defaultManager instanceof InMemoryTaskManager);
    }

    // Проверка правильного типа менеджера истории
    @Test
    void getDefaultHistoryReturnsCorrectHistoryType() {
        HistoryManager defaultHistory = Managers.getDefaultHistory();
        assertTrue(defaultHistory instanceof InMemoryHistoryManager);
    }
}
