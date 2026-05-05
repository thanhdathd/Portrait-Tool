package tools;

import core.image.crop.CropGuide;
import core.image.crop.CropProfile;
import core.image.crop.CropRatio;
import core.state.AppState;
import core.history.CropCommand;
import ui.canvas.ImageCanvas;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Arrays;
import java.util.List;

public class CropTool implements Tool {
    private boolean isReviewMode = false;
    private int mouseX = 0;
    private int mouseY = 0;
    private int frameWidth = 400;
    
    private boolean isLandscape = true;
    
    private int profileIndex = 0;
    private final List<CropProfile> profiles = Arrays.asList(
        new CropProfile(CropRatio.SQUARE, CropGuide.RULE_OF_THIRDS),
        new CropProfile(CropRatio.RATIO_4_3, CropGuide.RULE_OF_THIRDS),
        new CropProfile(CropRatio.RATIO_3_2, CropGuide.RULE_OF_THIRDS),
        new CropProfile(CropRatio.GOLDEN, CropGuide.RULE_OF_THIRDS),
        new CropProfile(CropRatio.GOLDEN, CropGuide.GOLDEN_SPIRAL),
        new CropProfile(CropRatio.A4, CropGuide.DIAGONAL)
    );

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
        
        CropProfile profile = profiles.get(profileIndex);
        float currentRatio = profile.ratio.ratio;
        if (!isLandscape && currentRatio != 1.0f) {
            currentRatio = 1.0f / currentRatio;
        }
        int frameHeight = Math.round(frameWidth / currentRatio);
        
        int x = mouseX - frameWidth/2;
        int y = mouseY - frameHeight/2;
        
        // Draw Dark Overlay outside frame
        Area screenArea = new Area(new Rectangle(0, 0, canvas.getWidth()*2, canvas.getHeight()*2));
        Area cropArea = new Area(new Rectangle(x, y, frameWidth, frameHeight));
        screenArea.subtract(cropArea);
        
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fill(screenArea);
        
        // Draw Frame Border
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(x, y, frameWidth, frameHeight);
        
        // Draw Composition Guide
        drawGuide(g2d, profile.guide, x, y, frameWidth, frameHeight);
        
        // Draw Mini-Filmstrip
        drawFilmstrip(g2d, x, y + frameHeight + 20);
        
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
        }
    }
    
    private void drawFilmstrip(Graphics2D g2d, int cx, int cy) {
        int itemSize = 40;
        int spacing = 10;
        int totalWidth = profiles.size() * itemSize + (profiles.size() - 1) * spacing;
        
        int startX = cx + (frameWidth / 2) - (totalWidth / 2);
        
        for (int i = 0; i < profiles.size(); i++) {
            CropProfile p = profiles.get(i);
            float r = p.ratio.ratio;
            if (!isLandscape && r != 1.0f) {
                r = 1.0f / r;
            }
            int fh = Math.round(itemSize / r);
            int yOffset = cy + (itemSize - fh)/2;
            
            if (i == profileIndex) {
                g2d.setColor(Color.YELLOW);
                g2d.drawRect(startX + i * (itemSize + spacing), yOffset, itemSize, fh);
                
                String label = p.ratio.label;
                FontMetrics fm = g2d.getFontMetrics();
                int labelWidth = fm.stringWidth(label);
                g2d.drawString(label, startX + i * (itemSize + spacing) + (itemSize - labelWidth)/2, cy + itemSize/2 + 20);
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
            profileIndex = (profileIndex - 1 + profiles.size()) % profiles.size();
            canvas.repaint();
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            profileIndex = (profileIndex + 1) % profiles.size();
            canvas.repaint();
        } else if (e.getKeyCode() == KeyEvent.VK_R) {
            isLandscape = !isLandscape;
            canvas.repaint();
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (isReviewMode) {
                isReviewMode = false;
                canvas.repaint();
            } else {
                canvas.setActiveTool(new HandTool());
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER && isReviewMode) {
            applyCrop(state, canvas);
        }
    }
    
    private void applyCrop(AppState state, ImageCanvas canvas) {
        CropProfile profile = profiles.get(profileIndex);
        float currentRatio = profile.ratio.ratio;
        if (!isLandscape && currentRatio != 1.0f) {
            currentRatio = 1.0f / currentRatio;
        }
        int frameHeight = Math.round(frameWidth / currentRatio);
        
        float zoom = state.getCurrentZoom();
        int ox = state.getCanvasState().getImageOffsetX();
        int oy = state.getCanvasState().getImageOffsetY();
        
        int unscaledX = Math.round((mouseX - frameWidth/2f - ox) / zoom);
        int unscaledY = Math.round((mouseY - frameHeight/2f - oy) / zoom);
        int unscaledW = Math.round(frameWidth / zoom);
        int unscaledH = Math.round(frameHeight / zoom);
        
        if (unscaledW <= 0 || unscaledH <= 0) return;
        Rectangle bounds = new Rectangle(unscaledX, unscaledY, unscaledW, unscaledH);
        
        java.awt.image.BufferedImage img = canvas.getBackgroundImage();
        if (img == null) return;
        
        CropCommand cmd = new CropCommand(canvas, state.getCanvasState(), img, bounds);
        state.getHistoryManager().push(cmd);
        canvas.setActiveTool(new HandTool());
    }
}
