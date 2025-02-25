package tests;

import manager.InMemoryHistoryManager;
import manager.InMemoryTaskManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import util.Managers;


public class ManagersTest {
    @Test
    public void testGetDefault() {
        Assertions.assertNotNull(Managers.getDefault());
        Assertions.assertTrue(Managers.getDefault() instanceof InMemoryTaskManager);
    }

    @Test
    public void testGetDefaultHistory() {
        Assertions.assertNotNull(Managers.getDefaultHistory());
        Assertions.assertTrue(Managers.getDefaultHistory() instanceof InMemoryHistoryManager);
    }
}
