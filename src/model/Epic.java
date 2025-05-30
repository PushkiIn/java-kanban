package model;

import enums.Status;
import enums.TaskType;

import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subTaskIds = new ArrayList<>();
    private TaskType type = TaskType.EPIC;

    public Epic(String name, String description) {
        super(name, description);
    }

    public Epic(int id, String name, String description, Status status) {
        super(id, name, description, status);
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
}
