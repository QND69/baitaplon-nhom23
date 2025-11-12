package com.example.farmSimulation.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

// Chứa các hằng số cấu hình game
public class GameConfig {
    // Cấu hình màn hình
    public static final double SCREEN_WIDTH = 1280;
    public static final double SCREEN_HEIGHT = 720;
    public static final double TILE_SIZE = 60;

    // Cấu hình vật lý
    public static final double PLAYER_SPEED = 5.0; // Tốc độ di chuyển

    // Tính toán số tile hiển thị trên màn hình
    public static final int NUM_COLS_ON_SCREEN = (int) (SCREEN_WIDTH / TILE_SIZE) + 2;
    public static final int NUM_ROWS_ON_SCREEN = (int) (SCREEN_HEIGHT / TILE_SIZE) + 2;

    public static final double PLAYER_FRAME_WIDTH = 128;
    public static final double PLAYER_FRAME_HEIGHT = 128;
    public static final double PLAYER_SPRITE_SCALE = 1.0;

    public static final long ANIMATION_SPEED = 120; // Tốc độ mỗi khung hình (ms/frame)
}
