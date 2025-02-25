package util;

import interfaces.TaskManager;
import manager.InMemoryHistoryManager;
import manager.InMemoryTaskManager;

public class Managers {
    private Managers() {}

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    static public InMemoryHistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}