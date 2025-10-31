package com.example.farmSimulation.view;

import com.example.farmSimulation.controller.GameController;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MainGame {

    private Pane rootPane; // Pane chính chứa tất cả
    private Pane worldPane; // Pane chứa MAP (sẽ di chuyển)
    private ImageView playerView; // Người chơi (đứng yên)

    public void initUI(Stage primaryStage, GameController controller) {

        // 1. Tạo các Pane
        rootPane = new Pane();
        worldPane = new Pane();

        // 2. Thêm worldPane vào rootPane
        rootPane.getChildren().add(worldPane);

        // 3. TẠO MAP CỎ (Cách đơn giản, CHƯA VÔ HẠN)
        // Lấy ảnh cỏ từ resources
        Image grassImage = new Image(getClass().getResourceAsStream("/assets/images/grassDraft.jpg"));

        // Tạo 1 map 50x50 ô (ví dụ)
        int tileSize = 64;
        for (int y = 0; y < 50; y++) {
            for (int x = 0; x < 50; x++) {
                ImageView tile = new ImageView(grassImage);
                tile.setX(x * tileSize);
                tile.setY(y * tileSize);
                worldPane.getChildren().add(tile); // Thêm ô cỏ vào WORLD
            }
        }

        // 4. TẠO NGƯỜI CHƠI (Đứng yên)
        Image playerImage = new Image(getClass().getResourceAsStream("/assets/images/playerDraft.png"));
        playerView = new ImageView(playerImage);

        // Đặt người chơi ở giữa màn hình (ví dụ: màn hình 800x600)
        playerView.setX(800 / 2 - playerImage.getWidth() / 2);
        playerView.setY(600 / 2 - playerImage.getHeight() / 2);

        // Thêm người chơi vào ROOT (không phải world)
        rootPane.getChildren().add(playerView);

        // 5. Hoàn tất
        Scene scene = new Scene(rootPane, 800, 600);

        // 6. GIAO SCENE CHO CONTROLLER
        controller.setupInputListeners(scene);

        primaryStage.setTitle("Farm Simulation");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Hàm để GameManager gọi
    public Pane getWorldPane() {
        return worldPane;
    }
}