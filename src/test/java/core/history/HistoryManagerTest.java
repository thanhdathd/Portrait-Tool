package core.history;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new HistoryManager(10); // Max capacity of 10
    }

    @Test
    void testUndoRedoBehavior() {
        // Create simple dummy commands
        DummyCommand cmd1 = new DummyCommand(1);
        DummyCommand cmd2 = new DummyCommand(2);
        DummyCommand cmd3 = new DummyCommand(3);

        // 1. Push three commands
        historyManager.push(cmd1);
        historyManager.push(cmd2);
        historyManager.push(cmd3);

        assertTrue(cmd1.executed);
        assertTrue(cmd2.executed);
        assertTrue(cmd3.executed);
        assertFalse(historyManager.canRedo());
        assertTrue(historyManager.canUndo());

        // 2. Undo two commands
        historyManager.undo();
        assertFalse(cmd3.executed);
        assertTrue(cmd2.executed);

        historyManager.undo();
        assertFalse(cmd2.executed);
        assertTrue(cmd1.executed);
        assertTrue(historyManager.canRedo());

        // 3. Redo one command
        historyManager.redo();
        assertTrue(cmd2.executed);
        assertFalse(cmd3.executed);
        assertTrue(historyManager.canUndo());
        assertTrue(historyManager.canRedo());
        
        // 4. Push new command (should clear redo stack)
        DummyCommand cmd4 = new DummyCommand(4);
        historyManager.push(cmd4);
        assertTrue(cmd4.executed);
        assertFalse(historyManager.canRedo());
        
        // Redo stack is cleared, so trying to redo should do nothing
        historyManager.redo();
        assertFalse(cmd3.executed);
    }
    
    @Test
    void testCapacityLimit() {
        historyManager = new HistoryManager(2);
        DummyCommand cmd1 = new DummyCommand(1);
        DummyCommand cmd2 = new DummyCommand(2);
        DummyCommand cmd3 = new DummyCommand(3);
        
        historyManager.push(cmd1);
        historyManager.push(cmd2);
        historyManager.push(cmd3);
        
        // It should have dropped cmd1
        historyManager.undo(); // undoes cmd3
        historyManager.undo(); // undoes cmd2
        assertFalse(historyManager.canUndo()); // cannot undo cmd1 because it dropped off
    }

    // A simple command implementation for testing
    static class DummyCommand implements Command {
        boolean executed = false;
        int id;

        DummyCommand(int id) {
            this.id = id;
        }

        @Override
        public void execute() {
            executed = true;
        }

        @Override
        public void undo() {
            executed = false;
        }
    }
}
