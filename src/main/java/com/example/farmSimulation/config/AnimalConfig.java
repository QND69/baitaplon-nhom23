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

    // --- [SỬA] Cấu hình State Timer (Phân tách Gà và Thú thường) ---

    // 1. Cấu hình cho GÀ (Chicken) - Giữ nguyên như cũ vì bạn thấy hợp lý
    /** Thời gian tối thiểu gà đứng yên (ms) */
    public static final int CHICKEN_MIN_IDLE_TIME_MS = 2000;

    /** Thời gian tối đa gà đứng yên (ms) */
    public static final int CHICKEN_MAX_IDLE_TIME_MS = 5000;

    /** Thời gian tối thiểu gà đi bộ (ms) */
    public static final int CHICKEN_MIN_WALK_TIME_MS = 1000;

    /** Thời gian tối đa gà đi bộ (ms) */
    public static final int CHICKEN_MAX_WALK_TIME_MS = 3000;

    /** Tỷ lệ gà chọn hành động đi bộ (0.3 = 30% đi, 70% đứng) */
    public static final double CHICKEN_WALK_CHANCE = 0.3;

    // 2. Cấu hình cho THÚ THƯỜNG (Cow, Pig, Sheep) - Tăng hoạt động lên
    /** Thời gian tối thiểu thú thường đứng yên (ms) - Giảm xuống để ít đứng hơn */
    public static final int STANDARD_MIN_IDLE_TIME_MS = 1000;

    /** Thời gian tối đa thú thường đứng yên (ms) */
    public static final int STANDARD_MAX_IDLE_TIME_MS = 3000;

    /** Thời gian tối thiểu thú thường đi bộ (ms) - Đi lâu hơn chút */
    public static final int STANDARD_MIN_WALK_TIME_MS = 1500;

    /** Thời gian tối đa thú thường đi bộ (ms) */
    public static final int STANDARD_MAX_WALK_TIME_MS = 4000;

    /** Tỷ lệ thú thường chọn hành động đi bộ (0.6 = 60% đi, 40% đứng) - Tăng gấp đôi so với gà */
    public static final double STANDARD_WALK_CHANCE = 0.6;

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

    // --- Cấu hình Kích thước Sprite (Theo đúng tên file ảnh) ---
    // Đây là kích thước để cắt frame từ sheet, không phải kích thước hiển thị

    /** Kích thước sprite Gà (Chicken) - từ file PNG 48x48 */
    public static final double SPRITE_SIZE_CHICKEN = 48.0;

    /** [MỚI] Kích thước sprite Gà con và Trứng - từ file PNG 32x32 */
    public static final double SPRITE_SIZE_BABY_CHICKEN_EGG = 32.0;

    /** Kích thước sprite Bò (Cow) - từ file PNG 96x96 */
    public static final double SPRITE_SIZE_COW = 96.0;

    /** Kích thước sprite Lợn (Pig) - từ file PNG 96x96 */
    public static final double SPRITE_SIZE_PIG = 96.0;

    /** Kích thước sprite Cừu (Sheep) - từ file PNG 64x64 */
    public static final double SPRITE_SIZE_SHEEP = 64.0;

    /** Kích thước sprite Trứng (Egg) - từ file PNG 32x32 */
    public static final double SPRITE_SIZE_EGG = 32.0;

    /** Offset Y để vẽ động vật (để căn chỉnh với tile) */
    public static final double ANIMAL_Y_OFFSET = 0.0;

    // --- Cấu hình Scale (Để hiển thị) ---

    // [MỚI] Scale riêng cho từng con trưởng thành để tùy chỉnh kích thước hiển thị
    public static final double SCALE_CHICKEN = 1.0;
    public static final double SCALE_COW = 1.0;
    public static final double SCALE_PIG = 1.0;
    public static final double SCALE_SHEEP = 1.0;
    public static final double SCALE_EGG = 1.0; // Scale cho trứng

    // Scale cho con non (khi dùng chung sheet với con trưởng thành)
    public static final double SCALE_BABY_COW = 0.5; // 96 -> 48
    public static final double SCALE_BABY_PIG = 0.5; // 96 -> 48
    public static final double SCALE_BABY_SHEEP = 0.5; // 64 -> 32

    // [SỬA] Gà con dùng sheet 32x32, scale 1.0 sẽ ra 32px (nhỏ hơn gà lớn 48px)
    public static final double SCALE_BABY_CHICKEN = 1.0;

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

    // 1. Cấu hình CHICKEN (Layout đặc biệt)
    // Hàng 0-1: IDLE (Left, Right) - 8 frames
    // Hàng 2-3: WALK (Left, Right) - 4 frames
    // Logic render: UP dùng frame RIGHT, DOWN dùng frame LEFT.

    /** Số frames mỗi hàng cho CHICKEN IDLE (hàng 0 và 1) */
    public static final int CHICKEN_IDLE_FRAMES = 8;

    /** Số frames mỗi hàng cho CHICKEN WALK (hàng 2 và 3) */
    public static final int CHICKEN_WALK_FRAMES = 4;

    public static final int CHICKEN_ROW_IDLE_LEFT = 0;
    public static final int CHICKEN_ROW_IDLE_RIGHT = 1;
    public static final int CHICKEN_ROW_WALK_LEFT = 2;
    public static final int CHICKEN_ROW_WALK_RIGHT = 3;


    // 2. Cấu hình STANDARD ANIMALS (Cow, Pig, Sheep, Baby Animals & BABY CHICKEN)
    // [LƯU Ý] Baby Chicken 32x32 cũng tuân theo layout này:
    // Hàng 0-3: WALK (Down, Up, Left, Right) - 6 frames
    // Hàng 4-7: IDLE (Down, Up, Left, Right) - 4 frames

    /** Số frames mỗi hàng cho Standard animals WALK (hàng 0-3) */
    public static final int STANDARD_WALK_FRAMES = 6;

    /** Số frames mỗi hàng cho Standard animals IDLE (hàng 4-7) */
    public static final int STANDARD_IDLE_FRAMES = 4;

    // Định nghĩa chỉ số hàng cho Standard Layout
    public static final int STANDARD_ROW_WALK_DOWN = 0;
    public static final int STANDARD_ROW_WALK_UP = 1;
    public static final int STANDARD_ROW_WALK_LEFT = 2;
    public static final int STANDARD_ROW_WALK_RIGHT = 3;

    public static final int STANDARD_ROW_IDLE_DOWN = 4;
    public static final int STANDARD_ROW_IDLE_UP = 5;
    public static final int STANDARD_ROW_IDLE_LEFT = 6;
    public static final int STANDARD_ROW_IDLE_RIGHT = 7;

    // 3. Cấu hình EGG (nằm trong sheet Baby Chicken & Egg)
    // Hàng 4 (index 4), cột 4 và 5

    /** Hàng chứa sprite Trứng (trong sheet Baby Chicken) - tương đương hàng IDLE Down */
    public static final int EGG_ROW_INDEX = 4;

    /** Số frame trứng */
    public static final int EGG_FRAME_COUNT = 2; // Gồm 2 trạng thái: Đứng và Nằm

    /** Cột bắt đầu của trứng (Cột 4 và 5) */
    public static final int EGG_FRAME_START_INDEX = 4;

    // --- [MỚI] Cấu hình tốc độ Animation (ms per frame) ---
    // Số càng lớn thì animation càng chậm

    /** Tốc độ animation đi bộ của Gà (ms/frame) */
    public static final int ANIM_SPEED_CHICKEN_WALK = 200; // Tăng lên để đi chậm lại

    /** Tốc độ animation đứng yên của Gà (ms/frame) */
    public static final int ANIM_SPEED_CHICKEN_IDLE = 300; // Tăng lên để mổ/nhìn chậm lại

    /** Tốc độ animation đi bộ của thú thường (Bò, Lợn, Cừu) (ms/frame) */
    public static final int ANIM_SPEED_STANDARD_WALK = 250; // Tăng lên

    /** Tốc độ animation đứng yên của thú thường (ms/frame) */
    public static final int ANIM_SPEED_STANDARD_IDLE = 500; // Tăng lên để chớp mắt/đuôi chậm thôi

    private AnimalConfig() {}
}