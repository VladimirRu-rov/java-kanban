package manager.task;

import task.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CsvTaskUtils {

    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String taskToCsvString(Task task) {
        String epicId = (task instanceof Subtask)
                ? String.valueOf(((Subtask) task).getEpicId())
                : "";
        return String.join(",",
                String.valueOf(task.getId()),
                task.getTaskType().toString(),
                task.getName(),
                task.getStatus().toString(),
                (task.getDescription() != null) ? task.getDescription() : "",
                (task.getDuration() != null) ? String.valueOf(task.getDuration().toMinutes()) : "0",
                (task.getStartTime() != null) ? task.getStartTime().format(DATE_FORMATTER) : "",
                epicId
        );
    }

    public static Task taskFromString(String line) {
        String[] parts = line.split(",", -1); // -1 → сохраняем все поля

        if (parts.length != 8) {
            throw new IllegalArgumentException(
                    "Неверный формат CSV: ожидается 8 полей, найдено " + parts.length + " в строке: '" + line + "'"
            );
        }

        try {
            int id = Integer.parseInt(parts[0]);
            TaskType type = TaskType.valueOf(parts[1]);
            String name = parts[2];
            Status status = Status.valueOf(parts[3]);
            String description = parts[4].isEmpty() ? null : parts[4];
            long durationMinutes = parts[5].isEmpty() ? 0 : Long.parseLong(parts[5]);
            LocalDateTime startTime = parts[6].isEmpty()
                    ? null
                    : LocalDateTime.parse(parts[6], DATE_FORMATTER);
            Duration duration = Duration.ofMinutes(durationMinutes);

            switch (type) {
                case TASK:
                    return new Task(id, name, description, status, duration, startTime);
                case EPIC:
                    Epic epic = new Epic(id, name, description, status, duration, startTime);
                    return epic;
                case SUBTASK:
                    int epicId = Integer.parseInt(parts[7]);
                    return new Subtask(id, name, description, status, duration, startTime, epicId);
                default:
                    throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка парсинга CSV: " + e.getMessage(), e);
        }
    }
}