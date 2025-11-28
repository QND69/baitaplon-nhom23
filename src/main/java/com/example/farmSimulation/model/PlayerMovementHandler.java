package com.example.farmSimulation.model;

import com.example.farmSimulation.config.GameLogicConfig;
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
    private CollisionManager collisionManager; // Quản lý collision (sẽ được set từ bên ngoài)

    public PlayerMovementHandler(Player mainPlayer, PlayerView playerView, GameController gameController, Camera camera, MainGameView mainGameView) {
        this.mainPlayer = mainPlayer;
        this.playerView = playerView;
        this.gameController = gameController;
        this.camera = camera;
        this.mainGameView = mainGameView;
    }
    
    public void setCollisionManager(CollisionManager collisionManager) {
        this.collisionManager = collisionManager;
    }

    // Hàm update chính cho di chuyển
    public void update(double deltaTime) {
        // Xử lý Input
        Point2D movementDelta = handleInput(deltaTime); // Trả về (dx, dy) - đã nhân với deltaTime
        double dx = movementDelta.getX();
        double dy = movementDelta.getY();

        // Cập nhật Model và View
        updatePlayerState(dx, dy); // Cập nhật trạng thái (IDLE/WALK)
        updatePlayerPosition(dx, dy); // Cập nhật vị trí
    }

    /**
     * Xử lý input và trả về vector di chuyển (đã nhân với deltaTime)
     * @param deltaTime Thời gian trôi qua (giây) - để di chuyển độc lập với FPS
     */
    private Point2D handleInput(double deltaTime) {
        // Tính toán hướng di chuyển (delta X, delta Y)
        double dx = 0;
        double dy = 0;

        // Chỉ cho phép di chuyển nếu đang không làm hành động khác
        if (mainPlayer.getState() == PlayerView.PlayerState.IDLE ||
                mainPlayer.getState() == PlayerView.PlayerState.WALK) {

            // Tính tốc độ di chuyển dựa trên deltaTime (pixel/giây * giây = pixel)
            double movementSpeed = GameLogicConfig.PLAYER_SPEED * deltaTime;
            
            // Áp dụng stamina penalty nếu stamina thấp
            if (mainPlayer.hasStaminaPenalty()) {
                movementSpeed *= GameLogicConfig.STAMINA_SPEED_PENALTY_MULTIPLIER;
                // Tốc độ animation cũng giảm (xử lý trong PlayerView nếu cần)
            }

            if (gameController.isKeyPressed(KeyCode.W)) { // Di chuyển PLAYER đi LÊN
                dy += movementSpeed; // Di chuyển WORLD đi XUỐNG
            }
            if (gameController.isKeyPressed(KeyCode.S)) { // Di chuyển PLAYER đi XUỐNG
                dy -= movementSpeed; // Di chuyển WORLD đi LÊN
            }
            if (gameController.isKeyPressed(KeyCode.A)) { // Di chuyển PLAYER đi TRÁI
                dx += movementSpeed; // Di chuyển WORLD đi PHẢI
            }
            if (gameController.isKeyPressed(KeyCode.D)) { // Di chuyển PLAYER đi PHẢI
                dx -= movementSpeed; // Di chuyển WORLD đi TRÁI
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
        // (Chỉ thay đổi nếu không đang làm hành động)
        if (mainPlayer.getState() == PlayerView.PlayerState.IDLE ||
                mainPlayer.getState() == PlayerView.PlayerState.WALK) {
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
    }

    /**
     * Cập nhật vị trí người chơi và camera
     */
    private void updatePlayerPosition(double dx, double dy) {
        // Cập nhật Map nếu di chuyển
        if (dx != 0 || dy != 0) {
            // Tính vị trí mới của player
            double newX = mainPlayer.getTileX() - dx;
            double newY = mainPlayer.getTileY() - dy;
            
            // Kiểm tra collision trước khi di chuyển
            if (collisionManager != null) {
                // Tất cả giá trị đã được scale trong PlayerSpriteConfig
                double hitboxWidth = com.example.farmSimulation.config.PlayerSpriteConfig.COLLISION_BOX_WIDTH;
                double hitboxHeight = com.example.farmSimulation.config.PlayerSpriteConfig.COLLISION_BOX_HEIGHT;
                
                // Tâm kiểm tra va chạm phải là TÂM CỦA HITBOX Ở CHÂN
                // Tất cả giá trị đã được scale, chỉ cần tính toán trực tiếp
                double scaledPlayerWidth = com.example.farmSimulation.config.PlayerSpriteConfig.BASE_PLAYER_FRAME_WIDTH * com.example.farmSimulation.config.PlayerSpriteConfig.BASE_PLAYER_FRAME_SCALE;
                double scaledPlayerHeight = com.example.farmSimulation.config.PlayerSpriteConfig.BASE_PLAYER_FRAME_HEIGHT * com.example.farmSimulation.config.PlayerSpriteConfig.BASE_PLAYER_FRAME_SCALE;
                
                double feetCenterX = newX + (scaledPlayerWidth / 2.0);
                
                // Tính toán Y dựa trên HEIGHT đã SCALE
                // Công thức: Vị trí Y + (Chiều cao đã scale) - (Nửa chiều cao hitbox) - Padding (đã scale)
                double feetCenterY = newY + scaledPlayerHeight
                                     - (hitboxHeight / 2.0) 
                                     - com.example.farmSimulation.config.PlayerSpriteConfig.COLLISION_BOX_BOTTOM_PADDING; 

                // Kiểm tra collision tại vị trí chân mới
                if (collisionManager.checkCollision(feetCenterX, feetCenterY, hitboxWidth, hitboxHeight)) {
                    return;
                }
            }

            // Không có collision, di chuyển bình thường
            // Cập nhật camera
            camera.move(dx, dy);

            // Cập nhật tọa độ logic của Player
            mainPlayer.setTileX(newX);
            mainPlayer.setTileY(newY);

            // *** YÊU CẦU VIEW VẼ LẠI MAP DỰA TRÊN VỊ TRÍ MỚI ***
            // Truyền vào vị trí offset (dịch chuyển) của worldPane
            mainGameView.updateMap(camera.getWorldOffsetX(), camera.getWorldOffsetY(), false);
        }
    }
}