package com.example.farmSimulation.view;

import com.example.farmSimulation.config.CropConfig;
import com.example.farmSimulation.config.HudConfig;
import com.example.farmSimulation.config.WorldConfig;
import com.example.farmSimulation.config.TreeConfig;
import com.example.farmSimulation.config.FenceConfig;
import com.example.farmSimulation.config.PlayerSpriteConfig;
import com.example.farmSimulation.model.Tile;
import com.example.farmSimulation.model.TileData;
import com.example.farmSimulation.model.WorldMap;
import com.example.farmSimulation.view.assets.AssetManager;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import lombok.Getter;

@Getter
public class WorldRenderer {
    // Mảng 2D LƯU TRỮ các ImageView
    private final ImageView[][] baseTiles; // Lớp 1: Các ô hiển thị trên màn hình (GRASS, SOIL, WATER)
    private final ImageView[][] overlayTiles; // Lớp 2: Phân bón
    private final ImageView[][] cropTiles;    // Lớp 3: Cây trồng (crops)
    private final ImageView[][] treeTiles;    // Lớp 4: Cây tự nhiên (trees)
    private final ImageView[][] fenceTiles;   // Lớp 5: Hàng rào (fences)
    private final ImageView[][] statusIconTiles; // Lớp 6: Icon báo hiệu
    private final ImageView[][] statusBackground; // Lớp 7: Mảng chứa nền mờ

    private final AssetManager assetManager; // Để lấy textures
    private final WorldMap worldMap;         // Để biết vẽ tile gì

    private final Pane worldPane;   // Pane "thế giới" chứa lưới, chỉ dùng để di chuyển cuộn mượt
    private final Rectangle tileSelector; // Hình vuông chứa ô được chọn
    
    // Ghost placement: Hiển thị bóng mờ khi cầm item có thể đặt
    private final ImageView ghostPlacement;
    
    // Debug: Hitbox collision của cây (chỉ hiển thị khi DEBUG_TREE_HITBOX = true)
    private final Rectangle[][] treeHitboxes;
    
    // Debug: Hitbox collision của rào (chỉ hiển thị khi DEBUG_FENCE_HITBOX = true)
    private final Rectangle[][] fenceHitboxes;

    // Lưu lại vị trí render map lần cuối
    private int lastRenderedStartCol = -1;
    private int lastRenderedStartRow = -1;

