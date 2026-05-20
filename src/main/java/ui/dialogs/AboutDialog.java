package ui.dialogs;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import utils.BuildInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

public class AboutDialog extends JDialog {

    public AboutDialog(Frame owner) {
        super(owner, "About Portrait Tool", true);
        initComponents();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(20, 20));
        
        // Root padding
        JPanel contentPanel = new JPanel(new BorderLayout(20, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));
        
        // Logo Label
        JLabel logoLabel = new JLabel();
        java.net.URL logoUrl = getClass().getResource("/icons/app_logo.svg");
        if (logoUrl != null) {
            logoLabel.setIcon(new FlatSVGIcon("icons/app_logo.svg", 96, 96));
        } else {
            logoLabel.setText("[ Logo ]");
            logoLabel.setPreferredSize(new Dimension(96, 96));
            logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            logoLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        }
        logoLabel.setVerticalAlignment(SwingConstants.TOP);
        contentPanel.add(logoLabel, BorderLayout.WEST);
        
        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        
        // App name
        JLabel nameLabel = new JLabel("Portrait Tool");
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(nameLabel);
        
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        // Version
        JLabel verLabel = new JLabel("Version "+BuildInfo.getVersion());
        verLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        verLabel.setForeground(Color.GRAY);
        verLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(verLabel);

        JLabel buildInfoLabel = new JLabel();
        buildInfoLabel.setText("Build: "+ BuildInfo.getBuildCount() + " - "+ BuildInfo.getCommit());
        buildInfoLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        buildInfoLabel.setForeground(Color.GRAY);
        buildInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(buildInfoLabel);
        
        infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Description
        JLabel descLabel = new JLabel("<html><body style='width: 250px;'>App dùng để hỗ trợ dựng hình khi vẽ chân dung.</body></html>");
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(descLabel);
        
        infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Separator line
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(separator);
        
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Author details
        JLabel authorLabel = new JLabel("Nhà phát triển: Phí Thành Đạt");
        authorLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        authorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(authorLabel);
        
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        // Email Link
        JLabel emailLabel = new JLabel("Email: phithanhdathd@gmail.com");
        emailLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        emailLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        emailLabel.setForeground(new Color(0, 102, 204));
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openUrl("mailto:phithanhdathd@gmail.com");
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                emailLabel.setText("<html><u>Email: phithanhdathd@gmail.com</u></html>");
            }
            @Override
            public void mouseExited(MouseEvent e) {
                emailLabel.setText("Email: phithanhdathd@gmail.com");
            }
        });
        infoPanel.add(emailLabel);
        
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        // GitHub Link
        JLabel githubLabel = new JLabel("GitHub: Portrait-Tool");
        githubLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        githubLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        githubLabel.setForeground(new Color(0, 102, 204));
        githubLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        githubLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openUrl("https://github.com/thanhdathd/Portrait-Tool");
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                githubLabel.setText("<html><u>GitHub: Portrait-Tool</u></html>");
            }
            @Override
            public void mouseExited(MouseEvent e) {
                githubLabel.setText("GitHub: Portrait-Tool");
            }
        });
        infoPanel.add(githubLabel);
        
        contentPanel.add(infoPanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
        
        // Close Button Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 15, 20));
        JButton closeBtn = new JButton("Đóng");
        closeBtn.addActionListener(e -> dispose());
        btnPanel.add(closeBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }
    
    private void openUrl(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
