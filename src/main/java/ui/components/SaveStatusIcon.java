package ui.components;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import javax.swing.*;
import java.awt.*;

public class SaveStatusIcon extends JLabel {
    
    private enum State { SAVED, DIRTY, SAVING }
    
    private final FlatSVGIcon savedIcon;
    private final FlatSVGIcon dirtyIcon;
    private final FlatSVGIcon savingIcon;
    
    private final Timer blinkTimer;
    private float alpha = 1.0f;
    private boolean increasing = false;
    private State currentState = State.SAVED;

    public SaveStatusIcon() {
        // Colors
        Color lightGray = new Color(180, 180, 180);
        Color savingGreen = Color.decode("#50f47d");

        // Load icons with color filters
        savedIcon = new FlatSVGIcon("icons/ic_file_saved.svg", 18, 18);
        savedIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> lightGray));
        
        dirtyIcon = new FlatSVGIcon("icons/ic_file_dirty.svg", 18, 18);
        dirtyIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> lightGray));
        
        savingIcon = new FlatSVGIcon("icons/ic_save.svg", 18, 18);
        savingIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> savingGreen));

        setIcon(savedIcon);
        setToolTipText("All changes saved");

        blinkTimer = new Timer(50, e -> {
            if (increasing) {
                alpha += 0.05f;
                if (alpha >= 1.0f) { alpha = 1.0f; increasing = false; }
            } else {
                alpha -= 0.05f;
                if (alpha <= 0.3f) { alpha = 0.3f; increasing = true; }
            }
            repaint();
        });
    }

    public void setDirty(boolean isDirty) {
        if (currentState == State.SAVING) return; // Saving state takes priority
        
        currentState = isDirty ? State.DIRTY : State.SAVED;
        setIcon(isDirty ? dirtyIcon : savedIcon);
        setToolTipText(isDirty ? "Unsaved changes" : "All changes saved");
        alpha = 1.0f;
        repaint();
    }

    public void startSaving() {
        currentState = State.SAVING;
        setIcon(savingIcon);
        setToolTipText("Auto-saving...");
        blinkTimer.start();
    }

    public void stopSaving() {
        // Keep the saving icon and blink state for 1 second before switching back
        Timer delayTimer = new Timer(1000, e -> {
            blinkTimer.stop();
            alpha = 1.0f;
            // Re-evaluate state (usually SAVED after a save, but could be DIRTY if user typed during save)
            // But AutoSaveManager sets isDirty=false after save.
            currentState = State.SAVED; 
            setIcon(savedIcon);
            setToolTipText("All changes saved");
            repaint();
        });
        delayTimer.setRepeats(false);
        delayTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        if (currentState == State.SAVING) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }
        super.paintComponent(g2);
        g2.dispose();
    }
}
