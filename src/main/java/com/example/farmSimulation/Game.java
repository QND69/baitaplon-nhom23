package com.example.farmSimulation;

import com.example.farmSimulation.config.AssetPaths;
import com.example.farmSimulation.controller.GameController;
import com.example.farmSimulation.model.GameManager;
import com.example.farmSimulation.model.Player;
import com.example.farmSimulation.model.WorldMap;
import com.example.farmSimulation.view.HotbarView;
import com.example.farmSimulation.view.MainGameView;
import com.example.farmSimulation.view.PlayerView;
import com.example.farmSimulation.view.assets.AssetManager;
import javafx.stage.Stage;

/**
 * Class chịu trách nhiệm khởi tạo và liên kết tất cả
 * các thành phần Model, View, Controller.
 */
public class Game {

    public void start(Stage primaryStage) {
        // Tải tài nguyên (Assets)
        AssetManager assetManager = new AssetManager();
        assetManager.loadAssets();

        // Khởi tạo Model (Dữ liệu)
        Player player = new Player();
        WorldMap worldMap = new WorldMap();

        // Khởi tạo View (Hình ảnh)
        // PlayerView được tạo và nhận Image từ AssetManager
        PlayerView playerView = new PlayerView(
                assetManager.getTexture(AssetPaths.PLAYER_SHEET), assetManager.getTexture(AssetPaths.PLAYER_ACTIONS_SHEET)
        );

        // Khởi tạo HotbarView
        HotbarView hotbarView = new HotbarView(player, assetManager);

        // MainGameView nhận AssetManager để vẽ map
        MainGameView mainGameView = new MainGameView(assetManager, worldMap, hotbarView);

        // Khởi tạo Controller (Input)
        GameController gameController = new GameController();

        // Khởi tạo "Bộ não Logic" (Game Manager)
        GameManager gameManager = new GameManager(
                player,
                worldMap,
                mainGameView,
                playerView,
                gameController
        );

        // Liên kết (Wiring)
        // Controller cần biết về GameManager để gọi logic
        gameController.setGameManager(gameManager);

        // Khởi tạo UI (Truyền các thành phần cần thiết)
        // UI cần Controller (để lắng nghe input) và PlayerSprite (để vẽ)
        mainGameView.initUI(
                primaryStage,
                gameController,
                playerView.getSpriteContainer(),
                playerView.getDebugBoundingBox(),
                playerView.getDebugCenterDot(),
                playerView.getDebugRangeCircle()
        );

        // Bắt đầu Game Loop
        gameManager.startGame();
        mainGameView.setGameManager(gameManager);
    }
}