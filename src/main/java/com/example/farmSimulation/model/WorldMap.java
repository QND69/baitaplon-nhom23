package com.example.farmSimulation.model;

import java.util.HashMap;


// Class này lưu trữ TOÀN BỘ dữ liệu bản đồ (Model).
// Đây là bản đồ hữu hạn
public class WorldMap {
    private HashMap<Long, Tile> tiles; // HashMap<Long, Tile> lưu trữ loại ô tại mỗi tọa độ [hàng][cột] và loại ô

    public WorldMap() {
        tiles = new HashMap<>();
    }

    //hàm tạo khóa
    /* Dùng phép dịch bit để ép 2 số int 32-bit col và row vào 1 key long 64-bit
    0xffffffffL là mask để lấy 32 bit thấp và loại bỏ sign-extension khi xử lý int như unsigned dưới dạng long*/
    private long toKey(int col, int row) {
        return ((long) col << 32) | (row & 0xffffffffL);
    }

    public Tile getTileType(int col, int row) {
        return tiles.getOrDefault(toKey(col, row), Tile.GRASS);
    }

    public void setTileType(int col, int row, Tile newTile) {
        tiles.put(toKey(col, row), newTile);
    }
}