package ui.dialogs;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;

public class ScaleCalculatorDialog extends JDialog {

    private JTextField pxField;
    private JTextField cmField;

    public ScaleCalculatorDialog(Dialog owner, JTextField targetScaleField) {
        super(owner, "Tính tỷ lệ tự động", true); // true = Modal dialog

        JPanel panel = new JPanel(new MigLayout("wrap 2, gap 10px", "[right][grow, fill]"));

        panel.add(new JLabel("Chiều rộng ảnh (px):"));
        pxField = new JTextField(10);
        panel.add(pxField);

        panel.add(new JLabel("Chiều rộng giấy (cm):"));
        cmField = new JTextField(10);
        panel.add(cmField);

        // Nút bấm
        JButton applyBtn = new JButton("Áp dụng");
        JButton cancelBtn = new JButton("Hủy");

        // Gom 2 nút xuống góc dưới cùng bên phải
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(applyBtn);
        btnPanel.add(cancelBtn);

        panel.add(btnPanel, "span 2, growx, pushx");

        // --- LOGIC XỬ LÝ ---
        applyBtn.addActionListener(e -> {
            try {
                double px = Double.parseDouble(pxField.getText().trim());
                double cm = Double.parseDouble(cmField.getText().trim());

                if (px <= 0 || cm <= 0) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập số lớn hơn 0!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Tính toán tỷ lệ: cm / px
                double ratio = cm / px;

                // Format số để tránh ra một dãy quá dài (ví dụ: lấy 6 số thập phân)
                targetScaleField.setText(String.format("%.6f", ratio).replace(",", "."));

                // Đóng dialog
                dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng chỉ nhập số hợp lệ!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dispose());

        // Cấu hình Dialog
        this.add(panel);
        this.pack();
        this.setLocationRelativeTo(owner); // Hiển thị ngay giữa dialog cha
        this.setResizable(false);
        this.getRootPane().setDefaultButton(applyBtn);
    }
}
