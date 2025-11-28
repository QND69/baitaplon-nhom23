package com.example.farmSimulation.model;

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
    
    // --- Hướng & Di chuyển ---
    /**
     * Hướng nhìn: 0 = Down, 1 = Right, 2 = Left, 3 = Up
     * Lưu ý: Sheet ảnh có đủ 4 hàng, không cần lật ảnh bằng code
     */
    private int direction; // 0: Down, 1: Right, 2: Left, 3: Up
    
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
    
    // --- Sinh trưởng ---
    private long spawnTime; // Thời điểm spawn (nanoTime) - để tính tuổi và trưởng thành
    private long lastDirectionChangeTime; // Thời điểm đổi hướng lần cuối (nanoTime)
    private long lastHungerUpdateTime; // Thời điểm cập nhật đói lần cuối (nanoTime)
    private long starvationStartTime; // Thời điểm bắt đầu đói (nanoTime) - để tính thời gian chết đói
    
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
}