package model;

import enums.Status;
import enums.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    public String toString() {
        return super.toString().replace("Task", "Epic").replace("]", "") + String.format(", subTasks=%s]", subTasksIds);
    }
}