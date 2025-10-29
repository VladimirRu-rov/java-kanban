package manage;

import task.*;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private int nextId = 1;

    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(Task.START_TIME_COMPARATOR);

    @Override
    public int getNextId() {
        return nextId++;
    }

    @Override
    public Task addTask(Task task) {
        if (!tasks.isEmpty() && task.hasOverlappingTasks(tasks.values())) {
            throw new IllegalArgumentException("Задача пересекается по времени с другой задачей");
        }
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        prioritizedTasks.add(task);  // Добавляем в отсортированный набор
        return task;
    }

    @Override
    public Epic addEpic(Epic epic) {
        epic.setId(getNextId());
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
        prioritizedTasks.add(subtask);  // Добавляем в отсортированный набор
        return subtask;
    }

    @Override
    public Task updateTask(Task task) {
        Integer taskID = task.getId();
        if (taskID == null || !tasks.containsKey(taskID)) {
            return null;
        }
        prioritizedTasks.remove(tasks.get(taskID));  // Удаляем старый экземпляр
        tasks.replace(taskID, task);
        prioritizedTasks.add(task);  // Добавляем обновлённый
        return task;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Integer epicID = epic.getId();
        if (!epics.containsKey(epicID)) {
            return null;
        }

        // Пересчитываем статус эпика на основе текущих подзадач
        updateEpicStatus(epic);

        // Сохраняем обновлённый эпик
        epics.replace(epicID, epic);
        return epic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Integer subtaskID = subtask.getId();
        if (subtaskID == null || !subtasks.containsKey(subtaskID)) {
            return null;
        }
        prioritizedTasks.remove(subtasks.get(subtaskID));  // Удаляем старый
        int epicID = subtask.getEpicId();
        Subtask oldSubtask = subtasks.get(subtaskID);
        subtasks.replace(subtaskID, subtask);
        Epic epic = epics.get(epicID);
        ArrayList<Subtask> subtaskList = epic.getSubTaskList();
        subtaskList.remove(oldSubtask);
        subtaskList.add(subtask);
        epic.setSubtaskList(subtaskList);
        updateEpicStatus(epic);
        prioritizedTasks.add(subtask);  // Добавляем обновлённый
        return subtask;
    }

    @Override
    public void deleteTasks() {
        tasks.clear();
        prioritizedTasks.removeAll(tasks.values());  // Очищаем отсортированный набор
        historyManager.removeAll();
    }

    @Override
    public void deleteEpics() {
        epics.clear();
        subtasks.clear();
        prioritizedTasks.removeAll(subtasks.values());  // Удаляем подзадачи из набора
        historyManager.removeAll();
    }

    @Override
    public void deleteSubtasks() {
        subtasks.clear();
        epics.values().forEach(Epic::clearSubtasks);
        prioritizedTasks.removeAll(subtasks.values());  // Удаляем из отсортированного набора
        historyManager.removeAll();
    }

    @Override
    public Task deleteTaskByID(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            historyManager.remove(id);
            prioritizedTasks.remove(task);  // Удаляем из отсортированного набора
        }
        return task;
    }

    @Override
    public Epic deleteEpicByID(int id) {
        if (!epics.containsKey(id)) {
            return null;
        }
        Epic epic = epics.remove(id);
        // Удаляем все подзадачи эпика
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
        prioritizedTasks.remove(subtask);  // Удаляем из отсортированного набора
        historyManager.remove(id);
        return subtask;
    }

    public void updateEpicStatus(Epic epic) {
        List<Subtask> subtasks = epic.getSubTaskList();

        if (subtasks.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allDone = subtasks.stream().allMatch(t -> t.getStatus() == Status.DONE);
        boolean hasNotDone = subtasks.stream().anyMatch(t -> t.getStatus() != Status.DONE);

        if (allDone) {
            epic.setStatus(Status.DONE);
        } else if (hasNotDone) {

            epic.setStatus(Status.IN_PROGRESS);
        } else {
            epic.setStatus(Status.NEW);
        }
    }

    @Override
    public Task getTaskByID(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
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

    protected Map<Integer, Epic> getEpicsMap() {
        return epics;
    }

}