package com.example.farmSimulation.model;

public class Item {
    private String name;
    private String usage;
    private double buyPrice;
    private double sellPrice;

    public Item() {
    }

    public Item(String name, String usage, double buyPrice, double sellPrice) {
        this.name = name;
        this.usage = usage;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    public String getName() {
        return name;
    }

    public String getUsage() {
        return usage;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }
}
