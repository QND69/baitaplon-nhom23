package com.example.farmSimulation.model;

import com.example.farmSimulation.config.AnimalConfig;
import lombok.Getter;
import lombok.Setter;

/**
 * Class (Model) đại diện cho một con vật cụ thể trong game.
 * Lưu trữ trạng thái của động vật: vị trí, hướng, tuổi, đói, sản phẩm, v.v.
 */
@Getter
@Setter
public class Animal {

    // --- Thông tin cơ bản ---
    private AnimalType type; // Loại động vật
    private double x; // Tọa độ X thực trên bản đồ (không phải tileX)
    private double y; // Tọa độ Y thực trên bản đồ (không phải tileY)

    // [MỚI] Vị trí neo (Anchor) để giới hạn phạm vi di chuyển
    private double anchorX;
    private double anchorY;

    // --- Hướng & Di chuyển ---
    /**
     * Hướng nhìn: 0 = Down, 1 = Right, 2 = Left, 3 = Up
     * Lưu ý: Sheet ảnh có đủ 4 hàng, không cần lật ảnh bằng code
     */
    private int direction; // 0: Down, 1: Right, 2: Left, 3: Up

    // --- Biến thể Sprite ---
    /**
     * Dùng cho EGG_ENTITY để lưu trạng thái ngẫu nhiên (Đứng hoặc Nằm).
     * 0 hoặc 1.
     */
    private int variant;

    // --- Trạng thái ---
    private int age; // Tuổi thọ (để tính lượng thịt rơi ra)
    private double hunger; // Chỉ số đói (0-100, 100 = no đói, 0 = chết đói)
    private boolean isDead; // Cờ đánh dấu đã chết

    // --- Hành động ---
    /**
     * Hành động hiện tại: IDLE, WALK, EAT
     */
    public enum Action {
        IDLE,
        WALK,
        EAT
    }
    private Action currentAction;

    // --- Sản phẩm ---
    private long productionTimer; // Thời gian đếm ngược để ra sản phẩm (nanoTime)
    private boolean hasProduct; // Cờ đánh dấu có sản phẩm sẵn sàng thu hoạch

    // --- Sinh trưởng & Logic ---
    private long spawnTime; // Thời điểm spawn (nanoTime) - để tính tuổi và trưởng thành
    private long lastDirectionChangeTime; // Thời điểm đổi hướng lần cuối (nanoTime)
    private long lastHungerUpdateTime; // Thời điểm cập nhật đói lần cuối (nanoTime)
    private long starvationStartTime; // Thời điểm bắt đầu đói (nanoTime) - để tính thời gian chết đói

    // [MỚI] Thời điểm kết thúc hành động hiện tại (để khóa trạng thái)
    private long actionEndTime;

    // [MỚI] Timer cho việc sinh sản (Breeding Cooldown) - nanoTime
    private long breedingCooldownTimer;

    // [MỚI - LOGIC SINH SẢN NÂNG CAO]
    private Animal breedingPartner; // Con vật đang nhắm tới để sinh sản
    private long matingStartTime; // Thời điểm bắt đầu "tán tỉnh" (đứng yên cạnh nhau), 0 nếu chưa

    /**
     * Constructor để tạo một con vật mới
     * @param type Loại động vật
     * @param x Tọa độ X ban đầu
     * @param y Tọa độ Y ban đầu
     */
    public Animal(AnimalType type, double x, double y) {
        this.type = type;
        this.x = x;
        this.y = y;

        // [MỚI] Gán vị trí neo là vị trí ban đầu
        this.anchorX = x;
        this.anchorY = y;

        this.direction = 0; // Mặc định nhìn xuống
        this.age = 0;
        this.hunger = com.example.farmSimulation.config.AnimalConfig.MAX_HUNGER; // Bắt đầu no đói
        this.isDead = false;
        this.currentAction = Action.IDLE;
        this.productionTimer = 0;
        this.hasProduct = false;
        this.spawnTime = System.nanoTime();
        this.lastDirectionChangeTime = System.nanoTime();
        this.lastHungerUpdateTime = System.nanoTime();
        this.starvationStartTime = 0; // Chưa đói
        this.actionEndTime = 0; // Sẵn sàng hành động ngay

        // [SỬA] Kiểm tra config để áp dụng cooldown sinh sản ngay khi spawn
        if (AnimalConfig.ENABLE_BREEDING_COOLDOWN_ON_SPAWN) {
            // Gán timer bằng thời gian hiện tại -> Logic check sẽ thấy (now - timer) gần bằng 0 < COOLDOWN -> Chưa đẻ được
            this.breedingCooldownTimer = System.nanoTime();
        } else {
            this.breedingCooldownTimer = 0; // Có thể sinh sản ngay nếu đủ điều kiện (trưởng thành)
        }

        this.breedingPartner = null;
        this.matingStartTime = 0;

        // Random trạng thái cho trứng (0 hoặc 1)
        if (type == AnimalType.EGG_ENTITY) {
            this.variant = Math.random() < 0.5 ? 0 : 1;
        } else {
            this.variant = 0;
        }
    }

