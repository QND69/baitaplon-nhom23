package com.example.farmSimulation.config;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

// Chứa các hằng số cấu hình cho Hotbar (Thanh công cụ)
public class HotbarConfig {

    public static final int HOTBAR_SLOT_COUNT = 10; // Số lượng ô

    // --- Giá trị CƠ SỞ (BASE) ---
    // Các giá trị này sẽ được nhân với hotbarScale
    public static final double BASE_SLOT_SIZE = 100.0; // Kích thước mỗi ô (ở scale 0.5)
    public static final double BASE_SLOT_SPACING = 10.0; // Khoảng cách giữa các ô (ở scale 0.5)
    public static final double BASE_STROKE_WIDTH = 4.0; // Độ dày viền (ở scale 0.5)
    public static final double BASE_Y_OFFSET = 60.0; // Khoảng cách từ đáy màn hình (ở scale 0.5)

    // --- Hằng số Scale ---
    public static final double DEFAULT_HOTBAR_SCALE = 0.6; // Tỉ lệ mặc định của hotbar

    /**
     * Hằng số "item scale"
     * Quyết định kích thước của Item (icon) so với ô (Slot).
     */
    public static final double ITEM_SCALE_RATIO = 0.8;

    // Màu sắc
    public static final Paint SLOT_BACKGROUND_COLOR = Color.rgb(0, 0, 0, 0.5); // Nền ô
    public static final Paint SLOT_BORDER_COLOR = Color.gray(0.7); // Viền ô
    public static final Paint SLOT_SELECTED_BORDER_COLOR = Color.GOLD; // Viền ô được chọn

    private HotbarConfig() {}
}