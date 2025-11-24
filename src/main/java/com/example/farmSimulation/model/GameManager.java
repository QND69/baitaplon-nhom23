package com.example.farmSimulation.model;

import com.example.farmSimulation.config.*;
import com.example.farmSimulation.controller.GameController;
import com.example.farmSimulation.view.MainGameView;
import com.example.farmSimulation.view.PlayerView;
import javafx.animation.AnimationTimer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameManager {
    // --- Các thành phần cốt lõi (Model, View, Controller) ---
    private final Player mainPlayer;
    private final WorldMap worldMap;
    private final MainGameView mainGameView;
    private final PlayerView playerView;
    private final GameController gameController;

    // --- Các Manager con (được tách ra) ---
    private final TimeManager timeManager;
    private final ActionManager actionManager;
    private final PlayerMovementHandler movementHandler;
    private final Camera camera;
    private final InteractionManager interactionManager;
    private final CropManager cropManager;
    private final TreeManager treeManager; // Quản lý cây tự nhiên
    private final FenceManager fenceManager; // Quản lý hàng rào
    private final CollisionManager collisionManager; // Quản lý collision

    // --- Trạng thái Game ---
    private AnimationTimer gameLoop; // Khởi tạo gameLoop
    private boolean isPaused = false;

    // Tọa độ ô chuột đang trỏ tới
    private int currentMouseTileX = 0;
    private int currentMouseTileY = 0;

    // Constructor nhận tất cả các thành phần cốt lõi
    public GameManager(Player player, WorldMap worldMap, MainGameView mainGameView,
                       PlayerView playerView, GameController gameController) {
        this.mainPlayer = player;
        this.worldMap = worldMap;
        this.mainGameView = mainGameView;
        this.playerView = playerView;
        this.gameController = gameController;

        // Khởi tạo các Manager con
        this.camera = new Camera();
        this.timeManager = new TimeManager(mainGameView);
        this.actionManager = new ActionManager(player, playerView); // Truyền player/playerView
        this.movementHandler = new PlayerMovementHandler(player, playerView, gameController, camera, mainGameView);
        this.interactionManager = new InteractionManager(this.actionManager);
        this.cropManager = new CropManager(this.worldMap);
        this.treeManager = new TreeManager(this.worldMap);
        this.fenceManager = new FenceManager(this.worldMap);
        this.collisionManager = new CollisionManager(this.worldMap);
        
        // Liên kết các Manager với nhau
        this.actionManager.setFenceManager(this.fenceManager);
        this.movementHandler.setCollisionManager(this.collisionManager);
    }

    public void startGame() {
        // Đặt vị trí camera ban đầu
        camera.initializePosition(mainPlayer, playerView);

        // Gọi updateMap để vẽ map lần đầu
        mainGameView.updateMap(camera.getWorldOffsetX(), camera.getWorldOffsetY(), true);

        // Bắt đầu game loop
        this.gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateGameLogic(now);
            }
        };
        gameLoop.start();
        System.out.println("Game Started!");
    }

    /**
     * Hàm update chính, chỉ điều phối các hàm con
     */
    private void updateGameLogic(long now) {
        // ⚠️ BỔ SUNG: Dừng ngay lập tức nếu game đang tạm dừng
        if (this.isPaused) {
            return;
        }

        // Cập nhật thời gian & chu kỳ ngày đêm
        //timeManager.update();

        // Cập nhật di chuyển & trạng thái Player
        movementHandler.update();

        // Cập nhật animation (View tự chạy)
        playerView.updateAnimation(); // [QUAN TRỌNG] Yêu cầu PlayerView tự chạy animation

        // Cập nhật các hành động chờ
        actionManager.updateTimedActions(worldMap, mainGameView, camera.getWorldOffsetX(), camera.getWorldOffsetY());

        // Cập nhật logic cây trồng
        boolean cropsUpdated = cropManager.updateCrops(now); // Truyền 'now'
        if (cropsUpdated) {
            actionManager.setMapNeedsUpdate(true); // Báo map cần vẽ lại
        }

        // Cập nhật logic cây tự nhiên
        boolean treesUpdated = treeManager.updateTrees(now, mainPlayer.getTileX(), mainPlayer.getTileY());
        if (treesUpdated) {
            actionManager.setMapNeedsUpdate(true); // Báo map cần vẽ lại
        }

        // Cập nhật chuột
        updateMouseSelector();
        
        // Cập nhật ghost placement
        updateGhostPlacement();
        
        // Cập nhật collision hitbox (debug mode)
        updateCollisionHitbox();
    }

    /**
     * Cập nhật vị trí ô vuông chọn
     */
    private void updateMouseSelector() {
        // Tọa độ logic của chuột trong thế giới
        double mouseWorldX = -camera.getWorldOffsetX() + gameController.getMouseX();
        double mouseWorldY = -camera.getWorldOffsetY() + gameController.getMouseY();

        // Tọa độ logic của ô mà chuột trỏ tới
        this.currentMouseTileX = (int) Math.floor(mouseWorldX / WorldConfig.TILE_SIZE);
        this.currentMouseTileY = (int) Math.floor(mouseWorldY / WorldConfig.TILE_SIZE);

        mainGameView.updateSelector(
                this.currentMouseTileX,       // Vị trí X của ô được chọn
                this.currentMouseTileY,       // Vị trí Y của ô được chọn
                camera.getWorldOffsetX(),     // Vị trí X của thế giới
                camera.getWorldOffsetY()      // Vị trí Y của thế giới
        );
    }
    
    /**
     * Cập nhật ghost placement (bóng mờ khi cầm item có thể đặt)
     */
    private void updateGhostPlacement() {
        ItemStack currentItem = mainPlayer.getCurrentItem();
        mainGameView.updateGhostPlacement(
                this.currentMouseTileX,
                this.currentMouseTileY,
                camera.getWorldOffsetX(),
                camera.getWorldOffsetY(),
                currentItem
        );
    }
    
    /**
     * Cập nhật collision hitbox (debug mode)
     */
    private void updateCollisionHitbox() {
        if (com.example.farmSimulation.config.PlayerSpriteConfig.DEBUG_PLAYER_BOUNDS) {
            mainGameView.updateCollisionHitbox(
                    mainPlayer.getTileX(),
                    mainPlayer.getTileY(),
                    camera.getWorldOffsetX(),
                    camera.getWorldOffsetY(),
                    playerView.getDebugCollisionHitbox()
            );
        }
    }

    /**
     * Kiểm tra xem người chơi có trong tầm tương tác không
     */
    private boolean isPlayerInRange(int col, int row, ItemStack currentStack) {
        // Tọa độ pixel logic của Tâm người chơi, lấy nửa dưới để tính khoảng cách
        double playerX = mainPlayer.getTileX() + PlayerSpriteConfig.BASE_PLAYER_FRAME_WIDTH / 2;
        double playerY = mainPlayer.getTileY() + PlayerSpriteConfig.BASE_PLAYER_FRAME_HEIGHT / 2 + PlayerSpriteConfig.PLAYER_FRAME_WIDTH / 8;

        // Tọa độ pixel logic của TÂM ô target
        double targetX = (col * WorldConfig.TILE_SIZE) + (WorldConfig.TILE_SIZE / 2.0);
        double targetY = (row * WorldConfig.TILE_SIZE) + (WorldConfig.TILE_SIZE / 2.0);

        // Tính khoảng cách
        double distance = Math.sqrt(
                Math.pow(playerX - targetX, 2) + Math.pow(playerY - targetY, 2)
        );

        // Mặc định là HAND range
        double range = GameLogicConfig.HAND_INTERACTION_RANGE;

        // Lấy tầm tương tác dựa trên công cụ
        if (currentStack != null) {
            ItemType type = currentStack.getItemType();

            if (type == ItemType.HOE) {
                range = GameLogicConfig.HOE_INTERACTION_RANGE;
            } else if (type == ItemType.WATERING_CAN) {
                range = GameLogicConfig.WATERING_CAN_INTERACTION_RANGE;
            } else if (type == ItemType.PICKAXE) {
                range = GameLogicConfig.PICKAXE_INTERACTION_RANGE;
            } else if (type == ItemType.SHOVEL) {
                range = GameLogicConfig.SHOVEL_INTERACTION_RANGE;
            } else if (type == ItemType.FERTILIZER) {
                range = GameLogicConfig.FERTILIZER_INTERACTION_RANGE;
            } else if (type.name().startsWith("SEEDS_")) {
                range = GameLogicConfig.PLANT_INTERACTION_RANGE; // Áp dụng cho tất cả loại hạt
            } else {
                // Các item khác dùng mặc định HAND range
                range = GameLogicConfig.HAND_INTERACTION_RANGE;
            }
        }

        return distance <= range;
    }

    /**
     * Quay hướng người chơi về phía ô (col, row)
     */
    private void updatePlayerDirectionTowards(int col, int row) {
        // Tọa độ pixel logic của Tâm người chơi, lấy nửa dưới để tính hướng
        double playerX = mainPlayer.getTileX() + PlayerSpriteConfig.BASE_PLAYER_FRAME_WIDTH / 2;
        double playerY = mainPlayer.getTileY() + PlayerSpriteConfig.BASE_PLAYER_FRAME_HEIGHT / 2 + PlayerSpriteConfig.PLAYER_FRAME_WIDTH / 8;

        // Tọa độ pixel logic của TÂM ô target
        double targetX = (col * WorldConfig.TILE_SIZE) + (WorldConfig.TILE_SIZE / 2.0);
        double targetY = (row * WorldConfig.TILE_SIZE) + (WorldConfig.TILE_SIZE / 2.0);

        // Tính góc (atan2(y, x) = góc giữa vector (x, y) và trục X dương)
        double angleDeg = Math.toDegrees(Math.atan2(targetY - playerY, targetX - playerX));

        // Quyết định hướng theo trục Oy ngược
        // -45 đến 45 = RIGHT
        // 45 đến 135 = DOWN
        // 135 đến 180 hoặc -180 đến -135 = LEFT
        // -135 đến -45 = UP
        if (angleDeg > -45 && angleDeg <= 45) {
            mainPlayer.setDirection(PlayerView.Direction.RIGHT);
        } else if (angleDeg > 45 && angleDeg <= 135) {
            mainPlayer.setDirection(PlayerView.Direction.DOWN);
        } else if (angleDeg > 135 || angleDeg < -135) {
            mainPlayer.setDirection(PlayerView.Direction.LEFT);
        } else { // (-135 đến -45)
            mainPlayer.setDirection(PlayerView.Direction.UP);
        }
    }

    /**
     * Xử lý click chuột
     * Hàm này chỉ kiểm tra điều kiện chung và ủy quyền
     */
    public void interactWithTile(int col, int row) {
        // Kiểm tra xem player có đang bận (làm hành động khác) không
        PlayerView.PlayerState currentState = mainPlayer.getState();
        if (currentState != PlayerView.PlayerState.IDLE && currentState != PlayerView.PlayerState.WALK) {
            return; // Player đang bận, không cho hành động
        }

        // Lấy công cụ *trước* khi kiểm tra tầm
        ItemStack currentStack = mainPlayer.getCurrentItem();

        // Kiểm tra tầm hoạt động (Sử dụng hàm isPlayerInRange)
        if (!isPlayerInRange(col, row, currentStack)) {
            // Vị trí hiển thị của text
            double playerScreenX = playerView.getSpriteContainer().getLayoutX();
            double playerScreenY = playerView.getSpriteContainer().getLayoutY() + PlayerSpriteConfig.PLAYER_SPRITE_OFFSET_Y;

            // Hiển thị text "It's too far"
            mainGameView.showTemporaryText(HudConfig.TOO_FAR_TEXT, playerScreenX, playerScreenY);
            return; // Quá xa, không làm gì cả
        }

        // Quay người chơi về hướng ô target
        updatePlayerDirectionTowards(col, row);

        // Nhận thông báo lỗi trực tiếp từ hàm processInteraction
        String errorMsg = interactionManager.processInteraction(mainPlayer, playerView, worldMap, col, row);

        // Nếu errorMsg != null nghĩa là có lỗi hoặc không hành động được -> Hiển thị text
        if (errorMsg != null) {
            double playerScreenX = playerView.getSpriteContainer().getLayoutX();
            double playerScreenY = playerView.getSpriteContainer().getLayoutY() + PlayerSpriteConfig.PLAYER_SPRITE_OFFSET_Y;
            mainGameView.showTemporaryText(errorMsg, playerScreenX, playerScreenY);
        }
    }
    /**
     * Được gọi từ Controller khi người dùng đổi slot hotbar
     */
    public void changeHotbarSlot(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < HotbarConfig.HOTBAR_SLOT_COUNT) {
            mainPlayer.setSelectedHotbarSlot(slotIndex);
            mainGameView.updateHotbar(); // Yêu cầu View vẽ lại
        }
    }

    /**
     * Được gọi từ HotbarView khi người dùng kéo thả item
     */
    public void swapHotbarItems(int indexA, int indexB) {
        mainPlayer.swapHotbarItems(indexA, indexB);
        mainGameView.updateHotbar(); // Cập nhật lại giao diện ngay
    }

    /**
     * Mở/đóng hàng rào (được gọi từ Controller khi click chuột phải)
     */
    public void toggleFence(int col, int row) {
        // [SỬA] Thêm kiểm tra tầm hoạt động bằng Tay (Hand)
        // Truyền null vào currentStack để sử dụng HAND_INTERACTION_RANGE mặc định
        if (!isPlayerInRange(col, row, null)) {
            // Hiển thị thông báo "Xa quá"
            double playerScreenX = playerView.getSpriteContainer().getLayoutX();
            double playerScreenY = playerView.getSpriteContainer().getLayoutY() + PlayerSpriteConfig.PLAYER_SPRITE_OFFSET_Y;
            mainGameView.showTemporaryText(HudConfig.TOO_FAR_TEXT, playerScreenX, playerScreenY);
            return; 
        }

        TileData data = worldMap.getTileData(col, row);
        if (data.getFenceData() != null) {
            fenceManager.toggleFence(col, row);
            actionManager.setMapNeedsUpdate(true);
            mainGameView.updateMap(camera.getWorldOffsetX(), camera.getWorldOffsetY(), true);
        }
    }

    public void toggleSettingsMenu() {
        this.isPaused = !this.isPaused; // Sử dụng this.isPaused
        if (this.isPaused) {
            if (gameLoop != null) {
                gameLoop.stop(); // ⬅️ Dừng game loop
                //System.out.println("Game Loop đã dừng.");
            }
            mainGameView.showSettingsMenu(mainPlayer.getName(), mainPlayer.getLevel());
        } else {
            if (gameLoop != null) {
                gameLoop.start(); // ⬅️ Tiếp tục game loop
                //System.out.println("Game Loop đã tiếp tục.");
            }
            mainGameView.hideSettingsMenu();
        }
    }
}