package com.example.farmSimulation.model;

public class Tool extends Item {
    public double durability;

    public Tool(String name, String usage, double buyPrice, double sellPrice, double durability) {
        super(name, usage, buyPrice, sellPrice);
        this.durability = durability;
    }

    public double getDurability() {
        return durability;
    }

    public void setDurability(double durability) {
        this.durability = durability;
    }
}
