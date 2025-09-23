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
        taskManager = Managers.getDefault(); // получаем экземпляр менеджера задач
    }

    // Тестируем сохранение старой версии задачи после обновления
    @Test
    void shouldStoreOldVersionAfterUpdate() {
        Task initialTask = new Task("Первоначальная задача", "Описание");
        taskManager.addTask(initialTask);
        taskManager.getTaskByID(initialTask.getId()); // видим задачу

        Task updatedTask = new Task(initialTask.getId(), "Новое название", "Новое описание", Status.IN_PROGRESS);
        taskManager.updateTask(updatedTask);

        List<Task> history = taskManager.getHistory();
        assertTrue(history.stream().anyMatch(t -> t.getName().equals("Первоначальная задача"))); // проверяем старую версию
    }

    // Тестируем удаление задачи и её исчезновение из истории
    @Test
    void removedTaskShouldDisappearFromHistory() {
        Task task = new Task("Моя задача", "Пример");
        taskManager.addTask(task);
        taskManager.getTaskByID(task.getId()); // просматриваем задачу

        taskManager.deleteTaskByID(task.getId()); // удаляем задачу

        List<Task> history = taskManager.getHistory();
        assertFalse(history.contains(task)); // проверяем отсутствие задачи в истории
    }

    // Проверяем очистку связей подзадач после удаления
    @Test
    void deletedSubtaskShouldNotHaveOldIds() {
        Epic epic = new Epic("Отдых", "Планирую отдых");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Купание", "Идем купаться", epic.getId());
        taskManager.addSubTask(subtask);

        taskManager.deleteSubtaskByID(subtask.getId()); // удаляем подзадачу

        assertNull(taskManager.getSubtaskByID(subtask.getId())); // проверяем, что подзадачи нет
    }

    // Проверяем очистку ссылок на подзадачи после удаления эпика
    @Test
    void deletedEpicShouldClearSubtasks() {
        Epic epic = new Epic("Отдых", "Планирую отдых");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Купание", "Идем купаться", epic.getId());
        taskManager.addSubTask(subtask);

        taskManager.deleteEpicByID(epic.getId()); // удаляем эпик

        assertNull(taskManager.getSubtaskByID(subtask.getId())); // проверяем, что подзадача тоже удалена
    }

    // Проверяем невозможность задания некорректного идентификатора эпика у подзадачи
    @Test
    void invalidEpicIdShouldThrowException() {
        try {
            Subtask invalidSubtask = new Subtask("Некорректная", "Ошибка", -1); // неправильно задан эпик
            taskManager.addSubTask(invalidSubtask); // добавляем подзадачу
            fail("Должно было выбрасывать исключение");
        } catch (IllegalArgumentException e) {
            assertEquals("Неправильный идентификатор эпика", e.getMessage());
        }
    }

    // Проверяем сохранение целевых данных после изменения через сеттеры
    @Test
    void changedFieldsThroughSettersDoNotAffectManagerData() {
        Task task = new Task("Покупка хлеба", "Нужно купить хлеб");
        taskManager.addTask(task);

        task.setName("Новый хлеб"); // меняем имя через setter

        Task retrievedTask = taskManager.getTaskByID(task.getId());
        assertEquals("Покупка хлеба", retrievedTask.getName()); // ожидаем прежнее имя
    }

    // Проверяем работу метода getDefault()
    @Test
    void getDefaultReturnsCorrectManagerType() {
        TaskManager defaultManager = Managers.getDefault();
        assertTrue(defaultManager instanceof InMemoryTaskManager);
    }

    // Проверяем работу метода getDefaultHistory()
    @Test
    void getDefaultHistoryReturnsCorrectHistoryType() {
        HistoryManager defaultHistory = Managers.getDefaultHistory();
        assertTrue(defaultHistory instanceof InMemoryHistoryManager);
    }
}
