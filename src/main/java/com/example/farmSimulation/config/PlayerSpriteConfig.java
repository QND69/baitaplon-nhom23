package com.example.farmSimulation.config;

import javafx.scene.paint.Color;

// Chứa các hằng số cấu hình cho Sprite và Animation của Player
public class PlayerSpriteConfig {
    // --- Cấu hình MẶC ĐỊNH ---
    public static final double BASE_PLAYER_FRAME_WIDTH = 192;
    public static final double BASE_PLAYER_FRAME_HEIGHT = 192;
    public static final double BASE_PLAYER_FRAME_SCALE = 1.0;

    // --- Định nghĩa loại Animation ---
    public enum AnimationType {
        LOOP,                   // Lặp vô hạn (IDLE, WALK)
        ONE_SHOT,               // Chạy 1 lần rồi dừng ở frame 0 (ATTACK)
        ACTION_LOOP             // Lặp khi state active (HOE)
    }

    // --- Cấu hình Player Sprite (player_scaled_4x.png) ---
    public static final double PLAYER_FRAME_WIDTH = 128;
    public static final double PLAYER_FRAME_HEIGHT = 128;
    public static final long ANIMATION_SPEED = 120; // Tốc độ mỗi khung hình (ms/frame)

    // --- Cấu hình Player Action Sprite (player_action_scaled_4x.png) ---
    public static final double ACTION_FRAME_WIDTH = 192;
    public static final double ACTION_FRAME_HEIGHT = 192;

    // Offset để căn giữa nhân vật trong khung mặc định
    public static final double PLAYER_SPRITE_OFFSET_X = (BASE_PLAYER_FRAME_WIDTH - PLAYER_FRAME_WIDTH) / 2;
    public static final double PLAYER_SPRITE_OFFSET_Y = (BASE_PLAYER_FRAME_HEIGHT - PLAYER_FRAME_HEIGHT) / 2;

    // Kích thước Hitbox va chạm (nhỏ hơn người, nằm ở chân)
    public static final double COLLISION_BOX_WIDTH = 33.0;  // Hẹp hơn chiều rộng người
    public static final double COLLISION_BOX_HEIGHT = 12.0; // Thấp, chỉ lấy phần chân
    
    /** Padding đáy để căn chỉnh hitbox collision (tính từ đáy sprite lên trên) */
    public static final double COLLISION_BOX_BOTTOM_PADDING = 69.0;

    // --- Hằng số Debug ---
    /**
     * Bật/Tắt hiển thị khung viền (bounding box)
     * và tâm (center dot) của player.
     */
    public static final boolean DEBUG_PLAYER_BOUNDS = true;
    public static final Color DEBUG_BOUNDING_BOX_COLOR = Color.RED;
    public static final Color DEBUG_CENTER_DOT_COLOR = Color.CYAN;
    public static final Color DEBUG_RANGE_COLOR = Color.GREENYELLOW;
    public static final Color DEBUG_COLLISION_HITBOX_COLOR = Color.ORANGE;

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

    // HOE (Cuốc)
    public static final int HOE_FRAMES = 2; // Số frame của hành động
    public static final int HOE_DOWN_ROW = 7;
    public static final int HOE_UP_ROW = 8;
    public static final int HOE_RIGHT_ROW = 6;
    public static final int HOE_LEFT_ROW = 6; // Dùng chung ảnh với RIGHT

    // WATER (Tưới nước)
    public static final int WATER_FRAMES = 2; // Số frame của hành động
    public static final int WATER_DOWN_ROW = 9;
    public static final int WATER_UP_ROW = 10;
    public static final int WATER_RIGHT_ROW = 11;
    public static final int WATER_LEFT_ROW = 11;

    // PLANT (Trồng cây)
    public static final int PLANT_FRAMES = 2;
    public static final int PLANT_DOWN_ROW = 12;
    public static final int PLANT_UP_ROW = 14;
    public static final int PLANT_RIGHT_ROW = 13;
    public static final int PLANT_LEFT_ROW = 13;

    // DIG (Xúc đất)
    public static final int DIG_FRAMES = 2;
    public static final int DIG_DOWN_ROW = 16;
    public static final int DIG_UP_ROW = 15;
    public static final int DIG_RIGHT_ROW = 15;
    public static final int DIG_LEFT_ROW = 17;

    // FERTILZED (Bón phân)
    public static final int FERTILZED_FRAMES = 2;
    public static final int FERTILZED_DOWN_ROW = 18;
    public static final int FERTILZED_UP_ROW = 19;
    public static final int FERTILZED_RIGHT_ROW = 20;
    public static final int FERTILZED_LEFT_ROW = 20;

    // AXE (Chặt cây bằng rìu) - Dùng player_action_merged.png
    public static final int AXE_FRAMES = 2; // Số frame của hành động
    public static final int AXE_DOWN_ROW = 4; // Row 8 trong player_action_merged.png
    public static final int AXE_UP_ROW = 5; // Row 9 trong player_action_merged.png
    public static final int AXE_RIGHT_ROW = 3; // Row 10 trong player_action_merged.png
    public static final int AXE_LEFT_ROW = 3; // Row 11 trong player_action_merged.png
    public static final long AXE_SPEED = 100; // Tốc độ chặt cây (ms/frame)

    private PlayerSpriteConfig() {}
}