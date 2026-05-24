package ui.canvas;

import core.state.AppState;
import filter.FilterProperties;
import tools.CropTool;
import tools.P2PTool;
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
import java.util.List;

import core.state.SPoint;
import core.state.Direction;
import workers.FilterWorker;
import ui.MainFrame;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.File;
import java.util.ArrayList;

public class ImageCanvas extends JPanel implements DropTargetListener {

    private final AppState appState;
    private Tool activeTool;
    private Tool previousTool; // For spacebar toggle
    private final HandTool globalHandTool = new HandTool();
    private BufferedImage backgroundImage;
    private ZoomWindow zoomWindow;
    private BufferedImage tempPreviewImage = null;
    private Rectangle tempPreviewBounds = null;
    private boolean drawLabels = true;
    private boolean isShiftDown = false;

    private boolean isLivePreviewFilterActive = false;
    private boolean isDraggingFile = false;
    private MainFrame mainFrame;
    public static final int CANVAS_PADDING = 80;

    // Arrow-key point movement debounce
    private SPoint arrowMovePointRef   = null;  // point being moved by arrow keys
    private SPoint arrowMoveOldState   = null;  // captured before the first key press in a sequence
    private javax.swing.Timer arrowDebounceTimer = null;
    private static final int ARROW_DEBOUNCE_MS = 400;

    public ImageCanvas(AppState appState) {
        this.appState = appState;
        this.appState.getCanvasState().setImageOffsetX(CANVAS_PADDING);
        this.appState.getCanvasState().setImageOffsetY(CANVAS_PADDING);
        this.appState.addPropertyChangeListener("currentZoom", evt -> {
            SwingUtilities.invokeLater(this::enforceScrollModeOffset);
        });
        this.appState.addPropertyChangeListener("checkerSize", evt -> {
            repaint();
        });
        setBackground(Color.LIGHT_GRAY);
        setFocusable(true);
        setAutoscrolls(true); // Enables auto-scrolling for drag events
        setFocusTraversalKeysEnabled(false);
        enableInputMethods(false);
        
        // Initialize Drop Target
        new DropTarget(this, this);
        
        // Mouse Listeners that delegate to the active tool
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (activeTool != null) {
                    activeTool.onMousePressed(e, appState, ImageCanvas.this);
                    if(activeTool instanceof HandTool) {
                        setCursor(CustomCursors.CLOSE_HAND_CURSOR);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (activeTool != null) {
                    activeTool.onMouseReleased(e, appState, ImageCanvas.this);
                    if(activeTool instanceof HandTool) {
                        setCursor(CustomCursors.OPEN_HAND_CURSOR);
                    }
                    if(zoomWindow != null && zoomWindow.isVisible()) {
                        zoomWindow.repaint();
                    }
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
                if(isLivePreviewFilterActive) {
                    applyLivePreviewToMainCanvas(null);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (activeTool != null) {
                    activeTool.onMouseMoved(e, appState, ImageCanvas.this);
                }
                updateZoomWindow(e.getPoint());
            }
        });

        this.addMouseWheelListener(e -> {
            if (activeTool != null) {
                activeTool.onMouseWheelMoved(e, appState, ImageCanvas.this);
                if (activeTool instanceof tools.CropTool && e.isShiftDown()) return;
            }
            if (zoomWindow != null && zoomWindow.isVisible() && backgroundImage != null) {
                int rotation = e.getWheelRotation();
                // rotation < 0 nghĩa là cuộn lên (Zoom In)
                if (appState.isCustomLabelMode()) {
                    int currentGap = appState.getCustomGap();
                    if (rotation < 0) { // Cuộn lên -> Phóng to vòng tròn
                        appState.setCustomGap(currentGap + 2);
                    } else {            // Cuộn xuống -> Thu nhỏ vòng tròn
                        appState.setCustomGap(currentGap - 2);
                    }
                    zoomWindow.repaint();
                } else {
                    if (rotation < 0) {
                        zoomWindow.zoomIn();
                    } else {
                        zoomWindow.zoomOut();
                    }
                }
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

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "pressUP");
        am.put("pressUP", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!handleArrowMovePoint(0, -1)) {
                    appState.setLabelDirection(Direction.NORTH);
                    if(zoomWindow != null && zoomWindow.isVisible()) {zoomWindow.repaint();}
                }
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "pressDown");
        am.put("pressDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!handleArrowMovePoint(0, 1)) {
                    appState.setLabelDirection(Direction.SOUTH);
                    if(zoomWindow != null && zoomWindow.isVisible()) {zoomWindow.repaint();}
                }
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "pressLeft");
        am.put("pressLeft", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!handleArrowMovePoint(-1, 0)) {
                    appState.setLabelDirection(Direction.WEST);
                    if(zoomWindow != null && zoomWindow.isVisible()) {zoomWindow.repaint();}
                }
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "pressRight");
        am.put("pressRight", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!handleArrowMovePoint(1, 0)) {
                    appState.setLabelDirection(Direction.EAST);
                    if(zoomWindow != null && zoomWindow.isVisible()) {zoomWindow.repaint();}
                }
            }
        });

        // Nút A: Bật/Tắt chế độ
        im.put(KeyStroke.getKeyStroke('a'), "toggleCustomMode");
        im.put(KeyStroke.getKeyStroke('A'), "toggleCustomMode");
        im.put(KeyStroke.getKeyStroke('â'), "toggleCustomMode");
        im.put(KeyStroke.getKeyStroke('Â'), "toggleCustomMode");
        am.put("toggleCustomMode", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                System.out.println("-> press A");
                if(zoomWindow != null && zoomWindow.isVisible()) {
                    appState.toggleCustomLabelMode();
                    zoomWindow.repaint(); // Ép ZoomWindow vẽ lại ngay
                    repaint();
                } else {
                    if (mainFrame != null && mainFrame.isExportFocusMode()) return;
                    setActiveTool(tools.ToolManager.initializeTools().selectTool);
                }
            }
        });

        // Phím ESC: Tắt chế độ custom label placement khi zoom window đang hiển thị
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "disableCustomPlacementEsc");
        am.put("disableCustomPlacementEsc", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (zoomWindow != null && zoomWindow.isVisible()) {
                    appState.setCustomLabelMode(false);
                    zoomWindow.repaint();
                    repaint();
                }
            }
        });

        // Nút X: Chuyển đổi màu nền XOR
        im.put(KeyStroke.getKeyStroke('x'), "cycleXorColor");
        im.put(KeyStroke.getKeyStroke('X'), "cycleXorColor");
        am.put("cycleXorColor", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (zoomWindow != null && zoomWindow.isVisible()) {
                    appState.cycleXorColor();
                    zoomWindow.updateTitle();
                    zoomWindow.repaint();
                }
            }
        });

