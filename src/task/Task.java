package task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public class Task {
    public static final Comparator<Task> START_TIME_COMPARATOR = (t1, t2) -> {
        if (t1.getStartTime() == null) return 1;
        if (t2.getStartTime() == null) return -1;

        int timeCompare = t1.getStartTime().compareTo(t2.getStartTime());
        if (timeCompare != 0) return timeCompare;

        return Integer.compare(t1.getId(), t2.getId());
    };
    protected static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private String name;
    private String description;
    private int id;
    private Status status;
    private Duration duration;
    private LocalDateTime startTime;

    protected Task() {
    }

    public Task(int id, String name, String description, Status status,
                Duration duration, LocalDateTime startTime) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
    }

    public Task(String name, String description, Duration duration, LocalDateTime startTime) {
        this.name = name;
        this.description = description;
        this.status = Status.NEW;
        this.duration = duration;
        this.startTime = startTime;
    }

    public Task(int id, String name, String description, Status status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        if (duration != null && duration.isNegative()) {
            throw new IllegalArgumentException("Длительность не может быть отрицательной");
        }
        this.duration = duration;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    public void setEndTime(LocalDateTime endTime) {
    }

    public TaskType getTaskType() {
        if (this instanceof Epic) return TaskType.EPIC;
        if (this instanceof Subtask) return TaskType.SUBTASK;
        return TaskType.TASK;
    }

    @Override
    public String toString() {
        return "Задача: " +
                "ID=" + getId() + ", " +
                "Название='" + getName() + "', " +
                "Статус=" + getStatus() + ", " +
                "Описание='" + getDescription() + "', " +
                "Длительность=" + getDuration() + ", " +
                "Начало=" + getStartTime();
    }
}
