package com.example.farmSimulation.model;

import com.example.farmSimulation.config.GameConfig;
import com.example.farmSimulation.controller.GameController;
import com.example.farmSimulation.view.MainGameView;
import com.example.farmSimulation.view.PlayerView;
import javafx.animation.AnimationTimer;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Getter
@Setter
public class GameManager {
    private final Player mainPlayer;
    private final WorldMap worldMap;
    private final MainGameView mainGameView;
    private final PlayerView playerView;
    private final GameController gameController;
    private final List<TimedTileAction> pendingActions;  // Thêm danh sách hành động chờ
    private AnimationTimer gameLoop; // Khởi tạo gameLoop

    // Tọa độ thế giới logic
    private double worldOffsetX = 0.0;
    private double worldOffsetY = 0.0;

    // Tọa độ ô chuột đang trỏ tới
    private int currentMouseTileX = 0;
    private int currentMouseTileY = 0;
    private boolean mapNeedsUpdate = false;

    // Constructor nhận tất cả các thành phần
    public GameManager(Player player, WorldMap worldMap, MainGameView mainGameView,
                       PlayerView playerView, GameController gameController) {
        this.mainPlayer = player;
        this.worldMap = worldMap;
        this.mainGameView = mainGameView;
        this.playerView = playerView;
        this.gameController = gameController;
        this.pendingActions = new ArrayList<>();
    }

    public void startGame() {
        // *** Đặt vị trí khởi đầu của worldPane (camera) ***
        // Sao cho người chơi (ở giữa màn hình) nhìn vào tọa độ logic (tileX, tileY) của player
        // Vị trí worldPane = -Tọa độ logic player + (Nửa màn hình) - (Độ dài nhân vật)
        this.worldOffsetX = -mainPlayer.getTileX() + GameConfig.SCREEN_WIDTH / 2 - playerView.getWidth() / 2;
        this.worldOffsetY = -mainPlayer.getTileY() + GameConfig.SCREEN_HEIGHT / 2 - playerView.getHeight() / 2;

        // Gọi updateMap để vẽ map lần đầu
        mainGameView.updateMap(this.worldOffsetX, this.worldOffsetY, true);

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
        // Xử lý Input
        Point2D movementDelta = handleInput(); // Trả về (dx, dy)
        double dx = movementDelta.getX();
        double dy = movementDelta.getY();

        // Cập nhật Model và View
        updatePlayerState(dx, dy); // Cập nhật trạng thái (IDLE/WALK)
        updatePlayerPosition(dx, dy); // Cập nhật vị trí

        playerView.updateAnimation(); // [QUAN TRỌNG] Yêu cầu PlayerView tự chạy animation

        // Cập nhật các hành động chờ
        updateTimedActions();

        // Cập nhật chuột
        updateMouseSelector();
    }

    /**
     * Xử lý input và trả về vector di chuyển
     */
    private Point2D handleInput() {
        // Tính toán hướng di chuyển (delta X, delta Y)
        double dx = 0;
        double dy = 0;

        // Chỉ cho phép di chuyển nếu đang không làm hành động khác
        if (mainPlayer.getState() == PlayerView.PlayerState.IDLE ||
                mainPlayer.getState() == PlayerView.PlayerState.WALK) {

            if (gameController.isKeyPressed(KeyCode.W)) { // Di chuyển PLAYER đi LÊN
                dy += GameConfig.PLAYER_SPEED; // Di chuyển WORLD đi XUỐNG
            }
            if (gameController.isKeyPressed(KeyCode.S)) { // Di chuyển PLAYER đi XUỐNG
                dy -= GameConfig.PLAYER_SPEED; // Di chuyển WORLD đi LÊN
            }
            if (gameController.isKeyPressed(KeyCode.A)) { // Di chuyển PLAYER đi TRÁI
                dx += GameConfig.PLAYER_SPEED; // Di chuyển WORLD đi PHẢI
            }
            if (gameController.isKeyPressed(KeyCode.D)) { // Di chuyển PLAYER đi PHẢI
                dx -= GameConfig.PLAYER_SPEED; // Di chuyển WORLD đi TRÁI
            }
        }
        return new Point2D(dx, dy);
    }

