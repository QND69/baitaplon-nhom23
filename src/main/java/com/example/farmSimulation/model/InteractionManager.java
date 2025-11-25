package com.example.farmSimulation.model;

import com.example.farmSimulation.config.AnimalConfig;
import com.example.farmSimulation.config.CropConfig;
import com.example.farmSimulation.config.GameLogicConfig;
import com.example.farmSimulation.config.HudConfig;
import com.example.farmSimulation.config.ItemSpriteConfig; // [MỚI]
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
    private AnimalManager animalManager; // Quản lý động vật
    private CollisionManager collisionManager; // Quản lý va chạm
    private WorldMap worldMap; // Bản đồ thế giới (để đặt item xuống đất)

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
    
    /**
     * Set AnimalManager (được gọi từ GameManager)
     */
    public void setAnimalManager(AnimalManager animalManager) {
        this.animalManager = animalManager;
    }
    
    /**
     * Set CollisionManager (được gọi từ GameManager)
     */
    public void setCollisionManager(CollisionManager collisionManager) {
        this.collisionManager = collisionManager;
    }
    
    /**
     * Set WorldMap (được gọi từ GameManager)
     */
    public void setWorldMap(WorldMap worldMap) {
        this.worldMap = worldMap;
    }

    /**
     * Kiểm tra xem có thể thêm item vào inventory không
     */
    private boolean canAddItem(Player player, ItemType type, int amount) {
        ItemStack[] hotbarItems = player.getHotbarItems();
        int maxStackSize = type.getMaxStackSize();
        
        // Kiểm tra stack vào ô có sẵn
        for (ItemStack stack : hotbarItems) {
            if (stack != null && stack.getItemType() == type) {
                if (stack.getQuantity() + amount <= maxStackSize) {
                    return true; // Có thể stack thêm
                }
                amount -= (maxStackSize - stack.getQuantity());
                if (amount <= 0) return true;
            }
        }
        
        // Kiểm tra ô trống
        for (ItemStack stack : hotbarItems) {
            if (stack == null) {
                return true; // Có ô trống
            }
        }
        
        return false; // Inventory đầy
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
        // Không xóa groundItem (giữ lại item trên đất)
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
        if (isFenceSolid(col - 1, row, worldMap)) {
            if (aabbIntersect(pMinX, pMaxX, pMinY, pMaxY, tileWorldX, fMinX, fMinY, fMaxY)) return true;
            double neighborPostRightX = (tileWorldX - (WorldConfig.TILE_SIZE / 2.0)) + fHalfW;
            if (aabbIntersect(pMinX, pMaxX, pMinY, pMaxY, neighborPostRightX, tileWorldX, fMinY, fMaxY)) return true;
        }
        if (isFenceSolid(col + 1, row, worldMap)) {
            if (aabbIntersect(pMinX, pMaxX, pMinY, pMaxY, fMaxX, tileWorldX + WorldConfig.TILE_SIZE, fMinY, fMaxY)) return true;
            double neighborPostLeftX = (tileWorldX + WorldConfig.TILE_SIZE + (WorldConfig.TILE_SIZE / 2.0)) - fHalfW;
            if (aabbIntersect(pMinX, pMaxX, pMinY, pMaxY, tileWorldX + WorldConfig.TILE_SIZE, neighborPostLeftX, fMinY, fMaxY)) return true;
        }
        if (isFenceSolid(col, row - 1, worldMap)) {
            double neighborPostBottomY = fCenterY - WorldConfig.TILE_SIZE; 
            if (aabbIntersect(pMinX, pMaxX, pMinY, pMaxY, fMinX, fMaxX, tileWorldY, fMinY)) return true;
            if (aabbIntersect(pMinX, pMaxX, pMinY, pMaxY, fMinX, fMaxX, neighborPostBottomY + fHalfH, tileWorldY)) return true;
        }
        if (isFenceSolid(col, row + 1, worldMap)) {
            double neighborPostTopY = fCenterY + WorldConfig.TILE_SIZE; 
            if (aabbIntersect(pMinX, pMaxX, pMinY, pMaxY, fMinX, fMaxX, fMaxY, tileWorldY + WorldConfig.TILE_SIZE)) return true;
            if (aabbIntersect(pMinX, pMaxX, pMinY, pMaxY, fMinX, fMaxX, tileWorldY + WorldConfig.TILE_SIZE, neighborPostTopY - fHalfH)) return true;
        }

        return false;
    }

    // Helper kiểm tra xem động vật có đang đứng ở vị trí đặt rào không
    private boolean isAnimalBlocking(int col, int row) {
        if (animalManager == null) return false;

        // [SỬA LỖI] Dùng hitbox CẢ Ô ĐẤT (64x64) để kiểm tra
        // Thay vì chỉ kiểm tra va chạm với cái cọc rào nhỏ xíu, ta kiểm tra với toàn bộ ô đất.
        // Điều này đảm bảo dù con vật chỉ mới bước vào mép ô, hoặc quả trứng nằm lệch, 
        // thì vẫn sẽ bị chặn không cho đặt rào.
        double tileMinX = col * WorldConfig.TILE_SIZE;
        double tileMaxX = tileMinX + WorldConfig.TILE_SIZE;
        
        double tileMinY = row * WorldConfig.TILE_SIZE;
        double tileMaxY = tileMinY + WorldConfig.TILE_SIZE;

        // Duyệt qua tất cả động vật
        for (Animal animal : animalManager.getAnimals()) {
            if (animal.isDead()) continue;

            // Tính hitbox động vật
            double aX = animal.getX();
            double aY = animal.getY();
            double aHalfW = animal.getType().getHitboxWidth() / 2.0;
            double aHalfH = animal.getType().getHitboxHeight() / 2.0;

            double aMinX = aX - aHalfW;
            double aMaxX = aX + aHalfW;
            double aMinY = aY - aHalfH;
            double aMaxY = aY + aHalfH;

            // Kiểm tra va chạm AABB giữa Ô ĐẤT và ĐỘNG VẬT
            if (aabbIntersect(tileMinX, tileMaxX, tileMinY, tileMaxY, aMinX, aMaxX, aMinY, aMaxY)) {
                return true;
            }
        }
        return false;
    }

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
                
                // Kiểm tra xem người chơi có chặn không
                if (isPlayerBlocking(col, row, mainPlayer, worldMap)) {
                    return null;
                }
                
                // [MỚI] Kiểm tra xem động vật có chặn không (dùng hàm đã sửa)
                if (isAnimalBlocking(col, row)) {
                    return null; // Trả về lỗi để HUD hiển thị
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

        // Kiểm tra item trên đất (ưu tiên nhặt item trước)
        if (currentData.getGroundItem() != null && currentData.getGroundItemAmount() > 0) {
            ItemType groundItemType = currentData.getGroundItem();
            int groundItemAmount = currentData.getGroundItemAmount();
            
            // Thử thêm vào inventory
            boolean success = mainPlayer.addItem(groundItemType, groundItemAmount);
            
            if (success) {
                // Xóa item trên đất
                TileData newData = new TileData(currentData);
                newData.setGroundItem(null);
                newData.setGroundItemAmount(0);
                // Reset offset về mặc định (tùy chọn, nhưng tốt cho lần sau)
                newData.setDefaultItemOffset(); 
                
                // Tạo action để cập nhật tile và hiển thị animation
                TimedTileAction action = new TimedTileAction(
                    col, row,
                    newData,
                    getDelayInFrames(GameLogicConfig.GENERIC_ACTION_DURATION_MS),
                    false,
                    mainPlayer.getSelectedHotbarSlot()
                );
                action.setHarvestedItem(groundItemType);
                action.setHarvestedAmount(groundItemAmount);
                
                mainPlayer.setState(PlayerView.PlayerState.BUSY);
                playerView.setState(mainPlayer.getState(), mainPlayer.getDirection());
                actionManager.addPendingAction(action);
                
                return null; // Thành công
            } else {
                // Inventory đầy
                return HudConfig.TEXT_INVENTORY_FULL;
            }
        }

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
                    // [MỚI] Kiểm tra động vật chặn và trả về thông báo lỗi
                    if (isAnimalBlocking(col, row)) {
                        return HudConfig.CANT_PLACE_TEXT;
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
    
    public String processAnimalInteraction(Player mainPlayer, PlayerView playerView, double worldX, double worldY) {
        if (animalManager == null) return null;
        
        // BƯỚC 0 (Ưu tiên Nhặt trứng): Kiểm tra trứng trước (cho phép cả tay không)
        Animal animalAtPosition = animalManager.getAnimalAt(worldX, worldY, WorldConfig.TILE_SIZE);
        if (animalAtPosition != null && animalAtPosition.getType() == AnimalType.EGG_ENTITY && !animalAtPosition.isDead()) {
            // Có trứng tại vị trí này, xử lý nhặt trứng
            ItemStack currentStack = mainPlayer.getCurrentItem();
            ItemType itemType = currentStack != null ? currentStack.getItemType() : null;
            
            // [FIX QUAN TRỌNG] Nếu đang cầm item động vật, trứng, hoặc gỗ thì KHÔNG nhặt trứng
            // Để cho logic đặt vật nuôi/rào bên dưới chạy
            // Nếu itemType == null (tay không) hoặc item khác -> Cho phép nhặt trứng
            if (itemType != ItemType.ITEM_COW && itemType != ItemType.ITEM_CHICKEN &&
                itemType != ItemType.ITEM_PIG && itemType != ItemType.ITEM_SHEEP &&
                itemType != ItemType.EGG && itemType != ItemType.WOOD) {
                // Tay không (itemType == null) hoặc item khác -> Cho phép nhặt trứng
                // Kiểm tra inventory có thể add item không
                if (!canAddItem(mainPlayer, ItemType.EGG, 1)) {
                    return HudConfig.TEXT_INVENTORY_FULL;
                }

                // Tính col/row từ tọa độ trứng để hiển thị animation
                int col = (int) Math.floor(animalAtPosition.getX() / WorldConfig.TILE_SIZE);
                int row = (int) Math.floor(animalAtPosition.getY() / WorldConfig.TILE_SIZE);

                // Lưu tọa độ động vật để xóa sau khi action hoàn thành (KHÔNG xóa ngay)
                double animalWorldX = animalAtPosition.getX();
                double animalWorldY = animalAtPosition.getY();
                
                // Set player state thành BUSY
                mainPlayer.setState(PlayerView.PlayerState.BUSY);
                playerView.setState(mainPlayer.getState(), mainPlayer.getDirection());
                
                // Tạo action để xóa động vật, thêm item và hiển thị animation
                TimedTileAction action = new TimedTileAction(
                    col, row,
                    null, // Không thay đổi tile
                    getDelayInFrames(GameLogicConfig.GENERIC_ACTION_DURATION_MS),
                    false, // Không tiêu thụ item
                    mainPlayer.getSelectedHotbarSlot()
                );
                action.setHarvestedItem(ItemType.EGG);
                action.setHarvestedAmount(1);
                action.setAnimalWorldX(animalWorldX);
                action.setAnimalWorldY(animalWorldY);
                actionManager.addPendingAction(action);
                
                return null; // Thành công
            }
        }
        
        // BƯỚC 1 (Ưu tiên Đặt): Kiểm tra Item trên tay người chơi trước
        ItemStack currentStack = mainPlayer.getCurrentItem();
        if (currentStack == null) {
            // Không có item trên tay, không thể đặt hoặc tương tác (trừ nhặt trứng đã xử lý ở trên)
            return null;
        }
        
        ItemType itemType = currentStack.getItemType();
        
        // Kiểm tra xem có phải hành động đặt không
        AnimalType animalTypeToPlace = null;
        if (itemType == ItemType.ITEM_COW) {
            animalTypeToPlace = AnimalType.COW;
        } else if (itemType == ItemType.ITEM_CHICKEN) {
            animalTypeToPlace = AnimalType.CHICKEN;
        } else if (itemType == ItemType.ITEM_PIG) {
            animalTypeToPlace = AnimalType.PIG;
        } else if (itemType == ItemType.ITEM_SHEEP) {
            animalTypeToPlace = AnimalType.SHEEP;
        } else if (itemType == ItemType.EGG) {
            animalTypeToPlace = AnimalType.EGG_ENTITY;
        }
        
        // Nếu là hành động đặt vật nuôi
        if (animalTypeToPlace != null) {
            // Kiểm tra va chạm tại vị trí đặt
            if (collisionManager == null || worldMap == null) {
                return null; // Không có CollisionManager, không thể kiểm tra
            }
            
            // [SỬA] Kiểm tra logic Tile: Không được đặt lên ô đang là Cây hoặc Rào
            int tileCol = (int) Math.floor(worldX / WorldConfig.TILE_SIZE);
            int tileRow = (int) Math.floor(worldY / WorldConfig.TILE_SIZE);
            TileData tileData = worldMap.getTileData(tileCol, tileRow);
            
            // 1. Kiểm tra Cây (Tree)
            if (tileData.getBaseTileType() == Tile.TREE && tileData.getTreeData() != null) {
                return HudConfig.CANT_PLACE_TEXT;
            }
            
            // 2. Kiểm tra Hàng rào (Fence)
            if (tileData.getBaseTileType() == Tile.FENCE && 
                tileData.getFenceData() != null && 
                tileData.getFenceData().isSolid()) {
                return HudConfig.CANT_PLACE_TEXT;
            }
            
            // [SỬA] Kiểm tra Collision vật lý (Connections/Rails của rào và Hitbox cây)
            // Sử dụng kích thước thật của động vật để đảm bảo không bị kẹt vào rào/cây
            double checkWidth = animalTypeToPlace.getHitboxWidth();
            double checkHeight = animalTypeToPlace.getHitboxHeight();
            
            // checkCollision trong CollisionManager đã xử lý logic các thanh nối (Rails) của hàng rào
            if (collisionManager.checkCollision(worldX, worldY, checkWidth, checkHeight)) {
                return HudConfig.CANT_PLACE_TEXT; // Vị trí bị chặn bởi Rào/Cây/Nước
            }
            
            // [ĐÃ SỬA LẠI LOGIC] Kiểm tra va chạm Hitbox với các động vật khác
            // Thay vì kiểm tra theo Tile (getAnimalAt), ta kiểm tra giao nhau giữa các hình chữ nhật (AABB)
            // Để cho phép đặt nhiều con trên cùng 1 tile miễn là không đè lên nhau
            double newMinX = worldX - checkWidth / 2.0;
            double newMaxX = worldX + checkWidth / 2.0;
            double newMinY = worldY - checkHeight / 2.0;
            double newMaxY = worldY + checkHeight / 2.0;
            
            for (Animal existing : animalManager.getAnimals()) {
                if (existing.isDead()) continue;
                
                double exW = existing.getType().getHitboxWidth();
                double exH = existing.getType().getHitboxHeight();
                double exMinX = existing.getX() - exW / 2.0;
                double exMaxX = existing.getX() + exW / 2.0;
                double exMinY = existing.getY() - exH / 2.0;
                double exMaxY = existing.getY() + exH / 2.0;
                
                if (aabbIntersect(newMinX, newMaxX, newMinY, newMaxY, exMinX, exMaxX, exMinY, exMaxY)) {
                    // [PHƯƠNG ÁN 3]: Cho phép đặt đè lên nhau, NHƯNG KHÔNG ĐƯỢC ĐÈ LÊN TRỨNG
                    // Nếu vật đang nằm đó là TRỨNG -> Chặn (để không bị che mất trứng)
                    if (existing.getType() == AnimalType.EGG_ENTITY) {
                        return HudConfig.CANT_PLACE_TEXT; 
                    }
                    
                    // Nếu vật đang đặt là TRỨNG, cũng không nên cho đặt đè lên con khác (tùy chọn, ở đây giữ logic đơn giản là cấm đặt đè lên trứng thôi)
                    // Nếu muốn chặt chẽ: if (animalTypeToPlace == AnimalType.EGG_ENTITY) return HudConfig.CANT_PLACE_TEXT;
                    
                    // Nếu không phải trứng, cho phép đặt chồng lên (Soft Collision) -> Không return lỗi
                }
            }
            
            // Vị trí hợp lệ -> Tạo Animal mới, thêm vào AnimalManager
            Animal newAnimal = new Animal(animalTypeToPlace, worldX, worldY);
            animalManager.addAnimal(newAnimal);
            
            // Trừ item
            currentStack.remove(1);
            if (currentStack.getQuantity() <= 0) {
                mainPlayer.getHotbarItems()[mainPlayer.getSelectedHotbarSlot()] = null;
            }
            
            // Set player state thành BUSY
            mainPlayer.setState(PlayerView.PlayerState.BUSY);
            playerView.setState(mainPlayer.getState(), mainPlayer.getDirection());
            
            // Thêm hành động chờ
            TimedTileAction action = new TimedTileAction(
                -1, -1,
                null,
                getDelayInFrames(GameLogicConfig.PLANT_DURATION_MS),
                false,
                mainPlayer.getSelectedHotbarSlot()
            );
            actionManager.addPendingAction(action);
            
            return null; // Thành công
        }
        
        // Bước 3 (Tương tác): Nếu không phải hành động đặt, tìm động vật để tương tác
        Animal animal = animalManager.getAnimalAt(worldX, worldY, WorldConfig.TILE_SIZE);
        if (animal == null || animal.isDead()) {
            return null; // Không tìm thấy động vật
        }
        
        // --- CHO ĂN ---
        if (animal.getType().acceptsFood(itemType)) {
            animalManager.feedAnimal(animal);
            
            // [SỬA] Thay decreaseQuantity thành remove
            currentStack.remove(1);
            if (currentStack.getQuantity() <= 0) {
                // [SỬA] Gán null trực tiếp
                mainPlayer.getHotbarItems()[mainPlayer.getSelectedHotbarSlot()] = null;
            }
            
            mainPlayer.setState(PlayerView.PlayerState.BUSY);
            playerView.setState(mainPlayer.getState(), mainPlayer.getDirection());
            
            TimedTileAction action = new TimedTileAction(
                -1, -1,
                null,
                getDelayInFrames(GameLogicConfig.PLANT_DURATION_MS),
                false,
                mainPlayer.getSelectedHotbarSlot()
            );
            actionManager.addPendingAction(action);
            
            return null;
        }
        
        // --- THU HOẠCH SẢN PHẨM ---
        // Bò: MILK_BUCKET
        if (itemType == ItemType.MILK_BUCKET && animal.getType() == AnimalType.COW && animal.isHasProduct()) {
            animalManager.harvestProduct(animal);
            
            // [SỬA] Thay decreaseQuantity thành remove
            currentStack.remove(1);
            if (currentStack.getQuantity() <= 0) {
                mainPlayer.getHotbarItems()[mainPlayer.getSelectedHotbarSlot()] = null;
            }
            
            // [SỬA] Gọi addItem đúng cú pháp
            if (!mainPlayer.addItem(ItemType.FULL_MILK_BUCKET, 1)) {
                mainPlayer.addItem(ItemType.MILK, 1);
            }
            
            mainPlayer.setState(PlayerView.PlayerState.BUSY);
            playerView.setState(mainPlayer.getState(), mainPlayer.getDirection());
            
            TimedTileAction action = new TimedTileAction(
                -1, -1,
                null,
                getDelayInFrames(GameLogicConfig.PLANT_DURATION_MS),
                false,
                mainPlayer.getSelectedHotbarSlot()
            );
            actionManager.addPendingAction(action);
            
            return null;
        }
        
        // Cừu: SHEARS
        if (itemType == ItemType.SHEARS && animal.getType() == AnimalType.SHEEP && animal.isHasProduct()) {
            if (currentStack.getCurrentDurability() <= 0) {
                return HudConfig.TEXT_WATER_EMPTY;
            }
            
            animalManager.harvestProduct(animal);
            
            currentStack.decreaseDurability(1);
            if (currentStack.getCurrentDurability() <= 0) {
                mainPlayer.getHotbarItems()[mainPlayer.getSelectedHotbarSlot()] = null;
            }
            
            // [SỬA] Gọi addItem đúng cú pháp
            mainPlayer.addItem(ItemType.WOOL, 1);
            
            mainPlayer.setState(PlayerView.PlayerState.BUSY);
            playerView.setState(mainPlayer.getState(), mainPlayer.getDirection());
            
            TimedTileAction action = new TimedTileAction(
                -1, -1,
                null,
                getDelayInFrames(GameLogicConfig.AXE_DURATION_PER_REPETITION_MS),
                false,
                mainPlayer.getSelectedHotbarSlot()
            );
            actionManager.addPendingAction(action);
            
            return null;
        }
        
        // Gà: Nhặt trứng
        if (animal.getType() == AnimalType.EGG_ENTITY) {
            // [FIX QUAN TRỌNG] Nếu đang cầm item động vật, trứng, hoặc gỗ thì KHÔNG nhặt trứng
            // Để cho logic đặt vật nuôi/rào bên dưới chạy
            if (itemType == ItemType.ITEM_COW || itemType == ItemType.ITEM_CHICKEN ||
                itemType == ItemType.ITEM_PIG || itemType == ItemType.ITEM_SHEEP ||
                itemType == ItemType.EGG || itemType == ItemType.WOOD) {
                return null;
            }

            // Kiểm tra inventory có thể add item không
            if (!canAddItem(mainPlayer, ItemType.EGG, 1)) {
                return HudConfig.TEXT_INVENTORY_FULL;
            }

            // Tính col/row từ tọa độ trứng để hiển thị animation
            int col = (int) Math.floor(animal.getX() / WorldConfig.TILE_SIZE);
            int row = (int) Math.floor(animal.getY() / WorldConfig.TILE_SIZE);

            // Lưu tọa độ động vật để xóa sau khi action hoàn thành (KHÔNG xóa ngay)
            double animalWorldX = animal.getX();
            double animalWorldY = animal.getY();
            
            // Set player state thành BUSY
            mainPlayer.setState(PlayerView.PlayerState.BUSY);
            playerView.setState(mainPlayer.getState(), mainPlayer.getDirection());
            
            // Tạo action để xóa động vật, thêm item và hiển thị animation
            TimedTileAction action = new TimedTileAction(
                col, row,
                null, // Không thay đổi tile
                getDelayInFrames(GameLogicConfig.GENERIC_ACTION_DURATION_MS),
                false, // Không tiêu thụ item
                mainPlayer.getSelectedHotbarSlot()
            );
            action.setHarvestedItem(ItemType.EGG);
            action.setHarvestedAmount(1);
            action.setAnimalWorldX(animalWorldX);
            action.setAnimalWorldY(animalWorldY);
            actionManager.addPendingAction(action);
            
            return null;
        }
        
        // --- TẤN CÔNG (GIẾT) ---
        if (itemType == ItemType.AXE || itemType == ItemType.SWORD || 
            (itemType.hasDurability() && itemType != ItemType.SHEARS && itemType != ItemType.MILK_BUCKET)) {
            // Lưu tọa độ động vật trước khi giết (để đặt thịt đúng vị trí)
            double animalX = animal.getX();
            double animalY = animal.getY();
            
            // Giết động vật ngay (set isDead = true)
            int meatAmount = animalManager.killAnimal(animal);
            
            if (meatAmount > 0 && worldMap != null) {
                // Lấy loại thịt tương ứng với động vật
                ItemType meatType = animal.getMeatType();
                if (meatType != null) {
                    // [FIX LỆCH TỌA ĐỘ THỊT]
                    // Thay vì lấy tọa độ chân (Y), ta lấy tọa độ TÂM của hình ảnh động vật
                    // Động vật vẽ từ chân lên trên, nên tâm Y = chân - (chiều cao / 2)
                    double visualCenterY = animalY - (animal.getType().getSpriteSize() / 2.0);
                    
                    // Tính toán ô chứa (Tile)
                    int tileCol = (int) Math.floor(animalX / WorldConfig.TILE_SIZE);
                    int tileRow = (int) Math.floor(visualCenterY / WorldConfig.TILE_SIZE);
                    
                    // Tính toán OFFSET (độ lệch so với góc trên trái của ô)
                    // Để item nằm chính xác tại animalX, visualCenterY
                    // Trừ đi một nửa kích thước item (32/2 = 16) để item nằm giữa điểm đó
                    double targetItemX = animalX - (ItemSpriteConfig.ITEM_SPRITE_WIDTH / 2.0);
                    double targetItemY = visualCenterY - (ItemSpriteConfig.ITEM_SPRITE_HEIGHT / 2.0);
                    
                    double offsetX = targetItemX - (tileCol * WorldConfig.TILE_SIZE);
                    double offsetY = targetItemY - (tileRow * WorldConfig.TILE_SIZE);
                    
                    TileData tileData = worldMap.getTileData(tileCol, tileRow);
                    tileData.setGroundItem(meatType);
                    tileData.setGroundItemAmount(meatAmount);
                    // [MỚI] Set offset
                    tileData.setGroundItemOffsetX(offsetX);
                    tileData.setGroundItemOffsetY(offsetY);
                    
                    worldMap.setTileData(tileCol, tileRow, tileData);
                }
            }
            
            // Sử dụng animation ATTACK từ player sheet thay vì AXE
            mainPlayer.setState(PlayerView.PlayerState.ATTACK);
            playerView.setState(mainPlayer.getState(), mainPlayer.getDirection());
            
            // Tính thời gian dựa trên ATTACK animation (ATTACK_SPEED * ATTACK_FRAMES)
            long attackDuration = PlayerSpriteConfig.ATTACK_SPEED * PlayerSpriteConfig.ATTACK_FRAMES;
            TimedTileAction action = new TimedTileAction(
                -1, -1,
                null,
                getDelayInFrames(attackDuration),
                false,
                mainPlayer.getSelectedHotbarSlot()
            );
            actionManager.addPendingAction(action);
            
            return null;
        }
        
        return null; 
    }
}