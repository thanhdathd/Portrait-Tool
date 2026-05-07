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

import userpackage.SPoint;
import user.Enum.Direction;
import workers.FilterWorker;

public class ImageCanvas extends JPanel {

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
    public static final int CANVAS_PADDING = 80;

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
                appState.setLabelDirection(Direction.NORTH);
                if(zoomWindow != null && zoomWindow.isVisible()) {zoomWindow.repaint();}
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "pressDown");
        am.put("pressDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                appState.setLabelDirection(Direction.SOUTH);
                if(zoomWindow != null && zoomWindow.isVisible()) {zoomWindow.repaint();}
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "pressLeft");
        am.put("pressLeft", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                appState.setLabelDirection(Direction.WEST);
                if(zoomWindow != null && zoomWindow.isVisible()) {zoomWindow.repaint();}
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "pressRight");
        am.put("pressRight", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                appState.setLabelDirection(Direction.EAST);
                if(zoomWindow != null && zoomWindow.isVisible()) {zoomWindow.repaint();}
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

        System.out.println("Enforcing scroll mode");
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
        if (backgroundImage != null) {
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
        RenderUtils.drawStickyPoints(g2d, sPoints, drawLabels);
        
        // 4. Draw Grids
        int width = backgroundImage != null ? backgroundImage.getWidth() : (this.getWidth() > 0 ? this.getWidth() : 800);
        int height = backgroundImage != null ? backgroundImage.getHeight() : (this.getHeight() > 0 ? this.getHeight() : 600);
        RenderUtils.drawGrids(g2d, zoom, appState, width, height);

        g2d.dispose();
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
}
