package com.example.farmSimulation.config;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

// Chứa các hằng số cấu hình game
public class GameConfig {
    // --- Cấu hình màn hình & UI ---
    public static final double SCREEN_WIDTH = 1280;
    public static final double SCREEN_HEIGHT = 720;
    public static final double TILE_SIZE = 60; // Cạnh ô vuông TILE
    public static final String GAME_TITLE = "Farm Simulation";
    public static final Paint BACKGROUND_COLOR = Color.GREENYELLOW; // Màu nền

    // --- Cấu hình Selector ---
    public static final Paint SELECTOR_COLOR = Color.BLACK;
    public static final double SELECTOR_STROKE_WIDTH = 1.0;

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

    // --- Cấu hình vật lý & Logic ---
    public static final double PLAYER_SPEED = 5.0; // Tốc độ di chuyển
    public static final double PLAYER_START_X = 0.0; // Tọa độ X spawn
    public static final double PLAYER_START_Y = 0.0; // Tọa độ Y spawn
    public static final double PLAYER_INTERACTION_RANGE_PIXELS = GameConfig.TILE_SIZE * 1.2; // Tầm tương tác
    public static final int ACTION_DELAY_FRAMES_HOE = 1; // Thời gian cuốc đất (1 frame = 16.6 ms)

    // Tính toán số tile hiển thị trên màn hình
    public static final int NUM_COLS_ON_SCREEN = (int) (SCREEN_WIDTH / TILE_SIZE) + 2;
    public static final int NUM_ROWS_ON_SCREEN = (int) (SCREEN_HEIGHT / TILE_SIZE) + 2;

    // --- Cấu hình Player Sprite (player_scaled_4x.png) ---
    public static final double PLAYER_FRAME_WIDTH = 128;
    public static final double PLAYER_FRAME_HEIGHT = 128;
    public static final double PLAYER_SPRITE_SCALE = 1.0;
    public static final long ANIMATION_SPEED = 120; // Tốc độ mỗi khung hình (ms/frame)

    // --- DỮ LIỆU ANIMATION ---

    // IDLE (player_scaled_4x.png)
    public static final int IDLE_DOWN_ROW = 0;
    public static final int IDLE_RIGHT_ROW = 1;
    public static final int IDLE_LEFT_ROW = 1; // Dùng chung ảnh với RIGHT
    public static final int IDLE_UP_ROW = 2;
    public static final int IDLE_FRAMES = 6; // Số frame của hành động

    // WALK (player_scaled_4x.png)
    public static final int WALK_DOWN_ROW = 3;
    public static final int WALK_RIGHT_ROW = 4;
    public static final int WALK_LEFT_ROW = 4; // Dùng chung ảnh với RIGHT
    public static final int WALK_UP_ROW = 5;
    public static final int WALK_FRAMES = 6; // Số frame của hành động

    // ATTACK (player_scaled_4x.png)
    public static final int ATTACK_DOWN_ROW = 6;
    public static final int ATTACK_RIGHT_ROW = 7;
    public static final int ATTACK_LEFT_ROW = 7; // Dùng chung ảnh với RIGHT
    public static final int ATTACK_UP_ROW = 8;
    public static final int ATTACK_FRAMES = 4; // Số frame của hành động
    public static final long ATTACK_SPEED = 100; // Tốc độ tấn công

    // TODO: Thêm các hằng số cho HOE, DEAD

    private GameConfig() {
    }
}
