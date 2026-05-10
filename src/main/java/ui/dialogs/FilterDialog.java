package ui.dialogs;

import filter.FilterProperties;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import workers.FilterWorker;

public class FilterDialog extends JDialog {

    private enum FilterFlag {
            GRAY, ORANGE, YELLOW
    }

    private boolean isUpdatingUI = false;
    private FilterFlag filterFlag = FilterFlag.GRAY;
    private final BufferedImage originalImage;
    private BufferedImage basePreviewSource;
    private final BiConsumer<BufferedImage, FilterProperties> onApply;
    private final Consumer<FilterProperties> onLivePreview;

    private JSlider redSlider, greenSlider, blueSlider, alphaSlider, graySlider;
    private JComboBox<String> filterModeCombo;
    private ImagePreviewPanel previewPanel;
    private JComboBox<String> presetCombo;
    private JCheckBox livePreviewCheckbox;
    
    private FilterWorker currentWorker; // Keep track to avoid too many running

    public FilterDialog(
            Frame owner,
            BufferedImage image,
            BiConsumer<BufferedImage, FilterProperties> onApply,
            Consumer<FilterProperties> onLivePreview) {
        super(owner, "Image Filters", false); // Non-modal so user can see main canvas
        this.originalImage = image;
        createBasePreviewSource();
        
        this.onApply = onApply;
        this.onLivePreview = onLivePreview;
        setupUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void setSliderValues(int r, int g, int b, int a, int gray, int modeIndex) {
        filterModeCombo.setSelectedIndex(modeIndex);
        redSlider.setValue(r);
        greenSlider.setValue(g);
        blueSlider.setValue(b);
        alphaSlider.setValue(a);
        graySlider.setValue(gray);
    }

    private void createBasePreviewSource() {
        if (originalImage != null) {
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            // 1. Tính toán kích thước mới duy trì Aspect Ratio
            int targetWidth = 500;
            // Công thức nhân chéo: targetH = (originalH * targetW) / originalW
            int targetHeight = (int) Math.round((double) originalHeight * targetWidth / originalWidth);

            // (Optional) Chống phóng to: Nếu ảnh gốc nhỏ hơn 300px, giữ nguyên
            if (originalWidth < 500) {
                targetWidth = originalWidth;
                targetHeight = originalHeight;
            }

            Image scaledSource = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);

            basePreviewSource = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = basePreviewSource.createGraphics();
            g2.drawImage(scaledSource, 0, 0, null);
            g2.dispose();
        }
    }

