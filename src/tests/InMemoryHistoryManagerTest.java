package tests;

import manager.HistoryManager;
import manager.InMemoryHistoryManager;
import model.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;
    private Task task1;

    @BeforeEach
    public void beforeEach() {
        historyManager = new InMemoryHistoryManager();
        task1 = new Task("Задача 1", "описание");
        task1.setId(1);
    }

    @Test
    public void testAddTask() {
        historyManager.add(task1);
        Assertions.assertEquals(1, historyManager.getHistory().size());
        Assertions.assertEquals(task1, historyManager.getHistory().get(0));
    }

    @Test
    public void testRemoveTaskFromHistory() {
        historyManager.add(task1);
        historyManager.remove(1);
        Assertions.assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    public void testGetHistory() {
        Task task2 = new Task("Task 2", "Description 2");
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();
        Assertions.assertEquals(2, history.size());
        Assertions.assertTrue(history.contains(task1));
        Assertions.assertTrue(history.contains(task2));
    }
}