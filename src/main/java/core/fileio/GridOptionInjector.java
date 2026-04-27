package core.fileio;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class GridOptionInjector {

    private static ListCellRenderer<? super Object> originalRenderer;
    private static int originalOrientation = -1;
    private static boolean isOriginalSaved = false;
    private static JToggleButton gridBtn;

    public static void inject(JFileChooser chooser, int iconSize) {
        // 1. Phục hồi Listener bắt sự kiện đổi view để sửa lỗi Row Height cho Detail View
        hookViewTypeProperty(chooser, iconSize);

        // 2. Dùng Timer thăm dò (tối đa 20 lần ~ 4 giây) chờ UI dựng xong để bơm nút Grid
        Timer initTimer = new Timer(200, null);
        initTimer.addActionListener(new java.awt.event.ActionListener() {
            int attempts = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                attempts++;
                AbstractButton listBtn = findListViewButton(chooser);
                if (listBtn != null) {
                    initTimer.stop();
                    injectButtonToToolbar(chooser, listBtn, iconSize);
                } else if (attempts > 20) {
                    initTimer.stop();
                    System.err.println("Timeout: Không tìm thấy thanh công cụ để chèn nút Grid.");
                }
            }
        });
        initTimer.start();
    }

    // --- Xử lý sự kiện chuyển View ẩn của FilePane ---
    private static void hookViewTypeProperty(JFileChooser chooser, int iconSize) {
        chooser.updateUI(); // Ép hệ thống khởi tạo cây Component

        java.beans.PropertyChangeListener viewTypeListener = evt -> {
            if ("viewType".equals(evt.getPropertyName())) {
                int viewType = (int) evt.getNewValue();
                SwingUtilities.invokeLater(() -> {
                    if (viewType == 0) { // Chuyển về List View
                        // Chỉ phục hồi List gốc nếu nút Grid KHÔNG ĐƯỢC CHỌN
                        if (gridBtn == null || !gridBtn.isSelected()) {
                            restoreListView(chooser);
                        }
                    } else if (viewType == 1) { // Chuyển sang Detail View
                        if (gridBtn != null) gridBtn.setSelected(false);
                        fixDetailViewHeight(chooser, iconSize);
                    }
                });
            }
        };
        attachListenerRecursively(chooser, viewTypeListener);
    }

    private static void attachListenerRecursively(Component c, java.beans.PropertyChangeListener listener) {
        c.addPropertyChangeListener("viewType", listener);
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                attachListenerRecursively(child, listener);
            }
        }
    }

    // --- Bơm nút vào Toolbar ---
    private static void injectButtonToToolbar(JFileChooser chooser, AbstractButton listViewBtn, int iconSize) {
        Container toolbar = listViewBtn.getParent();

        // Kiểm tra tránh chèn đúp nút
        for (Component c : toolbar.getComponents()) {
            if (c instanceof JToggleButton && "GridViewBtn".equals(c.getName())) return;
        }

        gridBtn = new JToggleButton();
        gridBtn.setName("GridViewBtn");
        gridBtn.setToolTipText("Grid View");

        // --- BÍ QUYẾT ĐỒNG BỘ STYLE VỚI NÚT NATIVE ---
        // Copy chính xác các thuộc tính hiển thị từ nút List mặc định
        gridBtn.setBorderPainted(listViewBtn.isBorderPainted());
        gridBtn.setContentAreaFilled(listViewBtn.isContentAreaFilled());
        gridBtn.setFocusPainted(listViewBtn.isFocusPainted());
        gridBtn.setRolloverEnabled(listViewBtn.isRolloverEnabled());
        gridBtn.setOpaque(listViewBtn.isOpaque());
        gridBtn.setBorder(listViewBtn.getBorder()); // Chép luôn hiệu ứng viền khi Hover của OS
        // ----------------------------------------------

        gridBtn.setIcon(createLineGridIcon());

        // (Giữ nguyên phần code Logic Action như cũ)
        gridBtn.addActionListener(e -> {
            gridBtn.setSelected(true);
            ActionMap am = getFilePaneActionMap(chooser);
            if (am != null && am.get("viewTypeList") != null) {
                am.get("viewTypeList").actionPerformed(new ActionEvent(gridBtn, ActionEvent.ACTION_PERFORMED, "viewTypeList"));
            } else {
                listViewBtn.doClick();
            }
            SwingUtilities.invokeLater(() -> applyGridView(chooser, iconSize));
        });

        listViewBtn.addActionListener(e -> {
            if (gridBtn != null) gridBtn.setSelected(false);
        });

        toolbar.add(gridBtn);
        toolbar.revalidate();
        toolbar.repaint();

        SwingUtilities.invokeLater(() -> fixDetailViewHeight(chooser, iconSize));
    }

    // --- Các hàm áp dụng Layout ---
    private static void applyGridView(JFileChooser chooser, int iconSize) {
        JList<?> list = findJList(chooser);
        if (list != null) {
            if (!isOriginalSaved) {
                originalRenderer = (ListCellRenderer<? super Object>) list.getCellRenderer();
                originalOrientation = list.getLayoutOrientation();
                isOriginalSaved = true;
            }
            list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
            list.setVisibleRowCount(0);
            list.setFixedCellWidth(iconSize + 40);
            list.setFixedCellHeight(iconSize + 50); // Thêm chút không gian cho Text
            list.setCellRenderer(new GridCellRenderer(chooser));
        }
    }

    private static void restoreListView(JFileChooser chooser) {
        JList<?> list = findJList(chooser);
        if (list != null && isOriginalSaved) {
            list.setLayoutOrientation(originalOrientation);
            list.setVisibleRowCount(-1);
            list.setFixedCellWidth(-1);
            list.setFixedCellHeight(-1);
            if (originalRenderer != null) {
                list.setCellRenderer(originalRenderer);
            }
        }
    }

    private static void fixDetailViewHeight(JFileChooser chooser, int iconSize) {
        JTable table = findTable(chooser);
        if (table != null) {
            int newHeight = iconSize + 4;
            if (table.getRowHeight() < newHeight) {
                table.setRowHeight(newHeight);
            }
        }
    }

    // --- Các hàm tìm kiếm Component ---
    private static AbstractButton findListViewButton(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof AbstractButton) {
                AbstractButton btn = (AbstractButton) comp;
                Action action = btn.getAction();
                String tooltip = btn.getToolTipText();

                // Thuật toán nhận diện rộng hơn: qua Action Command HOẶC Tooltip
                if ((action != null && "viewTypeList".equals(action.getValue(Action.NAME))) ||
                        (tooltip != null && tooltip.toLowerCase().contains("list"))) {
                    return btn;
                }
            } else if (comp instanceof Container) {
                AbstractButton found = findListViewButton((Container) comp);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static ActionMap getFilePaneActionMap(Container c) {
        for (Component comp : c.getComponents()) {
            if (comp.getClass().getName().endsWith("FilePane")) {
                return ((JComponent) comp).getActionMap();
            }
            if (comp instanceof Container) {
                ActionMap am = getFilePaneActionMap((Container) comp);
                if (am != null) return am;
            }
        }
        return null;
    }

    private static JList<?> findJList(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JList) return (JList<?>) comp;
            if (comp instanceof Container) {
                JList<?> found = findJList((Container) comp);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static JTable findTable(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTable) return (JTable) comp;
            if (comp instanceof Container) {
                JTable found = findTable((Container) comp);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static Icon createLineGridIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                // Bật khử răng cưa để nét vẽ mượt mà, không bị gai
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Dùng tông xám trung tính, tiệp màu với nét vẽ icon hệ thống
                g2d.setColor(new Color(100, 100, 100));

                // Đặt độ dày nét vẽ là 1.2 pixel để tạo cảm giác "Line Icon" thanh mảnh
                g2d.setStroke(new BasicStroke(1.2f));

                // Vẽ 4 ô vuông (Layout 2x2)
                int size = 5;
                int gap = 2;
                int startX = x + 2;
                int startY = y + 2;

                g2d.drawRect(startX, startY, size, size); // Ô trên trái
                g2d.drawRect(startX + size + gap, startY, size, size); // Ô trên phải
                g2d.drawRect(startX, startY + size + gap, size, size); // Ô dưới trái
                g2d.drawRect(startX + size + gap, startY + size + gap, size, size); // Ô dưới phải

                g2d.dispose();
            }
            @Override
            public int getIconWidth() { return 16; }
            @Override
            public int getIconHeight() { return 16; }
        };
    }

    // --- Custom Renderer ---
    private static class GridCellRenderer extends DefaultListCellRenderer {
        private final JFileChooser chooser;

        public GridCellRenderer(JFileChooser chooser) {
            this.chooser = chooser;
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.TOP);
            setHorizontalTextPosition(SwingConstants.CENTER);
            setVerticalTextPosition(SwingConstants.BOTTOM);
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof java.io.File) {
                java.io.File file = (java.io.File) value;
                String fileName = chooser.getName(file);
                if (fileName != null && fileName.length() > 15) {
                    fileName = fileName.substring(0, 12) + "...";
                }
                setText(fileName);
                setIcon(chooser.getIcon(file));
                setToolTipText(chooser.getName(file));
            }
            return this;
        }
    }
}