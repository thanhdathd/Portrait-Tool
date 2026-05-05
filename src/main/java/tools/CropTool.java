package tools;

import config.ConfigManager;
import core.image.crop.CropGuide;
import core.image.crop.CropProfile;
import core.image.crop.CropRatio;
import core.image.crop.CustomCropProfile;
import core.state.AppState;
import core.history.CropCommand;
import ui.canvas.ImageCanvas;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CropTool implements Tool {

    /** Unified crop profile: works for both built-in and custom entries. */
    private static class ActiveProfile {
        final String label;
        final float  ratio;
        final CropGuide guide;

        ActiveProfile(String label, float ratio, CropGuide guide) {
            this.label = label;
            this.ratio = ratio;
            this.guide = guide;
        }
    }

    private boolean isReviewMode = false;
    private int mouseX = 0;
    private int mouseY = 0;
    private int frameWidth = 400;
    private boolean isLandscape = true;
    private int spiralVariant = 0;

    private enum SnapMode { NONE, VERTICAL, HORIZONTAL }
    private SnapMode snapMode = SnapMode.NONE;

    private int profileIndex = 0;
    private final List<ActiveProfile> profiles = new ArrayList<>();

    public CropTool(ConfigManager configManager) {
        // Built-in profiles
        profiles.add(new ActiveProfile(CropRatio.GOLDEN.label, CropRatio.GOLDEN.ratio, CropGuide.RULE_OF_THIRDS));
        profiles.add(new ActiveProfile(CropRatio.GOLDEN.label + " ☯", CropRatio.GOLDEN.ratio, CropGuide.GOLDEN_SPIRAL));
        profiles.add(new ActiveProfile(CropRatio.RATIO_4_3.label, CropRatio.RATIO_4_3.ratio, CropGuide.RULE_OF_THIRDS));
        profiles.add(new ActiveProfile(CropRatio.RATIO_3_2.label, CropRatio.RATIO_3_2.ratio, CropGuide.RULE_OF_THIRDS));
        profiles.add(new ActiveProfile(CropRatio.SQUARE.label, CropRatio.SQUARE.ratio, CropGuide.RULE_OF_THIRDS));
        profiles.add(new ActiveProfile(CropRatio.A4.label, CropRatio.A4.ratio, CropGuide.DIAGONAL));

        // Append user-defined custom profiles
        if (configManager != null) {
            for (CustomCropProfile cp : configManager.getCustomCropProfileManager().getProfiles()) {
                profiles.add(new ActiveProfile(cp.name, cp.ratio, cp.guide));
            }
        }
    }

    /** No-arg constructor for convenience (no custom profiles). */
    public CropTool() {
        this(null);
    }

    @Override
    public void onMousePressed(MouseEvent e, AppState state, ImageCanvas canvas) {
        if (!isReviewMode && e.getButton() == MouseEvent.BUTTON1) {
            isReviewMode = true;
            canvas.repaint();
        }
    }

    @Override
    public void onMouseReleased(MouseEvent e, AppState state, ImageCanvas canvas) {}

    @Override
    public void onMouseDragged(MouseEvent e, AppState state, ImageCanvas canvas) {}

    @Override
    public void onPaint(Graphics2D g2d, AppState state, ImageCanvas canvas) {
        if (canvas.getBackgroundImage() == null) return;
        
        // Save original transform
        java.awt.geom.AffineTransform oldTransform = g2d.getTransform();
        
        // Revert the canvas transformation so we draw in raw screen coordinates!
        float zoom = state.getCurrentZoom();
        int ox = state.getCanvasState().getImageOffsetX();
        int oy = state.getCanvasState().getImageOffsetY();
        
        g2d.scale(1.0 / zoom, 1.0 / zoom);
        g2d.translate(-ox, -oy);
        
        Rectangle bounds = getCropBounds(state, canvas);
        int currentFrameWidth = bounds.width;
        int currentFrameHeight = bounds.height;
        int x = bounds.x;
        int y = bounds.y;
        
        // Draw Dark Overlay outside frame
        Area screenArea = new Area(new Rectangle(0, 0, canvas.getWidth()*2, canvas.getHeight()*2));
        Area cropArea = new Area(new Rectangle(x, y, currentFrameWidth, currentFrameHeight));
        screenArea.subtract(cropArea);
        
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fill(screenArea);
        
        // Draw Frame Border
        g2d.setColor(isReviewMode ? new Color(0x008083) : Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(x, y, currentFrameWidth, currentFrameHeight);
        
        // Draw Composition Guide
        drawGuide(g2d, profiles.get(profileIndex).guide, x, y, currentFrameWidth, currentFrameHeight);
        
        // Draw Mini-Filmstrip
        drawFilmstrip(g2d, x, y + currentFrameHeight + 20);
        
        // Restore transform
        g2d.setTransform(oldTransform);
    }

    private void drawGuide(Graphics2D g2d, CropGuide guide, int x, int y, int w, int h) {
        g2d.setColor(new Color(255, 255, 255, 128));
        g2d.setStroke(new BasicStroke(1));
        
        if (guide == CropGuide.RULE_OF_THIRDS) {
            g2d.drawLine(x + w/3, y, x + w/3, y + h);
            g2d.drawLine(x + 2*w/3, y, x + 2*w/3, y + h);
            g2d.drawLine(x, y + h/3, x + w, y + h/3);
            g2d.drawLine(x, y + 2*h/3, x + w, y + 2*h/3);
        } else if (guide == CropGuide.CROSSHAIR) {
            g2d.drawLine(x + w/2, y, x + w/2, y + h);
            g2d.drawLine(x, y + h/2, x + w, y + h/2);
        } else if (guide == CropGuide.DIAGONAL) {
            g2d.drawLine(x, y, x + w, y + h);
            g2d.drawLine(x, y + h, x + w, y);
        } else if (guide == CropGuide.GOLDEN_SPIRAL) {
            java.awt.geom.AffineTransform saveAT = g2d.getTransform();
            
            if (spiralVariant == 1) {
                g2d.translate(x + w / 2.0, y + h / 2.0);
                g2d.scale(-1, 1);
                g2d.translate(-(x + w / 2.0), -(y + h / 2.0));
            } else if (spiralVariant == 2) {
                g2d.translate(x + w / 2.0, y + h / 2.0);
                g2d.scale(1, -1);
                g2d.translate(-(x + w / 2.0), -(y + h / 2.0));
            } else if (spiralVariant == 3) {
                g2d.translate(x + w / 2.0, y + h / 2.0);
                g2d.scale(-1, -1);
                g2d.translate(-(x + w / 2.0), -(y + h / 2.0));
            }

            double cx = x, cy = y, cw = w, ch = h;
            int dir = (w >= h) ? 0 : 3; 

            for (int i = 0; i < 8; i++) {
                if (cw <= 1 || ch <= 1) break;
                if (dir % 4 == 0) { // Square on Left
                    double sq = ch;
                    g2d.drawRect((int)cx, (int)cy, (int)sq, (int)sq);
                    g2d.drawArc((int)cx, (int)cy, (int)(sq * 2), (int)(sq * 2), 90, 90);
                    cx += sq; cw -= sq;
                } else if (dir % 4 == 1) { // Square on Top
                    double sq = cw;
                    g2d.drawRect((int)cx, (int)cy, (int)sq, (int)sq);
                    g2d.drawArc((int)(cx - sq), (int)cy, (int)(sq * 2), (int)(sq * 2), 0, 90);
                    cy += sq; ch -= sq;
                } else if (dir % 4 == 2) { // Square on Right
                    double sq = ch;
                    g2d.drawRect((int)(cx + cw - sq), (int)cy, (int)sq, (int)sq);
                    g2d.drawArc((int)(cx + cw - sq * 2), (int)(cy - sq), (int)(sq * 2), (int)(sq * 2), 270, 90);
                    cw -= sq;
                } else if (dir % 4 == 3) { // Square on Bottom
                    double sq = cw;
                    g2d.drawRect((int)cx, (int)(cy + ch - sq), (int)sq, (int)sq);
                    g2d.drawArc((int)cx, (int)(cy + ch - sq * 2), (int)(sq * 2), (int)(sq * 2), 180, 90);
                    ch -= sq;
                }
                dir++;
            }
            
            g2d.setTransform(saveAT);
        }
    }
    
    private void drawFilmstrip(Graphics2D g2d, int cx, int cy) {
        int itemSize = 40;
        int spacing = 10;
        int totalWidth = profiles.size() * itemSize + (profiles.size() - 1) * spacing;
        
        int startX = cx + (frameWidth / 2) - (totalWidth / 2);
        
        for (int i = 0; i < profiles.size(); i++) {
            ActiveProfile p = profiles.get(i);
            float r = p.ratio;
            if (!isLandscape && r != 1.0f) {
                r = 1.0f / r;
            }
            int fh = Math.round(itemSize / r);
            int yOffset = cy + (itemSize - fh)/2;
            
            if (i == profileIndex) {
                g2d.setColor(Color.YELLOW);
                g2d.drawRect(startX + i * (itemSize + spacing), yOffset, itemSize, fh);
                
                String label = p.label;
                FontMetrics fm = g2d.getFontMetrics();
                int textAscent  = fm.getAscent();
                int labelWidth = fm.stringWidth(label);
                g2d.drawString(label, startX + i * (itemSize + spacing) + (itemSize - labelWidth)/2, cy + itemSize/2 + textAscent + 20);
            } else {
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.drawRect(startX + i * (itemSize + spacing), yOffset, itemSize, fh);
            }
        }
    }
    
    public void onMouseMoved(MouseEvent e, AppState state, ImageCanvas canvas) {
        if (!isReviewMode) {
            mouseX = e.getX();
            mouseY = e.getY();
            canvas.repaint();
        }
    }

    public void onMouseWheelMoved(MouseWheelEvent e, AppState state, ImageCanvas canvas) {
        if (e.isShiftDown()) {
            int speed = e.isAltDown() ? 5 : 20;
            frameWidth += (e.getWheelRotation() < 0 ? speed : -speed);
            frameWidth = Math.max(50, frameWidth);
            canvas.repaint();
        } else {
            // standard zoom handled by canvas
        }
    }

    public void onKeyPressed(KeyEvent e, AppState state, ImageCanvas canvas) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            if (isReviewMode) {
                mouseX -= 1;
            } else {
                profileIndex = (profileIndex - 1 + profiles.size()) % profiles.size();
            }
            canvas.repaint();
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (isReviewMode) {
                mouseX += 1;
            } else {
                profileIndex = (profileIndex + 1) % profiles.size();
            }
            canvas.repaint();
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            if (isReviewMode) {
                mouseY -= 1;
                canvas.repaint();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            if (isReviewMode) {
                mouseY += 1;
                canvas.repaint();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_R) {
            if (e.isShiftDown()) {
                if (profiles.get(profileIndex).guide == CropGuide.GOLDEN_SPIRAL) {
                    spiralVariant = (spiralVariant + 1) % 4;
                }
            } else {
                isLandscape = !isLandscape;
            }
            canvas.repaint();
        } else if (e.getKeyCode() == KeyEvent.VK_V) {
            if (!isReviewMode && e.isControlDown()) {
                snapMode = (snapMode == SnapMode.VERTICAL) ? SnapMode.NONE : SnapMode.VERTICAL;
                canvas.repaint();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_H) {
            if (!isReviewMode && e.isControlDown()) {
                snapMode = (snapMode == SnapMode.HORIZONTAL) ? SnapMode.NONE : SnapMode.HORIZONTAL;
                canvas.repaint();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (isReviewMode) {
                isReviewMode = false;
                canvas.repaint();
            } else {
                canvas.setActiveTool(new HandTool());
                canvas.repaint();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER && isReviewMode) {
            applyCrop(state, canvas);
        }
    }
    
    private void applyCrop(AppState state, ImageCanvas canvas) {
        ActiveProfile profile = profiles.get(profileIndex);
        float currentRatio = profile.ratio;
        if (!isLandscape && currentRatio != 1.0f) {
            currentRatio = 1.0f / currentRatio;
        }
        int frameHeight = Math.round(frameWidth / currentRatio);
        
        float zoom = state.getCurrentZoom();
        int ox = state.getCanvasState().getImageOffsetX();
        int oy = state.getCanvasState().getImageOffsetY();
        
        java.awt.Container parent = javax.swing.SwingUtilities.getUnwrappedParent(canvas);
        int scrollX = 0, scrollY = 0;
        if (parent instanceof javax.swing.JViewport) {
            java.awt.Point p = ((javax.swing.JViewport)parent).getViewPosition();
            scrollX = p.x;
            scrollY = p.y;
        }
        
        int oldVisualX = ox - scrollX;
        int oldVisualY = oy - scrollY;
        
        Rectangle scrBounds = getCropBounds(state, canvas);
        
        int unscaledX = Math.round((scrBounds.x - ox) / zoom);
        int unscaledY = Math.round((scrBounds.y - oy) / zoom);
        int unscaledW = Math.round(scrBounds.width / zoom);
        int unscaledH = Math.round(scrBounds.height / zoom);
        
        if (unscaledW <= 0 || unscaledH <= 0) return;
        Rectangle bounds = new Rectangle(unscaledX, unscaledY, unscaledW, unscaledH);
        
        java.awt.image.BufferedImage img = canvas.getBackgroundImage();
        if (img == null) return;
        
        CropCommand cmd = new CropCommand(canvas, state.getCanvasState(), img, bounds, zoom, oldVisualX, oldVisualY);
        state.getHistoryManager().push(cmd);
        canvas.setActiveTool(new HandTool());
    }

    private Rectangle getCropBounds(AppState state, ImageCanvas canvas) {
        float zoom = state.getCurrentZoom();
        int ox = state.getCanvasState().getImageOffsetX();
        int oy = state.getCanvasState().getImageOffsetY();
        java.awt.image.BufferedImage img = canvas.getBackgroundImage();
        
        ActiveProfile profile = profiles.get(profileIndex);
        float currentRatio = profile.ratio;
        if (!isLandscape && currentRatio != 1.0f) {
            currentRatio = 1.0f / currentRatio;
        }

        int currentFrameWidth = frameWidth;
        int currentFrameHeight = Math.round(currentFrameWidth / currentRatio);

        if (img != null) {
            if (snapMode == SnapMode.VERTICAL) {
                currentFrameHeight = Math.round(img.getHeight() * zoom);
                currentFrameWidth = Math.round(currentFrameHeight * currentRatio);
            } else if (snapMode == SnapMode.HORIZONTAL) {
                currentFrameWidth = Math.round(img.getWidth() * zoom);
                currentFrameHeight = Math.round(currentFrameWidth / currentRatio);
            }
        }

        int targetMouseX = mouseX;
        int targetMouseY = mouseY;
        
        if (img != null) {
            if (snapMode == SnapMode.VERTICAL) {
                targetMouseY = Math.round(oy + (img.getHeight() * zoom) / 2f);
            } else if (snapMode == SnapMode.HORIZONTAL) {
                targetMouseX = Math.round(ox + (img.getWidth() * zoom) / 2f);
            }
        }

        int x = targetMouseX - currentFrameWidth/2;
        int y = targetMouseY - currentFrameHeight/2;
        
        return new Rectangle(x, y, currentFrameWidth, currentFrameHeight);
    }
}
