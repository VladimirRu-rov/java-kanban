package manager.task;

import exceptions.ManagerSaveException;
import task.Epic;
import task.Subtask;
import task.Task;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private static final String DEFAULT_FILE_PATH = "./data/manager.csv";

    public FileBackedTaskManager() {
        this.file = new File(DEFAULT_FILE_PATH);
        initFile();
    }

    public FileBackedTaskManager(File file) {
        this.file = file;
        initFile();
    }

    private void initFile() {
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new ManagerSaveException("Ошибка при создании родительской директории: " + parentDir.getAbsolutePath());
            }
        }

        try {
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
        Integer epicID = epic.getId();
        if (epicID == null || !getEpicsMap().containsKey(epicID)) {
            return null;
        }
        updateEpicStatus(epic);
        getEpicsMap().replace(epicID, epic);
        save();
        return epic;
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

    public void save() {
        try {
            List<String> lines = new ArrayList<>();
            lines.add("id,type,name,status,description,duration,startTime,epicId");

            Stream.of(super.getTasks(), super.getEpics(), super.getSubtasks())
                    .flatMap(List::stream)
                    .map(CsvTaskUtils::taskToCsvString)
                    .forEach(lines::add);

            Files.writeString(
                    Paths.get(file.getAbsolutePath()),
                    String.join("\n", lines),
                    StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл: " + e.getMessage(), e);
        }
    }

    public static FileBackedTaskManager loadFromFile(String filePath) throws ManagerSaveException {
        File file = new File(filePath);
        try {
            String content = Files.readString(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);

            if (content.trim().isEmpty()) {
                throw new ManagerSaveException("Файл пуст");
            }

            String[] lines = content.split("\n");
            if (lines.length <= 1) {
                throw new ManagerSaveException("Файл не содержит данных (только заголовок)");
            }

            FileBackedTaskManager manager = new FileBackedTaskManager(file);

            List<Epic> epicsToAdd = new ArrayList<>();
            List<Subtask> subtasksToAdd = new ArrayList<>();
            List<Task> tasksToAdd = new ArrayList<>();

            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;

                try {
                    Task task = CsvTaskUtils.taskFromString(line);
                    if (task instanceof Subtask) {
                        subtasksToAdd.add((Subtask) task);
                    } else if (task instanceof Epic) {
                        epicsToAdd.add((Epic) task);
                    } else {
                        tasksToAdd.add(task);
                    }
                } catch (IllegalArgumentException e) {
                    throw new ManagerSaveException(
                            "Ошибка парсинга строки '" + line + "': " + e.getMessage(),
                            e
                    );
                }
            }

            epicsToAdd.forEach(manager::addEpic);
            subtasksToAdd.forEach(manager::addSubTask);
            tasksToAdd.forEach(manager::addTask);

            return manager;

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке из файла: " + e.getMessage(), e);
        }
    }
}