    /**
     * Kiểm tra xem động vật có đói không
     */
    public boolean isHungry() {
        return hunger < com.example.farmSimulation.config.AnimalConfig.HUNGER_WARNING_THRESHOLD;
    }

    /**
     * Kiểm tra xem động vật có thể tạo sản phẩm không
     */
    public boolean canProduce() {
        return type.canProduce() && !isDead && !isHungry();
    }

    /**
     * Kiểm tra xem động vật có thể trưởng thành không
     */
    public boolean canGrow() {
        return type.canGrow() && !isDead;
    }

    /**
     * Tính tuổi của động vật (tính bằng giây)
     */
    public long getAgeInSeconds() {
        return (System.nanoTime() - spawnTime) / 1_000_000_000L;
    }

    /**
     * Tính số lượng thịt rơi ra khi giết
     * Chỉ động vật trưởng thành mới có thịt
     */
    public int calculateMeatDrop() {
        // Con non không có thịt
        if (isBaby() || type == AnimalType.EGG_ENTITY) {
            return 0;
        }

        // [SỬA] Đã bỏ điều kiện (age < MIN_AGE) để đảm bảo giết là có thịt
        // Chỉ cần là con trưởng thành (not baby) thì luôn rơi ít nhất 1
        int meat = (int) Math.min(age * com.example.farmSimulation.config.AnimalConfig.MEAT_RATE,
                com.example.farmSimulation.config.AnimalConfig.MAX_MEAT_DROP);
        return Math.max(meat, 1); // Ít nhất 1 miếng thịt
    }

    /**
     * Kiểm tra xem động vật có phải con non không
     */
    public boolean isBaby() {
        return type == AnimalType.BABY_CHICKEN ||
                type == AnimalType.BABY_COW ||
                type == AnimalType.BABY_PIG ||
                type == AnimalType.BABY_SHEEP ||
                type == AnimalType.EGG_ENTITY;
    }

    /**
     * Lấy loại thịt tương ứng với động vật
     * Chỉ động vật trưởng thành mới có thịt
     */
    public ItemType getMeatType() {
        // Con non không có thịt
        if (isBaby()) {
            return null;
        }

        switch (type) {
            case CHICKEN:
                return ItemType.MEAT_CHICKEN;
            case COW:
                return ItemType.MEAT_COW;
            case PIG:
                return ItemType.MEAT_PIG;
            case SHEEP:
                return ItemType.MEAT_SHEEP;
            default:
                return null; // Trứng hoặc loại khác không có thịt
        }
    }

    /**
     * Kiểm tra xem động vật có đang trong quá trình sinh sản không
     * (Đang đi tìm bạn tình HOẶC đang đứng tán tỉnh)
     */
    public boolean isBreeding() {
        return this.breedingPartner != null || this.matingStartTime > 0;
    }

    /**
     * [MỚI] Lấy Scale hiển thị dựa trên loại động vật
     * Đảm bảo con non được vẽ nhỏ hơn, sử dụng Config thay vì Hardcode.
     */
    public double getVisualScale() {
        switch (type) {
            case BABY_COW:
                return AnimalConfig.SCALE_BABY_COW;
            case BABY_PIG:
                return AnimalConfig.SCALE_BABY_PIG;
            case BABY_SHEEP:
                return AnimalConfig.SCALE_BABY_SHEEP;
            case BABY_CHICKEN:
                return AnimalConfig.SCALE_BABY_CHICKEN;
            case EGG_ENTITY:
                return AnimalConfig.SCALE_EGG;
            case CHICKEN:
                return AnimalConfig.SCALE_CHICKEN;
            case COW:
                return AnimalConfig.SCALE_COW;
            case PIG:
                return AnimalConfig.SCALE_PIG;
            case SHEEP:
                return AnimalConfig.SCALE_SHEEP;
            default:
                return 1.0;
        }
    }
}