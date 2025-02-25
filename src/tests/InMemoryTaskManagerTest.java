package tests;

import static enums.Status.*;
import manager.InMemoryTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import model.Epic;
import model.SubTask;
import model.Task;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

public class InMemoryTaskManagerTest {
    private InMemoryTaskManager taskManager;

    @BeforeEach
    public void BeforeEach() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    public void testCreateTask() {
        Task task = new Task("Задача 1", "Описание задачи 1", NEW);
        taskManager.createTask(task);
        assertEquals(1, taskManager.getAllTasks().size());
        assertEquals(task, taskManager.getTaskById(1));
    }

    @Test
    public void testCreateEpic() {
        Epic epic = new Epic("Эпик 1", "Описание для эпика 1");
        taskManager.createEpic(epic);
        assertEquals(1, taskManager.getAllEpics().size());
        assertEquals(epic, taskManager.getEpicById(1));
    }

    @Test
    public void testCreateSubTask() {
        Epic epic = new Epic("Эпик 1", "Описание для эпика 1");
        taskManager.createEpic(epic);
        SubTask subTask = new SubTask("Подзадача 1", "Описание для подзадачи 1", epic.getId());
        taskManager.createSubTask(subTask);
        assertEquals(1, taskManager.getAllSubTasks().size());
        assertEquals(subTask, taskManager.getSubTaskById(2));
    }

    @Test
    public void testDeleteTask() {
        Task task = new Task("Task 1", "Description of Task 1");
        taskManager.createTask(task);
        taskManager.deleteTask(1);
        assertEquals(0, taskManager.getAllTasks().size());
    }

    @Test
    public void testDeleteEpic() {
        Epic epic = new Epic("Epic 1", "Description of Epic 1");
        taskManager.createEpic(epic);
        taskManager.deleteEpic(1);
        assertEquals(0, taskManager.getAllEpics().size());
    }

    @Test
    public void testDeleteSubTask() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.createEpic(epic);
        SubTask subTask = new SubTask("Подзадача 1", "Описание для подзадачи 1", epic.getId());
        taskManager.createSubTask(subTask);
        taskManager.deleteSubTask(2);
        assertEquals(0, taskManager.getAllSubTasks().size());
    }

    @Test
    public void testUpdateTask() {
        Task task = new Task("Задача 1", "Описание задачи 1");
        taskManager.createTask(task);
        task.setName("Измененная задача");
        taskManager.updateTask(task);
        assertEquals("Измененная задача", taskManager.getTaskById(1).getName());
    }

    @Test
    public void testGetSubTasksOfEpic() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.createEpic(epic);
        SubTask subTask1 = new SubTask("Подзадача 1", "Описание подзадачи 1", epic.getId());
        SubTask subTask2 = new SubTask("Подзадача 2", "Описание подзадачи 2", epic.getId());
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        ArrayList<SubTask> subTasks = taskManager.getSubTasksOfEpic(epic.getId());
        assertEquals(2, subTasks.size());
    }
}