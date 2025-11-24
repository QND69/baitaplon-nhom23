package com.example.farmSimulation.model;

import com.example.farmSimulation.config.CropConfig;
import com.example.farmSimulation.config.GameLogicConfig;
import com.example.farmSimulation.config.HudConfig;
import com.example.farmSimulation.config.TreeConfig;
import com.example.farmSimulation.view.PlayerView;
import com.example.farmSimulation.config.FenceConfig;
import com.example.farmSimulation.config.PlayerSpriteConfig;
import com.example.farmSimulation.config.WorldConfig;

import java.util.Random;

/**
 * Quản lý logic tương tác dựa trên quy tắc (rule-based).
 */
public class InteractionManager {
    private final ActionManager actionManager; // Để thêm hành động chờ

    // Record nội bộ để chứa kết quả tương tác
    private record InteractionResult(
            TileData newTileData,               // Dữ liệu mới của ô đất (null nếu không đổi)
            PlayerView.PlayerState playerState, // Animation cần chạy
            long totalDurationMs,                // Tổng thời gian thực hiện
            boolean consumeItem,                 // Cờ báo hiệu có trừ item không
            ItemType harvestedItem,              // Item thu hoạch được (để làm hiệu ứng)
            int harvestedAmount                  // Số lượng item thu hoạch được
    ) {
    }

    public InteractionManager(ActionManager actionManager) {
        this.actionManager = actionManager;
    }

    private int getDelayInFrames(long durationMs) {
        int delay = (int) (durationMs / (GameLogicConfig.SECONDS_PER_FRAME * 1000));
        return Math.max(1, delay); // Đảm bảo ít nhất 1 frame
    }

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

    // Helper check va chạm AABB
    private boolean aabbIntersect(double minX1, double maxX1, double minY1, double maxY1,
                                  double minX2, double maxX2, double minY2, double maxY2) {
        return minX1 < maxX2 && maxX1 > minX2 && minY1 < maxY2 && maxY1 > minY2;
    }

    // Helper check xem một ô có phải rào đóng không (để tính nối)
    private boolean isFenceSolid(int col, int row, WorldMap worldMap) {
        TileData data = worldMap.getTileData(col, row);
        return data.getBaseTileType() == Tile.FENCE && data.getFenceData() != null && data.getFenceData().isSolid();
    }

