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
    public static final String CANT_PLACE_TEXT = "Can't place this here";
    public static final String TEXT_INVENTORY_FULL = "Inventory Full";
    public static final String TEXT_PLANT_CAN_NOT_BE_FERTILIZED = "This plant cannot be fertilized";
    public static final String TEXT_WATER_EMPTY = "Watering Can is empty";
    public static final String TEXT_PLAYER_BLOCKING = "You are blocking the fence";

    public static final Paint TEMP_TEXT_COLOR = Color.WHITE; // Màu text
    public static final double TEMP_TEXT_STROKE_WIDTH = 0.9; // Độ dày viền
    public static final Paint TEMP_TEXT_STROKE_COLOR = Color.BLACK; // Màu viền
    public static final double TEMP_TEXT_FONT_SIZE = 18.0; // Kích thước font
    public static final double TEMP_TEXT_DISPLAY_DURATION = 1000.0; // Thời gian hiển thị (ms)
    public static final double TEMP_TEXT_FADE_DURATION = 200.0; // Thời gian mờ dần (ms)
    public static final double TEMP_TEXT_OFFSET_Y = 10.0; // Offset Y so với đầu player
    public static final Font TEMP_TEXT_FONT = Font.font("Arial", FontWeight.BOLD, TEMP_TEXT_FONT_SIZE); // Font chữ

    // --- Cấu hình HUD Layout ---
    // Top-Left Corner (Player Stats)
    public static final double HUD_TOP_LEFT_X = 10.0;
    public static final double HUD_TOP_LEFT_Y = 10.0;
    public static final double HUD_ELEMENT_SPACING = 5.0;
    
    // Top-Right Corner (Settings, Timer, Weather - từ trên xuống)
    // Vị trí Settings Icon: Top-Right (SCREEN_WIDTH - Margin)
    public static final double HUD_TOP_RIGHT_MARGIN = 15.0; // Khoảng cách từ cạnh phải (giảm từ 60 để dính sát cạnh)
    public static final double HUD_TOP_RIGHT_Y = 10.0; // Vị trí Y bắt đầu
    public static final double HUD_TOP_RIGHT_ELEMENT_SPACING = 10.0; // Khoảng cách giữa các elements ở top-right
    
    // Bottom-Right Corner (Shop Icon)
    // Vị trí Shop Icon: Bottom-Right (SCREEN_WIDTH - Margin, SCREEN_HEIGHT - Margin)
    public static final double HUD_BOTTOM_RIGHT_MARGIN = 15.0; // Khoảng cách từ cạnh phải và dưới (giảm từ 60 để dính sát cạnh)
    
    // --- Cấu hình Player Level (Top-Left) ---
    public static final double LEVEL_RECTANGLE_WIDTH = 120.0; // Width của Rounded Rectangle
    public static final double LEVEL_RECTANGLE_HEIGHT = 30.0; // Height của Rounded Rectangle
    public static final double LEVEL_RECTANGLE_CORNER_RADIUS = 8.0; // Corner radius cho rounded rectangle
    public static final double LEVEL_FONT_SIZE = 16.0;
    public static final Paint LEVEL_TEXT_COLOR = Color.WHITE;
    public static final Paint LEVEL_BG_COLOR = Color.rgb(100, 150, 200);
    
    // --- Cấu hình Bar Labels (Stamina, Exp) ---
    public static final double BAR_LABEL_FONT_SIZE = 12.0;
    public static final Paint BAR_LABEL_COLOR = Color.WHITE;
    
    // --- Cấu hình XP Bar (Top-Left, dưới Level) ---
    public static final double XP_BAR_WIDTH = 150.0;
    public static final double XP_BAR_HEIGHT = 8.0;
    public static final Paint XP_BAR_BG_COLOR = Color.rgb(50, 50, 50);
    public static final Paint XP_BAR_FILL_COLOR = Color.rgb(100, 200, 255); // Blue
    
    // --- Cấu hình Stamina Bar (Top-Left, dưới XP Bar) ---
    public static final double STAMINA_BAR_WIDTH = 150.0;
    public static final double STAMINA_BAR_HEIGHT = 12.0;
    public static final Paint STAMINA_BAR_BG_COLOR = Color.rgb(50, 50, 50);
    public static final Paint STAMINA_BAR_FULL_COLOR = Color.rgb(50, 200, 50); // Green
    public static final Paint STAMINA_BAR_LOW_COLOR = Color.rgb(200, 50, 50); // Red
    
    // --- Cấu hình Timer (Top-Right) ---
    public static final String DAY_DEFAULT_TEXT = "Day 1";
    public static final String TIME_DEFAULT_TEXT = "12:00";
    public static final String DAY_STYLE_CSS = "-fx-font-size: 16px; -fx-text-fill: white;"; // Không có background
    public static final String TIME_STYLE_CSS = "-fx-font-size: 16px; -fx-text-fill: white;"; // Không có background
    public static final double TIMER_LABEL_WIDTH = 200.0; // Fixed width cho timer label để căn phải
    
    // --- Cấu hình Weather Icon (Top-Right) ---
    public static final double WEATHER_ICON_SIZE = 32.0;
    public static final double ICON_BUTTON_SIZE = 40.0; // Shop và Settings icons
    
    // --- Cấu hình Shop/Settings Icons (Top-Right) ---
    public static final Paint ICON_BUTTON_BG_COLOR = Color.rgb(60, 60, 60, 0.7);
    public static final Paint ICON_BUTTON_HOVER_COLOR = Color.rgb(80, 80, 80, 0.9);
    
    // --- Cấu hình hiển thị Tiền (Money) ---
    public static final double MONEY_X_POSITION = 10.0; // Vị trí X của label tiền
    public static final double MONEY_Y_POSITION = 50.0; // Vị trí Y của label tiền (dưới Timer)
    public static final double MONEY_ICON_SIZE = 24.0; // Kích thước icon coin
    public static final Paint MONEY_ICON_COLOR = Color.GOLD; // Màu icon coin (vàng)
    public static final Paint MONEY_TEXT_COLOR = Color.WHITE; // Màu chữ số tiền
    public static final double MONEY_FONT_SIZE = 18.0; // Kích thước font số tiền
    public static final double MONEY_ICON_SPACING = 5.0; // Khoảng cách giữa icon và text
    public static final String MONEY_STYLE_CSS = "-fx-font-size: " + MONEY_FONT_SIZE + "px; -fx-text-fill: white; -fx-background-color: rgba(0, 0, 0, 0.5); -fx-padding: 5px;";
    
    // --- Cấu hình GUI Icons (from GUI_icon_32x32.png) ---
    public static final double GUI_ICON_SIZE = 32.0; // Kích thước mỗi icon trong sheet
    public static final int GUI_ICON_SETTINGS_COL = 0; // Settings (Gear) icon column
    public static final int GUI_ICON_SHOP_COL = 1; // Shop icon column
    public static final int GUI_ICON_MONEY_COL = 2; // Money ($) icon column
    public static final int GUI_ICON_SUNNY_COL = 3; // Sunny weather icon column
    public static final int GUI_ICON_RAIN_COL = 4; // Rain weather icon column
    public static final int GUI_ICON_ENERGY_EMPTY_COL = 5; // Energy Bar Empty (Lightning) column
    public static final int GUI_ICON_ENERGY_FULL_COL = 6; // Energy Bar Full (Lightning) column
    public static final int GUI_ICON_TRASH_COL = 8; // Trash Can icon column

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