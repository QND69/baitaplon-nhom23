package com.example.farmSimulation.model;

import com.example.farmSimulation.config.GameConfig;
import com.example.farmSimulation.controller.GameController;
import com.example.farmSimulation.view.MainGameView;
import com.example.farmSimulation.view.PlayerView;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;

public class PlayerMovementHandler {
    private final Player mainPlayer;
    private final PlayerView playerView;
    private final GameController gameController;
    private final Camera camera;
    private final MainGameView mainGameView;

    public PlayerMovementHandler(Player mainPlayer, PlayerView playerView, GameController gameController, Camera camera, MainGameView mainGameView) {
        this.mainPlayer = mainPlayer;
        this.playerView = playerView;
        this.gameController = gameController;
        this.camera = camera;
        this.mainGameView = mainGameView;
    }

    // Hàm update chính cho di chuyển
    public void update() {
        // Xử lý Input
        Point2D movementDelta = handleInput(); // Trả về (dx, dy)
        double dx = movementDelta.getX();
        double dy = movementDelta.getY();

        // Cập nhật Model và View
        updatePlayerState(dx, dy); // Cập nhật trạng thái (IDLE/WALK)
        updatePlayerPosition(dx, dy); // Cập nhật vị trí
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
            camera.move(dx, dy);

            // Cập nhật tọa độ logic của Player
            mainPlayer.setTileX(mainPlayer.getTileX() - dx);
            mainPlayer.setTileY(mainPlayer.getTileY() - dy);

            // *** YÊU CẦU VIEW VẼ LẠI MAP DỰA TRÊN VỊ TRÍ MỚI ***
            // Truyền vào vị trí offset (dịch chuyển) của worldPane
            mainGameView.updateMap(camera.getWorldOffsetX(), camera.getWorldOffsetY(), false);
        }
    }
}