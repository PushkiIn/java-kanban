package model;

import enums.Status;
import enums.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private final List<Integer> subTasksIds;
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description);
        this.subTasksIds = new ArrayList<>();
    }

    public Epic(int id, String name, String description) {
        super(id, name, description);
        this.subTasksIds = new ArrayList<>();
    }

    public Epic(int id, String name, String description, Status status, LocalDateTime startTime, Duration duration) {
        super(id, name, description, status, startTime, duration);
        subTasksIds = new ArrayList<>();
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.EPIC;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public List<Integer> getSubTasks() {
        return subTasksIds;
    }

    public void addSubTaskById(int subtaskId) {
        subTasksIds.add(subtaskId);
    }

    public void removeSubTaskById(int subTaskId) {
        if (subTasksIds.contains(subTaskId)) {
            subTasksIds.remove(Integer.valueOf(subTaskId));
        }
    }

    public void removeAllSubTasks() {
        subTasksIds.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Epic epic = (Epic) o;

        if (id != 0) {
            return id == epic.id;
        }

        return Objects.equals(name, epic.name)
                && Objects.equals(description, epic.description)
                && status == epic.status
                && Objects.equals(startTime, epic.startTime)
                && Objects.equals(duration, epic.duration)
                && Objects.equals(subTasksIds, epic.subTasksIds);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + Objects.hash(subTasksIds);
    }

    @Override
    public String toString() {
        return super.toString().replace("Task", "Epic").replace("]", "") + String.format(", subTasks=%s]", subTasksIds);
    }
}