package task;

import java.util.Objects;

public class Task {
    private String name;
    private String description;
    private int id;
    private Status status;

    public Task(int id, String name, String description, Status status) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = status;
    }

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = Status.NEW;
    }

    // Геттеры и сеттеры
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    // Новый метод для определения типа задачи
    public TaskType getTaskType() {
        if (this instanceof Epic) {
            return TaskType.EPIC;
        } else if (this instanceof Subtask) {
            return TaskType.SUBTASK;
        }
        return TaskType.TASK;
    }

    @Override
    public String toString() {
        return id + "," + getTaskType() + "," + name + "," + status + "," + description + ",";
    }

    // Метод для создания задачи из строки CSV
    public static Task fromString(String line) {
        String[] parts = line.split(",", -1); // Используем "-1", чтобы учитывать пустые поля

        if (parts.length < 6) {
            throw new IllegalArgumentException("Ошибка в формате CSV: недостаточно полей в строке [" + line + "]");
        }

        int id = Integer.parseInt(parts[0].trim());
        TaskType type = TaskType.valueOf(parts[1].trim());
        String name = parts[2].trim();
        Status status = Status.valueOf(parts[3].trim());
        String description = parts[4].trim();

        switch (type) {
            case TASK:
                return new Task(id, name, description, status);
            case EPIC:
                return new Epic(id, name, description, status);
            case SUBTASK:
                int epicId = Integer.parseInt(parts[5].trim());
                return new Subtask(id, name, description, status, epicId);
            default:
                throw new IllegalArgumentException("Ошибка в формате CSV: неизвестный тип задачи [" + type + "]");
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Task task = (Task) object;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    // Метод для вывода текста в файл
    public String toCsvString() {
        return id + "," + getTaskType() + "," + name + "," + status + "," + description + "," ;
    }
}