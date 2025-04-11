import enums.Status;
import manager.FileBackedTaskManager;
import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

public class FileBackedManagerTest {

    @Test
    public void testSaveAndLoadEmptyManager() throws IOException {
        File tempFile = File.createTempFile("tasks", ".csv");
        tempFile.deleteOnExit();

        FileBackedTaskManager manager = new FileBackedTaskManager();
        manager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubTasks().isEmpty());
    }

    @Test
    public void testSaveAndLoadMultipleTasks() throws IOException {
        File tempFile = new File("tasks.csv");

        FileBackedTaskManager manager = new FileBackedTaskManager();

        Task task1 = new Task(1, "Task 1", "Description 1", Status.NEW);
        Epic epic1 = new Epic(2, "Epic 1", "Description 2", Status.NEW);
        SubTask subTask1 = new SubTask(3, "Subtask 1", "Description 3", Status.NEW, 2);

        manager.createTask(task1);
        manager.createEpic(epic1);
        manager.createSubTask(subTask1);

        manager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loadedManager.getAllTasks().size());
        assertEquals(1, loadedManager.getAllEpics().size());
        assertEquals(1, loadedManager.getAllSubTasks().size());

        Task loadedTask = loadedManager.getAllTasks().get(0);
        Epic loadedEpic = loadedManager.getAllEpics().get(0);
        SubTask loadedSubTask = loadedManager.getAllSubTasks().get(0);

        assertEquals("Task 1", loadedTask.getName());
        assertEquals("Epic 1", loadedEpic.getName());
        assertEquals("Subtask 1", loadedSubTask.getName());
    }

    @Test
    public void testSaveAfterDeletion() throws IOException {
        File tempFile = File.createTempFile("tasks", ".csv");
        tempFile.deleteOnExit();

        FileBackedTaskManager manager = new FileBackedTaskManager();

        Task task1 = new Task(1, "Task 1", "Description 1", Status.NEW);
        Epic epic1 = new Epic(2, "Epic 1", "Description 2", Status.NEW);
        SubTask subTask1 = new SubTask(3, "Subtask 1", "Description 3", Status.NEW, 2);

        manager.createTask(task1);
        manager.createEpic(epic1);
        manager.createSubTask(subTask1);

        manager.save();

        manager.deleteAllTasks();
        manager.deleteAllEpics();
        manager.deleteAllSubTasks();

        manager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubTasks().isEmpty());
    }

    @Test
    public void testSerializationAndDeserialization() throws IOException {
        File tempFile = new File("tasks.csv");

        FileBackedTaskManager manager = new FileBackedTaskManager();

        Task task1 = new Task(1, "Task 1", "Description 1", Status.NEW);
        Epic epic1 = new Epic(2, "Epic 1", "Description 2", Status.NEW);
        SubTask subTask1 = new SubTask(3, "Subtask 1", "Description 3", Status.NEW, 2);

        manager.createTask(task1);
        manager.createEpic(epic1);
        manager.createSubTask(subTask1);

        manager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loadedManager.getAllTasks().size());
        assertEquals(1, loadedManager.getAllEpics().size());
        assertEquals(1, loadedManager.getAllSubTasks().size());

        Task loadedTask = loadedManager.getAllTasks().get(0);
        Epic loadedEpic = loadedManager.getAllEpics().get(0);
        SubTask loadedSubTask = loadedManager.getAllSubTasks().get(0);

        assertEquals("Task 1", loadedTask.getName());
        assertEquals("Epic 1", loadedEpic.getName());
        assertEquals("Subtask 1", loadedSubTask.getName());
    }
}