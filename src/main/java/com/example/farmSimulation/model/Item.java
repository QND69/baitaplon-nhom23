package com.example.farmSimulation.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Item {
    private String id;
    private String name;
    private String description;
    private int maxStack;
    private double buyPrice;
    private double sellPrice;

    //public abstract void usage();
}
