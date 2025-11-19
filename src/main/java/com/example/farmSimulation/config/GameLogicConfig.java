package com.example.farmSimulation.config;

// Chứa các hằng số cấu hình logic game, thời gian và vật lý
public class GameLogicConfig {

    // --- Cấu hình Game Time & Day Cycle ---
    public static final double PLAYER_START_TIME_SECONDS = 720.0; // 12:00 PM (ví dụ)
    public static final double SECONDS_PER_FRAME = 1.0 / 60.0;
    public static final double DAY_CYCLE_DURATION_SECONDS = 1440.0; // 24 phút
    public static final double MIN_LIGHT_INTENSITY = 0.1; // Độ sáng tối thiểu (10%)
    public static final double MAX_DARKNESS_OPACITY = 0.8; // Độ tối tối đa (80%)

    // --- Cấu hình vật lý & Logic ---
    public static final double PLAYER_SPEED = 5.0; // Tốc độ di chuyển
    public static final double PLAYER_START_X = 0.0; // Tọa độ X spawn
    public static final double PLAYER_START_Y = 0.0; // Tọa độ Y spawn

    // --- CẤU HÌNH CHO TỪNG TOOL ---
    // (Bao gồm Tầm tương tác và Thời gian hành động)

    // HAND (Mặc định)
    public static final double HAND_INTERACTION_RANGE = WorldConfig.TILE_SIZE * 1.2; // Tầm hoạt động

    // --- THỜI GIAN & HÀNH ĐỘNG ---
    // Delay cho hành động không anim
    public static final long GENERIC_ACTION_DURATION_MS = 800;

    // HOE (Cuốc)
    public static final double HOE_INTERACTION_RANGE = WorldConfig.TILE_SIZE * 1.1; // Tầm hoạt động
    public static final int HOE_REPETITIONS = 4; // Số lần lặp
    public static final long HOE_DURATION_PER_REPETITION_MS = 500; // Thời gian (ms) mỗi lần
    public static final int MAX_DURABILITY_HOE = 20;

    // WATERING CAN (Bình tưới)
    public static final double WATERING_CAN_INTERACTION_RANGE = WorldConfig.TILE_SIZE * 1.1;
    public static final int WATERING_CAN_REPETITIONS = 1; // Số lần lặp
    public static final long WATERING_CAN_DURATION_PER_REPETITION_MS = 1000; // Giữ trong 1 giây
    public static final int MAX_WATER_CAPACITY = 10;

    // PICKAXE (Cúp)
    public static final double PICKAXE_INTERACTION_RANGE = WorldConfig.TILE_SIZE * 1.2;
    public static final int PICKAXE_REPETITIONS = 3;
    public static final long PICKAXE_DURATION_PER_REPETITION_MS = 300;
    public static final int MAX_DURABILITY_PICKAXE = 30;

    // SHOVEL (Xẻng)
    public static final double SHOVEL_INTERACTION_RANGE = WorldConfig.TILE_SIZE * 1.3;
    public static final int SHOVEL_REPETITIONS = 1;
    public static final long SHOVEL_DURATION_PER_REPETITION_MS = 1000;
    public static final int MAX_DURABILITY_SHOVEL = 25;

    // PLANT (Trồng cây)
    public static final double PLANT_INTERACTION_RANGE = WorldConfig.TILE_SIZE * 0.85;
    public static final long PLANT_DURATION_MS = 1200;

    // FERTILIZER (Bón phân)
    public static final double FERTILIZER_INTERACTION_RANGE = WorldConfig.TILE_SIZE * 1.0;
    public static final long FERTILIZER_DURATION_MS = 1400;

    private GameLogicConfig() {}
}