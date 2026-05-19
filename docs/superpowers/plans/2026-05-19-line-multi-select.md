# Line Multi-Select Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enable multi-selection of lines via Shift/Ctrl + Click and selection rectangle drag, allowing synchronous translation of selected lines via midpoint handles and batch property editing.

**Architecture:** Extend state management in `CanvasState` to track `selectedLines`, implement segment-rectangle intersection selection in `SelectTool`, introduce a new `BatchLineCommand` history action for atomic multi-line changes, and adapt `LinePropertyPanel` for independent batch property updates.

**Tech Stack:** Java SE 17, Swing, JUnit 5

---

## File Structure Map
* **Modify**: [CanvasState.java](file:///c:/Users/DELL/Work/Java/Tool%20Projects/Portrai-Tool/src/main/java/core/state/CanvasState.java) — Manage multi-line selections and update listeners.
* **New**: [BatchLineCommand.java](file:///c:/Users/DELL/Work/Java/Tool%20Projects/Portrai-Tool/src/main/java/core/history/BatchLineCommand.java) — Handle batch modifications/deletions and their undos.
* **Modify**: [LinePropertyPanel.java](file:///c:/Users/DELL/Work/Java/Tool%20Projects/Portrai-Tool/src/main/java/ui/components/LinePropertyPanel.java) — Update UI based on selection size and delegate updates to `BatchLineCommand`.
* **Modify**: [SelectTool.java](file:///c:/Users/DELL/Work/Java/Tool%20Projects/Portrai-Tool/src/main/java/tools/SelectTool.java) — Coordinate selection rectangle intersection, Shift/Ctrl modifier toggles, handle cursor restriction, and multi-line translation.
* **Modify**: [LineTool.java](file:///c:/Users/DELL/Work/Java/Tool%20Projects/Portrai-Tool/src/main/java/tools/LineTool.java) — Restrict endpoint handles and implement multi-line midpoint translation.
* **New**: [BatchLineCommandTest.java](file:///c:/Users/DELL/Work/Java/Tool%20Projects/Portrai-Tool/src/test/java/core/history/BatchLineCommandTest.java) — Automated test for batch edits and deletes.
* **New**: [LineSelectionTest.java](file:///c:/Users/DELL/Work/Java/Tool%20Projects/Portrai-Tool/src/test/java/tools/LineSelectionTest.java) — Automated test for selection intersection checks.

---

### Task 1: Update CanvasState State Management

**Files:**
* Modify: [CanvasState.java](file:///c:/Users/DELL/Work/Java/Tool%20Projects/Portrai-Tool/src/main/java/core/state/CanvasState.java)

- [ ] **Step 1: Modify CanvasState fields and listener signatures**
  Replace lines 17-20 in `CanvasState.java` to support multiple selected lines:
  ```java
  private final List<userpackage.SLine> lines = new ArrayList<>();
  private final LinkedHashSet<userpackage.SLine> selectedLines = new LinkedHashSet<>();
  private Consumer<Set<userpackage.SLine>> lineSelectionListener;
  ```

- [ ] **Step 2: Update selection APIs in CanvasState**
  Replace selection getter/setter methods (lines 176-189):
  ```java
  public userpackage.SLine getSelectedLine() {
      if (selectedLines.isEmpty()) return null;
      userpackage.SLine last = null;
      for (userpackage.SLine l : selectedLines) last = l;
      return last;
  }

  public Set<userpackage.SLine> getSelectedLines() {
      return Collections.unmodifiableSet(selectedLines);
  }

  public void setSelectedLine(userpackage.SLine line) {
      selectedLines.clear();
      if (line != null) {
          selectedLines.add(line);
      }
      fireLineSelectionChanged();
  }

  public void setSelectedLines(Set<userpackage.SLine> lines) {
      selectedLines.clear();
      if (lines != null) {
          selectedLines.addAll(lines);
      }
      fireLineSelectionChanged();
  }

  public void addToSelection(userpackage.SLine line) {
      if (line != null && selectedLines.add(line)) {
          fireLineSelectionChanged();
      }
  }

  public void removeFromSelection(userpackage.SLine line) {
      if (line != null && selectedLines.remove(line)) {
          fireLineSelectionChanged();
      }
  }

  public void clearLineSelection() {
      if (!selectedLines.isEmpty()) {
          selectedLines.clear();
          fireLineSelectionChanged();
      }
  }

  public void setLineSelectionListener(Consumer<Set<userpackage.SLine>> listener) {
      this.lineSelectionListener = listener;
  }

  private void fireLineSelectionChanged() {
      if (lineSelectionListener != null) {
          lineSelectionListener.accept(Collections.unmodifiableSet(selectedLines));
      }
  }
  ```

- [ ] **Step 3: Update `clearAll()` method**
  Ensure `clearAll()` clears `selectedLines` by replacing:
  ```java
  setSelectedLine(null);
  ```
  with:
  ```java
  clearLineSelection();
  ```

- [ ] **Step 4: Run test to verify it compiles**
  Run: `mvn compile`
  Expected: BUILD SUCCESS (Note: line selection compiler errors will arise in `LinePropertyPanel.java` and `SelectTool.java` due to listener signature mismatch; we will resolve these in subsequent tasks).

---

### Task 2: Implement BatchLineCommand

**Files:**
* Create: [BatchLineCommand.java](file:///c:/Users/DELL/Work/Java/Tool%20Projects/Portrai-Tool/src/main/java/core/history/BatchLineCommand.java)
* Create: [BatchLineCommandTest.java](file:///c:/Users/DELL/Work/Java/Tool%20Projects/Portrai-Tool/src/test/java/core/history/BatchLineCommandTest.java)

- [ ] **Step 1: Write the failing/minimal BatchLineCommandTest**
  Create `src/test/java/core/history/BatchLineCommandTest.java` with test cases:
  ```java
  package core.history;

  import core.state.AppState;
  import core.state.CanvasState;
  import ui.canvas.ImageCanvas;
  import userpackage.SLine;
  import org.junit.jupiter.api.Test;
  import java.awt.Point;
  import java.awt.Color;
  import java.util.ArrayList;
  import java.util.List;
  import static org.junit.jupiter.api.Assertions.*;

  class BatchLineCommandTest {
      @Test
      void testBatchEditAndUndo() {
          AppState appState = new AppState();
          CanvasState canvasState = appState.getCanvasState();
          ImageCanvas canvas = new ImageCanvas(appState) {
              @Override public void repaint() {}
          };

          SLine line1 = new SLine(1, new Point(0, 0), new Point(10, 10), 2, Color.BLACK);
          SLine line2 = new SLine(2, new Point(20, 20), new Point(30, 30), 2, Color.BLACK);
          canvasState.getLines().add(line1);
          canvasState.getLines().add(line2);

          List<BatchLineCommand.LineStatePair> pairs = new ArrayList<>();
          
          SLine line1New = line1.copy();
          line1New.strokeColor = Color.RED;
          line1New.startPoint.setLocation(5, 5);
          pairs.add(new BatchLineCommand.LineStatePair(line1, line1.copy(), line1New));

          SLine line2New = line2.copy();
          line2New.strokeColor = Color.RED;
          line2New.startPoint.setLocation(25, 25);
          pairs.add(new BatchLineCommand.LineStatePair(line2, line2.copy(), line2New));

          BatchLineCommand cmd = new BatchLineCommand(canvasState, canvas, BatchLineCommand.Action.EDIT, pairs);
          cmd.execute();

          assertEquals(Color.RED, line1.strokeColor);
          assertEquals(new Point(5, 5), line1.startPoint);
          assertEquals(Color.RED, line2.strokeColor);
          assertEquals(new Point(25, 25), line2.startPoint);

          cmd.undo();

          assertEquals(Color.BLACK, line1.strokeColor);
          assertEquals(new Point(0, 0), line1.startPoint);
          assertEquals(Color.BLACK, line2.strokeColor);
          assertEquals(new Point(20, 20), line2.startPoint);
      }
  }
  ```

- [ ] **Step 2: Implement BatchLineCommand.java**
  Create `src/main/java/core/history/BatchLineCommand.java`:
  ```java
  package core.history;

  import core.state.CanvasState;
  import core.state.CommandData;
  import ui.canvas.ImageCanvas;
  import userpackage.SLine;
  import java.util.ArrayList;
  import java.util.HashSet;
  import java.util.List;

  public class BatchLineCommand implements Command {
      public enum Action { EDIT, DELETE }

      private final CanvasState canvasState;
      private final ImageCanvas canvas;
      private final Action action;
      private final List<LineStatePair> linePairs;

      public static class LineStatePair {
          public final SLine line;
          public final SLine oldState;
          public final SLine newState;

          public LineStatePair(SLine line, SLine oldState, SLine newState) {
              this.line = line;
              this.oldState = oldState;
              this.newState = newState;
          }
      }

      public BatchLineCommand(CanvasState canvasState, ImageCanvas canvas, Action action, List<LineStatePair> linePairs) {
          this.canvasState = canvasState;
          this.canvas = canvas;
          this.action = action;
          this.linePairs = linePairs != null ? linePairs : new ArrayList<>();
      }

      @Override
      public void execute() {
          if (action == Action.EDIT) {
              for (LineStatePair pair : linePairs) {
                  pair.line.startPoint.setLocation(pair.newState.startPoint);
                  pair.line.endPoint.setLocation(pair.newState.endPoint);
                  pair.line.strokeWidth = pair.newState.strokeWidth;
                  pair.line.strokeColor = pair.newState.strokeColor;
              }
          } else if (action == Action.DELETE) {
              for (LineStatePair pair : linePairs) {
                  canvasState.getLines().remove(pair.line);
                  canvasState.removeFromSelection(pair.line);
              }
          }
          canvas.repaint();
      }

      @Override
      public void undo() {
          if (action == Action.EDIT) {
              for (LineStatePair pair : linePairs) {
                  pair.line.startPoint.setLocation(pair.oldState.startPoint);
                  pair.line.endPoint.setLocation(pair.oldState.endPoint);
                  pair.line.strokeWidth = pair.oldState.strokeWidth;
                  pair.line.strokeColor = pair.oldState.strokeColor;
              }
          } else if (action == Action.DELETE) {
              HashSet<SLine> selectSet = new HashSet<>(canvasState.getSelectedLines());
              for (LineStatePair pair : linePairs) {
                  if (!canvasState.getLines().contains(pair.line)) {
                      canvasState.getLines().add(pair.line);
                  }
                  selectSet.add(pair.line);
              }
              canvasState.setSelectedLines(selectSet);
          }
          canvas.repaint();
      }

      @Override
      public CommandData capture() {
          return null; // Local command history, not autosaved
      }

      @Override
      public long getMemorySize() {
          return 256 + (linePairs.size() * 128);
      }
  }
  ```

- [ ] **Step 3: Run test to verify it passes**
  Run: `mvn test -Dtest=BatchLineCommandTest`
  Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**
  Run: `git add src/main/java/core/history/BatchLineCommand.java src/test/java/core/history/BatchLineCommandTest.java`
  Run: `git commit -m "feat: implement BatchLineCommand with test"`

---

### Task 3: Update LinePropertyPanel for Batch Operations

**Files:**
* Modify: [LinePropertyPanel.java](file:///c:/Users/DELL/Work/Java/Tool%20Projects/Portrai-Tool/src/main/java/ui/components/LinePropertyPanel.java)

- [ ] **Step 1: Modify LinePropertyPanel selection listener**
  Replace lines 112-113:
  ```java
  // --- Selection listener ---
  appState.getCanvasState().setLineSelectionListener(lines -> updateUIFromLines(lines));
  ```

- [ ] **Step 2: Replace `updateUIFromLine` with `updateUIFromLines`**
  Modify methods (lines 127-144):
  ```java
  private void updateUIFromLines(java.util.Set<SLine> lines) {
      if (lines == null || lines.isEmpty()) {
          setVisible(false);
          return;
      }

      isUpdatingUI = true;
      if (lines.size() == 1) {
          SLine line = lines.iterator().next();
          idLabel.setText("ID: " + line.id);
          colorBtn.setBackground(line.strokeColor);
          strokeWidthComboBox.setSelectedItem(line.strokeWidth);
      } else {
          idLabel.setText("ID: " + lines.size() + " lines selected");
          
          // Determine if all selected lines share the same color/width
          Color sharedColor = null;
          Integer sharedWidth = null;
          boolean uniformColor = true;
          boolean uniformWidth = true;

          for (SLine l : lines) {
              if (sharedColor == null) sharedColor = l.strokeColor;
              else if (!sharedColor.equals(l.strokeColor)) uniformColor = false;

              if (sharedWidth == null) sharedWidth = l.strokeWidth;
              else if (!sharedWidth.equals(l.strokeWidth)) uniformWidth = false;
          }

          if (uniformColor) {
              colorBtn.setBackground(sharedColor);
          } else {
              colorBtn.setBackground(Color.LIGHT_GRAY);
          }

          if (uniformWidth) {
              strokeWidthComboBox.setSelectedItem(sharedWidth);
          } else {
              strokeWidthComboBox.setSelectedIndex(-1);
          }
      }

      setVisible(true);
      isUpdatingUI = false;

      if (getParent() != null) {
          getParent().revalidate();
          getParent().repaint();
      }
  }
  ```

- [ ] **Step 3: Modify ListCellRenderer to use uniform rendering**
  Replace lines 80-82:
  ```java
  SLine currentLine = appState.getCanvasState().getSelectedLine();
  Color strokeColor = (currentLine != null) ? currentLine.strokeColor : appState.getBrushColor();
  if (strokeColor.equals(Color.LIGHT_GRAY) && appState.getCanvasState().getSelectedLines().size() > 1) {
      strokeColor = Color.DARK_GRAY; // Fallback for mixed color visualization
  }
  g2.setColor(strokeColor);
  ```

- [ ] **Step 4: Implement granular batch property changes**
  Modify properties change handlers (lines 146-206) to use `BatchLineCommand`:
  ```java
  private void applyStrokeWidthChange() {
      if (isUpdatingUI) return;
      java.util.Set<SLine> lines = appState.getCanvasState().getSelectedLines();
      if (lines.isEmpty()) return;

      Integer selectedWidth = (Integer) strokeWidthComboBox.getSelectedItem();
      if (selectedWidth == null) return;

      List<BatchLineCommand.LineStatePair> pairs = new ArrayList<>();
      for (SLine l : lines) {
          if (l.strokeWidth != selectedWidth) {
              SLine oldState = l.copy();
              SLine newState = l.copy();
              newState.strokeWidth = selectedWidth;
              pairs.add(new BatchLineCommand.LineStatePair(l, oldState, newState));
          }
      }

      if (!pairs.isEmpty()) {
          BatchLineCommand cmd = new BatchLineCommand(
                  appState.getCanvasState(),
                  canvas,
                  BatchLineCommand.Action.EDIT,
                  pairs
          );
          appState.getHistoryManager().push(cmd);
      }
  }

  private void changeColor() {
      java.util.Set<SLine> lines = appState.getCanvasState().getSelectedLines();
      if (lines.isEmpty()) return;

      Color initialColor = Color.BLACK;
      if (lines.size() == 1) {
          initialColor = lines.iterator().next().strokeColor;
      }
      Color newColor = JColorChooser.showDialog(this, "Select Line Color", initialColor);
      if (newColor == null) return;

      List<BatchLineCommand.LineStatePair> pairs = new ArrayList<>();
      for (SLine l : lines) {
          if (!newColor.equals(l.strokeColor)) {
              SLine oldState = l.copy();
              SLine newState = l.copy();
              newState.strokeColor = newColor;
              pairs.add(new BatchLineCommand.LineStatePair(l, oldState, newState));
          }
      }

      if (!pairs.isEmpty()) {
          BatchLineCommand cmd = new BatchLineCommand(
                  appState.getCanvasState(),
                  canvas,
                  BatchLineCommand.Action.EDIT,
                  pairs
          );
          appState.getHistoryManager().push(cmd);
          colorBtn.setBackground(newColor);
          strokeWidthComboBox.repaint();
      }
  }

  private void deleteSelectedLine() {
      java.util.Set<SLine> lines = appState.getCanvasState().getSelectedLines();
      if (lines.isEmpty()) return;

      List<BatchLineCommand.LineStatePair> pairs = new ArrayList<>();
      for (SLine l : lines) {
          pairs.add(new BatchLineCommand.LineStatePair(l, l.copy(), null));
      }

      BatchLineCommand cmd = new BatchLineCommand(
              appState.getCanvasState(),
              canvas,
              BatchLineCommand.Action.DELETE,
              pairs
      );
      appState.getHistoryManager().push(cmd);
  }
  ```

---

### Task 4: Implement Drag-Rect Intersection and Multi-line Dragging in SelectTool

**Files:**
* Modify: [SelectTool.java](file:///c:/Users/DELL/Work/Java/Tool%20Projects/Portrai-Tool/src/main/java/tools/SelectTool.java)
* Create: [LineSelectionTest.java](file:///c:/Users/DELL/Work/Java/Tool%20Projects/Portrai-Tool/src/test/java/tools/LineSelectionTest.java)

- [ ] **Step 1: Create LineSelectionTest for Rect-Segment Intersection**
  Create `src/test/java/tools/LineSelectionTest.java`:
  ```java
  package tools;

  import org.junit.jupiter.api.Test;
  import java.awt.Point;
  import java.awt.geom.Line2D;
  import static org.junit.jupiter.api.Assertions.*;

  class LineSelectionTest {
      private boolean lineIntersectsRect(Point a, Point b, double rx, double ry, double rw, double rh) {
          // Check if either point is inside
          if (a.x >= rx && a.x <= rx + rw && a.y >= ry && a.y <= ry + rh) return true;
          if (b.x >= rx && b.x <= rx + rw && b.y >= ry && b.y <= ry + rh) return true;

          // Check if segment AB intersects any of the 4 borders
          Line2D.Double segment = new Line2D.Double(a.x, a.y, b.x, b.y);
          Line2D.Double top = new Line2D.Double(rx, ry, rx + rw, ry);
          Line2D.Double bottom = new Line2D.Double(rx, ry + rh, rx + rw, ry + rh);
          Line2D.Double left = new Line2D.Double(rx, ry, rx, ry + rh);
          Line2D.Double right = new Line2D.Double(rx + rw, ry, rx + rw, ry + rh);

          return segment.intersectsLine(top) || segment.intersectsLine(bottom) || 
                 segment.intersectsLine(left) || segment.intersectsLine(right);
      }

      @Test
      void testLineIntersectsRect() {
          Point insideA = new Point(10, 10);
          Point insideB = new Point(20, 20);
          assertTrue(lineIntersectsRect(insideA, insideB, 0, 0, 30, 30));

          Point outsideA = new Point(-10, -10);
          Point outsideB = new Point(40, 40); // Cuts across rect diagonal
          assertTrue(lineIntersectsRect(outsideA, outsideB, 0, 0, 30, 30));

          Point parallelOutsideA = new Point(50, 0);
          Point parallelOutsideB = new Point(50, 30);
          assertFalse(lineIntersectsRect(parallelOutsideA, parallelOutsideB, 0, 0, 30, 30));
      }
  }
  ```

- [ ] **Step 2: Add Rect-Segment helper method in SelectTool.java**
  Add helper method inside `SelectTool.java` before nested class definition (lines 441):
  ```java
  private boolean lineIntersectsRect(Point a, Point b, double rx, double ry, double rw, double rh) {
      if (a.x >= rx && a.x <= rx + rw && a.y >= ry && a.y <= ry + rh) return true;
      if (b.x >= rx && b.x <= rx + rw && b.y >= ry && b.y <= ry + rh) return true;

      java.awt.geom.Line2D.Double segment = new java.awt.geom.Line2D.Double(a.x, a.y, b.x, b.y);
      java.awt.geom.Line2D.Double top = new java.awt.geom.Line2D.Double(rx, ry, rx + rw, ry);
      java.awt.geom.Line2D.Double bottom = new java.awt.geom.Line2D.Double(rx, ry + rh, rx + rw, ry + rh);
      java.awt.geom.Line2D.Double left = new java.awt.geom.Line2D.Double(rx, ry, rx, ry + rh);
      java.awt.geom.Line2D.Double right = new java.awt.geom.Line2D.Double(rx + rw, ry, rx + rw, ry + rh);

      return segment.intersectsLine(top) || segment.intersectsLine(bottom) || 
             segment.intersectsLine(left) || segment.intersectsLine(right);
  }
  ```

- [ ] **Step 3: Modify selection box handling in `handleDragRelease`**
  Modify selection logic inside `handleDragRelease` (lines 223-253) to support line drag selection (intersection):
  ```java
  private void handleDragRelease(MouseEvent e, AppState appState, ImageCanvas canvas) {
      boolean isShift = (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0;
      boolean isCtrl  = (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK)  != 0;

      int offsetX = appState.getCanvasState().getImageOffsetX();
      int offsetY = appState.getCanvasState().getImageOffsetY();
      float zoom  = appState.getCurrentZoom();

      int sx = Math.min(dragStart.x, dragCurrent.x);
      int sy = Math.min(dragStart.y, dragCurrent.y);
      int sw = Math.abs(dragCurrent.x - dragStart.x);
      int sh = Math.abs(dragCurrent.y - dragStart.y);

      float ix1 = (sx - offsetX) / zoom;
      float iy1 = (sy - offsetY) / zoom;
      float ix2 = ((sx + sw) - offsetX) / zoom;
      float iy2 = ((sy + sh) - offsetY) / zoom;

      double rx = ix1;
      double ry = iy1;
      double rw = ix2 - ix1;
      double rh = iy2 - iy1;

      Rectangle2D selRect = new Rectangle2D(ix1, iy1, ix2 - ix1, iy2 - iy1);

      // Check points
      LinkedHashSet<SPoint> matchedPoints = new LinkedHashSet<>();
      for (SPoint p : appState.getCanvasState().getStickyPoints()) {
          if (selRect.contains(p.X, p.Y)) {
              matchedPoints.add(p);
          }
      }

      // Check lines
      LinkedHashSet<SLine> matchedLines = new LinkedHashSet<>();
      for (SLine l : appState.getCanvasState().getLines()) {
          if (lineIntersectsRect(l.startPoint, l.endPoint, rx, ry, rw, rh)) {
              matchedLines.add(l);
          }
      }

      appState.getCanvasState().setSelectedGrid(null);

      if (isCtrl) {
          // Toggle selection
          LinkedHashSet<SPoint> newPoints = new LinkedHashSet<>(appState.getCanvasState().getSelectedPoints());
          for (SPoint p : matchedPoints) {
              if (newPoints.contains(p)) newPoints.remove(p);
              else newPoints.add(p);
          }
          appState.getCanvasState().setSelectedPoints(newPoints);

          LinkedHashSet<SLine> newLines = new LinkedHashSet<>(appState.getCanvasState().getSelectedLines());
          for (SLine l : matchedLines) {
              if (newLines.contains(l)) newLines.remove(l);
              else newLines.add(l);
          }
          appState.getCanvasState().setSelectedLines(newLines);
      } else if (isShift) {
          // Add to selection
          LinkedHashSet<SPoint> newPoints = new LinkedHashSet<>(appState.getCanvasState().getSelectedPoints());
          newPoints.addAll(matchedPoints);
          appState.getCanvasState().setSelectedPoints(newPoints);

          LinkedHashSet<SLine> newLines = new LinkedHashSet<>(appState.getCanvasState().getSelectedLines());
          newLines.addAll(matchedLines);
          appState.getCanvasState().setSelectedLines(newLines);
      } else {
          // Replace selection
          if (!matchedPoints.isEmpty()) {
              appState.getCanvasState().clearLineSelection();
              appState.getCanvasState().setSelectedPoints(matchedPoints);
          } else if (!matchedLines.isEmpty()) {
              appState.getCanvasState().clearPointSelection();
              appState.getCanvasState().setSelectedLines(matchedLines);
          } else {
              appState.getCanvasState().clearPointSelection();
              appState.getCanvasState().clearLineSelection();
          }
      }
      canvas.repaint();
  }
  ```

- [ ] **Step 4: Modify `handleClickRelease` to support Shift/Ctrl modifiers on Line selection**
  Modify lines 351-366 to use line selection set APIs:
  ```java
          if (!lineCandidates.isEmpty()) {
              SLine toSelect = null;
              Set<SLine> currentSelected = appState.getCanvasState().getSelectedLines();
              if (lineCandidates.size() > 1) {
                  List<SLine> unselected = new ArrayList<>();
                  for (SLine l : lineCandidates) {
                      if (!currentSelected.contains(l)) unselected.add(l);
                  }
                  if (!unselected.isEmpty()) {
                      toSelect = unselected.get(random.nextInt(unselected.size()));
                  } else {
                      SLine last = appState.getCanvasState().getSelectedLine();
                      List<SLine> others = new ArrayList<>(lineCandidates);
                      others.remove(last);
                      if (!others.isEmpty()) {
                          toSelect = others.get(random.nextInt(others.size()));
                      } else {
                          toSelect = last;
                      }
                  }
              } else {
                  toSelect = lineCandidates.get(0);
              }

              if (isCtrl) {
                  LinkedHashSet<SLine> newLines = new LinkedHashSet<>(currentSelected);
                  if (newLines.contains(toSelect)) {
                      appState.getCanvasState().removeFromSelection(toSelect);
                  } else {
                      appState.getCanvasState().setSelectedGrid(null);
                      appState.getCanvasState().clearPointSelection();
                      appState.getCanvasState().addToSelection(toSelect);
                  }
              } else if (isShift) {
                  appState.getCanvasState().setSelectedGrid(null);
                  appState.getCanvasState().clearPointSelection();
                  appState.getCanvasState().addToSelection(toSelect);
              } else {
                  appState.getCanvasState().setSelectedGrid(null);
                  appState.getCanvasState().clearPointSelection();
                  appState.getCanvasState().setSelectedLine(toSelect);
              }
              canvas.repaint();
              return;
          }
  ```

- [ ] **Step 5: Modify active handle checks and drag translations in SelectTool**
  Modify `onMouseMoved` (lines 47-75) to restrict selection handles:
  ```java
      @Override
      public void onMouseMoved(MouseEvent e, AppState appState, ImageCanvas canvas) {
          Set<SLine> selectedLines = appState.getCanvasState().getSelectedLines();
          if (!selectedLines.isEmpty()) {
              float zoom = appState.getCurrentZoom();
              int ox = appState.getCanvasState().getImageOffsetX();
              int oy = appState.getCanvasState().getImageOffsetY();

              Point mouseLoc = new Point(
                  Math.round((float)(e.getX() - ox) / zoom),
                  Math.round((float)(e.getY() - oy) / zoom)
              );

              double screenRadius = 15.0 / zoom; // 30px active zone

              if (selectedLines.size() == 1) {
                  SLine selectedLine = selectedLines.iterator().next();
                  Point midPoint = new Point(
                      (selectedLine.startPoint.x + selectedLine.endPoint.x) / 2,
                      (selectedLine.startPoint.y + selectedLine.endPoint.y) / 2
                  );

                  if (mouseLoc.distance(selectedLine.startPoint) <= screenRadius ||
                      mouseLoc.distance(selectedLine.endPoint) <= screenRadius ||
                      mouseLoc.distance(midPoint) <= screenRadius) {
                      canvas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                      return;
                  }
              } else {
                  // Only allow midpoint handle drag
                  for (SLine selectedLine : selectedLines) {
                      Point midPoint = new Point(
                          (selectedLine.startPoint.x + selectedLine.endPoint.x) / 2,
                          (selectedLine.startPoint.y + selectedLine.endPoint.y) / 2
                      );
                      if (mouseLoc.distance(midPoint) <= screenRadius) {
                          canvas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                          return;
                      }
                  }
              }
          }
          canvas.setCursor(Cursor.getDefaultCursor());
      }
  ```

  Modify `onMousePressed` (lines 78-127):
  ```java
      // Handle drag tracking for batch editing
      private List<BatchLineCommand.LineStatePair> draggingPairs = new ArrayList<>();
      private Point dragStartMouse = null;

      @Override
      public void onMousePressed(MouseEvent e, AppState appState, ImageCanvas canvas) {
          if (e.getButton() == MouseEvent.BUTTON1) {
              float zoom = appState.getCurrentZoom();
              int ox = appState.getCanvasState().getImageOffsetX();
              int oy = appState.getCanvasState().getImageOffsetY();

              Set<SLine> selectedLines = appState.getCanvasState().getSelectedLines();
              if (!selectedLines.isEmpty()) {
                  Point pMouse = new Point(
                      Math.round((float)(e.getX() - ox) / zoom),
                      Math.round((float)(e.getY() - oy) / zoom)
                  );
                  double screenRadius = 15.0 / zoom;

                  if (selectedLines.size() == 1) {
                      SLine selectedLine = selectedLines.iterator().next();
                      Point midPoint = new Point(
                          (selectedLine.startPoint.x + selectedLine.endPoint.x) / 2,
                          (selectedLine.startPoint.y + selectedLine.endPoint.y) / 2
                      );

                      if (pMouse.distance(selectedLine.startPoint) <= screenRadius) {
                          activeHandleIndex = 0;
                          draggingLine = selectedLine;
                          originalDraggingLineState = selectedLine.copy();
                          canvas.repaint();
                          return;
                      } else if (pMouse.distance(selectedLine.endPoint) <= screenRadius) {
                          activeHandleIndex = 1;
                          draggingLine = selectedLine;
                          originalDraggingLineState = selectedLine.copy();
                          canvas.repaint();
                          return;
                      } else if (pMouse.distance(midPoint) <= screenRadius) {
                          activeHandleIndex = 2;
                          draggingLine = selectedLine;
                          originalDraggingLineState = selectedLine.copy();
                          startPointOffset = new Point(selectedLine.startPoint.x - pMouse.x, selectedLine.startPoint.y - pMouse.y);
                          endPointOffset = new Point(selectedLine.endPoint.x - pMouse.x, selectedLine.endPoint.y - pMouse.y);
                          canvas.repaint();
                          return;
                      }
                  } else {
                      // Multi-select mode: Check midpoint handles only
                      for (SLine l : selectedLines) {
                          Point midPoint = new Point(
                              (l.startPoint.x + l.endPoint.x) / 2,
                              (l.startPoint.y + l.endPoint.y) / 2
                          );
                          if (pMouse.distance(midPoint) <= screenRadius) {
                              activeHandleIndex = 3; // Batch midpoint drag marker
                              dragStartMouse = new Point(pMouse);
                              draggingPairs.clear();
                              for (SLine active : selectedLines) {
                                  draggingPairs.add(new BatchLineCommand.LineStatePair(active, active.copy(), active.copy()));
                              }
                              canvas.repaint();
                              return;
                          }
                      }
                  }
              }

              // Normal drag init
              dragStart   = e.getPoint();
              dragCurrent = e.getPoint();
              isDragging  = false;
          }
      }
  ```

  Modify `onMouseDragged` (lines 130-181) to translate all lines:
  ```java
      @Override
      public void onMouseDragged(MouseEvent e, AppState appState, ImageCanvas canvas) {
          float zoom = appState.getCurrentZoom();
          int ox = appState.getCanvasState().getImageOffsetX();
          int oy = appState.getCanvasState().getImageOffsetY();

          if (activeHandleIndex == 3 && dragStartMouse != null && !draggingPairs.isEmpty()) {
              int mouseX = Math.round((float)(e.getX() - ox) / zoom);
              int mouseY = Math.round((float)(e.getY() - oy) / zoom);

              java.awt.image.BufferedImage img = canvas.getBackgroundImage();
              if (img != null) {
                  mouseX = Math.max(0, Math.min(img.getWidth() - 1, mouseX));
                  mouseY = Math.max(0, Math.min(img.getHeight() - 1, mouseY));
              }

              int dx = mouseX - dragStartMouse.x;
              int dy = mouseY - dragStartMouse.y;

              for (BatchLineCommand.LineStatePair pair : draggingPairs) {
                  pair.line.startPoint.setLocation(pair.oldState.startPoint.x + dx, pair.oldState.startPoint.y + dy);
                  pair.line.endPoint.setLocation(pair.oldState.endPoint.x + dx, pair.oldState.endPoint.y + dy);
              }
              canvas.repaint();
              return;
          }

          if (draggingLine != null && activeHandleIndex != -1) {
              int dragX = Math.round((float)(e.getX() - ox) / zoom);
              int dragY = Math.round((float)(e.getY() - oy) / zoom);

              java.awt.image.BufferedImage img = canvas.getBackgroundImage();
              if (img != null) {
                  dragX = Math.max(0, Math.min(img.getWidth() - 1, dragX));
                  dragY = Math.max(0, Math.min(img.getHeight() - 1, dragY));
              }

              if (activeHandleIndex == 0) {
                  draggingLine.startPoint.setLocation(dragX, dragY);
              } else if (activeHandleIndex == 1) {
                  draggingLine.endPoint.setLocation(dragX, dragY);
              } else if (activeHandleIndex == 2) {
                  int newStartX = dragX + startPointOffset.x;
                  int newStartY = dragY + startPointOffset.y;
                  int newEndX = dragX + endPointOffset.x;
                  int newEndY = dragY + endPointOffset.y;

                  if (img != null) {
                      int w = img.getWidth() - 1;
                      int h = img.getHeight() - 1;
                      if (newStartX >= 0 && newStartX <= w &&
                          newStartY >= 0 && newStartY <= h &&
                          newEndX >= 0 && newEndX <= w &&
                          newEndY >= 0 && newEndY <= h) {
                          draggingLine.startPoint.setLocation(newStartX, newStartY);
                          draggingLine.endPoint.setLocation(newEndX, newEndY);
                      }
                  } else {
                      draggingLine.startPoint.setLocation(newStartX, newStartY);
                      draggingLine.endPoint.setLocation(newEndX, newEndY);
                  }
              }
              canvas.repaint();
              return;
          }

          if (dragStart == null) return;
          dragCurrent = e.getPoint();
          double dist = dragStart.distance(dragCurrent);
          if (dist >= DRAG_THRESHOLD_PX) {
              isDragging = true;
          }
          if (isDragging) canvas.repaint();
      }
  ```

  Modify `onMouseReleased` (lines 184-204):
  ```java
      @Override
      public void onMouseReleased(MouseEvent e, AppState appState, ImageCanvas canvas) {
          if (activeHandleIndex == 3 && !draggingPairs.isEmpty()) {
              boolean moved = false;
              List<BatchLineCommand.LineStatePair> finalPairs = new ArrayList<>();
              for (BatchLineCommand.LineStatePair pair : draggingPairs) {
                  if (!pair.line.startPoint.equals(pair.oldState.startPoint) ||
                      !pair.line.endPoint.equals(pair.oldState.endPoint)) {
                      moved = true;
                  }
                  finalPairs.add(new BatchLineCommand.LineStatePair(pair.line, pair.oldState, pair.line.copy()));
              }
              if (moved) {
                  BatchLineCommand cmd = new BatchLineCommand(
                          appState.getCanvasState(),
                          canvas,
                          BatchLineCommand.Action.EDIT,
                          finalPairs
                  );
                  appState.getHistoryManager().push(cmd);
              }
              draggingPairs.clear();
              dragStartMouse = null;
              activeHandleIndex = -1;
              canvas.repaint();
              return;
          }

          if (draggingLine != null && originalDraggingLineState != null) {
              boolean moved = !draggingLine.startPoint.equals(originalDraggingLineState.startPoint) ||
                              !draggingLine.endPoint.equals(originalDraggingLineState.endPoint);
              if (moved) {
                  LineCommand cmd = new LineCommand(
                      appState.getCanvasState(),
                      canvas,
                      draggingLine,
                      LineCommand.Action.EDIT,
                      originalDraggingLineState,
                      draggingLine.copy()
                  );
                  appState.getHistoryManager().push(cmd);
              }
              draggingLine = null;
              originalDraggingLineState = null;
              activeHandleIndex = -1;
              canvas.repaint();
              return;
          }
  ```

---

### Task 5: Adapt LineTool parity and ImageCanvas Redraw updates

**Files:**
* Modify: [LineTool.java](file:///c:/Users/DELL/Work/Java/Tool%20Projects/Portrai-Tool/src/main/java/tools/LineTool.java)
* Modify: [ImageCanvas.java](file:///c:/Users/DELL/Work/Java/Tool%20Projects/Portrai-Tool/src/main/java/ui/canvas/ImageCanvas.java)

- [ ] **Step 1: Align handle hover feedback in LineTool**
  Modify LineTool's `onMouseMoved` to ignore endpoint handles if in multi-select (similar to Task 4, Step 5):
  ```java
      @Override
      public void onMouseMoved(MouseEvent e, AppState appState, ImageCanvas canvas) {
          Set<SLine> selectedLines = appState.getCanvasState().getSelectedLines();
          if (!selectedLines.isEmpty()) {
              float zoom = appState.getCurrentZoom();
              int ox = appState.getCanvasState().getImageOffsetX();
              int oy = appState.getCanvasState().getImageOffsetY();

              Point mouseLoc = new Point(
                  Math.round((float)(e.getX() - ox) / zoom),
                  Math.round((float)(e.getY() - oy) / zoom)
              );

              double screenRadius = 15.0 / zoom;

              if (selectedLines.size() == 1) {
                  SLine selectedLine = selectedLines.iterator().next();
                  Point midPoint = new Point(
                      (selectedLine.startPoint.x + selectedLine.endPoint.x) / 2,
                      (selectedLine.startPoint.y + selectedLine.endPoint.y) / 2
                  );

                  if (mouseLoc.distance(selectedLine.startPoint) <= screenRadius ||
                      mouseLoc.distance(selectedLine.endPoint) <= screenRadius ||
                      mouseLoc.distance(midPoint) <= screenRadius) {
                      canvas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                      return;
                  }
              } else {
                  for (SLine selectedLine : selectedLines) {
                      Point midPoint = new Point(
                          (selectedLine.startPoint.x + selectedLine.endPoint.x) / 2,
                          (selectedLine.startPoint.y + selectedLine.endPoint.y) / 2
                      );
                      if (mouseLoc.distance(midPoint) <= screenRadius) {
                          canvas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                          return;
                      }
                  }
              }
          }
          canvas.setCursor(Cursor.getDefaultCursor());
      }
  ```

- [ ] **Step 2: Align `onMousePressed` drag check in LineTool**
  Use the same multi-drag detection in LineTool's `onMousePressed`:
  ```java
      private List<BatchLineCommand.LineStatePair> draggingPairs = new ArrayList<>();
      private Point dragStartMouse = null;

      @Override
      public void onMousePressed(MouseEvent e, AppState appState, ImageCanvas canvas) {
          if (e.getButton() == MouseEvent.BUTTON1) {
              float zoom = appState.getCurrentZoom();
              int ox = appState.getCanvasState().getImageOffsetX();
              int oy = appState.getCanvasState().getImageOffsetY();

              Set<SLine> selectedLines = appState.getCanvasState().getSelectedLines();
              if (!selectedLines.isEmpty()) {
                  Point pMouse = new Point(
                      Math.round((float)(e.getX() - ox) / zoom),
                      Math.round((float)(e.getY() - oy) / zoom)
                  );
                  double screenRadius = 15.0 / zoom;

                  if (selectedLines.size() == 1) {
                      SLine selectedLine = selectedLines.iterator().next();
                      Point midPoint = new Point(
                          (selectedLine.startPoint.x + selectedLine.endPoint.x) / 2,
                          (selectedLine.startPoint.y + selectedLine.endPoint.y) / 2
                      );

                      if (pMouse.distance(selectedLine.startPoint) <= screenRadius) {
                          activeHandleIndex = 0;
                          draggingLine = selectedLine;
                          originalDraggingLineState = selectedLine.copy();
                          canvas.repaint();
                          return;
                      } else if (pMouse.distance(selectedLine.endPoint) <= screenRadius) {
                          activeHandleIndex = 1;
                          draggingLine = selectedLine;
                          originalDraggingLineState = selectedLine.copy();
                          canvas.repaint();
                          return;
                      } else if (pMouse.distance(midPoint) <= screenRadius) {
                          activeHandleIndex = 2;
                          draggingLine = selectedLine;
                          originalDraggingLineState = selectedLine.copy();
                          startPointOffset = new Point(selectedLine.startPoint.x - pMouse.x, selectedLine.startPoint.y - pMouse.y);
                          endPointOffset = new Point(selectedLine.endPoint.x - pMouse.x, selectedLine.endPoint.y - pMouse.y);
                          canvas.repaint();
                          return;
                      }
                  } else {
                      for (SLine l : selectedLines) {
                          Point midPoint = new Point(
                              (l.startPoint.x + l.endPoint.x) / 2,
                              (l.startPoint.y + l.endPoint.y) / 2
                          );
                          if (pMouse.distance(midPoint) <= screenRadius) {
                              activeHandleIndex = 3;
                              dragStartMouse = new Point(pMouse);
                              draggingPairs.clear();
                              for (SLine active : selectedLines) {
                                  draggingPairs.add(new BatchLineCommand.LineStatePair(active, active.copy(), active.copy()));
                              }
                              canvas.repaint();
                              return;
                          }
                      }
                  }
              }
              
              // Fallback drawing new line
              int x = Math.round((float)(e.getX() - ox) / zoom);
              int y = Math.round((float)(e.getY() - oy) / zoom);
              java.awt.image.BufferedImage img = canvas.getBackgroundImage();
              if (img != null && (x < 0 || x >= img.getWidth() || y < 0 || y >= img.getHeight())) {
                  return;
              }
              startPoint = new Point(x, y);
              currentDrag = new Point(x, y);
              isDrawing = true;
          }
      }
  ```

- [ ] **Step 3: Align `onMouseDragged` and `onMouseReleased` in LineTool**
  Update `onMouseDragged` and `onMouseReleased` in LineTool to copy SelectTool's activeHandleIndex == 3 logic.
  In `onMouseDragged` (add at start of method):
  ```java
          if (activeHandleIndex == 3 && dragStartMouse != null && !draggingPairs.isEmpty()) {
              int mouseX = Math.round((float)(e.getX() - ox) / zoom);
              int mouseY = Math.round((float)(e.getY() - oy) / zoom);
              java.awt.image.BufferedImage img = canvas.getBackgroundImage();
              if (img != null) {
                  mouseX = Math.max(0, Math.min(img.getWidth() - 1, mouseX));
                  mouseY = Math.max(0, Math.min(img.getHeight() - 1, mouseY));
              }
              int dx = mouseX - dragStartMouse.x;
              int dy = mouseY - dragStartMouse.y;
              for (BatchLineCommand.LineStatePair pair : draggingPairs) {
                  pair.line.startPoint.setLocation(pair.oldState.startPoint.x + dx, pair.oldState.startPoint.y + dy);
                  pair.line.endPoint.setLocation(pair.oldState.endPoint.x + dx, pair.oldState.endPoint.y + dy);
              }
              canvas.repaint();
              return;
          }
  ```
  In `onMouseReleased` (add at start of method):
  ```java
          if (activeHandleIndex == 3 && !draggingPairs.isEmpty()) {
              boolean moved = false;
              List<BatchLineCommand.LineStatePair> finalPairs = new ArrayList<>();
              for (BatchLineCommand.LineStatePair pair : draggingPairs) {
                  if (!pair.line.startPoint.equals(pair.oldState.startPoint) ||
                      !pair.line.endPoint.equals(pair.oldState.endPoint)) {
                      moved = true;
                  }
                  finalPairs.add(new BatchLineCommand.LineStatePair(pair.line, pair.oldState, pair.line.copy()));
              }
              if (moved) {
                  BatchLineCommand cmd = new BatchLineCommand(
                          appState.getCanvasState(),
                          canvas,
                          BatchLineCommand.Action.EDIT,
                          finalPairs
                  );
                  appState.getHistoryManager().push(cmd);
              }
              draggingPairs.clear();
              dragStartMouse = null;
              activeHandleIndex = -1;
              canvas.repaint();
              return;
          }
  ```

- [ ] **Step 4: Update RenderUtils.drawLines and ImageCanvas.java**
  Modify [RenderUtils.java](file:///c:/Users/DELL/Work/Java/Tool%20Projects/Portrai-Tool/src/main/java/ui/canvas/RenderUtils.java) lines 287-304:
  ```java
  public static void drawLines(Graphics2D g2d, List<userpackage.SLine> lines, java.util.Set<userpackage.SLine> selectedLines, float zoom) {
      if (lines == null || lines.isEmpty()) return;

      Stroke oldStroke = g2d.getStroke();
      Color oldColor = g2d.getColor();
      Object oldAntialiasing = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);

      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      for (userpackage.SLine line : lines) {
          // Draw segment line
          g2d.setColor(line.strokeColor);
          g2d.setStroke(new BasicStroke(line.strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
          g2d.drawLine(line.startPoint.x, line.startPoint.y, line.endPoint.x, line.endPoint.y);

          // Draw handles if selected
          if (selectedLines != null && selectedLines.contains(line)) {
  ```

  Modify [ImageCanvas.java](file:///c:/Users/DELL/Work/Java/Tool%20Projects/Portrai-Tool/src/main/java/ui/canvas/ImageCanvas.java) line 583 to pass `getSelectedLines()` instead of `getSelectedLine()`:
  ```java
          RenderUtils.drawLines(g2d, appState.getCanvasState().getLines(), appState.getCanvasState().getSelectedLines(), zoom);
  ```
