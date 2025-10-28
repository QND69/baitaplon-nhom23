package com.example.farmSimulation.model;

import java.util.HashMap;
import java.util.Map;

public class Inventory {
    Map<Item, Integer> items = new HashMap<>();
    private int maxQuantity;

    public Inventory() {}

    public Inventory(int maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    public int getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(int maxQuantity) {
        this.maxQuantity = maxQuantity;
    }
}
