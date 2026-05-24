# Design Specification: Save File Toast Notification

Replacing successful save and export dialog notifications with custom lightweight, transient Toast/Snackbar notifications positioned at the bottom-right corner of the application window.

## Overview

Currently, when the user saves the project, exports images, point maps, or matrix data, the application presents a modal dialog (`JOptionPane.showMessageDialog`). This interrupts the user's flow and requires explicit click confirmation.

This specification introduces a modern, lightweight, non-blocking toast notification that slides/appears at the bottom-right corner of the `MainFrame` and automatically disappears after 2 seconds.

## Design Details

### 1. ToastNotification Component
A new custom Swing component, `ToastNotification`, will be created at `ui/components/ToastNotification.java`:
- Extends `JPanel`.
- Uses custom painting (`paintComponent`) to render a rounded rectangle with a dark semi-transparent background (`new Color(30, 30, 30, 220)`) and a subtle border.
- Layout: `BorderLayout` with 10px horizontal and 8px vertical padding.
- Leading Icon: A success indicator (green circle with white checkmark) drawn dynamically using `Graphics2D` to avoid loading external files.
- Text: A `JLabel` with white text (`Color.WHITE`), font size 12px, bold.

### 2. Lifespan and Timer
- When shown, a `javax.swing.Timer` with a delay of **2000ms** is started.
- Once the timer fires, the Toast component is removed from its parent container (`JLayeredPane`) and the parent is repainted.
- Thread-safe: Invoking the toast is wrapped in `SwingUtilities.invokeLater` to ensure it always runs on the Event Dispatch Thread (EDT).

### 3. Positioning and Layering
- The toast is added to `JLayeredPane.POPUP_LAYER` of `MainFrame` so it is drawn on top of all normal panels.
- Its position is calculated dynamically:
  - `x = frame.getWidth() - toast.getPreferredSize().width - 20`
  - `y = frame.getHeight() - toast.getPreferredSize().height - 20`
- To handle frame resizing while the toast is visible, a `ComponentListener` is registered on the frame to adjust the toast's bounds in real time. This listener is automatically removed when the toast is dismissed to prevent memory leaks.

### 4. Owner Resolution
Since background threads or worker threads (like `SaveWorker` or `SavePointMapWorker`) might trigger saves with a `null` owner component, a helper method resolves the active visible instance of `MainFrame`:
```java
public static MainFrame findMainFrame() {
    for (Frame frame : Frame.getFrames()) {
        if (frame instanceof MainFrame && frame.isVisible()) {
            return (MainFrame) frame;
        }
    }
    return null;
}
```

## Integration Points

We will replace the successful save/export message dialogs at:
1. **Lưu Project**: `ui/MainFrame.java`
2. **Lưu Point Map**: `workers/SavePointMapWorker.java`
3. **Lưu Ảnh**: `workers/SaveWorker.java`
4. **Xuất PDF/PNG**: `ui/MainFrame.java`
5. **Xuất Matrix**: `workers/ExportMatrixWorker.java`

Error dialogs will remain unchanged (`JOptionPane.showMessageDialog` with error icon) to ensure critical problems are not missed.
