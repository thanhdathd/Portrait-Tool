package core.history;

import java.util.ArrayDeque;
import java.util.Deque;

public class HistoryManager {
    
    public interface HistoryListener {
        void onHistoryChanged(boolean canUndo, boolean canRedo, boolean isModified);
    }
    
    private final int capacity;
    private final Deque<Command> undoStack;
    private final Deque<Command> redoStack;
    private final java.util.List<HistoryListener> listeners = new java.util.ArrayList<>();

    // --- Các biến mới để theo dõi trạng thái Save ---
    private Command savedCommand = null;
    private boolean isSavedStateDropped = false;

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
            listener.onHistoryChanged(canUndo(), canRedo(), isModified());
        }
    }

    // GỌI HÀM NÀY SAU KHI USER LƯU ẢNH THÀNH CÔNG
    public void markAsSaved() {
        savedCommand = undoStack.peekLast(); // Trạng thái hiện tại chính là trạng thái đã lưu
        isSavedStateDropped = false;         // Reset lại cờ
        notifyListeners();
    }

    public boolean isModified() {
        // Nếu cái command chứa trạng thái save đã bị xóa khỏi bộ nhớ do quá capacity
        // thì chắc chắn user không thể undo về trạng thái đó được nữa -> Luôn modified.
        if (isSavedStateDropped) {
            return true;
        }

        // Nếu đỉnh của stack khác với command lúc save, nghĩa là có sự thay đổi
        Command currentCommand = undoStack.peekLast();
        return currentCommand != savedCommand;
    }

    public void push(Command command) {
        command.execute();
        
        if (undoStack.size() == capacity) {
            Command droppedCommand = undoStack.removeFirst(); // Drop the oldest command from the bottom
            // Kiểm tra xem command bị drop có phải là mốc Save không
            if (droppedCommand == savedCommand) {
                isSavedStateDropped = true;
            }
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
