package test;

import org.junit.jupiter.api.Test;
import manage.InMemoryTaskManager;
import manage.InMemoryHistoryManager;
import manage.TaskManager;
import manage.Managers;
import task.Epic;
import task.Subtask;
import task.Task;
import task.Status;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;


public class AllTest {

    private final TaskManager taskManager = Managers.getDefault(); // Получаем готовый менеджер задач

    // Проверяет ограничение на длину истории просмотра
    @Test
    public void shouldLimitHistoryToLast10Items() {
        // Добавляем больше задач, чем вместимость истории
        for (int i = 0; i < 20; i++) {
            taskManager.addTask(new Task("Новая задача №" + i, "Просто описание"));
        }

        // Обращаемся ко всем задачам
        List<Task> allTasks = taskManager.getTasks();
        for (Task task : allTasks) {
            taskManager.getTaskByID(task.getId()); // Добавляем каждую задачу в историю
        }

        // История должна содержать максимум 10 последних задач
        List<Task> history = taskManager.getHistory();
        assertEquals(10, history.size(), "История должна содержать ровно 10 записей");
    }

    // Проверяет сохранение старой версии задачи после её обновления
    @Test
    public void shouldKeepOldTaskVersionsOnUpdate() {
        // Создаем простую задачу
        Task digestPorogy = new Task("Пороги", "Переварить оба порога у хорошего мастера");
        taskManager.addTask(digestPorogy);
        taskManager.getTaskByID(digestPorogy.getId()); // Посмотрим на неё один раз

        // Обновляем задачу
        Task updatedWashFloors = new Task(digestPorogy.getId(), // Сохраняем тот же самый ID
                "Пороги и крыло", // Меняем название
                "Качественно", // Меняем описание
                Status.IN_PROGRESS // Меняем статус
        );
        taskManager.updateTask(updatedWashFloors);

        // Проверяем, что история хранит старую версию
        List<Task> history = taskManager.getHistory();
        Task firstItemInHistory = history.get(0); // Берём самую последнюю запись из истории

        // Сравниваем старые значения
        assertEquals(firstItemInHistory.getName(), "Пороги", "Старая версия не сохраняется");
        assertEquals(firstItemInHistory.getDescription(), "Переварить оба порога у хорошего мастера", "Старое описание не сохраняется");
    }

    // Проверяет сохранение старых версий эпиков после их обновления
    @Test
    public void shouldKeepOldEpicVersionsOnUpdate() {
        // Создаем эпик
        Epic renovation = new Epic("Ремонт машины", "Сделать ремонт к лету");
        taskManager.addEpic(renovation);
        taskManager.getEpicByID(renovation.getId()); // Посмотрим на него один раз

        // Обновляем эпик
        Epic updatedRenovation = new Epic(renovation.getId(), // Оставляем тот же ID
                "Перекраска", // Новое название
                "В серый цвет", // Новое описание
                Status.IN_PROGRESS // Новый статус
        );
        taskManager.updateEpic(updatedRenovation);

        // Проверяем, что история хранит старую версию
        List<Task> history = taskManager.getHistory();
        Epic firstItemInHistory = (Epic) history.get(0); // Самая последняя запись из истории

        // Сравниваем старые значения
        assertEquals(firstItemInHistory.getName(), "Ремонт машины", "Старая версия не сохраняется");
        assertEquals(firstItemInHistory.getDescription(), "Сделать ремонт к лету", "Старое описание не сохраняется");
    }

    // Проверяет сохранение старых версий подзадач после их обновления
    @Test
    public void shouldKeepOldSubtaskVersionsOnUpdate() {
        // Создаем эпик
        Epic renovation = new Epic("Найти жинку", "Красивую");
        taskManager.addEpic(renovation);

        // Создаем подзадачу
        Subtask buyChairs = new Subtask("Жениться", "Поехать в тур", renovation.getId());
        taskManager.addSubTask(buyChairs);
        taskManager.getSubtaskByID(buyChairs.getId()); // Посмотрим на неё один раз

        // Обновляем подзадачу
        Subtask updatedBuyChairs = new Subtask(buyChairs.getId(), // Оставляем тот же ID
                "Гулять дальше", // Новое название
                "Пушкинская", // Новое описание
                Status.IN_PROGRESS, // Новый статус
                renovation.getId());
        taskManager.updateSubtask(updatedBuyChairs);

        // Проверяем, что история хранит старую версию
        List<Task> history = taskManager.getHistory();
        Subtask firstItemInHistory = (Subtask) history.get(0); // Самая последняя запись из истории

        // Сравниваем старые значения
        assertEquals(firstItemInHistory.getName(), "Жениться", "Старая версия не сохраняется");
        assertEquals(firstItemInHistory.getDescription(), "Поехать в тур", "Старое описание не сохраняется");
    }

    // Тест проверяет, что метод getDefault создает и возвращает экземпляр класса InMemoryTaskManager
    @Test
    void getDefaultShouldInitializeInMemoryTaskManager() {
        assertInstanceOf(InMemoryTaskManager.class, Managers.getDefault());
    }

    // Тест проверяет, что метод getDefaultHistory создает и возвращает экземпляр класса InMemoryHistoryManager
    @Test
    void getDefaultHistoryShouldInitializeInMemoryHistoryManager() {
        assertInstanceOf(InMemoryHistoryManager.class, Managers.getDefaultHistory());
    }


}

