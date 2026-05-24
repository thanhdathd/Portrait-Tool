package core.history;

import core.state.CanvasState;
import core.state.CommandData;
import ui.canvas.ImageCanvas;
import core.state.SPoint;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Batch command for multi-point operations (delete all, change color for all).
 * Supports Undo/Redo and auto-save capture.
 */
public class BatchStickCommand implements Command {

    public enum BatchAction {
        DELETE_ALL,
        EDIT_COLOR_ALL
    }

    private final BatchAction action;
    private final CanvasState canvasState;
    private final ImageCanvas canvas;
    private final List<SPoint> targets;     // live refs in CanvasState
    private final List<SPoint> snapshots;   // copies at time of push (for undo)
    private final Color newColor;           // only for EDIT_COLOR_ALL

    /**
     * Constructor for DELETE_ALL.
     */
    public BatchStickCommand(CanvasState canvasState, ImageCanvas canvas,
                             List<SPoint> targets) {
        this(canvasState, canvas, targets, BatchAction.DELETE_ALL, null);
    }

    /**
     * Constructor for EDIT_COLOR_ALL.
     */
    public BatchStickCommand(CanvasState canvasState, ImageCanvas canvas,
                             List<SPoint> targets, Color newColor) {
        this(canvasState, canvas, targets, BatchAction.EDIT_COLOR_ALL, newColor);
    }

    private BatchStickCommand(CanvasState canvasState, ImageCanvas canvas,
                              List<SPoint> targets, BatchAction action, Color newColor) {
        this.canvasState = canvasState;
        this.canvas = canvas;
        this.action = action;
        this.newColor = newColor;
        this.targets = new ArrayList<>(targets);
        // Snapshot all points at creation time
        this.snapshots = new ArrayList<>();
        for (SPoint p : targets) {
            snapshots.add(p.copy());
        }
    }

    @Override
    public void execute() {
        switch (action) {
            case DELETE_ALL:
                for (SPoint p : targets) {
                    canvasState.removeStickyPoint(p);
                }
                // Clear any selection that references deleted points
                canvasState.clearPointSelection();
                break;
            case EDIT_COLOR_ALL:
                for (SPoint p : targets) {
                    p.c = newColor;
                }
                canvasState.notifyPointSelectionChanged();
                break;
        }
        if (canvas != null) canvas.repaint();
    }

    @Override
    public void undo() {
        switch (action) {
            case DELETE_ALL:
                for (SPoint p : targets) {
                    canvasState.addStickyPoint(p);
                }
                // Re-select all restored points
                canvasState.setSelectedPoints(new java.util.LinkedHashSet<>(targets));
                break;
            case EDIT_COLOR_ALL:
                for (int i = 0; i < targets.size(); i++) {
                    targets.get(i).c = snapshots.get(i).c;
                }
                canvasState.notifyPointSelectionChanged();
                break;
        }
        if (canvas != null) canvas.repaint();
    }

    @Override
    public CommandData capture() {
        CommandData cmd = new CommandData();
        switch (action) {
            case DELETE_ALL:
                cmd.type = CommandData.CommandType.BATCH_DELETE_POINTS;
                cmd.points = new ArrayList<>();
                for (SPoint p : snapshots) cmd.points.add(p.copy());
                break;
            case EDIT_COLOR_ALL:
                cmd.type = CommandData.CommandType.BATCH_EDIT_POINTS_COLOR;
                cmd.batchColor = newColor;
                cmd.points = new ArrayList<>();
                cmd.oldColors = new ArrayList<>();
                for (int i = 0; i < targets.size(); i++) {
                    cmd.points.add(snapshots.get(i).copy());
                    cmd.oldColors.add(snapshots.get(i).c);
                }
                break;
        }
        return cmd;
    }

    @Override
    public long getMemorySize() {
        return 256L * targets.size();
    }

    @Override
    public String toString() {
        return "BatchStickCommand[" + action + " x" + targets.size() + "]";
    }
}
