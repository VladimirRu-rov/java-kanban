package task;

import java.util.ArrayList;

public class Epic extends Task {

    private ArrayList<Subtask> subTaskList = new ArrayList<>();

    // Конструктор, принимающий имя и описание
    public Epic(String name, String description) {
        super(name, description);
    }

    // Конструктор, принимающий имя, описание, статус и идентификатор
    public Epic(int id, String name, String description, Status status) {
        super(id, name, description, status);
    }

    // Метод для добавления новой субзадачи в список
    public void addSubTask(Subtask subtask) {
        subTaskList.add(subtask);
    }

    // Метод для очистки списка субзадач
    public void clearSubtasks() {
        subTaskList.clear();
    }

    // Метод для получения списка субзадач
    public ArrayList<Subtask> getSubTaskList() {
        return subTaskList;
    }

    // Метод для установки нового списка субзадач
    public void setSubtaskList(ArrayList<Subtask> subtaskList) {
        this.subTaskList = subtaskList;
    }

    @Override
    public String toString() {
        return "Эпик -- " +
                "Название: " + getName() +
                ", Описание: " + getDescription() +
                ", id. " + getId() +
                ", subtaskList.size = " + subTaskList.size() +
                ", Статус: " + getStatus() +
                '.';
    }
}