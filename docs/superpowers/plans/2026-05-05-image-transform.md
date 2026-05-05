# Image Transforms Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement Image rotation (90 CW, 90 CCW, 180) and flipping (Horizontal, Vertical) from the Image menu, properly transforming the image and all markers.

**Architecture:** A new `core.image.ImageTransformUtils` will handle `BufferedImage` transforms via `AffineTransform`. A new `core.history.TransformCommand` will apply these changes while also transforming the coordinates and directions of `CanvasState` markers (`SPoint` and Grids). We will hook these into `MainFrame`'s Image menu.

**Tech Stack:** Java 17, Swing, JUnit 5, Java2D.

---

### Task 1: Create Transform Utilities

**Files:**
- Create: `src/main/java/core/image/ImageTransformUtils.java`
- Create: `src/test/java/core/image/ImageTransformUtilsTest.java`

- [ ] **Step 1: Write the failing test**

```java
package core.image;

import org.junit.jupiter.api.Test;
import java.awt.image.BufferedImage;
import static org.junit.jupiter.api.Assertions.*;

class ImageTransformUtilsTest {
    @Test
    void testTransforms() {
        BufferedImage img = new BufferedImage(100, 200, BufferedImage.TYPE_INT_ARGB);
        
        BufferedImage rot90cw = ImageTransformUtils.transform(img, ImageTransformUtils.TransformType.ROTATE_90_CW);
        assertEquals(200, rot90cw.getWidth());
        assertEquals(100, rot90cw.getHeight());

        BufferedImage flipH = ImageTransformUtils.transform(img, ImageTransformUtils.TransformType.FLIP_H);
        assertEquals(100, flipH.getWidth());
        assertEquals(200, flipH.getHeight());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ImageTransformUtilsTest`
Expected: FAIL with compilation error

- [ ] **Step 3: Write minimal implementation**

```java
package core.image;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class ImageTransformUtils {

    public enum TransformType {
        ROTATE_90_CW, ROTATE_90_CCW, ROTATE_180, FLIP_H, FLIP_V
    }

    public static BufferedImage transform(BufferedImage src, TransformType type) {
        int w = src.getWidth();
        int h = src.getHeight();
        AffineTransform at = new AffineTransform();

        switch (type) {
            case ROTATE_90_CW:
                at.translate(h, 0);
                at.rotate(Math.PI / 2);
                break;
            case ROTATE_90_CCW:
                at.translate(0, w);
                at.rotate(-Math.PI / 2);
                break;
            case ROTATE_180:
                at.translate(w, h);
                at.rotate(Math.PI);
                break;
            case FLIP_H:
                at.translate(w, 0);
                at.scale(-1, 1);
                break;
            case FLIP_V:
                at.translate(0, h);
                at.scale(1, -1);
                break;
        }

        AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        
        BufferedImage dest;
        if (type == TransformType.ROTATE_90_CW || type == TransformType.ROTATE_90_CCW) {
            dest = new BufferedImage(h, w, src.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : src.getType());
        } else {
            dest = new BufferedImage(w, h, src.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : src.getType());
        }

        return op.filter(src, dest);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=ImageTransformUtilsTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/core/image/ImageTransformUtils.java src/test/java/core/image/ImageTransformUtilsTest.java
git commit -m "feat: add ImageTransformUtils"
```

### Task 2: Create TransformCommand

**Files:**
- Create: `src/main/java/core/history/TransformCommand.java`

- [ ] **Step 1: Write implementation**

