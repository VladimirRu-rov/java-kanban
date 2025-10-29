package task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;

public class Task {
    private String name;
    private String description;
    private int id;
    private Status status;
    private Duration duration;
    private LocalDateTime startTime;

    protected static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final Comparator<Task> START_TIME_COMPARATOR = (t1, t2) -> {
        if (t1.getStartTime() == null) return 1;
        if (t2.getStartTime() == null) return -1;

        int timeCompare = t1.getStartTime().compareTo(t2.getStartTime());
        if (timeCompare != 0) return timeCompare;

        return Integer.compare(t1.getId(), t2.getId());
    };

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

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Duration getDuration() {
        return duration;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null) {
            return null; // Вернем null, если startTime не задан
        }
        return startTime.plus(duration);
    }

    public TaskType getTaskType() {
        if (this instanceof Epic) return TaskType.EPIC;
        if (this instanceof Subtask) return TaskType.SUBTASK;
        return TaskType.TASK;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public static Task fromString(String line) {
        String[] parts = line.split(",");
        if (parts.length < 7) throw new IllegalArgumentException("Неверный формат CSV");

        try {
            int id = Integer.parseInt(parts[0]);
            TaskType type = TaskType.valueOf(parts[1]);
            String name = parts[2];
            Status status = Status.valueOf(parts[3]);
            String description = parts[4];
            long durationMinutes = Long.parseLong(parts[5]);
            LocalDateTime startTime = parts[6].isEmpty()
                    ? null
                    : LocalDateTime.parse(parts[6], DATE_FORMATTER);
            Duration duration = Duration.ofMinutes(durationMinutes);

            switch (type) {
                case TASK:
                    return new Task(id, name, description, status, duration, startTime);
                case EPIC:
                    LocalDateTime endTime = parts.length > 7 && !parts[7].isEmpty()
                            ? LocalDateTime.parse(parts[7], DATE_FORMATTER)
                            : null;
                    return new Epic(id, name, description, status, duration, startTime);
                case SUBTASK:
                    int epicId = Integer.parseInt(parts[7]);
                    return new Subtask(id, name, description, status, duration, startTime, epicId);
                default:
                    throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка парсинга CSV: " + e.getMessage());
        }
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

    public String toCsvString() {
        return getId() + "," +
                getTaskType() + "," +
                getName() + "," +
                getStatus() + "," +
                getDescription() + "," +
                (getDuration() != null ? getDuration().toMinutes() : "") + "," +
                (getStartTime() != null ? getStartTime().format(DATE_FORMATTER) : "");
    }

    public boolean hasOverlappingTasks(Collection<Task> otherTasks) {
        return otherTasks.stream()
                .anyMatch(this::isOverlapping);
    }

    public boolean isOverlapping(Task other) {
        return this.getStartTime().isBefore(other.getEndTime()) &&
                other.getStartTime().isBefore(this.getEndTime());
    }
}