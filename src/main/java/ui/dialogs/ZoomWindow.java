package ui.dialogs;

import core.state.AppState;
import user.Enum.Direction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class ZoomWindow extends JWindow {

    private final AppState appState;
    private BufferedImage currentImage;
    
    private Point mousePosition = new Point(0, 0);
    private float zoomRate = 2.0f;
    private boolean showCross = true;
    private boolean showRuler = false;
    
    // Measurement caliper state
    private boolean measuring = false;
    private int xDistance = -35;
    private int yDistance = 35;
    private int rulerUnitPixels = 35; // Previously rA

    public ZoomWindow(Frame owner, AppState appState) {
        super(owner);
        this.appState = appState;
        
        setSize(400, 400); // Default, could be read from AppState later
        setAlwaysOnTop(true);
        setBackground(Color.DARK_GRAY);
        
        setupKeyBindings();
    }
    
    public void updateImage(BufferedImage image, Point mousePos) {
        this.currentImage = image;
        this.mousePosition = mousePos;
        repaint();
    }

    private void setupKeyBindings() {
        // To intercept keys, a JWindow must be focusable
        setFocusableWindowState(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!measuring) return;
                
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        yDistance += 5;
                        break;
                    case KeyEvent.VK_DOWN:
                        yDistance -= 5;
                        break;
                    case KeyEvent.VK_LEFT:
                        xDistance -= 5;
                        break;
                    case KeyEvent.VK_RIGHT:
                        xDistance += 5;
                        break;
                }
                repaint();
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        
        if (currentImage == null) return;
        
        Graphics2D g2 = (Graphics2D) g;
        
        int w = currentImage.getWidth();
        int h = currentImage.getHeight();
        
        int W = (int) (w * zoomRate);
        int H = (int) (h * zoomRate);
        
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        
        int xl = (int) (cx - zoomRate * mousePosition.x);
        int yl = (int) (cy - zoomRate * mousePosition.y);
        
        g2.drawImage(currentImage, xl, yl, W, H, this);
        
        if (showCross && !showRuler) {
            drawCross(g2, cx, cy);
        }
        
        if (showRuler) {
            drawRuler(g2, cx, cy);
        }
    }

    private void drawCross(Graphics2D g2, int cx, int cy) {
        g2.setXORMode(Color.WHITE);
        g2.drawOval(cx - 11, cy - 11, 20, 20);
        g2.drawLine(cx, cy - 5, cx, cy - 25);
        g2.drawLine(cx, cy + 5, cx, cy + 25);
        g2.drawLine(cx - 5, cy, cx - 25, cy);
        g2.drawLine(cx + 5, cy, cx + 25, cy);
        g2.setPaintMode();
    }

    private void drawRuler(Graphics2D g2, int cx, int cy) {
        g2.setXORMode(Color.WHITE);
        int width = getWidth();
        int height = getHeight();
        
        // Center lines
        g2.drawLine(cx, 0, cx, height);
        g2.drawLine(0, cy, width, cy);
        
        if (measuring) {
            drawMeasurementLines(g2, cx, cy, width, height);
        }
        g2.setPaintMode();
    }

    private void drawMeasurementLines(Graphics2D g2, int cx, int cy, int width, int height) {
        int y = cy - yDistance;
        g2.drawLine(0, y, width, y);
        
        float realYDist = yDistance / zoomRate;
        if (appState.isCmUnit()) {
            realYDist *= appState.getScale();
        }
        g2.drawString(String.format("%.2f", realYDist), cx - 100, y - 5);

        int x = cx + xDistance;
        g2.drawLine(x, 0, x, height);
        
        float realXDist = xDistance / zoomRate;
        if (appState.isCmUnit()) {
            realXDist *= appState.getScale();
        }
        g2.drawString(String.format("%.2f", realXDist), x + 5, cy - 100);
    }
    
    // API to control state from outside
    public void setZoomRate(float zoomRate) { this.zoomRate = zoomRate; repaint(); }
    public void toggleRuler() { this.showRuler = !showRuler; repaint(); }
    public void toggleMeasurement() { this.measuring = !measuring; repaint(); }
}
