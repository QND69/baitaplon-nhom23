package com.example.farmSimulation.model;

import com.example.farmSimulation.config.GameLogicConfig;
import lombok.Getter;

public enum ItemType {
    // --- CÔNG CỤ (TOOLS) ---
    HOE("Hoe", 1, false, GameLogicConfig.MAX_DURABILITY_HOE),
    WATERING_CAN("Watering Can", 1, false, GameLogicConfig.MAX_WATER_CAPACITY),
    PICKAXE("Pickaxe", 1, false, GameLogicConfig.MAX_DURABILITY_PICKAXE),
    SHOVEL("Shovel", 1, false, GameLogicConfig.MAX_DURABILITY_SHOVEL),
    AXE("Axe", 1, false, GameLogicConfig.MAX_DURABILITY_AXE), // Rìu để chặt cây
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
    WOOD("Wood", 36, true, 0); // Gỗ để xây hàng rào

    @Getter
    private final String name;
    @Getter
    private final int maxStackSize;
    @Getter
    private final boolean stackable;
    @Getter
    private final int maxDurability; // Độ bền tối đa

    ItemType(String name, int maxStackSize, boolean stackable,  int maxDurability) {
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