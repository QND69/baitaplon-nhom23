package com.example.farmSimulation.model;

import com.example.farmSimulation.config.GameLogicConfig; // [MỚI] Import config mới
import com.example.farmSimulation.view.MainGameView;

public class TimeManager {

    // --- Các trường (fields) để lưu cấu hình ---
    private double gameTimeSeconds; // Thời gian game hiện tại
    private final double SECONDS_PER_FRAME;
    private final double DAY_CYCLE_DURATION_SECONDS;
    private final double MIN_LIGHT_INTENSITY;

    private final MainGameView mainGameView;

    /**
     * [SỬA] Constructor (Hàm khởi tạo) đơn giản
     * Tự đọc các giá trị cấu hình từ GameLogicConfig
     */
    public TimeManager(MainGameView mainGameView) {
        this.mainGameView = mainGameView;

        // Tự gán giá trị từ file config
        this.gameTimeSeconds = GameLogicConfig.PLAYER_START_TIME_SECONDS;
        this.SECONDS_PER_FRAME = GameLogicConfig.SECONDS_PER_FRAME;
        this.DAY_CYCLE_DURATION_SECONDS = GameLogicConfig.DAY_CYCLE_DURATION_SECONDS;
        this.MIN_LIGHT_INTENSITY = GameLogicConfig.MIN_LIGHT_INTENSITY;
    }

    // Hàm update chính cho thời gian
    public void update() {
        updateGameTime();
        updateDayCycle();
    }

    /**
     * Tính toán cường độ ánh sáng dựa trên chu kỳ ngày đêm.
     * Cường độ: 1.0 (sáng) -> 0.0 (tối)
     */
    private void updateDayCycle() {
        // [SỬA] Đọc từ trường (field) của class
        double cycleProgress = (this.gameTimeSeconds % this.DAY_CYCLE_DURATION_SECONDS) / this.DAY_CYCLE_DURATION_SECONDS;

        // Chuyển đổi tỷ lệ thành Cường độ Ánh sáng (0.0 đến 1.0)
        // (Phép toán sin giữ nguyên)
        double radians = cycleProgress * 2 * Math.PI - (Math.PI / 2.0);
        double lightIntensity = (Math.sin(radians) + 1.0) / 2.0;

        // [SỬA] Đọc từ trường (field) của class
        lightIntensity = this.MIN_LIGHT_INTENSITY + (1.0 - this.MIN_LIGHT_INTENSITY) * lightIntensity;

        // Gửi cường độ ánh sáng tới View
        mainGameView.updateLighting(lightIntensity);
    }

    // Phương thức mới để cập nhật thời gian
    private void updateGameTime() {
        // [SỬA] Đọc từ trường (field) của class
        this.gameTimeSeconds += this.SECONDS_PER_FRAME;

        // Định dạng thời gian thành chuỗi HH:MM:SS
        int totalSeconds = (int) Math.round(this.gameTimeSeconds);
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        String timeString = String.format("Time: %02d:%02d:%02d", hours, minutes, seconds);

        // Gửi chuỗi thời gian đã định dạng tới View
        mainGameView.updateTimer(timeString);
    }
}