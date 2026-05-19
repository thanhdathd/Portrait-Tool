package core.history;

import core.state.CanvasState;
import core.state.CommandData;
import ui.canvas.ImageCanvas;
import userpackage.SLine;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class BatchLineCommand implements Command {
    public enum Action { EDIT, DELETE }

    private final CanvasState canvasState;
    private final ImageCanvas canvas;
    private final Action action;
    private final List<LineStatePair> linePairs;

    public static class LineStatePair {
        public final SLine line;
        public final SLine oldState;
        public final SLine newState;

        public LineStatePair(SLine line, SLine oldState, SLine newState) {
            this.line = line;
            this.oldState = oldState;
            this.newState = newState;
        }
    }

    public BatchLineCommand(CanvasState canvasState, ImageCanvas canvas, Action action, List<LineStatePair> linePairs) {
        this.canvasState = canvasState;
        this.canvas = canvas;
        this.action = action;
        this.linePairs = linePairs != null ? linePairs : new ArrayList<>();
    }

    @Override
    public void execute() {
        if (action == Action.EDIT) {
            for (LineStatePair pair : linePairs) {
                pair.line.startPoint.setLocation(pair.newState.startPoint);
                pair.line.endPoint.setLocation(pair.newState.endPoint);
                pair.line.strokeWidth = pair.newState.strokeWidth;
                pair.line.strokeColor = pair.newState.strokeColor;
            }
        } else if (action == Action.DELETE) {
            for (LineStatePair pair : linePairs) {
                canvasState.getLines().remove(pair.line);
                canvasState.removeFromSelection(pair.line);
            }
        }
        canvas.repaint();
    }

    @Override
    public void undo() {
        if (action == Action.EDIT) {
            for (LineStatePair pair : linePairs) {
                pair.line.startPoint.setLocation(pair.oldState.startPoint);
                pair.line.endPoint.setLocation(pair.oldState.endPoint);
                pair.line.strokeWidth = pair.oldState.strokeWidth;
                pair.line.strokeColor = pair.oldState.strokeColor;
            }
        } else if (action == Action.DELETE) {
            HashSet<SLine> selectSet = new HashSet<>(canvasState.getSelectedLines());
            for (LineStatePair pair : linePairs) {
                if (!canvasState.getLines().contains(pair.line)) {
                    canvasState.getLines().add(pair.line);
                }
                selectSet.add(pair.line);
            }
            canvasState.setSelectedLines(selectSet);
        }
        canvas.repaint();
    }

    @Override
    public CommandData capture() {
        return null; // Local command history, not autosaved
    }

    @Override
    public long getMemorySize() {
        return 256 + (linePairs.size() * 128);
    }
}
