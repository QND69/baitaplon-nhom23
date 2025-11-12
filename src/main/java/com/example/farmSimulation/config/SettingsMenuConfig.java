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
    public static final String SETTINGS_MUSIC_BUTTON_TEXT_PREFIX = "Music: ";
    public static final String SETTINGS_SOUND_BUTTON_TEXT_PREFIX = "Sound: ";
    public static final String SETTINGS_TEXT_ON = "ON";
    public static final String SETTINGS_TEXT_OFF = "OFF";
    public static final double SLIDER_MIN_VALUE = 0.0;
    public static final double SLIDER_MAX_VALUE = 1.0;
    public static final boolean DEFAULT_MUSIC_ON = true;
    public static final boolean DEFAULT_SOUND_ON = true;
    public static final double DEFAULT_MUSIC_VOLUME = 0.5;
    public static final double DEFAULT_SOUND_VOLUME = 0.5;

    private SettingsMenuConfig() {}
}