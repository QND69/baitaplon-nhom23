package com.example.farmSimulation.view.assets;

import com.example.farmSimulation.config.AssetPaths;
import com.example.farmSimulation.config.CropConfig;
import com.example.farmSimulation.config.ItemSpriteConfig;
import com.example.farmSimulation.config.TreeConfig;
import com.example.farmSimulation.config.FenceConfig;
import com.example.farmSimulation.model.*;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

// Tải và quản lý tất cả tài nguyên
public class AssetManager {
    // Cache là một cơ chế lưu trữ tạm thời dữ liệu đã tính toán hoặc đã tải về
    // để sử dụng lại sau này, nhằm tăng tốc độ truy xuất và giảm tài nguyên tiêu thụ.

    // Cache cho các ảnh <key, value> (key là đường dẫn)
    private final Map<String, Image> textureCache = new HashMap<>();

    // Cache đặc biệt cho Tile (tối ưu cho Enum)
    private final Map<Tile, Image> tileTextureMap = new EnumMap<>(Tile.class);

    // Cache cho các sprite đã được cắt
    private final Map<String, Image> spriteCache = new HashMap<>();

    // Cache cho icon của Item (để dùng cho HUD)
    private final Map<ItemType, Image> itemIconCache = new EnumMap<>(ItemType.class);

    // Cache riêng cho Icon trạng thái (Status)
    private final Map<CropStatusIndicator, Image> statusIconCache = new EnumMap<>(CropStatusIndicator.class);

    /**
     * Tải TOÀN BỘ tài nguyên game vào bộ nhớ.
     * Sẽ được gọi 1 lần duy nhất khi game bắt đầu.
     */
    public void loadAssets() {
        // Tải Player
        getTexture(AssetPaths.PLAYER_SHEET);
        getTexture(AssetPaths.PLAYER_ACTIONS_SHEET);

        // Tải GUI
        getTexture(AssetPaths.LOGO);
        Image toolsSheet = getTexture(AssetPaths.TOOLS_SHEET); // Load tools

        // Tải Tile Textures
        Image grass = getTexture(AssetPaths.GRASS);
        Image soil = getTexture(AssetPaths.SOIL);
        Image water = getTexture(AssetPaths.WATER);

        // Tải các tài nguyên trồng trọt
        Image soilWet = getTexture(AssetPaths.SOIL_WET); // Tải đất ướt
        getTexture(AssetPaths.FERTILIZER_OVERLAY); // Tải phân bón
        getTexture(AssetPaths.CROP_SHEET); // Tải ảnh cây
        getTexture(AssetPaths.ICON_BG); // Tải nền icon
        
        // Tải các tài nguyên cây và hàng rào
        getTexture(AssetPaths.TREE_SHEET); // Tải ảnh cây tự nhiên
        getTexture(AssetPaths.FENCE_SHEET); // Tải ảnh hàng rào

        // Liên kết Tile (Model) với Image (View)
        tileTextureMap.put(Tile.GRASS, grass);
        tileTextureMap.put(Tile.SOIL, soil);
        tileTextureMap.put(Tile.WATER, water);
        tileTextureMap.put(Tile.SOIL_WET, soilWet);
        // TREE và FENCE không có texture cố định, sẽ được vẽ từ spritesheet

        // Cắt và cache các icon trạng thái từ Tools Sheet
        loadStatusIcons(toolsSheet);
    }

