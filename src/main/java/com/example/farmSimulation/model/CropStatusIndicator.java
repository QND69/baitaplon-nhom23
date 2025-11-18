package com.example.farmSimulation.model;

/**
 * Enum kiểm soát icon trạng thái hiển thị trên một ô đất
 */
public enum CropStatusIndicator {
    NONE,             // Không hiển thị gì
    NEEDS_WATER,      // Hiển thị icon bình tưới
    NEEDS_FERTILIZER,  // Hiển thị icon túi phân bón
    NEED_WATER_AND_FERTILIZER, // Hiển thị icon kết hợp
    READY_TO_HARVEST,          // Hiển thị icon sẵn sàng thu hoạch
    DEAD
}