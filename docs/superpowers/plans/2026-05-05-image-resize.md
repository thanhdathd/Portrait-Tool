# Image Resize Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement an Image Resize feature triggered from the Image menu, allowing users to resize the canvas and scale markers (points, grids) accordingly.

**Architecture:** A new `ResizeDialog` collects user input (dimensions, percent/pixel mode, interpolation algorithm). `core.image.ImageResizer` handles `BufferedImage` scaling. `core.history.ResizeCommand` handles applying the new image and scaling `CanvasState` markers (undoable).

**Tech Stack:** Java 17, Swing, MigLayout, FlatLaf, JUnit 5.

---

### Task 1: Create ImageResizer Utilities

**Files:**
- Create: `src/main/java/core/image/ImageResizer.java`
- Create: `src/test/java/core/image/ImageResizerTest.java`

- [ ] **Step 1: Write the failing test**

```java
package core.image;

import org.junit.jupiter.api.Test;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import static org.junit.jupiter.api.Assertions.*;

class ImageResizerTest {
    @Test
    void testResizeImage() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        BufferedImage resized = ImageResizer.resize(img, 50, 50, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        assertEquals(50, resized.getWidth());
        assertEquals(50, resized.getHeight());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ImageResizerTest`
Expected: FAIL with compilation error (ImageResizer not found)

- [ ] **Step 3: Write minimal implementation**

```java
package core.image;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class ImageResizer {
    public static BufferedImage resize(BufferedImage original, int targetWidth, int targetHeight, Object interpolationHint) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolationHint);
        g2d.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return resizedImage;
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=ImageResizerTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/core/image/ImageResizer.java src/test/java/core/image/ImageResizerTest.java
git commit -m "feat: add ImageResizer utility"
```

### Task 2: Create ResizeCommand

**Files:**
- Create: `src/main/java/core/history/ResizeCommand.java`

- [ ] **Step 1: Write implementation**

```java
package core.history;

import core.state.CanvasState;
import ui.canvas.ImageCanvas;
import userpackage.SPoint;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ResizeCommand implements Command {
    private final ImageCanvas canvas;
    private final CanvasState canvasState;
    private final BufferedImage oldImage;
    private final BufferedImage newImage;
    private final List<SPoint> oldPoints;
    private final List<SPoint> oldGrids;
    private final double scaleX;
    private final double scaleY;

    public ResizeCommand(ImageCanvas canvas, CanvasState canvasState, BufferedImage oldImage, BufferedImage newImage) {
        this.canvas = canvas;
        this.canvasState = canvasState;
        this.oldImage = oldImage;
        this.newImage = newImage;
        this.oldPoints = clonePoints(canvasState.getStickyPoints());
        this.oldGrids = clonePoints(canvasState.getGrids());
        this.scaleX = (double) newImage.getWidth() / oldImage.getWidth();
        this.scaleY = (double) newImage.getHeight() / oldImage.getHeight();
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
        scalePoints(canvasState.getStickyPoints());
        scalePoints(canvasState.getGrids());
    }

    @Override
    public void undo() {
        canvas.setBackgroundImage(oldImage);
        canvasState.getStickyPoints().clear();
        canvasState.getStickyPoints().addAll(oldPoints);
        canvasState.getGrids().clear();
        canvasState.getGrids().addAll(oldGrids);
    }
    
    private void scalePoints(List<SPoint> points) {
        for (SPoint p : points) {
            p.X = (int) Math.round(p.X * scaleX);
            p.Y = (int) Math.round(p.Y * scaleY);
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/core/history/ResizeCommand.java
git commit -m "feat: add ResizeCommand for history tracking"
```

### Task 3: Create ResizeDialog

**Files:**
- Create: `src/main/java/ui/dialogs/ResizeDialog.java`

- [ ] **Step 1: Write implementation**

