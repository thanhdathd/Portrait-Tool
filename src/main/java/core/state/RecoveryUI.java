package core.state;

import ui.canvas.ImageCanvas;
import java.awt.image.BufferedImage;

/**
 * Interface to allow AutoSaveManager to interact with the UI during recovery
 * without being coupled to MainFrame.
 */
public interface RecoveryUI {
    /** Called when recovery starts. */
    void onRecoveryStarted();
    
    /** Called to update recovery progress. */
    void updateProgress(int percent, String message);
    
    /** Called when recovery completes successfully. */
    void onRecoveryFinished(String finalTitle);
    
    /** Called when an error occurs during recovery. */
    void onRecoveryError(String message);
    
    /** Updates the image on the canvas. */
    void setCanvasImage(BufferedImage img);
    
    /** Gets the AppState for command reconstruction. */
    AppState getAppState();
    
    /** Gets the ImageCanvas for command reconstruction. */
    ImageCanvas getCanvas();
}
