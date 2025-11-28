package com.example.farmSimulation.model;

import com.example.farmSimulation.config.CropConfig;
import com.example.farmSimulation.config.GameLogicConfig;

// Class quản lý hệ thống ngầm của cây
public class CropManager {
    private final WorldMap worldMap;
    private WeatherManager weatherManager; // Quản lý thời tiết
    private TimeManager timeManager; // Quản lý thời gian (để kiểm tra ban đêm)

    // Biến để theo dõi thời gian frame trước
    private long lastUpdateTime = 0;
    
    // Thời gian update lần cuối (để sử dụng interval)
    private long lastCropUpdateTimeMs = 0;
    
    // Index để track tiles đã update (để phân bổ update qua nhiều frame)
    private int lastProcessedIndex = 0;

    public CropManager(WorldMap worldMap) {
        this.worldMap = worldMap;
    }
    
    /**
     * Set WeatherManager (được gọi từ GameManager)
     */
    public void setWeatherManager(WeatherManager weatherManager) {
        this.weatherManager = weatherManager;
    }
    
    /**
     * Set TimeManager (được gọi từ GameManager)
     */
    public void setTimeManager(TimeManager timeManager) {
        this.timeManager = timeManager;
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
            lastCropUpdateTimeMs = currentTimeMs;
            return false; // Frame đầu chưa làm gì
        }
        long deltaTime = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        // Chỉ update crops theo interval, không phải mỗi frame
        if (currentTimeMs - lastCropUpdateTimeMs < GameLogicConfig.CROP_UPDATE_INTERVAL_MS) {
            return false; // Chưa đến lúc update
        }
        lastCropUpdateTimeMs = currentTimeMs;

        // Lấy tất cả tiles và chỉ xử lý những tiles cần thiết
        // Tối ưu: Chỉ update tiles có cây trồng, đất ướt, hoặc đất khô cần kiểm tra
        java.util.Collection<TileData> allTiles = worldMap.getAllTileData();
        java.util.ArrayList<TileData> tilesToUpdate = new java.util.ArrayList<>();
        
        // Lọc ra các tiles cần update (có cây, đất ướt, hoặc đất khô)
        for (TileData data : allTiles) {
            if (data.getCropData() != null || 
                data.isWatered() || 
                data.isFertilized() || 
                (data.getBaseTileType() == Tile.SOIL && data.getDryStartTime() > 0)) {
                tilesToUpdate.add(data);
            }
        }

        // Giới hạn số lượng tiles update mỗi lần để tránh lag spike
        int processedCount = 0;
        int startIndex = lastProcessedIndex;
        
        for (int i = 0; i < tilesToUpdate.size() && processedCount < GameLogicConfig.MAX_CROPS_UPDATE_PER_FRAME; i++) {
            int index = (startIndex + i) % tilesToUpdate.size();
            TileData data = tilesToUpdate.get(index);
            boolean changed = false;
            processedCount++;

            // --- LOGIC TƯỚI NƯỚC & ĐẤT ---
            // [MỚI] Nếu đang mưa, tự động tưới ướt tất cả đất có cây
            if (weatherManager != null && weatherManager.isRaining() && 
                data.getBaseTileType() == Tile.SOIL && data.getCropData() != null && !data.isWatered()) {
                // Mưa tự động tưới ướt đất có cây
                data.setWatered(true);
                data.setBaseTileType(Tile.SOIL_WET);
                data.setLastWateredTime(currentTime);
                data.setDryStartTime(0); // Xóa thời gian khô
                changed = true;
            }
            
            if (data.isWatered()) {
                // Đất ướt -> Tự khô (trừ khi đang mưa)
                if (weatherManager == null || !weatherManager.isRaining()) {
                    // Chỉ khô khi không mưa
                    if (currentTimeMs - data.getLastWateredTime() / 1_000_000 > CropConfig.SOIL_DRY_TIME_MS) {
                        data.setWatered(false);
                        data.setBaseTileType(Tile.SOIL);
                        data.setDryStartTime(currentTime); // Bắt đầu đếm giờ khô
                        changed = true;
                    }
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
                    
                    // Áp dụng weather/time multipliers
                    double growthSpeedMultiplier = CropConfig.BASE_GROWTH_SPEED;
                    
                    // Kiểm tra ban đêm (light intensity thấp)
                    boolean isNight = false;
                    if (timeManager != null) {
                        double lightIntensity = timeManager.getCurrentLightIntensity();
                        isNight = (lightIntensity < CropConfig.NIGHT_LIGHT_THRESHOLD);
                    }
                    
                    // Áp dụng multiplier cho ban đêm
                    if (isNight) {
                        growthSpeedMultiplier *= CropConfig.NIGHT_GROWTH_SPEED_MULTIPLIER;
                    }
                    
                    // Áp dụng multiplier cho mưa
                    if (weatherManager != null && weatherManager.isRaining()) {
                        growthSpeedMultiplier *= CropConfig.RAIN_GROWTH_SPEED_MULTIPLIER;
                    }
                    
                    // Điều chỉnh timePerStage dựa trên growth speed multiplier
                    timePerStage = timePerStage / growthSpeedMultiplier;

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

            if (changed) {
                mapNeedsRedraw = true;
            }
        }
        
        // Cập nhật index để lần sau tiếp tục từ vị trí này
        if (tilesToUpdate.size() > 0) {
            lastProcessedIndex = (startIndex + processedCount) % tilesToUpdate.size();
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