package com.example.farmSimulation.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    private String name;
    private double money;
    private int experience;
    private int level;
    private double stamina;

}
