package com.example.farmSimulation.view;

import com.example.farmSimulation.config.HudConfig;
import com.example.farmSimulation.config.PlayerSpriteConfig;
import com.example.farmSimulation.config.WindowConfig;
import com.example.farmSimulation.config.WorldConfig;
import com.example.farmSimulation.model.GameManager;
import com.example.farmSimulation.config.AssetPaths;
import com.example.farmSimulation.controller.GameController;
import com.example.farmSimulation.model.ItemStack;
import com.example.farmSimulation.model.ItemType;
import com.example.farmSimulation.model.WorldMap;
import com.example.farmSimulation.view.assets.AssetManager;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
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
    private HotbarView hotbarView;

    // [MỚI] Manager quản lý hiệu ứng
    private final VisualEffectManager visualEffectManager;

    /**
     * Constructor (Hàm khởi tạo) nhận các thành phần nó cần
     * (Dependency Injection)
     */
    public MainGameView(AssetManager assetManager, WorldMap worldMap, HotbarView hotbarView) {
        this.assetManager = assetManager;
        this.worldMap = worldMap;
        this.hotbarView = hotbarView;
        this.visualEffectManager = new VisualEffectManager(); // Khởi tạo
    }
    /**
     * initUI nhận Controller và PlayerSprite từ bên ngoài (từ class Game)
     */
    public void initUI(Stage primaryStage, GameController gameController, Pane playerSpriteContainer,
                       Rectangle debugBox, Circle debugDot, Circle debugRangeCircle) {
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
                playerSpriteContainer,          // Lớp 3: "Khung" Player
                hudView,                        // Lớp 4: HUD (Timer, Text, Darkness)
                hotbarView,                     // Lớp 5: Hotbar
                settingsMenu                    // Lớp 6: Menu (hiện đang ẩn)
        );

        // Đặt nhân vật (nhận từ bên ngoài) vào giữa màn hình
        playerSpriteContainer.setLayoutX(WindowConfig.SCREEN_WIDTH / 2 - PlayerSpriteConfig.BASE_PLAYER_FRAME_WIDTH / 2);
        playerSpriteContainer.setLayoutY(WindowConfig.SCREEN_HEIGHT / 2 - PlayerSpriteConfig.BASE_PLAYER_FRAME_HEIGHT / 2);

        // --- Ghim (bind) vị trí của debug nodes (CHỈ KHI DEBUG BẬT) ---
        // (Nếu debug=false, các node này sẽ là NULL)
        if (PlayerSpriteConfig.DEBUG_PLAYER_BOUNDS) {
            // Thêm vào rootPane (ở lớp trên cùng)
            rootPane.getChildren().addAll(debugBox, debugDot, debugRangeCircle);

            // Ghim vị trí Khung
            debugBox.layoutXProperty().bind(playerSpriteContainer.layoutXProperty());
            debugBox.layoutYProperty().bind(playerSpriteContainer.layoutYProperty());

            // Ghim tâm chấm vào "Tâm Logic"
            double logicCenterX = PlayerSpriteConfig.BASE_PLAYER_FRAME_WIDTH / 2;
            double logicCenterY = PlayerSpriteConfig.BASE_PLAYER_FRAME_HEIGHT / 2 + PlayerSpriteConfig.PLAYER_FRAME_WIDTH / 8;

            debugDot.layoutXProperty().bind(playerSpriteContainer.layoutXProperty().add(logicCenterX));
            debugDot.layoutYProperty().bind(playerSpriteContainer.layoutYProperty().add(logicCenterY));

            // Ghim Vòng tròn Range vào "Tâm Logic" (48, 72)
            debugRangeCircle.layoutXProperty().bind(playerSpriteContainer.layoutXProperty().add(logicCenterX));
            debugRangeCircle.layoutYProperty().bind(playerSpriteContainer.layoutYProperty().add(logicCenterY));
        }
        // --- Hết phần Debug ---

        Scene scene = new Scene(rootPane, WindowConfig.SCREEN_WIDTH, WindowConfig.SCREEN_HEIGHT, WindowConfig.BACKGROUND_COLOR);
        gameController.setupInputListeners(scene);

        primaryStage.getIcons().add(assetManager.getTexture(AssetPaths.LOGO)); // Lấy logo từ manager
        primaryStage.setTitle(WindowConfig.GAME_TITLE);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --- CÁC HÀM ĐIỀU PHỐI (DELEGATE) ---
    // (Chỉ gọi lệnh cho các View con)

    // Hàm cập nhật map
    public void updateMap(double worldOffsetX, double worldOffsetY, boolean forceRedraw) {
        worldRenderer.updateMap(worldOffsetX, worldOffsetY, forceRedraw);
    }

    // Hàm cập nhật ô được chọn
    public void updateSelector(int tileSelectedX, int tileSelectedY, double worldOffsetX, double worldOffsetY) {
        worldRenderer.updateSelector(tileSelectedX, tileSelectedY, worldOffsetX, worldOffsetY);
    }

    // Hàm cập nhật text trên đầu nhân vật
    public void showTemporaryText(String message, double playerScreenX, double playerScreenY) {
        // Căn lề text dựa trên Base Width
        // (playerScreenX là layoutX của "khung")
        double playerCenterX = playerScreenX + PlayerSpriteConfig.BASE_PLAYER_FRAME_WIDTH / 2;
        hudView.showTemporaryText(message, playerCenterX, playerScreenY);
    }
    // Hàm hiện setting
    public void showSettingsMenu(String playerName, int playerLevel) {
        settingsMenu.updatePlayerInfo(playerName, playerLevel);
        settingsMenu.show();
    }

    // Hàm ẩn setting
    public void hideSettingsMenu() {
        settingsMenu.hide();
    }

    // Hàm cập nhật thời gian
    public void updateTimer(String timeString) {
        hudView.updateTimer(timeString);
    }

    // Hàm cập nhật ánh sáng
    public void updateLighting(double intensity) {
        hudView.updateLighting(intensity);
    }

    // Hàm cập nhật hotbar
    public void updateHotbar() {
        hotbarView.updateView();
    }

    // Hàm hiển thị animation thu hoạch bay về túi
    public void playHarvestAnimation(ItemType itemType, int col, int row, double worldOffsetX, double worldOffsetY) {
        // Xác định tọa độ bắt đầu (Tại ô đất)
        // Căn giữa icon vào ô đất
        double startX = col * WorldConfig.TILE_SIZE + worldOffsetX + (WorldConfig.TILE_SIZE - HudConfig.HARVEST_ICON_SIZE) / 2;
        double startY = row * WorldConfig.TILE_SIZE + worldOffsetY + (WorldConfig.TILE_SIZE - HudConfig.HARVEST_ICON_SIZE) / 2;

        // Xác định tọa độ đích (Ô trong Hotbar)
        // Tìm xem item này đang nằm ở slot nào trong túi
        int targetSlotIndex = findSlotIndexForItem(itemType);

        // Mặc định bay về giữa màn hình dưới nếu không tìm thấy (dự phòng)
        double endX = WindowConfig.SCREEN_WIDTH / 2;
        double endY = WindowConfig.SCREEN_HEIGHT - 50;

        if (targetSlotIndex != -1) {
            Point2D slotCenter = hotbarView.getSlotCenter(targetSlotIndex);
            if (slotCenter != null) {
                endX = slotCenter.getX() - (HudConfig.HARVEST_ICON_SIZE / 2); // Căn chỉnh tâm
                endY = slotCenter.getY() - (HudConfig.HARVEST_ICON_SIZE / 2);
            }
        }

        // Gọi Manager xử lý hiệu ứng
        visualEffectManager.playItemFlyAnimation(
                rootPane,
                assetManager.getItemIcon(itemType),
                startX, startY,
                endX, endY
        );
    }

    /**
     * Hàm helper tìm vị trí slot chứa item (Ưu tiên slot đang chọn nếu trùng)
     */
    private int findSlotIndexForItem(ItemType type) {
        if (gameManager == null || gameManager.getMainPlayer() == null) return -1;

        ItemStack[] items = gameManager.getMainPlayer().getHotbarItems();
        int selectedSlot = gameManager.getMainPlayer().getSelectedHotbarSlot();

        // Ưu tiên 1: Nếu slot đang chọn có item này -> Bay về đây
        if (items[selectedSlot] != null && items[selectedSlot].getItemType() == type) {
            return selectedSlot;
        }

        // Ưu tiên 2: Tìm slot đầu tiên chứa item này
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null && items[i].getItemType() == type) {
                return i;
            }
        }

        // Ưu tiên 3: Item mới nhặt (chưa có trong túi)?
        // Logic Player.addItem đã thêm vào ô trống đầu tiên.
        // Ta tìm ô nào có item này (vừa được thêm vào)
        // (Code trên đã bao phủ trường hợp này vì addItem đã chạy xong rồi)

        return selectedSlot; // Fallback về slot đang chọn
    }
}