    private void setupUI() {
        setLayout(new BorderLayout(10, 10));

        // ==========================================
        // 1. TOP PANEL (Preview + Combos)
        // ==========================================
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));

        // Preview Area
        previewPanel = new ImagePreviewPanel();
        previewPanel.setPreferredSize(new Dimension(300, 300));
        previewPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Initial preview (scaled down if needed)
        if (basePreviewSource != null) {
            previewPanel.setOriginalImage(basePreviewSource);
        }
        topPanel.add(new JScrollPane(previewPanel), BorderLayout.CENTER);

        // Combos Area (Xếp dọc bằng BoxLayout)
        JPanel comboPanel = new JPanel();
        comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.Y_AXIS));
        comboPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        // Mode Combo với TitledBorder
        JPanel modeWrapper = new JPanel(new BorderLayout());
        modeWrapper.setBorder(BorderFactory.createTitledBorder("Mode"));
        filterModeCombo = new JComboBox<>(new String[]{"RGB", "Black & White"});
        filterModeCombo.addActionListener(e -> {
            if(filterModeCombo.getSelectedIndex() == 0 && presetCombo.getSelectedIndex() != 1){
                isUpdatingUI = true;
                presetCombo.setSelectedIndex(0);
                isUpdatingUI = false;
            }
            if (!isUpdatingUI) {
                firePreviewUpdate();
                fireLivePreviewToCanvas();
            }
        });
        modeWrapper.add(filterModeCombo, BorderLayout.CENTER);
        modeWrapper.setMaximumSize(new Dimension(250, 50)); // Giới hạn chiều cao trong BoxLayout

        // Preset Combo với TitledBorder
        JPanel presetWrapper = new JPanel(new BorderLayout());
        presetWrapper.setBorder(BorderFactory.createTitledBorder("Preset"));
        String[] presets = {
                "Custom", "Default RGB", "RGB Black & White",
                "Gray with Blue filter", "Gray with Red filter",
                "Gray with Green filter", "Gray with Orange filter",
                "Gray with Yelow filter", "Red off", "Green off", "Blue off"
        };
        presetCombo = new JComboBox<>(presets);
        presetCombo.addActionListener(e -> handlePresetChange()); // Tách logic ra hàm riêng cho gọn
        presetWrapper.add(presetCombo, BorderLayout.CENTER);
        presetWrapper.setMaximumSize(new Dimension(250, 50));

        // Thêm vào Combo Panel
        comboPanel.add(modeWrapper);
        comboPanel.add(Box.createRigidArea(new Dimension(0, 15))); // Khoảng cách giữa 2 combo
        comboPanel.add(presetWrapper);

        comboPanel.add(Box.createRigidArea(new Dimension(0, 15))); // Khoảng cách
        livePreviewCheckbox = new JCheckBox("Preview on Canvas");
        livePreviewCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Bắt sự kiện khi tích/bỏ tích Checkbox
        livePreviewCheckbox.addActionListener(e -> fireLivePreviewToCanvas());

        comboPanel.add(livePreviewCheckbox);

        topPanel.add(comboPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.CENTER);


        // ==========================================
        // 2. BOTTOM PANEL (Sliders + Buttons)
        // ==========================================
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));

        // Controls Area (Sliders)
        JPanel slidersPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        slidersPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        // Gọi hàm createSlider mới với Màu sắc và Label viết tắt
        // Đã cập nhật Gray Threshold max = 128, default = 64 (giữa)
        redSlider = createSlider("R", Color.RED, 0, 200, 100, slidersPanel);
        greenSlider = createSlider("G", new Color(0, 150, 0), 0, 200, 100, slidersPanel); // Xanh lá đậm cho dễ nhìn trên nền xám
        blueSlider = createSlider("B", Color.BLUE, 0, 200, 100, slidersPanel);
        alphaSlider = createSlider("A", Color.GRAY, 0, 100, 100, slidersPanel);
        graySlider = createSlider("Gray", Color.DARK_GRAY, 0, 128, 128, slidersPanel);

        bottomPanel.add(slidersPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnApply = new JButton("Apply");
        JButton btnCancel = new JButton("Cancel");

        btnApply.setBackground(Color.decode("#0078D7"));
        btnApply.setForeground(Color.WHITE);
        btnApply.setOpaque(true);
        btnApply.setBorderPainted(false);

        getRootPane().setDefaultButton(btnApply);

        btnApply.addActionListener(e -> {
            btnApply.setEnabled(false);
            btnApply.setText("Applying...");
            FilterProperties props = getCurrentProperties();
            new FilterWorker(originalImage, props, result -> {
                if (onApply != null) {
                    onApply.accept(result, props);
                }
                dispose();
            }).execute();
        });

        btnCancel.addActionListener(e -> {
            if(livePreviewCheckbox.isSelected() && onLivePreview != null) {
                onLivePreview.accept(null);
            }
            dispose();
        });

        buttonPanel.add(btnApply);
        buttonPanel.add(btnCancel);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void fireLivePreviewToCanvas() {
        boolean isLive = livePreviewCheckbox.isSelected();
        FilterProperties props = getCurrentProperties();
        if (onLivePreview != null) {
                 // Nếu isLive = true, truyền props ra để Canvas tự chạy worker vẽ mờ.
                 // Nếu isLive = false, truyền null để Canvas reset về ảnh gốc.
                 onLivePreview.accept(isLive ? props : null);
        }
    }

    private void handlePresetChange() {
        if (isUpdatingUI) return;

        String selectedPreset = (String) presetCombo.getSelectedItem();
        if ("Custom".equals(selectedPreset)) return;

        isUpdatingUI = true;
        filterFlag = FilterFlag.GRAY;

        switch (selectedPreset) {
            case "Default RGB":
                setSliderValues(100, 100, 100, 100, 128, 0); // Chú ý gray default về 64
                break;
            case "RGB Black & White":
                setSliderValues(100, 100, 100, 100, 128, 1);
                break;
            case "Gray with Blue filter":
                setSliderValues(0, 0, 100, 100, 128, 1);
                break;
            case "Gray with Red filter":
                setSliderValues(100, 0, 0, 100, 128, 1);
                break;
            case "Gray with Green filter":
                setSliderValues(0, 100, 0, 100, 128, 1);
                break;
            case "Gray with Orange filter":
                setSliderValues(50, 50, 0, 100, 128, 1); // Tùy chỉnh thông số thực tế
                filterFlag = FilterFlag.ORANGE;
                break;
            case "Gray with Yelow filter":
                setSliderValues(34, 66, 0, 100, 128, 1); // Tùy chỉnh thông số thực tế
                filterFlag = FilterFlag.YELLOW;
                break;
            case "Red off":
                setSliderValues(0, 100, 100, 100, 128, 1);
                break;
            case "Green off":
                setSliderValues(100, 0, 100, 100, 128, 1);
                break;
            case "Blue off":
                setSliderValues(100, 100, 0, 100, 128, 1);
                break;
        }

        isUpdatingUI = false; // Tắt cờ
        firePreviewUpdate(); // Chạy update 1 lần duy nhất sau khi đã kéo xong toàn bộ slider
        fireLivePreviewToCanvas();
    }

    private JSlider createSlider(String name, Color labelColor, int min, int max, int value, JPanel parent) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));

        // Cấu hình Label hiển thị tên (R, G, B, A)
        JLabel nameLabel = new JLabel(name);
        nameLabel.setForeground(labelColor);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD)); // In đậm để nhìn rõ màu hơn
        // Ép kích thước cố định cho Label để gióng thẳng hàng các Slider gutter
        nameLabel.setPreferredSize(new Dimension(35, 20));
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(nameLabel, BorderLayout.WEST);
        
        JSlider slider = new JSlider(min, max, value);
        slider.setMajorTickSpacing((max - min) / 2);
        slider.setPaintTicks(true);
        
        JLabel valueLabel = new JLabel(String.valueOf(value));
        valueLabel.setPreferredSize(new Dimension(30, 20));
        panel.add(valueLabel, BorderLayout.EAST);

        // Logic lắng nghe sự kiện
        slider.addChangeListener(e -> {
            valueLabel.setText(String.valueOf(slider.getValue()));

            // Nếu sự kiện sinh ra do người dùng kéo chuột (không phải do code set)
            if (!isUpdatingUI) {
                isUpdatingUI = true; // Khóa cờ lại
                presetCombo.setSelectedItem("Custom"); // Tự động nhảy sang Custom
                isUpdatingUI = false; // Mở cờ ra

                firePreviewUpdate(); // Chạy update ảnh
                fireLivePreviewToCanvas();
            }
        });

        panel.add(slider, BorderLayout.CENTER);
        parent.add(panel);
        return slider;
    }

    private FilterProperties getCurrentProperties() {
        int r = redSlider.getValue();
        int g = greenSlider.getValue();
        int b = blueSlider.getValue();
        int a = alphaSlider.getValue();
        if(filterFlag == FilterFlag.ORANGE ||  filterFlag == FilterFlag.YELLOW) {
            r *= 2;
            g *= 2;
            b *= 2;
        }
        int gray = graySlider.getValue();
        int mode = filterModeCombo.getSelectedIndex() == 0 ? 0 : -1; // 0 for RGB, -1 for BW in legacy

        String presetName = (String) presetCombo.getSelectedItem();
        FilterProperties props = new FilterProperties(r, g, b, a, gray, mode);
        props.setPresetName(presetName);
        return props;
    }

    private void firePreviewUpdate() {
        if (basePreviewSource == null) return;
        
        FilterProperties props = getCurrentProperties();
        
        // Cancel the old worker if it's still running
        if (currentWorker != null && !currentWorker.isDone()) {
            currentWorker.cancel(true);
        }
        
        // Spawn a background worker just for the live preview window
        currentWorker = new FilterWorker(basePreviewSource, props, resultImage -> {
            previewPanel.setFilteredImage(resultImage);
        });
        currentWorker.execute();
    }

    public class ImagePreviewPanel extends JPanel {
        private BufferedImage originalImage;
        private BufferedImage filteredImage;
        private boolean isShowingOriginal = false;

        public ImagePreviewPanel() {
            setDoubleBuffered(true); // Chống chớp màn hình

            // Thêm Listener để xử lý sự kiện Before/After
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    // Chỉ bật chế độ so sánh khi đã có cả ảnh gốc và ảnh filter
                    if (originalImage != null && filteredImage != null) {
                        isShowingOriginal = true;
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isShowingOriginal = false;
                    repaint();
                }
            });
        }

        // Gán ảnh gốc (sẽ gọi 1 lần khi khởi tạo Dialog)
        public void setOriginalImage(BufferedImage image) {
            this.originalImage = image;
            repaint();
        }

        // Gán ảnh đã qua xử lý (sẽ gọi liên tục khi kéo Slider)
        public void setFilteredImage(BufferedImage image) {
            this.filteredImage = image;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Quyết định xem sẽ vẽ ảnh nào dựa trên trạng thái chuột
            BufferedImage imageToDraw = isShowingOriginal ? originalImage :
                    (filteredImage != null ? filteredImage : originalImage);

            if (imageToDraw != null) {
                Graphics2D g2d = (Graphics2D) g.create();

                // Khử răng cưa và cấu hình render chất lượng cao
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int panelWidth = getWidth();
                int panelHeight = getHeight();

                double imgW = imageToDraw.getWidth();
                double imgH = imageToDraw.getHeight();

                double scale = Math.min(panelWidth / imgW, panelHeight / imgH);

                int drawW = (int) (imgW * scale);
                int drawH = (int) (imgH * scale);

                int x = (panelWidth - drawW) / 2;
                int y = (panelHeight - drawH) / 2;

                g2d.drawImage(imageToDraw, x, y, drawW, drawH, null);

                // Tùy chọn: Vẽ một chữ "Original" mờ góc trên cùng bên trái để user biết họ đang xem ảnh gốc
                if (isShowingOriginal) {
                    g2d.setColor(new Color(0, 0, 0, 150));
                    g2d.fillRect(10, 10, 60, 20);
                    g2d.setColor(Color.WHITE);
                    g2d.drawString("Original", 15, 24);
                }

                g2d.dispose();
            }
        }
    }
}
