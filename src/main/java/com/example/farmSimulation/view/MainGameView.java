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
    private ShopView shopView; // Giao diện shop
    private QuestBoardView questBoardView; // Giao diện quest board
    
    // Pane tĩnh chứa các thực thể động (Animals)
    private Pane entityPane;
    
    // Pane hiệu ứng thời tiết (mưa)
    private WeatherEffectView weatherEffectView;

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
                       Rectangle debugBox, Circle debugDot, Circle debugRangeCircle, Rectangle debugCollisionHitbox) {
        this.rootPane = new Pane();

        // Khởi tạo entityPane (pane tĩnh chứa động vật)
        this.entityPane = new Pane();
        this.entityPane.setPrefSize(WindowConfig.SCREEN_WIDTH, WindowConfig.SCREEN_HEIGHT);
        this.entityPane.setMouseTransparent(true); // Không chặn click chuột xuống đất

        // Khởi tạo các View con
        this.worldRenderer = new WorldRenderer(assetManager, worldMap, entityPane);
        this.hudView = new HudView();
        // HudView sẽ được set gameManager và mainGameView sau (trong setGameManager)
        // ⚠️ Phải khởi tạo SettingsMenu SAU KHI gameManager đã được set
        // Hoặc truyền gameManager vào sau
        // Chúng ta sẽ truyền gameManager vào đây:
        this.settingsMenu = new SettingsMenuView(this.gameManager);
        
        // [MỚI] Khởi tạo weatherEffectView (hiệu ứng mưa)
        this.weatherEffectView = new WeatherEffectView();


        // Thêm các thành phần vào rootPane theo đúng thứ tự (lớp)
        rootPane.getChildren().addAll(
                worldRenderer.getWorldPane(),   // Lớp 1: Bản đồ (Đất/Cây)
                worldRenderer.getTileSelector(),// Lớp 2: Ô chọn
                worldRenderer.getGhostPlacement(), // Bóng mờ nằm ở đây (Layer tĩnh)
                entityPane,                     // Lớp 3: Động vật (Animals)
                playerSpriteContainer,          // Lớp 4: "Khung" Player
                weatherEffectView,              // Lớp 4.5: Hiệu ứng thời tiết (mưa)
                hudView,                        // Lớp 5: HUD (Timer, Text, Darkness)
                hotbarView,                     // Lớp 6: Hotbar
                settingsMenu                    // Lớp 7: Menu (hiện đang ẩn)
        );
        
        // [MỚI] ShopView sẽ được thêm sau khi gameManager được set (trong setGameManager)

        // Đặt nhân vật (nhận từ bên ngoài) vào giữa màn hình
        // [SỬA] Tính toán vị trí dựa trên kích thước SAU KHI SCALE để căn giữa chuẩn hơn
        double scaledWidth = PlayerSpriteConfig.BASE_PLAYER_FRAME_WIDTH * PlayerSpriteConfig.BASE_PLAYER_FRAME_SCALE;
        double scaledHeight = PlayerSpriteConfig.BASE_PLAYER_FRAME_HEIGHT * PlayerSpriteConfig.BASE_PLAYER_FRAME_SCALE;
        playerSpriteContainer.setLayoutX(WindowConfig.SCREEN_WIDTH / 2 - scaledWidth / 2);
        playerSpriteContainer.setLayoutY(WindowConfig.SCREEN_HEIGHT / 2 - scaledHeight / 2);

        // --- Ghim (bind) vị trí của debug nodes (CHỈ KHI DEBUG BẬT) ---
        // (Nếu debug=false, các node này sẽ là NULL)
        if (PlayerSpriteConfig.DEBUG_PLAYER_BOUNDS) {
            // Thêm vào rootPane (ở lớp trên cùng) - Bỏ debugBox (hình vuông đỏ)
            rootPane.getChildren().addAll(debugDot, debugRangeCircle, debugCollisionHitbox);

            // Ghim tâm chấm vào "Tâm Logic" (đã scale)
            double logicCenterX = scaledWidth / 2;
            
            // [SỬA] Cộng thêm INTERACTION_CENTER_Y_OFFSET để hiển thị tâm debug đúng vị trí mới
            double logicCenterY = (scaledHeight / 2) + PlayerSpriteConfig.INTERACTION_CENTER_Y_OFFSET;

            debugDot.layoutXProperty().bind(playerSpriteContainer.layoutXProperty().add(logicCenterX));
            debugDot.layoutYProperty().bind(playerSpriteContainer.layoutYProperty().add(logicCenterY));

            // Ghim Vòng tròn Range
            debugRangeCircle.layoutXProperty().bind(playerSpriteContainer.layoutXProperty().add(logicCenterX));
            debugRangeCircle.layoutYProperty().bind(playerSpriteContainer.layoutYProperty().add(logicCenterY));
            
            // Collision hitbox sẽ được cập nhật động trong updateCollisionHitbox()
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
    
    // Hàm cập nhật ghost placement
    public void updateCollisionHitbox(double playerWorldX, double playerWorldY, double worldOffsetX, double worldOffsetY, javafx.scene.shape.Rectangle debugCollisionHitbox) {
        if (debugCollisionHitbox != null && PlayerSpriteConfig.DEBUG_PLAYER_BOUNDS) {
            // 1. Cập nhật kích thước hitbox theo Config mới
            if (debugCollisionHitbox.getWidth() != PlayerSpriteConfig.COLLISION_BOX_WIDTH) {
                debugCollisionHitbox.setWidth(PlayerSpriteConfig.COLLISION_BOX_WIDTH);
                debugCollisionHitbox.setHeight(PlayerSpriteConfig.COLLISION_BOX_HEIGHT);
            }

            // 2. Tính toán vị trí trên màn hình
            double screenX = playerWorldX + worldOffsetX;
            double screenY = playerWorldY + worldOffsetY;
            
            // 3. Căn chỉnh (ĐÃ SỬA LẠI CÔNG THỨC):
            // Player gốc rộng 192x192, nhưng đã Scale 0.6
            // => Kích thước thực tế hiển thị = 192 * 0.6 = 115.2
            
            double scaledPlayerWidth = PlayerSpriteConfig.BASE_PLAYER_FRAME_WIDTH * PlayerSpriteConfig.BASE_PLAYER_FRAME_SCALE;
            double scaledPlayerHeight = PlayerSpriteConfig.BASE_PLAYER_FRAME_HEIGHT * PlayerSpriteConfig.BASE_PLAYER_FRAME_SCALE;
            
            // Offset X: Căn giữa hitbox theo chiều ngang của nhân vật đã scale
            double offsetX = (scaledPlayerWidth - PlayerSpriteConfig.COLLISION_BOX_WIDTH) / 2;
            
            // Offset Y: Căn xuống dưới chân (chân nằm ở cuối ảnh đã scale)
            // Logic: (Chiều cao đã scale) - (Cao hitbox) - (Padding đáy)
            double offsetY = scaledPlayerHeight 
                             - PlayerSpriteConfig.COLLISION_BOX_HEIGHT 
                             - PlayerSpriteConfig.COLLISION_BOX_BOTTOM_PADDING; 

            debugCollisionHitbox.setLayoutX(screenX + offsetX);
            debugCollisionHitbox.setLayoutY(screenY + offsetY);
            
            debugCollisionHitbox.setVisible(true);
        } else if (debugCollisionHitbox != null) {
            debugCollisionHitbox.setVisible(false);
        }
    }

    // Hàm cập nhật text trên đầu nhân vật
    public void showTemporaryText(String message, double playerScreenX, double playerScreenY) {
        // Căn lề text dựa trên Base Width (đã scale)
        double scaledWidth = PlayerSpriteConfig.BASE_PLAYER_FRAME_WIDTH * PlayerSpriteConfig.BASE_PLAYER_FRAME_SCALE;
        double playerCenterX = playerScreenX + scaledWidth / 2;
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
    public void updateTimer(int day, String timeString) {
        hudView.updateTimer(day, timeString);
    }

    // Hàm cập nhật ánh sáng
    public void updateLighting(double intensity) {
        hudView.updateLighting(intensity);
    }

    // Hàm cập nhật hotbar
    public void updateHotbar() {
        hotbarView.updateView();
    }

    // Hàm cập nhật bóng mờ (Ghost Placement) - Ủy quyền cho WorldRenderer xử lý
    public void updateGhostPlacement(int tileX, int tileY, double worldOffsetX, double worldOffsetY, ItemStack currentItem) {
        if (worldRenderer != null) {
            worldRenderer.updateGhostPlacement(tileX, tileY, worldOffsetX, worldOffsetY, currentItem);
        }
    }
    
    /**
     * Cập nhật vẽ động vật
     */
    public void updateAnimals(java.util.List<com.example.farmSimulation.model.Animal> animals, double worldOffsetX, double worldOffsetY) {
        if (worldRenderer != null) {
            worldRenderer.updateAnimals(animals, worldOffsetX, worldOffsetY);
        }
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
    
    /**
     * Set GameManager (được gọi từ Game.java sau khi khởi tạo)
     * Dùng để khởi tạo ShopView (cần ShopManager từ GameManager)
     */
    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
        
        // Set references cho HudView
        if (hudView != null) {
            hudView.setGameManager(gameManager);
            hudView.setMainGameView(this);
            hudView.setAssetManager(assetManager); // Set AssetManager để load GUI icons
        }
        
        // Set callback cho HotbarView item drop (for trash can deletion)
        if (hotbarView != null) {
            hotbarView.setOnItemDropListener((slotIndex, scenePoint) -> {
                // Check if dropped on trash can
                if (hudView != null && hudView.isMouseOverTrash(scenePoint.getX(), scenePoint.getY())) {
                    // Delete item at slot
                    if (gameManager != null && gameManager.getMainPlayer() != null) {
                        ItemStack stack = gameManager.getMainPlayer().getHotbarItems()[slotIndex];
                        if (stack != null) {
                            gameManager.getMainPlayer().consumeItemAtSlot(slotIndex, stack.getQuantity());
                            updateHotbar(); // Refresh hotbar display
                            return true; // Indicate item was deleted
                        }
                    }
                }
                return false; // Not dropped on trash
            });
        }
        
        // Set GameManager cho SettingsMenuView (để Resume và Brightness hoạt động đúng)
        if (settingsMenu != null) {
            settingsMenu.setGameManager(gameManager);
        }
        
        // [MỚI] Khởi tạo ShopView sau khi có gameManager
        if (gameManager != null && gameManager.getShopManager() != null) {
            this.shopView = new ShopView(gameManager.getShopManager(), assetManager);
            // Đặt kích thước cố định để đảm bảo shop không bị shrink
            shopView.setPrefSize(com.example.farmSimulation.config.ShopConfig.SHOP_WIDTH, com.example.farmSimulation.config.ShopConfig.SHOP_HEIGHT);
            shopView.setMaxSize(com.example.farmSimulation.config.ShopConfig.SHOP_WIDTH, com.example.farmSimulation.config.ShopConfig.SHOP_HEIGHT);
            // Căn giữa shop trên màn hình: x = (SCREEN_WIDTH - SHOP_WIDTH) / 2, y = (SCREEN_HEIGHT - SHOP_HEIGHT) / 2
            shopView.setLayoutX((WindowConfig.SCREEN_WIDTH - com.example.farmSimulation.config.ShopConfig.SHOP_WIDTH) / 2);
            shopView.setLayoutY((WindowConfig.SCREEN_HEIGHT - com.example.farmSimulation.config.ShopConfig.SHOP_HEIGHT) / 2);
            // Thêm shopView vào rootPane (lớp trên cùng)
            rootPane.getChildren().add(shopView);
            // Đảm bảo shopView ở trên cùng (z-index cao nhất) khi được thêm vào
            shopView.toFront();
        }
        
        // [MỚI] Khởi tạo QuestBoardView sau khi có gameManager
        if (gameManager != null && gameManager.getQuestManager() != null && gameManager.getMainPlayer() != null) {
            this.questBoardView = new QuestBoardView(gameManager.getQuestManager(), gameManager.getMainPlayer());
            // Căn giữa quest board trên màn hình
            questBoardView.setLayoutX((WindowConfig.SCREEN_WIDTH - com.example.farmSimulation.config.QuestConfig.QUEST_BOARD_WIDTH) / 2);
            questBoardView.setLayoutY((WindowConfig.SCREEN_HEIGHT - com.example.farmSimulation.config.QuestConfig.QUEST_BOARD_HEIGHT) / 2);
            // Thêm questBoardView vào rootPane (lớp trên cùng)
            rootPane.getChildren().add(questBoardView);
            // Đảm bảo questBoardView ở trên cùng khi được thêm vào
            questBoardView.toFront();
        }
    }
    
    /**
     * Cập nhật hiển thị số tiền
     */
    public void updateMoneyDisplay(double amount) {
        if (hudView != null) {
            hudView.updateMoney(amount);
        }
    }
    
    /**
     * Cập nhật hiệu ứng thời tiết
     */
    public void updateWeather(boolean isRaining) {
        if (weatherEffectView != null) {
            // Chỉ setRaining khi trạng thái thay đổi (trong setRaining đã có check)
            weatherEffectView.setRaining(isRaining);
            
            // Luôn cập nhật animation mưa mỗi frame nếu đang mưa
            if (isRaining) {
                weatherEffectView.updateRain();
            }
        }
        
        // [MỚI] Làm tối màn hình một chút khi mưa
        if (hudView != null) {
            double currentIntensity = 1.0 - hudView.getDarknessOverlay().getOpacity();
            double rainDarkness = isRaining ? com.example.farmSimulation.config.WeatherConfig.RAIN_DARKNESS_OPACITY : 0.0;
            double newOpacity = Math.min(1.0 - currentIntensity + rainDarkness, 
                com.example.farmSimulation.config.GameLogicConfig.MAX_DARKNESS_OPACITY);
            hudView.getDarknessOverlay().setOpacity(newOpacity);
        }
    }
    
    /**
     * Toggle shop (bật/tắt shop)
     * Tự động đóng Settings Menu nếu đang mở để tránh overlap
     */
    public void toggleShop() {
        if (shopView != null) {
            boolean wasVisible = shopView.isShopVisible();
            shopView.toggle();
            
            // Nếu shop được mở, đóng Settings Menu nếu đang mở
            if (!wasVisible && shopView.isShopVisible()) {
                // Đóng Settings Menu nếu đang mở để tránh overlap
                if (settingsMenu != null && settingsMenu.isVisible()) {
                    hideSettingsMenu();
                    // Resume game loop nếu đang pause (Settings menu đang pause game)
                    if (gameManager != null && gameManager.isPaused()) {
                        gameManager.setPaused(false);
                        if (gameManager.getGameLoop() != null) {
                            gameManager.getGameLoop().start();
                        }
                    }
                }
            }
            
            // Đảm bảo shop hiển thị ở lớp trên cùng khi mở
            // (toFront() được gọi trong ShopView.toggle() khi mở)
        }
    }
    
    /**
     * Kiểm tra shop có đang hiển thị không
     */
    public boolean isShopVisible() {
        return shopView != null && shopView.isShopVisible();
    }
    
    /**
     * Toggle Quest Board
     */
    public void toggleQuestBoard() {
        if (questBoardView != null) {
            boolean wasVisible = questBoardView.isQuestBoardVisible();
            questBoardView.toggle();
            
            // Nếu quest board được mở, đóng Settings Menu và Shop nếu đang mở
            if (!wasVisible && questBoardView.isQuestBoardVisible()) {
                if (settingsMenu != null && settingsMenu.isVisible()) {
                    hideSettingsMenu();
                }
                if (shopView != null && shopView.isShopVisible()) {
                    shopView.toggle(); // Close shop by toggling
                }
                // Quest Board KHÔNG pause game - game tiếp tục chạy trong background
            }
            
            questBoardView.toFront();
        }
    }
    
    /**
     * Kiểm tra quest board có đang hiển thị không
     */
    public boolean isQuestBoardVisible() {
        return questBoardView != null && questBoardView.isQuestBoardVisible();
    }
}