// Nút [ và ]: Giảm/Tăng Gap
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, 0), "decreaseGap");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "decreaseGap");
        am.put("decreaseGap", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                appState.setCustomGap(appState.getCustomGap() - 2);
                zoomWindow.repaint();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, 0), "increaseGap");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0), "increaseGap");
        am.put("increaseGap", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                appState.setCustomGap(appState.getCustomGap() + 2);
                zoomWindow.repaint();
            }
        });

// Nút , (<) và . (>): Xoay góc (10 độ mỗi lần bấm để xoay nhanh)
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, 0), "rotateCW");
        am.put("rotateCW", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                // Phím < xoay cùng chiều kim đồng hồ (giảm góc)
                appState.setCustomAngle(appState.getCustomAngle() - 2);
                zoomWindow.repaint();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, 0), "rotateCCW");
        am.put("rotateCCW", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                // Phím > xoay ngược chiều kim đồng hồ (tăng góc) theo đúng yêu cầu
                appState.setCustomAngle(appState.getCustomAngle() + 2);
                zoomWindow.repaint();
            }
        });
        
        // Listen for SHIFT to update Zoom cursor and KeyEvents for Tools
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (activeTool instanceof tools.CropTool && e.getID() == KeyEvent.KEY_PRESSED) {
                ((tools.CropTool)activeTool).onKeyPressed(e, appState, ImageCanvas.this);
            }else if(activeTool instanceof tools.P2PTool && e.getID() == KeyEvent.KEY_PRESSED) {
                ((tools.P2PTool)activeTool).onKeyPressed(e, appState, ImageCanvas.this);
            }
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

    private Point lastPoint = new Point(-1,-1);
    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            isDraggingFile = true;
            repaint();
        }
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        // Just keep the flag true
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        isDraggingFile = false;
        repaint();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void drop(DropTargetDropEvent dtde) {
        isDraggingFile = false;
        repaint();
        
        try {
            if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY);
                Transferable t = dtde.getTransferable();
                List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                
                if (files != null && !files.isEmpty()) {
                    if (files.size() > 1) {
                        JOptionPane.showMessageDialog(this, 
                            "Please drop only one file at a time.", 
                            "Multiple Files Detected", 
                            JOptionPane.WARNING_MESSAGE);
                    } else {
                        File file = files.get(0);
                        if (mainFrame != null) {
                            mainFrame.openExternalFile(file);
                        }
                    }
                }
                dtde.dropComplete(true);
            } else {
                dtde.rejectDrop();
            }
        } catch (Exception e) {
            e.printStackTrace();
            dtde.dropComplete(false);
        }
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
            if (!unscaledP.equals(lastPoint)) {
                zoomWindow.updateMousePosition(unscaledP);
                lastPoint = unscaledP;
            }
        }
    }

    public void setActiveTool(Tool newTool) {
        Tool oldTool = this.activeTool;
        this.activeTool = newTool;
        updateCursor();
        firePropertyChange("activeTool", oldTool, newTool);
        if(oldTool instanceof CropTool cropTool) {
            cropTool.onDeactivate(appState);
        }
        repaint();
    }
    
    public void updateCursor() {
        if (activeTool instanceof HandTool) {
            setCursor(CustomCursors.OPEN_HAND_CURSOR);
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

    private void clampOffsets() {
        if (backgroundImage != null) {
            float zoom = appState.getCurrentZoom();
            int imgWidth = (int) (backgroundImage.getWidth() * zoom);
            int imgHeight = (int) (backgroundImage.getHeight() * zoom);
            
            Container parent = SwingUtilities.getUnwrappedParent(this);
            if (parent instanceof JViewport) {
                JViewport viewport = (JViewport) parent;
                core.state.CanvasState cs = appState.getCanvasState();
                
                if (imgWidth >= viewport.getWidth()) {
                    cs.setImageOffsetX(0);
                } else {
                    int maxX = viewport.getWidth() - imgWidth;
                    cs.setImageOffsetX(Math.max(0, Math.min(cs.getImageOffsetX(), maxX)));
                }
                
                if (imgHeight >= viewport.getHeight()) {
                    cs.setImageOffsetY(0);
                } else {
                    int maxY = viewport.getHeight() - imgHeight;
                    cs.setImageOffsetY(Math.max(0, Math.min(cs.getImageOffsetY(), maxY)));
                }
            }
        }
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
                    appState.setFloating(false);
                    return new Dimension(imgWidth + 2 * CANVAS_PADDING,
                            imgHeight + 2 * CANVAS_PADDING);
                } else {
                    appState.setFloating(true);
                    return new Dimension(viewport.getWidth(), viewport.getHeight());
                }
            }
            return new Dimension(imgWidth, imgHeight);
        }
        return super.getPreferredSize();
    }

    private void enforceScrollModeOffset() {
        if (backgroundImage == null) return;
        Container parent = SwingUtilities.getUnwrappedParent(this);
        if (!(parent instanceof JViewport)) return;
        JViewport viewport = (JViewport) parent;

        float zoom = appState.getCurrentZoom();
        int imgWidth = (int) (backgroundImage.getWidth() * zoom);
        int imgHeight = (int) (backgroundImage.getHeight() * zoom);
        int viewWidth = viewport.getWidth();
        int viewHeight = viewport.getHeight();
        boolean isCropTool = activeTool instanceof tools.CropTool;
        // Chỉ reset khi ảnh lớn hơn viewport ở CẢ hai chiều
        if (imgWidth > viewWidth && imgHeight > viewHeight && !isCropTool) {
            core.state.CanvasState cs = appState.getCanvasState();
            if (cs.getImageOffsetX() != CANVAS_PADDING || cs.getImageOffsetY() != CANVAS_PADDING) {
                cs.setImageOffsetX(CANVAS_PADDING);
                cs.setImageOffsetY(CANVAS_PADDING);
                repaint(); // vẽ lại với offset mới
            }
        }
    }

    public void setBackgroundImage(BufferedImage image) {
        this.backgroundImage = image;
        enforceScrollModeOffset();
        if (image != null) {
            this.revalidate();
        }
        if (zoomWindow != null && zoomWindow.isVisible()) {
            zoomWindow.updateImage(image);
        }
        this.repaint();
    }

    public BufferedImage getBackgroundImage() {
        return backgroundImage;
    }

    @Override
    protected void paintComponent(Graphics gp) {
        super.paintComponent(gp);
        Graphics2D g2d = (Graphics2D) gp.create();
        List<SPoint> sPoints = appState.getCanvasState().getStickyPoints();
        int CHECKER_SIZE = appState.getCheckerSize();
        
        // Draw Checkerboard Background
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        Container parent = SwingUtilities.getUnwrappedParent(this);
        if (parent instanceof JViewport) {
            viewWidth = Math.max(viewWidth, parent.getWidth());
            viewHeight = Math.max(viewHeight, parent.getHeight());
        }

        g2d.setColor(new Color(60, 60, 60, 255));
        g2d.fillRect(0, 0, viewWidth, viewHeight);
        g2d.setColor(new Color(90, 90, 90, 255));
        for (int y = 0; y < viewHeight; y += CHECKER_SIZE) {
            for (int x = 0; x < viewWidth; x += CHECKER_SIZE) {
                if (((x / CHECKER_SIZE) ^ (y / CHECKER_SIZE)) % 2 == 0) {
                    g2d.fillRect(x, y, CHECKER_SIZE, CHECKER_SIZE);
                }
            }
        }
        
//        clampOffsets();
        
        // Apply Image Offset (screen coordinates)
        int offsetX = appState.getCanvasState().getImageOffsetX();
        int offsetY = appState.getCanvasState().getImageOffsetY();
        g2d.translate(offsetX, offsetY);
        
        // Apply Zoom Transform
        float zoom = appState.getCurrentZoom();
        g2d.scale(zoom, zoom);
        
        // 1. Draw Background Image
        boolean showPointMap = appState.isShowPointMap() || appState.getCanvasState().isExportPreviewActive();
        if (showPointMap) {
            int width = backgroundImage != null ? backgroundImage.getWidth() : getWidth();
            int height = backgroundImage != null ? backgroundImage.getHeight() : getHeight();
            
            float scaleToUse = appState.getScale();
            if (appState.getCanvasState().isExportPreviewActive()) {
                scaleToUse = (float) appState.getCanvasState().getExportPreviewScale();
            }
            RenderUtils.drawPointMap(g2d, appState.getCanvasState().getStickyPoints(), scaleToUse, width, height);
        } else if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, this);
        }

        if (tempPreviewImage != null && tempPreviewBounds != null) {
            // Dù lúc getVisiblePreviewData ta có scale nhỏ ảnh xuống (ở chế độ zoom out),
            // thì khi vẽ drawImage với width, height của bounds, Java2D sẽ tự động
            // scale up (kéo giãn) nó ra khớp khít 100% với khung hình.
            g2d.drawImage(tempPreviewImage,
                    tempPreviewBounds.x, tempPreviewBounds.y,
                    tempPreviewBounds.width, tempPreviewBounds.height,
                    null);
        }
        
        // 2. Allow active tool to draw preview
        if (activeTool != null) {
            activeTool.onPaint(g2d, appState, this);
        }
        // Also draw previous tool if we are in a temporary switch mode (like spacebar pan)
        if (previousTool != null) {
            previousTool.onPaint(g2d, appState, this);
        }
        
        // 3. Draw Sticky Points
        if (!appState.getCanvasState().isExportPreviewActive()) {
            RenderUtils.drawStickyPoints(g2d, sPoints, drawLabels,
                    appState.getCanvasState().getSelectedPoints());
        }

        // Draw Lines
        RenderUtils.drawLines(g2d, appState.getCanvasState().getLines(), appState.getCanvasState().getSelectedLines(), zoom);
        
        // 4. Draw Grids
        int width = backgroundImage != null ? backgroundImage.getWidth() : (this.getWidth() > 0 ? this.getWidth() : 800);
        int height = backgroundImage != null ? backgroundImage.getHeight() : (this.getHeight() > 0 ? this.getHeight() : 600);
        RenderUtils.drawGrids(g2d, zoom, appState, width, height);

        // ==========================================
        // 5. Draw Live Preview Calibration Markers
        // ==========================================
        if (appState.getCanvasState().isExportPreviewActive()) {
            drawLivePreviewMarkers(g2d, width, height);
        }

        g2d.dispose();

        // 5. Draw Drag & Drop Overlay (Absolute coordinates, after disposing transformed g2d)
        if (isDraggingFile) {
            Graphics2D gDrag = (Graphics2D) gp.create();
            gDrag.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int w = getWidth();
            int h = getHeight();
            
            // Draw semi-transparent overlay
            gDrag.setColor(new Color(65, 105, 225, 50)); // Royal Blue with alpha
            gDrag.fillRect(0, 0, w, h);
            
            // Draw dashed border
            gDrag.setColor(new Color(65, 105, 225));
            gDrag.setStroke(new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{10}, 0));
            gDrag.drawRect(5, 5, w - 10, h - 10);
            
            // Draw Text
            String text = "Drop to open file";
            gDrag.setFont(new Font("SansSerif", Font.BOLD, 24));
            FontMetrics fm = gDrag.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getAscent();
            
            // Text shadow for readability
            gDrag.setColor(new Color(255, 255, 255, 150));
            gDrag.drawString(text, (w - textWidth) / 2 + 2, (h + textHeight) / 2 + 2);
            
            gDrag.setColor(new Color(65, 105, 225));
            gDrag.drawString(text, (w - textWidth) / 2, (h + textHeight) / 2);
            
            gDrag.dispose();
        }
    }

    private void drawLivePreviewMarkers(Graphics2D g2d, int width, int height) {
        double effScale = appState.getCanvasState().getExportPreviewScale();
        String title = appState.getCanvasState().getExportPreviewTitle();
        float pxPerCm = (float) (1.0 / effScale); // Pixel count equivalent to 1cm on the original unscaled image
        
        g2d.setColor(Color.BLACK);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 1. Corner Markers (1cm stroke, 4cm arm)
        int strokeWidthPx = Math.round(1.0f * pxPerCm);
        int armLengthPx = Math.round(4.0f * pxPerCm);
        
        // Top-Left
        g2d.fillRect(0, 0, armLengthPx, strokeWidthPx);
        g2d.fillRect(0, 0, strokeWidthPx, armLengthPx);
        // Top-Right
        g2d.fillRect(width - armLengthPx, 0, armLengthPx, strokeWidthPx);
        g2d.fillRect(width - strokeWidthPx, 0, strokeWidthPx, armLengthPx);
        // Bottom-Left
        g2d.fillRect(0, height - armLengthPx, strokeWidthPx, armLengthPx);
        g2d.fillRect(0, height - strokeWidthPx, armLengthPx, strokeWidthPx);

        // Punch out text inside Bottom-Left arm (using white to simulate transparency on canvas)
        g2d.setColor(Color.WHITE);
        int textFontSizePx = Math.round((10.0f / 72.0f * 2.54f) * pxPerCm);
        g2d.setFont(new Font("Arial", Font.PLAIN, textFontSizePx));
        
        // Calculate original width/height in cm
        double wCm = effScale * width;
        double hCm = effScale * height;
        String sizeText = Math.round(wCm * 10) + " x " + Math.round(hCm * 10) + " mm";
        
        FontMetrics fm = g2d.getFontMetrics();
        int textAscent = fm.getAscent();
        int textDescent = fm.getDescent();
        int textW = fm.stringWidth(sizeText);
        int armCenterY = height - strokeWidthPx / 2;
        int textY = armCenterY + (textAscent - textDescent) / 2;
//        int paddingPx = Math.round(0.2f * pxPerCm);
        int textX = armLengthPx / 2 - textW / 2;
        
        g2d.drawString(sizeText, textX, textY);
        
        // Restore black color for the rest
        g2d.setColor(Color.BLACK);
        
        // Bottom-Right
        g2d.fillRect(width - armLengthPx, height - strokeWidthPx, armLengthPx, strokeWidthPx);
        g2d.fillRect(width - strokeWidthPx, height - armLengthPx, strokeWidthPx, armLengthPx);

        // 2. Calibration Square (2x2 cm)
        int squareSizePx = Math.round(2.0f * pxPerCm);
        int squareOffsetPx = Math.round(2.0f * pxPerCm);
        int sqX = squareOffsetPx;
        int sqY = height - squareOffsetPx - squareSizePx;
        
        Stroke oldStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke(0.05f * pxPerCm)); // ~0.5mm
        g2d.drawRect(sqX, sqY, squareSizePx, squareSizePx);
        
        int midX = sqX + squareSizePx / 2;
        int midY = sqY + squareSizePx / 2;
        int tickLen = Math.round(0.2f * pxPerCm);
        g2d.drawLine(midX, sqY - tickLen/2, midX, sqY + tickLen/2);
        g2d.drawLine(midX, sqY + squareSizePx - tickLen/2, midX, sqY + squareSizePx + tickLen/2);
        g2d.drawLine(sqX - tickLen/2, midY, sqX + tickLen/2, midY);
        g2d.drawLine(sqX + squareSizePx - tickLen/2, midY, sqX + squareSizePx + tickLen/2, midY);
        g2d.setStroke(oldStroke);

        // Label "2x2 cm" (Physical size: 12pt = 12/72 * 2.54 cm)
        int fontSizePx = Math.round((12.0f / 72.0f * 2.54f) * pxPerCm);
        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizePx));
        int labelX = sqX + squareSizePx + Math.round(0.2f * pxPerCm);
        int labelY = sqY + squareSizePx;
        g2d.drawString("2x2 cm", labelX, labelY);

        // 3. Map Title
        if (title != null && !title.trim().isEmpty()) {
            g2d.setFont(new Font("Monospaced", Font.PLAIN, fontSizePx));
            int titleX = armLengthPx + Math.round(0.5f * pxPerCm);
            int titleY = height - Math.round(0.5f * pxPerCm);
            g2d.drawString(title, titleX, titleY);
        }
    }

    public AppState getAppState() {
        return appState;
    }

    private FilterWorker canvasPreviewWorker;
    FilterProperties filterProps;
    public void applyLivePreviewToMainCanvas(FilterProperties props) {
        if (props != null) {
            this.filterProps = props;
        }
        if (canvasPreviewWorker != null && !canvasPreviewWorker.isDone()) {
            canvasPreviewWorker.cancel(true);
        }

        PreviewRequest req = getVisiblePreviewData();
        if (req == null)return;
        canvasPreviewWorker = new FilterWorker(req.imageToProcess, filterProps, resultIamge -> {
            setTempPreview(resultIamge, req.originalBounds);
        });
        canvasPreviewWorker.execute();
    }

    public void clearTempPreview() {
        this.tempPreviewImage = null;
        this.tempPreviewBounds = null;
        repaint();
    }

    public void setTempPreview(BufferedImage img, Rectangle bounds) {
        this.tempPreviewImage = img;
        this.tempPreviewBounds = bounds;
        repaint();
    }

    public PreviewRequest getVisiblePreviewData() {
        if (backgroundImage == null) return null;
        int panX = appState.getCanvasState().getImageOffsetX();
        int panY = appState.getCanvasState().getImageOffsetY();
        float zoomFactor = appState.getCurrentZoom();

        // BƯỚC 1: Tính toán vùng ảnh gốc đang hiển thị trên màn hình
        // Công thức: (Tọa độ màn hình - Tọa độ Pan) / Zoom
        int startX = (int) (-panX / zoomFactor);
        int startY = (int) (-panY / zoomFactor);
        int endX = (int) ((getWidth() - panX) / zoomFactor);
        int endY = (int) ((getHeight() - panY) / zoomFactor);

        // Giới hạn (Clamp) tọa độ không được lọt ra ngoài ảnh gốc
        startX = Math.max(0, startX);
        startY = Math.max(0, startY);
        endX = Math.min(backgroundImage.getWidth(), endX);
        endY = Math.min(backgroundImage.getHeight(), endY);

        int cropW = endX - startX;
        int cropH = endY - startY;

        if (cropW <= 0 || cropH <= 0) return null; // Ảnh nằm ngoài khung hình

        Rectangle bounds = new Rectangle(startX, startY, cropW, cropH);

        // BƯỚC 2: Tối ưu hóa (LOD) theo ý tưởng của bạn
        BufferedImage imageToProcess;

        if (zoomFactor < 1.0) {
            // NẾU ZOOM OUT: Ảnh nhìn thấy rất nhỏ, nhưng bounds lại rất to (vd 4K).
            // Ta scale ảnh xuống bằng đúng độ phân giải màn hình trước khi xử lý để chống lag.
            int renderW = (int) (cropW * zoomFactor);
            int renderH = (int) (cropH * zoomFactor);

            // Cắt ảnh bằng getSubimage (rất nhanh, O(1))
            BufferedImage cropped = backgroundImage.getSubimage(startX, startY, cropW, cropH);

            // Tạo ảnh thu nhỏ để xử lý
            imageToProcess = new BufferedImage(renderW, renderH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = imageToProcess.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(cropped, 0, 0, renderW, renderH, null);
            g2d.dispose();
        } else {
            // NẾU ZOOM IN / BÌNH THƯỜNG: Vùng nhìn thấy đã nhỏ sẵn, ta cắt trực tiếp.
            // getSubimage tạo ra một ảnh mới nhưng dùng chung bộ nhớ (shared raster) với ảnh gốc nên không tốn RAM.
            imageToProcess = backgroundImage.getSubimage(startX, startY, cropW, cropH);
        }

        return new PreviewRequest(imageToProcess, bounds);
    }

    public void setLivePreviewFilterActive(boolean livePreviewFilterActive) {
        isLivePreviewFilterActive = livePreviewFilterActive;
    }

    /**
     * Di chuyển point đang được chọn theo hướng (dx, dy).
     * Sử dụng debounce timer để chỉ push 1 StickCommand vào history sau khi user dừng nhấn phím.
     *
     * @return true nếu đã xử lý (có selectedPoint), false nếu không để fallback sang hành vi cũ
     */
    private boolean handleArrowMovePoint(int dx, int dy) {
        // Chỉ xử lý khi tool hiện tại là SelectTool
        if (!(activeTool instanceof tools.SelectTool)) return false;

        // Disable multi-move: chỉ cho phép move khi chọn đúng 1 point
        java.util.Set<SPoint> sel = appState.getCanvasState().getSelectedPoints();
        if (sel.size() != 1) return sel.size() > 1; // consume event but don't move

        SPoint p = appState.getCanvasState().getSelectedPoint();
        if (p == null) return false;

        // Nếu đây là lần đầu trong chuỗi nhấn phím (timer chưa chạy)
        // thì chụp trạng thái ban đầu để dùng cho oldState của StickCommand
        if (arrowDebounceTimer == null || !arrowDebounceTimer.isRunning()
                || arrowMovePointRef != p) {
            arrowMovePointRef = p;
            arrowMoveOldState = p.copy(); // snapshot trước khi bắt đầu chuỗi di chuyển
        }

        // Di chuyển ngay lập tức để cho visual feedback mượt
        p.X += dx;
        p.Y += dy;
        repaint();
        // Re-fire listener để PointPropertyPanel cập nhật XY fields (tránh stale values)
        appState.getCanvasState().setSelectedPoint(p);

        // Reset/start debounce timer
        if (arrowDebounceTimer != null) {
            arrowDebounceTimer.stop();
        }
        final SPoint capturedOldState = arrowMoveOldState;
        arrowDebounceTimer = new javax.swing.Timer(ARROW_DEBOUNCE_MS, evt -> {
            // Khi timer kích hoạt (user đã dừng nhấn), push command vào history
            SPoint current = appState.getCanvasState().getSelectedPoint();
            if (current != null && current == arrowMovePointRef) {
                SPoint newState = current.copy();
                core.history.StickCommand cmd = new core.history.StickCommand(
                        appState.getCanvasState(), this, current,
                        core.history.StickCommand.Action.EDIT,
                        capturedOldState, newState);
                appState.getHistoryManager().push(cmd);
            }
            arrowDebounceTimer = null;
            arrowMovePointRef = null;
            arrowMoveOldState = null;
        });
        arrowDebounceTimer.setRepeats(false);
        arrowDebounceTimer.start();

        return true;
    }
}

