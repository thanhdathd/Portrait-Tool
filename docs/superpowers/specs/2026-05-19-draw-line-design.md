# Technical Design Specification: Persistent Draw Line Feature

This specification documents the architecture, data models, interaction mechanics, and user interface for the new persistent **Draw Line** feature in Portrait-Tool.

---

## 1. Objectives & Requirements

### Core Features
- **Drawing Tool:** A dedicated drawing tool activated via the toolbar or the **L** shortcut key. Users draw lines by clicking, dragging, and releasing (drag-and-drop interaction).
- **Line Entity properties:**
  - `id`: An auto-incrementing integer (starting from 1).
  - `startPoint` / `endPoint`: Coordinates relative to the original image coordinate system.
  - `strokeWidth`: Thickness of the line on canvas. Supports specific sizes: `1, 2, 3, 5, 8, 13`.
  - `strokeColor`: Color of the line. Defaults to the active brush color.
- **Selectable Lines:** Lines can be selected in single-select mode using the `SelectTool` (by clicking on the line segment). Priority selection order: **Point > Line > Grid**.
- **Interactive Neo Handles:** A selected line renders 3 handles (diameter 10px, color `#229fff`) at:
  - Endpoint 1 (Start point)
  - Endpoint 2 (End point)
  - Center point (Mid-point)
- **Handle Dragging:**
  - Dragging the endpoints changes the segment's length/angle.
  - Dragging the center handle translates the entire line (maintaining length and angle).
  - Features a generous **30px interactive grab zone** (radius 15px around handle center) and cursor feedback (changes to Hand cursor when hovering the grab zone).
- **Persistence:** Lines are fully persistent, packed into the `.pdw` ZIP projects, and supported by `AutoSaveManager` and recovery loops.
- **Undo/Redo:** Full history support via `LineCommand` (supporting actions: `ADD`, `DELETE`, `EDIT`). Dragging handles updates the canvas in real-time, but pushes only **one final command** to the history stack upon mouse release.

---

## 2. Technical Architecture & Component Design

### 2.1. Domain Model: `userpackage.SLine`
Encapsulates line properties and supports cloning:
```java
package userpackage;

import java.awt.Color;
import java.awt.Point;

public class SLine {
    public int id;
    public Point startPoint;
    public Point endPoint;
    public int strokeWidth;
    public Color strokeColor;

    public SLine() {
        this.id = 0;
        this.startPoint = new Point(0, 0);
        this.endPoint = new Point(0, 0);
        this.strokeWidth = 2;
        this.strokeColor = Color.BLACK;
    }

    public SLine(int id, Point start, Point end, int width, Color color) {
        this.id = id;
        this.startPoint = new Point(start);
        this.endPoint = new Point(end);
        this.strokeWidth = width;
        this.strokeColor = color;
    }

    public SLine copy() {
        return new SLine(this.id, this.startPoint, this.endPoint, this.strokeWidth, this.strokeColor);
    }
}
```

### 2.2. CanvasState & AppState Integration
- **`CanvasState` Changes:**
  - Add `private final List<SLine> lines = new ArrayList<>();`
  - Add `private SLine selectedLine = null;`
  - Implement getter/setter methods, `addSLine()`, `removeSLine()`, and observers (`Consumer<SLine> lineSelectionListener`).
- **`AppState` Changes:**
  - Add `private int activeLineStrokeWidth = 2;` representing the currently selected thickness on the toolbar.

---

## 3. Persistent Project Serialization & AutoSave

### 3.1. PDW Project Saving/Loading
- **`ProjectData.java`:** Update to include `public List<SLine> lines;`.
- **`ProjectFileManager.java`:**
  - When saving, serialize the `lines` list inside the project metadata JSON.
  - When loading a `.pdw` project, parse the `lines` list and populate them in `CanvasState` (and back up to `AppState` as `initialLines` for recovery safety).

