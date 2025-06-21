import model.SubTask;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SubTaskTest {
    @Test
    public void testSubTask() {
        SubTask subTask = new SubTask("Подзадача 1", "Описание подзадачи 1", 1);
        Assertions.assertEquals("Подзадача 1", subTask.getName());
        Assertions.assertEquals("Описание подзадачи 1", subTask.getDescription());
        Assertions.assertEquals(1, subTask.getEpicId());
    }

    @Test
    public void testSetAndGetEpicId() {
        SubTask subTask = new SubTask("Подзадача 1", "Описание подзадачи 1", 1);
        subTask.setEpicId(5);
        Assertions.assertEquals(5, subTask.getEpicId());
    }
}