    /**
     * [SỬA] Kiểm tra xem người chơi có đang đứng chắn vị trí đặt hàng rào không.
     * Tính toán cả cọc trung tâm và các thanh nối (rails) của ô HIỆN TẠI và các ô HÀNG XÓM.
     */
    private boolean isPlayerBlocking(int col, int row, Player mainPlayer, WorldMap worldMap) {
        // 1. Tính Hitbox Người chơi (World Coords)
        double pX = mainPlayer.getTileX();
        double pY = mainPlayer.getTileY();
        double pCenterX = pX + PlayerSpriteConfig.BASE_PLAYER_FRAME_WIDTH / 2.0;
        double pCenterY = pY + PlayerSpriteConfig.BASE_PLAYER_FRAME_HEIGHT
                - (PlayerSpriteConfig.COLLISION_BOX_HEIGHT / 2.0)
                - PlayerSpriteConfig.COLLISION_BOX_BOTTOM_PADDING;

        double pHalfW = PlayerSpriteConfig.COLLISION_BOX_WIDTH / 2.0;
        double pHalfH = PlayerSpriteConfig.COLLISION_BOX_HEIGHT / 2.0;

        double pMinX = pCenterX - pHalfW;
        double pMaxX = pCenterX + pHalfW;
        double pMinY = pCenterY - pHalfH;
        double pMaxY = pCenterY + pHalfH;

        // 2. Tính Hitbox Cọc Hàng Rào Trung Tâm của ô ĐANG ĐẶT (Tile C)
        double tileWorldX = col * WorldConfig.TILE_SIZE;
        double tileWorldY = row * WorldConfig.TILE_SIZE;

        double fCenterX = tileWorldX + WorldConfig.TILE_SIZE / 2.0;
        double fCenterY = (tileWorldY + WorldConfig.TILE_SIZE)
                - (FenceConfig.FENCE_HITBOX_HEIGHT / 2.0)
                - FenceConfig.FENCE_HITBOX_Y_OFFSET_FROM_BOTTOM;

        double fHalfW = FenceConfig.FENCE_HITBOX_WIDTH / 2.0;
        double fHalfH = FenceConfig.FENCE_HITBOX_HEIGHT / 2.0;

        double fMinX = fCenterX - fHalfW;
        double fMaxX = fCenterX + fHalfW;
        double fMinY = fCenterY - fHalfH;
        double fMaxY = fCenterY + fHalfH;

        // [CHECK 1] Va chạm với Cọc Trung Tâm (Tile C)
        if (aabbIntersect(pMinX, pMaxX, pMinY, pMaxY, fMinX, fMaxX, fMinY, fMaxY)) {
            return true;
        }

        // 3. Tính toán va chạm với các thanh nối (Rails)
        // Lưu ý: Cần kiểm tra 2 phần cho mỗi hướng nối:
        // Phần 1: Rail nằm trong ô Tile C (từ cọc ra biên)
        // Phần 2: Rail nằm trong ô Hàng xóm (từ biên vào cọc hàng xóm) - ĐÂY LÀ PHẦN HAY BỊ KẸT

        // --- CHECK LEFT (col - 1) ---
        if (isFenceSolid(col - 1, row, worldMap)) {
            // Phần 1: Trong Tile C (Từ mép trái Tile C -> Mép trái Cọc C)
            if (aabbIntersect(pMinX, pMaxX, pMinY, pMaxY, tileWorldX, fMinX, fMinY, fMaxY)) return true;

            // Phần 2: Trong Tile Left (Từ mép phải Cọc Hàng Xóm -> Mép phải Tile Left)
            // Tile Left kết thúc tại tileWorldX. Cọc hàng xóm nằm ở trung tâm Tile Left.
            // Mép phải cọc hàng xóm = (tileWorldX - 32) + fHalfW
            double neighborPostRightX = (tileWorldX - (WorldConfig.TILE_SIZE / 2.0)) + fHalfW;
            if (aabbIntersect(pMinX, pMaxX, pMinY, pMaxY, neighborPostRightX, tileWorldX, fMinY, fMaxY)) return true;
        }

        // --- CHECK RIGHT (col + 1) ---
        if (isFenceSolid(col + 1, row, worldMap)) {
            // Phần 1: Trong Tile C (Từ mép phải Cọc C -> Mép phải Tile C)
            if (aabbIntersect(pMinX, pMaxX, pMinY, pMaxY, fMaxX, tileWorldX + WorldConfig.TILE_SIZE, fMinY, fMaxY)) return true;

            // Phần 2: Trong Tile Right (Từ mép trái Tile Right -> Mép trái Cọc Hàng Xóm)
            // Tile Right bắt đầu tại tileWorldX + 64.
            // Mép trái cọc hàng xóm = (tileWorldX + 64 + 32) - fHalfW
            double neighborPostLeftX = (tileWorldX + WorldConfig.TILE_SIZE + (WorldConfig.TILE_SIZE / 2.0)) - fHalfW;
            if (aabbIntersect(pMinX, pMaxX, pMinY, pMaxY, tileWorldX + WorldConfig.TILE_SIZE, neighborPostLeftX, fMinY, fMaxY)) return true;
        }

        // --- CHECK TOP (row - 1) ---
        if (isFenceSolid(col, row - 1, worldMap)) {
            // Tính Y của cọc hàng xóm trên.
            // Công thức Y của cọc: (TileY + 64) - 10 - 18 = TileY + 36.
            // Cọc trên (row-1): TileY_Top = tileWorldY - 64. -> CenterY = tileWorldY - 64 + 36 = tileWorldY - 28.
            // Đáy cọc trên = CenterY + 10 = tileWorldY - 18.
            double neighborPostBottomY = fCenterY - WorldConfig.TILE_SIZE; // Cách nhau đúng 1 ô (64px)

            // Phần 1: Trong Tile C (Từ mép trên Tile C -> Mép trên Cọc C)
            if (aabbIntersect(pMinX, pMaxX, pMinY, pMaxY, fMinX, fMaxX, tileWorldY, fMinY)) return true;

            // Phần 2: Trong Tile Top (Từ đáy Cọc Hàng Xóm -> Đáy Tile Top/Mép trên Tile C)
            // Đáy Cọc Hàng Xóm = neighborPostBottomY + fHalfH
            if (aabbIntersect(pMinX, pMaxX, pMinY, pMaxY, fMinX, fMaxX, neighborPostBottomY + fHalfH, tileWorldY)) return true;
        }

        // --- CHECK BOTTOM (row + 1) ---
        if (isFenceSolid(col, row + 1, worldMap)) {
            // Cọc dưới (row+1): CenterY = tileWorldY + 64 + 36 = tileWorldY + 100.
            // Đỉnh cọc dưới = CenterY - 10 = tileWorldY + 90.
            double neighborPostTopY = fCenterY + WorldConfig.TILE_SIZE; // Cách nhau đúng 1 ô

            // Phần 1: Trong Tile C (Từ đáy Cọc C -> Đáy Tile C)
            if (aabbIntersect(pMinX, pMaxX, pMinY, pMaxY, fMinX, fMaxX, fMaxY, tileWorldY + WorldConfig.TILE_SIZE)) return true;

            // Phần 2: Trong Tile Bottom (Từ mép trên Tile Bottom -> Đỉnh Cọc Hàng Xóm)
            // Đỉnh Cọc Hàng Xóm = neighborPostTopY - fHalfH
            if (aabbIntersect(pMinX, pMaxX, pMinY, pMaxY, fMinX, fMaxX, tileWorldY + WorldConfig.TILE_SIZE, neighborPostTopY - fHalfH)) return true;
        }

        return false;
    }

