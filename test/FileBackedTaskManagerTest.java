import exceptions.ManagerSaveException;
import manage.FileBackedTaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Status;
import task.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;


import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private static final String TEST_FILE_PATH = "test_manager.csv";

    @Override
    protected FileBackedTaskManager createTaskManagerInstance() {
        return new FileBackedTaskManager(new File(TEST_FILE_PATH));
    }

    @BeforeEach
    public void setUpFile() {
        new File(TEST_FILE_PATH).delete();
    }

    @AfterEach
    public void tearDownFile() {
        new File(TEST_FILE_PATH).delete();
    }

    @Test
    public void testDataPersistence() throws IOException {
        Task task = new Task(1, "Задача 1", "Описание", Status.NEW, null, null);
        taskManager.addTask(task);
        taskManager.save();

        String content = Files.readString(Paths.get(TEST_FILE_PATH));
        assertTrue(content.contains("Задача 1"));
    }

    @Test
    public void testLoadingFromCorruptedFile() {
        File testFile = new File(TEST_FILE_PATH);
        try {
            Files.writeString(
                    Paths.get(testFile.getAbsolutePath()),
                    "id,type,name,status,desc,duration,start\n" +
                            "Некорректные данные\n"
            );

            assertTrue(testFile.exists(), "Файл не создан");
            assertTrue(testFile.length() > 0, "Файл пуст");

        } catch (IOException e) {
            fail("Ошибка при создании тестового файла: " + e.getMessage());
        }

        ManagerSaveException ex = assertThrows(
                ManagerSaveException.class,
                () -> FileBackedTaskManager.loadFromFile(TEST_FILE_PATH),
                "Ожидалось исключение ManagerSaveException при некорректных данных"
        );

        assertTrue(
                ex.getMessage().contains("Ошибка при загрузке из файла") ||
                        ex.getMessage().contains("недостаточно полей"),
                "Сообщение об ошибке должно указывать на проблему (регистр важен!)"
        );
    }

    @Test
    public void testLoadFromEmptyFile() {
        File file = new File(TEST_FILE_PATH);
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ManagerSaveException ex = assertThrows(ManagerSaveException.class, () -> {
            FileBackedTaskManager.loadFromFile(TEST_FILE_PATH);
        });

        assertTrue(ex.getMessage().contains("Файл пуст") || ex.getMessage().contains("не содержит данных"));
    }

    @Test
    public void testReloadingSavedTasks() {
        FileBackedTaskManager manager = new FileBackedTaskManager(new File(TEST_FILE_PATH));

        Task task1 = new Task(1, "Задача 1", "Описание", Status.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        Task task2 = new Task(2, "Задача 2", "Описание", Status.IN_PROGRESS, Duration.ofHours(1), LocalDateTime.now().plusDays(1));
        manager.addTask(task1);
        manager.addTask(task2);
        manager.save();

        FileBackedTaskManager reloadedManager = FileBackedTaskManager.loadFromFile(TEST_FILE_PATH);
        assertEquals(2, reloadedManager.getTasks().size());

        Collection<Task> allTasks = reloadedManager.getTasks();
        boolean foundTask1 = false;
        boolean foundTask2 = false;
        for (Task task : allTasks) {
            if (task.getId() == task1.getId() &&
                    task.getName().equals(task1.getName()) &&
                    task.getStatus() == task1.getStatus()) {
                foundTask1 = true;
            }
            if (task.getId() == task2.getId() &&
                    task.getName().equals(task2.getName()) &&
                    task.getStatus() == task2.getStatus()) {
                foundTask2 = true;
            }
        }
        assertTrue(foundTask1 && foundTask2, "Одна или обе задачи не найдены после перезагрузки.");
    }
}