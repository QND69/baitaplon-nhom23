package com.example.farmSimulation.model;

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

    // Constructor
    public Player() {
        this.tileX = 0;
        this.tileY = 0;
    }
}
