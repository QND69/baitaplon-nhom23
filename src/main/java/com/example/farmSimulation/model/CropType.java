package com.example.farmSimulation.model;

import lombok.Getter;

/**
 * Định nghĩa các loại cây trồng (Crop) trong game.
 * Chứa thông tin cố định như tên, số giai đoạn, và vị trí sprite.
 */
public enum CropType {
    // Định nghĩa các cây dựa trên seed1.png
    // Tên, Số stage, Hàng sprite, Min sản lượng, Max sản lượng
    STRAWBERRY("Strawberry", 6, 0, 2, 4),
    RADISH("Radish", 6, 1, 1, 2),
    POTATO("Potato", 6, 2, 2, 5),
    CARROT("Carrot", 6, 3, 1, 3);

    @Getter private final String name;
    @Getter private final int maxStages;
    @Getter private final int spriteRow;

    // Cấu hình sản lượng
    @Getter private final int minYield;
    @Getter private final int maxYield;

    CropType(String name, int maxStages, int spriteRow, int minYield, int maxYield) {
        this.name = name;
        this.maxStages = maxStages;
        this.spriteRow = spriteRow;
        this.minYield = minYield;
        this.maxYield = maxYield;
    }
}