    /**
     * Cắt icon từ tools5.png để làm icon trạng thái
     */
    private void loadStatusIcons(Image toolsSheet) {
        if (toolsSheet == null) return;
        PixelReader reader = toolsSheet.getPixelReader();

        // Cắt Icon Nước (Watering Can)
        WritableImage waterIcon = new WritableImage(reader,
                (int)(ItemSpriteConfig.TOOL_WATERING_CAN_COL * ItemSpriteConfig.TOOL_SPRITE_WIDTH), 0,
                (int)ItemSpriteConfig.TOOL_SPRITE_WIDTH, (int)ItemSpriteConfig.TOOL_SPRITE_HEIGHT);
        statusIconCache.put(CropStatusIndicator.NEEDS_WATER, waterIcon);

        // Cắt Icon Phân bón (Fertilizer)
        WritableImage fertilizerIcon = new WritableImage(reader,
                (int)(ItemSpriteConfig.TOOL_FERTILISER_COL * ItemSpriteConfig.TOOL_SPRITE_WIDTH), 0,
                (int)ItemSpriteConfig.TOOL_SPRITE_WIDTH, (int)ItemSpriteConfig.TOOL_SPRITE_HEIGHT);
        statusIconCache.put(CropStatusIndicator.NEEDS_FERTILIZER, fertilizerIcon);

        // Tạo Icon Kép (Nước + Phân bón)
        WritableImage combinedIcon = new WritableImage(reader,
                (int)(ItemSpriteConfig.TOOL_WATERING_CAN_COL * ItemSpriteConfig.TOOL_SPRITE_WIDTH), 0,
                (int)(ItemSpriteConfig.TOOL_SPRITE_WIDTH * 2), (int)ItemSpriteConfig.TOOL_SPRITE_HEIGHT);
        statusIconCache.put(CropStatusIndicator.NEED_WATER_AND_FERTILIZER, combinedIcon);

        // Icon Thu hoạch
        WritableImage harvestIcon = new WritableImage(reader,
                (int)(ItemSpriteConfig.TOOL_SCYTHE_COL * ItemSpriteConfig.TOOL_SPRITE_WIDTH), 0,
                (int)ItemSpriteConfig.TOOL_SPRITE_WIDTH, (int)ItemSpriteConfig.TOOL_SPRITE_HEIGHT);
        statusIconCache.put(CropStatusIndicator.READY_TO_HARVEST, harvestIcon);
    }

    /**
     * Lấy một ảnh từ cache. Nếu chưa có, nó sẽ tải và lưu lại.
     *
     * @param path Đường dẫn từ class AssetPaths
     */
    public Image getTexture(String path) {
        // computeIfAbsent là một method của Map (Java 8+).
        // computeIfAbsent: Nếu 'path' chưa có trong cache,
        // nó sẽ chạy hàm lambda (v -> new Image(...)) để tải ảnh,
        // sau đó tự động 'put' vào cache và return ảnh đó.
        // Cú pháp nếu ko dùng ->: V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)
        return textureCache.computeIfAbsent(path, p ->
                new Image(getClass().getResourceAsStream(p))
        );
    }

    /**
     * Lấy ảnh texture cho một loại Tile (GRASS, SOIL, etc.)
     * Siêu nhanh vì dùng EnumMap.
     */
    public Image getTileTexture(Tile tileType) {
        // Trả về ảnh mặc định (GRASS) nếu không tìm thấy
        return tileTextureMap.getOrDefault(tileType, tileTextureMap.get(Tile.GRASS));
    }

    /**
     * Lấy ảnh lớp phủ phân bón (đã cache)
     */
    public Image getFertilizerTexture() {
        return getTexture(AssetPaths.FERTILIZER_OVERLAY);
    }

    // Lấy ảnh nền Icon
    public Image getIconBG() {
        return getTexture(AssetPaths.ICON_BG);
    }

    // Lấy icon trạng thái đã cache
    public Image getStatusIcon(CropStatusIndicator status) {
        return statusIconCache.get(status);
    }

