package core.history;

import java.util.ArrayDeque;
import java.util.Deque;

public class HistoryManager {
    
    public interface HistoryListener {
        void onHistoryChanged(boolean canUndo, boolean canRedo);
    }
    
    private final int capacity;
    private final Deque<Command> undoStack;
    private final Deque<Command> redoStack;
    private final java.util.List<HistoryListener> listeners = new java.util.ArrayList<>();

    public HistoryManager(int capacity) {
        this.capacity = capacity;
        this.undoStack = new ArrayDeque<>();
        this.redoStack = new ArrayDeque<>();
    }

    public void addListener(HistoryListener listener) {
        listeners.add(listener);
        notifyListeners();
    }

    private void notifyListeners() {
        for (HistoryListener listener : listeners) {
            listener.onHistoryChanged(canUndo(), canRedo());
        }
    }

    public void push(Command command) {
        command.execute();
        
        if (undoStack.size() == capacity) {
            undoStack.removeFirst(); // Drop the oldest command from the bottom
        }
        
        undoStack.addLast(command);
        redoStack.clear(); // Pushing a new command clears the redo history
        notifyListeners();
    }

    public void undo() {
        if (canUndo()) {
            Command command = undoStack.removeLast();
            command.undo();
            redoStack.addLast(command);
            notifyListeners();
        }
    }

    public void redo() {
        if (canRedo()) {
            Command command = redoStack.removeLast();
            command.execute();
            undoStack.addLast(command);
            notifyListeners();
        }
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}
