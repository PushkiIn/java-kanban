package model;

import enums.Status;
import enums.TaskType;

public class SubTask extends Task {
    private int epicId;
    private TaskType type = TaskType.SUBTASK;

    public SubTask(String name, String description, int epicId) {
        super(name, description);
        this.epicId = epicId;
    }

    public SubTask(String name, String description, int epicId, Status status) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public SubTask(int id, String name, String description, Status status, int epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    @Override
    public String toString() {
        return "model.SubTask{id=" + getId() + ", name='" + getName() + '\'' + ", description='" + getDescription() + '\'' + ", status=" + getStatus() + ", epicId=" + epicId + '}';
    }
}
