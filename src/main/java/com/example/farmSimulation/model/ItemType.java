package com.example.farmSimulation.model;

import com.example.farmSimulation.config.GameLogicConfig;
import lombok.Getter;

public enum ItemType {
    // --- CÔNG CỤ (TOOLS) ---
    // Giá mua: 200-500, không bán được (sellPrice = 0)
    HOE("Hoe", 1, false, GameLogicConfig.MAX_DURABILITY_HOE, 200, 0),
    WATERING_CAN("Watering Can", 1, false, GameLogicConfig.MAX_WATER_CAPACITY, 250, 0),
    PICKAXE("Pickaxe", 1, false, GameLogicConfig.MAX_DURABILITY_PICKAXE, 300, 0),
    SHOVEL("Shovel", 1, false, GameLogicConfig.MAX_DURABILITY_SHOVEL, 250, 0),
    AXE("Axe", 1, false, GameLogicConfig.MAX_DURABILITY_AXE, 300, 0),
    SWORD("Sword", 1, false, GameLogicConfig.MAX_DURABILITY_SWORD, 400, 0),
    FERTILIZER("Fertilizer", 36, true, 0, 30, 0), // Phân bón: mua 30, không bán

    // --- HẠT GIỐNG (SEEDS) ---
    // Giá mua: 50-80, không bán được
    SEEDS_STRAWBERRY("Strawberry Seeds", 36, true, 0, 50, 0),
    SEEDS_RADISH("Radish Seeds", 36, true, 0, 50, 0),
    SEEDS_POTATO("Potato Seeds", 36, true, 0, 60, 0),
    SEEDS_CARROT("Carrot Seeds", 36, true, 0, 60, 0),

    // --- SẢN PHẨM (CROPS/PRODUCE) ---
    // Không mua được, chỉ bán: 80-120
    STRAWBERRY("Strawberry", 36, true, 0, 0, 100),
    RADISH("Radish", 36, true, 0, 0, 80),
    POTATO("Potato", 36, true, 0, 0, 100),
    CARROT("Carrot", 36, true, 0, 0, 100),
    
    // --- VẬT LIỆU (MATERIALS) ---
    WOOD("Wood", 36, true, 0, 20, 10), // Gỗ: mua 20, bán 10
    
    // --- SẢN PHẨM ĐỘNG VẬT (ANIMAL PRODUCTS) ---
    // Không mua được, chỉ bán: 50-150
    EGG("Egg", 9, true, 0, 0, 50), // Trứng
    MILK("Milk", 9, true, 0, 0, 80), // Sữa
    WOOL("Wool", 9, true, 0, 0, 100), // Len
    MEAT_CHICKEN("Chicken Meat", 9, true, 0, 0, 120), // Thịt gà
    MEAT_COW("Cow Meat", 9, true, 0, 0, 150), // Thịt bò
    MEAT_PIG("Pig Meat", 9, true, 0, 0, 130), // Thịt lợn
    MEAT_SHEEP("Sheep Meat", 9, true, 0, 0, 140), // Thịt cừu
    
    // --- CÔNG CỤ ĐỘNG VẬT (ANIMAL ITEMS) ---
    SHEARS("Shears", 1, false, 50, 300, 0), // Kéo: mua 300, không bán
    MILK_BUCKET("Milk Bucket", 18, true, 0, 100, 0), // Xô rỗng: mua 100
    FULL_MILK_BUCKET("Full Milk Bucket", 18, true, 0, 0, 150), // Xô đầy: bán 150
    
    // --- THỨC ĂN (FEED) ---
    SUPER_FEED("Super Feed", 36, true, 0, 150, 0), // Thức ăn siêu: mua 150 (cho mọi động vật)
    
    // --- ĐỒ ĂN NGƯỜI CHƠI (PLAYER FOOD) ---
    ENERGY_DRINK("Energy Drink", 9, true, 0, 100, 0), // Nước tăng lực: mua 100, hồi +50 Stamina
    
    // --- VẬT NUÔI (LIVESTOCK ITEMS - để đặt) ---
    // Giá mua: 500-800, không bán được
    ITEM_COW("Cow", 9, true, 0, 800, 0), 
    ITEM_CHICKEN("Chicken", 9, true, 0, 500, 0), 
    ITEM_SHEEP("Sheep", 9, true, 0, 700, 0), 
    ITEM_PIG("Pig", 9, true, 0, 600, 0); 

    @Getter
    private final String name;
    @Getter
    private final int maxStackSize;
    @Getter
    private final boolean stackable;
    @Getter
    private final int maxDurability; // Độ bền tối đa
    @Getter
    private final int buyPrice; // Giá mua từ shop (0 = không bán được)
    @Getter
    private final int sellPrice; // Giá bán cho shop (0 = không mua được)

    ItemType(String name, int maxStackSize, boolean stackable, int maxDurability, int buyPrice, int sellPrice) {
        this.name = name;
        this.maxStackSize = maxStackSize;
        this.stackable = stackable;
        this.maxDurability = maxDurability;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }
    
    // Constructor cũ để tương thích (giá mua/bán = 0)
    ItemType(String name, int maxStackSize, boolean stackable, int maxDurability) {
        this(name, maxStackSize, stackable, maxDurability, 0, 0);
    }

    // Kiểm tra xem item này có dùng độ bền không
    public boolean hasDurability() {
        return maxDurability > 0;
    }
}