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

    public static Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationAdapter());
        return gsonBuilder.create();
    }
}