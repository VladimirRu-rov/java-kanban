import org.junit.jupiter.api.Test;
import manage.FileBackedTaskManager;
import task.Epic;
import task.Subtask;
import task.Task;
import task.Status;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileBackedTaskManagerTest {

    private static final String TEST_FILE_PATH = "test_file.csv";

    private void cleanupTestFile() {
        File testFile = new File(TEST_FILE_PATH);
        if (testFile.exists()) {
            testFile.delete();
        }
    }

    @Test
    void testSaveAndLoadEmptyFile() {
        // Создаем новый менеджер задач с тестовым файлом
        File testFile = new File(TEST_FILE_PATH);
        FileBackedTaskManager manager = new FileBackedTaskManager(testFile);

        // Сохраняем пустой файл
        manager.save();

        // Проверяем существование файла
        assertTrue(Files.exists(Paths.get(TEST_FILE_PATH)));

        // Загружаем данные обратно
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(TEST_FILE_PATH);

        // Проверяем отсутствие задач, эпиков и подзадач
        assertEquals(0, loadedManager.getTasks().size());
        assertEquals(0, loadedManager.getEpics().size());
        assertEquals(0, loadedManager.getSubtasks().size());
    }

    @Test
    void testSaveMultipleTasks() throws Exception {
        File testFile = new File(TEST_FILE_PATH);
        FileBackedTaskManager manager = new FileBackedTaskManager(testFile);

        Task task1 = new Task(1, "Сделать отчет", "Написать ежемесячный отчет", Status.NEW);
        Epic epic1 = new Epic(2, "Ремонт дома", "Отремонтировать кухню и ванную комнату", Status.IN_PROGRESS);
        Subtask subtask1 = new Subtask(3, "Покупка материалов", "Закупить стройматериалы", Status.NEW, epic1.getId());

        manager.addTask(task1);
        manager.addEpic(epic1);
        manager.addSubTask(subtask1);

        manager.save();

        // Проверяем наличие записей в файле
        assertTrue(Files.lines(Paths.get(TEST_FILE_PATH)).count() > 1);
    }

    @Test
    void testLoadMultipleTasks() {
        File testFile = new File(TEST_FILE_PATH);
        FileBackedTaskManager manager = new FileBackedTaskManager(testFile);

        Task task1 = new Task(1, "Сделать отчет", "Написать ежемесячный отчет", Status.NEW);
        Epic epic1 = new Epic(2, "Ремонт дома", "Отремонтировать кухню и ванную комнату", Status.IN_PROGRESS);
        Subtask subtask1 = new Subtask(3, "Покупка материалов", "Закупить стройматериалы", Status.NEW, epic1.getId());

        manager.addTask(task1);
        manager.addEpic(epic1);
        manager.addSubTask(subtask1);

        manager.save();

        System.out.println("Saved data successfully!");

        // Загружаем данные заново
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(TEST_FILE_PATH);

        // Проверяем количество загруженных задач
        assertEquals(1, loadedManager.getTasks().size());   // Одна базовая задача
        assertEquals(1, loadedManager.getEpics().size());   // Один эпик
        assertEquals(1, loadedManager.getSubtasks().size()); // Одна подзадача

        // Убедимся, что типы задач совпадают
        assertEquals(task1.getClass(), loadedManager.getTasks().get(0).getClass());
        assertEquals(epic1.getClass(), loadedManager.getEpics().get(0).getClass());
        assertEquals(subtask1.getClass(), loadedManager.getSubtasks().get(0).getClass());
    }
}
