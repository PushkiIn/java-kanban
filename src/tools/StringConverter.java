package tools;

import enums.Status;
import enums.TaskType;
import model.Epic;
import model.SubTask;
import model.Task;

import java.time.Duration;
import java.time.LocalDateTime;

public class StringConverter {
    public static Task fromString(String value) {
        String[] fields = value.split(",");
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];
        Duration duration = Duration.parse(fields[5]);
        LocalDateTime startTime = LocalDateTime.parse(fields[6]);

        int epicId = fields.length > 7 ? Integer.parseInt(fields[7]) : 0;

        switch (type) {
            case TASK:
                return new Task(id, name, description, status, duration, startTime);
            case EPIC:
                return new Epic(id, name, description, status, duration, startTime);
            case SUBTASK:
                return new SubTask(id, name, description, status, duration, startTime, epicId);
            default:
                throw new IllegalArgumentException("Unknown task type: " + type);
        }
    }

    public static String toString(Task task) {
        if (task instanceof SubTask) {
            SubTask subTask = (SubTask) task;
            String durationStr = (task.getDuration() != null) ? task.getDuration().toString() : "";
            String startTimeStr = (task.getStartTime() != null) ? task.getStartTime().toString() : "";
            return String.format("%d,%s,%s,%s,%s,%s,%s,%d",
                subTask.getId(),
                subTask.getType(),
                subTask.getName(),
                subTask.getStatus(),
                subTask.getDescription(),
                durationStr,
                startTimeStr,
                subTask.getEpicId()
            );
        } else {
            String durationStr = (task.getDuration() != null) ? task.getDuration().toString() : "";
            String startTimeStr = (task.getStartTime() != null) ? task.getStartTime().toString() : "";
            return String.format("%d,%s,%s,%s,%s,%s,%s",
                task.getId(),
                task.getType(),
                task.getName(),
                task.getStatus(),
                task.getDescription(),
                durationStr,
                startTimeStr
            );
        }
    }
}