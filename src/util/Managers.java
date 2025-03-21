package util;

import manager.InMemoryHistoryManager;
import manager.InMemoryTaskManager;
import manager.TaskManager;

public class Managers {
    private Managers() {}

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    static public InMemoryHistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}