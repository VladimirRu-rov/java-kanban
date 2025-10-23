package manage;

import exceptions.ManagerSaveException;
import task.Epic;
import task.Subtask;
import task.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private static final String DEFAULT_FILE_PATH = "./data/ха-ха-ха.csv";

    // Конструктор с путем по умолчанию
    public FileBackedTaskManager() {
        this.file = new File(DEFAULT_FILE_PATH);
        initFile();
    }

    // Конструктор с возможностью указать свой путь
    public FileBackedTaskManager(File file) {
        this.file = file;
        initFile();
    }

    // Инициализация файла и директории
    private void initFile() {
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new ManagerSaveException("Ошибка при создании родительской директории: " + parentDir.getAbsolutePath());
            }
        }

        try {
            // Создаем сам файл, если его нет
            if (!file.exists()) {
                Files.createFile(Paths.get(file.getAbsolutePath()));
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при создании файла: " + e.getMessage());
        }
    }

    @Override
    public Task addTask(Task task) {
        Task addedTask = super.addTask(task);
        save();
        return addedTask;
    }

    @Override
    public Epic addEpic(Epic epic) {
        Epic addedEpic = super.addEpic(epic);
        save();
        return addedEpic;
    }

    @Override
    public Subtask addSubTask(Subtask subtask) {
        Subtask addedSubtask = super.addSubTask(subtask);
        save();
        return addedSubtask;
    }

    @Override
    public Task updateTask(Task task) {
        Task updatedTask = super.updateTask(task);
        save();
        return updatedTask;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic updatedEpic = super.updateEpic(epic);
        save();
        return updatedEpic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Subtask updatedSubtask = super.updateSubtask(subtask);
        save();
        return updatedSubtask;
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    @Override
    public Task deleteTaskByID(int id) {
        Task deletedTask = super.deleteTaskByID(id);
        save();
        return deletedTask;
    }

    @Override
    public Epic deleteEpicByID(int id) {
        Epic deletedEpic = super.deleteEpicByID(id);
        save();
        return deletedEpic;
    }

    @Override
    public Subtask deleteSubtaskByID(int id) {
        Subtask deletedSubtask = super.deleteSubtaskByID(id);
        save();
        return deletedSubtask;
    }

    // Метод сохранения (продолжение)
    public void save() {
        try {
            List<String> lines = new ArrayList<>();
            lines.add("id,type,name,status,description,epic");

            // Собираем все задачи
            lines.addAll(super.getTasks().stream()
                    .map(Task::toCsvString).toList());

            // Преобразуем эпики в строки CSV
            lines.addAll(super.getEpics().stream()
                    .map(Task::toCsvString).toList());

            // Преобразуем подзадачи в строки CSV
            lines.addAll(super.getSubtasks().stream()
                    .map(Task::toCsvString).toList());

            Files.writeString(Paths.get(file.getAbsolutePath()), String.join("\n", lines));
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл: " + e.getMessage());
        }
    }

    // Метод загрузки из файла
    public static FileBackedTaskManager loadFromFile(String filePath) {
        File file = new File(filePath);
        try {
            String content = Files.readString(Paths.get(file.getAbsolutePath()));
            String[] lines = content.split("\n");

            FileBackedTaskManager manager = new FileBackedTaskManager(file);

            // Пропускаем первую строку с заголовками
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                if (!line.isEmpty()) {
                    Task task = Task.fromString(line);

                    // Распределение задач по правильным коллекциям
                    if (task.getClass() == Subtask.class) {
                        manager.addSubTask((Subtask) task);
                    } else if (task.getClass() == Epic.class) {
                        manager.addEpic((Epic) task);
                    } else if (task.getClass() == Task.class) {
                        manager.addTask((Task) task);
                    }
                }
            }
            return manager;
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке из файла: " + e.getMessage());
        }
    }
}
