package com.example.farmSimulation.config;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

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
    public static final double ITEM_SCALE_RATIO = 0.7;

    // Màu sắc
    public static final Paint SLOT_BACKGROUND_COLOR = Color.rgb(0, 0, 0, 0.5); // Nền ô
    public static final Paint SLOT_BORDER_COLOR = Color.gray(0.7); // Viền ô
    public static final Paint SLOT_SELECTED_BORDER_COLOR = Color.GOLD; // Viền ô được chọn

    // --- CẤU HÌNH THANH ĐỘ BỀN ---
    public static final double DURABILITY_BAR_HEIGHT = 5.0; // Chiều cao thanh
    public static final double DURABILITY_BAR_WIDTH_RATIO = 0.8; // Chiều rộng so với ô
    public static final double DURABILITY_BAR_Y_OFFSET = -12.0; // Cách đáy ô bao nhiêu
    public static final Paint DURABILITY_COLOR_HIGH = Color.LIME;
    public static final Paint DURABILITY_COLOR_MEDIUM = Color.YELLOW;
    public static final Paint DURABILITY_COLOR_LOW = Color.RED;
    public static final Paint DURABILITY_BG_COLOR = Color.rgb(0, 0, 0, 0.6);

    // Dịch chuyển icon lên trên một chút để nhường chỗ cho thanh độ bền
    public static final double ICON_Y_TRANSLATE = -5.0;

    // --- CẤU HÌNH HOTBAR TEXT ---
    public static boolean SHOW_DURABILITY_BAR = true; // Bật/tắt hiển thị thanh độ bền (có thể chỉnh trong Settings)
    public static final double HOTBAR_NUMBER_FONT_SIZE = 11.0; // Số thứ tự (1-9)
    public static final double HOTBAR_QUANTITY_FONT_SIZE = 14.0; // Số lượng (x36)
    public static final Font HOTBAR_NUMBER_FONT = Font.font("Arial", FontWeight.BOLD, HOTBAR_NUMBER_FONT_SIZE);
    public static final Font HOTBAR_QUANTITY_FONT = Font.font("Arial", FontWeight.BOLD, HOTBAR_QUANTITY_FONT_SIZE);
    public static final Color HOTBAR_TEXT_COLOR = Color.WHITE;
    public static final Color HOTBAR_TEXT_STROKE_COLOR = Color.BLACK;
    public static final double HOTBAR_TEXT_STROKE_WIDTH = 0.5;
    public static final double HOTBAR_TEXT_PADDING = 6.0;

    private HotbarConfig() {}
}