package com.example.farmSimulation.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a single shop slot with item, quantity, and discount information
 */
@Getter
@Setter
public class ShopSlot {
    private ItemType itemType;
    private int quantity; // Remaining stock
    private double discountRate; // 0.0 for normal, 0.2 for 20% off, etc.
    
    public ShopSlot(ItemType itemType, int quantity, double discountRate) {
        this.itemType = itemType;
        this.quantity = quantity;
        this.discountRate = discountRate;
    }
    
    /**
     * Get the final price after discount
     * @return Base price * (1 - discountRate)
     */
    public double getPrice() {
        int basePrice = itemType.getBuyPrice();
        return basePrice * (1.0 - discountRate);
    }
    
    /**
     * Get the original base price (before discount)
     * @return Base buy price of the item
     */
    public int getBasePrice() {
        return itemType.getBuyPrice();
    }
    
    /**
     * Check if this item is on sale
     * @return true if discountRate > 0
     */
    public boolean isOnSale() {
        return discountRate > 0.0;
    }
    
    /**
     * Check if this item is sold out
     * @return true if quantity <= 0
     */
    public boolean isSoldOut() {
        return quantity <= 0;
    }
}

