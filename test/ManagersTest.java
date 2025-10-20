import manage.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ManagersTest {
    @Test
    public void getDefaultReturnsCorrectManagerType() {
        TaskManager defaultManager = Managers.getDefault();
        assertTrue(defaultManager instanceof InMemoryTaskManager);
    }

    @Test
    public void getDefaultHistoryReturnsCorrectHistoryType() {
        HistoryManager defaultHistory = Managers.getDefaultHistory();
        assertTrue(defaultHistory instanceof InMemoryHistoryManager);
    }
}
