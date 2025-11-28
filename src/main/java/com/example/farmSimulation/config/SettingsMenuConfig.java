package com.example.farmSimulation.config;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

// Chứa các hằng số cấu hình cho Menu Cài đặt
public class SettingsMenuConfig {
    // --- Cấu hình Settings Menu ---
    public static final double SETTINGS_MENU_SPACING = 15.0;
    public static final String SETTINGS_MENU_STYLE_CSS = "-fx-background-color: rgba(0,0,0,0.6); -fx-padding: 30; -fx-border-radius: 10; -fx-background-radius: 10;";
    public static final double SETTINGS_MENU_WIDTH = 400.0;
    public static final double SETTINGS_MENU_HEIGHT = 500.0;
    public static final String SETTINGS_MENU_TITLE = "⚙ Game Menu";
    public static final Paint SETTINGS_MENU_FONT_COLOR = Color.WHITESMOKE;
    public static final String SETTINGS_MENU_FONT_FAMILY = "Arial";
    public static final double SETTINGS_MENU_TITLE_FONT_SIZE = 28.0;
    public static final double SETTINGS_PLAYER_INFO_SPACING = 5.0;
    public static final double SETTINGS_MENU_BODY_FONT_SIZE = 18.0;
    public static final double SETTINGS_MENU_BUTTON_WIDTH = 200.0;
    public static final String SETTINGS_RESUME_BUTTON_TEXT = "Resume";
    public static final String SETTINGS_SAVE_BUTTON_TEXT = "Save Game";
    public static final String SETTINGS_EXIT_BUTTON_TEXT = "Exit Game";
    public static final String SETTINGS_TEXT_ON = "ON";
    public static final String SETTINGS_TEXT_OFF = "OFF";
    public static final String SETTINGS_MUTE_BUTTON_TEXT_PREFIX = "Mute: ";
    public static final String SETTINGS_MASTER_VOLUME_LABEL = "Master Volume:";
    public static final double SLIDER_MIN_VALUE = 0.0;
    public static final double SLIDER_MAX_VALUE = 1.0;
    // Audio Settings (Đơn giản hóa: chỉ Master Volume + Mute)
    public static final boolean DEFAULT_MUTE = false; // Mặc định không tắt tiếng
    public static final double DEFAULT_MASTER_VOLUME = 0.5; // Âm lượng tổng mặc định (50%)
    
    // --- Cấu hình Brightness ---
    public static final double DEFAULT_BRIGHTNESS = GameLogicConfig.DEFAULT_BRIGHTNESS; // 100%
    public static final double BRIGHTNESS_MIN = GameLogicConfig.MIN_BRIGHTNESS; // 0%
    public static final double BRIGHTNESS_MAX = GameLogicConfig.MAX_BRIGHTNESS; // 100%
    
    // --- Cấu hình Layout ---
    public static final double SETTINGS_MENU_WIDTH_NEW = 500.0; // Tăng width cho GridPane
    public static final double SETTINGS_MENU_HEIGHT_NEW = 600.0; // Tăng height cho GridPane

    private SettingsMenuConfig() {}
}