    /**
     * Lấy sprite của cây trồng và cache lại.
     */
    public Image getCropTexture(CropData cropData) {
        if (cropData == null) {
            return null;
        }

        // Kiểm tra nếu cây chết hoặc dữ liệu sai
        if (cropData.getGrowthStage() < 0) {
            return null;
        }

        // Tạo key cache
        String key = cropData.getType().name() + "_" + cropData.getGrowthStage();

        // Dùng computeIfAbsent để cắt 1 lần duy nhất
        return spriteCache.computeIfAbsent(key, k -> {
            Image cropSheet = getTexture(AssetPaths.CROP_SHEET);
            if (cropSheet == null) return null;

            PixelReader reader = cropSheet.getPixelReader();

            // Tính toán vị trí (x, y) để cắt
            double w = CropConfig.CROP_SPRITE_WIDTH;
            double h = CropConfig.CROP_SPRITE_HEIGHT;
            int x = (int) (cropData.getGrowthStage() * w);
            int y = (int) (cropData.getType().getSpriteRow() * h);

            if (x < 0 || y < 0 || x + w > cropSheet.getWidth() || y + h > cropSheet.getHeight()) return null;

            // Cắt và tạo ảnh mới
            return new WritableImage(reader, x, y, (int) w, (int) h);
        });
    }

    /**
     * Lấy icon hạt giống từ CROP_SHEET (Frame 0)
     */
    public Image getSeedIcon(CropType type) {
        Image cropSheet = getTexture(AssetPaths.CROP_SHEET);
        if (cropSheet == null) return null;

        PixelReader reader = cropSheet.getPixelReader();

        // Lấy Frame 0 (Giai đoạn hạt giống)
        int x = 0;
        // Lấy hàng tương ứng với loại cây
        int y = (int) (type.getSpriteRow() * CropConfig.CROP_SPRITE_HEIGHT);

        return new WritableImage(reader, x, y, (int)CropConfig.CROP_SPRITE_WIDTH, (int)CropConfig.CROP_SPRITE_HEIGHT);
    }

    /**
     * Lấy icon sản phẩm thu hoạch (Frame cuối cùng của cây)
     */
    public Image getHarvestIcon(CropType type) {
        Image cropSheet = getTexture(AssetPaths.CROP_SHEET);
        if (cropSheet == null) return null;

        PixelReader reader = cropSheet.getPixelReader();

        // Lấy Frame cuối cùng (MaxStages)
        int x = (int) (type.getMaxStages() * CropConfig.CROP_SPRITE_WIDTH);
        int y = (int) (type.getSpriteRow() * CropConfig.CROP_SPRITE_HEIGHT);

        return new WritableImage(reader, x, y, (int)CropConfig.CROP_SPRITE_WIDTH, (int)CropConfig.CROP_SPRITE_HEIGHT);
    }

    /**
     * Lưu trữ icon của tool (dùng cho HUD)
     * Hàm này sẽ được gọi bởi HotbarView khi nó tải icon.
     */
    public void cacheItemIcon(ItemType type, Image icon) {
        itemIconCache.put(type, icon);
    }

    /**
     * Lấy icon của item để hiển thị
     */
    public Image getItemIcon(ItemType type) {
        return itemIconCache.get(type);
    }

    /**
     * Lấy sprite của cây tự nhiên dựa trên growth stage
     */
    public Image getTreeTexture(TreeData treeData) {
        if (treeData == null) {
            return null;
        }

        int stage = treeData.getGrowthStage();
        String key = "tree_" + stage;

        return spriteCache.computeIfAbsent(key, k -> {
            Image treeSheet = getTexture(AssetPaths.TREE_SHEET);
            if (treeSheet == null) return null;

            PixelReader reader = treeSheet.getPixelReader();
            double w = TreeConfig.TREE_SPRITE_WIDTH;
            double h = TreeConfig.TREE_SPRITE_HEIGHT;
            
            // Cây có 3 frame: 0 (gốc), 1 (cây nhỏ), 2 (cây trưởng thành)
            // Cây chỉ phát triển tối đa đến stage 2
            int x = (int) (stage * w);
            int y = 0; // Chỉ có 1 hàng trong spritesheet

            if (x < 0 || y < 0 || x + w > treeSheet.getWidth() || y + h > treeSheet.getHeight()) return null;

            return new WritableImage(reader, x, y, (int) w, (int) h);
        });
    }

