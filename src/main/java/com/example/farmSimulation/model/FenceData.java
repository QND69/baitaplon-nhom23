package com.example.farmSimulation.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Dữ liệu của hàng rào trên một tile.
 * Hàng rào có thể mở/đóng và có auto-tiling pattern.
 */
@Getter
@Setter
public class FenceData {
    private boolean isOpen; // Trạng thái mở/đóng
    private boolean isSolid; // Có chặn đường không (true = đóng, false = mở)
    
    // Auto-tiling pattern: xác định hình dạng hàng rào dựa trên các hàng rào xung quanh
    // 0-15: các pattern khác nhau (4 bit: top, right, bottom, left)
    private int tilePattern;
    
    public FenceData() {
        this.isOpen = false;
        this.isSolid = true; // Mặc định là đóng (chặn đường)
        this.tilePattern = 0;
    }
    
    public FenceData(boolean isOpen) {
        this.isOpen = isOpen;
        this.isSolid = !isOpen; // Mở thì không chặn, đóng thì chặn
        this.tilePattern = 0;
    }
}























