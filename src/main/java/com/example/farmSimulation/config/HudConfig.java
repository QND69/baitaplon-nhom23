package com.example.farmSimulation.config;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

// Chứa các hằng số cấu hình cho Giao diện Người dùng (HUD)
public class HudConfig {

    // --- TEXT THÔNG BÁO (trên đầu Player) ---
    public static final String TOO_FAR_TEXT = "It's too far"; // Nội dung text
    public static final String WRONG_TOOL_TEXT = "Can't use this here";
    public static final String TEXT_INVENTORY_FULL = "Inventory Full";
    public static final String TEXT_PLANT_CAN_NOT_BE_FERTILIZED = "This plant cannot be fertilized";
    public static final String TEXT_WATER_EMPTY = "Watering Can is empty";

    public static final Paint TEMP_TEXT_COLOR = Color.WHITE; // Màu text
    public static final double TEMP_TEXT_STROKE_WIDTH = 0.9; // Độ dày viền
    public static final Paint TEMP_TEXT_STROKE_COLOR = Color.BLACK; // Màu viền
    public static final double TEMP_TEXT_FONT_SIZE = 18.0; // Kích thước font
    public static final double TEMP_TEXT_DISPLAY_DURATION = 1000.0; // Thời gian hiển thị (ms)
    public static final double TEMP_TEXT_FADE_DURATION = 200.0; // Thời gian mờ dần (ms)
    public static final double TEMP_TEXT_OFFSET_Y = 10.0; // Offset Y so với đầu player
    public static final Font TEMP_TEXT_FONT = Font.font("Arial", FontWeight.BOLD, TEMP_TEXT_FONT_SIZE); // Font chữ

    // --- Cấu hình HUD (Timer) ---
    public static final String TIMER_DEFAULT_TEXT = "Time: 06:00"; // [SỬA LẠI TỪ 00:00]
    public static final String TIMER_STYLE_CSS = "-fx-font-size: 18px; -fx-text-fill: white; -fx-background-color: rgba(0, 0, 0, 0.5); -fx-padding: 5px;";
    public static final double TIMER_X_POSITION = 10.0;
    public static final double TIMER_Y_POSITION = 10.0;

    /** Settings của icon thông báo*/
    // Nền mờ cho icon
    public static final double ICON_BG_SIZE = 64.0;

    // Icon bên trong
    public static final double ICON_SIZE = 24.0;

    // Vị trí toàn bộ cụm thông báo (bay cao hơn cây)
    public static final double ICON_Y_OFFSET = 32.0 + CropConfig.CROP_Y_OFFSET;

    // Căn chỉnh Icon nằm gọn trong phần "bong bóng" của Background
    // Vì Background 64x64 nhưng phần vẽ chỉ chiếm 2/3 trên, ta cần đẩy Icon lên một chút so với tâm BG
    public static final double ICON_PADDING_TOP = 12.0;

    // --- [MỚI] CẤU HÌNH ANIMATION THU HOẠCH ---
    public static final double HARVEST_ICON_SIZE = 32.0; // Kích thước icon bay
    public static final double HARVEST_FLY_DURATION_MS = 600; // Thời gian bay
    public static final double HARVEST_FADE_DURATION_MS = 200; // Thời gian mờ dần
    public static final double HARVEST_FADE_DELAY_MS = 400; // Delay trước khi mờ
    public static final double HARVEST_SCALE_FROM = 1.0;
    public static final double HARVEST_SCALE_TO = 0.5; // Thu nhỏ lại khi về túi

    private HudConfig() {
    }
}