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
    public TaskType getType() {
        return TaskType.EPIC;
    }

    public ArrayList<Integer> getSubTaskIds() {
        return subTaskIds;
    }

    public void addSubTaskId(int subtaskId) {
        subTaskIds.add(subtaskId);
    }

    public ArrayList<Integer> getSubtaskIds() {
        return subTaskIds;
    }

    public void removeSubTaskId(int subTaskId) {
        if (subTaskIds.contains(subTaskId)) {
            subTaskIds.remove(Integer.valueOf(subTaskId));
        }
    }

    public void removeAllSubTasks() {
        subTaskIds.clear();
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}