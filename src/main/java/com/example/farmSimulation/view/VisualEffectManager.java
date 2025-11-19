package com.example.farmSimulation.view;

import com.example.farmSimulation.config.HudConfig;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

/**
 * Class chịu trách nhiệm quản lý các hiệu ứng hình ảnh tạm thời
 * như: item bay về túi, hiệu ứng hạt (particles), v.v.
 */
public class VisualEffectManager {

    /**
     * Tạo và chạy animation item bay từ bản đồ về vị trí đích (UI)
     * @param rootPane Pane chính để vẽ item lên
     * @param icon Ảnh của item
     * @param startX Tọa độ bắt đầu X
     * @param startY Tọa độ bắt đầu Y
     * @param endX Tọa độ kết thúc X
     * @param endY Tọa độ kết thúc Y
     */
    public void playItemFlyAnimation(Pane rootPane, Image icon, double startX, double startY, double endX, double endY) {
        // Tạo ImageView
        ImageView flyingItem = new ImageView(icon);
        flyingItem.setFitWidth(HudConfig.HARVEST_ICON_SIZE);
        flyingItem.setFitHeight(HudConfig.HARVEST_ICON_SIZE);

        // Đặt vị trí bắt đầu
        flyingItem.setLayoutX(startX);
        flyingItem.setLayoutY(startY);

        // Thêm vào rootPane
        rootPane.getChildren().add(flyingItem);

        // Tạo Animation Bay (Translate)
        TranslateTransition translate = new TranslateTransition(Duration.millis(HudConfig.HARVEST_FLY_DURATION_MS), flyingItem);
        // TranslateTransition dùng delta (khoảng cách di chuyển) chứ không phải tọa độ tuyệt đối
        translate.setToX(endX - startX);
        translate.setToY(endY - startY);

        // Tạo Animation Thu nhỏ (Scale)
        ScaleTransition scale = new ScaleTransition(Duration.millis(HudConfig.HARVEST_FLY_DURATION_MS), flyingItem);
        scale.setFromX(HudConfig.HARVEST_SCALE_FROM);
        scale.setFromY(HudConfig.HARVEST_SCALE_FROM);
        scale.setToX(HudConfig.HARVEST_SCALE_TO);
        scale.setToY(HudConfig.HARVEST_SCALE_TO);

        // Tạo Animation Mờ dần (Fade) ở cuối
        FadeTransition fade = new FadeTransition(Duration.millis(HudConfig.HARVEST_FADE_DURATION_MS), flyingItem);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setDelay(Duration.millis(HudConfig.HARVEST_FADE_DELAY_MS)); // Chờ gần đến đích mới mờ

        // Chạy song song
        ParallelTransition parallel = new ParallelTransition(translate, scale, fade);

        // Dọn dẹp khi xong
        parallel.setOnFinished(e -> rootPane.getChildren().remove(flyingItem));

        parallel.play();
    }
}