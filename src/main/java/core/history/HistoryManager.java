package core.history;

import core.state.CommandData;
import utils.Utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class HistoryManager {

    private long maxMemoryBudget;
    private long currentMemoryUsage = 0;
    private final Deque<Command> undoStack;
    private final Deque<Command> redoStack;
    
    // Metadata stacks for persistent recovery
    private final Deque<CommandData> persistentUndoStack;
    private final Deque<CommandData> persistentRedoStack;

    private final List<HistoryListener> listeners = new ArrayList<>();

    // Save state tracking
    private Command savedCommand = null;
    private boolean isSavedStateDropped = false;

    public HistoryManager(long maxMemoryBudget) {
        this.maxMemoryBudget = maxMemoryBudget;
        this.undoStack = new ArrayDeque<>();
        this.redoStack = new ArrayDeque<>();
        this.persistentUndoStack = new ArrayDeque<>();
        this.persistentRedoStack = new ArrayDeque<>();
    }

    public void setMemoryBudget(long bytes) {
        this.maxMemoryBudget = bytes;
        checkBudget();
        notifyListeners();
    }

    public void addListener(HistoryListener listener) {
        listeners.add(listener);
        notifyListeners();
    }

    private void notifyListeners() {
        for (HistoryListener listener : listeners) {
            listener.onHistoryChanged(canUndo(), canRedo(), isModified());
        }
    }

    public void markAsClean() {
        savedCommand = undoStack.peekLast();
        isSavedStateDropped = false;
        notifyListeners();
    }

    public boolean isModified() {
        if (isSavedStateDropped) {
            return true;
        }
        Command currentCommand = undoStack.peekLast();
        return currentCommand != savedCommand;
    }

    public void push(Command command) {
        command.execute();
        
        long cmdSize = command.getMemorySize();
        currentMemoryUsage += cmdSize;
        
        undoStack.addLast(command);
        persistentUndoStack.addLast(command.capture());
        
        redoStack.clear(); 
        persistentRedoStack.clear();
        
        checkBudget();
        
        System.out.println("History add: " + command
                + " (Size: " + Utils.bytesToHumanReadable(cmdSize)
                + " bytes, Total: " + Utils.bytesToHumanReadable(currentMemoryUsage) + ")");
        notifyListeners();
    }

    private void checkBudget() {
        while (currentMemoryUsage > maxMemoryBudget && undoStack.size() > 0) {
            Command droppedCommand = undoStack.removeFirst();
            currentMemoryUsage -= droppedCommand.getMemorySize();
            System.out.println("drop "+droppedCommand+" save "+
                    Utils.bytesToHumanReadable(droppedCommand.getMemorySize())+" bytes");
            
            if (droppedCommand == savedCommand) {
                isSavedStateDropped = true;
            }
        }
    }

    public void undo() {
        if (canUndo()) {
            Command command = undoStack.removeLast();
            command.undo();
            redoStack.addLast(command);
            
            // Sync metadata
            if (!persistentUndoStack.isEmpty()) {
                persistentRedoStack.addLast(persistentUndoStack.removeLast());
            }
            
            notifyListeners();
        }
    }

    public void redo() {
        if (canRedo()) {
            Command command = redoStack.removeLast();
            command.execute();
            undoStack.addLast(command);
            
            // Sync metadata
            if (!persistentRedoStack.isEmpty()) {
                persistentUndoStack.addLast(persistentRedoStack.removeLast());
            }
            
            notifyListeners();
        }
    }

    public void clearAll() {
        undoStack.clear();
        redoStack.clear();
        persistentUndoStack.clear();
        persistentRedoStack.clear();
        currentMemoryUsage = 0;
        notifyListeners();
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public Deque<Command> getUndoStack() {
        return undoStack;
    }

    public Deque<Command> getRedoStack() {
        return redoStack;
    }

    public Deque<CommandData> getPersistentUndoStack() {
        return persistentUndoStack;
    }

    public Deque<CommandData> getPersistentRedoStack() {
        return persistentRedoStack;
    }

    public void reconstructStacks(Deque<Command> undo, Deque<Command> redo, 
                                  Deque<CommandData> persistentUndo, Deque<CommandData> persistentRedo) {
        this.undoStack.clear();
        this.undoStack.addAll(undo);
        this.redoStack.clear();
        this.redoStack.addAll(redo);
        
        this.persistentUndoStack.clear();
        this.persistentUndoStack.addAll(persistentUndo);
        this.persistentRedoStack.clear();
        this.persistentRedoStack.addAll(persistentRedo);

        // Re-calculate memory usage
        currentMemoryUsage = 0;
        for (Command c : undoStack) currentMemoryUsage += c.getMemorySize();
        
        checkBudget();
        notifyListeners();
    }

    public interface HistoryListener {
        void onHistoryChanged(boolean canUndo, boolean canRedo, boolean isModified);
    }
}
