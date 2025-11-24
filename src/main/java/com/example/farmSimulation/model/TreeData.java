package com.example.farmSimulation.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Dữ liệu của cây trên một tile.
 * Cây có các giai đoạn: 0 (gốc), 1 (cây nhỏ), 2 (cây lớn), 3 (cây trưởng thành)
 */
@Getter
@Setter
public class TreeData {
    private int growthStage; // 0 = gốc, 1-3 = các giai đoạn cây
    private long lastChopTime; // Thời gian chặt lần cuối (nanoTime)
    private long regrowStartTime; // Thời gian bắt đầu mọc lại (nanoTime)
    
    // Số lần đã chặt (để xác định chặt lần 1 hay lần 2)
    private int chopCount = 0;
    
    public TreeData() {
        this.growthStage = 2; // Mặc định là cây trưởng thành (stage 2)
        this.lastChopTime = 0;
        this.regrowStartTime = 0;
    }
    
    public TreeData(int growthStage) {
        this.growthStage = growthStage;
        this.lastChopTime = 0;
        this.regrowStartTime = 0;
    }
}

