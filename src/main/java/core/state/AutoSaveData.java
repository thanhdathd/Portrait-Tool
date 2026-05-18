package core.state;

import userpackage.SLine;
import userpackage.SPoint;
import java.awt.Color;
import java.util.List;

/**
 * Data Transfer Object for serializing the application state to JSON.
 */
public class AutoSaveData {
    public String version = "2.0";
    public long timestamp;
    public String imagePath;
    public String shadowPath;
    public String originalHash;
    
    // Settings
    public float scale;
    public float gridSize;
    public boolean gridInCm;
    public Color brushColor;

    // History Data
    public List<CommandData> undoStack;
    public List<CommandData> redoStack;

    // Project Initial State (only populated if active file is a .pdw project)
    public List<SPoint> initialPoints;
    public List<SPoint> initialGrids;
    public List<SLine> initialLines;

    public AutoSaveData() {
        this.timestamp = System.currentTimeMillis();
    }
}
