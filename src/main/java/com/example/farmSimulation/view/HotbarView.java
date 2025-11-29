package com.example.farmSimulation.view;

import com.example.farmSimulation.config.AssetPaths;
import com.example.farmSimulation.config.HotbarConfig;
import com.example.farmSimulation.config.ItemSpriteConfig;
import com.example.farmSimulation.config.WindowConfig;
import com.example.farmSimulation.model.CropType;
import com.example.farmSimulation.model.ItemStack;
import com.example.farmSimulation.model.ItemType;
import com.example.farmSimulation.model.Player;
import com.example.farmSimulation.view.assets.AssetManager;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class HotbarView extends Pane {

    private final Player player;
    private final AssetManager assetManager;

    // Mỗi slot là một StackPane để chứa (Nền, Icon, Số thứ tự, Số lượng)
    private final StackPane[] slots;
    private final Rectangle slotSelector; // Ô chọn
    private final Map<ItemType, Image> itemTextureMap; // Map ItemType -> Image
    private final Text itemNameLabel; // Label hiển thị tên item đang cầm (ở giữa HUD, phía trên hotbar)

    // Biến lưu trữ scale hiện tại của hotbar (được cập nhật từ Settings)
    private double currentScale = HotbarConfig.DEFAULT_HOTBAR_SCALE;

    // Biến cho Drag & Drop
    private BiConsumer<Integer, Integer> onSwapListener; // Callback gọi về GameManager (slotA, slotB)
    private java.util.function.BiFunction<Integer, javafx.geometry.Point2D, Boolean> onItemDropListener; // Callback cho drop item (slotIndex, scenePoint) -> isTrash
    private ImageView ghostIcon; // Icon bay theo chuột
    private int dragSourceIndex = -1; // Vị trí bắt đầu kéo
    private double mouseAnchorX, mouseAnchorY; // Điểm neo khi bắt đầu kéo để tính offset

    public HotbarView(Player player, AssetManager assetManager) {
        this.player = player;
        this.assetManager = assetManager;
        this.itemTextureMap = new EnumMap<>(ItemType.class);
        this.slots = new StackPane[HotbarConfig.HOTBAR_SLOT_COUNT];

        // Khởi tạo Ghost Icon (ẩn)
        this.ghostIcon = new ImageView();
        this.ghostIcon.setMouseTransparent(true); // Không cản sự kiện chuột
        this.ghostIcon.setVisible(false);
        // Ghost icon phải được thêm vào Pane cuối cùng để nằm trên cùng
        // Nhưng sẽ thêm sau khi vòng lặp slot chạy xong

        // Tải ảnh cho từng công cụ
        loadItemTextures(assetManager);

        // Khởi tạo các slot
        for (int i = 0; i < HotbarConfig.HOTBAR_SLOT_COUNT; i++) {
            StackPane slot = new StackPane();
            slot.setAlignment(Pos.CENTER); // Mặc định căn giữa cho icon

            // 0. Nền
            Rectangle bg = new Rectangle();
            bg.setFill(HotbarConfig.SLOT_BACKGROUND_COLOR);
            bg.setStroke(HotbarConfig.SLOT_BORDER_COLOR);

            // 1. Icon
            ImageView icon = new ImageView();

            // [QUAN TRỌNG - MỚI THÊM] Gắn sự kiện kéo thả cho icon
            setupDragHandlers(icon, i);

            // 2. Thanh nền độ bền (Màu đen)
            Rectangle durBg = new Rectangle();
            durBg.setFill(HotbarConfig.DURABILITY_BG_COLOR);
            durBg.setVisible(false);

            // 3. Thanh độ bền (Chỉ 1 thanh màu, bỏ nền đen)
            Rectangle durBar = new Rectangle();
            durBar.setFill(HotbarConfig.DURABILITY_COLOR_HIGH);
            durBar.setVisible(false);

            // 4. Số thứ tự (Góc trái trên)
            int keyNum = (i + 1) % 10; // 1, 2... 9, 0
            Text keyLabel = new Text(String.valueOf(keyNum));
            keyLabel.setFont(HotbarConfig.HOTBAR_NUMBER_FONT);
            keyLabel.setFill(HotbarConfig.HOTBAR_TEXT_COLOR);
            keyLabel.setStroke(HotbarConfig.HOTBAR_TEXT_STROKE_COLOR);
            keyLabel.setStrokeWidth(HotbarConfig.HOTBAR_TEXT_STROKE_WIDTH);
            // Căn chỉnh thủ công trong updateLayout

            // 5. Số lượng (Góc giữa dưới)
            Text qtyLabel = new Text("");
            qtyLabel.setFont(HotbarConfig.HOTBAR_QUANTITY_FONT);
            qtyLabel.setFill(HotbarConfig.HOTBAR_TEXT_COLOR);
            qtyLabel.setStroke(HotbarConfig.HOTBAR_TEXT_STROKE_COLOR);
            qtyLabel.setStrokeWidth(HotbarConfig.HOTBAR_TEXT_STROKE_WIDTH);


            // Thêm theo thứ tự vẽ: Nền -> Icon -> DurBg -> DurBar -> Text
            slot.getChildren().addAll(bg, icon, durBg, durBar, keyLabel, qtyLabel);
            this.slots[i] = slot;
            this.getChildren().add(slot);
        }

        this.slotSelector = new Rectangle();
        this.slotSelector.setFill(null);
        this.slotSelector.setStroke(HotbarConfig.SLOT_SELECTED_BORDER_COLOR);
        this.getChildren().add(slotSelector);

        // Khởi tạo Label hiển thị tên item
        this.itemNameLabel = new Text("");
        this.itemNameLabel.setFont(HotbarConfig.HOTBAR_ITEM_NAME_FONT);
        this.itemNameLabel.setFill(HotbarConfig.HOTBAR_TEXT_COLOR);
        this.itemNameLabel.setStroke(HotbarConfig.HOTBAR_TEXT_STROKE_COLOR);
        this.itemNameLabel.setStrokeWidth(HotbarConfig.HOTBAR_TEXT_STROKE_WIDTH);
        this.itemNameLabel.setVisible(false); // Ẩn ban đầu
        this.getChildren().add(itemNameLabel);

        // Thêm ghost icon vào view (lớp trên cùng)
        this.getChildren().add(ghostIcon);

        updateLayout(HotbarConfig.DEFAULT_HOTBAR_SCALE);
        updateView();
    }

    // Setter cho callback
    public void setOnSwapListener(BiConsumer<Integer, Integer> listener) {
        this.onSwapListener = listener;
    }
    
    /**
     * Set callback cho item drop (including trash can deletion)
     * Callback receives: (slotIndex, scenePoint) and returns true if dropped on trash
     */
    public void setOnItemDropListener(java.util.function.BiFunction<Integer, javafx.geometry.Point2D, Boolean> listener) {
        this.onItemDropListener = listener;
    }

    /**
     * Thiết lập sự kiện kéo thả cho ImageView
     */
    private void setupDragHandlers(ImageView icon, int slotIndex) {
        // 1. Bắt đầu nhấn chuột (Pressed)
        icon.setOnMousePressed(e -> {
            // Block drag when game is paused
            if (player.getMainGameView() != null && player.getMainGameView().getGameManager() != null 
                    && player.getMainGameView().getGameManager().isPaused()) {
                return;
            }
            
            // Chỉ kéo được nếu có item và dùng chuột trái
            if (icon.getImage() != null && e.isPrimaryButtonDown()) {
                dragSourceIndex = slotIndex;

                // Tính offset để icon bay theo chuột mượt mà (tránh bị giật về góc 0,0)
                mouseAnchorX = e.getX();
                mouseAnchorY = e.getY();

                // Setup Ghost Icon
                ghostIcon.setImage(icon.getImage());
                ghostIcon.setFitWidth(icon.getFitWidth());
                ghostIcon.setFitHeight(icon.getFitHeight());

                // Chuyển tọa độ chuột từ Icon (Local) sang Pane (HotbarView)
                Point2D pointInPane = icon.localToParent(e.getX(), e.getY());
                // Vì icon nằm trong StackPane, parent là StackPane (Slot).
                // Ta cần tọa độ trong HotbarView.
                // Slot.getLayoutX() + Icon trong Slot (đã căn giữa)
                // Cách đơn giản: Lấy tọa độ Scene rồi convert về HotbarView
                Point2D scenePoint = new Point2D(e.getSceneX(), e.getSceneY());
                Point2D localPoint = this.sceneToLocal(scenePoint);

                ghostIcon.setLayoutX(localPoint.getX() - (ghostIcon.getFitWidth() / 2));
                ghostIcon.setLayoutY(localPoint.getY() - (ghostIcon.getFitHeight() / 2));

                ghostIcon.setVisible(true);
                ghostIcon.setOpacity(0.7); // Làm mờ icon đang kéo

                // Làm mờ icon gốc (tùy chọn)
                icon.setOpacity(0.3);
                e.consume();
            }
        });

        // 2. Kéo chuột (Dragged)
        icon.setOnMouseDragged(e -> {
            // Block drag when game is paused
            if (player.getMainGameView() != null && player.getMainGameView().getGameManager() != null 
                    && player.getMainGameView().getGameManager().isPaused()) {
                return;
            }
            
            if (dragSourceIndex != -1 && e.isPrimaryButtonDown()) {
                // Cập nhật vị trí Ghost Icon theo chuột
                Point2D scenePoint = new Point2D(e.getSceneX(), e.getSceneY());
                Point2D localPoint = this.sceneToLocal(scenePoint);

                ghostIcon.setLayoutX(localPoint.getX() - (ghostIcon.getFitWidth() / 2));
                ghostIcon.setLayoutY(localPoint.getY() - (ghostIcon.getFitHeight() / 2));
                e.consume();
            }
        });

        // 3. Thả chuột (Released)
        icon.setOnMouseReleased(e -> {
            // Block drop when game is paused
            if (player.getMainGameView() != null && player.getMainGameView().getGameManager() != null 
                    && player.getMainGameView().getGameManager().isPaused()) {
                // Reset drag state if paused
                if (dragSourceIndex != -1) {
                    icon.setOpacity(1.0);
                    ghostIcon.setVisible(false);
                    dragSourceIndex = -1;
                }
                return;
            }
            
            if (dragSourceIndex != -1) {
                // Khôi phục icon gốc
                icon.setOpacity(1.0);

                // Ẩn ghost icon
                ghostIcon.setVisible(false);

                // Xác định slot đích
                int targetIndex = -1;

                // Chuyển tọa độ thả chuột sang tọa độ local của HotbarView
                Point2D scenePoint = new Point2D(e.getSceneX(), e.getSceneY());
                Point2D localPoint = this.sceneToLocal(scenePoint);

                // Duyệt qua các slot để xem chuột nằm trong slot nào
                for (int i = 0; i < slots.length; i++) {
                    if (slots[i].getBoundsInParent().contains(localPoint)) {
                        targetIndex = i;
                        break;
                    }
                }

                // Check if dropped on Trash Can (if not dropped on any hotbar slot)
                if (targetIndex == -1 && onItemDropListener != null) {
                    // Not dropped on hotbar slot - check if it's trash via callback
                    // Pass scene coordinates to callback for accurate trash detection (reuse scenePoint)
                    Boolean isTrash = onItemDropListener.apply(dragSourceIndex, scenePoint);
                    if (isTrash != null && isTrash) {
                        // Dropped on trash - item will be deleted by callback handler
                        // Don't do anything here, callback will handle deletion
                    }
                } else if (targetIndex != -1 && targetIndex != dragSourceIndex) {
                    // Normal swap - dropped on different hotbar slot
                    if (onSwapListener != null) {
                        onSwapListener.accept(dragSourceIndex, targetIndex);
                    }
                }

                dragSourceIndex = -1; // Reset
                e.consume();
            }
        });
    }

    /**
     * Hàm này tính toán lại toàn bộ layout của Hotbar dựa trên scale.
     * Được gọi từ Constructor và SettingsMenu
     *
     * @param scale Tỉ lệ scale mới
     */
    public void updateLayout(double scale) {
        this.currentScale = scale;

        // Tính toán các kích thước động
        double currentSlotSize = HotbarConfig.BASE_SLOT_SIZE * scale;
        double currentSpacing = HotbarConfig.BASE_SLOT_SPACING * scale;
        double currentStrokeWidth = HotbarConfig.BASE_STROKE_WIDTH * scale;

        // Tính toán kích thước thanh độ bền
        double barHeight = HotbarConfig.DURABILITY_BAR_HEIGHT * scale;

        // Hằng số item scale
        // Kích thước icon = kích thước ô * tỉ lệ
        double currentItemSize = currentSlotSize * HotbarConfig.ITEM_SCALE_RATIO;

        // Cập nhật layout cho từng ô
        for (int i = 0; i < HotbarConfig.HOTBAR_SLOT_COUNT; i++) {
            double x = i * (currentSlotSize + currentSpacing);

            StackPane slot = slots[i];
            slot.setLayoutX(x);
            slot.setLayoutY(0);
            slot.setPrefSize(currentSlotSize, currentSlotSize);

            // 0. Cập nhật Nền (Background)
            Rectangle bg = (Rectangle) slot.getChildren().get(0);
            bg.setWidth(currentSlotSize);
            bg.setHeight(currentSlotSize);
            bg.setStrokeWidth(currentStrokeWidth);

            // 1. Cập nhật Icon
            ImageView icon = (ImageView) slot.getChildren().get(1);
            icon.setFitWidth(currentItemSize);
            icon.setFitHeight(currentItemSize);
            icon.setTranslateY(HotbarConfig.ICON_Y_TRANSLATE * scale); // Dịch icon lên trên

            // Cấu hình chung cho vị trí thanh độ bền
            double maxBarWidth = currentSlotSize * HotbarConfig.DURABILITY_BAR_WIDTH_RATIO;
            double sidePadding = (currentSlotSize - maxBarWidth) / 2;
            double barYOffset = HotbarConfig.DURABILITY_BAR_Y_OFFSET * scale;

            // 2. Cập nhật layout thanh nền độ bền (DurBg)
            Rectangle durBg = (Rectangle) slot.getChildren().get(2);
            durBg.setHeight(barHeight);
            durBg.setWidth(maxBarWidth); // Full width cho background
            StackPane.setAlignment(durBg, Pos.BOTTOM_LEFT);
            durBg.setTranslateX(sidePadding);
            durBg.setTranslateY(barYOffset);

            // 3. Cập nhật layout thanh độ bền (DurBar)
            Rectangle durBar = (Rectangle) slot.getChildren().get(3);
            durBar.setHeight(barHeight);
            // Width của durBar sẽ được update trong updateView dựa trên % độ bền
            StackPane.setAlignment(durBar, Pos.BOTTOM_LEFT);
            durBar.setTranslateX(sidePadding);
            durBar.setTranslateY(barYOffset);

            // 4. Căn chỉnh Text Số thứ tự
            Text keyLabel = (Text) slot.getChildren().get(4);
            StackPane.setAlignment(keyLabel, Pos.TOP_LEFT);
            keyLabel.setTranslateX(HotbarConfig.HOTBAR_TEXT_PADDING * scale);
            keyLabel.setTranslateY(HotbarConfig.HOTBAR_TEXT_PADDING * scale);

            // 5. Căn chỉnh Text Số lượng
            Text qtyLabel = (Text) slot.getChildren().get(5);
            StackPane.setAlignment(qtyLabel, Pos.BOTTOM_RIGHT);
            qtyLabel.setTranslateX(-HotbarConfig.HOTBAR_TEXT_PADDING * scale);
            qtyLabel.setTranslateY(-HotbarConfig.HOTBAR_TEXT_PADDING * scale);
        }

        // Cập nhật Ô chọn (Selector)
        this.slotSelector.setWidth(currentSlotSize);
        this.slotSelector.setHeight(currentSlotSize);
        this.slotSelector.setStrokeWidth(currentStrokeWidth);
        updateSelectorPosition(); // Gọi hàm helper để đặt đúng vị trí

        // Tính toán và cập nhật vị trí của TOÀN BỘ PANE (HotbarView)
        double totalWidth = (HotbarConfig.HOTBAR_SLOT_COUNT * currentSlotSize) + ((HotbarConfig.HOTBAR_SLOT_COUNT - 1) * currentSpacing);
        double yOffset = HotbarConfig.BASE_Y_OFFSET * scale; // Scale cả khoảng cách từ đáy

        this.setLayoutX((WindowConfig.SCREEN_WIDTH - totalWidth) / 2); // Tự động căn giữa
        this.setLayoutY(WindowConfig.SCREEN_HEIGHT - currentSlotSize - yOffset); // Cách đáy
        
        // Cập nhật Label tên item (ở giữa, phía trên hotbar)
        double itemNameFontSize = HotbarConfig.BASE_ITEM_NAME_FONT_SIZE * scale;
        itemNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, itemNameFontSize));
        itemNameLabel.setStrokeWidth(HotbarConfig.HOTBAR_TEXT_STROKE_WIDTH * scale);
        
        // Tính vị trí Y của label (phía trên hotbar) - sẽ được cập nhật trong updateView()
        double itemNameY = -HotbarConfig.ITEM_NAME_Y_OFFSET * scale;
        itemNameLabel.setY(itemNameY);
        itemNameLabel.setTextOrigin(javafx.geometry.VPos.BASELINE); // Căn theo baseline
    }

    /**
     * Cắt ảnh từ sprite sheet items_32x32.png và cache
     */
    private void loadItemTextures(AssetManager assetManager) {
        Image itemsSheet = assetManager.getTexture(AssetPaths.ITEMS_SHEET);
        if (itemsSheet == null) return;

        // Cache Items (công cụ)
        cacheItemSprite(itemsSheet, ItemType.HOE, ItemSpriteConfig.ITEM_HOE_COL);
        cacheItemSprite(itemsSheet, ItemType.WATERING_CAN, ItemSpriteConfig.ITEM_WATERING_CAN_COL);
        cacheItemSprite(itemsSheet, ItemType.PICKAXE, ItemSpriteConfig.ITEM_PICKAXE_COL);
        cacheItemSprite(itemsSheet, ItemType.SHOVEL, ItemSpriteConfig.ITEM_SHOVEL_COL);
        cacheItemSprite(itemsSheet, ItemType.FERTILIZER, ItemSpriteConfig.ITEM_FERTILISER_COL);
        cacheItemSprite(itemsSheet, ItemType.AXE, ItemSpriteConfig.ITEM_AXE_COL);
        cacheItemSprite(itemsSheet, ItemType.SWORD, ItemSpriteConfig.ITEM_SWORD_COL); // Kiếm
        cacheItemSprite(itemsSheet, ItemType.SHEARS, ItemSpriteConfig.ITEM_SCISSORS_COL); // Kéo

        // Cache Seeds (Frame 0 từ crop sheet)
        cacheItemSprite(ItemType.SEEDS_STRAWBERRY, assetManager.getSeedIcon(CropType.STRAWBERRY));
        cacheItemSprite(ItemType.SEEDS_RADISH, assetManager.getSeedIcon(CropType.RADISH));
        cacheItemSprite(ItemType.SEEDS_POTATO, assetManager.getSeedIcon(CropType.POTATO));
        cacheItemSprite(ItemType.SEEDS_CARROT, assetManager.getSeedIcon(CropType.CARROT));

        // Cache Harvest Items (Frame cuối từ crop sheet)
        cacheItemSprite(ItemType.STRAWBERRY, assetManager.getHarvestIcon(CropType.STRAWBERRY));
        cacheItemSprite(ItemType.RADISH, assetManager.getHarvestIcon(CropType.RADISH));
        cacheItemSprite(ItemType.POTATO, assetManager.getHarvestIcon(CropType.POTATO));
        cacheItemSprite(ItemType.CARROT, assetManager.getHarvestIcon(CropType.CARROT));
        
        // Cache Wood (từ tree sheet)
        cacheItemSprite(ItemType.WOOD, assetManager.getWoodIcon());
        
        // Cache các item mới từ items_32x32.png
        cacheItemSprite(itemsSheet, ItemType.MILK_BUCKET, ItemSpriteConfig.ITEM_MILK_BUCKET_COL);
        cacheItemSprite(itemsSheet, ItemType.FULL_MILK_BUCKET, ItemSpriteConfig.ITEM_FULL_MILK_BUCKET_COL);
        cacheItemSprite(itemsSheet, ItemType.MEAT_CHICKEN, ItemSpriteConfig.ITEM_MEAT_CHICKEN_COL);
        cacheItemSprite(itemsSheet, ItemType.MEAT_COW, ItemSpriteConfig.ITEM_MEAT_COW_COL);
        cacheItemSprite(itemsSheet, ItemType.MEAT_PIG, ItemSpriteConfig.ITEM_MEAT_PIG_COL);
        cacheItemSprite(itemsSheet, ItemType.MEAT_SHEEP, ItemSpriteConfig.ITEM_MEAT_SHEEP_COL);
        cacheItemSprite(itemsSheet, ItemType.EGG, ItemSpriteConfig.ITEM_EGG_COL);
        cacheItemSprite(itemsSheet, ItemType.WOOL, ItemSpriteConfig.ITEM_WOOL_COL);
        cacheItemSprite(itemsSheet, ItemType.ENERGY_DRINK, ItemSpriteConfig.ITEM_ENERGY_DRINK_COL);
        cacheItemSprite(itemsSheet, ItemType.SUPER_FEED, ItemSpriteConfig.ITEM_SUPER_FEED_COL);
        
        // Cache các item vật nuôi sống (sử dụng getAnimalItemIcon để lấy icon đã resize)
        cacheItemSprite(ItemType.ITEM_COW, assetManager.getAnimalItemIcon(ItemType.ITEM_COW));
        cacheItemSprite(ItemType.ITEM_CHICKEN, assetManager.getAnimalItemIcon(ItemType.ITEM_CHICKEN));
        cacheItemSprite(ItemType.ITEM_SHEEP, assetManager.getAnimalItemIcon(ItemType.ITEM_SHEEP));
        cacheItemSprite(ItemType.ITEM_PIG, assetManager.getAnimalItemIcon(ItemType.ITEM_PIG));
    }

    /**
     * Hàm helper mới để vừa cắt, vừa cache vào map, vừa cache vào AssetManager
     */
    private void cacheItemSprite(Image sheet, ItemType type, int col) {
        PixelReader reader = sheet.getPixelReader();
        WritableImage icon = new WritableImage(reader, (int) (col * ItemSpriteConfig.ITEM_SPRITE_WIDTH), 0, (int) ItemSpriteConfig.ITEM_SPRITE_WIDTH, (int) ItemSpriteConfig.ITEM_SPRITE_HEIGHT);
        itemTextureMap.put(type, icon);
        // Gọi cacheItemIcon
        assetManager.cacheItemIcon(type, icon);
    }

    /**
     * Hàm helper mới để cache icon đã có sẵn (dùng cho hạt giống)
     */
    private void cacheItemSprite(ItemType type, Image icon) {
        if (icon != null) {
            itemTextureMap.put(type, icon);
            // Gọi cacheItemIcon
            assetManager.cacheItemIcon(type, icon);
        }
    }

    /**
     * Được gọi bởi GameManager khi đổi slot,
     * HOẶC khi layout thay đổi
     */
    public void updateView() {
        ItemStack[] items = player.getHotbarItems();
        // Tính bar width max cho tính toán tỉ lệ
        double slotSize = HotbarConfig.BASE_SLOT_SIZE * currentScale;
        double maxBarWidth = slotSize * HotbarConfig.DURABILITY_BAR_WIDTH_RATIO;
        
        // Cập nhật Label tên item đang cầm
        ItemStack currentItem = player.getCurrentItem();
        if (currentItem != null) {
            itemNameLabel.setText(currentItem.getItemType().getName());
            itemNameLabel.setVisible(true);
            // Cập nhật lại vị trí sau khi set text (vì width thay đổi)
            // Tính lại totalWidth của hotbar
            double totalWidth = (HotbarConfig.HOTBAR_SLOT_COUNT * slotSize) + ((HotbarConfig.HOTBAR_SLOT_COUNT - 1) * (HotbarConfig.BASE_SLOT_SPACING * currentScale));
            // Căn giữa theo hotbar (vì label có parent là HotbarView)
            double labelWidth = itemNameLabel.getLayoutBounds().getWidth();
            itemNameLabel.setX((totalWidth - labelWidth) / 2);
        } else {
            itemNameLabel.setVisible(false);
        }

        for (int i = 0; i < HotbarConfig.HOTBAR_SLOT_COUNT; i++) {
            ItemStack stack = items[i];
            ImageView icon = (ImageView) slots[i].getChildren().get(1);
            Rectangle durBg = (Rectangle) slots[i].getChildren().get(2);
            Rectangle durBar = (Rectangle) slots[i].getChildren().get(3);
            Text qtyLabel = (Text) slots[i].getChildren().get(5);

            if (stack != null) {
                icon.setImage(itemTextureMap.get(stack.getItemType()));
                icon.setVisible(true);

                // [LOGIC ĐỘ BỀN]
                if (HotbarConfig.SHOW_DURABILITY_BAR && stack.getItemType().hasDurability()) {
                    durBg.setVisible(true);
                    durBar.setVisible(true);

                    double ratio = (double) stack.getCurrentDurability() / stack.getItemType().getMaxDurability();
                    durBar.setWidth(maxBarWidth * ratio);

                    // Đổi màu theo độ bền
                    if (ratio > 0.5) durBar.setFill(HotbarConfig.DURABILITY_COLOR_HIGH);
                    else if (ratio > 0.2) durBar.setFill(HotbarConfig.DURABILITY_COLOR_MEDIUM);
                    else durBar.setFill(HotbarConfig.DURABILITY_COLOR_LOW);
                } else {
                    durBg.setVisible(false);
                    durBar.setVisible(false);
                }

                // [LOGIC SỐ LƯỢNG]
                if (stack.getItemType().isStackable()) {
                    qtyLabel.setText(String.valueOf(stack.getQuantity()));
                    qtyLabel.setVisible(true);
                } else {
                    qtyLabel.setVisible(false);
                }
            } else {
                icon.setImage(null);
                icon.setVisible(false);
                qtyLabel.setVisible(false);
                durBg.setVisible(false);
                durBar.setVisible(false);
            }
        }
        updateSelectorPosition();
    }

    /**
     * Hàm helper tính toán lại vị trí Ô CHỌN
     */
    private void updateSelectorPosition() {
        // Tính toán kích thước động
        double currentSlotSize = HotbarConfig.BASE_SLOT_SIZE * this.currentScale;
        double currentSpacing = HotbarConfig.BASE_SLOT_SPACING * this.currentScale;

        // Di chuyển ô chọn
        int selectedSlot = player.getSelectedHotbarSlot();
        double selectorX = selectedSlot * (currentSlotSize + currentSpacing);
        slotSelector.setLayoutX(selectorX);
    }

    /**
     * Tính toán tọa độ TÂM của một slot trên màn hình.
     * Dùng để làm đích đến cho animation thu hoạch.
     */
    /**
     * Tính slot index từ tọa độ chuột (trong HotbarView local coordinates)
     * @param mouseX Tọa độ X của chuột (trong HotbarView)
     * @param mouseY Tọa độ Y của chuột (trong HotbarView)
     * @return Slot index nếu chuột đang ở trên hotbar, -1 nếu không
     */
    public int getSlotIndexFromMouse(double mouseX, double mouseY) {
        double slotSize = HotbarConfig.BASE_SLOT_SIZE * currentScale;
        double spacing = HotbarConfig.BASE_SLOT_SPACING * currentScale;
        
        // Kiểm tra xem chuột có nằm trong vùng hotbar không (theo chiều cao)
        if (mouseY < 0 || mouseY > slotSize) {
            return -1; // Chuột không nằm trong hotbar
        }
        
        // Tính slot index từ tọa độ X
        for (int i = 0; i < HotbarConfig.HOTBAR_SLOT_COUNT; i++) {
            double slotX = i * (slotSize + spacing);
            if (mouseX >= slotX && mouseX < slotX + slotSize) {
                return i; // Tìm thấy slot
            }
        }
        
        return -1; // Không tìm thấy slot
    }
    
    public Point2D getSlotCenter(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= HotbarConfig.HOTBAR_SLOT_COUNT) return null;

        // Tính toán kích thước động hiện tại
        double currentSlotSize = HotbarConfig.BASE_SLOT_SIZE * this.currentScale;
        double currentSpacing = HotbarConfig.BASE_SLOT_SPACING * this.currentScale;

        // Tọa độ X của slot so với HotbarView (Local)
        double slotLocalX = slotIndex * (currentSlotSize + currentSpacing);
        // Tọa độ Y của slot so với HotbarView (Local) = 0 (vì Hotbar nằm ngang)

        // Chuyển sang tọa độ Global (Màn hình)
        // = Tọa độ Hotbar + Tọa độ Slot + Bán kính (để lấy tâm)
        double centerX = this.getLayoutX() + slotLocalX + (currentSlotSize / 2);
        double centerY = this.getLayoutY() + (currentSlotSize / 2);

        return new Point2D(centerX, centerY);
    }

    /**
     * Lấy scale hiện tại của hotbar
     */
    public double getCurrentScale() {
        return currentScale;
    }
}