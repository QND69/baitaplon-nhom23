package com.example.farmSimulation.model;

import com.example.farmSimulation.config.GameLogicConfig;
import com.example.farmSimulation.view.PlayerView;

/**
 * Quản lý logic tương tác dựa trên quy tắc (rule-based).
 */
public class InteractionManager {
    private final ActionManager actionManager; // Để thêm hành động chờ

    // Record nội bộ để chứa kết quả tương tác
    private record InteractionResult(
            TileData newTileData,           // Dữ liệu mới của ô đất (null nếu không đổi)
            PlayerView.PlayerState playerState, // Animation cần chạy
            long totalDurationMs            // Tổng thời gian thực hiện
    ) {
    }

    public InteractionManager(ActionManager actionManager) {
        this.actionManager = actionManager;
    }

    private int getDelayInFrames(long durationMs) {
        int delay = (int) (durationMs / (GameLogicConfig.SECONDS_PER_FRAME * 1000));
        return Math.max(1, delay); // Đảm bảo ít nhất 1 frame
    }

    /**
     * Hàm Reset Tile về trạng thái Soil khô mặc định
     */
    private TileData createResetTileData(TileData currentData) {
        TileData newData = new TileData(currentData);
        newData.setBaseTileType(Tile.SOIL); // Về đất khô
        newData.setCropData(null);          // Xóa cây
        newData.setWatered(false);          // Xóa nước
        newData.setFertilized(false);       // Xóa phân
        newData.setDryStartTime(System.nanoTime()); // Reset timer khô
        newData.setFertilizerStartTime(0);  // Reset timer phân
        newData.setStatusIndicator(CropStatusIndicator.NONE);
        return newData;
    }

    /**
     * Hàm trung tâm: Quyết định xem Input hiện tại sẽ tạo ra kết quả gì.
     */
    private InteractionResult calculateInteractionResult(Tool currentTool, TileData currentData) {
        Tile baseTile = currentData.getBaseTileType();

        // CUỐC (HOE): Cỏ -> Đất
        if (currentTool == Tool.HOE && baseTile == Tile.GRASS) {
            TileData newData = new TileData(currentData); // Copy
            newData.setBaseTileType(Tile.SOIL); // Dự kiến biến thành SOIL

            long duration = (long) GameLogicConfig.HOE_REPETITIONS * GameLogicConfig.HOE_DURATION_PER_REPETITION_MS;
            return new InteractionResult(newData, PlayerView.PlayerState.HOE, duration);
        }

        // HẠT GIỐNG: Gieo lên đất
        if (currentTool.name().startsWith("SEEDS_")) {
            // Chỉ gieo được nếu chưa có cây
            if ((baseTile == Tile.SOIL || baseTile == Tile.SOIL_WET) && currentData.getCropData() == null) {
                try {
                    CropType type = CropType.valueOf(currentTool.name().substring(6));

                    TileData newData = new TileData(currentData);
                    newData.setCropData(new CropData(type, 0, System.nanoTime()));

                    if (baseTile == Tile.SOIL) { // Nếu trồng trên đất khô, phải reset bộ đếm khô về hiện tại
                        newData.setDryStartTime(System.nanoTime());
                    } else { // Nếu đất ướt, không có thời gian khô
                        newData.setDryStartTime(0);
                    }

                    return new InteractionResult(newData, PlayerView.PlayerState.BUSY, GameLogicConfig.GENERIC_ACTION_DURATION_MS);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        // TƯỚI NƯỚC: Chỉ tưới đất KHÔ có CÂY
        if (currentTool == Tool.WATERING_CAN) {
            // Chỉ tưới được đất khô (SOIL) và phải có cây (theo yêu cầu mới của bạn)
            // "Người chơi chỉ có thể tưới lên đất khô có cây"
            if (baseTile == Tile.SOIL && currentData.getCropData() != null) {
                TileData newData = new TileData(currentData);
                newData.setBaseTileType(Tile.SOIL_WET);
                newData.setWatered(true);
                newData.setLastWateredTime(System.nanoTime());
                newData.setDryStartTime(0); // Xóa thời gian khô

                long duration = (long) GameLogicConfig.WATERING_CAN_REPETITIONS * GameLogicConfig.WATERING_CAN_DURATION_PER_REPETITION_MS;
                return new InteractionResult(newData, PlayerView.PlayerState.WATER, duration);
            }
        }

        // BÓN PHÂN: Bón bất cứ lúc nào sau khi nảy mầm (stage > 0)
        if (currentTool == Tool.FERTILIZER) {
            // Điều kiện: Có cây + Cây > stage 0 + Đất CHƯA có lớp phân
            // (Nếu muốn cho phép bón chồng lên để reset time thì bỏ !isFertilized)
            if (currentData.getCropData() != null &&
                    currentData.getCropData().getGrowthStage() > 0 &&
                    !currentData.isFertilized()) {
                TileData newData = new TileData(currentData);
                newData.setFertilized(true);
                newData.setFertilizerStartTime(System.nanoTime());

                return new InteractionResult(newData, PlayerView.PlayerState.BUSY, GameLogicConfig.GENERIC_ACTION_DURATION_MS);
            }
        }

        // XẺNG: Hủy cây
        if (currentTool == Tool.SHOVEL) {
            if (currentData.getCropData() != null) {
                // Reset hoàn toàn về đất khô
                TileData newData = createResetTileData(currentData);

                long duration = (long) GameLogicConfig.SHOVEL_REPETITIONS * GameLogicConfig.SHOVEL_DURATION_PER_REPETITION_MS;
                return new InteractionResult(newData, PlayerView.PlayerState.BUSY, duration);
            }
        }

        // THU HOẠCH (BẰNG TAY)
        if (currentTool == Tool.HAND) {
            CropData crop = currentData.getCropData();
            // Điều kiện: Có cây + Đã chín (Stage cuối)
            if (crop != null && crop.getGrowthStage() >= crop.getType().getMaxStages() - 1) {

                // Tính sản lượng
                int min = crop.getType().getMinYield();
                int max = crop.getType().getMaxYield();

                int yield = (int)(Math.random() * (max - min + 1)) + min;
                System.out.println("Harvested " + crop.getType().getName() + " x" + yield);
                // TODO: Cộng vào Inventory ở đây (sau này)

                // Reset hoàn toàn về đất khô sau khi thu hoạch
                TileData newData = createResetTileData(currentData);

                return new InteractionResult(newData, PlayerView.PlayerState.BUSY, GameLogicConfig.GENERIC_ACTION_DURATION_MS);
            }
        }

        return null; // Không có hành động hợp lệ
    }

    /**
     * Hàm processInteraction xử lý logic trồng trọt
     */
    public boolean processInteraction(Player mainPlayer, PlayerView playerView, WorldMap worldMap, int col, int row) {
        Tool currentTool = mainPlayer.getCurrentTool();
        TileData currentData = worldMap.getTileData(col, row);

        // Tính toán kết quả (Logic)
        InteractionResult result = calculateInteractionResult(currentTool, currentData);

        if (result != null) {
            // Cập nhật View (Animation)
            mainPlayer.setState(result.playerState());
            playerView.setState(mainPlayer.getState(), mainPlayer.getDirection());

            // Lên lịch Cập nhật Model (Delayed)
            // Dữ liệu TileData mới sẽ được áp dụng SAU KHI hết thời gian duration
            actionManager.addPendingAction(
                    new TimedTileAction(col, row, result.newTileData(), getDelayInFrames(result.totalDurationMs()))
            );
            return true;
        }
        return false;
    }
}