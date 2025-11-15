package manager.task;

import exceptions.NotFoundException;
import exceptions.TaskOverlapException;
import manager.Managers;
import manager.history.HistoryManager;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected final HistoryManager historyManager = Managers.getDefaultHistory();
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(Task.START_TIME_COMPARATOR);
    private int nextId = 1;

    @Override
    public int getNextId() {
        return nextId++;
    }

    @Override
    public Task addTask(Task task) {
        if (task.getDuration() != null && task.getDuration().isNegative()) {
            throw new IllegalArgumentException("Длительность не может быть отрицательной");
        }

        if (task.getStartTime() != null) {
            if (!tasks.isEmpty() && hasOverlappingTasks(task, tasks.values())) {
                throw new TaskOverlapException("Задача пересекается по времени с другой задачей");
            }
        }

        task.setId(getNextId());
        tasks.put(task.getId(), task);
        prioritizedTasks.add(task);
        return task;
    }


    @Override
    public Epic addEpic(Epic epic) {
        if (epic.getId() <= 0) {
            epic.setId(getNextId());
        }

        if (epic.getStatus() == null) {
            epic.setStatus(Status.NEW);
        }
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Subtask addSubTask(Subtask subtask) {
        if (!epics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Указанный эпик не найден.");
        }
        subtask.setId(getNextId());
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubTask(subtask);
        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(epic);
        prioritizedTasks.add(subtask);
        return subtask;
    }

    @Override
    public Task updateTask(Task task) {
        Integer taskID = task.getId();
        if (taskID == null || !tasks.containsKey(taskID)) {
            return null;
        }
        prioritizedTasks.remove(tasks.get(taskID));
        tasks.replace(taskID, task);
        prioritizedTasks.add(task);
        return task;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Integer epicID = epic.getId();
        if (epicID == null || !epics.containsKey(epicID)) {
            throw new NotFoundException("Эпик с ID " + epicID + " не найден");
        }

        Epic existingEpic = epics.get(epicID);

        if (epic.getStatus() != null) {
            existingEpic.setStatus(epic.getStatus());
        } else {
            updateEpicStatus(existingEpic);
        }

        existingEpic.setName(epic.getName());
        existingEpic.setDescription(epic.getDescription());

        epics.replace(epicID, existingEpic);
        return existingEpic;
    }


    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Integer subtaskID = subtask.getId();
        if (subtaskID == null || !subtasks.containsKey(subtaskID)) {
            return null;
        }
        prioritizedTasks.remove(subtasks.get(subtaskID));
        int epicID = subtask.getEpicId();
        Subtask oldSubtask = subtasks.get(subtaskID);
        subtasks.replace(subtaskID, subtask);
        Epic epic = epics.get(epicID);

        if (epic.getSubTaskList() == null) {
            epic.setSubtaskList(new ArrayList<>());
        }

        ArrayList<Subtask> subtaskList = epic.getSubTaskList();
        subtaskList.remove(oldSubtask);
        subtaskList.add(subtask);
        epic.setSubtaskList(subtaskList);
        updateEpicStatus(epic);
        prioritizedTasks.add(subtask);
        return subtask;
    }

    @Override
    public void deleteTasks() {
        tasks.clear();
        prioritizedTasks.removeAll(tasks.values());
        historyManager.removeAll();
    }

    @Override
    public void deleteEpics() {
        epics.clear();
        subtasks.clear();
        prioritizedTasks.removeAll(subtasks.values());
        historyManager.removeAll();
    }

    @Override
    public void deleteSubtasks() {
        subtasks.clear();
        epics.values().forEach(Epic::clearSubtasks);
        prioritizedTasks.removeAll(subtasks.values());
        historyManager.removeAll();
    }

    @Override
    public Task deleteTaskByID(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            historyManager.remove(id);
            prioritizedTasks.remove(task);
        }
        return task;
    }

    @Override
    public Epic deleteEpicByID(int id) {
        if (!epics.containsKey(id)) {
            return null;
        }
        Epic epic = epics.remove(id);
        List<Subtask> epicSubtasks = epic.getSubTaskList();

        epicSubtasks.forEach(subtask -> {
            subtasks.remove(subtask.getId());
            prioritizedTasks.remove(subtask);
            historyManager.remove(subtask.getId());
        });
        historyManager.remove(id);
        return epic;
    }

    @Override
    public Subtask deleteSubtaskByID(int id) {
        if (!subtasks.containsKey(id)) {
            return null;
        }
        Subtask subtask = subtasks.remove(id);
        int epicID = subtask.getEpicId();
        Epic epic = epics.get(epicID);
        ArrayList<Subtask> subtaskList = epic.getSubTaskList();
        subtaskList.remove(subtask);
        epic.setSubtaskList(subtaskList);
        updateEpicStatus(epic);
        prioritizedTasks.remove(subtask);
        historyManager.remove(id);
        return subtask;
    }

    public void updateEpicStatus(Epic epic) {
        List<Subtask> subtasks = epic.getSubTaskList();

        if (subtasks.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allNew = subtasks.stream().allMatch(t -> t.getStatus() == Status.NEW);
        boolean allDone = subtasks.stream().allMatch(t -> t.getStatus() == Status.DONE);

        if (allNew) {
            epic.setStatus(Status.NEW);
        } else if (allDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public boolean isOverlapping(Task task1, Task task2) {
        if (task1.getStartTime() == null || task2.getStartTime() == null) {
            return false;
        }

        LocalDateTime endTime1 = task1.getEndTime();
        LocalDateTime endTime2 = task2.getEndTime();

        if (endTime1 == null) endTime1 = task1.getStartTime();
        if (endTime2 == null) endTime2 = task2.getStartTime();

        return task1.getStartTime().isBefore(endTime2) &&
                task2.getStartTime().isBefore(endTime1);
    }

    private boolean hasOverlappingTasks(Task task, Collection<Task> otherTasks) {
        return otherTasks.stream()
                .anyMatch(other -> isOverlapping(task, other));
    }

    @Override
    public Task getTaskByID(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            throw new NotFoundException("Задача с ID " + id + " не найдена");
        }
        historyManager.add(task);
        return task;
    }


    @Override
    public Epic getEpicByID(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Subtask getSubtaskByID(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Subtask> getEpicSubtasks(Epic epic) {
        return epic.getSubTaskList();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    protected Map<Integer, Epic> getEpicsMap() {
        return epics;
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }
}