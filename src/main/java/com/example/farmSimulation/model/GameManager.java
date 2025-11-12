package com.example.farmSimulation.model;

import com.example.farmSimulation.config.GameConfig;
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

    // --- Trạng thái Game ---
    private AnimationTimer gameLoop; // Khởi tạo gameLoop
    private boolean isPaused = false;

    // Tọa độ ô chuột đang trỏ tới
    private int currentMouseTileX = 0;
    private int currentMouseTileY = 0;

    // Constructor nhận tất cả các thành phần
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
        this.actionManager = new ActionManager();
        this.movementHandler = new PlayerMovementHandler(player, playerView, gameController, camera, mainGameView);
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
     * [TỐI ƯU] Hàm update chính, chỉ điều phối các hàm con
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
        this.currentMouseTileX = (int) Math.floor(mouseWorldX / GameConfig.TILE_SIZE);
        this.currentMouseTileY = (int) Math.floor(mouseWorldY / GameConfig.TILE_SIZE);

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
    private boolean isPlayerInRange(int col, int row) {
        // Tọa độ pixel logic của Tâm người chơi
        double playerX = mainPlayer.getTileX() + GameConfig.PLAYER_FRAME_WIDTH / 2;
        double playerY = mainPlayer.getTileY() + GameConfig.PLAYER_FRAME_HEIGHT * 3 / 4;

        // Tọa độ pixel logic của TÂM ô target
        double targetX = (col * GameConfig.TILE_SIZE) + (GameConfig.TILE_SIZE / 2.0);
        double targetY = (row * GameConfig.TILE_SIZE) + (GameConfig.TILE_SIZE / 2.0);

        // Tính khoảng cách
        double distance = Math.sqrt(
                Math.pow(playerX - targetX, 2) + Math.pow(playerY - targetY, 2)
        );

        return distance <= GameConfig.PLAYER_INTERACTION_RANGE_PIXELS;
    }

    /**
     * Xử lý click chuột
     * [TỐI ƯU] Sẵn sàng cho Tool System
     * [TỐI ƯU] Thêm kiểm tra tầm hoạt động và dùng config
     */
    public void interactWithTile(int col, int row) {
        // Kiểm tra tầm hoạt động trước
        if (!isPlayerInRange(col, row)) {
            // Hiển thị text "It's too far"
            double playerScreenX = playerView.getSprite().getLayoutX() + playerView.getWidth() / 2;
            double playerScreenY = playerView.getSprite().getLayoutY(); // Đầu player
            mainGameView.showTemporaryText(GameConfig.TOO_FAR_TEXT, playerScreenX, playerScreenY);
            return; // Quá xa, không làm gì cả
        }

        Tile currentType = worldMap.getTileType(col, row);

        // --- ÁP DỤNG "LUẬT CHƠI" ---

        // TODO: Sửa logic này khi có Hệ thống Dụng cụ (Tool System)
        // PlayerTool tool = mainPlayer.getSelectedTool();
        // if (tool == PlayerTool.HOE && currentType == Tile.GRASS) { ... }

        // VÍ DỤ: Cuốc đất (Grass -> Soil)
        if (currentType == Tile.GRASS) {
            // Đặt độ trễ là 1 frame (hoặc 0 nếu muốn tức thì)
            // [TỐI ƯU] Lấy giá trị từ GameConfig
            int delayInFrames = GameConfig.ACTION_DELAY_FRAMES_HOE;

            // Thêm hành động "Biến thành Đất" vào hàng đợi
            actionManager.addPendingAction(new TimedTileAction(col, row, Tile.SOIL, delayInFrames));

            // Ra lệnh cho PlayerView chạy animation "Cuốc"
            // (Hiện tại chưa làm, chỉ là ví dụ)
            // mainPlayer.setState(PlayerView.PlayerState.HOE);
        }
    }

    public void toggleSettingsMenu() {
        this.isPaused = !this.isPaused; // Sử dụng this.isPaused
        if (this.isPaused) {
            if (gameLoop != null) {
                gameLoop.stop(); // ⬅️ Dừng game loop
                System.out.println("Game Loop đã dừng.");
            }
            mainGameView.showSettingsMenu(mainPlayer.getName(), mainPlayer.getLevel());
        } else {
            if (gameLoop != null) {
                gameLoop.start(); // ⬅️ Tiếp tục game loop
                System.out.println("Game Loop đã tiếp tục.");
            }
            mainGameView.hideSettingsMenu();
        }
    }
}