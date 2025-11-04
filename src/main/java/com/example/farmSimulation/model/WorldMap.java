package com.example.farmSimulation.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor

// Class này lưu trữ TOÀN BỘ dữ liệu bản đồ (Model).
// Đây là bản đồ hữu hạn
public class WorldMap {

    private int width;  // Tổng số cột (chiều ngang) của map
    private int height; // Tổng số hàng (chiều dọc) của map
    private Tile[][] tiles; // Mảng 2D lưu trữ loại ô tại mỗi tọa độ [hàng][cột]

    public WorldMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new Tile[height][width]; // Khởi tạo mảng [hàng][cột]

        // Khởi tạo map mặc định (Lấp đầy toàn bộ map bằng GRASS)
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                tiles[row][col] = Tile.GRASS;
            }
        }

        // TEST
        setTileType(10, 5, Tile.SOIL);
        setTileType(11, 5, Tile.SOIL);
        setTileType(10, 6, Tile.SOIL);
        setTileType(11, 6, Tile.SOIL);

        // Ví dụ tạo một hồ nước nhỏ
        setTileType(15, 10, Tile.WATER);
        setTileType(16, 10, Tile.WATER);
        setTileType(15, 11, Tile.WATER);
        setTileType(16, 11, Tile.WATER);
    }

    // Hàm kiểm tra ô (col, row) là loại gì
    public Tile getTileType(int col, int row) {
        // *** KIỂM TRA BIÊN (RẤT QUAN TRỌNG) ***
        // Nếu camera nhìn ra ngoài biên giới (ví dụ: col = -1),
        // cứ trả về là CỎ (hoặc một loại ô "VOID" nào đó)
        if (col < 0 || col >= width || row < 0 || row >= height) {
            return Tile.GRASS; // Mặc định là CỎ
        }

        // Nếu trong biên, trả về loại ô đã lưu
        return tiles[row][col];
    }

    // Hàm để "thay đổi" trạng thái 1 ô (ví dụ: cuốc đất)
    public void setTileType(int col, int row, Tile type) {
        // Kiểm tra biên trước khi set
        if (col >= 0 && col < width && row >= 0 && row < height) {
            tiles[row][col] = type;
        }
    }
}