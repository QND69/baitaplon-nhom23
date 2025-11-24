package com.example.farmSimulation.model;

import com.example.farmSimulation.config.CropConfig;
import com.example.farmSimulation.config.FenceConfig;  
import com.example.farmSimulation.config.TreeConfig;
import com.example.farmSimulation.config.WorldConfig;

/**
 * Class quản lý collision (va chạm) cho player.
 * Kiểm tra xem player có thể di chuyển đến một vị trí không.
 */
public class CollisionManager {
    private final WorldMap worldMap;

    public CollisionManager(WorldMap worldMap) {
        this.worldMap = worldMap;
    }

    /**
     * Kiểm tra xem một vị trí có thể đi qua được không
     * 
     * @param tileX Tọa độ X (tile)
     * @param tileY Tọa độ Y (tile)
     * @return true nếu có thể đi qua, false nếu bị chặn
     */
    public boolean canPassThrough(double tileX, double tileY) {
        int col = (int) Math.floor(tileX / WorldConfig.TILE_SIZE);
        int row = (int) Math.floor(tileY / WorldConfig.TILE_SIZE);

        TileData data = worldMap.getTileData(col, row);
        
        // Kiểm tra hàng rào
        if (data.getFenceData() != null && data.getFenceData().isSolid()) {
            // 1. Tính toán Hitbox Trung Tâm (Cái cọc)
            double tileWorldX = col * WorldConfig.TILE_SIZE;
            double tileWorldY = row * WorldConfig.TILE_SIZE;
            
            double tileCenterX = tileWorldX + (WorldConfig.TILE_SIZE / 2.0);
            double tileBottomY = tileWorldY + WorldConfig.TILE_SIZE;

            // Tâm của cọc rào
            double centerHitboxX = tileCenterX;
            double centerHitboxY = tileBottomY 
                                   - (FenceConfig.FENCE_HITBOX_HEIGHT / 2.0) 
                                   - FenceConfig.FENCE_HITBOX_Y_OFFSET_FROM_BOTTOM;
            
            double halfW = FenceConfig.FENCE_HITBOX_WIDTH / 2.0;
            double halfH = FenceConfig.FENCE_HITBOX_HEIGHT / 2.0;

            // --- CHECK 1: Cọc Trung Tâm ---
            if (tileX >= centerHitboxX - halfW && tileX <= centerHitboxX + halfW &&
                tileY >= centerHitboxY - halfH && tileY <= centerHitboxY + halfH) {
                return false;
            }

            // 2. Tính toán các "Cánh tay" nối (Rails) dựa trên Pattern
            int pattern = data.getFenceData().getTilePattern();
            
            // Bit 0: Top, Bit 1: Right, Bit 2: Bottom, Bit 3: Left
            boolean hasTop = (pattern & 1) != 0;
            boolean hasRight = (pattern & 2) != 0;
            boolean hasBottom = (pattern & 4) != 0;
            boolean hasLeft = (pattern & 8) != 0;

            // --- CHECK 2: Tay nối sang TRÁI ---
            if (hasLeft) {
                // Vùng từ mép trái Tile đến mép trái Cọc trung tâm
                double railLeftX = tileWorldX; 
                double railRightX = centerHitboxX - halfW;
                
                if (tileX >= railLeftX && tileX <= railRightX &&
                    tileY >= centerHitboxY - halfH && tileY <= centerHitboxY + halfH) { // Y giữ nguyên theo cọc
                    return false;
                }
            }

            // --- CHECK 3: Tay nối sang PHẢI ---
            if (hasRight) {
                // Vùng từ mép phải Cọc trung tâm đến mép phải Tile
                double railLeftX = centerHitboxX + halfW; 
                double railRightX = tileWorldX + WorldConfig.TILE_SIZE;

                if (tileX >= railLeftX && tileX <= railRightX &&
                    tileY >= centerHitboxY - halfH && tileY <= centerHitboxY + halfH) {
                    return false;
                }
            }

            // --- CHECK 4: Tay nối lên TRÊN (Top) ---
            if (hasTop) {
                // Vùng từ mép trên Tile đến mép trên Cọc
                // (Lưu ý: rào dọc thường mỏng hơn rào ngang một chút về visual, nhưng để chặn thì cứ lấy full width cọc)
                double railTopY = tileWorldY;
                double railBottomY = centerHitboxY - halfH;

                if (tileX >= centerHitboxX - halfW && tileX <= centerHitboxX + halfW &&
                    tileY >= railTopY && tileY <= railBottomY) {
                    return false;
                }
            }

            // --- CHECK 5: Tay nối xuống DƯỚI (Bottom) ---
            if (hasBottom) {
                double railTopY = centerHitboxY + halfH;
                double railBottomY = tileWorldY + WorldConfig.TILE_SIZE;

                if (tileX >= centerHitboxX - halfW && tileX <= centerHitboxX + halfW &&
                    tileY >= railTopY && tileY <= railBottomY) {
                    return false;
                }
            }
        }
        
        // Kiểm tra cây
        // [SỬA] Gốc cây (stage 0) vẫn có collision, chỉ khi chặt lần 2 mới mất hoàn toàn
        // Nếu có TreeData và baseTileType == TREE thì luôn có collision (dù là cây sống hay gốc cây)
        if (data.getTreeData() != null && data.getBaseTileType() == Tile.TREE) {
            TreeData tree = data.getTreeData();
            // Gốc cây (stage 0) và cây sống (stage > 0) đều có collision
            // Chỉ mất collision khi TreeData == null (đã chặt lần 2 và xóa hoàn toàn)
            if (tree.getGrowthStage() >= 0) {
                double tileWorldX = col * WorldConfig.TILE_SIZE;
                double tileWorldY = row * WorldConfig.TILE_SIZE;
    
                // [LOGIC CHUẨN]
                // Đáy ô Tile là (tileWorldY + 64).
                // Cây được vẽ dịch lên 16px (CROP_Y_OFFSET).
                // => Đáy thật sự của cây (nơi cần chặn) = Đáy Tile - 16px.
                double visualTreeBottomY = (tileWorldY + WorldConfig.TILE_SIZE) - CropConfig.CROP_Y_OFFSET;
                
                // Tâm Hitbox X: Giữa ô
                double hitboxCenterX = tileWorldX + WorldConfig.TILE_SIZE / 2.0;
                
                // Tâm Hitbox Y: Từ đáy thật sự, nhích lên nửa chiều cao hitbox
                // [SỬA] Thêm "- TreeConfig.TREE_HITBOX_Y_OFFSET_FROM_BOTTOM" vào công thức
                // Để đẩy tâm hitbox lên cao hơn
                double hitboxCenterY = visualTreeBottomY 
                - (TreeConfig.TREE_HITBOX_HEIGHT / 2.0) 
                - TreeConfig.TREE_HITBOX_Y_OFFSET_FROM_BOTTOM;
    
                double halfWidth = TreeConfig.TREE_HITBOX_WIDTH / 2.0;
                double halfHeight = TreeConfig.TREE_HITBOX_HEIGHT / 2.0;
                
                // Kiểm tra AABB (Axis-Aligned Bounding Box)
                if (tileX >= hitboxCenterX - halfWidth && 
                    tileX <= hitboxCenterX + halfWidth &&
                    tileY >= hitboxCenterY - halfHeight && 
                    tileY <= hitboxCenterY + halfHeight) {
                    return false; // Va chạm -> Chặn lại
                }
            }
        }
        if (data.getBaseTileType() == Tile.WATER) {
            return false;
        }

        return true;
    }

