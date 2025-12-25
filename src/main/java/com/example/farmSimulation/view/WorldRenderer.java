package com.example.farmSimulation.view;

import com.example.farmSimulation.config.AnimalConfig;
import com.example.farmSimulation.config.CropConfig;
import com.example.farmSimulation.config.HudConfig;
import com.example.farmSimulation.config.WorldConfig;
import com.example.farmSimulation.config.TreeConfig;
import com.example.farmSimulation.config.FenceConfig;
import com.example.farmSimulation.config.ItemSpriteConfig;
import com.example.farmSimulation.config.PlayerSpriteConfig;
import com.example.farmSimulation.model.Animal;
import com.example.farmSimulation.model.Tile;
import com.example.farmSimulation.model.TileData;
import com.example.farmSimulation.model.WorldMap;
import com.example.farmSimulation.view.assets.ImageManager;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class WorldRenderer {
    // Mảng 2D LƯU TRỮ các ImageView
    private final ImageView[][] baseTiles; // Lớp 1: Các ô hiển thị trên màn hình (GRASS, SOIL, WATER)
    private final ImageView[][] overlayTiles; // Lớp 2: Phân bón
    private final ImageView[][] cropTiles;    // Lớp 3: Cây trồng (crops)
    private final ImageView[][] treeTiles;    // Lớp 4: Cây tự nhiên (trees)
    private final ImageView[][] fenceTiles;   // Lớp 5: Hàng rào (fences)
    private final ImageView[][] groundItemTiles; // Lớp 6: Item trên đất (thịt rơi ra)
    private final ImageView[][] statusIconTiles; // Lớp 7: Icon báo hiệu
    private final ImageView[][] statusBackground; // Lớp 8: Mảng chứa nền mờ

    private final ImageManager assetManager; // Để lấy textures
    private final WorldMap worldMap;         // Để biết vẽ tile gì

    private final Pane worldPane;   // Pane "thế giới" chứa lưới, chỉ dùng để di chuyển cuộn mượt
    private final Pane entityPane;  // Pane tĩnh chứa các thực thể động (Animals)
    private final Rectangle tileSelector; // Hình vuông chứa ô được chọn

    // Ghost placement: Hiển thị bóng mờ khi cầm item có thể đặt
    private final ImageView ghostPlacement;

    // Debug: Hitbox collision của cây (chỉ hiển thị khi DEBUG_TREE_HITBOX = true)
    private final Rectangle[][] treeHitboxes;

    // Debug: Hitbox collision của rào (chỉ hiển thị khi DEBUG_FENCE_HITBOX = true)
    private final Rectangle[][] fenceHitboxes;

    // Động vật: Map lưu ImageView cho mỗi con vật (key = Animal object reference)
    private final Map<Animal, ImageView> animalViews;
    private final Map<Animal, ImageView> animalStatusIcons; // Icon trạng thái (đói/sản phẩm)
    private final Map<Animal, ImageView> animalStatusBackgrounds; // Nền icon

    // Lưu lại vị trí render map lần cuối
    private int lastRenderedStartCol = -1;
    private int lastRenderedStartRow = -1;

    public WorldRenderer(ImageManager assetManager, WorldMap worldMap, Pane entityPane) {
        this.assetManager = assetManager;
        this.worldMap = worldMap;
        this.entityPane = entityPane;

        // Khởi tạo tất cả các lớp
        this.baseTiles = new ImageView[WorldConfig.NUM_ROWS_ON_SCREEN][WorldConfig.NUM_COLS_ON_SCREEN];
        this.overlayTiles = new ImageView[WorldConfig.NUM_ROWS_ON_SCREEN][WorldConfig.NUM_COLS_ON_SCREEN];
        this.cropTiles = new ImageView[WorldConfig.NUM_ROWS_ON_SCREEN][WorldConfig.NUM_COLS_ON_SCREEN];
        this.treeTiles = new ImageView[WorldConfig.NUM_ROWS_ON_SCREEN][WorldConfig.NUM_COLS_ON_SCREEN];
        this.fenceTiles = new ImageView[WorldConfig.NUM_ROWS_ON_SCREEN][WorldConfig.NUM_COLS_ON_SCREEN];
        this.groundItemTiles = new ImageView[WorldConfig.NUM_ROWS_ON_SCREEN][WorldConfig.NUM_COLS_ON_SCREEN];
        this.statusIconTiles = new ImageView[WorldConfig.NUM_ROWS_ON_SCREEN][WorldConfig.NUM_COLS_ON_SCREEN];
        this.statusBackground = new ImageView[WorldConfig.NUM_ROWS_ON_SCREEN][WorldConfig.NUM_COLS_ON_SCREEN];

        this.treeHitboxes = new Rectangle[WorldConfig.NUM_ROWS_ON_SCREEN][WorldConfig.NUM_COLS_ON_SCREEN];
        this.fenceHitboxes = new Rectangle[WorldConfig.NUM_ROWS_ON_SCREEN][WorldConfig.NUM_COLS_ON_SCREEN];

        this.animalViews = new HashMap<>();
        this.animalStatusIcons = new HashMap<>();
        this.animalStatusBackgrounds = new HashMap<>();

        this.worldPane = new Pane();

        // --- KHỞI TẠO ĐỐI TƯỢNG (Chưa add vào Pane) ---
        for (int r = 0; r < WorldConfig.NUM_ROWS_ON_SCREEN; r++) {
            for (int c = 0; c < WorldConfig.NUM_COLS_ON_SCREEN; c++) {
                baseTiles[r][c] = createTileView(c, r, 0, WorldConfig.TILE_SIZE, WorldConfig.TILE_SIZE);
                overlayTiles[r][c] = createTileView(c, r, 0, WorldConfig.TILE_SIZE, WorldConfig.TILE_SIZE);
                cropTiles[r][c] = createTileView(c, r, -CropConfig.CROP_Y_OFFSET, CropConfig.CROP_SPRITE_WIDTH, CropConfig.CROP_SPRITE_HEIGHT);
                treeTiles[r][c] = createTileView(c, r, -TreeConfig.TREE_Y_OFFSET, TreeConfig.TREE_SPRITE_WIDTH, TreeConfig.TREE_SPRITE_HEIGHT);
                fenceTiles[r][c] = createTileView(c, r, -FenceConfig.FENCE_Y_OFFSET, FenceConfig.FENCE_SPRITE_WIDTH, FenceConfig.FENCE_SPRITE_HEIGHT);

                groundItemTiles[r][c] = createTileView(c, r, 0, ItemSpriteConfig.ITEM_SPRITE_WIDTH, ItemSpriteConfig.ITEM_SPRITE_HEIGHT);

                // Status Background
                ImageView bg = createTileView(c, r, -HudConfig.ICON_Y_OFFSET, HudConfig.ICON_BG_SIZE, HudConfig.ICON_BG_SIZE);
                bg.setImage(assetManager.getIconBG());
                bg.setVisible(false);
                bg.setLayoutX(c * WorldConfig.TILE_SIZE);
                statusBackground[r][c] = bg;

                // Status Icon
                ImageView icon = createTileView(c, r, -HudConfig.ICON_Y_OFFSET, HudConfig.ICON_SIZE, HudConfig.ICON_SIZE);
                double iconOffset = (HudConfig.ICON_BG_SIZE - HudConfig.ICON_SIZE) / 2;
                icon.setLayoutX(c * WorldConfig.TILE_SIZE + iconOffset);
                icon.setLayoutY(r * WorldConfig.TILE_SIZE - HudConfig.ICON_Y_OFFSET + HudConfig.ICON_PADDING_TOP);
                statusIconTiles[r][c] = icon;

                // Debug Hitboxes
                if (TreeConfig.DEBUG_TREE_HITBOX && PlayerSpriteConfig.DEBUG_PLAYER_BOUNDS) {
                    Rectangle treeHitbox = new Rectangle(TreeConfig.TREE_HITBOX_WIDTH, TreeConfig.TREE_HITBOX_HEIGHT);
                    treeHitbox.setFill(null);
                    treeHitbox.setStroke(TreeConfig.DEBUG_TREE_HITBOX_COLOR);
                    treeHitbox.setStrokeWidth(2.0);
                    treeHitbox.setMouseTransparent(true);
                    treeHitbox.setVisible(false);
                    treeHitboxes[r][c] = treeHitbox;
                } else {
                    treeHitboxes[r][c] = null;
                }

                if (FenceConfig.DEBUG_FENCE_HITBOX && PlayerSpriteConfig.DEBUG_PLAYER_BOUNDS) {
                    Rectangle fenceHitbox = new Rectangle(FenceConfig.FENCE_HITBOX_WIDTH, FenceConfig.FENCE_HITBOX_HEIGHT);
                    fenceHitbox.setFill(null);
                    fenceHitbox.setStroke(FenceConfig.DEBUG_FENCE_HITBOX_COLOR);
                    fenceHitbox.setStrokeWidth(2.0);
                    fenceHitbox.setMouseTransparent(true);
                    fenceHitbox.setVisible(false);
                    fenceHitboxes[r][c] = fenceHitbox;
                } else {
                    fenceHitboxes[r][c] = null;
                }
            }
        }

        // --- ADD VÀO PANE THEO LỚP (Z-ORDERING FIX) ---
        // [QUAN TRỌNG] Add theo từng vòng lặp riêng biệt để đảm bảo lớp trên luôn đè lên lớp dưới của TOÀN BỘ MAP.

        // Layer 1: Môi trường (Đất, Cây, Rào...)
        for (int r = 0; r < WorldConfig.NUM_ROWS_ON_SCREEN; r++) {
            for (int c = 0; c < WorldConfig.NUM_COLS_ON_SCREEN; c++) {
                worldPane.getChildren().addAll(
                        baseTiles[r][c],
                        overlayTiles[r][c],
                        cropTiles[r][c],
                        treeTiles[r][c],
                        fenceTiles[r][c]
                );
            }
        }

        // Layer 2: Item trên đất (Sẽ luôn nằm trên cây cối của hàng dưới)
        for (int r = 0; r < WorldConfig.NUM_ROWS_ON_SCREEN; r++) {
            for (int c = 0; c < WorldConfig.NUM_COLS_ON_SCREEN; c++) {
                worldPane.getChildren().add(groundItemTiles[r][c]);
            }
        }

        // Layer 3: UI và Debug (Trên cùng)
        for (int r = 0; r < WorldConfig.NUM_ROWS_ON_SCREEN; r++) {
            for (int c = 0; c < WorldConfig.NUM_COLS_ON_SCREEN; c++) {
                worldPane.getChildren().addAll(
                        statusBackground[r][c],
                        statusIconTiles[r][c]
                );

                if (treeHitboxes[r][c] != null) worldPane.getChildren().add(treeHitboxes[r][c]);
                if (fenceHitboxes[r][c] != null) worldPane.getChildren().add(fenceHitboxes[r][c]);
            }
        }

        // Selector và Ghost
        this.tileSelector = new Rectangle(WorldConfig.TILE_SIZE, WorldConfig.TILE_SIZE);
        this.tileSelector.setFill(null);
        this.tileSelector.setStroke(WorldConfig.SELECTOR_COLOR);
        this.tileSelector.setStrokeWidth(WorldConfig.SELECTOR_STROKE_WIDTH);
        this.tileSelector.setVisible(true);

        this.ghostPlacement = new ImageView();
        this.ghostPlacement.setMouseTransparent(true);
        this.ghostPlacement.setVisible(false);
        this.ghostPlacement.setOpacity(WorldConfig.GHOST_PLACEMENT_OPACITY);
    }

    private ImageView createTileView(int c, int r, double yOffset, double width, double height) {
        ImageView tileView = new ImageView();
        tileView.setFitWidth(width);
        tileView.setFitHeight(height);
        tileView.setPreserveRatio(false);
        tileView.setLayoutX(c * WorldConfig.TILE_SIZE);
        tileView.setLayoutY(r * WorldConfig.TILE_SIZE + yOffset);
        tileView.setSmooth(false);
        return tileView;
    }

    public void updateMap(double worldOffsetX, double worldOffsetY, boolean forceRedraw) {
        double cameraWorldX = -worldOffsetX;
        double cameraWorldY = -worldOffsetY;

        int startCol = (int) Math.floor(cameraWorldX / WorldConfig.TILE_SIZE);
        int startRow = (int) Math.floor(cameraWorldY / WorldConfig.TILE_SIZE);

        double pixelOffsetX = -(cameraWorldX - (startCol * WorldConfig.TILE_SIZE));
        double pixelOffsetY = -(cameraWorldY - (startRow * WorldConfig.TILE_SIZE));

        worldPane.setLayoutX(pixelOffsetX);
        worldPane.setLayoutY(pixelOffsetY);

        boolean needsTileUpdate = (startCol != lastRenderedStartCol ||
                startRow != lastRenderedStartRow ||
                forceRedraw);

        if (!needsTileUpdate) {
            return;
        }

        for (int r = 0; r < WorldConfig.NUM_ROWS_ON_SCREEN; r++) {
            for (int c = 0; c < WorldConfig.NUM_COLS_ON_SCREEN; c++) {
                int logicalCol = startCol + c;
                int logicalRow = startRow + r;
                TileData data = worldMap.getTileData(logicalCol, logicalRow);

                Tile type = data.getBaseTileType();
                Tile baseType = (type == Tile.TREE || type == Tile.FENCE) ? Tile.GRASS : type;
                this.baseTiles[r][c].setImage(assetManager.getTileTexture(baseType));

                this.overlayTiles[r][c].setImage(data.isFertilized() ? assetManager.getFertilizerTexture() : null);
                this.cropTiles[r][c].setImage(assetManager.getCropTexture(data.getCropData()));
                this.treeTiles[r][c].setImage(assetManager.getTreeTexture(data.getTreeData()));

                if (data.getGroundItem() != null && data.getGroundItemAmount() > 0) {
                    Image itemTexture = assetManager.getItemIcon(data.getGroundItem());
                    this.groundItemTiles[r][c].setImage(itemTexture);
                    this.groundItemTiles[r][c].setTranslateX(data.getGroundItemOffsetX());
                    this.groundItemTiles[r][c].setTranslateY(data.getGroundItemOffsetY());
                    this.groundItemTiles[r][c].setVisible(true);
                } else {
                    this.groundItemTiles[r][c].setImage(null);
                    this.groundItemTiles[r][c].setVisible(false);
                }

                if (TreeConfig.DEBUG_TREE_HITBOX && PlayerSpriteConfig.DEBUG_PLAYER_BOUNDS && treeHitboxes[r][c] != null) {
                    if (data.getTreeData() != null && data.getTreeData().getGrowthStage() > 0) {
                        double tileLocalX = c * WorldConfig.TILE_SIZE;
                        double tileLocalY = r * WorldConfig.TILE_SIZE;
                        double layoutX = tileLocalX + (WorldConfig.TILE_SIZE - TreeConfig.TREE_HITBOX_WIDTH) / 2.0;
                        double visualTreeBottomY = (tileLocalY + WorldConfig.TILE_SIZE) - CropConfig.CROP_Y_OFFSET;
                        double layoutY = visualTreeBottomY - TreeConfig.TREE_HITBOX_HEIGHT - TreeConfig.TREE_HITBOX_Y_OFFSET_FROM_BOTTOM;

                        treeHitboxes[r][c].setLayoutX(layoutX);
                        treeHitboxes[r][c].setLayoutY(layoutY);
                        treeHitboxes[r][c].setVisible(true);
                    } else {
                        treeHitboxes[r][c].setVisible(false);
                    }
                }

                this.fenceTiles[r][c].setImage(assetManager.getFenceTexture(data.getFenceData()));

                if (FenceConfig.DEBUG_FENCE_HITBOX && PlayerSpriteConfig.DEBUG_PLAYER_BOUNDS && fenceHitboxes[r][c] != null) {
                    if (data.getFenceData() != null && data.getFenceData().isSolid()) {
                        double tileLocalX = c * WorldConfig.TILE_SIZE;
                        double tileLocalY = r * WorldConfig.TILE_SIZE;
                        double layoutX = tileLocalX + (WorldConfig.TILE_SIZE - FenceConfig.FENCE_HITBOX_WIDTH) / 2.0;
                        double layoutY = (tileLocalY + WorldConfig.TILE_SIZE) - FenceConfig.FENCE_HITBOX_HEIGHT - FenceConfig.FENCE_HITBOX_Y_OFFSET_FROM_BOTTOM;

                        fenceHitboxes[r][c].setLayoutX(layoutX);
                        fenceHitboxes[r][c].setLayoutY(layoutY);
                        fenceHitboxes[r][c].setVisible(true);
                    } else {
                        fenceHitboxes[r][c].setVisible(false);
                    }
                }

                // UI Icons
                Image statusIcon = assetManager.getStatusIcon(data.getStatusIndicator());
                if (data.getStatusIndicator() == com.example.farmSimulation.model.CropStatusIndicator.NEED_WATER_AND_FERTILIZER) {
                    double doubleWidth = HudConfig.ICON_SIZE * 2;
                    this.statusIconTiles[r][c].setFitWidth(doubleWidth);
                    double iconOffset = (HudConfig.ICON_BG_SIZE - doubleWidth) / 2;
                    this.statusIconTiles[r][c].setLayoutX((c) * WorldConfig.TILE_SIZE + iconOffset);
                } else {
                    this.statusIconTiles[r][c].setFitWidth(HudConfig.ICON_SIZE);
                    double iconOffset = (HudConfig.ICON_BG_SIZE - HudConfig.ICON_SIZE) / 2;
                    this.statusIconTiles[r][c].setLayoutX((c) * WorldConfig.TILE_SIZE + iconOffset);
                }
                this.statusIconTiles[r][c].setImage(statusIcon);
                this.statusBackground[r][c].setVisible(statusIcon != null);
                this.statusBackground[r][c].setLayoutX((c) * WorldConfig.TILE_SIZE);
            }
        }
        this.lastRenderedStartCol = startCol;
        this.lastRenderedStartRow = startRow;
    }

    public void updateSelector(int tileSelectedX, int tileSelectedY, double worldOffsetX, double worldOffsetY) {
        if (this.tileSelector == null) return;
        double tileSelectedOnScreenX = tileSelectedX * WorldConfig.TILE_SIZE + worldOffsetX;
        double tileSelectedOnScreenY = tileSelectedY * WorldConfig.TILE_SIZE + worldOffsetY;
        this.tileSelector.setLayoutX(tileSelectedOnScreenX);
        this.tileSelector.setLayoutY(tileSelectedOnScreenY);
    }

    public void updateGhostPlacement(int tileX, int tileY, double worldOffsetX, double worldOffsetY, com.example.farmSimulation.model.ItemStack currentItem) {
        boolean shouldShow = false;
        Image ghostImage = null;
        double yOffsetCorrection = 0.0;

        if (currentItem != null) {
            com.example.farmSimulation.model.ItemType itemType = currentItem.getItemType();
            if (itemType == com.example.farmSimulation.model.ItemType.WOOD) {
                ghostImage = assetManager.getFenceTexture(new com.example.farmSimulation.model.FenceData(false));
                shouldShow = true;
                yOffsetCorrection = FenceConfig.FENCE_Y_OFFSET;
            } else if (itemType.name().startsWith("SEEDS_")) {
                // SEEDS_TREE: Sử dụng tree seed icon
                if (itemType == com.example.farmSimulation.model.ItemType.SEEDS_TREE) {
                    ghostImage = assetManager.getTreeSeedIcon();
                    shouldShow = true;
                    yOffsetCorrection = TreeConfig.TREE_Y_OFFSET;
                } else {
                    // Các loại hạt giống cây trồng khác
                    try {
                        com.example.farmSimulation.model.CropType cropType = com.example.farmSimulation.model.CropType.valueOf(itemType.name().substring(6));
                        ghostImage = assetManager.getSeedIcon(cropType);
                        shouldShow = true;
                        yOffsetCorrection = CropConfig.CROP_Y_OFFSET;
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }

        if (shouldShow && ghostImage != null) {
            double screenX = tileX * WorldConfig.TILE_SIZE + worldOffsetX;
            double screenY = tileY * WorldConfig.TILE_SIZE + worldOffsetY;
            double imageWidth = ghostImage.getWidth();
            double imageHeight = ghostImage.getHeight();
            double offsetX = (WorldConfig.TILE_SIZE - imageWidth) / 2.0;

            // [SỬA] Đối với SEEDS_TREE, sử dụng cùng logic positioning như treeTiles
            // treeTiles được tạo với yOffset = -TreeConfig.TREE_Y_OFFSET trong createTileView
            // createTileView set LayoutY = r * WorldConfig.TILE_SIZE + yOffset
            // Vậy treeTile LayoutY = tileY * WorldConfig.TILE_SIZE - TreeConfig.TREE_Y_OFFSET (trong worldPane coordinates)
            // Ghost placement cần match chính xác điều này
            double offsetY;
            if (currentItem != null && currentItem.getItemType() == com.example.farmSimulation.model.ItemType.SEEDS_TREE) {
                // Match treeTiles positioning: LayoutY = tileY * WorldConfig.TILE_SIZE - TreeConfig.TREE_Y_OFFSET
                // screenY đã là tileY * WorldConfig.TILE_SIZE + worldOffsetY
                // Cần trừ TreeConfig.TREE_Y_OFFSET để match treeTiles
                offsetY = -TreeConfig.TREE_Y_OFFSET;
            } else {
                // Các item khác dùng logic center vertically
                offsetY = (WorldConfig.TILE_SIZE - imageHeight) / 2.0 - yOffsetCorrection;
            }

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

    public void updateAnimals(java.util.List<Animal> animals, double worldOffsetX, double worldOffsetY) {
        java.util.List<Animal> toRemove = new java.util.ArrayList<>();
        for (Animal animal : animalViews.keySet()) {
            if (!animals.contains(animal) || animal.isDead()) {
                toRemove.add(animal);
            }
        }
        for (Animal animal : toRemove) {
            ImageView view = animalViews.remove(animal);
            ImageView icon = animalStatusIcons.remove(animal);
            ImageView bg = animalStatusBackgrounds.remove(animal);
            if (view != null) entityPane.getChildren().remove(view);
            if (icon != null) entityPane.getChildren().remove(icon);
            if (bg != null) entityPane.getChildren().remove(bg);
        }

        // [MỚI] Lấy thời gian hiện tại để tính frame animation chung cho tất cả động vật
        long now = System.currentTimeMillis();

        for (Animal animal : animals) {
            if (animal.isDead()) continue;
            ImageView animalView = animalViews.get(animal);
            if (animalView == null) {
                animalView = new ImageView();
                animalView.setSmooth(false);
                animalView.setPreserveRatio(true);
                animalView.setMouseTransparent(true);
                animalViews.put(animal, animalView);
                entityPane.getChildren().add(animalView);
            }

            // [SỬA] Tính toán frameIndex dựa trên thời gian và cấu hình
            int frameIndex = 0;

            // Xử lý đặc biệt cho TRỨNG: Không animate theo thời gian, mà dùng variant cố định
            if (animal.getType() == com.example.farmSimulation.model.AnimalType.EGG_ENTITY) {
                // [SỬA] Trứng nằm ở cột 4 và 5 trong hàng (tính theo index 0-based)
                // Cần cộng thêm Offset để trỏ đúng frame
                frameIndex = AnimalConfig.EGG_FRAME_START_INDEX + animal.getVariant();
            }
            // Xử lý cho các động vật khác: Animate theo thời gian
            else {
                int frameCount = 1;
                int animationSpeedMs = 200; // Default fallback

                if (animal.getType() == com.example.farmSimulation.model.AnimalType.CHICKEN) {
                    if (animal.getCurrentAction() == Animal.Action.WALK) {
                        frameCount = AnimalConfig.CHICKEN_WALK_FRAMES;
                        animationSpeedMs = AnimalConfig.ANIM_SPEED_CHICKEN_WALK; // Dùng config mới
                    } else {
                        frameCount = AnimalConfig.CHICKEN_IDLE_FRAMES;
                        animationSpeedMs = AnimalConfig.ANIM_SPEED_CHICKEN_IDLE; // Dùng config mới
                    }
                } else { // Các động vật chuẩn (Bò, Cừu, Lợn, GÀ CON)
                    // Gà con (Baby Chicken) dùng layout chuẩn: Walk 6 frames, Idle 4 frames
                    if (animal.getCurrentAction() == Animal.Action.WALK) {
                        frameCount = AnimalConfig.STANDARD_WALK_FRAMES;
                        animationSpeedMs = AnimalConfig.ANIM_SPEED_STANDARD_WALK; // Dùng config mới
                    } else {
                        frameCount = AnimalConfig.STANDARD_IDLE_FRAMES;
                        animationSpeedMs = AnimalConfig.ANIM_SPEED_STANDARD_IDLE; // Dùng config mới
                    }
                }

                // Tính frame index: (time / speed) % totalFrames
                frameIndex = (int) ((now / animationSpeedMs) % frameCount);
            }

            Image animalTexture = assetManager.getAnimalTexture(animal.getType(), animal.getDirection(), animal.getCurrentAction(), frameIndex);
            animalView.setImage(animalTexture);
            double spriteSize = animal.getType().getSpriteSize();
            animalView.setFitWidth(spriteSize);
            animalView.setFitHeight(spriteSize);
            double screenX = animal.getX() + worldOffsetX;
            double screenY = animal.getY() + worldOffsetY + AnimalConfig.ANIMAL_Y_OFFSET;
            animalView.setLayoutX(screenX - spriteSize / 2.0);
            animalView.setLayoutY(screenY - spriteSize);
            updateAnimalStatusIcon(animal, worldOffsetX, worldOffsetY);
        }
    }

    private void updateAnimalStatusIcon(Animal animal, double worldOffsetX, double worldOffsetY) {
        ImageView iconView = animalStatusIcons.get(animal);
        ImageView bgView = animalStatusBackgrounds.get(animal);
        boolean needsIcon = false;
        Image iconImage = null;

        if (animal.isHungry()) {
            // Always use SUPER_FEED icon for hunger (universal visual cue)
            iconImage = assetManager.getItemIcon(com.example.farmSimulation.model.ItemType.SUPER_FEED);
            needsIcon = iconImage != null;
        } else if (animal.isHasProduct() && animal.getType().canProduce()) {
            // Display the specific product item icon (Egg, Milk, Wool)
            iconImage = assetManager.getItemIcon(animal.getType().getProduct());
            needsIcon = iconImage != null;
        }

        if (needsIcon && iconImage != null) {
            if (iconView == null) {
                iconView = new ImageView();
                iconView.setSmooth(false);
                iconView.setMouseTransparent(true);
                animalStatusIcons.put(animal, iconView);
                entityPane.getChildren().add(iconView);
            }
            if (bgView == null) {
                bgView = new ImageView();
                bgView.setImage(assetManager.getIconBG());
                bgView.setSmooth(false);
                bgView.setMouseTransparent(true);
                animalStatusBackgrounds.put(animal, bgView);
                entityPane.getChildren().add(bgView);
            }
            iconView.setImage(iconImage);
            iconView.setFitWidth(HudConfig.ICON_SIZE);
            iconView.setFitHeight(HudConfig.ICON_SIZE);
            bgView.setFitWidth(HudConfig.ICON_BG_SIZE);
            bgView.setFitHeight(HudConfig.ICON_BG_SIZE);
            double spriteSize = animal.getType().getSpriteSize();
            double screenX = animal.getX() + worldOffsetX;
            double screenY = animal.getY() + worldOffsetY + AnimalConfig.ANIMAL_Y_OFFSET - spriteSize;
            double iconOffset = (HudConfig.ICON_BG_SIZE - HudConfig.ICON_SIZE) / 2.0;
            bgView.setLayoutX(screenX - HudConfig.ICON_BG_SIZE / 2.0);
            bgView.setLayoutY(screenY - HudConfig.ICON_BG_SIZE - HudConfig.ICON_Y_OFFSET);
            iconView.setLayoutX(screenX - HudConfig.ICON_BG_SIZE / 2.0 + iconOffset);
            iconView.setLayoutY(screenY - HudConfig.ICON_BG_SIZE - HudConfig.ICON_Y_OFFSET + iconOffset);
            iconView.setVisible(true);
            bgView.setVisible(true);
        } else {
            if (iconView != null) iconView.setVisible(false);
            if (bgView != null) bgView.setVisible(false);
        }
    }
}