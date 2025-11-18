package com.example.farmSimulation.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Class (Model) đại diện cho một thể hiện (instance) của cây đang được trồng.
 * Được lưu trữ bên trong TileData.
 * [SỬA] Đã thay đổi từ Enum sang Class.
 */
@Getter
@Setter
public class CropData {

    private CropType type; // Loại cây (Dâu, Cà rốt, ...)
    private int growthStage; // Giai đoạn hiện tại (0 là hạt giống)
    private long plantTime; // Thời điểm gieo hạt (dùng System.nanoTime())

    /**
     * Constructor để tạo một cây mới.
     * @param type Loại cây (từ enum CropType)
     * @param growthStage Giai đoạn bắt đầu (thường là 0)
     * @param plantTime Thời điểm gieo hạt
     */
    public CropData(CropType type, int growthStage, long plantTime) {
        this.type = type;
        this.growthStage = growthStage;
        this.plantTime = plantTime;
    }
}