```java
package ui.dialogs;

import core.image.ImageResizer;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class ResizeDialog extends JDialog {

    private final BufferedImage sourceImage;
    private final Consumer<BufferedImage> onApply;

    private JTextField widthField;
    private JTextField heightField;
    private JComboBox<String> unitBox;
    private JCheckBox lockAspectRatio;
    private JComboBox<String> interpolationBox;
    
    private boolean isUpdating = false;

    public ResizeDialog(Frame owner, BufferedImage sourceImage, Consumer<BufferedImage> onApply) {
        super(owner, "Resize Image", true);
        this.sourceImage = sourceImage;
        this.onApply = onApply;
        
        initUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        setLayout(new MigLayout("wrap 2, insets 15", "[right][grow,fill]"));

        widthField = new JTextField(String.valueOf(sourceImage.getWidth()), 10);
        heightField = new JTextField(String.valueOf(sourceImage.getHeight()), 10);
        
        unitBox = new JComboBox<>(new String[]{"Pixels", "Percent"});
        lockAspectRatio = new JCheckBox("Lock Aspect Ratio", true);
        
        interpolationBox = new JComboBox<>(new String[]{
            "Nearest Neighbor", "Bilinear", "Bicubic"
        });
        interpolationBox.setSelectedIndex(2);

        unitBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateFieldsForUnit();
            }
        });

        widthField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { syncSize(true); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { syncSize(true); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { syncSize(true); }
        });

        heightField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { syncSize(false); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { syncSize(false); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { syncSize(false); }
        });

        JButton applyBtn = new JButton("Apply");
        applyBtn.addActionListener(e -> applyResize());
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        add(new JLabel("Width:"));
        add(widthField);
        
        add(new JLabel("Height:"));
        add(heightField);
        
        add(new JLabel("Unit:"));
        add(unitBox);
        
        add(new JLabel(""), "span 1");
        add(lockAspectRatio);
        
        add(new JLabel("Interpolation:"));
        add(interpolationBox);
        
        add(applyBtn, "span 2, split 2, right");
        add(cancelBtn, "right");
    }

    private void updateFieldsForUnit() {
        isUpdating = true;
        if (unitBox.getSelectedIndex() == 0) { // Pixels
            widthField.setText(String.valueOf(sourceImage.getWidth()));
            heightField.setText(String.valueOf(sourceImage.getHeight()));
        } else { // Percent
            widthField.setText("100");
            heightField.setText("100");
        }
        isUpdating = false;
    }

    private void syncSize(boolean fromWidth) {
        if (isUpdating || !lockAspectRatio.isSelected()) return;
        
        try {
            double ratio = (double) sourceImage.getHeight() / sourceImage.getWidth();
            isUpdating = true;
            if (fromWidth) {
                int w = Integer.parseInt(widthField.getText());
                int h = (int) Math.round(w * ratio);
                heightField.setText(String.valueOf(h));
            } else {
                int h = Integer.parseInt(heightField.getText());
                int w = (int) Math.round(h / ratio);
                widthField.setText(String.valueOf(w));
            }
        } catch (NumberFormatException ignored) {}
        finally { isUpdating = false; }
    }

    private void applyResize() {
        try {
            int w = Integer.parseInt(widthField.getText());
            int h = Integer.parseInt(heightField.getText());
            
            if (unitBox.getSelectedIndex() == 1) { // Percent
                w = (int) Math.round(sourceImage.getWidth() * (w / 100.0));
                h = (int) Math.round(sourceImage.getHeight() * (h / 100.0));
            }
            
            if (w <= 0 || h <= 0) {
                JOptionPane.showMessageDialog(this, "Dimensions must be positive.");
                return;
            }

            Object hint = switch (interpolationBox.getSelectedIndex()) {
                case 0 -> RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
                case 1 -> RenderingHints.VALUE_INTERPOLATION_BILINEAR;
                default -> RenderingHints.VALUE_INTERPOLATION_BICUBIC;
            };

            BufferedImage newImg = ImageResizer.resize(sourceImage, w, h, hint);
            onApply.accept(newImg);
            dispose();
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers.");
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/ui/dialogs/ResizeDialog.java
git commit -m "feat: add ResizeDialog UI"
```

### Task 4: Integrate Resize Feature into MainFrame

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

        // --- ADD RESIZE MENU ITEM HERE ---
        JMenuItem resizeItem = new JMenuItem("Resize...");
        resizeItem.addActionListener(e -> performOpenResize());
        imageMenu.add(resizeItem);
        // ---------------------------------
```

Add the new `performOpenResize()` method in `MainFrame.java`:

```java
    private void performOpenResize() {
        BufferedImage currentImage = canvas.getBackgroundImage();
        if (currentImage != null) {
            new ui.dialogs.ResizeDialog(this, currentImage, (newImage) -> {
                core.history.Command resizeCmd = new core.history.ResizeCommand(
                        canvas, appState.getCanvasState(), currentImage, newImage);
                appState.getHistoryManager().push(resizeCmd);
                canvas.repaint();
            }).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Please open an image first.", "No Image", JOptionPane.WARNING_MESSAGE);
        }
    }
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/ui/MainFrame.java
git commit -m "feat: hook Resize dialog into MainFrame menu"
```
