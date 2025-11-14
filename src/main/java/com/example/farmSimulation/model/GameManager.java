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
                updateGameLogic();
            }
        };
        gameLoop.start();
        System.out.println("Game Started!");
    }

    /**
     * Hàm update chính, chỉ điều phối các hàm con
     */
    private void updateGameLogic() {
        // ⚠️ BỔ SUNG: Dừng ngay lập tức nếu game đang tạm dừng
        if (this.isPaused) {
            return;
        }

        // Cập nhật thời gian & chu kỳ ngày đêm
        timeManager.update();

        // Cập nhật di chuyển & trạng thái Player
        movementHandler.update();

        // Cập nhật animation (View tự chạy)
        playerView.updateAnimation(); // [QUAN TRỌNG] Yêu cầu PlayerView tự chạy animation

        // Cập nhật các hành động chờ
        actionManager.updateTimedActions(worldMap, mainGameView, camera.getWorldOffsetX(), camera.getWorldOffsetY());

        // Cập nhật chuột
        updateMouseSelector();
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
     * Kiểm tra xem người chơi có trong tầm tương tác không
     */
    private boolean isPlayerInRange(int col, int row, Tool tool) {
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

        // Lấy tầm tương tác dựa trên công cụ
        double range;
        switch (tool) {
            case HOE:
                range = GameLogicConfig.HOE_INTERACTION_RANGE;
                break;
            case WATERING_CAN:
                range = GameLogicConfig.WATERING_CAN_INTERACTION_RANGE;
                break;
            case PICKAXE:
                range = GameLogicConfig.PICKAXE_INTERACTION_RANGE;
                break;
            case SHOVEL:
                range = GameLogicConfig.SHOVEL_INTERACTION_RANGE;
                break;
            case HAND:
            default:
                range = GameLogicConfig.HAND_INTERACTION_RANGE;
                break;
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
        Tool currentTool = mainPlayer.getCurrentTool();

        // Kiểm tra tầm hoạt động (Sử dụng hàm isPlayerInRange)
        if (!isPlayerInRange(col, row, currentTool)) {
            // Vị trí hiển thị của text
            double playerScreenX = playerView.getSpriteContainer().getLayoutX();
            double playerScreenY = playerView.getSpriteContainer().getLayoutY() + PlayerSpriteConfig.PLAYER_SPRITE_OFFSET_Y;

            // Hiển thị text "It's too far"
            mainGameView.showTemporaryText(HudConfig.TOO_FAR_TEXT, playerScreenX, playerScreenY);
            return; // Quá xa, không làm gì cả
        }

        // Quay người chơi về hướng ô target
        updatePlayerDirectionTowards(col, row);

        // Ủy thác logic xử lý cho InteractionManager
        boolean actionTaken = interactionManager.processInteraction(
                mainPlayer,
                playerView,
                worldMap,
                col,
                row
        );

        // Xử lý nếu không có quy tắc nào khớp (dùng sai tool)
        if (!actionTaken) {
            // Vị trí hiển thị của text
            double playerScreenX = playerView.getSpriteContainer().getLayoutX();
            double playerScreenY = playerView.getSpriteContainer().getLayoutY() + PlayerSpriteConfig.PLAYER_SPRITE_OFFSET_Y;

            // Hiển thị text "You need the right tool"
            mainGameView.showTemporaryText(HudConfig.WRONG_TOOL_TEXT, playerScreenX, playerScreenY);
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