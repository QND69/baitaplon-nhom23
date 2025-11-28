package com.example.farmSimulation;

import com.example.farmSimulation.config.AssetPaths;
import com.example.farmSimulation.controller.GameController;
import com.example.farmSimulation.model.GameManager;
import com.example.farmSimulation.model.Player;
import com.example.farmSimulation.model.WorldMap;
import com.example.farmSimulation.view.CharacterCreationView;
import com.example.farmSimulation.view.HotbarView;
import com.example.farmSimulation.view.MainGameView;
import com.example.farmSimulation.view.PlayerView;
import com.example.farmSimulation.view.assets.AssetManager;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Class chịu trách nhiệm khởi tạo và liên kết tất cả
 * các thành phần Model, View, Controller.
 */
public class Game {
    private Stage primaryStage;
    private AssetManager assetManager;
    private Player player;
    private WorldMap worldMap;

    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Tải tài nguyên (Assets)
        assetManager = new AssetManager();
        assetManager.loadAssets();

        // Khởi tạo Model (Dữ liệu) - sẽ được cập nhật với name và gender từ CharacterCreationView
        player = new Player();
        worldMap = new WorldMap();

        // Tạo và hiển thị Character Creation Screen
        CharacterCreationView characterCreationView = new CharacterCreationView();
        
        // Set callback khi người chơi click "Start Game"
        characterCreationView.setOnStartGame((name, gender) -> {
            // Cập nhật Player với thông tin từ Character Creation
            player.setName(name);
            player.setGender(gender);
            
            // Khởi tạo game và chuyển sang MainGameView
            initializeAndStartGame();
        });
        
        // Hiển thị Character Creation Scene
        Scene characterCreationScene = characterCreationView.createScene();
        primaryStage.setTitle("Farm Simulation - Character Creation");
        primaryStage.setScene(characterCreationScene);
        primaryStage.show();
    }
    
    /**
     * Khởi tạo và bắt đầu game sau khi character creation hoàn tất
     */
    private void initializeAndStartGame() {
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
        // Controller cần biết về MainGameView để toggle shop
        gameController.setMainGameView(mainGameView);

        // Liên kết callback kéo thả item: HotbarView -> GameManager
        // Khi UI phát hiện swap, nó gọi hàm swap của GameManager
        hotbarView.setOnSwapListener((indexA, indexB) -> {
            gameManager.swapHotbarItems(indexA, indexB);
        });
        
        // Liên kết callback cho item drop (including trash can deletion)
        // Note: This needs to be set after mainGameView.setGameManager() is called
        // We'll set it in a separate call after setGameManager

        // Khởi tạo UI (Truyền các thành phần cần thiết)
        // UI cần Controller (để lắng nghe input) và PlayerSprite (để vẽ)
        mainGameView.initUI(
                primaryStage,
                gameController,
                playerView.getSpriteContainer(),
                playerView.getDebugBoundingBox(),
                playerView.getDebugCenterDot(),
                playerView.getDebugRangeCircle(),
                playerView.getDebugCollisionHitbox()
        );

        // Bắt đầu Game Loop
        gameManager.startGame();
        mainGameView.setGameManager(gameManager);
        
        // Bắt đầu phát nhạc nền
        gameManager.getAudioManager().playMusic(AssetPaths.BACKGROUND_MUSIC);
        
        // Cập nhật title
        primaryStage.setTitle("Farm Simulation");
    }
}