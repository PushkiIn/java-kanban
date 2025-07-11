import manager.HistoryManager;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Managers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    void setup() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void testEmptyHistory() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }

    @Test
    void testAddAndNoDuplicates() {
        Task task = new Task("test", "Desc");
        task.setId(1);

        historyManager.add(task);
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
    }

    @Test
    void testRemoveFromBeginningMiddleEnd() {
        Task task1 = new Task("test1", "description1");
        task1.setId(1);
        Task task2 = new Task("test2", "description2");
        task2.setId(2);
        Task task3 = new Task("test3", "description13");
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(1);
        assertEquals(List.of(task2, task3), historyManager.getHistory());

        historyManager.remove(2);
        assertEquals(List.of(task3), historyManager.getHistory());

        historyManager.remove(3);
        assertTrue(historyManager.getHistory().isEmpty());
    }
}