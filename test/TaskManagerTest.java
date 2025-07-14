import enums.Status;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
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

    protected Task createTask(LocalDateTime time, Duration duration) {
        Task task = new Task("Task1", "Description", Status.NEW, time, duration);
        return task;
    }

    protected Epic createEpic() {
        return new Epic("Epic1", "Epic Description");
    }

    protected Subtask createSubtask(int epicId) {
        Subtask subtask = new Subtask("SubTask1", "SubDesc", Status.NEW, LocalDateTime.now().plusHours(1), Duration.ofHours(1), epicId);
        return subtask;
    }

    protected Subtask createSubtask(int epicId, Status status, LocalDateTime startTime, Duration duration) {
        Subtask subtask = new Subtask("SubTask1", "SubDesc", status, startTime, duration, epicId);
        return subtask;
    }

    @BeforeEach
    void setup() {
        taskManager = createTaskManager();
    }

    protected abstract T createTaskManager();

    @Test
    void testCreateAndGetTask() {
        Task task = createTask(LocalDateTime.now(), Duration.ofMinutes(30));
        taskManager.createTask(task);
        assertEquals(1, taskManager.getTasks().size());
        assertEquals(task, taskManager.getTaskById(task.getId()));
    }

    @Test
    public void testDeleteTask() {
        Task task = createTask(LocalDateTime.now(), Duration.ofMinutes(15));
        taskManager.createTask(task);
        taskManager.deleteTaskById(task.getId());
        assertEquals(0, taskManager.getTasks().size());
        assertNull(taskManager.getTaskById(1));
    }

    @Test
    public void testUpdateTask() {
        Task task = createTask(LocalDateTime.now(), Duration.ofMinutes(15));
        taskManager.createTask(task);
        task.setName("Измененная задача");
        taskManager.updateTask(task);
        assertEquals("Измененная задача", taskManager.getTaskById(task.getId()).getName());
    }


    @Test
    void testCreateAndGetSubTask() {
        Epic epic = createEpic();
        taskManager.createEpic(epic);

        Subtask subTask = createSubtask(epic.getId());
        taskManager.createSubTask(subTask);

        assertEquals(subTask, taskManager.getSubTaskById(subTask.getId()));
    }

    @Test
    public void testCreateAndGetEpic() {
        Epic epic = createEpic();
        taskManager.createEpic(epic);
        assertEquals(1, taskManager.getEpics().size());
        assertEquals(epic, taskManager.getEpicById(epic.getId()));
    }

    @Test
    void testEpicStatus_AllNew() {
        Epic epic = createEpic();
        taskManager.createEpic(epic);

        Subtask st1 = createSubtask(epic.getId(), Status.NEW, LocalDateTime.now(), Duration.ofHours(1));
        Subtask st2 = createSubtask(epic.getId(), Status.NEW, LocalDateTime.now().plusHours(2), Duration.ofHours(2));
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

        Subtask st1 = createSubtask(epic.getId(), Status.DONE, LocalDateTime.now(), Duration.ofHours(1));
        Subtask st2 = createSubtask(epic.getId(), Status.DONE, LocalDateTime.now().plusHours(2), Duration.ofHours(2));
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

        Subtask st1 = createSubtask(epic.getId(), Status.NEW, LocalDateTime.now(), Duration.ofHours(1));
        Subtask st2 = createSubtask(epic.getId(), Status.DONE, LocalDateTime.now().plusHours(2), Duration.ofHours(2));
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

        Subtask st1 = createSubtask(epic.getId());
        st1.setStatus(Status.IN_PROGRESS);
        taskManager.createSubTask(st1);

        taskManager.updateEpicStatus(epic);
        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(Status.IN_PROGRESS, updatedEpic.getStatus());
    }

    @Test
    void shouldAddTaskBeforeExisting() {
        Task task1 = createTask(LocalDateTime.now(), Duration.ofHours(1));
        taskManager.createTask(task1);

        Task task2 = createTask(task1.getStartTime().minusHours(2), Duration.ofHours(1));
        boolean conflict = invokeHasTimeConflict(task2);
        assertFalse(conflict);
    }

    @Test
    void shouldAddTaskAfterExisting() {
        Task task1 = createTask(LocalDateTime.now(), Duration.ofHours(1));
        taskManager.createTask(task1);

        Task task2 = createTask(task1.getStartTime().plusHours(2), Duration.ofMinutes(30));
        boolean conflict = invokeHasTimeConflict(task2);
        assertFalse(conflict);
    }

    @Test
    void shouldFailWhenNewTaskStartsDuringExisting() {
        Task task1 = createTask(LocalDateTime.now(), Duration.ofHours(1));
        taskManager.createTask(task1);

        Task task2 = createTask(task1.getStartTime().plusMinutes(30), Duration.ofHours(1));
        boolean conflict = invokeHasTimeConflict(task2);
        assertTrue(conflict);
    }

    @Test
    void shouldFailWhenNewTaskEndsDuringExisting() {
        Task task1 = createTask(LocalDateTime.now(), Duration.ofHours(1));
        taskManager.createTask(task1);

        Task task2 = createTask(task1.getStartTime().minusMinutes(30), Duration.ofHours(1));
        boolean conflict = invokeHasTimeConflict(task2);
        assertTrue(conflict);
    }

    @Test
    void shouldFailWhenNewTaskInsideExisting() {
        Task task1 = createTask(LocalDateTime.now(), Duration.ofHours(1));
        taskManager.createTask(task1);

        Task task2 = createTask(task1.getStartTime().plusMinutes(30), Duration.ofMinutes(15));
        boolean conflict = invokeHasTimeConflict(task2);
        assertTrue(conflict);
    }

    @Test
    void shouldFailWhenExistingInsideNewTask() {
        Task task1 = createTask(LocalDateTime.now(), Duration.ofHours(1));
        taskManager.createTask(task1);

        Task task2 = createTask(task1.getStartTime().minusMinutes(30), Duration.ofHours(2));
        boolean conflict = invokeHasTimeConflict(task2);
        assertTrue(conflict);
    }

    @Test
    void shouldFailWhenStartAndEndAreEqual() {
        Task task1 = createTask(LocalDateTime.now(), Duration.ofHours(1));
        taskManager.createTask(task1);

        Task task2 = createTask(task1.getStartTime(), Duration.ofHours(1));
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
        Task task1 = createTask(LocalDateTime.now(), Duration.ofMinutes(30));
        Task task2 = createTask(LocalDateTime.now().plusHours(1), Duration.ofHours(30));
        task2.setName("Task2");
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        List<Task> tasks = taskManager.getTasks();
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

        List<Epic> epics = taskManager.getEpics();
        assertTrue(epics.contains(epic1));
        assertTrue(epics.contains(epic2));
        assertEquals(2, epics.size());
    }

    @Test
    void testGetAllSubTasks() {
        Epic epic = createEpic();
        taskManager.createEpic(epic);

        Subtask subtask1 = createSubtask(epic.getId(), Status.NEW, LocalDateTime.now(), Duration.ofHours(1));
        Subtask subtask2 = createSubtask(epic.getId(), Status.NEW, LocalDateTime.now().plusHours(3), Duration.ofHours(2));
        taskManager.createSubTask(subtask1);
        taskManager.createSubTask(subtask2);

        List<Subtask> subtasks = taskManager.getSubTasks();
        assertTrue(subtasks.contains(subtask1));
        assertTrue(subtasks.contains(subtask2));
        assertEquals(2, subtasks.size());
    }

    @Test
    void testDeleteAllTasks() {
        Task task1 = createTask(LocalDateTime.now(), Duration.ofMinutes(30));
        Task task2 = createTask(LocalDateTime.now().plusHours(1), Duration.ofHours(30));
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        taskManager.deleteTasks();
        assertTrue(taskManager.getTasks().isEmpty());
    }

    @Test
    void testDeleteAllEpics() {
        Epic epic1 = createEpic();
        Epic epic2 = createEpic();
        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);

        taskManager.deleteEpics();
        assertTrue(taskManager.getEpics().isEmpty());
    }

    @Test
    void testDeleteAllSubTasks() {
        Epic epic = createEpic();
        taskManager.createEpic(epic);

        Subtask subtask1 = createSubtask(epic.getId(), Status.NEW, LocalDateTime.now(), Duration.ofHours(1));
        Subtask subtask2 = createSubtask(epic.getId(), Status.NEW, LocalDateTime.now().plusHours(3), Duration.ofHours(2));
        taskManager.createSubTask(subtask1);
        taskManager.createSubTask(subtask2);

        taskManager.deleteSubtasks();
        assertTrue(taskManager.getSubTasks().isEmpty());
    }

    @Test
    void testGetHistory() {
        Task task1 = createTask(LocalDateTime.now(), Duration.ofMinutes(30));
        task1.setName("Task 1");
        Task task2 = createTask(LocalDateTime.now().plusHours(1), Duration.ofHours(30));
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