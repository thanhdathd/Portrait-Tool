# Line Multi-Select and Batch Operations Spec

Design document for implementing multi-selection and batch operations for lines in the Portrai-Tool application.

## 1. Goal

Enable users to select multiple lines simultaneously, perform synchronous spatial translation via midpoint handles, and apply batch operations (deletion, color modification, stroke width adjustment) independently from the property panel.

---

## 2. Proposed Changes

### Component 1: CanvasState.java
* **State Array**: Replace `private SLine selectedLine = null` with `private final LinkedHashSet<SLine> selectedLines = new LinkedHashSet<>()`.
* **Backward Compatibility**:
  - `getSelectedLine()` returns the last element added to `selectedLines` (most recently selected) or `null` if empty.
  - `setSelectedLine(SLine line)` clears the collection and adds the specified line.
* **New Selection API**:
  - `getSelectedLines()` returns an unmodifiable view of the selection set.
  - `setSelectedLines(Set<SLine> lines)` updates the active selection.
  - `addToSelection(SLine line)` and `removeFromSelection(SLine line)`.
  - `clearLineSelection()`.
* **Listeners**:
  - Add `Consumer<Set<SLine>> lineSelectionListener` and `setLineSelectionListener()`.
  - Update `clearAll()` to reset `selectedLines`.

---

### Component 2: SelectTool.java (Selection Mechanics)
* **Drag-Rect Intersection**:
  - Implement a segment-rectangle intersection check: A line $AB$ is selected by a rectangle $R$ if either endpoint $A$ or $B$ is inside $R$, or if the segment $AB$ intersects any of the 4 borders of $R$.
  - When dragging a selection box, collect all matching lines.
* **Modifiers**:
  - **Click with Shift/Ctrl**: Toggle selection of the clicked line.
  - **Drag-release with Shift/Ctrl**: Add matched lines to the existing selection set.
  - **Normal Click/Drag-release**: Replace the selection set entirely.
* **Handle Dragging**:
  - If `selectedLines.size() > 1`:
    - Disable hover and drag interactions on the endpoint handles of all selected lines.
    - Keep hover (`HAND_CURSOR`) and drag interactions active only on the **midpoint handles** of selected lines.
    - Dragging a midpoint handle translates **all** selected lines synchronously by the same $(dx, dy)$ offset.
    - Drag release creates a single `BatchLineCommand` of action `EDIT`.

---

### Component 3: LineTool.java
* **Handle Editing parity**:
  - Apply the same handle restriction when `selectedLines.size() > 1`: only allow midpoint hover/dragging, translating all selected lines synchronously, and pushing a single `BatchLineCommand` on mouse release.

---

### Component 4: BatchLineCommand.java
Create a new history command `BatchLineCommand` that represents a batch of line operations:
* **Actions**: Supports `Action.EDIT` (translating coordinates or updating stroke width/color) and `Action.DELETE` (removing multiple lines).
* **LineStatePair**: Store each target line alongside its `oldState` and `newState` (using clones of `SLine` to allow granular restorations).
* **Execute/Undo**:
  - `Action.EDIT`: Apply `newState` fields (coordinate adjustments, color, or stroke width) to the live lines during execute; restore `oldState` fields during undo.
  - `Action.DELETE`: Remove live lines from the canvas state during execute; restore them during undo.

---

### Component 5: LinePropertyPanel.java
* **Event Listening**:
  - Listen to the new line selection listener (`Consumer<Set<SLine>>`).
* **Visual Display**:
  - If single line: Show `ID: <line.id>`, active stroke width, and color.
  - If multiple lines: Show `ID: <count> lines selected`. Show common values for stroke width and color if they match; otherwise show blank/neutral default states.
* **Granular Batch Updates**:
  - **Color**: Changing color creates a `BatchLineCommand` of type `Action.EDIT` updating only the `strokeColor` for all selected lines. Other properties remain untouched.
  - **Stroke Width**: Changing stroke width creates a `BatchLineCommand` of type `Action.EDIT` updating only `strokeWidth` for all selected lines.
  - **Delete**: Clicking delete creates a `BatchLineCommand` of type `Action.DELETE` removing all selected lines.

---

## 3. Verification Plan

### Automated Tests
* Create `BatchLineCommandTest.java` verifying:
  - Batch editing of stroke widths (leaving colors unchanged).
  - Batch editing of colors (leaving stroke widths unchanged).
  - Batch deletion and restoration on undo.
  - Multi-line translation (editing coordinates of all lines synchronously).
* Create `LineSelectionTest.java` verifying:
  - Drag-rect intersection checks (identifying intersecting segments).
  - Selection modifiers (Shift/Ctrl add/remove toggles).
