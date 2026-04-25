package core.history;

import java.util.ArrayDeque;
import java.util.Deque;

public class HistoryManager {
    
    private final int capacity;
    private final Deque<Command> undoStack;
    private final Deque<Command> redoStack;

    public HistoryManager(int capacity) {
        this.capacity = capacity;
        this.undoStack = new ArrayDeque<>();
        this.redoStack = new ArrayDeque<>();
    }

    public void push(Command command) {
        command.execute();
        
        if (undoStack.size() == capacity) {
            undoStack.removeFirst(); // Drop the oldest command from the bottom
        }
        
        undoStack.addLast(command);
        redoStack.clear(); // Pushing a new command clears the redo history
    }

    public void undo() {
        if (canUndo()) {
            Command command = undoStack.removeLast();
            command.undo();
            redoStack.addLast(command);
        }
    }

    public void redo() {
        if (canRedo()) {
            Command command = redoStack.removeLast();
            command.execute();
            undoStack.addLast(command);
        }
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}
