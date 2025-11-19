package com.example.farmSimulation.model;

import com.example.farmSimulation.config.CropConfig;

// Class quản lý hệ thống ngầm của cây
public class CropManager {
    private final WorldMap worldMap;

    // [MỚI] Biến để theo dõi thời gian frame trước
    private long lastUpdateTime = 0;

    public CropManager(WorldMap worldMap) {
        this.worldMap = worldMap;
    }

    /**
     * Hàm Reset Tile về trạng thái Soil khô mặc định (Helper)
     * Dùng khi cây chết để reset mọi thứ
     */
    private void resetTileToSoil(TileData data, long currentTime) {
        data.setBaseTileType(Tile.SOIL);
        data.setCropData(null);
        data.setWatered(false);
        data.setFertilized(false);
        data.setDryStartTime(currentTime); // Reset timer để bắt đầu đếm lùi mọc cỏ
        data.setFertilizerStartTime(0);
        data.setStatusIndicator(CropStatusIndicator.NONE);
    }

    public boolean updateCrops(long currentTime) {
        boolean mapNeedsRedraw = false;
        long currentTimeMs = currentTime / 1_000_000;

        // Tính delta time (thời gian trôi qua giữa 2 frame)
        if (lastUpdateTime == 0) {
            lastUpdateTime = currentTime;
            return false; // Frame đầu chưa làm gì
        }
        long deltaTime = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        for (TileData data : worldMap.getAllTileData()) {
            boolean changed = false;

            // --- LOGIC TƯỚI NƯỚC & ĐẤT ---
            if (data.isWatered()) {
                // Đất ướt -> Tự khô
                if (currentTimeMs - data.getLastWateredTime() / 1_000_000 > CropConfig.SOIL_DRY_TIME_MS) {
                    data.setWatered(false);
                    data.setBaseTileType(Tile.SOIL);
                    data.setDryStartTime(currentTime); // Bắt đầu đếm giờ khô
                    changed = true;
                }
            } else if (data.getBaseTileType() == Tile.SOIL) { // Đất khô
                if (data.getCropData() == null) { // Đất hoang -> Mọc cỏ
                    // Kiểm tra nếu dryStartTime chưa có (bằng 0) thì set ngay
                    if (data.getDryStartTime() == 0) {
                        data.setDryStartTime(currentTime);
                    } else {
                        // Nếu đã khô đủ lâu -> Mọc cỏ
                        if ((currentTime - data.getDryStartTime()) / 1_000_000 > CropConfig.SOIL_REVERT_TIME_MS) {
                            data.setBaseTileType(Tile.GRASS);
                            data.setDryStartTime(0);
                            changed = true;
                        }
                    }
                } else { // Có cây trên đất khô -> Kiểm tra chết
                    // Nếu đất khô mà DryTime = 0, reset ngay lập tức để tránh cây chết oan
                    if (data.getDryStartTime() == 0) {
                        data.setDryStartTime(currentTime);
                    }
                    long dryDuration = (currentTime - data.getDryStartTime()) / 1_000_000;
                    long deathTime = CropConfig.WATER_WARNING_DELAY_MS + CropConfig.CROP_DEATH_TIME_MS;

                    if (data.getCropData().getGrowthStage() != -1 && dryDuration > deathTime) {
                        // Cây chết -> Reset hoàn toàn tile
                        resetTileToSoil(data, currentTime);
                        changed = true;
                    }
                }
            }

            // --- LOGIC PHÂN BÓN ---
            if (data.isFertilized()) {
                // Phân bón tự hết lớp hiển thị (nhưng buff có thể vẫn còn hoặc hết tùy logic)
                if ((currentTime - data.getFertilizerStartTime()) / 1_000_000 > CropConfig.FERTILIZER_EFFECT_DURATION_MS) {
                    data.setFertilized(false); // Mất lớp hiển thị
                    changed = true;
                }
            }

            // --- LOGIC CÂY LỚN ---
            // Điều kiện lớn: Có cây, chưa chết, chưa lớn hết
            CropData crop = data.getCropData();
            if (crop != null && crop.getGrowthStage() != -1 && crop.getGrowthStage() < crop.getType().getMaxStages() - 1) {

                // Kiểm tra điều kiện nước để lớn:
                // Cây lớn bình thường khi đất ướt HOẶC đất khô nhưng chưa đến warning time
                boolean canGrowWater = data.isWatered();
                if (!data.isWatered() && data.getDryStartTime() > 0) {
                    long dryDuration = (currentTime - data.getDryStartTime()) / 1_000_000;
                    if (dryDuration <= CropConfig.WATER_WARNING_DELAY_MS) {
                        canGrowWater = true;
                    }
                }

                if (canGrowWater) {
                    // Kiểm tra buff phân bón
                    // Buff còn tác dụng nếu: Đang bón HOẶC đã hết phân nhưng chưa hết warning time
                    boolean hasBuff = false;
                    if (data.getFertilizerStartTime() > 0) {
                        long timeSinceFertilizer = (currentTime - data.getFertilizerStartTime()) / 1_000_000;
                        // Chỉ BUFF khi còn trong thời gian hiệu lực
                        if (timeSinceFertilizer <= (CropConfig.FERTILIZER_EFFECT_DURATION_MS + CropConfig.FERTILIZER_WARNING_DELAY_MS)) {
                            hasBuff = true;
                        }
                    }

                    double timePerStage = hasBuff ? (CropConfig.TIME_PER_GROWTH_STAGE_MS / CropConfig.FERTILIZER_BUFF) : CropConfig.TIME_PER_GROWTH_STAGE_MS;

                    long timeElapsedMs = (currentTime - crop.getPlantTime()) / 1_000_000;
                    int targetStage = (int) (timeElapsedMs / timePerStage);
                    targetStage = Math.min(targetStage, crop.getType().getMaxStages() - 1);

                    if (targetStage > crop.getGrowthStage()) {
                        crop.setGrowthStage(targetStage);
                        // Nếu cây đã lớn tối đa (Chín) -> Mất lớp phân bón ngay lập tức
                        if (targetStage >= crop.getType().getMaxStages() - 1) {
                            data.setFertilized(false);
                        }
                        changed = true;
                    }
                } else {
                    // ĐÓNG BĂNG THỜI GIAN
                    // Nếu thiếu nước và đã qua Warning Time -> Cây ngừng lớn
                    // Cách làm: Đẩy plantTime về phía trước 1 khoảng bằng deltaTime
                    // Điều này khiến (currentTime - plantTime) không thay đổi
                    long newPlantTime = crop.getPlantTime() + deltaTime;
                    crop.setPlantTime(newPlantTime);
                    // Không set changed = true vì stage không đổi
                }
            }

            // --- CẬP NHẬT ICON ---
            CropStatusIndicator newStatus = calculateStatus(data, crop, currentTime);
            if (data.getStatusIndicator() != newStatus) {
                data.setStatusIndicator(newStatus);
                changed = true;
            }

            if (changed) mapNeedsRedraw = true;
        }
        return mapNeedsRedraw;
    }

