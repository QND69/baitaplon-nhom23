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
    private final static int WORLD_WIDTH = 10000; // Độ rộng thế giới
    private Player mainPlayer;
    private GameController gameController;
    private MainGameView mainGameView;

    private WorldMap worldMap;

    private AnimationTimer gameLoop; // Khởi tạo gameLoop
    private Pane worldPane;          // Map (lấy từ MainGameView)
    private double playerSpeed = 5.0;  // Tốc độ di chuyển (pixel mỗi frame)

    public GameManager(Player player, GameController gameController, MainGameView mainGameView) {
        this.mainPlayer = player;
        this.gameController = gameController;
        this.mainGameView = mainGameView;
        this.worldMap = new WorldMap(WORLD_WIDTH, WORLD_WIDTH);
    }

    public void startGame() {
        // Lấy worldPane từ View (gọi sau initUI)
        this.worldPane = mainGameView.getWorldPane();



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
        if (gameController.isKeyPressed(KeyCode.W)) {
            dy += playerSpeed; // Di chuyển WORLD đi LÊN
        }
        if (gameController.isKeyPressed(KeyCode.S)) {
            dy -= playerSpeed; // Di chuyển WORLD đi XUỐNG
        }
        if (gameController.isKeyPressed(KeyCode.A)) {
            dx += playerSpeed; // Di chuyển WORLD đi SANG TRÁI
        }
        if (gameController.isKeyPressed(KeyCode.D)) {
            dx -= playerSpeed; // Di chuyển WORLD đi SANG PHẢI
        }

        // Cập nhật Map
        worldPane.setLayoutX(worldPane.getLayoutX() + dx);
        worldPane.setLayoutY(worldPane.getLayoutY() + dy);

        // Cập nhật tọa độ logic của Player
        mainPlayer.setWorldX(mainPlayer.getWorldX() - dx);
        mainPlayer.setWorldY(mainPlayer.getWorldY() - dy);
    }
}