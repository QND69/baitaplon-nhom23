package com.example.farmSimulation.model;

import com.example.farmSimulation.config.GameConfig;
import com.example.farmSimulation.view.PlayerView;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class Player {
    private String name;
    private double money;
    private int experience;
    private int level;
    private double stamina;

    // Tọa độ logic của người chơi trong thế giới
    private double tileX;
    private double tileY;

    // Lưu trữ trạng thái LOGIC (Model)
    // PlayerView sẽ lưu trạng thái VISUAL (View)
    private PlayerView.PlayerState state;
    private PlayerView.Direction direction;

    // Constructor
    public Player() {
        this.tileX = GameConfig.PLAYER_START_X;
        this.tileY = GameConfig.PLAYER_START_Y;
        this.state = PlayerView.PlayerState.IDLE; // Trạng thái ban đầu
        this.direction = PlayerView.Direction.DOWN; // Hướng ban đầu
    }
}
