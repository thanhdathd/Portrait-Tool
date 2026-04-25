package ui.canvas;

import core.state.AppState;
import tools.Tool;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import ui.dialogs.ZoomWindow;
import ui.CustomCursors;
import tools.HandTool;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import userpackage.SPoint;
import user.Enum.Direction;

public class ImageCanvas extends JPanel {

    private final AppState appState;
    private Tool activeTool;
    private Tool previousTool; // For spacebar toggle
    private final HandTool globalHandTool = new HandTool();
    private BufferedImage backgroundImage;
    private ZoomWindow zoomWindow;
    private static final int CHECKER_SIZE = 20;
    private boolean drawLabels = true;
    private boolean isShiftDown = false;
    private final java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public ImageCanvas(AppState appState) {
        this.appState = appState;
        setBackground(Color.LIGHT_GRAY);
        setFocusable(true);
        setAutoscrolls(true); // Enables auto-scrolling for drag events
        
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
                updateZoomWindow(e.getPoint());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                updateZoomWindow(e.getPoint());
            }
        });
        
        // KeyBindings for Spacebar to temporary toggle HandTool
        InputMap im = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = this.getActionMap();
        
        im.put(KeyStroke.getKeyStroke("pressed SPACE"), "spacePressed");
        am.put("spacePressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (activeTool != globalHandTool) {
                    previousTool = activeTool;
                    setActiveTool(globalHandTool);
                }
            }
        });

        im.put(KeyStroke.getKeyStroke("released SPACE"), "spaceReleased");
        am.put("spaceReleased", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (previousTool != null) {
                    setActiveTool(previousTool);
                    previousTool = null;
                }
            }
        });
        
        // Listen for SHIFT to update Zoom cursor
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                boolean shiftNow = (e.getID() == KeyEvent.KEY_PRESSED);
                if (isShiftDown != shiftNow) {
                    isShiftDown = shiftNow;
                    if (activeTool instanceof tools.ZoomCanvasTool) {
                        updateCursor();
                    }
                }
            }
            return false;
        });
    }

    public void setZoomWindow(ZoomWindow zoomWindow) {
        this.zoomWindow = zoomWindow;
    }

    private void updateZoomWindow(java.awt.Point p) {
        if (zoomWindow != null && zoomWindow.isVisible() && backgroundImage != null) {
            // zoomWindow expects coordinates relative to the unscaled image
            int offsetX = appState.getCanvasState().getImageOffsetX();
            int offsetY = appState.getCanvasState().getImageOffsetY();
            
            Point unscaledP = new Point(
                    Math.round((p.x - offsetX) / appState.getCurrentZoom()),
                    Math.round((p.y - offsetY) / appState.getCurrentZoom())
            );
            zoomWindow.updateImage(backgroundImage, unscaledP);
        }
    }

    public void setActiveTool(Tool newTool) {
        Tool oldTool = this.activeTool;
        this.activeTool = newTool;
        updateCursor();
        pcs.firePropertyChange("activeTool", oldTool, newTool);
    }
    
    public void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    
    public void updateCursor() {
        if (activeTool instanceof HandTool) {
            setCursor(CustomCursors.HAND_CURSOR);
        } else if (activeTool instanceof tools.StickTool) {
            setCursor(CustomCursors.STICK_CURSOR);
        } else if (activeTool instanceof tools.P2PTool) {
            setCursor(CustomCursors.P2P_CURSOR);
        } else if (activeTool instanceof tools.GridTool) {
            setCursor(CustomCursors.GRID_CURSOR);
        } else if (activeTool instanceof tools.ZoomCanvasTool) {
            boolean isZoomIn = ((tools.ZoomCanvasTool) activeTool).isZoomInMode();
            
            // Invert if SHIFT is currently held
            if (isShiftDown) {
                isZoomIn = !isZoomIn;
            }
            
            setCursor(isZoomIn ? CustomCursors.ZOOM_IN_CURSOR : CustomCursors.ZOOM_OUT_CURSOR);
            
        } else {
            setCursor(CustomCursors.DEFAULT_CURSOR);
        }
    }
    
    public Tool getActiveTool() {
        return activeTool;
    }

    @Override
    public Dimension getPreferredSize() {
        if (backgroundImage != null) {
            float zoom = appState.getCurrentZoom();
            int imgWidth = (int) (backgroundImage.getWidth() * zoom);
            int imgHeight = (int) (backgroundImage.getHeight() * zoom);
            
            Container parent = SwingUtilities.getUnwrappedParent(this);
            if (parent instanceof JViewport) {
                JViewport viewport = (JViewport) parent;
                if (imgWidth > viewport.getWidth() && imgHeight > viewport.getHeight()) {
                    return new Dimension(imgWidth, imgHeight);
                } else {
                    return new Dimension(viewport.getWidth(), viewport.getHeight());
                }
            }
            return new Dimension(imgWidth, imgHeight);
        }
        return super.getPreferredSize();
    }

    public void setDrawLabels(boolean drawLabels) {
        this.drawLabels = drawLabels;
    }

    public void setBackgroundImage(BufferedImage image) {
        this.backgroundImage = image;
        if (image != null) {
            this.revalidate();
        }
        this.repaint();
    }

    public BufferedImage getBackgroundImage() {
        return backgroundImage;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        // Draw Checkerboard Background
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        Container parent = SwingUtilities.getUnwrappedParent(this);
        if (parent instanceof JViewport) {
            viewWidth = Math.max(viewWidth, parent.getWidth());
            viewHeight = Math.max(viewHeight, parent.getHeight());
        }
        
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0, 0, viewWidth, viewHeight);
        g2d.setColor(Color.WHITE);
        for (int y = 0; y < viewHeight; y += CHECKER_SIZE) {
            for (int x = 0; x < viewWidth; x += CHECKER_SIZE) {
                if (((x / CHECKER_SIZE) ^ (y / CHECKER_SIZE)) % 2 == 0) {
                    g2d.fillRect(x, y, CHECKER_SIZE, CHECKER_SIZE);
                }
            }
        }
        
        // Apply Image Offset (screen coordinates)
        int offsetX = appState.getCanvasState().getImageOffsetX();
        int offsetY = appState.getCanvasState().getImageOffsetY();
        g2d.translate(offsetX, offsetY);
        
        // Apply Zoom Transform
        float zoom = appState.getCurrentZoom();
        g2d.scale(zoom, zoom);
        
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
            
            if (drawLabels) {
                // Adjust label font size back so it doesn't scale massively with zoom
                // Keep font size constant on screen
                Font originalFont = g2d.getFont();
                g2d.setFont(originalFont.deriveFont(originalFont.getSize() / zoom));
                
                // Label rendering
                if (p.dr == Direction.EAST) {
                    g2d.drawString(String.valueOf(p.id), p.X + 12, p.Y + 12);
                } else if (p.dr == Direction.WEST) {
                    g2d.drawString(String.valueOf(p.id), p.X - 30, p.Y + 15);
                } else if (p.dr == Direction.SOUTH) {
                    g2d.drawString(String.valueOf(p.id), p.X - 5, p.Y + 25);
                } else {
                    g2d.drawString(String.valueOf(p.id), p.X - 5, p.Y - 12);
                }
                
                g2d.setFont(originalFont);
            }
        }
        
        // 4. Draw Grids
        java.awt.Stroke oldStroke = g2d.getStroke();
        g2d.setStroke(new java.awt.BasicStroke(1, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_BEVEL, 0, new float[]{2f, 4f}, 0));
        
        for (SPoint grid : appState.getCanvasState().getGrids()) {
            g2d.setColor(grid.c);
            int gridSize = grid.id;
            int xR = grid.X;
            int yR = grid.Y;
            
            int width = backgroundImage != null ? backgroundImage.getWidth() : (this.getWidth() > 0 ? this.getWidth() : 800);
            int height = backgroundImage != null ? backgroundImage.getHeight() : (this.getHeight() > 0 ? this.getHeight() : 600);
            
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
        g2d.setStroke(oldStroke);
        
        g2d.dispose();
    }
}
