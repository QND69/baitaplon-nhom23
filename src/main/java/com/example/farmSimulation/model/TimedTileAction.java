package com.example.farmSimulation.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TimedTileAction {
    private int col; // Tọa độ X
    private int row; // Tọa độ Y
    // Nếu null nghĩa là không thay đổi dữ liệu map
    private TileData newTileData; // Loại Tile mới sẽ đổi thành
    private int framesRemaining; // Số frame còn lại

    // Lưu thông tin để trừ item khi hoàn thành
    private boolean consumeItem; // Có trừ item không?
    private int itemSlotIndex;   // Trừ ở slot nào? (Tránh trường hợp người chơi đổi tay)

    // Item thu hoạch (nếu có) để kích hoạt animation
    private ItemType harvestedItem;
    private int harvestedAmount; // Số lượng item thu hoạch được
    
    // Thông tin động vật cần xóa sau khi action hoàn thành (cho nhặt trứng)
    private double animalWorldX; // Tọa độ X của động vật cần xóa
    private double animalWorldY; // Tọa độ Y của động vật cần xóa
    
    // Thông tin thịt cần tạo sau khi action hoàn thành (cho giết động vật)
    private ItemType meatType; // Loại thịt
    private int meatAmount; // Số lượng thịt
    private int meatTileCol; // Cột tile để đặt thịt
    private int meatTileRow; // Hàng tile để đặt thịt

    // Constructor (dành cho các hành động không tốn item như Cuốc)
    public TimedTileAction(int col, int row, TileData newTileData, int framesRemaining) {
        this(col, row, newTileData, framesRemaining, false, -1);
    }

    // Constructor đầy đủ cho action cơ bản (chưa có harvest)
    public TimedTileAction(int col, int row, TileData newTileData, int framesRemaining, boolean consumeItem, int itemSlotIndex) {
        this.col = col;
        this.row = row;
        this.newTileData = newTileData;
        this.framesRemaining = framesRemaining;
        this.consumeItem = consumeItem;
        this.itemSlotIndex = itemSlotIndex;
        this.harvestedItem = null; // Mặc định không có item thu hoạch
        this.harvestedAmount = 0; // Mặc định số lượng = 0
    }

    public boolean tick() {
        this.framesRemaining--; // Đếm lùi
        return this.framesRemaining <= 0; // Trả về true nếu hết giờ
    }
}
