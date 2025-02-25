package interfaces;

import model.Task;

import java.util.List;

public interface HistoryManager {
    void add(Task task);

    public List<Task> getHistory();
}