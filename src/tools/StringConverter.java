package tools;

import enums.Status;
import enums.TaskType;
import model.Epic;
import model.SubTask;
import model.Task;

public class StringConverter {
    public static Task fromString(String value) {
        String[] fields = value.split(",");
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];
        int epicId = fields.length > 5 ? Integer.parseInt(fields[5]) : 0;

        switch (type) {
            case TASK:
                return new Task(id, name, description, status);
            case EPIC:
                return new Epic(id, name, description, status);
            case SUBTASK:
                return new SubTask(id, name, description, status, epicId);
            default:
                throw new IllegalArgumentException("Unknown task type: " + type);
        }
    }

    public static String toString(Task task) {
        if(task instanceof SubTask) {
            SubTask subtask = (SubTask) task;
            return String.format("%d,%s,%s,%s,%s,%d",
                subtask.getId(),
                subtask.getType(),
                subtask.getName(),
                subtask.getStatus(),
                subtask.getDescription(),
                subtask.getEpicId()
            );
        } else {
            return String.format("%d,%s,%s,%s,%s",
                task.getId(),
                task.getType(),
                task.getName(),
                task.getStatus(),
                task.getDescription()
            );
        }
    }
}