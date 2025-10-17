package manage;

import task.Epic;
import task.Subtask;
import task.Task;
import task.Status;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    public final Map<Integer, Task> tasks = new HashMap<>();
    public final Map<Integer, Epic> epics = new HashMap<>();
    public final Map<Integer, Subtask> subtasks = new HashMap<>();
    public final HistoryManager historyManager = Managers.getDefaultHistory();


    public int nextId = 1;

    // Метод для получения следующего уникального идентификатора
    public int getNextId() {
        return nextId++;
    }

    // Метод для добавления новой задачи
    @Override
    public Task addTask(Task task) {
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        return task;
    }

    // Метод для добавления нового эпика
    @Override
    public Epic addEpic(Epic epic) {
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    // Метод для добавления новой субзадачи
    @Override
    public Subtask addSubTask(Subtask subtask) {
        if (!epics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Указанный эпик не найден.");
        }
        subtask.setId(getNextId()); // Генерация идентификатора сразу при добавлении подзадачи
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubTask(subtask);
        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(epic);
        return subtask;
    }

    // Метод для обновления существующей задачи (task.Task)
    @Override
    public Task updateTask(Task task) {
        Integer taskID = task.getId();
        if (taskID == null || !tasks.containsKey(taskID)) {
            return null;
        }
        tasks.replace(taskID, task);
        return task;
    }

    // Метод для обновления существующего эпика
    @Override
    public Epic updateEpic(Epic epic) {
        Integer epicID = epic.getId();
        if (epicID == null || !epics.containsKey(epicID)) {
            return null;
        }
        Epic oldEpic = epics.get(epicID);
        ArrayList<Subtask> oldEpicSubTaskList = oldEpic.getSubTaskList();
        if (!oldEpicSubTaskList.isEmpty()) {
            for (Subtask subtask : oldEpicSubTaskList) {
                subtasks.remove(subtask.getId());
            }
        }
        epics.replace(epicID, epic);
        ArrayList<Subtask> newEpicSubtaskList = epic.getSubTaskList();
        for (Subtask subtask : newEpicSubtaskList) {
            subtasks.put(subtask.getId(), subtask);
        }
        updateEpicStatus(epic);
        return epic;
    }

    // Метод для обновления существующей субзадачи
    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Integer subtaskID = subtask.getId();
        if (subtaskID == null || !subtasks.containsKey(subtaskID)) {
            return null;
        }
        int epicID = subtask.getEpicId();
        Subtask oldSubtask = subtasks.get(subtaskID);
        subtasks.replace(subtaskID, subtask);
        Epic epic = epics.get(epicID);
        ArrayList<Subtask> subtaskList = epic.getSubTaskList();
        subtaskList.remove(oldSubtask);
        subtaskList.add(subtask);
        epic.setSubtaskList(subtaskList);
        updateEpicStatus(epic);
        return subtask;
    }

    // Метод для получения задачи по её идентификатору
    @Override
    public Task getTaskByID(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    // Метод для получения эпика по его идентификатору
    @Override
    public Epic getEpicByID(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    // Метод для получения субзадачи по её идентификатору
    @Override
    public Subtask getSubtaskByID(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    // Метод для получения списка всех задач
    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    // Метод для получения списка всех эпиков
    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    // Метод для получения списка всех субзадач
    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    // Метод для получения списка субзадач конкретного эпика
    @Override
    public List<Subtask> getEpicSubtasks(Epic epic) {
        return epic.getSubTaskList();
    }

    // Метод для удаления всех задач
    @Override
    public void deleteTasks() {
        tasks.clear();
    }

    // Метод для удаления всех эпиков и связанных с ними субзадач
    @Override
    public void deleteEpics() {
        epics.clear();
        subtasks.clear();
    }

    // Метод для удаления всех субзадач
    @Override
    public void deleteSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            epic.setStatus(Status.NEW);
        }
    }

    // Метод для удаления задачи по её идентификатору
    @Override
    public Task deleteTaskByID(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            // Здесь уведомляем менеджера истории о необходимости удаления задачи
            historyManager.remove(id);
        }
        return task;
    }

    @Override
    public Epic deleteEpicByID(int id) {
        ArrayList<Subtask> epicSubtasks = epics.get(id).getSubTaskList();
        for (Subtask subtask : epicSubtasks) {
            subtasks.remove(subtask.getId());
        }
        return epics.remove(id);
    }

    // Метод для удаления субзадачи по её идентификатору
    @Override
    public Subtask deleteSubtaskByID(int id) {
        if (!subtasks.containsKey(id)) {
            return null;
        }
        Subtask subtask = subtasks.get(id);
        int epicID = subtask.getEpicId();
        Subtask detetedSubtask = subtasks.remove(id);
        // обновляем список подзадач и статус эпика
        Epic epic = epics.get(epicID);
        ArrayList<Subtask> subtaskList = epic.getSubTaskList();
        subtaskList.remove(subtask);
        epic.setSubtaskList(subtaskList);
        updateEpicStatus(epic);
        return detetedSubtask;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // Метод для обновления статуса эпика в зависимости от статуса его субзадач
    private void updateEpicStatus(Epic epic) {
        int doneCount = 0;
        int newCount = 0;
        ArrayList<Subtask> subtaskList = epic.getSubTaskList();
        for (Subtask subtask : subtaskList) {
            if (subtask.getStatus() == Status.DONE) {
                doneCount++;
            }
            if (subtask.getStatus() == Status.NEW) {
                newCount++;
            }
        }

        if (doneCount == subtaskList.size()) {
            epic.setStatus(Status.DONE);
        } else if (newCount == subtaskList.size()) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
}


