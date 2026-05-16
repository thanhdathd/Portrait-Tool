package tools;

import core.state.AppState;

public class ToolManager {
    public HandTool handTool;
    public StickTool stickTool;
    public P2PTool p2pTool;
    public GridTool gridTool;
    public SelectTool selectTool;

    private static ToolManager toolManager = null;

    private ToolManager() {
        this.handTool = new HandTool();
        this.stickTool = new StickTool();
        this.p2pTool = new P2PTool();
        this.gridTool = new GridTool();
        this.selectTool = new SelectTool();
    }

    public static ToolManager initializeTools() {
        if (toolManager == null) {
            toolManager = new ToolManager();
        }
        return toolManager;
    }
}
