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
    
    private int profileIndex = 0;
    private final List<CropProfile> profiles = Arrays.asList(
        new CropProfile(CropRatio.FREEFORM, CropGuide.RULE_OF_THIRDS),
        new CropProfile(CropRatio.SQUARE, CropGuide.RULE_OF_THIRDS),
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
        
        CropProfile profile = profiles.get(profileIndex);
        int frameHeight = profile.ratio.ratio == 0 ? frameWidth : (int)(frameWidth / profile.ratio.ratio);
        
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
            // Simplified golden spiral visualization
            g2d.drawArc(x, y, w, h, 0, 90);
        }
    }
    
    private void drawFilmstrip(Graphics2D g2d, int cx, int cy) {
        int itemSize = 40;
        int spacing = 10;
        int totalWidth = profiles.size() * itemSize + (profiles.size() - 1) * spacing;
        
        int startX = cx + (frameWidth / 2) - (totalWidth / 2);
        
        for (int i = 0; i < profiles.size(); i++) {
            CropProfile p = profiles.get(i);
            int fh = p.ratio.ratio == 0 ? itemSize : (int)(itemSize / p.ratio.ratio);
            int yOffset = cy + (itemSize - fh)/2;
            
            g2d.setColor(i == profileIndex ? Color.YELLOW : new Color(255, 255, 255, 100));
            g2d.drawRect(startX + i * (itemSize + spacing), yOffset, itemSize, fh);
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
        int frameHeight = profile.ratio.ratio == 0 ? frameWidth : (int)(frameWidth / profile.ratio.ratio);
        
        float zoom = state.getCurrentZoom();
        int ox = state.getCanvasState().getImageOffsetX();
        int oy = state.getCanvasState().getImageOffsetY();
        
        int unscaledX = Math.round((mouseX - frameWidth/2 - ox) / zoom);
        int unscaledY = Math.round((mouseY - frameHeight/2 - oy) / zoom);
        int unscaledW = Math.round(frameWidth / zoom);
        int unscaledH = Math.round(frameHeight / zoom);
        
        Rectangle bounds = new Rectangle(unscaledX, unscaledY, unscaledW, unscaledH);
        
        // Clamp to image bounds
        java.awt.image.BufferedImage img = canvas.getBackgroundImage();
        if (img == null) return;
        
        if (bounds.x < 0) { bounds.width += bounds.x; bounds.x = 0; }
        if (bounds.y < 0) { bounds.height += bounds.y; bounds.y = 0; }
        if (bounds.x + bounds.width > img.getWidth()) bounds.width = img.getWidth() - bounds.x;
        if (bounds.y + bounds.height > img.getHeight()) bounds.height = img.getHeight() - bounds.y;

        if (bounds.width > 0 && bounds.height > 0) {
            CropCommand cmd = new CropCommand(canvas, state.getCanvasState(), img, bounds);
            state.getHistoryManager().push(cmd);
            canvas.setActiveTool(new HandTool());
        }
    }
}
