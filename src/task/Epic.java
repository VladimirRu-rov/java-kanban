package task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {
    private ArrayList<Subtask> subTaskList = new ArrayList<>();

    public Epic(int id, String name, String description, Status status,
                Duration duration, LocalDateTime startTime) {
        super(id, name, description, status);
        setDuration(duration);
        setStartTime(startTime);
        recalculateEpicTimes();
    }

    public void addSubTask(Subtask subtask) {
        if (subTaskList == null) {
            subTaskList = new ArrayList<>();
        }
        subTaskList.add(subtask);
        recalculateEpicTimes();
    }

    public void clearSubtasks() {
        subTaskList.clear();
        recalculateEpicTimes();
    }

    public void setSubtaskList(ArrayList<Subtask> subtaskList) {
        this.subTaskList = (subtaskList != null) ? subtaskList : new ArrayList<>();
        recalculateEpicTimes();
    }

    private void calculateEpicDuration() {
        if (subTaskList.isEmpty()) {
            setDuration(Duration.ofMinutes(0));
            return;
        }

        Duration totalDuration = subTaskList.stream()
                .filter(subtask -> subtask.getDuration() != null)
                .map(Subtask::getDuration)
                .reduce(Duration.ZERO, Duration::plus);
        setDuration(totalDuration);
    }

    private void calculateEpicStartTime() {
        if (subTaskList.isEmpty()) {
            setStartTime(null);
            return;
        }

        LocalDateTime earliestStart = subTaskList.stream()
                .filter(subtask -> subtask.getStartTime() != null)
                .map(Subtask::getStartTime)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        setStartTime(earliestStart);
    }

    private void calculateEpicEndTime() {
        if (subTaskList.isEmpty()) {
            return;
        }

        LocalDateTime latestEnd = subTaskList.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        setEndTime(latestEnd);
    }

    @Override
    public LocalDateTime getEndTime() {
        if (subTaskList.isEmpty()) return null;

        return subTaskList.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    private void recalculateEpicTimes() {
        calculateEpicDuration();
        calculateEpicStartTime();
        calculateEpicEndTime();
    }

    public ArrayList<Subtask> getSubTaskList() {
        if (subTaskList == null) {
            subTaskList = new ArrayList<>();
        }
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
}