    private CropStatusIndicator calculateStatus(TileData data, CropData crop, long currentTime) {
        if (crop == null) return CropStatusIndicator.NONE;
        if (crop.getGrowthStage() == -1) return CropStatusIndicator.DEAD;
        if (crop.getGrowthStage() >= crop.getType().getMaxStages() - 1) return CropStatusIndicator.READY_TO_HARVEST;

        // Warning Nước: Đất khô > Warning Time
        boolean waterWarning = false;
        if (!data.isWatered() && data.getDryStartTime() > 0) {
            long dryDuration = (currentTime - data.getDryStartTime()) / 1_000_000;
            if (dryDuration > CropConfig.WATER_WARNING_DELAY_MS) {
                waterWarning = true;
            }
        }

        // Warning Phân bón:
        boolean fertilizerWarning = false;

        // Chỉ xét cảnh báo phân bón khi đủ tuổi và chưa chín
        if (crop.getGrowthStage() >= CropConfig.MIN_GROWTH_STAGE_FOR_FERTILIZER && crop.getGrowthStage() < crop.getType().getMaxStages() - 1) {
            if (data.isFertilized()) {
                // Đang có phân trên đất -> KHÔNG hiện cảnh báo
                fertilizerWarning = false;
            } else {
                // Đất không có phân (hoặc đã tan hết)
                // Kiểm tra xem đã hết tác dụng chưa
                if (data.getFertilizerStartTime() > 0) {
                    long timeSinceStart = (currentTime - data.getFertilizerStartTime()) / 1_000_000;
                    // Đã qua thời gian (Effect + Warning) -> Hết tác dụng -> Cần bón lại
                    if (timeSinceStart > (CropConfig.FERTILIZER_EFFECT_DURATION_MS + CropConfig.FERTILIZER_WARNING_DELAY_MS)) {
                        fertilizerWarning = true;
                    }
                } else {
                    // Chưa bón bao giờ -> Cần bón
                    fertilizerWarning = true;
                }
            }
        }

        if (waterWarning && fertilizerWarning) return CropStatusIndicator.NEED_WATER_AND_FERTILIZER;
        if (waterWarning) return CropStatusIndicator.NEEDS_WATER;
        if (fertilizerWarning) return CropStatusIndicator.NEEDS_FERTILIZER;

        return CropStatusIndicator.NONE;
    }
}