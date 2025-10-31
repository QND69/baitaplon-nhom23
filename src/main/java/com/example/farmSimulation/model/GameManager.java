package com.example.farmSimulation.model;

import com.example.farmSimulation.controller.GameController;
import com.example.farmSimulation.view.MainGame;
import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;

// Class quản lý logic game
public class GameManager {

    private GameController gameController; // Tham chiếu đến Controller để lấy input

    private MainGame mainView; // Cần tham chiếu đến View
    private Player mainPlayer;

    private Pane worldPane; // "Camera" của chúng ta
    private double playerSpeed = 5.0;

    private AnimationTimer gameLoop;// Vòng lặp game, gọi liên tục để cập nhật input, logic, và view

    public GameManager(Player player, MainGame view, GameController controller) {
        this.mainPlayer = player;
        this.mainView = view;
        this.gameController = controller;

        // Lấy worldPane từ View
        this.worldPane = mainView.getWorldPane();

        // ...

        // Khởi tạo game loop
        /* AnimationTimer là công cụ JavaFX (1 abstract class) để tạo game loop, gọi lại một hàm mỗi frame (mỗi frame ~16ms cho 60 FPS) */
        gameLoop = new AnimationTimer() { // Tạo một instance (object) ẩn danh của AnimationTimer
            @Override
            public void handle(long now) {
                /* Hàm handle được gọi mỗi frame
                Tham số "now" là thời gian hiện tại (tính bằng nano giây) */

                // Xử lý Input (Lấy từ Controller)
                updateInput();

                // Cập nhật Logic Game (vị trí, trạng thái, vật phẩm dựa trên input và các quy tắc game)
                updateGameLogic();

                // Vẽ lại View (View sẽ tự cập nhật, không cần gọi hàm)
                /* (Trong JavaFX, View tự vẽ lại khi thuộc tính thay đổi, không cần gọi hàm thủ công) */
            }
        };
    }

    // Hàm để Main.java gọi khi game bắt đầu
    public void startGame() {
        gameLoop.start();
    }
    /* Hàm startGame gọi 1 lần duy nhất, nó sẽ khởi chạy AnimationTimer gameLoop
    Sau đó, JavaFX engine sẽ tự động gọi gameLoop.handle(now); 60 lần mỗi giây (60 Hz) */

    private void updateInput() {
        // Lấy thông tin phím bấm từ GameController
    }

    private void updateGameLogic() {// Cập nhật logic dựa trên input
        // Tính toán di chuyển
        double dx = 0;
        double dy = 0;

        if (gameController.isKeyPressed(KeyCode.W)) {
            dy += playerSpeed; // Di chuyển world LÊN (nhân vật đi XUỐNG)
        }
        if (gameController.isKeyPressed(KeyCode.S)) {
            dy -= playerSpeed; // Di chuyển world XUỐNG (nhân vật đi LÊN)
        }
        if (gameController.isKeyPressed(KeyCode.A)) {
            dx += playerSpeed;
        }
        if (gameController.isKeyPressed(KeyCode.D)) {
            dx -= playerSpeed;
        }

        // CẬP NHẬT TỌA ĐỘ CỦA WORLD (CAMERA)
        // Dùng setLayout để di chuyển Pane
        worldPane.setLayoutX(worldPane.getLayoutX() + dx);
        worldPane.setLayoutY(worldPane.getLayoutY() + dy);

        // Cập nhật tọa độ logic của Player (nếu cần)
        // mainPlayer.setWorldX(mainPlayer.getWorldX() - dx);
        // mainPlayer.setWorldY(mainPlayer.getWorldY() - dy);
    }

}