    /**
     * Lấy icon gỗ (WOOD) từ frame cuối cùng của tools_32x32.png
     */
    public Image getWoodIcon() {
        String key = "wood_icon";
        return spriteCache.computeIfAbsent(key, k -> {
            Image toolsSheet = getTexture(AssetPaths.TOOLS_SHEET);
            if (toolsSheet == null) return null;

            PixelReader reader = toolsSheet.getPixelReader();
            double w = ItemSpriteConfig.TOOL_SPRITE_WIDTH;
            double h = ItemSpriteConfig.TOOL_SPRITE_HEIGHT;
            
            // Frame cuối cùng trong tools sheet (cột ITEM_WOOD_COL)
            int x = (int) (ItemSpriteConfig.ITEM_WOOD_COL * w);
            int y = 0;

            if (x < 0 || y < 0 || x + w > toolsSheet.getWidth() || y + h > toolsSheet.getHeight()) return null;

            return new WritableImage(reader, x, y, (int) w, (int) h);
        });
    }

    /**
     * Lấy sprite của hàng rào dựa trên pattern và trạng thái mở/đóng
     */
    public Image getFenceTexture(FenceData fenceData) {
        if (fenceData == null) return null;

        // 1. Nếu rào đang MỞ (Cổng) -> Lấy hình cái cọc đơn
        if (fenceData.isOpen()) {
            return spriteCache.computeIfAbsent("fence_open", k -> {
                Image fenceSheet = getTexture(AssetPaths.FENCE_SHEET);
                return new WritableImage(fenceSheet.getPixelReader(), 
                    0, (int)(3 * 64), 64, 64); // Cột 0, Hàng 3
            });
        }

        // 2. Nếu rào ĐÓNG -> Map theo Pattern 0-15
        int pattern = fenceData.getTilePattern();
        String key = "fence_" + pattern;

        return spriteCache.computeIfAbsent(key, k -> {
            Image fenceSheet = getTexture(AssetPaths.FENCE_SHEET);
            PixelReader reader = fenceSheet.getPixelReader();
            
            int col, row;

            switch (pattern) {
                // --- NHÓM 1: CÁC ĐẦU MÚT & ĐƯỜNG THẲNG (CỘT 0 & HÀNG 0) ---
                case 0:  col = 0; row = 3; break; // Cọc đơn (Không nối gì)
                
                case 1:  col = 0; row = 2; break; // Chỉ nối Lên (Đầu dưới)
                case 4:  col = 0; row = 0; break; // Chỉ nối Xuống (Đầu trên)
                case 5:  col = 0; row = 1; break; // Dọc (Lên + Xuống)
                
                case 2:  col = 1; row = 0; break; // Chỉ nối Phải (Đầu trái)
                case 8:  col = 3; row = 0; break; // Chỉ nối Trái (Đầu phải)
                case 10: col = 2; row = 0; break; // Ngang (Trái + Phải)

                // --- NHÓM 2: CÁC GÓC (KHỐI 3x3 - BỐN GÓC) ---
                case 6:  col = 1; row = 1; break; // Góc Trên-Trái (Nối Phải+Xuống)
                case 12: col = 3; row = 1; break; // Góc Trên-Phải (Nối Trái+Xuống)
                case 3:  col = 1; row = 3; break; // Góc Dưới-Trái (Nối Phải+Lên)
                case 9:  col = 3; row = 3; break; // Góc Dưới-Phải (Nối Trái+Lên)

                // --- NHÓM 3: NGÃ BA (CHỮ T) ---
                case 7:  col = 1; row = 2; break; // T quay Phải (Lên+Xuống+Phải)
                case 11: col = 2; row = 3; break; // T quay Lên (Trái+Phải+Lên) - Úp ngược
                case 13: col = 3; row = 2; break; // T quay Trái (Lên+Xuống+Trái)
                case 14: col = 2; row = 1; break; // T quay Xuống (Trái+Phải+Xuống)

                // --- NHÓM 4: NGÃ TƯ (DẤU +) ---
                case 15: col = 2; row = 2; break; // Nối cả 4 hướng

                default: col = 0; row = 3; break; // Fallback về cọc đơn
            }
            
            // Cắt ảnh 64x64
            return new WritableImage(reader, col * 64, row * 64, 64, 64);
        });
    }
}