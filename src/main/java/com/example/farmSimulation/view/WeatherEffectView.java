package com.example.farmSimulation.view;

import com.example.farmSimulation.config.WeatherConfig;
import com.example.farmSimulation.config.WindowConfig;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Quản lý hiệu ứng mưa (Rain Effect)
 */
public class WeatherEffectView extends Pane {
    private final List<Line> rainDrops;
    private final Random random;
    private boolean isRaining;
    
    public WeatherEffectView() {
        this.rainDrops = new ArrayList<>();
        this.random = new Random();
        this.isRaining = false;
        this.setPrefSize(WindowConfig.SCREEN_WIDTH, WindowConfig.SCREEN_HEIGHT);
        this.setMouseTransparent(true);
        this.setVisible(false);
    }
    
    /**
     * Cập nhật trạng thái mưa
     */
    public void setRaining(boolean raining) {
        if (this.isRaining == raining) {
            return; // Không thay đổi
        }
        
        this.isRaining = raining;
        this.setVisible(raining);
        
        if (raining) {
            // Tạo các giọt mưa
            createRainDrops();
        } else {
            // Xóa tất cả giọt mưa
            this.getChildren().clear();
            rainDrops.clear();
        }
    }
    
    /**
     * Tạo các giọt mưa
     */
    private void createRainDrops() {
        this.getChildren().clear();
        rainDrops.clear();
        
        for (int i = 0; i < WeatherConfig.RAIN_DROP_COUNT; i++) {
            Line drop = new Line();
            drop.setStroke(WeatherConfig.RAIN_COLOR);
            drop.setStrokeWidth(2.0);
            drop.setOpacity(WeatherConfig.RAIN_OPACITY);
            
            // Vị trí ngẫu nhiên
            double x = random.nextDouble() * WindowConfig.SCREEN_WIDTH;
            double y = random.nextDouble() * WindowConfig.SCREEN_HEIGHT;
            
            drop.setStartX(x);
            drop.setStartY(y);
            drop.setEndX(x);
            drop.setEndY(y + WeatherConfig.RAIN_DROP_LENGTH);
            
            rainDrops.add(drop);
            this.getChildren().add(drop);
        }
    }
    
    /**
     * Cập nhật animation mưa (gọi mỗi frame)
     */
    public void updateRain() {
        if (!isRaining) {
            return;
        }
        
        for (Line drop : rainDrops) {
            // Di chuyển giọt mưa xuống dưới
            double newStartY = drop.getStartY() + WeatherConfig.RAIN_DROP_SPEED;
            double newEndY = drop.getEndY() + WeatherConfig.RAIN_DROP_SPEED;
            
            // Nếu giọt mưa rơi ra ngoài màn hình, reset về phía trên
            if (newStartY > WindowConfig.SCREEN_HEIGHT) {
                newStartY = -WeatherConfig.RAIN_DROP_LENGTH;
                newEndY = 0;
                drop.setStartX(random.nextDouble() * WindowConfig.SCREEN_WIDTH);
            }
            
            drop.setStartY(newStartY);
            drop.setEndY(newEndY);
            drop.setEndX(drop.getStartX());
        }
    }
}

