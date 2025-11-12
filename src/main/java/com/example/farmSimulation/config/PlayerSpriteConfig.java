package com.example.farmSimulation.config;

// Chứa các hằng số cấu hình cho Sprite và Animation của Player
public class PlayerSpriteConfig {
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

    private PlayerSpriteConfig() {}
}