    /**
     * Hàm trung tâm: Quyết định xem Input hiện tại sẽ tạo ra kết quả gì.
     */
    private InteractionResult calculateInteractionResult(ItemStack currentStack, TileData currentData, Player mainPlayer, WorldMap worldMap, int col, int row) {
        if (currentStack == null) return null; // Tay không

        ItemType itemType = currentStack.getItemType();
        Tile baseTile = currentData.getBaseTileType();

        // CUỐC (HOE): Cỏ -> Đất
        if (itemType == ItemType.HOE && baseTile == Tile.GRASS) {
            TileData newData = new TileData(currentData);
            newData.setBaseTileType(Tile.SOIL);
            long duration = (long) GameLogicConfig.HOE_REPETITIONS * GameLogicConfig.HOE_DURATION_PER_REPETITION_MS;
            return new InteractionResult(newData, PlayerView.PlayerState.HOE, duration, true, null, 0);
        }

        // HẠT GIỐNG: Gieo lên đất
        if (itemType.name().startsWith("SEEDS_")) {
            if ((baseTile == Tile.SOIL || baseTile == Tile.SOIL_WET) && currentData.getCropData() == null) {
                try {
                    CropType type = CropType.valueOf(itemType.name().substring(6));
                    TileData newData = new TileData(currentData);
                    newData.setCropData(new CropData(type, 0, System.nanoTime()));
                    if (baseTile == Tile.SOIL) {
                        newData.setDryStartTime(System.nanoTime());
                    } else {
                        newData.setDryStartTime(0);
                    }
                    return new InteractionResult(newData, PlayerView.PlayerState.PLANT, GameLogicConfig.PLANT_DURATION_MS, true, null, 0);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        // TƯỚI NƯỚC
        if (itemType == ItemType.WATERING_CAN) {
            if (currentStack.getCurrentDurability() <= 0) return null;

            if (baseTile == Tile.WATER) {
                currentStack.refillDurability();
                System.out.println("Refilled Water!");
                return new InteractionResult(null, PlayerView.PlayerState.WATER, 500, false, null, 0);
            }

            if (baseTile == Tile.SOIL && currentData.getCropData() != null) {
                TileData newData = new TileData(currentData);
                newData.setBaseTileType(Tile.SOIL_WET);
                newData.setWatered(true);
                newData.setLastWateredTime(System.nanoTime());
                newData.setDryStartTime(0);
                long duration = (long) GameLogicConfig.WATERING_CAN_REPETITIONS * GameLogicConfig.WATERING_CAN_DURATION_PER_REPETITION_MS;
                return new InteractionResult(newData, PlayerView.PlayerState.WATER, duration, true, null, 0);
            }
        }

        // BÓN PHÂN
        if (itemType == ItemType.FERTILIZER) {
            if (currentData.getCropData() != null && !currentData.isFertilized()) {
                int currentStage = currentData.getCropData().getGrowthStage();
                int maxStage = currentData.getCropData().getType().getMaxStages();
                if (currentStage >= CropConfig.MIN_GROWTH_STAGE_FOR_FERTILIZER && currentStage < maxStage - 1) {
                    TileData newData = new TileData(currentData);
                    newData.setFertilized(true);
                    newData.setFertilizerStartTime(System.nanoTime());
                    return new InteractionResult(newData, PlayerView.PlayerState.FERTILIZE, GameLogicConfig.FERTILIZER_DURATION_MS, true, null, 0);
                } else {
                    return null;
                }
            }
        }

        // XẺNG
        if (itemType == ItemType.SHOVEL) {
            if (currentData.getCropData() != null) {
                TileData newData = createResetTileData(currentData);
                long duration = (long) GameLogicConfig.SHOVEL_REPETITIONS * GameLogicConfig.SHOVEL_DURATION_PER_REPETITION_MS;
                return new InteractionResult(newData, PlayerView.PlayerState.SHOVEL, duration, true, null, 0);
            }
        }

        // RÌU (AXE)
        if (itemType == ItemType.AXE) {
            if (currentData.getTreeData() != null && baseTile == Tile.TREE) {
                TreeData tree = currentData.getTreeData();
                TileData newData = new TileData(currentData);
                int woodAmount = 0;
                
                if (tree.getGrowthStage() > 0) {
                    int currentStage = tree.getGrowthStage();
                    if (currentStage == 1) woodAmount = TreeConfig.WOOD_PER_STAGE_1;
                    else if (currentStage == 2) woodAmount = TreeConfig.WOOD_PER_STAGE_2;
                    
                    tree.setGrowthStage(0);
                    tree.setLastChopTime(System.nanoTime());
                    tree.setRegrowStartTime(System.nanoTime());
                    tree.setChopCount(1);
                    newData.setTreeData(tree);
                    long duration = (long) GameLogicConfig.AXE_REPETITIONS * GameLogicConfig.AXE_DURATION_PER_REPETITION_MS;
                    return new InteractionResult(newData, PlayerView.PlayerState.AXE, duration, true, ItemType.WOOD, woodAmount);
                }
                else if (tree.getGrowthStage() == 0 && tree.getChopCount() == 1) {
                    newData.setBaseTileType(Tile.GRASS);
                    newData.setTreeData(null);
                    long duration = (long) GameLogicConfig.AXE_REPETITIONS * GameLogicConfig.AXE_DURATION_PER_REPETITION_MS;
                    return new InteractionResult(newData, PlayerView.PlayerState.AXE, duration, true, null, 0);
                }
            }
            if (currentData.getFenceData() != null && baseTile == Tile.FENCE) {
                TileData newData = new TileData(currentData);
                newData.setBaseTileType(Tile.GRASS);
                newData.setFenceData(null);
                long duration = (long) GameLogicConfig.AXE_REPETITIONS * GameLogicConfig.AXE_DURATION_PER_REPETITION_MS;
                return new InteractionResult(newData, PlayerView.PlayerState.AXE, duration, true, null, 0);
            }
        }

        // GỖ (WOOD): Xây hàng rào
        if (itemType == ItemType.WOOD) {
            if (baseTile == Tile.GRASS && 
                currentData.getCropData() == null && 
                currentData.getTreeData() == null &&
                currentData.getFenceData() == null) {
                
                // [SỬA] Kiểm tra va chạm kỹ lưỡng với cả các phần kết nối hàng xóm
                if (isPlayerBlocking(col, row, mainPlayer, worldMap)) {
                    return null;
                }

                TileData newData = new TileData(currentData);
                newData.setBaseTileType(Tile.FENCE);
                FenceData fence = new FenceData(false);
                newData.setFenceData(fence);
                return new InteractionResult(newData, PlayerView.PlayerState.BUSY, GameLogicConfig.PLANT_DURATION_MS, true, null, 0);
            }
        }

        return null;
    }

    private InteractionResult checkHarvest(TileData currentData, Player mainPlayer) {
        CropData crop = currentData.getCropData();
        Random random = new Random();
        if (crop != null && crop.getGrowthStage() >= crop.getType().getMaxStages() - 1) {
            int yield = random.nextInt(crop.getType().getMaxYield() - crop.getType().getMinYield() + 1) + crop.getType().getMinYield();
            boolean success = mainPlayer.addItem(crop.getType().getHarvestItem(), yield);

            if (!success) {
                return null;
            }

            TileData newData = createResetTileData(currentData);
            return new InteractionResult(newData, PlayerView.PlayerState.BUSY, GameLogicConfig.GENERIC_ACTION_DURATION_MS, false, crop.getType().getHarvestItem(), yield);
        }
        return null;
    }

    public String processInteraction(Player mainPlayer, PlayerView playerView, WorldMap worldMap, int col, int row) {
        ItemStack currentStack = mainPlayer.getCurrentItem();
        TileData currentData = worldMap.getTileData(col, row);
        InteractionResult result = null;

        CropData crop = currentData.getCropData();
        if (crop != null && crop.getGrowthStage() >= crop.getType().getMaxStages() - 1) {
            result = checkHarvest(currentData, mainPlayer);
            if (result == null) {
                return HudConfig.TEXT_INVENTORY_FULL;
            }
        }
        else {
            result = calculateInteractionResult(currentStack, currentData, mainPlayer, worldMap, col, row);
        }

        if (result == null) {
            if (currentStack != null) {
                if (currentStack.getItemType() == ItemType.WATERING_CAN && currentStack.getCurrentDurability() <= 0) {
                    return HudConfig.TEXT_WATER_EMPTY;
                }
                else if (currentStack.getItemType() == ItemType.FERTILIZER && currentData.getCropData() != null) {
                    if (currentData.getCropData().getGrowthStage() < CropConfig.MIN_GROWTH_STAGE_FOR_FERTILIZER) {
                        return HudConfig.TEXT_PLANT_CAN_NOT_BE_FERTILIZED;
                    }
                }
                else if (currentStack.getItemType() == ItemType.WOOD && currentData.getBaseTileType() == Tile.GRASS) {
                    if (isPlayerBlocking(col, row, mainPlayer, worldMap)) {
                        return HudConfig.TEXT_PLAYER_BLOCKING;
                    }
                }
            }
            return HudConfig.WRONG_TOOL_TEXT;
        }

        if (result != null) {
            mainPlayer.setState(result.playerState());
            playerView.setState(mainPlayer.getState(), mainPlayer.getDirection());

            TimedTileAction action = new TimedTileAction(
                    col, row,
                    result.newTileData(),
                    getDelayInFrames(result.totalDurationMs()),
                    result.consumeItem(),
                    mainPlayer.getSelectedHotbarSlot()
            );

            if (result.harvestedItem() != null) {
                action.setHarvestedItem(result.harvestedItem());
                action.setHarvestedAmount(result.harvestedAmount());
            }

            actionManager.addPendingAction(action);
            return null;
        }

        return HudConfig.WRONG_TOOL_TEXT;
    }
}