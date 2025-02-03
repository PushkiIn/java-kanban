import java.util.ArrayList;

class Epic extends Task {
    private ArrayList<Integer> subTaskIds = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description);
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

    @Override
    public String toString() {
        return "Epic{id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subTaskIds=" + subTaskIds + '}';
    }
}
