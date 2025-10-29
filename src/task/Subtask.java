package task;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(int id, String name, String description, Status status, Duration duration, LocalDateTime startTime, int epicId) {
        super(id, name, description, status, duration, startTime);
        this.epicId = epicId;
    }

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
                ". Длительность: " + getDuration() +
                ". Начало: " + getStartTime() +
                ". Статус: " + getStatus() +
                '.';
    }

    @Override
    public String toCsvString() {
        return getId() + "," +
                getTaskType() + "," +
                getName() + "," +
                getStatus() + "," +
                getDescription() + "," +
                getDuration().toMinutes() + "," +
                (getStartTime() != null ? getStartTime().format(DATE_FORMATTER) : "") + "," +
                getEpicId();
    }
}