package ui.canvas;

import core.state.AppState;
import tools.Tool;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import userpackage.SPoint;
import user.Enum.Direction;

public class ImageCanvas extends JPanel {

    private final AppState appState;
    private Tool activeTool;
    private BufferedImage backgroundImage;

    public ImageCanvas(AppState appState) {
        this.appState = appState;
        
        // Mouse Listeners that delegate to the active tool
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (activeTool != null) {
                    activeTool.onMousePressed(e, appState, ImageCanvas.this);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (activeTool != null) {
                    activeTool.onMouseReleased(e, appState, ImageCanvas.this);
                }
            }
        });
        
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (activeTool != null) {
                    activeTool.onMouseDragged(e, appState, ImageCanvas.this);
                }
            }
        });
    }

    public void setActiveTool(Tool tool) {
        this.activeTool = tool;
    }
    
    public Tool getActiveTool() {
        return activeTool;
    }

    public void setBackgroundImage(BufferedImage image) {
        this.backgroundImage = image;
        if (image != null) {
            this.setPreferredSize(new java.awt.Dimension(image.getWidth(), image.getHeight()));
            this.revalidate();
        }
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // 1. Draw Background Image
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, this);
        }
        
        // 2. Allow active tool to draw preview
        if (activeTool != null) {
            activeTool.onPaint(g2d, appState, this);
        }
        
        // 3. Draw Sticky Points
        for (SPoint p : appState.getCanvasState().getStickyPoints()) {
            g2d.setColor(p.c);
            g2d.fillRect(p.X - 1, p.Y - 1, 2, 2);
            // Label rendering simplified for now
            if (p.dr == Direction.EAST) {
                g2d.drawString(String.valueOf(p.id), p.X + 12, p.Y + 12);
            } else if (p.dr == Direction.WEST) {
                g2d.drawString(String.valueOf(p.id), p.X - 30, p.Y + 15);
            } else if (p.dr == Direction.SOUTH) {
                g2d.drawString(String.valueOf(p.id), p.X - 5, p.Y + 25);
            } else {
                g2d.drawString(String.valueOf(p.id), p.X - 5, p.Y - 12);
            }
        }
        
        // 4. Draw Grids
        for (SPoint grid : appState.getCanvasState().getGrids()) {
            g2d.setColor(grid.c);
            int gridSize = grid.id;
            int xR = grid.X;
            int yR = grid.Y;
            
            // Simplified grid rendering
            int width = this.getWidth() > 0 ? this.getWidth() : 800;
            int height = this.getHeight() > 0 ? this.getHeight() : 600;
            
            for (int x = xR; x < width; x += gridSize) {
                g2d.drawLine(x, 0, x, height);
            }
            for (int x = xR; x > 0; x -= gridSize) {
                g2d.drawLine(x, 0, x, height);
            }
            for (int y = yR; y < height; y += gridSize) {
                g2d.drawLine(0, y, width, y);
            }
            for (int y = yR; y > 0; y -= gridSize) {
                g2d.drawLine(0, y, width, y);
            }
        }
    }
}
