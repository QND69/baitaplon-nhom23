package com.example.farmSimulation.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Class (Model) chứa TOÀN BỘ trạng thái của một ô (tile) trong thế giới.
 * Thay thế cho việc chỉ lưu trữ một enum Tile đơn giản.
 */
@Getter
@Setter
public class TileData {
    // Đây sẽ là GRASS, SOIL (đất khô), hoặc SOIL_WET (đất ướt)
    private Tile baseTileType;

    // Dữ liệu trồng trọt
    private CropData cropData; // Cây đang trồng (null nếu không có)

    // Logic tưới nước
    private boolean isWatered = false; // Đã được tưới chưa
    private long lastWateredTime = 0; // Thời điểm tưới lần cuối
    private long dryStartTime = 0; // Thời điểm đất bắt đầu khô (để tính cây chết)

    // Logic Phân bón
    private boolean isFertilized = false; // Đất có đang hiển thị lớp phân bón không
    private long fertilizerStartTime = 0; // Thời điểm bắt đầu bón phân

    // Visual
    private CropStatusIndicator statusIndicator = CropStatusIndicator.NONE;

    /**
     * Constructor mặc định, tạo ra một ô GRASS
     */
    public TileData() {
        this.baseTileType = Tile.GRASS; // Mặc định là CỎ
    }

    /**
     * Constructor để tạo ô với loại cụ thể
     */
    public TileData(Tile baseTileType) {
        this.baseTileType = baseTileType;
    }

    // Copy constructor để tạo bản sao (dùng cho InteractionManager)
    public TileData(TileData other) {
        this.baseTileType = other.baseTileType;
        this.cropData = other.cropData; // Lưu ý: CropData là object, nếu cần deep copy phải clone thêm
        this.isWatered = other.isWatered;
        this.lastWateredTime = other.lastWateredTime;
        this.dryStartTime = other.dryStartTime;
        this.isFertilized = other.isFertilized;
        this.fertilizerStartTime = other.fertilizerStartTime;
        this.statusIndicator = other.statusIndicator;
    }
}