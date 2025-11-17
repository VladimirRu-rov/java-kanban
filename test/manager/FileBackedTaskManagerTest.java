package manager;

import exceptions.ManagerSaveException;
import exceptions.NotFoundException;
import exceptions.TaskOverlapException;
import manager.task.FileBackedTaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {

    private static final String TEST_FILE_PATH = "./test_tasks.csv";
    ManagerSaveException ex = assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(TEST_FILE_PATH),
            "Ожидалось исключение ManagerSaveException при некорректных данных"
    );
    private FileBackedTaskManager taskManager;

    private Path getTestFilePath() {
        return Paths.get(TEST_FILE_PATH);
    }

    @BeforeEach
    public void setUp() throws IOException {
        Path dir = getTestFilePath().getParent();
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        Files.writeString(getTestFilePath(), "");
        taskManager = new FileBackedTaskManager(getTestFilePath().toFile());
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(getTestFilePath());
    }

    @Test
    public void testCsvFormat_Exactly8Fields() throws IOException {
        Task task = new Task(1, "Задача", "Описание", Status.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        taskManager.addTask(task);
        taskManager.save();

        List<String> lines = Files.readAllLines(Paths.get(TEST_FILE_PATH));
        assertEquals(2, lines.size()); // Заголовок + 1 задача

        String taskLine = lines.get(1);
        String[] fields = taskLine.split(",", -1); // -1 → все поля
        assertEquals(8, fields.length, "CSV должен содержать ровно 8 полей");
    }

    @Test
    public void testDataPersistence() throws IOException {
        Duration duration = Duration.ofMinutes(30);
        LocalDateTime startTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        Task task = new Task(1, "Задача 1", "Описание", Status.NEW, duration, startTime);

        taskManager.addTask(task);
        taskManager.save();

        String content = Files.readString(Paths.get(TEST_FILE_PATH));
        assertTrue(content.contains("Задача 1"));
    }

    @Test
    public void testLoadingFromCorruptedFile() {
        File testFile = new File(TEST_FILE_PATH);
        try {
            Files.writeString(Paths.get(testFile.getAbsolutePath()),
                    "id,type,name,status,desc,duration,start" + "Некорректные данные\n");

            assertTrue(testFile.exists(), "Файл не создан");
            assertTrue(testFile.length() > 0, "Файл пуст");

        } catch (IOException e) {
            fail("Ошибка при создании тестового файла: " + e.getMessage());
        }
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

    @Test
    public void testAddingAndGettingTask() {
        Duration duration = Duration.ofMinutes(30);
        LocalDateTime startTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        Task task = new Task(1, "Задача 1", "Описание", Status.NEW, duration, startTime);

        taskManager.addTask(task);
        Task retrievedTask = taskManager.getTaskByID(task.getId());
        assertEquals(task, retrievedTask);
    }

    @Test
    public void testUpdatingTask() {
        LocalDateTime startTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        Task task = new Task(1, "Задача 1", "Описание", Status.NEW, Duration.ZERO, startTime);
        taskManager.addTask(task);

        LocalDateTime updatedStartTime = startTime.plusHours(1);

        Task updatedTask = new Task(task.getId(), "Изменённая задача", "Изменённое описание",
                Status.IN_PROGRESS, Duration.ofHours(2), updatedStartTime);

        taskManager.updateTask(updatedTask);
        Task result = taskManager.getTaskByID(task.getId());

        assertEquals(updatedTask, result);
    }

    @Test
    public void testConflictDetection() {
        Task task1 = new Task(1, "Задача 1", "Описание", Status.NEW, Duration.ofHours(2), LocalDateTime.now());
        Task task2 = new Task(2, "Задача 2", "Описание", Status.NEW, Duration.ofHours(2), LocalDateTime.now().plusHours(1));
        taskManager.addTask(task1);

        Exception ex = assertThrows(TaskOverlapException.class, () -> taskManager.addTask(task2));
        assertTrue(ex.getMessage().contains("пересекается по времени"));
    }

    @Test
    public void testAddNonOverlappingTask_NoException() {
        assertDoesNotThrow(() -> {
            LocalDateTime now = LocalDateTime.of(2025, 10, 29, 12, 0);
            Task t1 = new Task(1, "Задача 1", "Описание", Status.NEW, Duration.ofHours(1), now);
            Task t2 = new Task(2, "Задача 1", "Описание", Status.NEW, Duration.ofHours(1), now.plusHours(2));

            taskManager.addTask(t1);
            taskManager.addTask(t2);
        }, "Добавление непересекающихся задач не должно вызывать исключений");
    }

    @Test
    public void testUpdateTask_NoOverlapException() {
        LocalDateTime now = LocalDateTime.of(2025, 10, 29, 12, 0);
        Task original = new Task(1, "Исходная", "Описание", Status.NEW, Duration.ofHours(1), now);
        taskManager.addTask(original);

        Task updated = new Task(1, "Обновлённая", "Описание", Status.IN_PROGRESS, Duration.ofHours(1), now.plusHours(3));
        assertDoesNotThrow(() -> taskManager.updateTask(updated),
                "Обновление задачи без пересечения не должно бросать исключение");
    }

    @Test
    public void testCsv_NullFields() throws IOException {
        LocalDateTime startTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        Task task = new Task(1, "Задача", null, Status.NEW, null, startTime);

        taskManager.addTask(task);
        taskManager.save();

        String content = Files.readString(Paths.get(TEST_FILE_PATH));

        assertTrue(content.contains("1,TASK,Задача,NEW,,0,"),
                "CSV должен содержать '0' для null-значения duration. " +
                        "Фактическое содержимое: " + content);
    }

    @Test
    public void testSaveEmptyTasks() throws IOException {
        taskManager.save();
        Path path = Paths.get(TEST_FILE_PATH);
        assertTrue(Files.exists(path), "Файл должен быть создан после save()");
        List<String> lines = Files.readAllLines(path);

        assertEquals(1, lines.size(), "Файл должен содержать только заголовок");
        String header = lines.get(0);
        assertTrue(
                header.startsWith("id,type,name,status,description,duration,startTime,epicId"),
                "Заголовок файла должен быть корректным"
        );
    }

    @Test
    public void testSaveLoad_EpicWithoutSubtasks() {
        Epic epic = new Epic(1, "Эпик без подзадач", "Описание", Status.NEW, Duration.ZERO, null);
        taskManager.addEpic(epic);
        taskManager.save();

        FileBackedTaskManager reloaded = FileBackedTaskManager.loadFromFile(TEST_FILE_PATH);
        Epic loadedEpic = reloaded.getEpicByID(epic.getId());

        assertNotNull(loadedEpic);
        assertEquals(0, loadedEpic.getSubTaskList().size(), "Подзадачи должны отсутствовать");
        assertEquals(Status.NEW, loadedEpic.getStatus(), "Статус эпика должен сохраниться");
    }

    @Test
    public void testSaveLoad_EmptyHistory() {
        Task dummyTask = new Task(1, "Временная задача", "Описание", Status.NEW, Duration.ofHours(1),
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        taskManager.addTask(dummyTask);
        taskManager.save();

        FileBackedTaskManager reloaded = FileBackedTaskManager.loadFromFile(TEST_FILE_PATH);
        assertTrue(reloaded.getHistory().isEmpty(), "История должна быть пустой после загрузки");
    }

    @Test
    public void testGetTaskByID_AfterReload_Nonexistent() {

        Task dummyTask = new Task(1, "Временная задача", "Описание", Status.NEW, Duration.ofHours(1),
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );

        taskManager.addTask(dummyTask);
        taskManager.save();

        FileBackedTaskManager reloaded = FileBackedTaskManager.loadFromFile(TEST_FILE_PATH);
        assertNotNull(reloaded, "Менеджер должен успешно загрузиться из файла");
        assertThrows(
                NotFoundException.class,
                () -> reloaded.getTaskByID(999),
                "Задача с несуществующим ID должна вызывать NotFoundException"
        );
    }

    @Test
    public void testAddTask_AfterReload() {
        Task task = new Task(1, "Задача", "Описание", Status.NEW, Duration.ofHours(1),
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        taskManager.addTask(task);
        taskManager.save();

        FileBackedTaskManager reloaded = FileBackedTaskManager.loadFromFile(TEST_FILE_PATH);

        assertEquals(1, reloaded.getTasks().size(), "Должна быть 1 задача после загрузки");
        Task loadedTask = reloaded.getTaskByID(task.getId());
        assertNotNull(loadedTask, "Задача должна быть найдена по ID");

        assertEquals(task.getId(), loadedTask.getId(), "ID должен совпадать");
        assertEquals(task.getName(), loadedTask.getName(), "Название должно совпадать");
        assertEquals(task.getDescription(), loadedTask.getDescription(), "Описание должно совпадать");
        assertEquals(task.getStatus(), loadedTask.getStatus(), "Статус должен совпадать");
        assertEquals(task.getDuration(), loadedTask.getDuration(), "Длительность должна совпадать");
        assertEquals(task.getStartTime(), loadedTask.getStartTime(), "Время начала должно совпадать");
    }

    @Test
    public void testDeleteTask_AfterReload() throws IOException {

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Task task = new Task(1, "Задача", "Описание", Status.NEW, Duration.ofHours(1), now);

        taskManager.addTask(task);
        taskManager.save();

        FileBackedTaskManager reloaded = FileBackedTaskManager.loadFromFile(TEST_FILE_PATH);
        assertNotNull(reloaded, "Менеджер должен успешно загрузиться из файла");

        Task deleted = reloaded.deleteTaskByID(task.getId());
        assertNotNull(deleted, "Удаленная задача не должна быть null");

        assertEquals(task.getId(), deleted.getId(), "ID задачи должен совпадать");
        assertEquals(task.getName(), deleted.getName(), "Название задачи должно совпадать");
        assertEquals(task.getDescription(), deleted.getDescription(), "Описание должно совпадать");
        assertEquals(task.getStatus(), deleted.getStatus(), "Статус должен совпадать");
        assertEquals(task.getDuration(), deleted.getDuration(), "Длительность должна совпадать");
        assertEquals(task.getStartTime(), deleted.getStartTime(), "Время начала должно совпадать");
        assertThrows(NotFoundException.class, () -> reloaded.getTaskByID(task.getId()),
                "getTaskByID() должен бросать NotFoundException для удалённой задачи");
        assertTrue(reloaded.getTasks().isEmpty(), "Список задач должен быть пуст после удаления единственной задачи");
    }

    @Test
    public void testUpdateEpic_StatusAfterSubtaskChange() {
        Epic epic = new Epic(1, "Эпик", "Описание", Status.NEW, Duration.ZERO, null);
        Subtask subtask = new Subtask(10, "Подзадача", "Описание", Status.NEW, Duration.ZERO, null, epic.getId());
        taskManager.addEpic(epic);
        taskManager.addSubTask(subtask);

        subtask.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask);
        taskManager.updateEpic(epic);

        assertEquals(Status.DONE, epic.getStatus(), "Если все подзадачи DONE, статус эпика должен быть DONE");
        taskManager.save();
        FileBackedTaskManager reloaded = FileBackedTaskManager.loadFromFile(TEST_FILE_PATH);
        Epic reloadedEpic = reloaded.getEpicByID(epic.getId());

        assertEquals(Status.DONE, reloadedEpic.getStatus(), "Статус эпика должен сохраниться после перезагрузки");
    }

    @Test
    public void testSaveLoad_WithEmptyFieldsInEpic() throws IOException {
        Epic epic = new Epic(1, "Эпик", null, Status.NEW, null, null);
        taskManager.addEpic(epic);
        taskManager.save();

        String content = Files.readString(Paths.get(TEST_FILE_PATH));
        assertTrue(content.contains(",NEW,,0,,") || content.contains(",NEW,,0,,\n"),
                "В CSV не найдена ожидаемая последовательность ',NEW,,0,,' или ',NEW,,0,,\n'. " +
                        "Фактическое содержимое:\n" + content
        );
    }

    @Test
    public void testLoadFromCorruptedFile() {
        String corruptedContent = "id,type,name,status,duration,start_time,epic_id\n" +
                "1,TASK,Задача,NEW";

        try {
            Files.writeString(Paths.get(TEST_FILE_PATH), corruptedContent);
        } catch (IOException e) {
            fail("Не удалось создать тестовый файл: " + e.getMessage());
        }

        ManagerSaveException ex = assertThrows(ManagerSaveException.class, () -> {
            FileBackedTaskManager.loadFromFile(TEST_FILE_PATH);
        });

        assertTrue(ex.getMessage().contains("Неверный формат CSV") && ex.getMessage().contains("ожидается 8 полей") &&
                        ex.getMessage().contains("найдено 4"),
                "Сообщение об ошибке должно указывать на нехватку полей. Фактическое сообщение: " + ex.getMessage()
        );
    }
}