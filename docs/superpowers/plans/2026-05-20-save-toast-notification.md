# Save File Toast Notification Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace blocking success JOptionPane dialogs for save/export operations with a transient, auto-hiding toast notification.

**Architecture:** Create a custom Swing `ToastNotification` component rendering a dark semi-transparent rounded pane containing a green checkmark icon and text. It dynamically calculates its position inside the `MainFrame`'s `JLayeredPane.POPUP_LAYER` at the bottom-right corner and handles resize events.

**Tech Stack:** Java, Swing (JPanel, JLayeredPane, JTimer, ComponentListener).

---

### Task 1: Implement ToastNotification Component

**Files:**
- Create: `src/main/java/ui/components/ToastNotification.java`
- Create: `src/test/java/ui/components/ToastNotificationTest.java`

- [ ] **Step 1: Write the failing unit test**
  Create the test file to verify active frame resolution and thread safety.
  ```java
  package ui.components;

  import org.junit.jupiter.api.Test;
  import javax.swing.JFrame;
  import static org.junit.jupiter.api.Assertions.*;

  class ToastNotificationTest {

      @Test
      void testFindMainFrameReturnsActiveVisibleFrame() {
          JFrame frame = new JFrame("Test Frame");
          frame.setVisible(true);
          try {
              JFrame resolved = ToastNotification.findMainFrame();
              assertNotNull(resolved, "Should find the active frame");
              assertEquals("Test Frame", resolved.getTitle());
          } finally {
              frame.dispose();
          }
      }
  }
  ```

- [ ] **Step 2: Run tests to verify failure**
  Run: `mvn test`
  Expected: Compilation failure because `ToastNotification` class and `findMainFrame` method do not exist yet.

- [ ] **Step 3: Implement ToastNotification class**
  Create `src/main/java/ui/components/ToastNotification.java` with the custom layout, paint operations, sizing, and position updates.
  ```java
  package ui.components;

  import ui.MainFrame;
  import javax.swing.*;
  import java.awt.*;
  import java.awt.event.ComponentAdapter;
  import java.awt.event.ComponentEvent;

  public class ToastNotification extends JPanel {
      private final String message;
      private final JFrame frame;
      private final Timer timer;
      private final ComponentAdapter resizeListener;

      public ToastNotification(JFrame frame, String message) {
          this.frame = frame;
          this.message = message;
          
          setOpaque(false);
          setLayout(new BorderLayout(12, 0));
          setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
          
          JLabel label = new JLabel(message);
          label.setForeground(Color.WHITE);
          label.setFont(new Font("SansSerif", Font.BOLD, 12));
          add(label, BorderLayout.CENTER);
          
          JPanel iconPanel = new JPanel() {
              @Override
              protected void paintComponent(Graphics g) {
                  super.paintComponent(g);
                  Graphics2D g2 = (Graphics2D) g.create();
                  g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                  
                  g2.setColor(new Color(46, 125, 50));
                  g2.fillOval(0, 0, 16, 16);
                  
                  g2.setColor(Color.WHITE);
                  g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                  g2.drawLine(4, 8, 7, 11);
                  g2.drawLine(7, 11, 12, 5);
                  g2.dispose();
              }
              
              @Override
              public Dimension getPreferredSize() {
                  return new Dimension(16, 16);
              }
          };
          iconPanel.setOpaque(false);
          add(iconPanel, BorderLayout.WEST);
          
          timer = new Timer(2000, e -> dismiss());
          timer.setRepeats(false);
          
          resizeListener = new ComponentAdapter() {
              @Override
              public void componentResized(ComponentEvent e) {
                  updateBounds();
              }
          };
      }
      
      @Override
      protected void paintComponent(Graphics g) {
          Graphics2D g2 = (Graphics2D) g.create();
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          
          g2.setColor(new Color(30, 30, 30, 220));
          g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
          
          g2.setColor(new Color(255, 255, 255, 30));
          g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
          g2.dispose();
          super.paintComponent(g);
      }
      
      private void updateBounds() {
          Dimension toastSize = getPreferredSize();
          int x = frame.getWidth() - toastSize.width - 20;
          int y = frame.getHeight() - toastSize.height - 20;
          
          setBounds(x, y, toastSize.width, toastSize.height);
          revalidate();
      }
      
      public void showToast() {
          timer.start();
          frame.getLayeredPane().add(this, JLayeredPane.POPUP_LAYER);
          frame.addComponentListener(resizeListener);
          updateBounds();
          frame.getLayeredPane().repaint();
      }
      
      public void dismiss() {
          timer.stop();
          frame.removeComponentListener(resizeListener);
          Container parent = getParent();
          if (parent != null) {
              parent.remove(this);
              parent.repaint();
          }
      }
      
      public static void show(String message) {
          SwingUtilities.invokeLater(() -> {
              JFrame frame = findMainFrame();
              if (frame != null) {
                  ToastNotification toast = new ToastNotification(frame, message);
                  toast.showToast();
              }
          });
      }
      
      public static JFrame findMainFrame() {
          for (Frame f : Frame.getFrames()) {
              if (f instanceof JFrame && f.isVisible()) {
                  return (JFrame) f;
              }
          }
          return null;
      }
  }
  ```

