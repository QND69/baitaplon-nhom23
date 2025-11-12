package com.example.farmSimulation.config;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

// Chứa các hằng số cấu hình cho Giao diện Người dùng (HUD)
public class HudConfig {

    // --- Cấu hình Text tạm thời ---
    public static final String TOO_FAR_TEXT = "It's too far!"; // Nội dung text
    public static final Paint TEMP_TEXT_COLOR = Color.WHITE; // Màu text
    public static final double TEMP_TEXT_STROKE_WIDTH = 0.9; // Độ dày viền
    public static final Paint TEMP_TEXT_STROKE_COLOR = Color.BLACK; // Màu viền
    public static final double TEMP_TEXT_FONT_SIZE = 18.0; // Kích thước font
    public static final double TEMP_TEXT_DISPLAY_DURATION = 1000.0; // Thời gian hiển thị (ms)
    public static final double TEMP_TEXT_FADE_DURATION = 200.0; // Thời gian mờ dần (ms)
    public static final double TEMP_TEXT_OFFSET_Y = 10.0; // Offset Y so với đầu player
    public static final Font TEMP_TEXT_FONT = Font.font("Arial", TEMP_TEXT_FONT_SIZE); // Font chữ

    // --- Cấu hình HUD (Timer) ---
    public static final String TIMER_DEFAULT_TEXT = "Time: 06:00"; // [SỬA LẠI TỪ 00:00]
    public static final String TIMER_STYLE_CSS = "-fx-font-size: 18px; -fx-text-fill: white; -fx-background-color: rgba(0, 0, 0, 0.5); -fx-padding: 5px;";
    public static final double TIMER_X_POSITION = 10.0;
    public static final double TIMER_Y_POSITION = 10.0;

    private HudConfig() {}
}