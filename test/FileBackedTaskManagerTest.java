import manager.FileBackedTaskManager;

import model.Epic;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;

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
                    for (var task : getAllTasks()) {
                        writer.write(tools.StringConverter.toString(task) + "\n");
                    }
                    for (var epic : getAllEpics()) {
                        writer.write(tools.StringConverter.toString(epic) + "\n");
                    }
                    for (var sub : getAllSubTasks()) {
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
        taskManager.createTask(createTask());
        taskManager.createEpic(createEpic());
        Epic epic = taskManager.getAllEpics().get(0);
        taskManager.createSubTask(createSubTask(epic.getId()));

        FileBackedTaskManager loadedManager = new FileBackedTaskManager().loadFromFile(testFile);

        assertEquals(taskManager.getAllTasks().size(), loadedManager.getAllTasks().size());
        assertEquals(taskManager.getAllEpics().size(), loadedManager.getAllEpics().size());
        assertEquals(taskManager.getAllSubTasks().size(), loadedManager.getAllSubTasks().size());
    }
}