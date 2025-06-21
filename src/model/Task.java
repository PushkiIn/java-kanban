package model;

import enums.Status;
import enums.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    private String name;
    private String description;
    private Status status;
    private int id = 0;
    protected Duration duration;
    protected LocalDateTime startTime;

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = Status.NEW;
    }

    public Task(String name, String description, Status status, LocalDateTime startTime, Duration duration) {
        this.description = description;
        this.name = name;
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(String name, String description, LocalDateTime startTime, Duration duration) {
        this.description = description;
        this.name = name;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(String name, String description, Status status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public Task(int id, String name, String description, Status status) {
        this.id = id;
        this.description = description;
        this.name = name;
        this.status = status;
    }

    public Task(int id, String name, String description, Status status, Duration duration, LocalDateTime startTime) {
        this.id = id;
        this.description = description;
        this.name = name;
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (this.id == 0) {
            this.id = id;
        }
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TaskType getType() {
        return TaskType.TASK;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getEndTime() {
        return startTime.plus(duration);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        if (this.id == 0 || task.id == 0) {
            return Objects.equals(this.name, task.name) && Objects.equals(this.description, task.description);
        } else {
            return id == task.id;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description);
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,%s,%s",
                this.getId(),
                this.getType(),
                this.getName(),
                this.getStatus(),
                this.getDescription(),
                this.startTime.toString(),
                this.duration.toString());
    }
}