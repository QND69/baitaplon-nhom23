package com.example.farmSimulation.model;

import com.example.farmSimulation.config.TreeConfig;
import com.example.farmSimulation.config.GameLogicConfig;
import com.example.farmSimulation.config.WorldConfig;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Class quản lý hệ thống cây tự nhiên.
 * [SỬA ĐỔI] Áp dụng Procedural Generation (Giống Minecraft) để tránh cây mọc chồng chéo.
 */
public class TreeManager {
    private final WorldMap worldMap;
    private final Random random;
    
    private final long worldSeed;
    private long lastUpdateTimeMs = 0;
    private double lastPlayerX = 0;
    private double lastPlayerY = 0;
    
    private final Set<Long> generatedTiles;

    public TreeManager(WorldMap worldMap) {
        this.worldMap = worldMap;
        this.random = new Random();
        this.worldSeed = random.nextLong(); 
        this.generatedTiles = new HashSet<>();
    }
    
    private long toTileKey(int tileX, int tileY) {
        return ((long) tileX << 32) | (tileY & 0xffffffffL);
    }

    /**
     * Kiểm tra mật độ cây (Giãn cách xã hội cho cây)
     */
    private boolean hasTreeNearby(int col, int row) {
        // [SỬA] Sử dụng radius từ Config
        int radius = TreeConfig.TREE_SPACING_RADIUS; 
        
        for (int r = row - radius; r <= row + radius; r++) {
            for (int c = col - radius; c <= col + radius; c++) {
                if (c == col && r == row) continue;
                TileData neighbor = worldMap.getTileData(c, r);
                if (neighbor.getBaseTileType() == Tile.TREE || neighbor.getTreeData() != null) {
                    return true; 
                }
            }
        }
        return false;
    }

    private double getDeterministicNoise(int x, int y) {
        long hash = worldSeed;
        hash ^= (long) x * 73856093;
        hash ^= (long) y * 19349663;
        hash ^= (long) x * y * 83492791;
        hash = (hash ^ (hash >>> 16)) * 0x85ebca6b;
        hash = (hash ^ (hash >>> 13)) * 0xc2b2ae35;
        hash = hash ^ (hash >>> 16);
        return (hash & Long.MAX_VALUE) / (double) Long.MAX_VALUE;
    }

    public boolean updateTrees(long currentTime, double playerX, double playerY) {
        boolean mapNeedsRedraw = false;
        long currentTimeMs = currentTime / 1_000_000;

        // --- PHẦN 1: CÂY LỚN LÊN ---
        if (currentTimeMs - lastUpdateTimeMs >= GameLogicConfig.CROP_UPDATE_INTERVAL_MS) {
            lastUpdateTimeMs = currentTimeMs;
            for (TileData data : worldMap.getAllTileData()) {
                if (data.getTreeData() != null && data.getBaseTileType() == Tile.TREE) {
                    TreeData tree = data.getTreeData();
                    
                    if (tree.getGrowthStage() == 0 && tree.getRegrowStartTime() > 0) {
                        long timeSinceRegrow = (currentTime - tree.getRegrowStartTime()) / 1_000_000;
                        if (timeSinceRegrow >= TreeConfig.REGROW_TIME_MS) {
                            tree.setGrowthStage(1);
                            tree.setRegrowStartTime(0);
                            mapNeedsRedraw = true;
                        }
                    }
                    
                    if (tree.getGrowthStage() > 0 && tree.getGrowthStage() < 2) {
                        long timeSinceLastChop = tree.getLastChopTime() > 0 ? 
                            (currentTime - tree.getLastChopTime()) / 1_000_000 : 
                            TreeConfig.TIME_PER_GROWTH_STAGE_MS;
                        
                        int targetStage = (int) (timeSinceLastChop / TreeConfig.TIME_PER_GROWTH_STAGE_MS) + 1;
                        targetStage = Math.min(targetStage, 2);
                        
                        if (targetStage > tree.getGrowthStage()) {
                            tree.setGrowthStage(targetStage);
                            mapNeedsRedraw = true;
                        }
                    }
                }
            }
        }

        // --- PHẦN 2: SINH CÂY MỚI (PROCEDURAL GENERATION) ---
        if (Math.abs(playerX - lastPlayerX) > WorldConfig.TILE_SIZE || Math.abs(playerY - lastPlayerY) > WorldConfig.TILE_SIZE) {
            boolean spawned = generateTreesAroundPlayer(playerX, playerY);
            if (spawned) mapNeedsRedraw = true;
            
            lastPlayerX = playerX;
            lastPlayerY = playerY;
        }

        return mapNeedsRedraw;
    }

    private boolean generateTreesAroundPlayer(double playerX, double playerY) {
        boolean anyChange = false;
        int playerTileX = (int) Math.floor(playerX / WorldConfig.TILE_SIZE);
        int playerTileY = (int) Math.floor(playerY / WorldConfig.TILE_SIZE);

        int generationRadius = 14; 

        for (int row = playerTileY - generationRadius; row <= playerTileY + generationRadius; row++) {
            for (int col = playerTileX - generationRadius; col <= playerTileX + generationRadius; col++) {
                
                long key = toTileKey(col, row);

                if (generatedTiles.contains(key)) {
                    continue; 
                }

                generatedTiles.add(key);

                double noiseValue = getDeterministicNoise(col, row);
                
                // [SỬA] Sử dụng hằng số TỶ LỆ từ Config (VD: 0.06 thay vì 0.15)
                if (noiseValue < TreeConfig.TREE_GENERATION_PROBABILITY) {
                    
                    TileData data = worldMap.getTileData(col, row);

                    if (data.getBaseTileType() == Tile.GRASS && 
                        data.getTreeData() == null && 
                        data.getCropData() == null &&
                        data.getFenceData() == null) {
                        
                        if (!hasTreeNearby(col, row)) {
                            data.setBaseTileType(Tile.TREE);
                            double stageNoise = getDeterministicNoise(col + 1000, row + 1000); 
                            int initialStage = (stageNoise > 0.5) ? 2 : 1; 
                            
                            TreeData tree = new TreeData(initialStage); 
                            data.setTreeData(tree);
                            worldMap.setTileData(col, row, data);
                            anyChange = true;
                        }
                    }
                }
            }
        }
        return anyChange;
    }
}