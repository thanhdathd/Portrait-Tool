package core.state;

import java.awt.Color;
import java.util.List;

/**
 * Data Transfer Object for serializing the project state to a .pdw file.
 * Unlike AutoSaveData, this only contains the static state without history.
 */
public class ProjectData {
    public String version = "1.0";
    public long timestamp;
    
    // Settings snapshot
    public float scale;
    public float gridSize;
    public boolean gridInCm;
    public Color brushColor;

    // Static data points
    public List<SPoint> stickyPoints;
    public List<SPoint> grids;
    public List<SLine> lines;

    public ProjectData() {
        this.timestamp = System.currentTimeMillis();
    }
}
