package com.example.farmSimulation.model;

import com.example.farmSimulation.config.CropConfig;
import com.example.farmSimulation.config.GameLogicConfig;
import com.example.farmSimulation.config.HudConfig;
import com.example.farmSimulation.view.PlayerView;

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
            ItemType harvestedItem               // Item thu hoạch được (để làm hiệu ứng)
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
    private InteractionResult calculateInteractionResult(ItemStack currentStack, TileData currentData) {
        if (currentStack == null) return null; // Tay không (nếu không phải mode Hand)

        ItemType itemType = currentStack.getItemType();
        Tile baseTile = currentData.getBaseTileType();

        // CUỐC (HOE): Cỏ -> Đất
        if (itemType == ItemType.HOE && baseTile == Tile.GRASS) {
            TileData newData = new TileData(currentData); // Copy
            newData.setBaseTileType(Tile.SOIL); // Dự kiến biến thành SOIL

            long duration = (long) GameLogicConfig.HOE_REPETITIONS * GameLogicConfig.HOE_DURATION_PER_REPETITION_MS;
            return new InteractionResult(newData, PlayerView.PlayerState.HOE, duration, true, null);
        }

        // HẠT GIỐNG: Gieo lên đất
        if (itemType.name().startsWith("SEEDS_")) {
            // Chỉ gieo được nếu chưa có cây
            if ((baseTile == Tile.SOIL || baseTile == Tile.SOIL_WET) && currentData.getCropData() == null) {
                try {
                    // Lấy tên cây từ tên hạt giống (VD: SEEDS_STRAWBERRY -> STRAWBERRY)
                    CropType type = CropType.valueOf(itemType.name().substring(6));

                    TileData newData = new TileData(currentData);
                    newData.setCropData(new CropData(type, 0, System.nanoTime()));

                    if (baseTile == Tile.SOIL) { // Nếu trồng trên đất khô, phải reset bộ đếm khô về hiện tại
                        newData.setDryStartTime(System.nanoTime());
                    } else { // Nếu đất ướt, không có thời gian khô
                        newData.setDryStartTime(0);
                    }

                    // [SỬA] Dùng PlayerState.PLANT và duration mới
                    return new InteractionResult(newData, PlayerView.PlayerState.PLANT, GameLogicConfig.PLANT_DURATION_MS, true, null); // Consume = true
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        // TƯỚI NƯỚC: Chỉ tưới đất KHÔ có CÂY
        if (itemType == ItemType.WATERING_CAN) {
            // [LOGIC NƯỚC] Kiểm tra độ bền (lượng nước)
            if (currentStack.getCurrentDurability() <= 0) {
                return null; // Hết nước -> Không tưới được (MainGameView sẽ hiện thông báo sau)
            }

            // Logic Múc nước (Refill) khi click vào nước
            if (baseTile == Tile.WATER) {
                // Refill không đổi tile, chỉ đổi item
                // Trả về null ở đây vì ta xử lý logic item riêng?
                // Không, ta có thể trả về InteractionResult với newTileData = null
                currentStack.refillDurability(); // Refill ngay lập tức hoặc qua ActionManager?
                // Để đơn giản, refill ngay ở đây cũng được vì không ảnh hưởng tile map
                // Hoặc tạo animation múc nước. Tạm thời refill luôn.
                System.out.println("Refilled Water!");
                return new InteractionResult(null, PlayerView.PlayerState.WATER, 500, false, null); // Animation múc, không tốn độ bền
            }

            // Logic Tưới cây
            // Chỉ tưới được đất khô (SOIL) và phải có cây
            // "Người chơi chỉ có thể tưới lên đất khô có cây"
            if (baseTile == Tile.SOIL && currentData.getCropData() != null) {
                TileData newData = new TileData(currentData);
                newData.setBaseTileType(Tile.SOIL_WET);
                newData.setWatered(true);
                newData.setLastWateredTime(System.nanoTime());
                newData.setDryStartTime(0); // Xóa thời gian khô

                long duration = (long) GameLogicConfig.WATERING_CAN_REPETITIONS * GameLogicConfig.WATERING_CAN_DURATION_PER_REPETITION_MS;
                return new InteractionResult(newData, PlayerView.PlayerState.WATER, duration, true, null);
            }
        }

        // BÓN PHÂN: Bón bất cứ lúc nào sau khi nảy mầm (stage > 0)
        if (itemType == ItemType.FERTILIZER) {
            if (currentData.getCropData() != null && !currentData.isFertilized()) {
                int currentStage = currentData.getCropData().getGrowthStage();
                int maxStage = currentData.getCropData().getType().getMaxStages();

                // Kiểm tra stage >= min stage VÀ chưa chín ( < max - 1 )
                // Nếu cây đã chín (READY_TO_HARVEST) thì không cho bón nữa
                if (currentStage >= CropConfig.MIN_GROWTH_STAGE_FOR_FERTILIZER && currentStage < maxStage - 1) {
                    TileData newData = new TileData(currentData);
                    newData.setFertilized(true);
                    newData.setFertilizerStartTime(System.nanoTime());
                    // [SỬA] Dùng PlayerState.FERTILIZE và duration mới
                    return new InteractionResult(newData, PlayerView.PlayerState.FERTILIZE, GameLogicConfig.FERTILIZER_DURATION_MS, true, null);
                } else {
                    // Cây quá nhỏ hoặc đã chín -> Return null để báo lỗi
                    return null;
                }
            }
        }

        // XẺNG: Hủy cây
        if (itemType == ItemType.SHOVEL) {
            if (currentData.getCropData() != null) {
                // Reset hoàn toàn về đất khô
                TileData newData = createResetTileData(currentData);

                long duration = (long) GameLogicConfig.SHOVEL_REPETITIONS * GameLogicConfig.SHOVEL_DURATION_PER_REPETITION_MS;
                // [SỬA] Dùng PlayerState.SHOVEL
                return new InteractionResult(newData, PlayerView.PlayerState.SHOVEL, duration, true, null);
            }
        }

        return null; // Không có hành động hợp lệ
    }

    // Tách riêng logic thu hoạch vì nó không cần item trên tay
    private InteractionResult checkHarvest(TileData currentData, Player mainPlayer) {
        CropData crop = currentData.getCropData();
        Random random = new Random();
        if (crop != null && crop.getGrowthStage() >= crop.getType().getMaxStages() - 1) {
            int yield = random.nextInt(crop.getType().getMaxYield() - crop.getType().getMinYield() + 1) + crop.getType().getMinYield();

            // Thêm item và kiểm tra full
            boolean success = mainPlayer.addItem(crop.getType().getHarvestItem(), yield);

            if (!success) {
                // Full inventory -> Không cho thu hoạch
                // Chúng ta có thể trả về null để kích hoạt thông báo lỗi ở bên ngoài
                // Nhưng thông báo lỗi bên ngoài là "Wrong Tool".
                // Ta cần cơ chế báo lỗi riêng.
                return null;
            }

            TileData newData = createResetTileData(currentData);
            // Trả về item thu hoạch được để làm animation
            return new InteractionResult(newData, PlayerView.PlayerState.BUSY, GameLogicConfig.GENERIC_ACTION_DURATION_MS, false, crop.getType().getHarvestItem());
        }
        return null;
    }

    /**
     * Hàm processInteraction xử lý logic trồng trọt
     */
    public String processInteraction(Player mainPlayer, PlayerView playerView, WorldMap worldMap, int col, int row) {
        ItemStack currentStack = mainPlayer.getCurrentItem();
        TileData currentData = worldMap.getTileData(col, row);

        InteractionResult result = null;

        // --- ƯU TIÊN 1: THU HOẠCH (HARVEST) ---
        // Kiểm tra xem có cây chín không TRƯỚC KHI làm bất cứ điều gì khác
        CropData crop = currentData.getCropData();
        if (crop != null && crop.getGrowthStage() >= crop.getType().getMaxStages() - 1) {
            // Có cây chín -> Cố gắng thu hoạch
            result = checkHarvest(currentData, mainPlayer);

            if (result == null) {
                // Nếu có cây chín mà checkHarvest trả về null -> Có nghĩa là Túi đầy
                // Trả về lỗi ngay lập tức, KHÔNG thực hiện hành động của Tool (để tránh lỡ tay cuốc đất)
                return HudConfig.TEXT_INVENTORY_FULL;
            }
            // Nếu result != null -> Thu hoạch thành công, code sẽ chạy xuống phần xử lý result bên dưới
        }

        // --- ƯU TIÊN 2: SỬ DỤNG ITEM (USE ITEM) ---
        // Chỉ chạy vào đây nếu KHÔNG PHẢI trường hợp thu hoạch (hoặc ô đất không có cây chín)
        else {
            // Gọi logic tính toán dùng item cũ
            result = calculateInteractionResult(currentStack, currentData);
        }

        // --- XỬ LÝ LỖI NẾU KHÔNG CÓ HÀNH ĐỘNG NÀO THÀNH CÔNG ---
        if (result == null) {
            if (currentStack != null) {
                // Case: Hết nước
                if (currentStack.getItemType() == ItemType.WATERING_CAN && currentStack.getCurrentDurability() <= 0) {
                    return HudConfig.TEXT_WATER_EMPTY;
                }
                // Case: Lỗi bón phân
                else if (currentStack.getItemType() == ItemType.FERTILIZER && currentData.getCropData() != null) {
                    if (currentData.getCropData().getGrowthStage() < CropConfig.MIN_GROWTH_STAGE_FOR_FERTILIZER) {
                        return HudConfig.TEXT_PLANT_CAN_NOT_BE_FERTILIZED;
                    }
                }
            }
            // Nếu không rơi vào các case trên -> Báo sai công cụ
            return HudConfig.WRONG_TOOL_TEXT;
        }

        // --- THỰC THI KẾT QUẢ (SUCCESS) ---
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
            }

            actionManager.addPendingAction(action);
            return null; // Thành công
        }

        return HudConfig.WRONG_TOOL_TEXT;
    }
}