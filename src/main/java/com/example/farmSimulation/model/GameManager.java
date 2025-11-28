package com.example.farmSimulation.model;

import com.example.farmSimulation.config.*;
import com.example.farmSimulation.controller.GameController;
import com.example.farmSimulation.view.MainGameView;
import com.example.farmSimulation.view.PlayerView;
import javafx.animation.AnimationTimer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameManager {
    // --- Các thành phần cốt lõi (Model, View, Controller) ---
    private final Player mainPlayer;
    private final WorldMap worldMap;
    private final MainGameView mainGameView;
    private final PlayerView playerView;
    private final GameController gameController;

    // --- Các Manager con (được tách ra) ---
    private final TimeManager timeManager;
    private final ActionManager actionManager;
    private final PlayerMovementHandler movementHandler;
    private final Camera camera;
    private final InteractionManager interactionManager;
    private final CropManager cropManager;
    private final TreeManager treeManager; // Quản lý cây tự nhiên
    private final FenceManager fenceManager; // Quản lý hàng rào
    private final CollisionManager collisionManager; // Quản lý collision
    private final AnimalManager animalManager; // Quản lý động vật
    private final ShopManager shopManager; // Quản lý shop
    private final WeatherManager weatherManager; // Quản lý thời tiết

    // --- Trạng thái Game ---
    private AnimationTimer gameLoop; // Khởi tạo gameLoop
    private boolean isPaused = false;
    private long lastUpdateTime = 0; // Thời gian update lần cuối (nanoTime) - để tính deltaTime

    // Tọa độ ô chuột đang trỏ tới
    private int currentMouseTileX = 0;
    private int currentMouseTileY = 0;
    
    // [MỚI] Tọa độ chuột thực tế trong thế giới
    private double currentMouseWorldX = 0;
    private double currentMouseWorldY = 0;

    // Constructor nhận tất cả các thành phần cốt lõi
    public GameManager(Player player, WorldMap worldMap, MainGameView mainGameView,
                       PlayerView playerView, GameController gameController) {
        this.mainPlayer = player;
        this.worldMap = worldMap;
        this.mainGameView = mainGameView;
        this.playerView = playerView;
        this.gameController = gameController;

        // Khởi tạo các Manager con
        this.camera = new Camera();
        this.timeManager = new TimeManager(mainGameView);
        this.actionManager = new ActionManager(player, playerView); // Truyền player/playerView
        this.movementHandler = new PlayerMovementHandler(player, playerView, gameController, camera, mainGameView);
        this.interactionManager = new InteractionManager(this.actionManager);
        this.cropManager = new CropManager(this.worldMap);
        this.treeManager = new TreeManager(this.worldMap);
        this.fenceManager = new FenceManager(this.worldMap);
        this.collisionManager = new CollisionManager(this.worldMap);
        this.animalManager = new AnimalManager(this.worldMap, this.collisionManager);
        this.shopManager = new ShopManager(player); // Khởi tạo ShopManager
        this.weatherManager = new WeatherManager(); // Khởi tạo WeatherManager
        
        // Liên kết Player với MainGameView để hiển thị thông báo
        player.setMainGameView(mainGameView);
        
        // Liên kết các Manager với nhau
        this.actionManager.setFenceManager(this.fenceManager);
        this.actionManager.setAnimalManager(this.animalManager); // Liên kết AnimalManager với ActionManager
        this.movementHandler.setCollisionManager(this.collisionManager);
        this.interactionManager.setAnimalManager(this.animalManager); // Liên kết AnimalManager với InteractionManager
        this.interactionManager.setCollisionManager(this.collisionManager); // Liên kết CollisionManager với InteractionManager
        this.interactionManager.setWorldMap(this.worldMap); // Liên kết WorldMap với InteractionManager
        this.cropManager.setWeatherManager(this.weatherManager); // Liên kết WeatherManager với CropManager
        this.cropManager.setTimeManager(this.timeManager); // Liên kết TimeManager với CropManager
    }

    public void startGame() {
        // Đặt vị trí camera ban đầu
        camera.initializePosition(mainPlayer, playerView);

        // Gọi updateMap để vẽ map lần đầu
        mainGameView.updateMap(camera.getWorldOffsetX(), camera.getWorldOffsetY(), true);

        // Bắt đầu game loop
        this.gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateGameLogic(now);
            }
        };
        gameLoop.start();
        System.out.println("Game Started!");
    }

    /**
     * Hàm update chính, chỉ điều phối các hàm con
     */
    private void updateGameLogic(long now) {
        // ⚠️ BỔ SUNG: Dừng ngay lập tức nếu game đang tạm dừng
        if (this.isPaused) {
            return;
        }

        // Tính deltaTime (thời gian trôi qua giữa 2 frame) - tính bằng giây
        double deltaTime = 0.0;
        if (lastUpdateTime > 0) {
            long deltaNanos = now - lastUpdateTime;
            deltaTime = deltaNanos / 1_000_000_000.0; // Chuyển từ nano giây sang giây
            // Giới hạn deltaTime tối đa để tránh lag spike (ví dụ: 0.1 giây = 100ms)
            if (deltaTime > 0.1) {
                deltaTime = 0.1;
            }
        }
        lastUpdateTime = now;

        // Cập nhật thời gian & chu kỳ ngày đêm
        timeManager.update();
        
        // Check if new day started and refresh shop stock
        if (timeManager.hasNewDayStarted()) {
            shopManager.generateDailyStock(true); // Allow discounts on natural day refresh
            System.out.println("New day started! Shop stock refreshed.");
        }
        
        // Tự động hồi phục stamina (khi không hoạt động)
        updateStaminaRecovery(deltaTime);

        // Cập nhật di chuyển & trạng thái Player (truyền deltaTime)
        movementHandler.update(deltaTime);

        // Cập nhật animation (View tự chạy)
        playerView.updateAnimation(); // [QUAN TRỌNG] Yêu cầu PlayerView tự chạy animation

        // Cập nhật các hành động chờ
        actionManager.updateTimedActions(worldMap, mainGameView, camera.getWorldOffsetX(), camera.getWorldOffsetY());

        // Cập nhật logic cây trồng
        boolean cropsUpdated = cropManager.updateCrops(now); // Truyền 'now'
        if (cropsUpdated) {
            actionManager.setMapNeedsUpdate(true); // Báo map cần vẽ lại
        }

        // Cập nhật logic cây tự nhiên
        boolean treesUpdated = treeManager.updateTrees(now, mainPlayer.getTileX(), mainPlayer.getTileY());
        if (treesUpdated) {
            actionManager.setMapNeedsUpdate(true); // Báo map cần vẽ lại
        }
        
        // Cập nhật logic động vật
        boolean animalsUpdated = animalManager.updateAnimals(now);
        if (animalsUpdated) {
            actionManager.setMapNeedsUpdate(true); // Báo map cần vẽ lại
        }
        
        // Cập nhật vẽ động vật
        mainGameView.updateAnimals(animalManager.getAnimals(), camera.getWorldOffsetX(), camera.getWorldOffsetY());
        
        // [MỚI] Cập nhật thời tiết
        weatherManager.updateWeather(now);
        mainGameView.updateWeather(weatherManager.isRaining());
        
        // Cập nhật HUD (Player Stats, Weather Icon)
        if (mainGameView.getHudView() != null) {
            mainGameView.getHudView().updatePlayerStats();
            mainGameView.getHudView().updateWeather(weatherManager.isRaining());
        }

        // Cập nhật chuột
        updateMouseSelector();
        
        // Cập nhật ghost placement
        updateGhostPlacement();
        
        // Cập nhật collision hitbox (debug mode)
        updateCollisionHitbox();
        
        // [MỚI] Cập nhật hiển thị tiền
        mainGameView.updateMoneyDisplay(mainPlayer.getMoney());
    }

    /**
     * Tự động hồi phục stamina theo thời gian
     */
    private void updateStaminaRecovery(double deltaTime) {
        // Chỉ hồi phục khi không hoạt động (IDLE hoặc WALK)
        PlayerView.PlayerState currentState = mainPlayer.getState();
        if (currentState == PlayerView.PlayerState.IDLE || currentState == PlayerView.PlayerState.WALK) {
            // Chỉ hồi phục nếu chưa đầy
            if (mainPlayer.getCurrentStamina() < mainPlayer.getMaxStamina()) {
                double recoveryAmount = GameLogicConfig.STAMINA_RECOVERY_RATE * deltaTime;
                mainPlayer.recoverStamina(recoveryAmount);
            }
        }
    }
    
    /**
     * Cập nhật vị trí ô vuông chọn
     */
    private void updateMouseSelector() {
        // Tọa độ logic của chuột trong thế giới
        this.currentMouseWorldX = -camera.getWorldOffsetX() + gameController.getMouseX();
        this.currentMouseWorldY = -camera.getWorldOffsetY() + gameController.getMouseY();

        // Tọa độ logic của ô mà chuột trỏ tới
        this.currentMouseTileX = (int) Math.floor(currentMouseWorldX / WorldConfig.TILE_SIZE);
        this.currentMouseTileY = (int) Math.floor(currentMouseWorldY / WorldConfig.TILE_SIZE);

        mainGameView.updateSelector(
                this.currentMouseTileX,       // Vị trí X của ô được chọn
                this.currentMouseTileY,       // Vị trí Y của ô được chọn
                camera.getWorldOffsetX(),     // Vị trí X của thế giới
                camera.getWorldOffsetY()      // Vị trí Y của thế giới
        );
    }
    
    /**
     * Cập nhật ghost placement (bóng mờ khi cầm item có thể đặt)
     */
    private void updateGhostPlacement() {
        ItemStack currentItem = mainPlayer.getCurrentItem();
        // [SỬA] Truyền thêm thông tin tọa độ thực tế vào MainGameView để hỗ trợ đặt động vật tự do
        // (MainGameView và WorldRenderer cần được sửa để xử lý logic này nếu muốn hiển thị bóng mờ tự do)
        // Hiện tại chúng ta vẫn dùng tile-based cho cây trồng/rào, nhưng động vật sẽ đặt ở mouseWorldX/Y
        // Tạm thời logic hiển thị ghost placement vẫn dựa trên TileX/Y trong WorldRenderer
        // Nếu bạn muốn ghost động vật đi theo chuột, cần sửa WorldRenderer.updateGhostPlacement
        mainGameView.updateGhostPlacement(
                this.currentMouseTileX,
                this.currentMouseTileY,
                camera.getWorldOffsetX(),
                camera.getWorldOffsetY(),
                currentItem
        );
    }
    
    /**
     * Cập nhật collision hitbox (debug mode)
     */
    private void updateCollisionHitbox() {
        if (com.example.farmSimulation.config.PlayerSpriteConfig.DEBUG_PLAYER_BOUNDS) {
            mainGameView.updateCollisionHitbox(
                    mainPlayer.getTileX(),
                    mainPlayer.getTileY(),
                    camera.getWorldOffsetX(),
                    camera.getWorldOffsetY(),
                    playerView.getDebugCollisionHitbox()
            );
        }
    }

    /**
     * Kiểm tra xem người chơi có trong tầm tương tác không
     */
    private boolean isPlayerInRange(int col, int row, ItemStack currentStack) {
        // Tọa độ pixel logic của Tâm người chơi (sử dụng giá trị đã scale)
        double scaledPlayerWidth = PlayerSpriteConfig.BASE_PLAYER_FRAME_WIDTH * PlayerSpriteConfig.BASE_PLAYER_FRAME_SCALE;
        double scaledPlayerHeight = PlayerSpriteConfig.BASE_PLAYER_FRAME_HEIGHT * PlayerSpriteConfig.BASE_PLAYER_FRAME_SCALE;
        double playerX = mainPlayer.getTileX() + scaledPlayerWidth / 2;
        
        // [SỬA] Cộng thêm INTERACTION_CENTER_Y_OFFSET để tâm tính toán hạ thấp xuống (ngang hông/chân)
        double playerY = mainPlayer.getTileY() + scaledPlayerHeight / 2 + PlayerSpriteConfig.INTERACTION_CENTER_Y_OFFSET;

        // Tọa độ pixel logic của TÂM ô target
        double targetX = (col * WorldConfig.TILE_SIZE) + (WorldConfig.TILE_SIZE / 2.0);
        double targetY = (row * WorldConfig.TILE_SIZE) + (WorldConfig.TILE_SIZE / 2.0);

        // Tính khoảng cách
        double distance = Math.sqrt(
                Math.pow(playerX - targetX, 2) + Math.pow(playerY - targetY, 2)
        );

        // Mặc định là HAND range (đã scale theo BASE_PLAYER_FRAME_SCALE)
        double range = GameLogicConfig.HAND_INTERACTION_RANGE * PlayerSpriteConfig.BASE_PLAYER_FRAME_SCALE;

        // Lấy tầm tương tác dựa trên công cụ (tất cả đều scale theo BASE_PLAYER_FRAME_SCALE)
        if (currentStack != null) {
            ItemType type = currentStack.getItemType();

            if (type == ItemType.HOE) {
                range = GameLogicConfig.HOE_INTERACTION_RANGE * PlayerSpriteConfig.BASE_PLAYER_FRAME_SCALE;
            } else if (type == ItemType.WATERING_CAN) {
                range = GameLogicConfig.WATERING_CAN_INTERACTION_RANGE * PlayerSpriteConfig.BASE_PLAYER_FRAME_SCALE;
            } else if (type == ItemType.PICKAXE) {
                range = GameLogicConfig.PICKAXE_INTERACTION_RANGE * PlayerSpriteConfig.BASE_PLAYER_FRAME_SCALE;
            } else if (type == ItemType.SHOVEL) {
                range = GameLogicConfig.SHOVEL_INTERACTION_RANGE * PlayerSpriteConfig.BASE_PLAYER_FRAME_SCALE;
            } else if (type == ItemType.FERTILIZER) {
                range = GameLogicConfig.FERTILIZER_INTERACTION_RANGE * PlayerSpriteConfig.BASE_PLAYER_FRAME_SCALE;
            } else if (type.name().startsWith("SEEDS_")) {
                range = GameLogicConfig.PLANT_INTERACTION_RANGE * PlayerSpriteConfig.BASE_PLAYER_FRAME_SCALE; // Áp dụng cho tất cả loại hạt
            } else if (type == ItemType.ITEM_COW || type == ItemType.ITEM_CHICKEN || 
                       type == ItemType.ITEM_PIG || type == ItemType.ITEM_SHEEP || 
                       type == ItemType.EGG) {
                range = AnimalConfig.PLACEMENT_RANGE * WorldConfig.TILE_SIZE * PlayerSpriteConfig.BASE_PLAYER_FRAME_SCALE;
            } else {
                // Các item khác dùng mặc định HAND range (đã scale)
                range = GameLogicConfig.HAND_INTERACTION_RANGE * PlayerSpriteConfig.BASE_PLAYER_FRAME_SCALE;
            }
        }

        return distance <= range;
    }

    /**
     * Quay hướng người chơi về phía ô (col, row)
     */
    private void updatePlayerDirectionTowards(int col, int row) {
        // Tọa độ pixel logic của Tâm người chơi (sử dụng giá trị đã scale)
        double scaledPlayerWidth = PlayerSpriteConfig.BASE_PLAYER_FRAME_WIDTH * PlayerSpriteConfig.BASE_PLAYER_FRAME_SCALE;
        double scaledPlayerHeight = PlayerSpriteConfig.BASE_PLAYER_FRAME_HEIGHT * PlayerSpriteConfig.BASE_PLAYER_FRAME_SCALE;
        double playerX = mainPlayer.getTileX() + scaledPlayerWidth / 2;
        
        // [SỬA] Cộng thêm INTERACTION_CENTER_Y_OFFSET để tâm quay hướng cũng chính xác
        double playerY = mainPlayer.getTileY() + scaledPlayerHeight / 2 + PlayerSpriteConfig.INTERACTION_CENTER_Y_OFFSET;

        // Tọa độ pixel logic của TÂM ô target
        double targetX = (col * WorldConfig.TILE_SIZE) + (WorldConfig.TILE_SIZE / 2.0);
        double targetY = (row * WorldConfig.TILE_SIZE) + (WorldConfig.TILE_SIZE / 2.0);

        // Tính góc (atan2(y, x) = góc giữa vector (x, y) và trục X dương)
        double angleDeg = Math.toDegrees(Math.atan2(targetY - playerY, targetX - playerX));

        // Quyết định hướng theo trục Oy ngược
        // -45 đến 45 = RIGHT
        // 45 đến 135 = DOWN
        // 135 đến 180 hoặc -180 đến -135 = LEFT
        // -135 đến -45 = UP
        if (angleDeg > -45 && angleDeg <= 45) {
            mainPlayer.setDirection(PlayerView.Direction.RIGHT);
        } else if (angleDeg > 45 && angleDeg <= 135) {
            mainPlayer.setDirection(PlayerView.Direction.DOWN);
        } else if (angleDeg > 135 || angleDeg < -135) {
            mainPlayer.setDirection(PlayerView.Direction.LEFT);
        } else { // (-135 đến -45)
            mainPlayer.setDirection(PlayerView.Direction.UP);
        }
    }

    /**
     * Xử lý click chuột
     * Hàm này chỉ kiểm tra điều kiện chung và ủy quyền
     */
    public void interactWithTile(int col, int row) {
        // Kiểm tra xem player có đang bận (làm hành động khác) không
        PlayerView.PlayerState currentState = mainPlayer.getState();
        if (currentState != PlayerView.PlayerState.IDLE && currentState != PlayerView.PlayerState.WALK) {
            return; // Player đang bận, không cho hành động
        }

        // Lấy công cụ *trước* khi kiểm tra tầm
        ItemStack currentStack = mainPlayer.getCurrentItem();

        // Kiểm tra tầm hoạt động (Sử dụng hàm isPlayerInRange)
        if (!isPlayerInRange(col, row, currentStack)) {
            // Vị trí hiển thị của text
            double playerScreenX = playerView.getSpriteContainer().getLayoutX();
            double playerScreenY = playerView.getSpriteContainer().getLayoutY() + PlayerSpriteConfig.PLAYER_SPRITE_OFFSET_Y;

            // Hiển thị text "It's too far"
            mainGameView.showTemporaryText(HudConfig.TOO_FAR_TEXT, playerScreenX, playerScreenY);
            return; // Quá xa, không làm gì cả
        }

        // Quay người chơi về hướng ô target
        updatePlayerDirectionTowards(col, row);

        // [SỬA] Sử dụng biến currentMouseWorldX/Y đã được cập nhật chính xác trong updateMouseSelector
        // Thay vì tính lại từ đầu, ta dùng giá trị đã được đồng bộ với camera mới nhất
        double mouseWorldX = this.currentMouseWorldX;
        double mouseWorldY = this.currentMouseWorldY;
        
        // Lưu lại state trước khi hành động
        PlayerView.PlayerState oldState = mainPlayer.getState();

        // Bước 1: Ưu tiên xử lý tương tác động vật (nếu đang cầm item động vật hoặc EGG)
        // Gọi processAnimalInteraction TRƯỚC với tọa độ chuột thực tế (để đặt tự do)
        String animalErrorMsg = interactionManager.processAnimalInteraction(mainPlayer, playerView, mouseWorldX, mouseWorldY);
        
        // Nếu processAnimalInteraction đã xử lý (trả về lỗi hoặc thành công), hiển thị thông báo và return
        if (animalErrorMsg != null) {
            double playerScreenX = playerView.getSpriteContainer().getLayoutX();
            double playerScreenY = playerView.getSpriteContainer().getLayoutY() + PlayerSpriteConfig.PLAYER_SPRITE_OFFSET_Y;
            mainGameView.showTemporaryText(animalErrorMsg, playerScreenX, playerScreenY);
            return; // Đã xử lý xong, không cần gọi processInteraction
        }
        
        // [QUAN TRỌNG - SỬA LỖI FALL-THROUGH]
        // Kiểm tra xem hành động với động vật có THÀNH CÔNG hay không (state thay đổi sang BUSY, AXE...)
        // Nếu thành công thì không chạy tiếp xuống logic đào đất/trồng cây
        if (mainPlayer.getState() != oldState) {
             mainGameView.updateHotbar();
             return; // Dừng lại ở đây
        }
        
        // [MỚI] Nếu đặt động vật thành công (trả về null và không lỗi), cần cập nhật Hotbar
        // Kiểm tra xem có phải vừa đặt động vật không?
        // Nếu InteractionManager.processAnimalInteraction trả về null, nó có thể là "không làm gì" HOẶC "thành công".
        // Để chắc chắn, ta gọi updateHotbar() ở đây để đồng bộ Item
        mainGameView.updateHotbar();
        
        // Bước 2: Nếu không phải tương tác động vật, xử lý tương tác với tile (cây trồng/đất)
        // Nhận thông báo lỗi trực tiếp từ hàm processInteraction
        String errorMsg = interactionManager.processInteraction(mainPlayer, playerView, worldMap, col, row);

        // Nếu errorMsg != null nghĩa là có lỗi hoặc không hành động được -> Hiển thị text
        if (errorMsg != null) {
            double playerScreenX = playerView.getSpriteContainer().getLayoutX();
            double playerScreenY = playerView.getSpriteContainer().getLayoutY() + PlayerSpriteConfig.PLAYER_SPRITE_OFFSET_Y;
            mainGameView.showTemporaryText(errorMsg, playerScreenX, playerScreenY);
        }
    }
    /**
     * Được gọi từ Controller khi người dùng đổi slot hotbar
     */
    public void changeHotbarSlot(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < HotbarConfig.HOTBAR_SLOT_COUNT) {
            mainPlayer.setSelectedHotbarSlot(slotIndex);
            mainGameView.updateHotbar(); // Yêu cầu View vẽ lại
        }
    }

    /**
     * Được gọi từ HotbarView khi người dùng kéo thả item
     */
    public void swapHotbarItems(int indexA, int indexB) {
        mainPlayer.swapHotbarItems(indexA, indexB);
        mainGameView.updateHotbar(); // Cập nhật lại giao diện ngay
    }

    /**
     * Mở/đóng hàng rào (được gọi từ Controller khi click chuột phải)
     */
    public void toggleFence(int col, int row) {
        // [SỬA] Thêm kiểm tra tầm hoạt động bằng Tay (Hand)
        // Truyền null vào currentStack để sử dụng HAND_INTERACTION_RANGE mặc định
        if (!isPlayerInRange(col, row, null)) {
            // Hiển thị thông báo "Xa quá"
            double playerScreenX = playerView.getSpriteContainer().getLayoutX();
            double playerScreenY = playerView.getSpriteContainer().getLayoutY() + PlayerSpriteConfig.PLAYER_SPRITE_OFFSET_Y;
            mainGameView.showTemporaryText(HudConfig.TOO_FAR_TEXT, playerScreenX, playerScreenY);
            return; 
        }

        TileData data = worldMap.getTileData(col, row);
        if (data.getFenceData() != null) {
            fenceManager.toggleFence(col, row);
            actionManager.setMapNeedsUpdate(true);
            mainGameView.updateMap(camera.getWorldOffsetX(), camera.getWorldOffsetY(), true);
        }
    }

    public void toggleSettingsMenu() {
        this.isPaused = !this.isPaused; // Sử dụng this.isPaused
        if (this.isPaused) {
            // Đóng Shop nếu đang mở để tránh overlap
            if (mainGameView != null && mainGameView.getShopView() != null && mainGameView.getShopView().isShopVisible()) {
                mainGameView.getShopView().toggle();
            }
            
            if (gameLoop != null) {
                gameLoop.stop(); // ⬅️ Dừng game loop
                //System.out.println("Game Loop đã dừng.");
            }
            mainGameView.showSettingsMenu(mainPlayer.getName(), mainPlayer.getLevel());
        } else {
            if (gameLoop != null) {
                gameLoop.start(); // ⬅️ Tiếp tục game loop
                //System.out.println("Game Loop đã tiếp tục.");
            }
            mainGameView.hideSettingsMenu();
        }
    }
    
    /**
     * [MỚI] Toggle thời tiết (dùng cho test)
     */
    public void toggleWeather() {
        if (weatherManager != null) {
            if (weatherManager.isRaining()) {
                weatherManager.setWeather(com.example.farmSimulation.config.WeatherConfig.WeatherType.SUNNY);
            } else {
                weatherManager.setWeather(com.example.farmSimulation.config.WeatherConfig.WeatherType.RAIN);
            }
        }
    }
    
    /**
     * [MỚI] Getter cho ShopManager
     */
    public ShopManager getShopManager() {
        return shopManager;
    }
    
    /**
     * [MỚI] Getter cho WeatherManager
     */
    public WeatherManager getWeatherManager() {
        return weatherManager;
    }
    
    /**
     * Tính slot index từ tọa độ chuột (trong scene coordinates)
     */
    public int getHotbarSlotFromMouse(double mouseX, double mouseY) {
        if (mainGameView == null || mainGameView.getHotbarView() == null) {
            return -1;
        }
        
        // Chuyển tọa độ scene sang tọa độ local của HotbarView
        javafx.geometry.Point2D scenePoint = new javafx.geometry.Point2D(mouseX, mouseY);
        javafx.geometry.Point2D localPoint = mainGameView.getHotbarView().sceneToLocal(scenePoint);
        
        // Gọi hàm trong HotbarView để tính slot index
        return mainGameView.getHotbarView().getSlotIndexFromMouse(localPoint.getX(), localPoint.getY());
    }
    
    /**
     * Ném item từ hotbar slot chỉ định xuống dưới chân player
     * @param slotIndex Slot index cần ném item
     */
    public void dropItemFromHotbar(int slotIndex) {
        // Kiểm tra xem player có đang bận không
        PlayerView.PlayerState currentState = mainPlayer.getState();
        if (currentState != PlayerView.PlayerState.IDLE && currentState != PlayerView.PlayerState.WALK) {
            return; // Player đang bận, không cho ném item
        }
        
        // Kiểm tra slot index hợp lệ
        if (slotIndex < 0 || slotIndex >= mainPlayer.getHotbarItems().length) {
            return;
        }
        
        // Lấy item từ slot chỉ định
        ItemStack stackToDrop = mainPlayer.getHotbarItems()[slotIndex];
        if (stackToDrop == null) {
            return; // Không có item để ném
        }
        
        // Tính toán vị trí player (vị trí để ném item)
        // Item ném ra từ vị trí player (tileX, tileY), giống như thịt rơi từ động vật
        // Vị trí player là góc trên-trái của sprite, item ném từ đó
        double playerX = mainPlayer.getTileX() + (PlayerSpriteConfig.BASE_PLAYER_FRAME_WIDTH * PlayerSpriteConfig.BASE_PLAYER_FRAME_SCALE) / 2.0; // Tâm ngang
        double playerY = mainPlayer.getTileY() + (PlayerSpriteConfig.BASE_PLAYER_FRAME_HEIGHT * PlayerSpriteConfig.BASE_PLAYER_FRAME_SCALE) - ItemSpriteConfig.ITEM_SPRITE_HEIGHT / 2.0; // Gần chân player
        
        // Ném item xuống dưới chân player (không theo hướng chuột)
        // Vị trí đích: ngay dưới chân player, với scatter ngẫu nhiên nhỏ
        double targetX = playerX;
        double targetY = playerY + WorldConfig.TILE_SIZE * 0.3; // Ném xuống dưới một chút
        
        // Tính tile và offset
        int targetTileCol = (int) Math.floor(targetX / WorldConfig.TILE_SIZE);
        int targetTileRow = (int) Math.floor(targetY / WorldConfig.TILE_SIZE);
        
        // Tính offset trong tile (để item không dính ở giữa ô)
        double offsetX = targetX - (targetTileCol * WorldConfig.TILE_SIZE);
        double offsetY = targetY - (targetTileRow * WorldConfig.TILE_SIZE);
        
        // Trừ đi một nửa kích thước item để item nằm giữa điểm đó
        offsetX -= ItemSpriteConfig.ITEM_SPRITE_WIDTH / 2.0;
        offsetY -= ItemSpriteConfig.ITEM_SPRITE_HEIGHT / 2.0;
        
        // Thêm scatter ngẫu nhiên nhỏ để item không bị dính chặt
        double scatter = GameLogicConfig.ITEM_DROP_SCATTER_RANGE * 0.5; // Scatter nhỏ hơn một chút
        offsetX += (Math.random() - 0.5) * scatter;
        offsetY += (Math.random() - 0.5) * scatter;
        
        // Lấy item type và số lượng
        ItemType itemType = stackToDrop.getItemType();
        int amount = stackToDrop.getQuantity();
        
        // Tìm ô trống xung quanh để đặt item (giống như thịt)
        int searchRadius = GameLogicConfig.ITEM_DROP_SEARCH_RADIUS;
        int finalCol = -1;
        int finalRow = -1;
        boolean foundSpot = false;
        
        // 1. Kiểm tra ô lý tưởng trước
        TileData idealTile = worldMap.getTileData(targetTileCol, targetTileRow);
        if (idealTile.getGroundItem() == null) {
            finalCol = targetTileCol;
            finalRow = targetTileRow;
            foundSpot = true;
        } else if (idealTile.getGroundItem() == itemType) {
            // Trùng loại -> Cộng dồn
            finalCol = targetTileCol;
            finalRow = targetTileRow;
            foundSpot = true;
        } else {
            // Ô lý tưởng đã có item khác -> Tìm xung quanh
            for (int r = targetTileRow - searchRadius; r <= targetTileRow + searchRadius; r++) {
                for (int c = targetTileCol - searchRadius; c <= targetTileCol + searchRadius; c++) {
                    if (r == targetTileRow && c == targetTileCol) continue; // Đã check rồi
                    
                    TileData checkTile = worldMap.getTileData(c, r);
                    if (checkTile.getGroundItem() == null) {
                        finalCol = c;
                        finalRow = r;
                        foundSpot = true;
                        break;
                    }
                }
                if (foundSpot) break;
            }
        }
        
        // Nếu vẫn không tìm thấy chỗ -> Bắt buộc phải đè lên ô lý tưởng
        if (!foundSpot) {
            finalCol = targetTileCol;
            finalRow = targetTileRow;
        }
        
        // Đặt item vào ô đã chọn
        TileData finalTile = worldMap.getTileData(finalCol, finalRow);
        
        // Lấy độ bền hiện tại của item (nếu có)
        int itemDurability = stackToDrop.getCurrentDurability();
        
        // Nếu cộng dồn
        if (finalTile.getGroundItem() == itemType) {
            finalTile.setGroundItemAmount(finalTile.getGroundItemAmount() + amount);
            // Giữ nguyên offset cũ của item đang có
            // Lưu ý: Với stackable items, độ bền không quan trọng, nhưng vẫn cần lưu cho tools
            if (itemType.hasDurability()) {
                finalTile.setGroundItemDurability(itemDurability); // Cập nhật độ bền khi cộng dồn
            }
        } else {
            // Đặt mới hoặc đè
            finalTile.setGroundItem(itemType);
            finalTile.setGroundItemAmount(amount);
            // Lưu độ bền của item (0 nếu không có độ bền hoặc không áp dụng)
            finalTile.setGroundItemDurability(itemType.hasDurability() ? itemDurability : 0);
            
            // Nếu đặt đúng ô lý tưởng -> Dùng offset đã tính
            if (finalCol == targetTileCol && finalRow == targetTileRow) {
                finalTile.setGroundItemOffsetX(offsetX);
                finalTile.setGroundItemOffsetY(offsetY);
            } else {
                // Nếu phải đặt sang ô bên cạnh -> Dùng offset mặc định cộng thêm scatter
                finalTile.setDefaultItemOffset();
                double jitterX = (Math.random() - 0.5) * GameLogicConfig.ITEM_DROP_SCATTER_RANGE;
                double jitterY = (Math.random() - 0.5) * GameLogicConfig.ITEM_DROP_SCATTER_RANGE;
                finalTile.setGroundItemOffsetX(finalTile.getGroundItemOffsetX() + jitterX);
                finalTile.setGroundItemOffsetY(finalTile.getGroundItemOffsetY() + jitterY);
            }
        }
        
        worldMap.setTileData(finalCol, finalRow, finalTile);
        actionManager.setMapNeedsUpdate(true);
        
        // Xóa item khỏi hotbar slot chỉ định
        mainPlayer.getHotbarItems()[slotIndex] = null;
        
        // Cập nhật hotbar view
        mainGameView.updateHotbar();
    }
}