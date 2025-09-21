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
        taskManager = Managers.getDefault(); // Получаем экземпляр менеджера задач
    }

    // Тестируем сохранение старых версий задач после обновления
    @Test
    void shouldStoreOldVersionAfterUpdate() {
        // Создаем задачу
        Task initialTask = new Task("Первоначальная задача", "Описание");
        taskManager.addTask(initialTask);
        taskManager.getTaskByID(initialTask.getId()); // Посмотреть задачу

        // Обновляем задачу
        Task updatedTask = new Task(initialTask.getId(), "Новое название", "Новое описание", Status.IN_PROGRESS);
        taskManager.updateTask(updatedTask);

        // Проверяем, что в истории остается старая версия
        List<Task> history = taskManager.getHistory();
        assertTrue(history.stream().anyMatch(t -> t.getName().equals("Первоначальная задача")));
    }

    // Тестируем удаление задачи и ее исчезновение из истории
    @Test
    void removedTaskShouldDisappearFromHistory() {
        // Создаем задачу
        Task task = new Task("Моя задача", "Пример");
        taskManager.addTask(task);
        taskManager.getTaskByID(task.getId()); // Посмотреть задачу

        // Удаляем задачу
        taskManager.deleteTaskByID(task.getId());

        // Проверяем, что задача исчезла из истории
        List<Task> history = taskManager.getHistory();
        assertFalse(history.contains(task));
    }

    // Проверяем очистку связей подзадач после удаления
    @Test
    void deletedSubtaskShouldNotHaveOldIds() {
        // Создаем эпик
        Epic epic = new Epic("Отдых", "Планирую отдых");
        taskManager.addEpic(epic);

        // Создаем подзадачу
        Subtask subtask = new Subtask("Купание", "Идем купаться", epic.getId());
        taskManager.addSubTask(subtask);

        // Удаляем подзадачу
        taskManager.deleteSubtaskByID(subtask.getId());

        // Проверяем, что подзадача не сохранилась ни в каком виде
        assertNull(taskManager.getSubtaskByID(subtask.getId()));
    }

    // Проверяем очистку ссылок на подзадачи после удаления эпика
    @Test
    void deletedEpicShouldClearSubtasks() {
        // Создаем эпик
        Epic epic = new Epic("Отдых", "Планирую отдых");
        taskManager.addEpic(epic);

        // Создаем подзадачу
        Subtask subtask = new Subtask("Купание", "Идем купаться", epic.getId());
        taskManager.addSubTask(subtask);

        // Удаляем эпик
        taskManager.deleteEpicByID(epic.getId());

        // Проверяем, что подзадача удалена
        assertNull(taskManager.getSubtaskByID(subtask.getId()));
    }

    // Проверяем невозможность задать некорректный идентификатор эпика у подзадачи
    @Test
    void invalidEpicIdShouldThrowException() {
        // Создаем подзадачу с неправильным идентификатором эпика
        try {
            Subtask invalidSubtask = new Subtask("Некорректная", "Ошибка", -1);
            taskManager.addSubTask(invalidSubtask);
            fail("Исключительная ситуация не произошла");
        } catch (IllegalArgumentException e) {
            assertEquals("Неправильный идентификатор эпика", e.getMessage());
        }
    }

    // Проверяем сохранение целевых данных после изменения через сеттеры
    @Test
    void changedFieldsThroughSettersDoNotAffectManagerData() {
        // Создаем задачу
        Task task = new Task("Покупка хлеба", "Нужно купить хлеб");
        taskManager.addTask(task);

        // Меняем название через сеттер
        task.setName("Новый хлеб");

        // Проверяем, что менеджер продолжает показывать оригинальное название
        Task retrievedTask = taskManager.getTaskByID(task.getId());
        assertEquals("Покупка хлеба", retrievedTask.getName());
    }

    // Проверяем коректность работы метода getDefault()
    @Test
    void getDefaultReturnsCorrectManagerType() {
        TaskManager defaultManager = Managers.getDefault();
        assertTrue(defaultManager instanceof InMemoryTaskManager);
    }

    // Проверяем корректность работы метода getDefaultHistory()
    @Test
    void getDefaultHistoryReturnsCorrectHistoryType() {
        HistoryManager defaultHistory = Managers.getDefaultHistory();
        assertTrue(defaultHistory instanceof InMemoryHistoryManager);
    }
}

