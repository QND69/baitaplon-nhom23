package com.example.farmSimulation.model;

/**
 * Class quản lý hệ thống hàng rào.
 * Xử lý auto-tiling và trạng thái mở/đóng.
 */
public class FenceManager {
    private final WorldMap worldMap;

    public FenceManager(WorldMap worldMap) {
        this.worldMap = worldMap;
    }

    /**
     * Tính toán auto-tiling pattern cho một ô hàng rào.
     * Pattern được tính dựa trên các hàng rào xung quanh (top, right, bottom, left).
     * * @param col Cột của tile
     * @param row Hàng của tile
     * @return Pattern index (0-15)
     */
    public int calculateFencePattern(int col, int row) {
        // Kiểm tra 4 hướng xung quanh
        boolean hasTop = isFence(col, row - 1);
        boolean hasRight = isFence(col + 1, row);
        boolean hasBottom = isFence(col, row + 1);
        boolean hasLeft = isFence(col - 1, row);

        // Tính pattern: 4 bit (top, right, bottom, left)
        int pattern = 0;
        if (hasTop) pattern |= 1 << 0;    // Bit 0: top
        if (hasRight) pattern |= 1 << 1;  // Bit 1: right
        if (hasBottom) pattern |= 1 << 2; // Bit 2: bottom
        if (hasLeft) pattern |= 1 << 3;   // Bit 3: left

        return pattern;
    }

    /**
     * Kiểm tra xem một tile có phải là hàng rào không.
     * [SỬA] Rào đang MỞ (Cổng) sẽ được coi như KHÔNG PHẢI LÀ RÀO để các rào bên cạnh ngắt kết nối.
     */
    private boolean isFence(int col, int row) {
        TileData data = worldMap.getTileData(col, row);
        // Chỉ kết nối nếu là FENCE và đang ĐÓNG (Solid)
        return data.getBaseTileType() == Tile.FENCE
                && data.getFenceData() != null
                && data.getFenceData().isSolid(); // isSolid == true nghĩa là Đóng
    }

    /**
     * Cập nhật auto-tiling pattern cho một ô hàng rào và các ô xung quanh
     */
    public void updateFencePattern(int col, int row) {
        // Cập nhật pattern cho chính ô này (nếu nó vẫn là rào)
        TileData data = worldMap.getTileData(col, row);
        if (data.getFenceData() != null && data.getBaseTileType() == Tile.FENCE) {
            int pattern = calculateFencePattern(col, row);
            data.getFenceData().setTilePattern(pattern);
            worldMap.setTileData(col, row, data);
        }

        // Cập nhật pattern cho các ô xung quanh (để chúng tự nối lại hoặc ngắt ra)
        updateFencePatternAt(col, row - 1); // Top
        updateFencePatternAt(col + 1, row); // Right
        updateFencePatternAt(col, row + 1); // Bottom
        updateFencePatternAt(col - 1, row); // Left
    }

    /**
     * Helper method để cập nhật pattern tại một vị trí cụ thể
     */
    private void updateFencePatternAt(int col, int row) {
        TileData data = worldMap.getTileData(col, row);
        // Chỉ update nếu ô hàng xóm cũng là rào
        if (data.getBaseTileType() == Tile.FENCE && data.getFenceData() != null) {
            int pattern = calculateFencePattern(col, row);
            data.getFenceData().setTilePattern(pattern);
            worldMap.setTileData(col, row, data);
        }
    }

    /**
     * Mở/đóng một ô hàng rào
     */
    public void toggleFence(int col, int row) {
        TileData data = worldMap.getTileData(col, row);
        if (data.getFenceData() != null) {
            FenceData fence = data.getFenceData();
            fence.setOpen(!fence.isOpen());
            fence.setSolid(!fence.isOpen()); // Mở thì không chặn, đóng thì chặn
            worldMap.setTileData(col, row, data);

            // [QUAN TRỌNG] Sau khi mở/đóng, trạng thái kết nối thay đổi -> Cần update lại hình ảnh
            // Cập nhật cho chính nó và hàng xóm
            updateFencePattern(col, row);
        }
    }

    /**
     * [MỚI] Cập nhật lại pattern cho TOÀN BỘ hàng rào trên bản đồ.
     * Được gọi sau khi load game để đảm bảo hình ảnh kết nối đúng.
     */
    public void updateAllFencePatterns() {
        for (java.util.Map.Entry<Long, TileData> entry : worldMap.getTileDataMap().entrySet()) {
            TileData data = entry.getValue();
            if (data.getBaseTileType() == Tile.FENCE && data.getFenceData() != null) {
                long key = entry.getKey();
                int col = (int) (key >> 32);
                int row = (int) key;

                int pattern = calculateFencePattern(col, row);
                data.getFenceData().setTilePattern(pattern);
            }
        }
    }
}