    /**
     * Cập nhật trạng thái (Logic) và
     * "ra lệnh" cho PlayerView (Visual)
     */
    private void updatePlayerState(double dx, double dy) {
        // Quyết định Trạng thái (Logic)
        if (dx != 0 || dy != 0) {
            mainPlayer.setState(PlayerView.PlayerState.WALK);
        } else {
            mainPlayer.setState(PlayerView.PlayerState.IDLE);
        }

        // Quyết định Hướng (Logic)
        if (dy > 0) {
            mainPlayer.setDirection(PlayerView.Direction.UP);
        } else if (dy < 0) {
            mainPlayer.setDirection(PlayerView.Direction.DOWN);
        } else if (dx > 0) {
            mainPlayer.setDirection(PlayerView.Direction.LEFT);
        } else if (dx < 0) {
            mainPlayer.setDirection(PlayerView.Direction.RIGHT);
        }

        // "RA LỆNH" cho PlayerView cập nhật hình ảnh
        playerView.setState(mainPlayer.getState(), mainPlayer.getDirection());
    }

    /**
     * Cập nhật vị trí người chơi và camera
     */
    private void updatePlayerPosition(double dx, double dy) {
        // Cập nhật Map nếu di chuyển
        if (dx != 0 || dy != 0) {
            // (Sau này thêm logic va chạm (Collision) ở đây)

            // Cập nhật camera
            this.worldOffsetX += dx;
            this.worldOffsetY += dy;

            // Cập nhật tọa độ logic của Player
            mainPlayer.setTileX(mainPlayer.getTileX() - dx);
            mainPlayer.setTileY(mainPlayer.getTileY() - dy);

            // *** YÊU CẦU VIEW VẼ LẠI MAP DỰA TRÊN VỊ TRÍ MỚI ***
            // Truyền vào vị trí offset (dịch chuyển) của worldPane
            mainGameView.updateMap(this.worldOffsetX, this.worldOffsetY, false);
        }
    }

    /**
     * Cập nhật vị trí ô vuông chọn
     */
    private void updateMouseSelector() {
        // Tọa độ logic của chuột trong thế giới
        double mouseWorldX = -this.worldOffsetX + gameController.getMouseX();
        double mouseWorldY = -this.worldOffsetY + gameController.getMouseY();

        // Tọa độ logic của ô mà chuột trỏ tới
        this.currentMouseTileX = (int) Math.floor(mouseWorldX / GameConfig.TILE_SIZE);
        this.currentMouseTileY = (int) Math.floor(mouseWorldY / GameConfig.TILE_SIZE);

        mainGameView.updateSelector(
                this.currentMouseTileX,       // Vị trí X của ô được chọn
                this.currentMouseTileY,       // Vị trí Y của ô được chọn
                this.worldOffsetX,           // Vị trí X của thế giới
                this.worldOffsetY           // Vị trí Y của thế giới
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
            int delayInFrames = 1;

            // Thêm hành động "Biến thành Đất" vào hàng đợi
            pendingActions.add(new TimedTileAction(col, row, Tile.SOIL, delayInFrames));

            // Ra lệnh cho PlayerView chạy animation "Cuốc"
            // (Hiện tại chưa làm, chỉ là ví dụ)
            // mainPlayer.setState(PlayerView.PlayerState.HOE);
        }
    }

    /**
     * Hàm này được gọi 60 LẦN/GIÂY.
     * Nhiệm vụ: Lặp qua tất cả hành động chờ, "tick" chúng,
     * và thực thi những hành động đã hết giờ.
     */
    private void updateTimedActions() {
        // Dùng Iterator để chúng ta có thể XÓA phần tử khỏi List pendingActions một cách an toàn
        Iterator<TimedTileAction> iterator = pendingActions.iterator();

        while (iterator.hasNext()) {
            TimedTileAction action = iterator.next();

            // Gọi tick(). Nếu nó trả về "true" (hết giờ)
            if (action.tick()) {
                // THỰC THI HÀNH ĐỘNG: Thay đổi Model
                worldMap.setTileType(action.getCol(), action.getRow(), action.getNewType());

                // Báo cho View biết cần vẽ lại bản đồ
                this.mapNeedsUpdate = true;

                // Xóa hành động này khỏi hàng đợi
                iterator.remove();
            }
        }

        // Update map nếu cần
        if (this.mapNeedsUpdate) {
            mainGameView.updateMap(this.worldOffsetX, this.worldOffsetY, true);
            this.mapNeedsUpdate = false;
        }
    }
}