package core.fileio;

import ui.dialogs.ImagePreviewPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Injects a toggle button into JFileChooser toolbar to hide/show the Preview Panel.
 * Uses a similar injection pattern as GridOptionInjector for UI consistency.
 */
public class PreviewOptionInjector {

    public static void inject(JFileChooser chooser, ImagePreviewPanel previewPanel) {
        // Use a Timer to poll for the toolbar since UI might not be fully initialized
        Timer initTimer = new Timer(200, null);
        initTimer.addActionListener(new java.awt.event.ActionListener() {
            int attempts = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                attempts++;
                AbstractButton listBtn = findListViewButton(chooser);
                if (listBtn != null) {
                    initTimer.stop();
                    injectButton(chooser, listBtn, previewPanel);
                } else if (attempts > 20) {
                    initTimer.stop();
                }
            }
        });
        initTimer.start();
    }

    private static void injectButton(JFileChooser chooser, AbstractButton listBtn, ImagePreviewPanel previewPanel) {
        Container toolbar = listBtn.getParent();

        // Avoid double injection
        for (Component c : toolbar.getComponents()) {
            if ("PreviewViewBtn".equals(c.getName())) return;
        }

        JToggleButton previewBtn = new JToggleButton();
        previewBtn.setName("PreviewViewBtn");
        previewBtn.setToolTipText("Toggle Preview Panel");
        
        // Sync style with native buttons
        previewBtn.setBorderPainted(listBtn.isBorderPainted());
        previewBtn.setContentAreaFilled(listBtn.isContentAreaFilled());
        previewBtn.setFocusPainted(listBtn.isFocusPainted());
        previewBtn.setRolloverEnabled(listBtn.isRolloverEnabled());
        previewBtn.setOpaque(listBtn.isOpaque());
        previewBtn.setBorder(listBtn.getBorder());

        // Create an "Eye" icon or similar for Preview
        previewBtn.setIcon(createPreviewIcon());
        previewBtn.setSelected(true); // Default to visible

        previewBtn.addActionListener(e -> {
            chooser.setAccessory(previewBtn.isSelected() ? previewPanel : null);
            chooser.revalidate();
            chooser.repaint();
        });

        toolbar.add(previewBtn);
        toolbar.revalidate();
        toolbar.repaint();
    }

    private static AbstractButton findListViewButton(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof AbstractButton) {
                AbstractButton btn = (AbstractButton) comp;
                Action action = btn.getAction();
                String tooltip = btn.getToolTipText();

                if ((action != null && "viewTypeList".equals(action.getValue(Action.NAME))) ||
                        (tooltip != null && tooltip.toLowerCase().contains("list"))) {
                    return btn;
                }
            } else if (comp instanceof Container) {
                AbstractButton found = findListViewButton((Container) comp);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static Icon createPreviewIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(100, 100, 100));
                g2d.setStroke(new BasicStroke(1.2f));

                // Draw a simple eye-like shape
                int w = 12;
                int h = 8;
                int cx = x + 8;
                int cy = y + 8;
                
                // Eye outer
                g2d.drawArc(cx - w/2, cy - h/2, w, h, 0, 360);
                // Pupil
                g2d.fillOval(cx - 2, cy - 2, 4, 4);

                g2d.dispose();
            }
            @Override public int getIconWidth() { return 16; }
            @Override public int getIconHeight() { return 16; }
        };
    }
}
