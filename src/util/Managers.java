package util;

import manager.FileBackedTaskManager;
import manager.InMemoryHistoryManager;
import manager.TaskManager;

public class Managers {
    private Managers() {

    }

    public static TaskManager getDefault() {
        return new FileBackedTaskManager();
    }

    public static InMemoryHistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}