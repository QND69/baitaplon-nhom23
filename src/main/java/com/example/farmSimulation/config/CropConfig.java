package com.example.farmSimulation.config;

/**
 * Chứa các hằng số cấu hình logic liên quan đến Cây trồng (Crops).
 */
public class CropConfig {

    // --- Cấu hình Logic ---

    /** Thời gian (ms) cho mỗi giai đoạn phát triển (khi được tưới nước) */
    public static final long TIME_PER_GROWTH_STAGE_MS = 20_000; // 60 giây / giai đoạn
    
    // --- Cấu hình Tốc độ phát triển theo thời tiết/thời gian ---
    /** Tốc độ phát triển cơ bản */
    public static final double BASE_GROWTH_SPEED = 1.0;
    /** Tốc độ phát triển vào ban đêm (nhân với BASE_GROWTH_SPEED) */
    public static final double NIGHT_GROWTH_SPEED_MULTIPLIER = 0.8;
    /** Tốc độ phát triển khi mưa (nhân với BASE_GROWTH_SPEED) */
    public static final double RAIN_GROWTH_SPEED_MULTIPLIER = 1.2;
    /** Ngưỡng ánh sáng để coi là ban đêm (dưới ngưỡng này = đêm) */
    public static final double NIGHT_LIGHT_THRESHOLD = 0.4;

    /** Thời gian (ms) để đất ướt (SOIL_WET) trở lại thành đất khô (SOIL) */
    public static final long SOIL_DRY_TIME_MS = 30_000; // 40s

    // Thời gian từ lúc đất khô -> Hiện icon Warning
    public static final long WATER_WARNING_DELAY_MS = 10_000; // 30_000

    // FERTILIZER LOGIC
    public static final long FERTILIZER_EFFECT_DURATION_MS = 20_000; // Thời gian phân bón tồn tại trên đất
    public static final long FERTILIZER_WARNING_DELAY_MS = 20_000; // Thời gian "Buff ẩn" sau khi phân hết
    public static final double FERTILIZER_BUFF = 2; // Thời gian nhanh hơn khi dùng phân bón

    // Giai đoạn tối thiểu để được bón phân (Stage 0: Hạt, Stage 1: Mầm, Stage 2: Cây nhỏ...)
    public static final int MIN_GROWTH_STAGE_FOR_FERTILIZER = 2;

    /**
     * Thời gian (ms) cây có thể sống sót trên đất khô.
     * Nếu đất khô quá thời gian này, cây sẽ chết (biến mất).
     */
    public static final long CROP_DEATH_TIME_MS = 360_000;

    // Thời gian đất trống tự biến về cỏ
    public static final long SOIL_REVERT_TIME_MS = 40_000;

    // --- Cấu hình Sprite seed ---

    /** Kích thước (pixel) của MỘT frame cây trong spritesheet gốc*/
    public static final double CROP_SPRITE_WIDTH = 64.0;
    public static final double CROP_SPRITE_HEIGHT = 64.0;

    // Độ dịch chuyển Y của cây
    public static final double CROP_Y_OFFSET = 16.0;

    private CropConfig() {}
}