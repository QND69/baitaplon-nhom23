package com.example.farmSimulation.view;

import com.example.farmSimulation.config.AssetPaths;
import com.example.farmSimulation.config.HotbarConfig;
import com.example.farmSimulation.config.ItemSpriteConfig;
import com.example.farmSimulation.config.WindowConfig;
import com.example.farmSimulation.model.Player;
import com.example.farmSimulation.model.Tool;
import com.example.farmSimulation.view.assets.AssetManager;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import java.util.EnumMap;
import java.util.Map;

public class HotbarView extends Pane {

    private final Player player;
    private final Rectangle[] slotBackgrounds; // Nền ô
    private final ImageView[] slotIcons;       // Icon công cụ
    private final Rectangle slotSelector;      // Ô chọn
    private final Map<Tool, Image> toolTextureMap; // Cache ảnh công cụ

    // Biến lưu trữ scale hiện tại
    private double currentScale = HotbarConfig.DEFAULT_HOTBAR_SCALE;

    public HotbarView(Player player, AssetManager assetManager) {
        this.player = player;
        this.toolTextureMap = new EnumMap<>(Tool.class);
        this.slotBackgrounds = new Rectangle[HotbarConfig.HOTBAR_SLOT_COUNT];
        this.slotIcons = new ImageView[HotbarConfig.HOTBAR_SLOT_COUNT];

        // Tải ảnh cho từng công cụ
        loadToolTextures(assetManager);

        // Khởi tạo chỉ TẠO đối tượng, không SET layout
        for (int i = 0; i < HotbarConfig.HOTBAR_SLOT_COUNT; i++) {
            // Nền
            Rectangle bg = new Rectangle();
            bg.setFill(HotbarConfig.SLOT_BACKGROUND_COLOR);
            bg.setStroke(HotbarConfig.SLOT_BORDER_COLOR);
            this.slotBackgrounds[i] = bg;

            // Icon
            ImageView icon = new ImageView();
            this.slotIcons[i] = icon;

            this.getChildren().addAll(bg, icon);
        }

        // Khởi tạo ô chọn
        this.slotSelector = new Rectangle();
        this.slotSelector.setFill(null);
        this.slotSelector.setStroke(HotbarConfig.SLOT_SELECTED_BORDER_COLOR);
        this.getChildren().add(slotSelector);

        // Gọi hàm updateLayout với scale mặc định
        updateLayout(HotbarConfig.DEFAULT_HOTBAR_SCALE);

        // Cập nhật icon lần đầu
        updateView();
    }

    /**
     * Hàm này tính toán lại toàn bộ layout của Hotbar dựa trên scale.
     * Được gọi từ Constructor và SettingsMenu
     * @param scale Tỉ lệ scale mới
     */
    public void updateLayout(double scale) {
        this.currentScale = scale;

        // Tính toán các kích thước động
        double currentSlotSize = HotbarConfig.BASE_SLOT_SIZE * scale;
        double currentSpacing = HotbarConfig.BASE_SLOT_SPACING * scale;
        double currentStrokeWidth = HotbarConfig.BASE_STROKE_WIDTH * scale;

        // Hằng số item scale
        // Kích thước icon = kích thước ô * tỉ lệ
        double currentItemSize = currentSlotSize * HotbarConfig.ITEM_SCALE_RATIO;
        double itemPadding = (currentSlotSize - currentItemSize) / 2; // Căn giữa 1 slot

        // Cập nhật layout cho từng ô
        for (int i = 0; i < HotbarConfig.HOTBAR_SLOT_COUNT; i++) {
            double x = i * (currentSlotSize + currentSpacing);

            // Cập nhật Nền (Background)
            Rectangle bg = this.slotBackgrounds[i];
            bg.setLayoutX(x);
            bg.setLayoutY(0);
            bg.setWidth(currentSlotSize);
            bg.setHeight(currentSlotSize);
            bg.setStrokeWidth(currentStrokeWidth);

            // Cập nhật Icon
            ImageView icon = this.slotIcons[i];
            icon.setLayoutX(x + itemPadding);
            icon.setLayoutY(itemPadding);
            icon.setFitWidth(currentItemSize);
            icon.setFitHeight(currentItemSize);
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
    }

    /**
     * Cắt ảnh từ sprite sheet tools.png và cache
     */
    private void loadToolTextures(AssetManager assetManager) {
        Image toolsSheet = assetManager.getTexture(AssetPaths.TOOLS_SHEET);
        if (toolsSheet == null) return;

        // Helper cắt ảnh
        toolTextureMap.put(Tool.HOE, getToolSprite(toolsSheet, ItemSpriteConfig.TOOL_HOE_COL));
        toolTextureMap.put(Tool.PICKAXE, getToolSprite(toolsSheet, ItemSpriteConfig.TOOL_PICKAXE_COL));
        toolTextureMap.put(Tool.WATERING_CAN, getToolSprite(toolsSheet, ItemSpriteConfig.TOOL_WATERING_CAN_COL));
    }

    /**
     * Hàm helper để cắt một sprite từ sheet
     */
    private Image getToolSprite(Image sheet, int col) {
        PixelReader reader = sheet.getPixelReader();
        double w = ItemSpriteConfig.TOOL_SPRITE_WIDTH;
        double h = ItemSpriteConfig.TOOL_SPRITE_HEIGHT;
        int x = (int) (col * w);
        int y = 0; // (Tất cả tool đều ở hàng 0)
        return new WritableImage(reader, x, y, (int) w, (int) h);
    }

    /**
     * Được gọi bởi GameManager khi đổi slot,
     * HOẶC khi layout thay đổi
     */
    public void updateView() {
        // Cập nhật icon (nội dung)
        Tool[] tools = player.getHotbarTools();
        for (int i = 0; i < HotbarConfig.HOTBAR_SLOT_COUNT; i++) {
            Tool tool = tools[i];
            if (tool != null) {
                slotIcons[i].setImage(toolTextureMap.get(tool)); // Lấy ảnh từ cache
                slotIcons[i].setVisible(true);
            } else {
                slotIcons[i].setImage(null);
                slotIcons[i].setVisible(false);
            }
        }
        // Cập nhật vị trí ô chọn
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
}