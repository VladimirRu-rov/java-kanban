package task;

public class Subtask extends Task {

    private final int epicId;

    // Конструктор, принимающий имя, описание и идентификатор task.Epic
    public Subtask(String name, String description, int epicId) {
        super(name, description);
        this.epicId = epicId;
    }

    // Конструктор, принимающий имя, описание, статус, идентификатор и идентификатор task.Epic
    public Subtask(int id, String name, String description, Status status, int epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    // Метод для получения идентификатора task.Epic
    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Подзадача -- " +
                "Название: " + getName() +
                ". Описание: " + getDescription() +
                ". id = " + getId() +
                ". epicID = " + epicId +
                ". Статус: " + getStatus() +
                '.';
    }

    // Метод для вывода текста в файл
    @Override
    public String toCsvString() {
        return getId() + "," + getTaskType() + "," + getName() + "," + getStatus() + "," + getDescription() + "," + epicId + ",";
    }
}