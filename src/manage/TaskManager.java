package manage;

import task.Epic;
import task.Subtask;
import task.Task;
import task.Status;
import java.util.*;

public class TaskManager {

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    private int nextId = 1;

    // Метод для получения следующего уникального идентификатора
    private int getNextId() {
        return nextId++;
    }

    // Метод для добавления новой задачи (task.Task)
    public Task addTask(Task task) {
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        return task;
    }

    // Метод для добавления нового эпика (task.Epic)
    public Epic addEpic(Epic epic) {
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    // Метод для добавления новой субзадачи (task.Subtask)
    public Subtask addSubTask(Subtask subtask) {
        subtask.setId(getNextId());
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubTask(subtask);
        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(epic);
        return subtask;
    }

    // Метод для обновления существующей задачи (task.Task)
    public Task updateTask(Task task) {
        Integer taskID = task.getId();
        if (taskID == null || !tasks.containsKey(taskID)) {
            return null;
        }
        tasks.replace(taskID, task);
        return task;
    }

    // Метод для обновления существующего эпика (task.Epic)
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

    // Метод для обновления существующей субзадачи (task.Subtask)
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
    public Task getTaskByID(int id) {
        return tasks.get(id);
    }

    // Метод для получения эпика по его идентификатору
    public Epic getEpicByID(int id) {
        return epics.get(id);
    }

    // Метод для получения субзадачи по её идентификатору
    public Subtask getSubtaskByID(int id) {
        return subtasks.get(id);
    }

    // Метод для получения списка всех задач
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    // Метод для получения списка всех эпиков
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    // Метод для получения списка всех субзадач
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    // Метод для получения списка субзадач конкретного эпика
    public ArrayList<Subtask> getEpicSubtasks(Epic epic) {
        return epic.getSubTaskList();
    }

    // Метод для удаления всех задач
    public void deleteTasks() {
        tasks.clear();
    }

    // Метод для удаления всех эпиков и связанных с ними субзадач
    public void deleteEpics() {
        epics.clear();
        subtasks.clear();
    }

    // Метод для удаления всех субзадач
    public void deleteSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            epic.setStatus(Status.NEW);
        }
    }

    // Метод для удаления задачи по её идентификатору
    public void deleteTaskByID(int id) {
        tasks.remove(id);
    }


    public void deleteEpicByID(int id) {
        ArrayList<Subtask> epicSubtasks = epics.get(id).getSubTaskList();
        epics.remove(id);
        for (Subtask subtask : epicSubtasks) {
            subtasks.remove(subtask.getId());
        }
    }

    // Метод для удаления субзадачи по её идентификатору
    public void deleteSubtaskByID(int id) {
        Subtask subtask = subtasks.get(id);
        int epicID = subtask.getEpicId();
        subtasks.remove(id);
        Epic epic = epics.get(epicID);
        ArrayList<Subtask> subtaskList = epic.getSubTaskList();
        subtaskList.remove(subtask);
        epic.setSubtaskList(subtaskList);
        updateEpicStatus(epic);
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
        //
        if (doneCount == subtaskList.size()) {
            epic.setStatus(Status.DONE);
        } else if (newCount == subtaskList.size()) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
}