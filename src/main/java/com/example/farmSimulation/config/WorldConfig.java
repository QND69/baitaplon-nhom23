package com.example.farmSimulation.config;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

// Chứa các hằng số cấu hình liên quan đến thế giới game
public class WorldConfig {
    public static final double TILE_SIZE = 64; // Cạnh ô vuông TILE

    // --- Cấu hình Selector ---
    public static final Paint SELECTOR_COLOR = Color.BLACK;
    public static final double SELECTOR_STROKE_WIDTH = 1.0;
    
    // --- Cấu hình Ghost Placement ---
    /** Độ mờ (opacity) của ghost placement khi hiển thị bóng mờ (0.0 - 1.0) */
    public static final double GHOST_PLACEMENT_OPACITY = 0.5;

    // Tính toán số tile hiển thị trên màn hình
    // (Phụ thuộc vào WindowConfig và TILE_SIZE)
    public static final int NUM_COLS_ON_SCREEN = (int) (WindowConfig.SCREEN_WIDTH / TILE_SIZE) + 2;
    public static final int NUM_ROWS_ON_SCREEN = (int) (WindowConfig.SCREEN_HEIGHT / TILE_SIZE) + 2;

    private WorldConfig() {}
}