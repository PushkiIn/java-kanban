package tests;

import interfaces.HistoryManager;
import manager.InMemoryHistoryManager;
import model.Task;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static enums.Status.IN_PROGRESS;
import static enums.Status.NEW;

public class InMemoryHistoryManagerTest {

    HistoryManager historyManager;

    @BeforeEach
    public void BeforeEach() {
        historyManager = new InMemoryHistoryManager();
    }
    
    @Test
    public void testAdd() {
        Task task = new Task("Задача 1", "описание задачи 1", NEW);
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();
        Assertions.assertTrue(history.contains(task));
    }

    @Test
    public void testGetHistory() {
        Task task1 = new Task("Задача 1", "описание задачи 1", NEW);
        Task task2 = new Task("Задача 2", "описание задачи 2", IN_PROGRESS);
        List<Task> expectedHistory = new ArrayList<>();
        expectedHistory.add(task1);
        expectedHistory.add(task2);
        historyManager.add(task1);
        historyManager.add(task2);
        Assertions.assertEquals(expectedHistory, historyManager.getHistory());
    }
}
