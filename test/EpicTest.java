import model.Epic;
import model.Subtask;
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
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", epic.getId());
        subtask2.setId(5);
        epic.addSubTaskById(subtask1.getId());
        epic.addSubTaskById(subtask2.getId());
        ArrayList<Integer> expectedSubTaskIds = new ArrayList<>();
        expectedSubTaskIds.add(subtask1.getId());
        expectedSubTaskIds.add(subtask2.getId());
        Assertions.assertEquals(expectedSubTaskIds, epic.getSubTasks());
    }

    @Test
    public void addSubTasksId() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        Subtask subTask = new Subtask("Подзадача 1", "Описание подзадачи 1", epic.getId());
        epic.addSubTaskById(subTask.getId());
        Assertions.assertTrue(epic.getSubTasks().contains(subTask.getId()));
    }

    @Test
    public void testRemoveSubTaskId() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        Subtask subTask = new Subtask("Подзадача 1", "Описание подзадачи 1", epic.getId());
        epic.addSubTaskById(subTask.getId());
        Assertions.assertTrue(epic.getSubTasks().contains(subTask.getId()));
        epic.removeSubTaskById(subTask.getId());
        Assertions.assertFalse(epic.getSubTasks().contains(subTask.getId()));
    }
}