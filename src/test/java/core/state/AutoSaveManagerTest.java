package core.state;

import core.history.Command;
import core.history.StickCommand;
import org.junit.jupiter.api.Test;
import ui.canvas.ImageCanvas;
import core.state.SPoint;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AutoSaveManagerTest {

    @Test
    public void testReconstructStack() {
        // Use real AppState and CanvasState to avoid Mockito issues with Java 23
        AppState appState = new AppState();
        CanvasState canvasState = appState.getCanvasState();
        
        // Mock dependencies
        ImageCanvas mockCanvas = mock(ImageCanvas.class);
        RecoveryUI mockUI = mock(RecoveryUI.class);
        
        when(mockUI.getAppState()).thenReturn(appState);
        when(mockUI.getCanvas()).thenReturn(mockCanvas);
        when(mockCanvas.getAppState()).thenReturn(appState);
        
        // Setup mock CommandData list
        List<CommandData> list = new ArrayList<>();
        CommandData d1 = new CommandData();
        d1.type = CommandData.CommandType.ADD_POINT;
        d1.point = new SPoint(1, 10, 20);
        list.add(d1);
        
        BufferedImage startImg = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        
        // Use AutoSaveManager
        AutoSaveManager manager = new AutoSaveManager(mockCanvas);
        
        // Execution
        Deque<Command> stack = manager.reconstructStack(mockUI, list, startImg, false);
        
        // Verification
        assertNotNull(stack);
        assertEquals(1, stack.size());
        assertTrue(stack.peek() instanceof StickCommand);
        
        // Verify no sticky points were added because execute=false
        assertEquals(0, canvasState.getStickyPoints().size());
        
        verify(mockUI, atLeastOnce()).getAppState();
    }
}
