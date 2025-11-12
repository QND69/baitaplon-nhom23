package com.example.farmSimulation.model;

import com.example.farmSimulation.config.GameConfig;
import com.example.farmSimulation.view.PlayerView;
import lombok.Getter;

@Getter
public class Camera {
    // Tọa độ thế giới logic
    private double worldOffsetX = 0.0;
    private double worldOffsetY = 0.0;

    public void initializePosition(Player mainPlayer, PlayerView playerView) {
        // *** Đặt vị trí khởi đầu của worldPane (camera) ***
        // Sao cho người chơi (ở giữa màn hình) nhìn vào tọa độ logic (tileX, tileY) của player
        // Vị trí worldPane = -Tọa độ logic player + (Nửa màn hình) - (Độ dài nhân vật)
        this.worldOffsetX = -mainPlayer.getTileX() + GameConfig.SCREEN_WIDTH / 2 - playerView.getWidth() / 2;
        this.worldOffsetY = -mainPlayer.getTileY() + GameConfig.SCREEN_HEIGHT / 2 - playerView.getHeight() / 2;
    }

    public void move(double dx, double dy) {
        this.worldOffsetX += dx;
        this.worldOffsetY += dy;
    }
}