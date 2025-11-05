package com.example.farmSimulation.model;

import com.example.farmSimulation.controller.GameController;
import com.example.farmSimulation.view.MainGameView;
import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// Class quản lý logic game
public class GameManager {
    private Player mainPlayer;
    private GameController gameController;
    private MainGameView mainGameView;

    private Pane worldPane;          // lấy từ MainGameView
    private WorldMap worldMap;       // lấy từ MainGameView
    private AnimationTimer gameLoop; // Khởi tạo gameLoop
    private double playerSpeed = 5.0;  // Tốc độ di chuyển (pixel mỗi frame)

    public GameManager(Player player, GameController gameController, MainGameView mainGameView) {
        this.mainPlayer = player;
        this.gameController = gameController;
        this.mainGameView = mainGameView;
    }

    public void startGame() {
        // Lấy worldPane và worldMap từ View (gọi sau initUI)
        this.worldPane = mainGameView.getWorldPane();
        this.worldMap = mainGameView.getWorldMap();

        // *** Đặt vị trí khởi đầu của worldPane (camera) ***
        // Sao cho người chơi (ở giữa màn hình) nhìn vào tọa độ logic (tileX, tileY) của player
        // Vị trí worldPane = -Tọa độ logic player + (Nửa màn hình) - (Độ dài nhân vật)
        worldPane.setLayoutX(-mainPlayer.getTileX() + mainGameView.getSCREEN_WIDTH() / 2 - mainGameView.getPlayerView().getWidth() / 2);
        worldPane.setLayoutY(-mainPlayer.getTileY() + mainGameView.getSCREEN_HEIGHT() / 2 - mainGameView.getPlayerView().getHeight() / 2);

        // Gọi updateMap để vẽ map lần đầu
        mainGameView.updateMap(worldPane.getLayoutX(), worldPane.getLayoutY());
        this.gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) { // Hàm handle() này sẽ được gọi 60 lần mỗi giây
                updateGameLogic();
            }
        };
        gameLoop.start(); // Bắt đầu gọi hàm handle() 60 lần/giây
        System.out.println("Game Started!");
    }

    private void updateGameLogic() {
        // Tính toán hướng di chuyển (delta X, delta Y)
        double dx = 0;
        double dy = 0;

        // "Hỏi" GameController xem phím nào đang được nhấn
        if (gameController.isKeyPressed(KeyCode.W)) { // Di chuyển PLAYER đi LÊN
            dy += playerSpeed; // Di chuyển WORLD đi XUỐNG
        }
        if (gameController.isKeyPressed(KeyCode.S)) { // Di chuyển PLAYER đi XUỐNG
            dy -= playerSpeed; // Di chuyển WORLD đi LÊN
        }
        if (gameController.isKeyPressed(KeyCode.A)) { // Di chuyển PLAYER đi TRÁI
            dx += playerSpeed; // Di chuyển WORLD đi SANG PHẢI
        }
        if (gameController.isKeyPressed(KeyCode.D)) { // Di chuyển PLAYER đi PHẢI
            dx -= playerSpeed; // Di chuyển WORLD đi SANG TRÁI
        }

        // Cập nhật Map nếu di chuyển
        if (dx != 0 || dy != 0) {
            worldPane.setLayoutX(worldPane.getLayoutX() + dx);
            worldPane.setLayoutY(worldPane.getLayoutY() + dy);

            // Cập nhật tọa độ logic của Player
            mainPlayer.setTileX(mainPlayer.getTileX() - dx);
            mainPlayer.setTileY(mainPlayer.getTileY() - dy);

            // *** YÊU CẦU VIEW VẼ LẠI MAP DỰA TRÊN VỊ TRÍ MỚI ***
            // Truyền vào vị trí offset (dịch chuyển) của worldPane
            mainGameView.updateMap(worldPane.getLayoutX(), worldPane.getLayoutY());
        }

        // Selector LUÔN chạy để theo chuột mượt mà
        mainGameView.updateSelector(
                gameController.getMouseX(),       // Lấy từ Controller
                gameController.getMouseY(),       // Lấy từ Controller
                worldPane.getLayoutX(),           // Vị trí X của thế giới
                worldPane.getLayoutY()            // Vị trí Y của thế giới
        );
    }
}