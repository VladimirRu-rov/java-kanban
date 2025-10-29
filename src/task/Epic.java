package task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {
    private ArrayList<Subtask> subTaskList = new ArrayList<>();

    public Epic(int id, String name, String description, Status status,
                Duration duration, LocalDateTime startTime) {
        super();
        setId(id);
        setName(name);
        setDescription(description);
        setStatus(status);
        setDuration(duration);
        setStartTime(startTime);
    }

    public void addSubTask(Subtask subtask) {
        subTaskList.add(subtask);
        calculateEpicTime();
    }

    public void clearSubtasks() {
        subTaskList.clear();
        calculateEpicTime();
    }

    public void setSubtaskList(ArrayList<Subtask> subtaskList) {
        this.subTaskList = subtaskList;
        calculateEpicTime();
    }

    private void calculateEpicTime() {
        if (subTaskList.isEmpty()) {
            setDuration(Duration.ofMinutes(0));
            setStartTime(null);
            return;
        }

        // Определяем общую длительность
        Duration totalDuration = subTaskList.stream()
                .filter(subtask -> subtask.getDuration() != null) // фильтруем задачи с null-длительностью
                .map(Subtask::getDuration)
                .reduce(Duration.ZERO, Duration::plus);
        setDuration(totalDuration);

        // Находим самое раннее время начала среди подзадач
        LocalDateTime earliestStart = subTaskList.stream()
                .filter(subtask -> subtask.getStartTime() != null) // фильтруем задачи с null-временем
                .map(Subtask::getStartTime)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        setStartTime(earliestStart);
    }

    @Override
    public LocalDateTime getEndTime() {
        if (subTaskList.isEmpty()) return null;

        return subTaskList.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull) // 👈 добавляем фильтрацию, чтобы отбрасывать null-значения
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    public ArrayList<Subtask> getSubTaskList() {
        return subTaskList;
    }

    @Override
    public String toString() {
        return "Эпик -- " +
                "Название: " + getName() +
                ", Описание: " + getDescription() +
                ", id: " + getId() +
                ", Длительность: " + getDuration() +
                ", Начало: " + getStartTime() +
                ", Окончание: " + getEndTime() +
                ", subtaskList.size = " + subTaskList.size() +
                ", Статус: " + getStatus() +
                '.';
    }

    @Override
    public String toCsvString() {
        return getId() + "," +
                getTaskType() + "," +
                getName() + "," +
                getStatus() + "," +
                getDescription() + "," +
                (getDuration() != null ? getDuration().toMinutes() : "") + "," +
                (getStartTime() != null ? getStartTime().format(DATE_FORMATTER) : "") + "," +
                (getEndTime() != null ? getEndTime().format(DATE_FORMATTER) : "");
    }
}