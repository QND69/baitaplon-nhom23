package com.example.farmSimulation.config;

// Chứa các hằng số cấu hình cho sprite của item/tool (tools.png)
public class ItemSpriteConfig {

    public static final double TOOL_SPRITE_WIDTH = 32.0;
    public static final double TOOL_SPRITE_HEIGHT = 32.0;

    // Tọa độ (cột) của từng công cụ trong ảnh tools.png
    public static final int TOOL_AXE_COL = 0;
    public static final int TOOL_HAMMER_COL = 1; // (Không dùng trong enum Tool)
    public static final int TOOL_HOE_COL = 2;
    public static final int TOOL_PICKAXE_COL = 3;
    public static final int ITEM_SEEDS_COL = 4; // (Không phải Tool)
    public static final int TOOL_WATERING_CAN_COL = 5;

    // (Tool.SHOVEL và Tool.HAND không có icon trong sheet này)
    private ItemSpriteConfig() {}
}