```java
package core.history;

import core.image.ImageTransformUtils.TransformType;
import core.state.CanvasState;
import ui.canvas.ImageCanvas;
import userpackage.SPoint;
import user.Enum.Direction;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class TransformCommand implements Command {
    private final ImageCanvas canvas;
    private final CanvasState canvasState;
    private final BufferedImage oldImage;
    private final BufferedImage newImage;
    private final List<SPoint> oldPoints;
    private final List<SPoint> oldGrids;
    private final TransformType type;

    public TransformCommand(ImageCanvas canvas, CanvasState canvasState, BufferedImage oldImage, BufferedImage newImage, TransformType type) {
        this.canvas = canvas;
        this.canvasState = canvasState;
        this.oldImage = oldImage;
        this.newImage = newImage;
        this.type = type;
        this.oldPoints = clonePoints(canvasState.getStickyPoints());
        this.oldGrids = clonePoints(canvasState.getGrids());
    }

    private List<SPoint> clonePoints(List<SPoint> points) {
        List<SPoint> cloned = new ArrayList<>();
        for (SPoint p : points) {
            SPoint newP = new SPoint(p.id, p.X, p.Y, p.c, p.dr);
            newP.isCustomPlacement = p.isCustomPlacement;
            newP.customGap = p.customGap;
            newP.customAngle = p.customAngle;
            cloned.add(newP);
        }
        return cloned;
    }

    @Override
    public void execute() {
        canvas.setBackgroundImage(newImage);
        transformPoints(canvasState.getStickyPoints(), oldImage.getWidth(), oldImage.getHeight());
        transformPoints(canvasState.getGrids(), oldImage.getWidth(), oldImage.getHeight());
    }

    @Override
    public void undo() {
        canvas.setBackgroundImage(oldImage);
        canvasState.getStickyPoints().clear();
        canvasState.getStickyPoints().addAll(oldPoints);
        canvasState.getGrids().clear();
        canvasState.getGrids().addAll(oldGrids);
    }
    
    private void transformPoints(List<SPoint> points, int w, int h) {
        for (SPoint p : points) {
            int x = p.X;
            int y = p.Y;
            switch (type) {
                case ROTATE_90_CW:
                    p.X = h - 1 - y;
                    p.Y = x;
                    p.dr = rotateCW(p.dr);
                    p.customAngle = (p.customAngle + 270) % 360;
                    break;
                case ROTATE_90_CCW:
                    p.X = y;
                    p.Y = w - 1 - x;
                    p.dr = rotateCCW(p.dr);
                    p.customAngle = (p.customAngle + 90) % 360;
                    break;
                case ROTATE_180:
                    p.X = w - 1 - x;
                    p.Y = h - 1 - y;
                    p.dr = rotateCW(rotateCW(p.dr));
                    p.customAngle = (p.customAngle + 180) % 360;
                    break;
                case FLIP_H:
                    p.X = w - 1 - x;
                    if (p.dr == Direction.EAST) p.dr = Direction.WEST;
                    else if (p.dr == Direction.WEST) p.dr = Direction.EAST;
                    p.customAngle = (180 - p.customAngle + 360) % 360;
                    break;
                case FLIP_V:
                    p.Y = h - 1 - y;
                    if (p.dr == Direction.NORTH) p.dr = Direction.SOUTH;
                    else if (p.dr == Direction.SOUTH) p.dr = Direction.NORTH;
                    p.customAngle = (360 - p.customAngle) % 360;
                    break;
            }
        }
    }

    private Direction rotateCW(Direction d) {
        if (d == Direction.NORTH) return Direction.EAST;
        if (d == Direction.EAST) return Direction.SOUTH;
        if (d == Direction.SOUTH) return Direction.WEST;
        return Direction.NORTH;
    }

    private Direction rotateCCW(Direction d) {
        if (d == Direction.NORTH) return Direction.WEST;
        if (d == Direction.WEST) return Direction.SOUTH;
        if (d == Direction.SOUTH) return Direction.EAST;
        return Direction.NORTH;
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/core/history/TransformCommand.java
git commit -m "feat: add TransformCommand for history tracking"
```

### Task 3: Integrate Transform Features into MainFrame

**Files:**
- Modify: `src/main/java/ui/MainFrame.java`

- [ ] **Step 1: Write implementation**

Modify `src/main/java/ui/MainFrame.java` inside `initMenuBar` method (around line 515), under the "Image" menu creation block:

```java
        // Image Menu
        JMenu imageMenu = new JMenu("Image");
        imageMenu.setMnemonic(KeyEvent.VK_I);
        
        JMenuItem filterItem = new JMenuItem("Filters...");
        filterItem.addActionListener(e -> performOpenFilter());
        imageMenu.add(filterItem);

        JMenuItem resizeItem = new JMenuItem("Resize...");
        resizeItem.addActionListener(e -> performOpenResize());
        imageMenu.add(resizeItem);

        imageMenu.addSeparator();

        // --- ADD TRANSFORM MENU ITEMS HERE ---
        JMenuItem rot90cw = new JMenuItem("Rotate 90 CW");
        rot90cw.addActionListener(e -> performTransform(core.image.ImageTransformUtils.TransformType.ROTATE_90_CW));
        imageMenu.add(rot90cw);

        JMenuItem rot90ccw = new JMenuItem("Rotate 90 CCW");
        rot90ccw.addActionListener(e -> performTransform(core.image.ImageTransformUtils.TransformType.ROTATE_90_CCW));
        imageMenu.add(rot90ccw);

        JMenuItem rot180 = new JMenuItem("Rotate 180");
        rot180.addActionListener(e -> performTransform(core.image.ImageTransformUtils.TransformType.ROTATE_180));
        imageMenu.add(rot180);

        imageMenu.addSeparator();

        JMenuItem flipH = new JMenuItem("Flip Horizontal");
        flipH.addActionListener(e -> performTransform(core.image.ImageTransformUtils.TransformType.FLIP_H));
        imageMenu.add(flipH);

        JMenuItem flipV = new JMenuItem("Flip Vertical");
        flipV.addActionListener(e -> performTransform(core.image.ImageTransformUtils.TransformType.FLIP_V));
        imageMenu.add(flipV);
        // ---------------------------------
```

Add the new `performTransform()` method in `MainFrame.java`:

```java
    private void performTransform(core.image.ImageTransformUtils.TransformType type) {
        BufferedImage currentImage = canvas.getBackgroundImage();
        if (currentImage != null) {
            BufferedImage newImage = core.image.ImageTransformUtils.transform(currentImage, type);
            core.history.Command transformCmd = new core.history.TransformCommand(
                    canvas, appState.getCanvasState(), currentImage, newImage, type);
            appState.getHistoryManager().push(transformCmd);
            canvas.repaint();
        } else {
            JOptionPane.showMessageDialog(this, "Please open an image first.", "No Image", JOptionPane.WARNING_MESSAGE);
        }
    }
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/ui/MainFrame.java
git commit -m "feat: hook Transform options into MainFrame menu"
```
