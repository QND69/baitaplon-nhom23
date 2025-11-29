package com.example.farmSimulation.view;

import com.example.farmSimulation.config.GameLogicConfig;
import com.example.farmSimulation.config.HudConfig;
import com.example.farmSimulation.config.WindowConfig;
import com.example.farmSimulation.model.GameManager;
import com.example.farmSimulation.view.assets.AssetManager;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class HudView extends Pane {
    // Reference để lấy thông tin player và weather
    private GameManager gameManager;
    private MainGameView mainGameView;
    private AssetManager assetManager; // Reference to AssetManager for GUI icons
    
    // Brightness setting (0.0 - 1.0)
    private double brightness = GameLogicConfig.DEFAULT_BRIGHTNESS;
    
    // --- Top-Left: Player Stats ---
    private final Rectangle levelRectangle; // Rounded Rectangle hiển thị Level (badge style)
    private final Label levelLabel; // Text Level (hiển thị "LEVEL: X")
    private final Rectangle xpBarBg; // Nền XP Bar
    private final Rectangle xpBarFill; // Thanh XP
    private final Rectangle staminaBarBg; // Nền Stamina Bar
    private final Rectangle staminaBarFill; // Thanh Stamina
    private final Label staminaLabel; // Label "Stamina" cho Stamina Bar
    private final Label xpLabel; // Label "Exp" cho XP Bar
    private final Label moneyLabel; // Label hiển thị số tiền
    private final HBox moneyContainer; // Container for money icon and text (with shared background)
    private ImageView moneyIcon; // Icon money (from GUI icons)
    
    // --- Top-Right: Info & Controls ---
    private final Label dayLabel; // Label hiển thị Day (ngày)
    private final Label timerLabel; // Label hiển thị Time (giờ trong ngày)
    private final StackPane weatherIconPane; // Container for weather icon (ImageView)
    private ImageView weatherIcon; // Icon thời tiết (Sunny/Rain from GUI icons)
    private final StackPane shopIconButtonPane; // Container for shop icon button (clickable)
    private ImageView shopIconButton; // Icon Shop (from GUI icons, clickable)
    private final StackPane questIconButtonPane; // Container for quest icon button (clickable)
    private ImageView questIconButton; // Quest Icon (from GUI icons, clickable)
    private final StackPane settingsIconButtonPane; // Container for settings icon button (clickable)
    private ImageView settingsIconButton; // Icon Settings (from GUI icons, clickable)
    private final StackPane trashIconButtonPane; // Container for trash icon (for drag-and-drop deletion)
    private ImageView trashIconButton; // Icon Trash Can (from GUI icons)
    
    // --- Overlays ---
    private final Rectangle darknessOverlay; // Lớp phủ màu đen để tạo hiệu ứng tối
    
    // --- Temporary Text ---
    private final Text temporaryText; // Đối tượng Text để hiển thị thông báo tạm thời
    private SequentialTransition temporaryTextAnimation; // Animation cho text

    public HudView() {
        double currentY = HudConfig.HUD_TOP_LEFT_Y;
        
        // --- Khởi tạo Top-Left Elements ---
        // Level Rounded Rectangle (Badge style)
        levelRectangle = new Rectangle(HudConfig.HUD_TOP_LEFT_X, currentY, 
                                      HudConfig.LEVEL_RECTANGLE_WIDTH, HudConfig.LEVEL_RECTANGLE_HEIGHT);
        levelRectangle.setFill(HudConfig.LEVEL_BG_COLOR);
        levelRectangle.setStroke(Color.WHITE);
        levelRectangle.setStrokeWidth(2);
        levelRectangle.setArcWidth(HudConfig.LEVEL_RECTANGLE_CORNER_RADIUS * 2);
        levelRectangle.setArcHeight(HudConfig.LEVEL_RECTANGLE_CORNER_RADIUS * 2);
        levelRectangle.setMouseTransparent(true);
        
        levelLabel = new Label("LEVEL: 1");
        levelLabel.setLayoutX(HudConfig.HUD_TOP_LEFT_X);
        levelLabel.setLayoutY(currentY);
        levelLabel.setPrefSize(HudConfig.LEVEL_RECTANGLE_WIDTH, HudConfig.LEVEL_RECTANGLE_HEIGHT);
        levelLabel.setStyle("-fx-font-size: " + HudConfig.LEVEL_FONT_SIZE + "px; -fx-text-fill: white; -fx-alignment: center; -fx-font-weight: bold;");
        levelLabel.setTextAlignment(TextAlignment.CENTER);
        levelLabel.setAlignment(Pos.CENTER);
        levelLabel.setMouseTransparent(true);
        
        currentY += HudConfig.LEVEL_RECTANGLE_HEIGHT + HudConfig.HUD_ELEMENT_SPACING;
        
        // XP Label
        xpLabel = new Label("Exp");
        xpLabel.setLayoutX(HudConfig.HUD_TOP_LEFT_X);
        xpLabel.setLayoutY(currentY);
        xpLabel.setStyle("-fx-font-size: " + HudConfig.BAR_LABEL_FONT_SIZE + "px; -fx-text-fill: " + 
                        ((Color)HudConfig.BAR_LABEL_COLOR).toString().replace("0x", "#") + ";");
        xpLabel.setMouseTransparent(true);
        currentY += 15.0; // Spacing cho label
        
        // XP Bar
        xpBarBg = new Rectangle(HudConfig.HUD_TOP_LEFT_X, currentY, HudConfig.XP_BAR_WIDTH, HudConfig.XP_BAR_HEIGHT);
        xpBarBg.setFill(HudConfig.XP_BAR_BG_COLOR);
        xpBarBg.setStroke(Color.BLACK);
        xpBarBg.setStrokeWidth(1);
        xpBarBg.setMouseTransparent(true);
        
        xpBarFill = new Rectangle(HudConfig.HUD_TOP_LEFT_X, currentY, 0, HudConfig.XP_BAR_HEIGHT);
        xpBarFill.setFill(HudConfig.XP_BAR_FILL_COLOR);
        xpBarFill.setMouseTransparent(true);
        
        currentY += HudConfig.XP_BAR_HEIGHT + HudConfig.HUD_ELEMENT_SPACING;
        
        // Stamina Label
        staminaLabel = new Label("Stamina");
        staminaLabel.setLayoutX(HudConfig.HUD_TOP_LEFT_X);
        staminaLabel.setLayoutY(currentY);
        staminaLabel.setStyle("-fx-font-size: " + HudConfig.BAR_LABEL_FONT_SIZE + "px; -fx-text-fill: " + 
                             ((Color)HudConfig.BAR_LABEL_COLOR).toString().replace("0x", "#") + ";");
        staminaLabel.setMouseTransparent(true);
        currentY += 15.0; // Spacing cho label
        
        // Stamina Bar
        staminaBarBg = new Rectangle(HudConfig.HUD_TOP_LEFT_X, currentY, HudConfig.STAMINA_BAR_WIDTH, HudConfig.STAMINA_BAR_HEIGHT);
        staminaBarBg.setFill(HudConfig.STAMINA_BAR_BG_COLOR);
        staminaBarBg.setStroke(Color.BLACK);
        staminaBarBg.setStrokeWidth(1);
        staminaBarBg.setMouseTransparent(true);
        
        staminaBarFill = new Rectangle(HudConfig.HUD_TOP_LEFT_X, currentY, 0, HudConfig.STAMINA_BAR_HEIGHT);
        staminaBarFill.setFill(HudConfig.STAMINA_BAR_FULL_COLOR);
        staminaBarFill.setMouseTransparent(true);
        
        currentY += HudConfig.STAMINA_BAR_HEIGHT + HudConfig.HUD_ELEMENT_SPACING;
        
        // Money Display - Grouped Icon and Text in a single container with shared background
        moneyContainer = new HBox(5); // Spacing 5 between icon and text
        moneyContainer.setAlignment(Pos.CENTER_LEFT);
        moneyContainer.setStyle(HudConfig.MONEY_CONTAINER_STYLE);
        moneyContainer.setLayoutX(HudConfig.HUD_TOP_LEFT_X);
        moneyContainer.setLayoutY(currentY);
        moneyContainer.setMouseTransparent(true);
        
        // Money Icon
        moneyIcon = new ImageView();
        moneyIcon.setFitWidth(HudConfig.MONEY_ICON_SIZE);
        moneyIcon.setFitHeight(HudConfig.MONEY_ICON_SIZE);
        moneyIcon.setPreserveRatio(true);
        moneyContainer.getChildren().add(moneyIcon);
        
        // Money Label (Text only, no background - background is on container)
        moneyLabel = new Label("$0");
        moneyLabel.setStyle(HudConfig.MONEY_TEXT_STYLE);
        moneyLabel.setMouseTransparent(true);
        moneyContainer.getChildren().add(moneyLabel);
        
        // --- Khởi tạo Top-Right Elements (từ trên xuống: Settings, Timer, Weather) ---
        // Tính toán vị trí X để dính sát cạnh phải: Icons centered at SCREEN_WIDTH - MARGIN - RADIUS
        double iconRadius = HudConfig.ICON_BUTTON_SIZE / 2;
        double settingsIconCenterX = WindowConfig.SCREEN_WIDTH - HudConfig.HUD_TOP_RIGHT_MARGIN - iconRadius;
        currentY = HudConfig.HUD_TOP_RIGHT_Y;
        
        // Settings Icon Button (trên cùng ở Top-Right) - Use ImageView from GUI icons
        settingsIconButtonPane = new StackPane();
        settingsIconButtonPane.setLayoutX(settingsIconCenterX - iconRadius);
        settingsIconButtonPane.setLayoutY(currentY);
        settingsIconButtonPane.setPrefSize(HudConfig.ICON_BUTTON_SIZE, HudConfig.ICON_BUTTON_SIZE);
        // No background - transparent
        
        settingsIconButton = new ImageView();
        settingsIconButton.setFitWidth(HudConfig.ICON_BUTTON_SIZE);
        settingsIconButton.setFitHeight(HudConfig.ICON_BUTTON_SIZE);
        settingsIconButton.setPreserveRatio(true);
        settingsIconButtonPane.getChildren().add(settingsIconButton);
        
        settingsIconButtonPane.setOnMouseClicked(this::onSettingsIconClicked);
        // No hover effect background - clean look
        
        currentY += HudConfig.ICON_BUTTON_SIZE + HudConfig.HUD_TOP_RIGHT_ELEMENT_SPACING;
        
        // Day Label (dòng 1) - dưới Settings ở Right side
        dayLabel = new Label(HudConfig.DAY_DEFAULT_TEXT);
        dayLabel.setStyle(HudConfig.DAY_STYLE_CSS);
        dayLabel.setPrefWidth(HudConfig.TIMER_LABEL_WIDTH);
        dayLabel.setAlignment(Pos.CENTER_RIGHT); // Căn text sang phải
        dayLabel.setLayoutX(WindowConfig.SCREEN_WIDTH - HudConfig.HUD_TOP_RIGHT_MARGIN - HudConfig.TIMER_LABEL_WIDTH);
        dayLabel.setLayoutY(currentY);
        dayLabel.setMouseTransparent(true);
        
        currentY += 20.0; // Khoảng cách giữa Day và Time labels
        
        // Time Label (dòng 2) - dưới Day ở Right side
        timerLabel = new Label(HudConfig.TIME_DEFAULT_TEXT);
        timerLabel.setStyle(HudConfig.TIME_STYLE_CSS);
        timerLabel.setPrefWidth(HudConfig.TIMER_LABEL_WIDTH);
        timerLabel.setAlignment(Pos.CENTER_RIGHT); // Căn text sang phải
        timerLabel.setLayoutX(WindowConfig.SCREEN_WIDTH - HudConfig.HUD_TOP_RIGHT_MARGIN - HudConfig.TIMER_LABEL_WIDTH);
        timerLabel.setLayoutY(currentY);
        timerLabel.setMouseTransparent(true);
        
        currentY += 25.0; // Chiều cao của time label + spacing
        
        // Weather Icon (dưới Timer ở Right side) - Use ImageView from GUI icons
        double weatherIconRadius = HudConfig.WEATHER_ICON_SIZE / 2;
        double weatherIconCenterX = WindowConfig.SCREEN_WIDTH - HudConfig.HUD_TOP_RIGHT_MARGIN - weatherIconRadius;
        weatherIconPane = new StackPane();
        weatherIconPane.setLayoutX(weatherIconCenterX - weatherIconRadius);
        weatherIconPane.setLayoutY(currentY);
        weatherIconPane.setPrefSize(HudConfig.WEATHER_ICON_SIZE, HudConfig.WEATHER_ICON_SIZE);
        weatherIconPane.setMouseTransparent(true);
        
        weatherIcon = new ImageView();
        weatherIcon.setFitWidth(HudConfig.WEATHER_ICON_SIZE);
        weatherIcon.setFitHeight(HudConfig.WEATHER_ICON_SIZE);
        weatherIcon.setPreserveRatio(true);
        weatherIconPane.getChildren().add(weatherIcon);
        
        currentY += HudConfig.WEATHER_ICON_SIZE + HudConfig.HUD_TOP_RIGHT_ELEMENT_SPACING;
        
        // Quest Icon Button (dưới Weather Icon ở Top-Right) - Use ImageView from GUI icons
        double questIconRadius = HudConfig.ICON_BUTTON_SIZE / 2;
        double questIconCenterX = WindowConfig.SCREEN_WIDTH - HudConfig.HUD_TOP_RIGHT_MARGIN - questIconRadius;
        
        questIconButtonPane = new StackPane();
        questIconButtonPane.setLayoutX(questIconCenterX - questIconRadius);
        questIconButtonPane.setLayoutY(currentY);
        questIconButtonPane.setPrefSize(HudConfig.ICON_BUTTON_SIZE, HudConfig.ICON_BUTTON_SIZE);
        // No background - transparent
        
        questIconButton = new ImageView();
        questIconButton.setFitWidth(HudConfig.ICON_BUTTON_SIZE);
        questIconButton.setFitHeight(HudConfig.ICON_BUTTON_SIZE);
        questIconButton.setPreserveRatio(true);
        questIconButtonPane.getChildren().add(questIconButton);
        
        questIconButtonPane.setOnMouseClicked(e -> {
            // Block quest interaction when game is paused
            if (gameManager != null && gameManager.isPaused()) return;
            
            if (mainGameView != null) {
                mainGameView.toggleQuestBoard();
            }
        });
        // No hover effect background - clean look
        
        // --- Khởi tạo Bottom-Right Elements (Shop Icon) ---
        // Tính toán vị trí để dính sát cạnh phải và dưới: Icon centered at SCREEN_WIDTH - MARGIN - RADIUS
        double shopIconRadius = HudConfig.ICON_BUTTON_SIZE / 2;
        double shopIconCenterX = WindowConfig.SCREEN_WIDTH - HudConfig.HUD_BOTTOM_RIGHT_MARGIN - shopIconRadius;
        double shopIconCenterY = WindowConfig.SCREEN_HEIGHT - HudConfig.HUD_BOTTOM_RIGHT_MARGIN - shopIconRadius;
        
        // Shop Icon Button (góc dưới-phải) - Use ImageView from GUI icons
        shopIconButtonPane = new StackPane();
        shopIconButtonPane.setLayoutX(shopIconCenterX - shopIconRadius);
        shopIconButtonPane.setLayoutY(shopIconCenterY - shopIconRadius);
        shopIconButtonPane.setPrefSize(HudConfig.ICON_BUTTON_SIZE, HudConfig.ICON_BUTTON_SIZE);
        // No background - transparent
        
        shopIconButton = new ImageView();
        shopIconButton.setFitWidth(HudConfig.ICON_BUTTON_SIZE);
        shopIconButton.setFitHeight(HudConfig.ICON_BUTTON_SIZE);
        shopIconButton.setPreserveRatio(true);
        shopIconButtonPane.getChildren().add(shopIconButton);
        
        shopIconButtonPane.setOnMouseClicked(this::onShopIconClicked);
        // No hover effect background - clean look
        
        // Trash Can Icon (góc dưới-trái) - Use ImageView from GUI icons
        // Position at bottom-left: strictly calculated
        double trashIconX = HudConfig.HUD_TOP_LEFT_X;
        double trashIconY = WindowConfig.SCREEN_HEIGHT - HudConfig.HUD_BOTTOM_RIGHT_MARGIN - HudConfig.ICON_BUTTON_SIZE;
        
        trashIconButtonPane = new StackPane();
        trashIconButtonPane.setLayoutX(trashIconX);
        trashIconButtonPane.setLayoutY(trashIconY);
        trashIconButtonPane.setPrefSize(HudConfig.ICON_BUTTON_SIZE, HudConfig.ICON_BUTTON_SIZE);
        // No background - transparent
        
        trashIconButton = new ImageView();
        trashIconButton.setFitWidth(HudConfig.ICON_BUTTON_SIZE);
        trashIconButton.setFitHeight(HudConfig.ICON_BUTTON_SIZE);
        trashIconButton.setPreserveRatio(true);
        trashIconButtonPane.getChildren().add(trashIconButton);
        // Trash icon is not clickable, only for drag-and-drop detection
        
        // --- Khởi tạo Temporary Text ---
        temporaryText = new Text();
        temporaryText.setFont(HudConfig.TEMP_TEXT_FONT);
        temporaryText.setFill(HudConfig.TEMP_TEXT_COLOR);
        temporaryText.setStroke(HudConfig.TEMP_TEXT_STROKE_COLOR);
        temporaryText.setStrokeWidth(HudConfig.TEMP_TEXT_STROKE_WIDTH);
        temporaryText.setOpacity(0);
        temporaryText.setManaged(false);

        // Khởi tạo Lớp phủ Tối
        this.darknessOverlay = new Rectangle(WindowConfig.SCREEN_WIDTH, WindowConfig.SCREEN_HEIGHT);
        this.darknessOverlay.setFill(Color.BLACK);
        this.darknessOverlay.setOpacity(0.0);
        this.darknessOverlay.setMouseTransparent(true);

        // Thêm tất cả vào pane theo đúng thứ tự z-index:
        // 1. Darkness Overlay (phủ lên world, nhưng dưới HUD icons)
        this.getChildren().add(darknessOverlay);
        
        // 2. HUD Icons/Text (ở trên darkness overlay để luôn visible và clickable)
        this.getChildren().addAll(
            levelRectangle, levelLabel,
            xpLabel, xpBarBg, xpBarFill,
            staminaLabel, staminaBarBg, staminaBarFill,
            moneyContainer, // Money Container (Icon + Text with shared background)
            dayLabel, timerLabel, weatherIconPane, questIconButtonPane, // Day, Time, Weather Icon, Quest Icon (Top-Right)
            shopIconButtonPane, // Shop Icon (ImageView) ở Bottom-Right
            trashIconButtonPane, // Trash Can Icon (ImageView) ở Bottom-Left
            settingsIconButtonPane, // Settings Icon (ImageView) ở Top-Right
            temporaryText
        );
        
        this.setMouseTransparent(false); // Cần nhận click cho icon buttons
    }
    /**
     * Set GameManager và MainGameView references
     */
    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    public void setMainGameView(MainGameView mainGameView) {
        this.mainGameView = mainGameView;
    }
    
    /**
     * Set AssetManager reference and load GUI icons
     */
    public void setAssetManager(AssetManager assetManager) {
        this.assetManager = assetManager;
        updateGuiIcons(); // Load icons when AssetManager is available
    }
    
    /**
     * Load and update GUI icons from AssetManager
     */
    private void updateGuiIcons() {
        if (assetManager == null) return;
        
        // Load Money icon
        Image moneyIconImage = assetManager.getGuiIcon("MONEY");
        if (moneyIconImage != null && moneyIcon != null) {
            moneyIcon.setImage(moneyIconImage);
        }
        
        // Load Settings icon
        Image settingsIconImage = assetManager.getGuiIcon("SETTINGS");
        if (settingsIconImage != null && settingsIconButton != null) {
            settingsIconButton.setImage(settingsIconImage);
        }
        
        // Load Shop icon
        Image shopIconImage = assetManager.getGuiIcon("SHOP");
        if (shopIconImage != null && shopIconButton != null) {
            shopIconButton.setImage(shopIconImage);
        }
        
        // Load Trash Can icon
        Image trashIconImage = assetManager.getGuiIcon("TRASH");
        if (trashIconImage != null && trashIconButton != null) {
            trashIconButton.setImage(trashIconImage);
        }
        
        // Load Quest icon
        Image questIconImage = assetManager.getGuiIcon("QUEST");
        if (questIconImage != null && questIconButton != null) {
            questIconButton.setImage(questIconImage);
        }
        
        // Weather icon will be updated in updateWeather() method
    }
    
    /**
     * Event handler cho Shop Icon click
     */
    private void onShopIconClicked(MouseEvent e) {
        // Block shop interaction when game is paused
        if (gameManager != null && gameManager.isPaused()) return;
        
        if (mainGameView != null) {
            mainGameView.toggleShop();
        }
    }
    
    /**
     * Event handler cho Settings Icon click
     */
    private void onSettingsIconClicked(MouseEvent e) {
        if (gameManager != null) {
            gameManager.toggleSettingsMenu();
        }
    }
    
    /**
     * Cập nhật hiển thị player stats (Level, XP, Stamina)
     */
    public void updatePlayerStats() {
        if (gameManager == null || gameManager.getMainPlayer() == null) return;
        
        var player = gameManager.getMainPlayer();
        
        // Update Level
        levelLabel.setText("LEVEL: " + player.getLevel());
        
        // Update XP Bar - Sử dụng getter từ Lombok (@Getter annotation)
        double xpProgress = 0.0;
        if (player.getXpToNextLevel() > 0) {
            xpProgress = Math.min(1.0, player.getCurrentXP() / player.getXpToNextLevel());
        }
        xpBarFill.setWidth(HudConfig.XP_BAR_WIDTH * xpProgress);
        
        // Update Stamina Bar - Sử dụng getter từ Lombok
        double staminaProgress = 0.0;
        if (player.getMaxStamina() > 0) {
            staminaProgress = Math.min(1.0, player.getCurrentStamina() / player.getMaxStamina());
        }
        staminaBarFill.setWidth(HudConfig.STAMINA_BAR_WIDTH * staminaProgress);
        
        // Dynamic Stamina Bar Color based on remaining percentage
        double percentage = staminaProgress;
        Color staminaColor;
        if (percentage > 0.6) {
            // Green when above 60%
            staminaColor = Color.web("#2ecc71");
        } else if (percentage > 0.15) {
            // Yellow when between 15% and 60%
            staminaColor = Color.web("#f1c40f");
        } else {
            // Red when 15% or below
            staminaColor = Color.web("#e74c3c");
        }
        staminaBarFill.setFill(staminaColor);
    }
    
    /**
     * Cập nhật hiển thị thời tiết
     */
    public void updateWeather(boolean isRaining) {
        if (assetManager == null || weatherIcon == null) return;
        
        // Load weather icon from GUI icons
        Image weatherIconImage;
        if (isRaining) {
            weatherIconImage = assetManager.getGuiIcon("RAIN");
        } else {
            weatherIconImage = assetManager.getGuiIcon("SUNNY");
        }
        
        if (weatherIconImage != null) {
            weatherIcon.setImage(weatherIconImage);
        }
    }

    /**
     * Hiển thị một đoạn text tạm thời trên đầu người chơi
     */
    public void showTemporaryText(String message, double playerScreenX, double playerScreenY) {
        if (temporaryTextAnimation != null && temporaryTextAnimation.getStatus().equals(javafx.animation.Animation.Status.RUNNING)) {
            temporaryTextAnimation.stop();
        }

        temporaryText.setText(message);
        temporaryText.setLayoutX(playerScreenX - temporaryText.getLayoutBounds().getWidth() / 2);
        temporaryText.setLayoutY(playerScreenY + HudConfig.TEMP_TEXT_OFFSET_Y);
        temporaryText.setOpacity(1);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(HudConfig.TEMP_TEXT_FADE_DURATION), temporaryText);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        PauseTransition pause = new PauseTransition(Duration.millis(HudConfig.TEMP_TEXT_DISPLAY_DURATION - HudConfig.TEMP_TEXT_FADE_DURATION));

        temporaryTextAnimation = new SequentialTransition(temporaryText, pause, fadeOut);
        temporaryTextAnimation.play();
    }

    public void updateTimer(int day, String timeString) {
        this.dayLabel.setText("Day " + day);
        this.timerLabel.setText(timeString);
        // Vị trí X đã được set cố định trong constructor, không cần tính lại
        // Day và Time labels đã có fixed width và alignment CENTER_RIGHT
    }

    /**
     * Cập nhật độ tối của màn hình dựa trên cường độ ánh sáng từ Model
     * Áp dụng brightness setting sử dụng overlay method
     */
    public void updateLighting(double intensity) {
        final double MAX_DARKNESS = 0.95; // Maximum opacity clamp
        // Tính opacity tự nhiên dựa trên intensity (0.0 = sáng, 1.0 = tối)
        double naturalDarkness = 1.0 - intensity;
        
        // Áp dụng brightness modifier sử dụng overlay method
        // Formula: finalOpacity = naturalDarkness + (1.0 - brightness) * 0.5
        // Nếu brightness = 1.0 (Max): finalOpacity = naturalDarkness + 0 = naturalDarkness (không thêm tối)
        // Nếu brightness = 0.0 (Min): finalOpacity = naturalDarkness + 0.5 (thêm tối đáng kể)
        double finalOpacity = naturalDarkness + (1.0 - brightness) * 0.5;
        
        // Clamp opacity giữa 0.0 và MAX_DARKNESS (0.95)
        finalOpacity = Math.max(0.0, Math.min(finalOpacity, MAX_DARKNESS));
        
        this.darknessOverlay.setOpacity(finalOpacity);
    }
    
    /**
     * Set brightness (0.0 - 1.0)
     */
    public void setBrightness(double brightness) {
        this.brightness = Math.max(GameLogicConfig.MIN_BRIGHTNESS, Math.min(GameLogicConfig.MAX_BRIGHTNESS, brightness));
    }
    
    /**
     * Get brightness
     */
    public double getBrightness() {
        return brightness;
    }
    
    /**
     * Check if mouse is over Trash Can icon (for drag-and-drop deletion)
     * @param screenX Screen X coordinate
     * @param screenY Screen Y coordinate
     * @return true if mouse is over Trash Can bounds
     */
    public boolean isMouseOverTrash(double screenX, double screenY) {
        if (trashIconButtonPane == null) return false;
        
        // Convert screen coordinates to local coordinates
        javafx.geometry.Point2D localPoint = this.sceneToLocal(screenX, screenY);
        
        double x = trashIconButtonPane.getLayoutX();
        double y = trashIconButtonPane.getLayoutY();
        double width = trashIconButtonPane.getPrefWidth();
        double height = trashIconButtonPane.getPrefHeight();
        
        return localPoint.getX() >= x && localPoint.getX() <= x + width &&
               localPoint.getY() >= y && localPoint.getY() <= y + height;
    }
    
    /**
     * Cập nhật hiển thị số tiền
     */
    public void updateMoney(double amount) {
        this.moneyLabel.setText("$" + (int)amount);
    }
    
    /**
     * Lấy darknessOverlay để điều chỉnh độ tối khi mưa
     */
    public Rectangle getDarknessOverlay() {
        return darknessOverlay;
    }
}
