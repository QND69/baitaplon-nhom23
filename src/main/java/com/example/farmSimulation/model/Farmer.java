package com.example.farmSimulation.model;

public class Farmer {
    private String name;
    private double money;
    private int experience;
    private int level;
    private double stamina;

    public Farmer() {
    }

    public Farmer(String name, double money, int experience, int level, double stamina) {
        this.name = name;
        this.money = money;
        this.experience = experience;
        this.level = level;
        this.stamina = stamina;
    }

    public String getName() {
        return name;
    }

    public double getMoney() {
        return money;
    }

    public int getExperience() {
        return experience;
    }

    public int getLevel() {
        return level;
    }

    public double getStamina() {
        return stamina;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
