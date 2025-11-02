package com.example.farmSimulation.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Player {
    private String name;
    private double money;
    private int experience;
    private int level;
    private double stamina;

    // Tọa độ logic của người chơi trong thế giới (không phải trên màn hình)
    private double worldX;
    private double worldY;

    // Constructor
    public Player() {
        this.worldX = 0;
        this.worldY = 0;
    }
}
