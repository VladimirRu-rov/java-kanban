package manage;

import task.Task;
import task.Epic;
import task.Subtask;

import java.util.List;


public interface TaskManager {
    int getNextId();

    Task addTask(Task task);

    Epic addEpic(Epic epic);

    Subtask addSubTask(Subtask subtask);

    Task updateTask(Task task);

    Epic updateEpic(Epic epic);

    Subtask updateSubtask(Subtask subtask);

    Task getTaskByID(int id);

    Epic getEpicByID(int id);

    Subtask getSubtaskByID(int id);

    List<Task> getTasks();

    List<Epic> getEpics();

    List<Subtask> getSubtasks();

    List<Subtask> getEpicSubtasks(Epic epic);

    void deleteTasks();

    void deleteEpics();

    void deleteSubtasks();

    Task deleteTaskByID(int id);

    Epic deleteEpicByID(int id);

    Subtask deleteSubtaskByID(int id);

    List<Task> getHistory();
}