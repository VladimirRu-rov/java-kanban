public class Subtask extends Task {

    private final int epicId;

    // Конструктор, принимающий имя, описание и идентификатор Epic
    public Subtask(String name, String description, int epicId) {
        super(name, description);
        this.epicId = epicId;
    }

    // Конструктор, принимающий имя, описание, статус, идентификатор и идентификатор Epic
    public Subtask(int id, String name, String description, Status status, int epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    // Метод для получения идентификатора Epic
    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", epicID=" + epicId +
                ", status=" + getStatus() +
                '}';
    }
}
