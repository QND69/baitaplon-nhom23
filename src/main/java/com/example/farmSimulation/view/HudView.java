package com.example.farmSimulation.view;

import com.example.farmSimulation.config.GameLogicConfig;
import com.example.farmSimulation.config.HudConfig;
import com.example.farmSimulation.config.WindowConfig;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class HudView extends Pane {
    private final Label timerLabel; // bộ đếm thời gian
    private final Rectangle darknessOverlay; // Lớp phủ màu đen để tạo hiệu ứng tối

    // --- Các thành phần cho UI tạm thời ---
    private final Text temporaryText;       // Đối tượng Text để hiển thị thông báo tạm thời
    private SequentialTransition temporaryTextAnimation; // Animation cho text

    public HudView() {
        // [TỐI ƯU] Lấy giá trị mặc định từ GameConfig
        this.timerLabel = new Label(HudConfig.TIMER_DEFAULT_TEXT);
        // [TỐI ƯU] Lấy giá trị mặc định từ GameConfig
        this.timerLabel.setStyle(HudConfig.TIMER_STYLE_CSS);
        // Đặt Timer Label ở góc trên bên trái
        // [TỐI ƯU] Lấy giá trị mặc định từ GameConfig
        this.timerLabel.setLayoutX(HudConfig.TIMER_X_POSITION);
        this.timerLabel.setLayoutY(HudConfig.TIMER_Y_POSITION);

        // --- Khởi tạo Temporary Text ---
        temporaryText = new Text();
        temporaryText.setFont(HudConfig.TEMP_TEXT_FONT);
        temporaryText.setFill(HudConfig.TEMP_TEXT_COLOR);
        temporaryText.setFont(HudConfig.TEMP_TEXT_FONT);
        temporaryText.setStroke(HudConfig.TEMP_TEXT_STROKE_COLOR);
        temporaryText.setStrokeWidth(HudConfig.TEMP_TEXT_STROKE_WIDTH);
        temporaryText.setOpacity(0); // Ban đầu ẩn
        temporaryText.setManaged(false); // Không ảnh hưởng layout

        // Khởi tạo Lớp phủ Tối
        this.darknessOverlay = new Rectangle(WindowConfig.SCREEN_WIDTH, WindowConfig.SCREEN_HEIGHT);
        this.darknessOverlay.setFill(Color.BLACK);
        this.darknessOverlay.setOpacity(0.0); // Ban đầu không tối (sáng)
        this.darknessOverlay.setMouseTransparent(true); // Không nhận click chuột

        this.getChildren().addAll(timerLabel, temporaryText, darknessOverlay);
        this.setMouseTransparent(true); // Toàn bộ HUD không nhận click
    }

    /**
     * Hiển thị một đoạn text tạm thời trên đầu người chơi, sau đó mờ dần và biến mất.
     *
     * @param message       Nội dung text cần hiển thị.
     * @param playerScreenX Tọa độ X của người chơi trên màn hình.
     * @param playerScreenY Tọa độ Y của người chơi trên màn hình.
     */
    public void showTemporaryText(String message, double playerScreenX, double playerScreenY) {
        if (temporaryTextAnimation != null && temporaryTextAnimation.getStatus().equals(javafx.animation.Animation.Status.RUNNING)) {
            temporaryTextAnimation.stop(); // Dừng animation cũ nếu đang chạy
        }

        temporaryText.setText(message);
        temporaryText.setLayoutX(playerScreenX - temporaryText.getLayoutBounds().getWidth() / 2); // Căn giữa
        temporaryText.setLayoutY(playerScreenY + HudConfig.TEMP_TEXT_OFFSET_Y); // Trên đầu player
        temporaryText.setOpacity(1); // Hiển thị ngay lập tức

        FadeTransition fadeOut = new FadeTransition(Duration.millis(HudConfig.TEMP_TEXT_FADE_DURATION), temporaryText);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        PauseTransition pause = new PauseTransition(Duration.millis(HudConfig.TEMP_TEXT_DISPLAY_DURATION - HudConfig.TEMP_TEXT_FADE_DURATION));

        temporaryTextAnimation = new SequentialTransition(temporaryText, pause, fadeOut);
        temporaryTextAnimation.play();
    }

    public void updateTimer(String timeString) {
        this.timerLabel.setText(timeString);
    }

    /**
     * Cập nhật độ tối của màn hình dựa trên cường độ ánh sáng từ Model.
     * @param intensity Cường độ ánh sáng (1.0 là sáng nhất, 0.0 là tối nhất)
     */
    public void updateLighting(double intensity) {
        // Cường độ ánh sáng 1.0 => Opacity của lớp phủ tối là 0.0
        // Cường độ ánh sáng 0.0 => Opacity của lớp phủ tối là 1.0 (hoặc tối đa 0.8)

        // Đảo ngược cường độ để có độ mờ (opacity)
        // Giới hạn độ mờ tối đa (Ví dụ: 80% tối)
        final double MAX_DARKNESS = GameLogicConfig.MAX_DARKNESS_OPACITY;

        double opacity = 1.0 - intensity;

        // Áp dụng giới hạn tối đa
        opacity = Math.min(opacity, MAX_DARKNESS);

        this.darknessOverlay.setOpacity(opacity);
    }
}