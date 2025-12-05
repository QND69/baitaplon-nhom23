package com.example.farmSimulation.model;

import lombok.Getter;

/**
 * Định nghĩa các loại cây trồng (Crop) trong game.
 * Chứa thông tin cố định như tên, số giai đoạn, và vị trí sprite.
 */
public enum CropType {
    // Định nghĩa các cây dựa
    // Tên, Số stage, Hàng sprite, Min sản lượng, Max sản lượng, [MỚI] Sản phẩm thu hoạch
    STRAWBERRY("Strawberry", 5, 0, 2, 4, ItemType.STRAWBERRY),
    DAIKON("Daikon", 5, 1, 1, 2, ItemType.DAIKON),
    POTATO("Potato", 5, 2, 2, 5, ItemType.POTATO),
    CARROT("Carrot", 5, 3, 1, 3, ItemType.CARROT),
    WATERMELON("Watermelon", 5, 4, 1, 2, ItemType.WATERMELON),
    TOMATO("Tomato", 5, 5, 2, 4, ItemType.TOMATO),
    WHEAT("Wheat", 5, 6, 2, 4, ItemType.WHEAT),
    CORN("Corn", 5, 7, 2, 4, ItemType.CORN);

    @Getter private final String name;
    @Getter private final int maxStages;
    @Getter private final int spriteRow;
    @Getter private final int minYield; // Sản lượng tối thiểu
    @Getter private final int maxYield; // Sản lưuọng tối đa
    @Getter private final ItemType harvestItem; // Loại item nhận được khi thu hoạch

    CropType(String name, int maxStages, int spriteRow, int minYield, int maxYield, ItemType harvestItem) {
        this.name = name;
        this.maxStages = maxStages;
        this.spriteRow = spriteRow;
        this.minYield = minYield;
        this.maxYield = maxYield;
        this.harvestItem = harvestItem;
    }
}