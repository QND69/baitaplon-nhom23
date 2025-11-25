package com.example.farmSimulation.model;

import com.example.farmSimulation.config.GameLogicConfig;
import lombok.Getter;

public enum ItemType {
    // --- CÔNG CỤ (TOOLS) ---
    HOE("Hoe", 1, false, GameLogicConfig.MAX_DURABILITY_HOE),
    WATERING_CAN("Watering Can", 1, false, GameLogicConfig.MAX_WATER_CAPACITY),
    PICKAXE("Pickaxe", 1, false, GameLogicConfig.MAX_DURABILITY_PICKAXE),
    SHOVEL("Shovel", 1, false, GameLogicConfig.MAX_DURABILITY_SHOVEL),
    AXE("Axe", 1, false, GameLogicConfig.MAX_DURABILITY_AXE),
    SWORD("Sword", 1, false, GameLogicConfig.MAX_DURABILITY_SWORD),
    FERTILIZER("Fertilizer", 36, true, 0),

    // --- HẠT GIỐNG (SEEDS) ---
    SEEDS_STRAWBERRY("Strawberry Seeds", 36, true, 0),
    SEEDS_RADISH("Radish Seeds", 36, true, 0),
    SEEDS_POTATO("Potato Seeds", 36, true, 0),
    SEEDS_CARROT("Carrot Seeds", 36, true, 0),

    // --- SẢN PHẨM (CROPS/PRODUCE) ---
    STRAWBERRY("Strawberry", 36, true, 0),
    RADISH("Radish", 36, true, 0),
    POTATO("Potato", 36, true, 0),
    CARROT("Carrot", 36, true, 0),
    
    // --- VẬT LIỆU (MATERIALS) ---
    WOOD("Wood", 36, true, 0), // Gỗ để xây hàng rào
    
    // --- SẢN PHẨM ĐỘNG VẬT (ANIMAL PRODUCTS) ---
    EGG("Egg", 9, true, 0), // Trứng (maxStack = 1/4 của 36)
    MILK("Milk", 9, true, 0), // Sữa (maxStack = 1/4 của 36)
    WOOL("Wool", 9, true, 0), // Len (maxStack = 1/4 của 36)
    MEAT_CHICKEN("Chicken Meat", 9, true, 0), // Thịt gà (rơi ra khi gà chết, maxStack = 1/4 của 36)
    MEAT_COW("Cow Meat", 9, true, 0), // Thịt bò (rơi ra khi bò chết, maxStack = 1/4 của 36)
    MEAT_PIG("Pig Meat", 9, true, 0), // Thịt lợn (rơi ra khi lợn chết, maxStack = 1/4 của 36)
    MEAT_SHEEP("Sheep Meat", 9, true, 0), // Thịt cừu (rơi ra khi cừu chết, maxStack = 1/4 của 36)
    
    // --- CÔNG CỤ ĐỘNG VẬT (ANIMAL ITEMS) ---
    SHEARS("Shears", 1, false, 50), // Kéo cắt lông (có độ bền)
    MILK_BUCKET("Milk Bucket", 18, true, 0), // Xô rỗng (stack 18)
    FULL_MILK_BUCKET("Full Milk Bucket", 18, true, 0), // Xô đầy sữa (stack 18)
    
    // --- THỨC ĂN (FEED) ---
    SUPER_FEED("Super Feed", 36, true, 0), // Thức ăn tổng hợp
    
    // --- VẬT NUÔI (LIVESTOCK ITEMS - để đặt) ---
    // [SỬA] Đặt stackable = true để hiển thị số lượng trong HotbarView
    ITEM_COW("Cow", 9, true, 0), 
    ITEM_CHICKEN("Chicken", 9, true, 0), 
    ITEM_SHEEP("Sheep", 9, true, 0), 
    ITEM_PIG("Pig", 9, true, 0); 

    @Getter
    private final String name;
    @Getter
    private final int maxStackSize;
    @Getter
    private final boolean stackable;
    @Getter
    private final int maxDurability; // Độ bền tối đa

    ItemType(String name, int maxStackSize, boolean stackable, int maxDurability) {
        this.name = name;
        this.maxStackSize = maxStackSize;
        this.stackable = stackable;
        this.maxDurability = maxDurability;
    }

    // Kiểm tra xem item này có dùng độ bền không
    public boolean hasDurability() {
        return maxDurability > 0;
    }
}