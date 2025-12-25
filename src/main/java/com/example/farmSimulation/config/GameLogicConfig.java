package com.example.farmSimulation.config;

// Chứa các hằng số cấu hình logic game, thời gian và vật lý
public class GameLogicConfig {

    // --- Cấu hình Game Time & Day Cycle ---
    // Thời gian bắt đầu game: 12:00 PM (Noon) - đỉnh điểm sáng nhất trong ngày
    // Công thức: DAY_CYCLE_DURATION_SECONDS / 2 để game bắt đầu ở giữa chu kỳ ngày (sáng nhất)
    public static final double DAY_CYCLE_DURATION_SECONDS = 1440.0; // 24 phút
    public static final double PLAYER_START_TIME_SECONDS = DAY_CYCLE_DURATION_SECONDS / 2; // 12:00 PM (Noon)
    public static final double SECONDS_PER_FRAME = 1.0 / 60.0;
    public static final double MIN_LIGHT_INTENSITY = 0.1; // Độ sáng tối thiểu (10%)
    public static final double MAX_DARKNESS_OPACITY = 0.8; // Độ tối tối đa (80%)

    // --- Cấu hình Brightness (Độ sáng) ---
    public static final double DEFAULT_BRIGHTNESS = 1.0; // 100% độ sáng mặc định
    public static final double MIN_BRIGHTNESS = 0.0; // Độ sáng tối thiểu (0%)
    public static final double MAX_BRIGHTNESS = 1.0; // Độ sáng tối đa (100%)

    // --- Cấu hình vật lý & Logic ---
    public static final double PLAYER_SPEED = 300.0; // Tốc độ di chuyển (pixel/giây) - đã chuyển từ pixel/frame sang pixel/giây
    public static final double PLAYER_START_X = 0.0; // Tọa độ X spawn
    public static final double PLAYER_START_Y = 0.0; // Tọa độ Y spawn
    public static final double PLAYER_START_MONEY = 500.0; // Số tiền ban đầu của người chơi

    // --- Cấu hình Stamina & XP ---
    public static final double PLAYER_MAX_STAMINA = 100.0; // Stamina tối đa
    public static final double PLAYER_START_STAMINA = 100.0; // Stamina ban đầu
    public static final double STAMINA_PENALTY_THRESHOLD = 15.0; // Ngưỡng stamina thấp để áp dụng penalty (15% của maxStamina = 100)
    public static final double STAMINA_SPEED_PENALTY_MULTIPLIER = 0.5; // Giảm tốc độ khi stamina thấp (50%)
    public static final double STAMINA_RECOVERY_RATE = 1.0; // Tốc độ hồi phục stamina mỗi giây (khi đứng yên IDLE) - slow recovery
    public static final double STAMINA_DRAIN_RUNNING = 2.0; // Stamina tiêu hao mỗi giây khi đang chạy (WALK state)

    // XP & Leveling
    public static final int PLAYER_START_LEVEL = 1; // Level ban đầu
    public static final double PLAYER_START_XP = 0.0; // XP ban đầu
    public static final double PLAYER_START_XP_TO_NEXT_LEVEL = 100.0; // XP cần để lên level tiếp theo
    public static final double XP_MULTIPLIER_PER_LEVEL = 1.2; // Nhân XP cần thiết mỗi level (1.2x)
    public static final double STAMINA_INCREASE_PER_LEVEL = 10.0; // Tăng stamina tối đa mỗi level

    // XP Gain from Actions
    public static final double XP_GAIN_HARVEST = 10.0; // XP nhận được khi thu hoạch thành công
    public static final double XP_GAIN_PLANT = 2.0; // XP nhận được khi trồng cây
    public static final double XP_GAIN_WATER = 1.0; // XP nhận được khi tưới nước
    public static final double XP_GAIN_HOE = 1.0; // XP nhận được khi cuốc đất
    public static final double XP_GAIN_AXE = 3.0; // XP nhận được khi dùng rìu (chặt cây/phá rào)
    public static final double XP_GAIN_SHOVEL = 2.0; // XP nhận được khi dùng xẻng (xúc đất/xóa cây)
    public static final double XP_GAIN_PICKAXE = 3.0; // XP nhận được khi dùng cuốc chim (đào đá)

    // --- Cấu hình Chi phí Stamina cho hành động ---
    public static final double STAMINA_COST_HOE = 2.0; // Chi phí stamina mỗi lần cuốc
    public static final double STAMINA_COST_WATERING_CAN = 2.0; // Chi phí stamina mỗi lần tưới
    public static final double STAMINA_COST_AXE = 3.0; // Chi phí stamina mỗi lần chặt
    public static final double STAMINA_COST_PICKAXE = 3.0; // Chi phí stamina mỗi lần đào
    public static final double STAMINA_COST_SHOVEL = 2.0; // Chi phí stamina mỗi lần xẻng
    public static final double STAMINA_COST_PLANT = 1.0; // Chi phí stamina mỗi lần trồng
    public static final double STAMINA_COST_FERTILIZER = 1.0; // Chi phí stamina mỗi lần bón phân

    // --- CẤU HÌNH CHO TỪNG ITEM ---
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

    // AXE (Rìu)
    public static final double AXE_INTERACTION_RANGE = WorldConfig.TILE_SIZE * 1.0;
    public static final int AXE_REPETITIONS = 3;
    public static final long AXE_DURATION_PER_REPETITION_MS = 600;
    public static final int MAX_DURABILITY_AXE = 30;

    // SWORD (Kiếm)
    public static final double SWORD_INTERACTION_RANGE = WorldConfig.TILE_SIZE * 1.5;
    public static final int SWORD_REPETITIONS = 3;
    public static final long SWORD_DURATION_PER_REPETITION_MS = 600;
    public static final int MAX_DURABILITY_SWORD = 30;

    // PLANT (Trồng cây)
    public static final double PLANT_INTERACTION_RANGE = WorldConfig.TILE_SIZE * 0.85;
    public static final long PLANT_DURATION_MS = 1200;

    // FERTILIZER (Bón phân)
    public static final double FERTILIZER_INTERACTION_RANGE = WorldConfig.TILE_SIZE * 1.0;
    public static final long FERTILIZER_DURATION_MS = 1400;

    // --- Cấu hình Performance Optimization ---
    // Tần suất update cây trồng (ms). Thay vì update mỗi frame (16ms), update mỗi 100ms để tiết kiệm CPU
    public static final long CROP_UPDATE_INTERVAL_MS = 100;

    // Tần suất update map render (ms). Chỉ update map khi cần thiết
    public static final long MAP_UPDATE_INTERVAL_MS = 50;

    // Số lượng tiles tối đa được update mỗi frame trong CropManager (để tránh lag spike)
    public static final int MAX_CROPS_UPDATE_PER_FRAME = 100;

    // [MỚI] Bán kính tìm kiếm ô trống để rơi item (1 = tìm trong 3x3 xung quanh)
    public static final int ITEM_DROP_SEARCH_RADIUS = 1;

    // [MỚI] Độ phân tán ngẫu nhiên khi rơi item (pixel) - để item không bị dính chặt vào giữa ô
    public static final double ITEM_DROP_SCATTER_RANGE = 24.0;

    // Game Over
    public static final double GAME_OVER_DELAY_SECONDS = 3.0; // Delay before showing Game Over UI after death

    // [MỚI] Cấu hình Cheat code
    public static final double CHEAT_MONEY_AMOUNT = 10000.0;

    private GameLogicConfig() {}
}