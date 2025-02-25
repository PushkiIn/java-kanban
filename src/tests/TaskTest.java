package tests;

import model.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TaskTest {
    @Test
    public void testTask() {
        Task task = new Task("Задача 1", "Описание задачи 1");
        Assertions.assertEquals("Задача 1", task.getName());
        Assertions.assertEquals("Описание задачи 1", task.getDescription());
    }

    @Test
    public void testSetId() {
        Task task = new Task("Задача 1", "Описание задачи 1");
        task.setId(5);
        Assertions.assertEquals(5, task.getId());
        task.setId(10);
        Assertions.assertEquals(5, task.getId());
    }

    @Test
    public void testImmutabilityId() {
        Task task = new Task("Задача 1", "Описание задачи 1");
        task.setId(5);
        Assertions.assertEquals(5, task.getId());
        task.setId(10);
        Assertions.assertNotEquals(10, task.getId());
        Assertions.assertEquals(5, task.getId());
    }
}
