package tests;

import manager.InMemoryTaskManager;
import model.Epic;
import model.SubTask;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class EpicTest {
    @Test
    public void testEpic() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        Assertions.assertEquals("Эпик 1", epic.getName());
        Assertions.assertEquals("Описание эпика 1", epic.getDescription());
    }

    @Test
    public void testGetSubTasksIds() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        SubTask subTask1 = new SubTask("Подзадача 1", "Описание подзадачи 1", epic.getId());
        SubTask subTask2 = new SubTask("Подзадача 2", "Описание подзадачи 2", epic.getId());
        subTask2.setId(5);
        epic.addSubTaskId(subTask1.getId());
        epic.addSubTaskId(subTask2.getId());
        ArrayList<Integer> expectedSubTaskIds = new ArrayList<>();
        expectedSubTaskIds.add(subTask1.getId());
        expectedSubTaskIds.add(subTask2.getId());
        Assertions.assertEquals(expectedSubTaskIds, epic.getSubTaskIds());
    }

    @Test
    public void addSubTasksId() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        SubTask subTask = new SubTask("Подзадача 1", "Описание подзадачи 1", epic.getId());
        epic.addSubTaskId(subTask.getId());
        Assertions.assertTrue(epic.getSubTaskIds().contains(subTask.getId()));
    }

    @Test
    public void testRemoveSubTaskId() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        SubTask subTask = new SubTask("Подзадача 1", "Описание подзадачи 1", epic.getId());
        epic.addSubTaskId(subTask.getId());
        Assertions.assertTrue(epic.getSubTaskIds().contains(subTask.getId()));
        epic.removeSubTaskId(subTask.getId());
        Assertions.assertFalse(epic.getSubTaskIds().contains(subTask.getId()));
    }
}