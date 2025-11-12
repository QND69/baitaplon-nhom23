package com.example.farmSimulation.view;

import com.example.farmSimulation.model.GameManager;
import com.example.farmSimulation.config.AssetPaths;
import com.example.farmSimulation.config.GameConfig;
import com.example.farmSimulation.controller.GameController;
import com.example.farmSimulation.model.WorldMap;
import com.example.farmSimulation.view.assets.AssetManager;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MainGameView {
    private final AssetManager assetManager; // Để lấy textures
    private final WorldMap worldMap;         // Để biết vẽ tile gì
    private GameManager gameManager;

    private Pane rootPane;    // Root pane

    // Các thành phần View con
    private WorldRenderer worldRenderer;
    private HudView hudView;
    private SettingsMenuView settingsMenu;

    /**
     * Constructor (Hàm khởi tạo) nhận các thành phần nó cần
     * (Dependency Injection)
     */
    public MainGameView(AssetManager assetManager, WorldMap worldMap) {
        this.assetManager = assetManager;
        this.worldMap = worldMap;
    }

    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    /**
     * initUI nhận Controller và PlayerSprite từ bên ngoài (từ class Game)
     */
    public void initUI(Stage primaryStage, GameController gameController, ImageView playerSprite) {
        this.rootPane = new Pane();

        // Khởi tạo các View con
        this.worldRenderer = new WorldRenderer(assetManager, worldMap);
        this.hudView = new HudView();
        // ⚠️ Phải khởi tạo SettingsMenu SAU KHI gameManager đã được set
        // Hoặc truyền gameManager vào sau
        // Chúng ta sẽ truyền gameManager vào đây:
        this.settingsMenu = new SettingsMenuView(this.gameManager);


        // Thêm các thành phần vào rootPane theo đúng thứ tự (lớp)
        rootPane.getChildren().addAll(
                worldRenderer.getWorldPane(),   // Lớp 1: Bản đồ
                worldRenderer.getTileSelector(),// Lớp 2: Ô chọn
                playerSprite,                   // Lớp 3: Người chơi
                hudView,                        // Lớp 4: HUD (Timer, Text, Darkness)
                settingsMenu                    // Lớp 5: Menu (hiện đang ẩn)
        );

        // Đặt nhân vật (nhận từ bên ngoài) vào giữa màn hình
        playerSprite.setLayoutX(GameConfig.SCREEN_WIDTH / 2 - playerSprite.getFitWidth() / 2);
        playerSprite.setLayoutY(GameConfig.SCREEN_HEIGHT / 2 - playerSprite.getFitHeight() / 2);

        Scene scene = new Scene(rootPane, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT, GameConfig.BACKGROUND_COLOR);
        gameController.setupInputListeners(scene);

        primaryStage.getIcons().add(assetManager.getTexture(AssetPaths.LOGO)); // Lấy logo từ manager
        primaryStage.setTitle(GameConfig.GAME_TITLE);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --- CÁC HÀM ĐIỀU PHỐI (DELEGATE) ---
    // (Chỉ gọi lệnh cho các View con)

    public void updateMap(double worldOffsetX, double worldOffsetY, boolean forceRedraw) {
        worldRenderer.updateMap(worldOffsetX, worldOffsetY, forceRedraw);
    }

    public void updateSelector(int tileSelectedX, int tileSelectedY, double worldOffsetX, double worldOffsetY) {
        worldRenderer.updateSelector(tileSelectedX, tileSelectedY, worldOffsetX, worldOffsetY);
    }

    public void showTemporaryText(String message, double playerScreenX, double playerScreenY) {
        hudView.showTemporaryText(message, playerScreenX, playerScreenY);
    }

    public void showSettingsMenu(String playerName, int playerLevel) {
        settingsMenu.updatePlayerInfo(playerName, playerLevel);
        settingsMenu.show();
    }

    public void hideSettingsMenu() {
        settingsMenu.hide();
    }

    public void updateTimer(String timeString) {
        hudView.updateTimer(timeString);
    }

    public void updateLighting(double intensity) {
        hudView.updateLighting(intensity);
    }
}