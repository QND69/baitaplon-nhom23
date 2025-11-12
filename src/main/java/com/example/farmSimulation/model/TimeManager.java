package com.example.farmSimulation.model;

import com.example.farmSimulation.config.GameConfig;
import com.example.farmSimulation.view.MainGameView;

public class TimeManager {
    // Thời gian và tốc độ cập nhật.
    // Lấy giá trị mặc định từ GameConfig
    private double gameTimeSeconds = GameConfig.PLAYER_START_TIME_SECONDS;
    // Lấy giá trị mặc định từ GameConfig
    private final double SECONDS_PER_FRAME = GameConfig.SECONDS_PER_FRAME;

    private final MainGameView mainGameView;

    public TimeManager(MainGameView mainGameView) {
        this.mainGameView = mainGameView;
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
        // Định nghĩa chu kỳ (Ví dụ: 24 phút = 1440 giây thực)
        // Lấy giá trị từ GameConfig
        final double DAY_CYCLE_DURATION_SECONDS = GameConfig.DAY_CYCLE_DURATION_SECONDS;

        // Tính tỷ lệ phần trăm đã trôi qua trong chu kỳ
        double cycleProgress = (this.gameTimeSeconds % DAY_CYCLE_DURATION_SECONDS) / DAY_CYCLE_DURATION_SECONDS;

        // Chuyển đổi tỷ lệ thành Cường độ Ánh sáng (0.0 đến 1.0)

        // Ví dụ về một chu kỳ đơn giản (dùng hàm sin để tạo độ cong)
        // Tỷ lệ Sin(0) = 0 (giữa đêm); Sin(Pi/2) = 1 (giữa ngày)
        // Cần dịch pha để 0% (00:00) là đêm, 50% (12:00) là ngày.

        // Biến đổi: (0 -> 1) thành (-Pi/2 -> 3*Pi/2) để bao phủ toàn bộ chu kỳ sin
        double radians = cycleProgress * 2 * Math.PI - (Math.PI / 2.0);

        // Ánh sáng sẽ dao động từ -1.0 đến 1.0. Dịch chuyển và chia 2 để được (0.0 đến 1.0)
        double lightIntensity = (Math.sin(radians) + 1.0) / 2.0;

        // Giới hạn tối thiểu (Đảm bảo ban đêm không quá tối, ví dụ min 0.1)
        // [TỐI ƯU] Lấy giá trị từ GameConfig
        final double MIN_INTENSITY = GameConfig.MIN_LIGHT_INTENSITY;
        lightIntensity = MIN_INTENSITY + (1.0 - MIN_INTENSITY) * lightIntensity;

        // Gửi cường độ ánh sáng tới View
        mainGameView.updateLighting(lightIntensity);
    }

    // Phương thức mới để cập nhật thời gian
    private void updateGameTime() {
        this.gameTimeSeconds += SECONDS_PER_FRAME;

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