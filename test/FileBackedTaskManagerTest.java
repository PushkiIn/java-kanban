import enums.Status;
import manager.FileBackedTaskManager;

import model.Epic;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @TempDir
    File tempDir;
    File testFile;

    @BeforeEach
    void beforeEach() {
        testFile = new File(tempDir, "test.csv");
    }

    @Override
    protected FileBackedTaskManager createTaskManager() {
        return new FileBackedTaskManager() {
            @Override
            public void save() {
                try (FileWriter writer = new FileWriter(testFile)) {
                    writer.write("id,type,name,status,description,duration,startTime,epic\n");
                    for (var task : getTasks()) {
                        writer.write(tools.StringConverter.toString(task) + "\n");
                    }
                    for (var epic : getEpics()) {
                        writer.write(tools.StringConverter.toString(epic) + "\n");
                    }
                    for (var sub : getSubTasks()) {
                        writer.write(tools.StringConverter.toString(sub) + "\n");
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Ошибка при сохранении в файл во время теста", e);
                }
            }
        };
    }

    @Override
    protected boolean invokeHasTimeConflict(Task task) {
        return ((FileBackedTaskManager) taskManager).hasTimeConflict(task);
    }

    @AfterEach
    void cleanUp() {
        if (testFile.exists()) {
            testFile.delete();
        }
    }

    @Test
    void testLoadFromFile() {
        taskManager.createTask(createTask(LocalDateTime.now(), Duration.ofMinutes(15)));
        taskManager.createEpic(createEpic());
        Epic epic = taskManager.getEpics().get(0);
        taskManager.createSubTask(createSubtask(epic.getId(), Status.NEW, LocalDateTime.now().plusHours(1), Duration.ofMinutes(15)));

        FileBackedTaskManager loadedManager = new FileBackedTaskManager().loadFromFile(testFile);

        assertEquals(taskManager.getTasks().size(), loadedManager.getTasks().size());
        assertEquals(taskManager.getEpics().size(), loadedManager.getEpics().size());
        assertEquals(taskManager.getSubTasks().size(), loadedManager.getSubTasks().size());
    }
}