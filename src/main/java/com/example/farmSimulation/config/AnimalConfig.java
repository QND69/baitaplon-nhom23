package com.example.farmSimulation.config;

/**
 * Chứa các hằng số cấu hình logic liên quan đến Động vật (Animals).
 */
public class AnimalConfig {
    
    // --- Cấu hình Tốc độ & Di chuyển ---
    
    /** Tốc độ di chuyển cơ bản của động vật (pixel/giây) - đã chuyển từ pixel/frame sang pixel/giây */
    public static final double BASE_MOVEMENT_SPEED = 60.0;
    
    /** Khoảng cách tối đa động vật có thể di chuyển từ vị trí spawn (tiles) */
    public static final double MAX_WANDER_DISTANCE = 10.0;
    
    /** * Xác suất động vật di chuyển mỗi frame (0.0 - 1.0) 
     * [SỬA] Tăng lên 0.6 (60%) để động vật di chuyển rất thường xuyên.
     */
    public static final double MOVEMENT_CHANCE = 0.6;
    
    /** * Thời gian giữa các lần đổi hướng (ms)
     * [SỬA] Giảm xuống 1000ms để động vật đổi hướng nhanh hơn nếu bị kẹt.
     */
    public static final long DIRECTION_CHANGE_INTERVAL_MS = 1000;
    
    // --- Cấu hình Đói & Sinh tồn ---
    
    /** Tốc độ giảm đói mỗi giây (điểm đói/giây) */
    public static final double HUNGER_DECREASE_RATE = 0.1;
    
    /** Mức đói tối đa (100 = no đói, 0 = chết đói) */
    public static final double MAX_HUNGER = 100.0;
    
    /** Mức đói tối thiểu để sống (dưới mức này sẽ chết) */
    public static final double MIN_HUNGER_TO_SURVIVE = 0.0;
    
    /** Thời gian chết đói (ms) - thời gian đói liên tục trước khi chết */
    public static final long STARVATION_TIME_MS = 30000; // 30 giây
    
    /** Mức đói để hiển thị icon đói (< mức này) */
    public static final double HUNGER_WARNING_THRESHOLD = 30.0;
    
    // --- Cấu hình Sản phẩm ---
    
    /** Thời gian cơ bản để tạo sản phẩm (ms) */
    public static final long BASE_PRODUCTION_TIME_MS = 60000; // 60 giây
    
    /** Thời gian tối thiểu để tạo sản phẩm khi đói (ms) - chậm hơn khi đói */
    public static final long HUNGRY_PRODUCTION_TIME_MS = 120000; // 120 giây
    
    // --- Cấu hình Thịt (Meat) ---
    
    /** Số lượng thịt tối đa rơi ra khi giết động vật */
    public static final int MAX_MEAT_DROP = 5;
    
    /** Tỷ lệ tính thịt: age * MEAT_RATE = số lượng thịt */
    public static final double MEAT_RATE = 0.1;
    
    /** Tuổi tối thiểu để có thịt */
    public static final int MIN_AGE_FOR_MEAT = 1;
    
    // --- Cấu hình Đặt vật nuôi ---
    
    /** Tầm tương tác khi đặt vật nuôi (tiles) */
    public static final double PLACEMENT_RANGE = 1.5;
    
    // --- Cấu hình Sprite ---
    
    /** Kích thước sprite động vật nhỏ (Chicken) - từ file PNG 32x32 */
    public static final double SMALL_ANIMAL_SIZE = 32.0;
    
    /** Kích thước sprite động vật lớn (Cow, Pig, Sheep) - từ file PNG 64x64 */
    public static final double LARGE_ANIMAL_SIZE = 64.0;
    
    /** Kích thước sprite gà con và trứng - từ file PNG 16x16 */
    public static final double BABY_CHICKEN_SIZE = 16.0;
    
    /** Offset Y để vẽ động vật (để căn chỉnh với tile) */
    public static final double ANIMAL_Y_OFFSET = 0.0;
    
    // --- Cấu hình Hitbox ---
    
    /** Chiều rộng hitbox động vật nhỏ (Chicken) */
    public static final double SMALL_ANIMAL_HITBOX_WIDTH = 24.0;
    
    /** Chiều cao hitbox động vật nhỏ (Chicken) */
    public static final double SMALL_ANIMAL_HITBOX_HEIGHT = 20.0;
    
    /** Chiều rộng hitbox động vật lớn (Cow, Pig, Sheep) */
    public static final double LARGE_ANIMAL_HITBOX_WIDTH = 48.0;
    
    /** Chiều cao hitbox động vật lớn (Cow, Pig, Sheep) */
    public static final double LARGE_ANIMAL_HITBOX_HEIGHT = 40.0;
    
    /** Chiều rộng hitbox gà con và trứng (16x16) */
    public static final double BABY_CHICKEN_HITBOX_WIDTH = 12.0;
    
    /** Chiều cao hitbox gà con và trứng (16x16) */
    public static final double BABY_CHICKEN_HITBOX_HEIGHT = 10.0;
    
    // --- Cấu hình Sinh trưởng ---
    
    /** Thời gian trứng nở thành gà con (ms) */
    public static final long EGG_HATCH_TIME_MS = 60000; // 60 giây
    
    /** Thời gian gà con trưởng thành (ms) */
    public static final long BABY_CHICKEN_GROWTH_TIME_MS = 120000; // 120 giây
    
    /** Thời gian các con non khác (bò, lợn, cừu) trưởng thành (ms) */
    public static final long BABY_ANIMAL_GROWTH_TIME_MS = 180000; // 180 giây
    
    // --- Cấu hình Update Interval ---
    
    /** Tần suất update động vật (ms) - để tối ưu performance */
    public static final long ANIMAL_UPDATE_INTERVAL_MS = 100;
    
    // --- Cấu hình Sprite Sheet Layout ---
    
    /** Số frames mỗi hàng cho CHICKEN IDLE (hàng 0 và 1) */
    public static final int CHICKEN_IDLE_FRAMES = 8;
    
    /** Số frames mỗi hàng cho CHICKEN WALK (hàng 2 và 3) */
    public static final int CHICKEN_WALK_FRAMES = 4;
    
    /** Số frames mỗi hàng cho Standard animals WALK (hàng 0-3) */
    public static final int STANDARD_WALK_FRAMES = 6;
    
    /** Số frames mỗi hàng cho Standard animals IDLE (hàng 4-7) */
    public static final int STANDARD_IDLE_FRAMES = 4;
    
    /** Hàng IDLE Down trong Standard layout (dùng cho EGG_ENTITY) */
    public static final int STANDARD_IDLE_DOWN_ROW = 4;
    
    /** Số frame trứng trong hàng IDLE Down (2 frame cuối) */
    public static final int EGG_FRAME_COUNT = 2;
    
    /** Cột bắt đầu của trứng (Cột 4 và 5) */
    public static final int EGG_FRAME_START_INDEX = 4;
    
    private AnimalConfig() {}
}