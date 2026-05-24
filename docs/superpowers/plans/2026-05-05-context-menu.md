# Context Menu Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a comprehensive right-click context menu on the Image Canvas containing shortcuts for Undo, Redo, Resize, and Image Transformations.

**Architecture:** We will construct a `JPopupMenu` during `MainFrame` initialization and attach it to the `ImageCanvas` using `setComponentPopupMenu()`. The menu items will reuse the existing functional methods in `MainFrame` (`performOpenResize()`, `performTransform()`, and history operations), ensuring DRY principles are maintained. We will also hook the Undo/Redo context menu items into the existing `HistoryManager` listener so their enabled states automatically synchronize with the toolbar buttons.

**Tech Stack:** Java 17, Swing.

---

### Task 1: Create and Integrate Context Menu

**Files:**
- Modify: `src/main/java/ui/MainFrame.java`

- [ ] **Step 1: Write implementation**

Modify `src/main/java/ui/MainFrame.java`. Add a new private method `initContextMenu()` and call it at the end of the `MainFrame` constructor (right before `pack()`).

```java
    // Add this near the end of the MainFrame constructor:
    // initMenuBar();
    // initToolBar();
    // initContextMenu(); // <--- ADD THIS
    // ...

    // Add this method anywhere in MainFrame:
    private void initContextMenu() {
        JPopupMenu contextMenu = new JPopupMenu();

        JMenuItem undoItem = new JMenuItem("Undo");
        undoItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        undoItem.addActionListener(e -> {
            if (appState.getHistoryManager().canUndo()) {
                appState.getHistoryManager().undo();
                canvas.repaint();
            }
        });

        JMenuItem redoItem = new JMenuItem("Redo");
        redoItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        redoItem.addActionListener(e -> {
            if (appState.getHistoryManager().canRedo()) {
                appState.getHistoryManager().redo();
                canvas.repaint();
            }
        });

        contextMenu.add(undoItem);
        contextMenu.add(redoItem);
        contextMenu.addSeparator();

        JMenuItem resizeItem = new JMenuItem("Resize...");
        resizeItem.addActionListener(e -> performOpenResize());
        contextMenu.add(resizeItem);

        contextMenu.addSeparator();

        JMenuItem rot90cw = new JMenuItem("Rotate 90 CW");
        rot90cw.addActionListener(e -> performTransform(core.image.ImageTransformUtils.TransformType.ROTATE_90_CW));
        contextMenu.add(rot90cw);

        JMenuItem rot90ccw = new JMenuItem("Rotate 90 CCW");
        rot90ccw.addActionListener(e -> performTransform(core.image.ImageTransformUtils.TransformType.ROTATE_90_CCW));
        contextMenu.add(rot90ccw);

        JMenuItem rot180 = new JMenuItem("Rotate 180");
        rot180.addActionListener(e -> performTransform(core.image.ImageTransformUtils.TransformType.ROTATE_180));
        contextMenu.add(rot180);

        contextMenu.addSeparator();

        JMenuItem flipH = new JMenuItem("Flip Horizontal");
        flipH.addActionListener(e -> performTransform(core.image.ImageTransformUtils.TransformType.FLIP_H));
        contextMenu.add(flipH);

        JMenuItem flipV = new JMenuItem("Flip Vertical");
        flipV.addActionListener(e -> performTransform(core.image.ImageTransformUtils.TransformType.FLIP_V));
        contextMenu.add(flipV);

        // Dynamic Enable/Disable based on History state
        contextMenu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                undoItem.setEnabled(appState.getHistoryManager().canUndo());
                redoItem.setEnabled(appState.getHistoryManager().canRedo());
            }

            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {}

            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {}
        });

        canvas.setComponentPopupMenu(contextMenu);
    }
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/ui/MainFrame.java
git commit -m "feat: add canvas context menu with image operations and history"
```