### 3.2. AutoSave & Recovery
- **`AutoSaveData.java`:** Include `public List<SLine> lines;` and `public List<SLine> initialLines;`.
- **`AutoSaveManager.java`:**
  - Write current lines and initial lines into `autosave.pdt` on every periodic save.
  - During startup recovery reconstruction, load `initialLines` onto `CanvasState` first, then replay history stack changes.
  - Add support for reconstruction of `LineCommand` inside `reconstructStack()`.

---

## 4. UI Components

### 4.1. Toolbar Dropdown (`JComboBox`)
- Visible next to the color picker **only when the active tool is the Line Tool**.
- Options: `1px, 2px, 3px, 5px, 8px, 13px`.
- Renders dynamically using a **Custom List Cell Renderer** that paints visual lines representing the relative thickness matching the current brush color, avoiding text strings.

### 4.2. Floating Property Panel `LinePropertyPanel`
- Appears overlays on the top-right corner of the canvas only when `selectedLine != null`.
- **Title:** "Line Properties (ID: #)"
- **Stroke Color Picker:** Clicking opens a standard JColorChooser dialog.
- **Stroke Width Dropdown:** An identical `JComboBox` matching the toolbar's design to select and change the line's thickness dynamically.
- **Delete Button:** Sleek red trash can button to delete the selected line.
- **Delete Hotkey:** Listening to the **Delete** key while a line is selected will invoke the deletion command.

---

## 5. Interaction & Mathematics

### 5.1. Perpendicular Distance Calculation (Click Detection)
Given a segment $AB$ and a click point $P$, we project $P$ onto the line defined by $AB$ and clamp the projection parameter $t$ to the range $[0, 1]$ to check the distance from $P$ to the segment:
$$t = \max\left(0, \min\left(1, \frac{(P - A) \cdot (B - A)}{\|B - A\|^2}\right)\right)$$
$$\text{Distance} = \|P - (A + t(B - A))\|$$
If the computed Distance $\le 10$ pixels in screen space, the segment is selected.

### 5.2. Drag offset preservation
Upon mouse press on handle $H$ at coordinate $H_c$:
$$\text{dragOffset} = P_{\text{mouse}} - H_c$$
During drag, the handle center is positioned at:
$$H_c^{\text{new}} = P_{\text{mouse}}^{\text{current}} - \text{dragOffset}$$
This prevents sudden handle center jumps.

---

## 6. History Command pattern

### `LineCommand.java`
Implements the standard command interface:
- **Action types:** `ADD`, `DELETE`, `EDIT`
- **Fields:** `SLine oldState`, `SLine newState`
- **Undo / Redo execution:** Modifies `CanvasState.getLines()` directly and calls `repaint()`.

---

## 7. Verification plan

### Automated Verification
- Run `mvn clean compile` to check that compilation finishes with `BUILD SUCCESS`.

### Manual Testing Matrix
1. **Tool Switch:** Select Line tool -> verify toolbar dropdown appears. Select Hand tool -> verify dropdown disappears.
2. **Drawing Interaction:** Drag mouse -> verify live preview draws exactly with current color and stroke width. Release -> verify line created and selected.
3. **Handle Hover:** Move mouse over selected line handles -> verify con trỏ changes to HAND when within 30px (screen coords) of handle centers.
4. **Endpoint Moving:** Drag endpoint handle -> verify length/angle changes dynamically. Release -> verify undoable command is pushed.
5. **Tịnh tiến lines:** Drag center handle -> verify whole line translates smoothly. Release -> verify undoable command is pushed.
6. **Property Updates:** Select line -> change color/width in floating panel -> verify canvas updates instantly.
7. **Deletion:** Press Delete hotkey or click Delete button -> verify line is deleted and panel disappears.
8. **Undo/Redo:** Perform Undo/Redo on all actions -> verify canvas restores state perfectly.
9. **Project Load/Save:** Save project as `.pdw` -> close app -> reopen `.pdw` -> verify lines restore with correct coordinates, IDs, widths, and colors.
10. **Autosave Recovery:** Trigger autosave -> kill app -> restore -> verify all lines and grids restore perfectly.