- [ ] **Step 4: Run tests to verify they pass**
  Run: `mvn test`
  Expected: BUILD SUCCESS with all 15 tests passing.

---

### Task 2: Replace Success Dialogs with Toast Notifications

**Files:**
- Modify: `src/main/java/ui/MainFrame.java`
- Modify: `src/main/java/workers/SavePointMapWorker.java`
- Modify: `src/main/java/workers/SaveWorker.java`
- Modify: `src/main/java/workers/ExportMatrixWorker.java`

- [ ] **Step 1: Update MainFrame.java**
  Add imports and replace dialogs with Toast calls:
  ```java
  // Import
  import ui.components.ToastNotification;
  ```
  Around line 789:
  ```diff
  -               JOptionPane.showMessageDialog(this, "Project saved successfully!");
  +               ToastNotification.show("Project saved successfully!");
  ```
  Around line 957:
  ```diff
  -                 JOptionPane.showMessageDialog(this, "Successfully exported to " + file.getName(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
  +                 ToastNotification.show("Successfully exported to " + file.getName());
  ```

- [ ] **Step 2: Update SavePointMapWorker.java**
  Add imports and replace dialog:
  ```java
  // Import
  import ui.components.ToastNotification;
  ```
  Around line 243:
  ```diff
  -             JOptionPane.showMessageDialog(null, "Point map successfully saved to:\n" + outputFile.getAbsolutePath(), "Save Complete", JOptionPane.INFORMATION_MESSAGE);
  +             ToastNotification.show("Point map successfully saved to:\n" + outputFile.getAbsolutePath());
  ```

- [ ] **Step 3: Update SaveWorker.java**
  Add imports and replace dialog:
  ```java
  // Import
  import ui.components.ToastNotification;
  ```
  Around line 95:
  ```diff
  -             JOptionPane.showMessageDialog(null, "Image successfully saved to:\n" + outputFile.getAbsolutePath(), "Save Complete", JOptionPane.INFORMATION_MESSAGE);
  +             ToastNotification.show("Image successfully saved to:\n" + outputFile.getAbsolutePath());
  ```

- [ ] **Step 4: Update ExportMatrixWorker.java**
  Add imports and replace dialog:
  ```java
  // Import
  import ui.components.ToastNotification;
  ```
  Around line 82:
  ```diff
  -             JOptionPane.showMessageDialog(null, "Matrix Exported Successfully!", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
  +             ToastNotification.show("Matrix Exported Successfully!");
  ```

- [ ] **Step 5: Run tests and verify build**
  Run: `mvn clean test`
  Expected: BUILD SUCCESS with 15 passing tests.
