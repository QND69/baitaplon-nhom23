package com.example.farmSimulation.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// Clas đại diện cho một ô chứa đồ
public class ItemStack {
    private ItemType itemType;
    private int quantity;
    private int currentDurability; // Độ bền hiện tại

    public ItemStack(ItemType itemType, int quantity) {
        this.itemType = itemType;
        this.quantity = quantity;
        // Khởi tạo độ bền đầy
        this.currentDurability = itemType.getMaxDurability();
    }

    /**
     * Thêm số lượng vào stack.
     * @param amount Số lượng thêm
     * @return Số lượng THỪA ra nếu vượt quá max stack (0 nếu thêm thành công hết)
     */
    public int add(int amount) {
        int newQuantity = this.quantity + amount;
        if (newQuantity <= itemType.getMaxStackSize()) {
            this.quantity = newQuantity;
            return 0;
        } else {
            this.quantity = itemType.getMaxStackSize();
            return newQuantity - itemType.getMaxStackSize();
        }
    }

    /**
     * Giảm số lượng
     * @param amount Số lượng giảm
     * @return true nếu giảm thành công, false nếu không đủ
     */
    public boolean remove(int amount) {
        if (this.quantity >= amount) {
            this.quantity -= amount;
            return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return quantity <= 0;
    }

    /**
     * Giảm độ bền.
     * @return true nếu vật phẩm bị hỏng (độ bền <= 0)
     */
    public boolean decreaseDurability(int amount) {
        if (!itemType.hasDurability()) return false; // Item không có độ bền thì không giảm

        this.currentDurability -= amount;
        if (this.currentDurability < 0) this.currentDurability = 0;

        // Trả về true nếu hết độ bền (hỏng/hết nước)
        return this.currentDurability == 0;
    }

    /**
     * Hồi phục độ bền (Dùng cho fill nước)
     */
    public void refillDurability() {
        if (itemType.hasDurability()) {
            this.currentDurability = itemType.getMaxDurability();
        }
    }
}