    /**
     * Kiểm tra collision với một bounding box (hình chữ nhật)
     * 
     * @param centerX Tọa độ X trung tâm
     * @param centerY Tọa độ Y trung tâm
     * @param width Chiều rộng
     * @param height Chiều cao
     * @return true nếu có collision, false nếu không
     */
    /**
     * [SỬA] Kiểm tra va chạm kỹ hơn (thêm điểm giữa và các cạnh)
     * Để khắc phục lỗi "lách qua" khi hitbox cây nhỏ.
     */
    public boolean checkCollision(double centerX, double centerY, double width, double height) {
        double halfW = width / 2.0;
        double halfH = height / 2.0;

        // 1. Kiểm tra 4 góc (Cũ)
        if (!canPassThrough(centerX - halfW, centerY - halfH)) return true; // Top-Left
        if (!canPassThrough(centerX + halfW, centerY - halfH)) return true; // Top-Right
        if (!canPassThrough(centerX - halfW, centerY + halfH)) return true; // Bottom-Left
        if (!canPassThrough(centerX + halfW, centerY + halfH)) return true; // Bottom-Right

        // 2. [MỚI - QUAN TRỌNG] Kiểm tra 4 trung điểm các cạnh (Mid-points)
        // Giúp chặn việc đi xuyên khi vật cản nhỏ hơn chiều rộng player
        if (!canPassThrough(centerX, centerY - halfH)) return true; // Top-Mid
        if (!canPassThrough(centerX, centerY + halfH)) return true; // Bottom-Mid
        if (!canPassThrough(centerX - halfW, centerY)) return true; // Left-Mid
        if (!canPassThrough(centerX + halfW, centerY)) return true; // Right-Mid
        
        // 3. [MỚI] Kiểm tra tâm (Center) - Dự phòng
        if (!canPassThrough(centerX, centerY)) return true;

        return false;
    }
}