    public WorldRenderer(AssetManager assetManager, WorldMap worldMap) {
        this.assetManager = assetManager;
        this.worldMap = worldMap;

        // Khởi tạo tất cả các lớp
        this.baseTiles = new ImageView[WorldConfig.NUM_ROWS_ON_SCREEN][WorldConfig.NUM_COLS_ON_SCREEN];
        this.overlayTiles = new ImageView[WorldConfig.NUM_ROWS_ON_SCREEN][WorldConfig.NUM_COLS_ON_SCREEN];
        this.cropTiles = new ImageView[WorldConfig.NUM_ROWS_ON_SCREEN][WorldConfig.NUM_COLS_ON_SCREEN];
        this.treeTiles = new ImageView[WorldConfig.NUM_ROWS_ON_SCREEN][WorldConfig.NUM_COLS_ON_SCREEN];
        this.fenceTiles = new ImageView[WorldConfig.NUM_ROWS_ON_SCREEN][WorldConfig.NUM_COLS_ON_SCREEN];
        this.statusIconTiles = new ImageView[WorldConfig.NUM_ROWS_ON_SCREEN][WorldConfig.NUM_COLS_ON_SCREEN];
        this.statusBackground = new ImageView[WorldConfig.NUM_ROWS_ON_SCREEN][WorldConfig.NUM_COLS_ON_SCREEN];
        
        // Khởi tạo mảng hitbox debug cho cây (chỉ khi DEBUG_TREE_HITBOX = true)
        this.treeHitboxes = new Rectangle[WorldConfig.NUM_ROWS_ON_SCREEN][WorldConfig.NUM_COLS_ON_SCREEN];
        
        // Khởi tạo mảng hitbox debug cho rào (chỉ khi DEBUG_FENCE_HITBOX = true)
        this.fenceHitboxes = new Rectangle[WorldConfig.NUM_ROWS_ON_SCREEN][WorldConfig.NUM_COLS_ON_SCREEN];

        this.worldPane = new Pane();

        // Chèn các tile rỗng vào worldPane
        for (int r = 0; r < WorldConfig.NUM_ROWS_ON_SCREEN; r++) {
            for (int c = 0; c < WorldConfig.NUM_COLS_ON_SCREEN; c++) {
                // Gọi hàm helper để tạo ImageView
                baseTiles[r][c] = createTileView(c, r, 0, WorldConfig.TILE_SIZE, WorldConfig.TILE_SIZE);
                overlayTiles[r][c] = createTileView(c, r, 0, WorldConfig.TILE_SIZE, WorldConfig.TILE_SIZE);
                cropTiles[r][c] = createTileView(c, r, -CropConfig.CROP_Y_OFFSET, CropConfig.CROP_SPRITE_WIDTH, CropConfig.CROP_SPRITE_HEIGHT);
                treeTiles[r][c] = createTileView(c, r, -TreeConfig.TREE_Y_OFFSET, TreeConfig.TREE_SPRITE_WIDTH, TreeConfig.TREE_SPRITE_HEIGHT);
                fenceTiles[r][c] = createTileView(c, r, -FenceConfig.FENCE_Y_OFFSET, FenceConfig.FENCE_SPRITE_WIDTH, FenceConfig.FENCE_SPRITE_HEIGHT);

                // Status Background
                ImageView bg = createTileView(c, r, -HudConfig.ICON_Y_OFFSET, HudConfig.ICON_BG_SIZE, HudConfig.ICON_BG_SIZE);
                bg.setImage(assetManager.getIconBG());
                bg.setVisible(false);

                // Căn BG góc trái trên khớp với ô tile
                bg.setLayoutX(c * WorldConfig.TILE_SIZE);

                statusBackground[r][c] = bg;

                // Status Icon
                ImageView icon = createTileView(c, r, -HudConfig.ICON_Y_OFFSET, HudConfig.ICON_SIZE, HudConfig.ICON_SIZE);

                // Căn giữa Icon so với BG
                double iconOffset = (HudConfig.ICON_BG_SIZE - HudConfig.ICON_SIZE) / 2;
                icon.setLayoutX(c * WorldConfig.TILE_SIZE + iconOffset);

                // Offset Y: Dịch thêm HudConfig.ICON_PADDING_TOP để lọt vào bong bóng
                icon.setLayoutY(r * WorldConfig.TILE_SIZE - HudConfig.ICON_Y_OFFSET + HudConfig.ICON_PADDING_TOP);

                statusIconTiles[r][c] = icon;
                
                // Khởi tạo tree hitbox debug (chỉ khi DEBUG_TREE_HITBOX = true)
                if (TreeConfig.DEBUG_TREE_HITBOX && PlayerSpriteConfig.DEBUG_PLAYER_BOUNDS) {
                    Rectangle treeHitbox = new Rectangle(
                        TreeConfig.TREE_HITBOX_WIDTH,
                        TreeConfig.TREE_HITBOX_HEIGHT
                    );
                    treeHitbox.setFill(null);
                    treeHitbox.setStroke(TreeConfig.DEBUG_TREE_HITBOX_COLOR);
                    treeHitbox.setStrokeWidth(2.0);
                    treeHitbox.setMouseTransparent(true);
                    treeHitbox.setVisible(false); // Sẽ được cập nhật trong updateMap()
                    treeHitboxes[r][c] = treeHitbox;
                } else {
                    treeHitboxes[r][c] = null;
                }
                
                // Khởi tạo fence hitbox debug (chỉ khi DEBUG_FENCE_HITBOX = true)
                if (FenceConfig.DEBUG_FENCE_HITBOX && PlayerSpriteConfig.DEBUG_PLAYER_BOUNDS) {
                    Rectangle fenceHitbox = new Rectangle(
                        FenceConfig.FENCE_HITBOX_WIDTH,
                        FenceConfig.FENCE_HITBOX_HEIGHT
                    );
                    fenceHitbox.setFill(null);
                    fenceHitbox.setStroke(FenceConfig.DEBUG_FENCE_HITBOX_COLOR);
                    fenceHitbox.setStrokeWidth(2.0);
                    fenceHitbox.setMouseTransparent(true);
                    fenceHitbox.setVisible(false); // Sẽ được cập nhật trong updateMap()
                    fenceHitboxes[r][c] = fenceHitbox;
                } else {
                    fenceHitboxes[r][c] = null;
                }

                // Thêm vào worldPane theo ĐÚNG THỨ TỰ RENDER
                // Lớp dưới cùng được thêm vào trước
                worldPane.getChildren().addAll(
                        baseTiles[r][c],         // Lớp 1: Đất (GRASS, SOIL, WATER)
                        overlayTiles[r][c],      // Lớp 2: Phân bón
                        cropTiles[r][c],         // Lớp 3: Cây trồng (crops)
                        treeTiles[r][c],         // Lớp 4: Cây tự nhiên (trees)
                        fenceTiles[r][c],        // Lớp 5: Hàng rào (fences)
                        statusBackground[r][c],  // Lớp 6: Nền icon
                        statusIconTiles[r][c]    // Lớp 7: Icon
                );
                
                // Thêm tree hitbox vào worldPane (lớp trên cùng, sau khi thêm tất cả)
                if (treeHitboxes[r][c] != null) {
                    worldPane.getChildren().add(treeHitboxes[r][c]);
                }
                
                // Thêm fence hitbox vào worldPane (lớp trên cùng, sau khi thêm tất cả)
                if (fenceHitboxes[r][c] != null) {
                    worldPane.getChildren().add(fenceHitboxes[r][c]);
                }
            }
        }

        // Khởi tạo ô vuông selector
        this.tileSelector = new Rectangle(WorldConfig.TILE_SIZE, WorldConfig.TILE_SIZE);
        this.tileSelector.setFill(null);                                    // Không tô nền
        this.tileSelector.setStroke(WorldConfig.SELECTOR_COLOR);             // Màu viền
        this.tileSelector.setStrokeWidth(WorldConfig.SELECTOR_STROKE_WIDTH); // Độ dày viền
        this.tileSelector.setVisible(true);                                 // Luôn hiển thị
        
        // Khởi tạo ghost placement
        this.ghostPlacement = new ImageView();
        this.ghostPlacement.setMouseTransparent(true); // Không cản sự kiện chuột
        this.ghostPlacement.setVisible(false); // Ẩn mặc định
        this.ghostPlacement.setOpacity(WorldConfig.GHOST_PLACEMENT_OPACITY);
        
        // Thêm ghost placement vào worldPane (lớp trên cùng)
        // worldPane.getChildren().add(ghostPlacement);
    }

