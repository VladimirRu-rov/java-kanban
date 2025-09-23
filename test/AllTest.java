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

        // Проверяем, что в истории остаётся старая версия
        List<Task> history = taskManager.getHistory();
        assertTrue(history.stream().anyMatch(t -> t.getName().equals("Первоначальная задача")));
    }

    // Тестируем удаление задачи и её исчезновение из истории
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
        assertFalse(history.contains(task)); // Задача должна отсутствовать в истории
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

        // Проверяем, что подзадача была удалена
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

        // Проверяем, что подзадача тоже удалена
        assertNull(taskManager.getSubtaskByID(subtask.getId()));
    }

    // Проверяем невозможность задания некорректного идентификатора эпика у подзадачи
    @Test
    void invalidEpicIdShouldThrowException() {
        // Подготовка
        try {
            Subtask invalidSubtask = new Subtask("Некорректная", "Ошибка", -1); // создаём подзадачу с неверным эпиком
            taskManager.addSubTask(invalidSubtask); // пробуем добавить подзадачу
            fail("Исключение должно было произойти");
        } catch (IllegalArgumentException e) {
            assertEquals("Неправильный идентификатор эпика", e.getMessage());
        }
    }

    // Проверяем, что изменения полей через сеттеры не влияют на данные в менеджере
    @Test
    void changedFieldsThroughSettersDoNotAffectManagerData() {
        // Создаем задачу
        Task task = new Task("Покупка хлеба", "Нужно купить хлеб");
        taskManager.addTask(task);

        // Изменяем название через сеттер
        task.setName("Новый хлеб");

        // Проверяем, что менеджер возвращает старое название
        Task retrievedTask = taskManager.getTaskByID(task.getId());
        assertEquals("Покупка хлеба", retrievedTask.getName());
    }

    // Проверяем корректность работы метода getDefault()
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
