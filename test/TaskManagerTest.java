import enums.Status;
import manager.TaskManager;
import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;

    protected Task createTask() {
        Task task = new Task("Task1", "Description", Status.NEW, LocalDateTime.now().plusHours(1), Duration.ofHours(1));
        return task;
    }

    protected Epic createEpic() {
        return new Epic("Epic1", "Epic Description");
    }

    protected SubTask createSubTask(int epicId) {
        SubTask subTask = new SubTask("SubTask1", "SubDesc", Status.NEW, LocalDateTime.now().plusHours(1), Duration.ofHours(1), epicId);
        return subTask;
    }

    protected SubTask createSubTask(int epicId, Status status, LocalDateTime startTime, Duration duration) {
        SubTask subTask = new SubTask("SubTask1", "SubDesc", status, startTime, duration, epicId);
        return subTask;
    }

    @BeforeEach
    void setup() {
        taskManager = createTaskManager();
    }

    protected abstract T createTaskManager();

    @Test
    void testCreateAndGetTask() {
        Task task = createTask();
        taskManager.createTask(task);
        assertEquals(1, taskManager.getAllTasks().size());
        assertEquals(task, taskManager.getTaskById(task.getId()));
    }

    @Test
    public void testDeleteTask() {
        Task task = createTask();
        taskManager.createTask(task);
        taskManager.deleteTask(task.getId());
        assertEquals(0, taskManager.getAllTasks().size());
        assertNull(taskManager.getTaskById(1));
    }

    @Test
    public void testUpdateTask() {
        Task task = createTask();
        taskManager.createTask(task);
        task.setName("Измененная задача");
        taskManager.updateTask(task);
        assertEquals("Измененная задача", taskManager.getTaskById(task.getId()).getName());
    }


    @Test
    void testCreateAndGetSubTask() {
        Epic epic = createEpic();
        taskManager.createEpic(epic);

        SubTask subTask = createSubTask(epic.getId());
        taskManager.createSubTask(subTask);

        assertEquals(subTask, taskManager.getSubTaskById(subTask.getId()));
    }

    @Test
    public void testCreateAndGetEpic() {
        Epic epic = createEpic();
        taskManager.createEpic(epic);
        assertEquals(1, taskManager.getAllEpics().size());
        assertEquals(epic, taskManager.getEpicById(epic.getId()));
    }

    @Test
    void testEpicStatus_AllNew() {
        Epic epic = createEpic();
        taskManager.createEpic(epic);

        SubTask st1 = createSubTask(epic.getId(), Status.NEW, LocalDateTime.now(), Duration.ofHours(1));
        SubTask st2 = createSubTask(epic.getId(), Status.NEW, LocalDateTime.now().plusHours(2), Duration.ofHours(2));
        taskManager.createSubTask(st1);
        taskManager.createSubTask(st2);

        taskManager.updateEpicStatus(epic);
        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(Status.NEW, updatedEpic.getStatus());
    }

    @Test
    void testEpicStatus_AllDone() {
        Epic epic = createEpic();
        taskManager.createEpic(epic);

        SubTask st1 = createSubTask(epic.getId(), Status.DONE, LocalDateTime.now(), Duration.ofHours(1));
        SubTask st2 = createSubTask(epic.getId(), Status.DONE, LocalDateTime.now().plusHours(2), Duration.ofHours(2));
        taskManager.createSubTask(st1);
        taskManager.createSubTask(st2);

        taskManager.updateEpicStatus(epic);
        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(Status.DONE, updatedEpic.getStatus());
    }

    @Test
    void testEpicStatus_NewAndDone() {
        Epic epic = createEpic();
        taskManager.createEpic(epic);

        SubTask st1 = createSubTask(epic.getId(), Status.NEW, LocalDateTime.now(), Duration.ofHours(1));
        SubTask st2 = createSubTask(epic.getId(), Status.DONE, LocalDateTime.now().plusHours(2), Duration.ofHours(2));
        taskManager.createSubTask(st1);
        taskManager.createSubTask(st2);

        taskManager.updateEpicStatus(epic);
        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(Status.IN_PROGRESS, updatedEpic.getStatus());
    }

    @Test
    void testEpicStatus_InProgress() {
        Epic epic = createEpic();
        taskManager.createEpic(epic);

        SubTask st1 = createSubTask(epic.getId());
        st1.setStatus(Status.IN_PROGRESS);
        taskManager.createSubTask(st1);

        taskManager.updateEpicStatus(epic);
        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(Status.IN_PROGRESS, updatedEpic.getStatus());
    }

    @Test
    void testTimeConflict_NoConflict() {
        Task task1 = createTask();
        taskManager.createTask(task1);

        Task task2 = createTask();
        task2.setStartTime(task1.getStartTime().plusHours(2));  // после task1
        task2.setDuration(Duration.ofHours(1));
        boolean conflict = invokeHasTimeConflict(task2);
        assertFalse(conflict);
    }

    @Test
    void testTimeConflict_Conflict() {
        Task task1 = createTask();
        taskManager.createTask(task1);

        Task task2 = createTask();
        task2.setStartTime(task1.getStartTime().plusMinutes(30));  // пересекается с task1
        task2.setDuration(Duration.ofHours(1));
        boolean conflict = invokeHasTimeConflict(task2);
        assertTrue(conflict);
    }

    protected boolean invokeHasTimeConflict(Task task) {
        try {
            var method = taskManager.getClass().getDeclaredMethod("hasTimeConflict", Task.class);
            method.setAccessible(true);
            return (boolean) method.invoke(taskManager, task);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetAllTasks() {
        Task task1 = createTask();
        Task task2 = createTask();
        task2.setName("Task2");
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        List<Task> tasks = taskManager.getAllTasks();
        assertTrue(tasks.contains(task1));
        assertTrue(tasks.contains(task2));
        assertEquals(2, tasks.size());
    }


    @Test
    void testGetAllEpics() {
        Epic epic1 = createEpic();
        Epic epic2 = createEpic();
        epic2.setName("Epic2");
        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);

        List<Epic> epics = taskManager.getAllEpics();
        assertTrue(epics.contains(epic1));
        assertTrue(epics.contains(epic2));
        assertEquals(2, epics.size());
    }

    @Test
    void testGetAllSubTasks() {
        Epic epic = createEpic();
        taskManager.createEpic(epic);

        SubTask subTask1 = createSubTask(epic.getId(), Status.NEW, LocalDateTime.now(), Duration.ofHours(1));
        SubTask subTask2 = createSubTask(epic.getId(), Status.NEW, LocalDateTime.now().plusHours(3), Duration.ofHours(2));
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);

        List<SubTask> subTasks = taskManager.getAllSubTasks();
        assertTrue(subTasks.contains(subTask1));
        assertTrue(subTasks.contains(subTask2));
        assertEquals(2, subTasks.size());
    }

    @Test
    void testDeleteAllTasks() {
        Task task1 = createTask();
        Task task2 = createTask();
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        taskManager.deleteAllTasks();
        assertTrue(taskManager.getAllTasks().isEmpty());
    }

    @Test
    void testDeleteAllEpics() {
        Epic epic1 = createEpic();
        Epic epic2 = createEpic();
        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);

        taskManager.deleteAllEpics();
        assertTrue(taskManager.getAllEpics().isEmpty());
    }

    @Test
    void testDeleteAllSubTasks() {
        Epic epic = createEpic();
        taskManager.createEpic(epic);

        SubTask subTask1 = createSubTask(epic.getId(), Status.NEW, LocalDateTime.now(), Duration.ofHours(1));
        SubTask subTask2 = createSubTask(epic.getId(), Status.NEW, LocalDateTime.now().plusHours(3), Duration.ofHours(2));
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);

        taskManager.deleteAllSubTasks();
        assertTrue(taskManager.getAllSubTasks().isEmpty());
    }

    @Test
    void testGetHistory() {
        Task task1 = createTask();
        task1.setName("Task 1");
        Task task2 = createTask();
        task2.setName("Task 2");

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());
        taskManager.getTaskById(task1.getId());

        List<Task> history = taskManager.getHistory();

        assertEquals(2, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task1, history.get(1));
    }

    @Test
    void testGetPrioritizedTasks() {
        Task task1 = new Task("T1", "Desc1", LocalDateTime.of(2023, 1, 1, 10, 0), Duration.ofMinutes(60));
        Task task2 = new Task("T2", "Desc2", LocalDateTime.of(2023, 1, 1, 8, 0), Duration.ofMinutes(30));
        Task task3 = new Task("T3", "Desc3", LocalDateTime.of(2023, 1, 1, 12, 0), Duration.ofMinutes(45));

        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.createTask(task3);

        List<Task> prioritized = new ArrayList<>(taskManager.getPrioritizedTasks());

        assertEquals(3, prioritized.size());
        assertEquals(task2, prioritized.get(0));
        assertEquals(task1, prioritized.get(1));
        assertEquals(task3, prioritized.get(2));
    }
}