    /**
     * Hàm helper để tạo một ImageView cho một lớp
     */
    private ImageView createTileView(int c, int r, double yOffset, double width, double height) {
        ImageView tileView = new ImageView();
        tileView.setFitWidth(width);   // Set chiều rộng
        tileView.setFitHeight(height); // Set chiều cao
        tileView.setPreserveRatio(false); // Không giữ tỉ lệ, hiển thị đúng kích thước
        tileView.setLayoutX(c * WorldConfig.TILE_SIZE);
        tileView.setLayoutY(r * WorldConfig.TILE_SIZE + yOffset);
        tileView.setSmooth(false);
        return tileView;
    }

    // Hàm này được gọi nếu có thay đổi về thế giới
    // Nhiệm vụ: Xóa map cũ, chỉ vẽ các ô (tile) mà camera thấy.
    public void updateMap(double worldOffsetX, double worldOffsetY, boolean forceRedraw) {
        // *** TÍNH TOÁN VÙNG CAMERA NHÌN THẤY ***
        /* Tính tọa độ của camera (góc trên-trái màn hình) trong thế giới
        Tọa độ của worldPane (worldOffsetX và worldOffsetY) là tọa độ của (0,0) của thế giới so với màn hình
        => Tọa độ của màn hình so với thế giới là giá trị âm của nó */
        double cameraWorldX = -worldOffsetX;
        double cameraWorldY = -worldOffsetY;

        // Tính ô logic bắt đầu (số nguyên) mà camera nhìn thấy
        int startCol = (int) Math.floor(cameraWorldX / WorldConfig.TILE_SIZE);
        int startRow = (int) Math.floor(cameraWorldY / WorldConfig.TILE_SIZE);

        // Tính phần dư (pixel lẻ) để cuộn mượt
        // Đây là mấu chốt: worldPane chỉ di chuyển trong phạm vi 1 ô
        double pixelOffsetX = -(cameraWorldX - (startCol * WorldConfig.TILE_SIZE));
        double pixelOffsetY = -(cameraWorldY - (startRow * WorldConfig.TILE_SIZE));

        // Di chuyển TOÀN BỘ worldPane (chứa lưới) để tạo hiệu ứng mượt
        worldPane.setLayoutX(pixelOffsetX);
        worldPane.setLayoutY(pixelOffsetY);

        // Kiểm tra xem có CẦN vẽ lại các ô hay không
        boolean needsTileUpdate = (startCol != lastRenderedStartCol ||
                startRow != lastRenderedStartRow ||
                forceRedraw);

        // Không cần vẽ lại, tiết kiệm rất nhiều CPU
        if (!needsTileUpdate) {
            return;
        }

        // CẬP NHẬT HÌNH ẢNH (TEXTURE) cho TẤT CẢ CÁC LỚP
        for (int r = 0; r < WorldConfig.NUM_ROWS_ON_SCREEN; r++) {
            for (int c = 0; c < WorldConfig.NUM_COLS_ON_SCREEN; c++) {
                // Tính ô logic (thế giới) mà ô lưới (màn hình) này cần hiển thị
                int logicalCol = startCol + c;
                int logicalRow = startRow + r;

                // Lấy TOÀN BỘ TileData (Model)
                TileData data = worldMap.getTileData(logicalCol, logicalRow);

                // --- Lớp 1: Đất (Base) ---
                // Lấy loại tile từ Model
                Tile type = data.getBaseTileType();
                // Nếu là TREE hoặc FENCE, base tile vẫn là GRASS (để hiển thị nền)
                Tile baseType = (type == Tile.TREE || type == Tile.FENCE) ? Tile.GRASS : type;
                Image baseTexture = assetManager.getTileTexture(baseType);
                this.baseTiles[r][c].setImage(baseTexture);

                // --- Lớp 2: Phân bón (Overlay) ---
                Image overlayTexture = data.isFertilized() ? assetManager.getFertilizerTexture() : null;
                this.overlayTiles[r][c].setImage(overlayTexture);

                // --- Lớp 3: Cây trồng (Crop) ---
                Image cropTexture = assetManager.getCropTexture(data.getCropData());
                this.cropTiles[r][c].setImage(cropTexture);
                
                // --- Lớp 4: Cây tự nhiên (Tree) ---
                Image treeTexture = assetManager.getTreeTexture(data.getTreeData());
                this.treeTiles[r][c].setImage(treeTexture);
                
                // Cập nhật tree hitbox debug (chỉ khi DEBUG_TREE_HITBOX = true và có cây)
                if (TreeConfig.DEBUG_TREE_HITBOX && PlayerSpriteConfig.DEBUG_PLAYER_BOUNDS && treeHitboxes[r][c] != null) {
                    if (data.getTreeData() != null && data.getTreeData().getGrowthStage() > 0) {
                        double tileLocalX = c * WorldConfig.TILE_SIZE;
                        double tileLocalY = r * WorldConfig.TILE_SIZE;
                        
                        // X: Căn giữa ngang
                        double layoutX = tileLocalX + (WorldConfig.TILE_SIZE - TreeConfig.TREE_HITBOX_WIDTH) / 2.0;
                        
                        // Y: Căn theo đáy thật sự của cây (Đáy Tile - Offset - Chiều cao Hitbox)
                        // Công thức này phải khớp với CollisionManager
                        double visualTreeBottomY = (tileLocalY + WorldConfig.TILE_SIZE) - CropConfig.CROP_Y_OFFSET;
                        double layoutY = visualTreeBottomY 
                                         - TreeConfig.TREE_HITBOX_HEIGHT 
                                         - TreeConfig.TREE_HITBOX_Y_OFFSET_FROM_BOTTOM;
                        
                        treeHitboxes[r][c].setWidth(TreeConfig.TREE_HITBOX_WIDTH);
                        treeHitboxes[r][c].setHeight(TreeConfig.TREE_HITBOX_HEIGHT);
                        treeHitboxes[r][c].setLayoutX(layoutX);
                        treeHitboxes[r][c].setLayoutY(layoutY);
                        treeHitboxes[r][c].setVisible(true);
                    } else {
                        treeHitboxes[r][c].setVisible(false);
                    }
                }
                
                // --- Lớp 5: Hàng rào (Fence) ---
                Image fenceTexture = assetManager.getFenceTexture(data.getFenceData());
                this.fenceTiles[r][c].setImage(fenceTexture);
                
                // Cập nhật fence hitbox debug (chỉ khi DEBUG_FENCE_HITBOX = true và có rào đóng)
                if (FenceConfig.DEBUG_FENCE_HITBOX && PlayerSpriteConfig.DEBUG_PLAYER_BOUNDS && fenceHitboxes[r][c] != null) {
                    if (data.getFenceData() != null && data.getFenceData().isSolid()) {
                        double tileLocalX = c * WorldConfig.TILE_SIZE;
                        double tileLocalY = r * WorldConfig.TILE_SIZE;
                        
                        // [SỬA LẠI CÔNG THỨC VẼ]
                        // Căn giữa ngang
                        double layoutX = tileLocalX + (WorldConfig.TILE_SIZE - FenceConfig.FENCE_HITBOX_WIDTH) / 2.0;
                        
                        // Căn dọc theo offset cấu hình
                        double layoutY = (tileLocalY + WorldConfig.TILE_SIZE) 
                                         - FenceConfig.FENCE_HITBOX_HEIGHT 
                                         - FenceConfig.FENCE_HITBOX_Y_OFFSET_FROM_BOTTOM;
                        
                        fenceHitboxes[r][c].setWidth(FenceConfig.FENCE_HITBOX_WIDTH);
                        fenceHitboxes[r][c].setHeight(FenceConfig.FENCE_HITBOX_HEIGHT);
                        fenceHitboxes[r][c].setLayoutX(layoutX);
                        fenceHitboxes[r][c].setLayoutY(layoutY);
                        
                        fenceHitboxes[r][c].setVisible(true);
                    } else {
                        // Nhớ tắt đi nếu không phải rào đóng
                        fenceHitboxes[r][c].setVisible(false);
                    }
                }

                // --- Lớp 3: Cây trồng (Crop) ---
                // Cập nhật Icon & Background
                Image statusIcon = assetManager.getStatusIcon(data.getStatusIndicator());

                // --- Lớp 4 & 5: Icon và Background ---
                // Xử lý icon kép (Size icon thay đổi, nhưng BG giữ nguyên)
                if (data.getStatusIndicator() == com.example.farmSimulation.model.CropStatusIndicator.NEED_WATER_AND_FERTILIZER) {
                    // Icon kép rộng gấp đôi
                    double doubleWidth = HudConfig.ICON_SIZE * 2;
                    this.statusIconTiles[r][c].setFitWidth(doubleWidth);

                    // Căn giữa lại
                    double iconOffset = (HudConfig.ICON_BG_SIZE - doubleWidth) / 2;
                    this.statusIconTiles[r][c].setLayoutX((startCol + c - startCol) * WorldConfig.TILE_SIZE + iconOffset);

                    // Background: GIỮ NGUYÊN, không resize
                } else {
                    // Reset về icon đơn
                    this.statusIconTiles[r][c].setFitWidth(HudConfig.ICON_SIZE);
                    double iconOffset = (HudConfig.ICON_BG_SIZE - HudConfig.ICON_SIZE) / 2;
                    this.statusIconTiles[r][c].setLayoutX((startCol + c - startCol) * WorldConfig.TILE_SIZE + iconOffset);
                }

                this.statusIconTiles[r][c].setImage(statusIcon);

                // Hiện BG nếu có icon
                this.statusBackground[r][c].setVisible(statusIcon != null);

                // Reset vị trí Background về mặc định (căn trái) để đảm bảo không bị lệch do logic cũ
                this.statusBackground[r][c].setLayoutX((startCol + c - startCol) * WorldConfig.TILE_SIZE);
                this.statusBackground[r][c].setFitWidth(HudConfig.ICON_BG_SIZE); // Đảm bảo luôn là 64
            }
        }
        // Ghi nhớ vị trí render lần cối
        this.lastRenderedStartCol = startCol;
        this.lastRenderedStartRow = startRow;
    }

