package model;

import enums.Status;
import enums.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String name, String description, int epicId) {
        super(name, description);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, Status status, int epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public Subtask(int id, String name, String description, Status status, int epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    public Subtask(int id, String name, String description, Status status, LocalDateTime startTime, Duration duration, int epicId) {
        super(id, name, description, status, startTime, duration);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, Status status, LocalDateTime startTime, Duration duration, int epicId) {
        super(name, description, status, startTime, duration);
        this.epicId = epicId;
    }

    public Subtask(Subtask subTask) {
        super(subTask);
        this.epicId = subTask.epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.SUBTASK;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Subtask subTask = (Subtask) o;

        return Objects.equals(id, subTask.id)
                && Objects.equals(name, subTask.name)
                && Objects.equals(description, subTask.description)
                && status == subTask.status
                && Objects.equals(startTime, subTask.startTime)
                && Objects.equals(duration, subTask.duration)
                && Objects.equals(epicId, subTask.epicId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicId);
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedStart = (startTime != null) ? startTime.format(formatter) : "null";
        String formattedDuration = (duration != null) ? duration.toMinutes() + " mins" : "null";

        return String.format("SubTask[id=%d, taskType='%s', name='%s', status=%s, startTime=%s, duration=%s, epicId=%d]",
                id, getTaskType().name(), name, status, formattedStart, formattedDuration, epicId);
    }
}