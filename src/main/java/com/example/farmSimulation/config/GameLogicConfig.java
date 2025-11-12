package com.example.farmSimulation.config;

// Chứa các hằng số cấu hình logic game, thời gian và vật lý
public class GameLogicConfig {

    // --- Cấu hình Game Time & Day Cycle ---
    public static final double PLAYER_START_TIME_SECONDS = 360.0; // 6:00 AM (ví dụ)
    public static final double SECONDS_PER_FRAME = 1.0 / 60.0;
    public static final double DAY_CYCLE_DURATION_SECONDS = 1440.0; // 24 phút
    public static final double MIN_LIGHT_INTENSITY = 0.1; // Độ sáng tối thiểu (10%)
    public static final double MAX_DARKNESS_OPACITY = 0.8; // Độ tối tối đa (80%)

    // --- Cấu hình vật lý & Logic ---
    public static final double PLAYER_SPEED = 5.0; // Tốc độ di chuyển
    public static final double PLAYER_START_X = 0.0; // Tọa độ X spawn
    public static final double PLAYER_START_Y = 0.0; // Tọa độ Y spawn
    // (Phụ thuộc vào WorldConfig)
    public static final double PLAYER_INTERACTION_RANGE_PIXELS = WorldConfig.TILE_SIZE * 1.2; // Tầm tương tác
    public static final int ACTION_DELAY_FRAMES_HOE = 1; // Thời gian cuốc đất (1 frame = 16.6 ms)

    private GameLogicConfig() {}
}