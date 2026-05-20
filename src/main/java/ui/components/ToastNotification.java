package ui.components;

import ui.MainFrame;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class ToastNotification extends JPanel {
    private final String message;
    private final JFrame frame;
    private final Timer timer;
    private final ComponentAdapter resizeListener;

    public ToastNotification(JFrame frame, String message) {
        this.frame = frame;
        this.message = message;
        
        setOpaque(false);
        setLayout(new BorderLayout(12, 0));
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        
        JLabel label = new JLabel(message);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        add(label, BorderLayout.CENTER);
        
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(new Color(46, 125, 50));
                g2.fillOval(0, 0, 16, 16);
                
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(4, 8, 7, 11);
                g2.drawLine(7, 11, 12, 5);
                g2.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(16, 16);
            }
        };
        iconPanel.setOpaque(false);
        add(iconPanel, BorderLayout.WEST);
        
        timer = new Timer(3500, e -> dismiss());
        timer.setRepeats(false);
        
        resizeListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateBounds();
            }
        };
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor(new Color(30, 30, 30, 220));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
        
        g2.setColor(new Color(255, 255, 255, 30));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
        g2.dispose();
        super.paintComponent(g);
    }
    
    private void updateBounds() {
        Dimension toastSize = getPreferredSize();
        int x = frame.getWidth() - toastSize.width - 20;
        int y = frame.getHeight() - toastSize.height - 20;
        
        setBounds(x, y, toastSize.width, toastSize.height);
        revalidate();
    }
    
    public void showToast() {
        timer.start();
        frame.getLayeredPane().add(this, JLayeredPane.POPUP_LAYER);
        frame.addComponentListener(resizeListener);
        updateBounds();
        frame.getLayeredPane().repaint();
    }
    
    public void dismiss() {
        timer.stop();
        frame.removeComponentListener(resizeListener);
        Container parent = getParent();
        if (parent != null) {
            parent.remove(this);
            parent.repaint();
        }
    }
    
    public static void show(String message) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = findMainFrame();
            if (frame != null) {
                ToastNotification toast = new ToastNotification(frame, message);
                toast.showToast();
            }
        });
    }
    
    public static JFrame findMainFrame() {
        for (Frame f : Frame.getFrames()) {
            if (f instanceof JFrame && f.isVisible()) {
                return (JFrame) f;
            }
        }
        return null;
    }
}
