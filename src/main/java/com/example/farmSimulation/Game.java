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
import com.example.farmSimulation.view.assets.ImageManager;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Class chịu trách nhiệm khởi tạo và liên kết tất cả
 * các thành phần Model, View, Controller.
 */
public class Game {
    private Stage primaryStage;
    private ImageManager imageManager;
    private Player player;
    private WorldMap worldMap;

    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Tải tài nguyên (Assets) - Chỉ tải 1 lần khi ứng dụng bắt đầu
        imageManager = new ImageManager();
        imageManager.loadAssets();

        // Icon hiển thị game (Application Icon)
        primaryStage.getIcons().add(imageManager.getTexture(AssetPaths.LOGO));

        // Hiển thị Main Menu
        showMainMenu();
    }

    /**
     * [MỚI] Hàm hiển thị màn hình chính (Character Creation / Menu)
     * Được tách ra để có thể gọi lại khi Game Over
     */
    private void showMainMenu() {
        // Khởi tạo Model (Dữ liệu) mới cho phiên chơi mới
        // Điều này đảm bảo khi start game mới, dữ liệu cũ không còn tồn đọng
        player = new Player();
        worldMap = new WorldMap();

        // Tạo và hiển thị Character Creation Screen
        CharacterCreationView characterCreationView = new CharacterCreationView();

        // Set callback khi người chơi click "Start New Game"
        characterCreationView.setOnStartGame((name, gender) -> {
            // Cập nhật Player với thông tin từ Character Creation
            player.setName(name);
            player.setGender(gender);

            // Khởi tạo game và chuyển sang MainGameView (chế độ New Game)
            initializeAndStartGame(false);
        });

        // Set callback khi người chơi click "Load Game"
        characterCreationView.setOnLoadGame(() -> {
            // Khởi tạo game và chuyển sang MainGameView (chế độ Load Game)
            initializeAndStartGame(true);
        });

        // Hiển thị Character Creation Scene
        Scene characterCreationScene = characterCreationView.createScene();
        primaryStage.setTitle("Farm Simulation - Character Creation");
        primaryStage.setScene(characterCreationScene);
        primaryStage.show();
    }

    /**
     * Khởi tạo và bắt đầu game sau khi character creation hoàn tất
     * @param loadFromSave Nếu true, sẽ tải dữ liệu từ file save thay vì dùng mặc định
     */
    private void initializeAndStartGame(boolean loadFromSave) {
        // Khởi tạo View (Hình ảnh)
        // PlayerView được tạo và nhận Image từ AssetManager
        PlayerView playerView = new PlayerView(
                imageManager.getTexture(AssetPaths.PLAYER_SHEET), imageManager.getTexture(AssetPaths.PLAYER_ACTIONS_SHEET)
        );

        // Khởi tạo HotbarView
        HotbarView hotbarView = new HotbarView(player, imageManager);

        // MainGameView nhận AssetManager để vẽ map
        MainGameView mainGameView = new MainGameView(imageManager, worldMap, hotbarView);

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

        // [QUAN TRỌNG - SỬA LỖI] Đăng ký Handler để quay về Main Menu
        // Khi GameManager gọi returnToMainMenu(), hàm này sẽ chạy
        gameManager.setOnReturnToMainMenuHandler(() -> {
            showMainMenu();
        });

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

        // Liên kết GameManager vào View
        mainGameView.setGameManager(gameManager);

        // Nếu là Load Game, thực hiện load dữ liệu TRƯỚC khi start game loop
        if (loadFromSave) {
            gameManager.loadGameData();
        }

        // Bắt đầu Game Loop
        gameManager.startGame();

        // Bắt đầu phát nhạc nền
        gameManager.getAudioManager().playMusic(AssetPaths.BACKGROUND_MUSIC);

        // Cập nhật title
        primaryStage.setTitle("Farm Simulation");
    }
}