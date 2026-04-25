package tools;

import core.state.AppState;
import ui.canvas.ImageCanvas;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

/**
 * Strategy interface for all interactive tools on the canvas.
 */
public interface Tool {
    
    /**
     * Called when the mouse is pressed on the canvas.
     */
    void onMousePressed(MouseEvent e, AppState appState, ImageCanvas canvas);
    
    /**
     * Called when the mouse is released on the canvas.
     */
    void onMouseReleased(MouseEvent e, AppState appState, ImageCanvas canvas);
    
    /**
     * Called when the mouse is dragged on the canvas.
     */
    void onMouseDragged(MouseEvent e, AppState appState, ImageCanvas canvas);
    
    /**
     * Called when the canvas repaints to allow the tool to draw custom UI or previews.
     */
    void onPaint(Graphics2D g2d, AppState appState, ImageCanvas canvas);
}
