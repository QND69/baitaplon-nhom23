package com.example.farmSimulation.config;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * Cấu hình cho hệ thống Thời tiết (Weather)
 */
public class WeatherConfig {
    
    // --- Các loại thời tiết ---
    public enum WeatherType {
        SUNNY, // Nắng
        RAIN   // Mưa
    }
    
    // --- Xác suất thời tiết ---
    public static final double SUNNY_CHANCE = 0.7; // 70% nắng
    public static final double RAIN_CHANCE = 0.3;  // 30% mưa
    
    // --- Cấu hình hiệu ứng Mưa ---
    public static final int RAIN_DROP_COUNT = 200; // Số lượng giọt mưa
    public static final double RAIN_DROP_LENGTH = 20.0; // Chiều dài giọt mưa
    public static final double RAIN_DROP_SPEED = 5.0; // Tốc độ rơi của mưa (pixel/frame)
    public static final Paint RAIN_COLOR = Color.rgb(150, 200, 255); // Màu mưa (xanh nhạt)
    public static final double RAIN_OPACITY = 0.6; // Độ trong suốt của mưa
    
    // --- Cấu hình hiệu ứng Tối khi mưa ---
    public static final double RAIN_DARKNESS_OPACITY = 0.2; // Độ tối thêm khi mưa (20%)
    
    // --- Thời gian thay đổi thời tiết ---
    public static final long WEATHER_UPDATE_INTERVAL_MS = 60000; // Cập nhật thời tiết mỗi 60 giây (1 phút)
    
    private WeatherConfig() {}
}





