package com.example.farmSimulation.model;

import com.example.farmSimulation.config.GameLogicConfig; // [MỚI] Import config mới
import com.example.farmSimulation.view.MainGameView;

public class TimeManager {

    // --- Các trường (fields) để lưu cấu hình ---
    private double gameTimeSeconds; // Thời gian game hiện tại
    private final double SECONDS_PER_FRAME;
    private final double DAY_CYCLE_DURATION_SECONDS;
    private final double MIN_LIGHT_INTENSITY;
    private double currentLightIntensity; // Cường độ ánh sáng hiện tại (0.0 - 1.0)
    private int currentDay; // Ngày hiện tại (bắt đầu từ 1)
    private int lastCheckedDay; // Ngày đã kiểm tra lần cuối (để tránh trigger nhiều lần)

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
        this.currentLightIntensity = 1.0; // Khởi tạo ban đầu (sáng)
        this.currentDay = 1; // Bắt đầu từ ngày 1
        this.lastCheckedDay = 1; // Khởi tạo last checked day
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

        // Lưu light intensity hiện tại
        this.currentLightIntensity = lightIntensity;

        // Gửi cường độ ánh sáng tới View
        mainGameView.updateLighting(lightIntensity);
    }

    /**
     * Lấy cường độ ánh sáng hiện tại (dùng để kiểm tra ban đêm)
     * @return Cường độ ánh sáng (0.0 - 1.0)
     */
    public double getCurrentLightIntensity() {
        return currentLightIntensity;
    }

    // Phương thức mới để cập nhật thời gian
    private void updateGameTime() {
        // [SỬA] Đọc từ trường (field) của class
        this.gameTimeSeconds += this.SECONDS_PER_FRAME;

        // Update current day based on elapsed time
        updateCurrentDay();

        // Định dạng thời gian thành chuỗi HH:MM (thời gian trong ngày hiện tại) - định dạng 12 giờ
        double timeInCurrentDay = this.gameTimeSeconds % this.DAY_CYCLE_DURATION_SECONDS;
        int totalSeconds = (int) Math.round(timeInCurrentDay);
        int hours24 = totalSeconds / 3600; // Giờ theo format 24h (0-23)
        int minutes = (totalSeconds % 3600) / 60;

        // Chuyển đổi từ 24 giờ sang 12 giờ (đơn giản, không có AM/PM)
        int hours12 = hours24 % 12;
        if (hours12 == 0) {
            hours12 = 12; // 0 giờ và 12 giờ đều hiển thị là 12
        }

        String timeString = String.format("%d:%02d", hours12, minutes); // Định dạng 12 giờ: "12:10" hoặc "1:05"

        // Gửi day và time riêng biệt tới View
        mainGameView.updateTimer(this.currentDay, timeString);
    }

    /**
     * Update current day number based on elapsed game time
     */
    private void updateCurrentDay() {
        // Calculate current day based on total elapsed time
        // +1 because day starts from 1
        this.currentDay = (int)(this.gameTimeSeconds / this.DAY_CYCLE_DURATION_SECONDS) + 1;
    }

    /**
     * Get current day number
     * @return Current day (starts from 1)
     */
    public int getCurrentDay() {
        return currentDay;
    }

    /**
     * Check if a new day has started since last check
     * @return true if new day started since last check, false otherwise
     */
    public boolean hasNewDayStarted() {
        if (this.currentDay > this.lastCheckedDay) {
            this.lastCheckedDay = this.currentDay;
            return true; // New day started
        }
        return false; // Still same day
    }

    /**
     * Get game time in seconds
     * @return Current game time in seconds
     */
    public double getGameTimeSeconds() {
        return gameTimeSeconds;
    }

    /**
     * Check if it's night time
     * @return true if light intensity is low (night)
     */
    public boolean isNight() {
        return currentLightIntensity < 0.5; // Consider night when intensity < 50%
    }

    /**
     * [MỚI] Set game time (dùng cho Load Game)
     */
    public void setGameTime(double seconds) {
        this.gameTimeSeconds = seconds;
        updateCurrentDay(); // Recalculate day
        this.lastCheckedDay = this.currentDay; // Reset check to avoid re-triggering daily events immediately
    }
}