    // Hàm này được gọi 60 lần/giây bởi Game Loop.
    // Nhiệm vụ: Tính toán và di chuyển "ô chọn" (selector)
    // để nó "bắt dính" (snap) vào ô (Tile) mà chuột đang trỏ vào.
    public void updateSelector(int tileSelectedX, int tileSelectedY, double worldOffsetX, double worldOffsetY) {
        // Kiểm tra tileSelector được khai báo chưa
        if (this.tileSelector == null) {
            return;
        }
        // Tọa độ thực của ô trên màn hình
        double tileSelectedOnScreenX = tileSelectedX * WorldConfig.TILE_SIZE + worldOffsetX;
        double tileSelectedOnScreenY = tileSelectedY * WorldConfig.TILE_SIZE + worldOffsetY;

        // Hiển thị ô được trỏ chuột
        this.tileSelector.setLayoutX(tileSelectedOnScreenX);
        this.tileSelector.setLayoutY(tileSelectedOnScreenY);
    }
    
    /**
     * Cập nhật ghost placement: Hiển thị bóng mờ khi cầm item có thể đặt (WOOD hoặc SEEDS)
     * @param tileX Tọa độ X của ô chuột đang trỏ vào
     * @param tileY Tọa độ Y của ô chuột đang trỏ vào
     * @param worldOffsetX Offset X của thế giới
     * @param worldOffsetY Offset Y của thế giới
     * @param currentItem Item hiện tại đang cầm (null nếu không có)
     */
    public void updateGhostPlacement(int tileX, int tileY, double worldOffsetX, double worldOffsetY, com.example.farmSimulation.model.ItemStack currentItem) {
        // Kiểm tra xem có item phù hợp không
        boolean shouldShow = false;
        Image ghostImage = null;
        
        double yOffsetCorrection = 0.0; // Biến để lưu offset cần điều chỉnh

        if (currentItem != null) {
            com.example.farmSimulation.model.ItemType itemType = currentItem.getItemType();
            
            // WOOD (Fence)
            if (itemType == com.example.farmSimulation.model.ItemType.WOOD) {
                ghostImage = assetManager.getFenceTexture(new com.example.farmSimulation.model.FenceData(false));
                shouldShow = true;
                yOffsetCorrection = FenceConfig.FENCE_Y_OFFSET; // Lấy offset của rào
            }
            // SEEDS (Crop)
            else if (itemType.name().startsWith("SEEDS_")) {
                try {
                    com.example.farmSimulation.model.CropType cropType = com.example.farmSimulation.model.CropType.valueOf(itemType.name().substring(6));
                    ghostImage = assetManager.getSeedIcon(cropType); // Lấy ảnh hạt giống hoặc cây con
                    shouldShow = true;
                    yOffsetCorrection = CropConfig.CROP_Y_OFFSET; // [QUAN TRỌNG] Lấy offset của cây trồng (16.0)
                } catch (IllegalArgumentException ignored) {}
            }
        }
        
        if (shouldShow && ghostImage != null) {
            double screenX = tileX * WorldConfig.TILE_SIZE + worldOffsetX;
            double screenY = tileY * WorldConfig.TILE_SIZE + worldOffsetY;
            
            // Căn giữa ghost image trong ô tile
            double imageWidth = ghostImage.getWidth();
            double imageHeight = ghostImage.getHeight();
            double offsetX = (WorldConfig.TILE_SIZE - imageWidth) / 2.0;
            
            // [SỬA CÔNG THỨC Y]
            // Trừ đi yOffsetCorrection để nâng bóng mờ lên cao bằng vật thể thật
            double offsetY = (WorldConfig.TILE_SIZE - imageHeight) / 2.0 - yOffsetCorrection; 
            
            ghostPlacement.setImage(ghostImage);
            ghostPlacement.setFitWidth(imageWidth);
            ghostPlacement.setFitHeight(imageHeight);
            ghostPlacement.setLayoutX(screenX + offsetX);
            ghostPlacement.setLayoutY(screenY + offsetY);
            ghostPlacement.setVisible(true);
        } else {
            ghostPlacement.setVisible(false);
        }
    }
}