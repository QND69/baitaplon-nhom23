package com.example.farmSimulation.view;

import com.example.farmSimulation.config.*;
import com.example.farmSimulation.config.SettingsMenuConfig;
import com.example.farmSimulation.model.GameManager;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class SettingsMenuView extends GridPane {
    private GameManager gameManager; // Không phải final để có thể set sau khi khởi tạo
    private final Label nameLabel;
    private final Label levelLabel;
    private double brightness = SettingsMenuConfig.DEFAULT_BRIGHTNESS;
    private Slider brightnessSlider;
    
    // Audio Settings (Đơn giản hóa: chỉ Master Volume)
    private double masterVolume = SettingsMenuConfig.DEFAULT_MASTER_VOLUME;
    private Slider masterVolumeSlider;
    
    private final TabPane tabPane;

    public SettingsMenuView(GameManager gameManager) {
        this.gameManager = gameManager; // Có thể null khi khởi tạo

        this.setStyle(SettingsMenuConfig.SETTINGS_MENU_STYLE_CSS);
        this.setPrefSize(SettingsMenuConfig.SETTINGS_MENU_WIDTH_NEW, SettingsMenuConfig.SETTINGS_MENU_HEIGHT_NEW);
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(30));
        this.setHgap(20);
        this.setVgap(15);

        // Cấu hình GridPane columns
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(40); // Cột 1: Labels
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(60); // Cột 2: Controls
        this.getColumnConstraints().addAll(col1, col2);

        int currentRow = 0;

        // Tiêu đề (span 2 cột)
        Label title = new Label(SettingsMenuConfig.SETTINGS_MENU_TITLE);
        title.setTextFill(SettingsMenuConfig.SETTINGS_MENU_FONT_COLOR);
        title.setFont(Font.font(SettingsMenuConfig.SETTINGS_MENU_FONT_FAMILY, SettingsMenuConfig.SETTINGS_MENU_TITLE_FONT_SIZE));
        title.setTextAlignment(TextAlignment.CENTER);
        this.add(title, 0, currentRow++, 2, 1);
        GridPane.setHalignment(title, HPos.CENTER);

        // Thông tin người chơi (span 2 cột)
        VBox playerInfo = new VBox(SettingsMenuConfig.SETTINGS_PLAYER_INFO_SPACING);
        playerInfo.setAlignment(Pos.CENTER);
        nameLabel = new Label("Player: ");
        levelLabel = new Label("Level: ");
        nameLabel.setTextFill(SettingsMenuConfig.SETTINGS_MENU_FONT_COLOR);
        levelLabel.setTextFill(SettingsMenuConfig.SETTINGS_MENU_FONT_COLOR);
        nameLabel.setFont(Font.font(SettingsMenuConfig.SETTINGS_MENU_FONT_FAMILY, SettingsMenuConfig.SETTINGS_MENU_BODY_FONT_SIZE));
        levelLabel.setFont(Font.font(SettingsMenuConfig.SETTINGS_MENU_FONT_FAMILY, SettingsMenuConfig.SETTINGS_MENU_BODY_FONT_SIZE));
        playerInfo.getChildren().addAll(nameLabel, levelLabel);
        this.add(playerInfo, 0, currentRow++, 2, 1);
        GridPane.setHalignment(playerInfo, HPos.CENTER);

        // TabPane chứa các tabs
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE); // Không cho đóng tabs
        
        // Style TabPane với minimal look - Text + Underline style
        // Áp dụng aggressive transparency để loại bỏ tất cả default backgrounds
        String tabPaneStyle = 
            "-fx-background-color: transparent; " +
            "-fx-tab-min-width: 80px; " +
            "-fx-tab-max-width: 80px; " +
            "-fx-tab-min-height: 30px; " +
            "-fx-tab-header-background: transparent; " +
            "-fx-tab-header-area-background: transparent; " +
            "-fx-tab-header-area-background-color: transparent; " +
            "-fx-tab-header-background-color: transparent; " +
            "-fx-control-inner-background: transparent; " +
            "-fx-focus-color: transparent; " +
            "-fx-faint-focus-color: transparent; " +
            "-fx-padding: 0 50 0 50;"; // Push tabs to center
        tabPane.setStyle(tabPaneStyle);
        
        // Tab 1: General
        Tab generalTab = createGeneralTab();
        tabPane.getTabs().add(generalTab);
        
        // Tab 2: Controls
        Tab controlsTab = createControlsTab();
        tabPane.getTabs().add(controlsTab);
        
        // Tab 3: Tutorial
        Tab tutorialTab = createTutorialTab();
        tabPane.getTabs().add(tutorialTab);
        
        // Apply initial tab styles
        updateTabStyles();
        
        // Update tab styles when selection changes
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            updateTabStyles();
        });
        
        this.add(tabPane, 0, currentRow++, 2, 1);
        GridPane.setHgrow(tabPane, javafx.scene.layout.Priority.ALWAYS);
        GridPane.setVgrow(tabPane, javafx.scene.layout.Priority.ALWAYS);

        // Đặt menu giữa màn hình
        this.setLayoutX(WindowConfig.SCREEN_WIDTH / 2 - SettingsMenuConfig.SETTINGS_MENU_WIDTH_NEW / 2);
        this.setLayoutY(WindowConfig.SCREEN_HEIGHT / 2 - SettingsMenuConfig.SETTINGS_MENU_HEIGHT_NEW / 2);

        this.setVisible(false); // Ẩn ban đầu
    }
    
    /**
     * Tạo Tab "General" chứa Volume, Brightness, và các nút Resume/Save/Exit
     */
    private Tab createGeneralTab() {
        Tab tab = new Tab("General");
        tab.setClosable(false);
        
        GridPane contentGrid = new GridPane();
        contentGrid.setHgap(20);
        contentGrid.setVgap(15);
        contentGrid.setPadding(new Insets(20));
        
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(40);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(60);
        contentGrid.getColumnConstraints().addAll(col1, col2);
        
        int row = 0;
        
        // Master Volume Slider
        Label masterVolLabel = new Label(SettingsMenuConfig.SETTINGS_MASTER_VOLUME_LABEL);
        masterVolLabel.setTextFill(SettingsMenuConfig.SETTINGS_MENU_FONT_COLOR);
        masterVolLabel.setFont(Font.font(SettingsMenuConfig.SETTINGS_MENU_FONT_FAMILY, SettingsMenuConfig.SETTINGS_MENU_BODY_FONT_SIZE));
        contentGrid.add(masterVolLabel, 0, row);

        masterVolumeSlider = new Slider(SettingsMenuConfig.SLIDER_MIN_VALUE, SettingsMenuConfig.SLIDER_MAX_VALUE, SettingsMenuConfig.DEFAULT_MASTER_VOLUME);
        masterVolumeSlider.setPrefWidth(200);
        masterVolumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            masterVolume = newVal.doubleValue();
            // Áp dụng master volume vào audio system ngay lập tức (real-time update)
            if (gameManager != null && gameManager.getAudioManager() != null) {
                gameManager.getAudioManager().setGlobalVolume(masterVolume);
            }
        });
        contentGrid.add(masterVolumeSlider, 1, row++);

        // Brightness Slider
        Label brightnessLabel = new Label("Brightness:");
        brightnessLabel.setTextFill(SettingsMenuConfig.SETTINGS_MENU_FONT_COLOR);
        brightnessLabel.setFont(Font.font(SettingsMenuConfig.SETTINGS_MENU_FONT_FAMILY, SettingsMenuConfig.SETTINGS_MENU_BODY_FONT_SIZE));
        contentGrid.add(brightnessLabel, 0, row);

        brightnessSlider = new Slider(SettingsMenuConfig.BRIGHTNESS_MIN, SettingsMenuConfig.BRIGHTNESS_MAX, brightness);
        brightnessSlider.setPrefWidth(200);
        brightnessSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            brightness = newVal.doubleValue();
            // Áp dụng brightness vào HudView ngay lập tức (real-time update)
            if (gameManager != null && gameManager.getMainGameView() != null && gameManager.getMainGameView().getHudView() != null) {
                HudView hudView = gameManager.getMainGameView().getHudView();
                hudView.setBrightness(brightness);
                // Cập nhật lại lighting để áp dụng brightness mới ngay lập tức
                if (gameManager.getTimeManager() != null) {
                    double currentIntensity = gameManager.getTimeManager().getCurrentLightIntensity();
                    hudView.updateLighting(currentIntensity);
                }
            }
        });
        contentGrid.add(brightnessSlider, 1, row++);
        
        // Buttons section
        VBox buttonsBox = new VBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setPadding(new Insets(20, 0, 0, 0));
        
        // Resume button
        Button resume = new Button(SettingsMenuConfig.SETTINGS_RESUME_BUTTON_TEXT);
        resume.setPrefWidth(SettingsMenuConfig.SETTINGS_MENU_BUTTON_WIDTH);
        resume.setOnAction(e -> {
            if (this.gameManager != null) {
                // Đóng menu
                this.gameManager.toggleSettingsMenu();
                
                // Request focus để key bindings hoạt động ngay lập tức
                if (this.gameManager.getMainGameView() != null && 
                    this.gameManager.getMainGameView().getRootPane() != null &&
                    this.gameManager.getMainGameView().getRootPane().getScene() != null) {
                    this.gameManager.getMainGameView().getRootPane().requestFocus();
                }
            }
        });
        
        // Save button
        Button save = new Button(SettingsMenuConfig.SETTINGS_SAVE_BUTTON_TEXT);
        save.setPrefWidth(SettingsMenuConfig.SETTINGS_MENU_BUTTON_WIDTH);
        save.setOnAction(e -> System.out.println("Save game logic here"));
        
        // Exit button
        Button exit = new Button(SettingsMenuConfig.SETTINGS_EXIT_BUTTON_TEXT);
        exit.setPrefWidth(SettingsMenuConfig.SETTINGS_MENU_BUTTON_WIDTH);
        exit.setOnAction(e -> System.exit(0));
        
        buttonsBox.getChildren().addAll(resume, save, exit);
        contentGrid.add(buttonsBox, 0, row++, 2, 1);
        GridPane.setHalignment(buttonsBox, HPos.CENTER);
        
        ScrollPane scrollPane = new ScrollPane(contentGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        tab.setContent(scrollPane);
        return tab;
    }
    
    /**
     * Tạo Tab "Controls" chứa Key Bindings list
     */
    private Tab createControlsTab() {
        Tab tab = new Tab("Controls");
        tab.setClosable(false);
        
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(20));
        contentBox.setAlignment(Pos.TOP_LEFT);
        
        Label keyBindingsLabel = new Label("Key Bindings:");
        keyBindingsLabel.setTextFill(SettingsMenuConfig.SETTINGS_MENU_FONT_COLOR);
        keyBindingsLabel.setFont(Font.font(SettingsMenuConfig.SETTINGS_MENU_FONT_FAMILY, 16));
        keyBindingsLabel.setStyle("-fx-font-weight: bold;");
        
        // Tạo GridPane để căn chỉnh Key và Action (Key bên trái, Action bên phải)
        GridPane keyBindingsGrid = new GridPane();
        keyBindingsGrid.setHgap(30);
        keyBindingsGrid.setVgap(8);
        keyBindingsGrid.setAlignment(Pos.CENTER_LEFT);
        
        // Tạo các labels với key bindings chi tiết hơn
        int row = 0;
        
        // Movement keys (broken down)
        Label keyW = new Label("W");
        Label actionW = new Label("Move Up");
        Label keyA = new Label("A");
        Label actionA = new Label("Move Left");
        Label keyS = new Label("S");
        Label actionS = new Label("Move Down");
        Label keyD = new Label("D");
        Label actionD = new Label("Move Right");
        
        // Other keys
        Label keyB = new Label("B");
        Label actionB = new Label("Open / Close Shop");
        Label keyQ = new Label("Q");
        Label actionQ = new Label("Drop Item (at mouse cursor)");
        Label keyJ = new Label("J");
        Label actionJ = new Label("Open / Close Quest Board");
        Label keyESC = new Label("ESC");
        Label actionESC = new Label("Pause / Resume Game");
        Label keyNum = new Label("1 - 9");
        Label actionNum = new Label("Select Hotbar Slot");
        Label keyMouseLeft = new Label("Mouse Left");
        Label actionMouseLeft = new Label("Use Tool / Interact");
        Label keyMouseRight = new Label("Mouse Right");
        Label actionMouseRight = new Label("Toggle Fence Gate");
        
        // Style all key labels (left column)
        Label[] keyLabels = {keyW, keyA, keyS, keyD, keyB, keyQ, keyJ, keyESC, keyNum, keyMouseLeft, keyMouseRight};
        for (Label lbl : keyLabels) {
            lbl.setTextFill(SettingsMenuConfig.SETTINGS_MENU_FONT_COLOR);
            lbl.setFont(Font.font(SettingsMenuConfig.SETTINGS_MENU_FONT_FAMILY, FontWeight.BOLD, 14));
        }
        
        // Style all action labels (right column)
        Label[] actionLabels = {actionW, actionA, actionS, actionD, actionB, actionQ, actionJ, actionESC, actionNum, actionMouseLeft, actionMouseRight};
        for (Label lbl : actionLabels) {
            lbl.setTextFill(SettingsMenuConfig.SETTINGS_MENU_FONT_COLOR);
            lbl.setFont(Font.font(SettingsMenuConfig.SETTINGS_MENU_FONT_FAMILY, 14));
        }
        
        // Add to grid
        keyBindingsGrid.add(keyW, 0, row++);
        keyBindingsGrid.add(actionW, 1, row - 1);
        keyBindingsGrid.add(keyA, 0, row++);
        keyBindingsGrid.add(actionA, 1, row - 1);
        keyBindingsGrid.add(keyS, 0, row++);
        keyBindingsGrid.add(actionS, 1, row - 1);
        keyBindingsGrid.add(keyD, 0, row++);
        keyBindingsGrid.add(actionD, 1, row - 1);
        keyBindingsGrid.add(keyB, 0, row++);
        keyBindingsGrid.add(actionB, 1, row - 1);
        keyBindingsGrid.add(keyQ, 0, row++);
        keyBindingsGrid.add(actionQ, 1, row - 1);
        keyBindingsGrid.add(keyJ, 0, row++);
        keyBindingsGrid.add(actionJ, 1, row - 1);
        keyBindingsGrid.add(keyESC, 0, row++);
        keyBindingsGrid.add(actionESC, 1, row - 1);
        keyBindingsGrid.add(keyNum, 0, row++);
        keyBindingsGrid.add(actionNum, 1, row - 1);
        keyBindingsGrid.add(keyMouseLeft, 0, row++);
        keyBindingsGrid.add(actionMouseLeft, 1, row - 1);
        keyBindingsGrid.add(keyMouseRight, 0, row++);
        keyBindingsGrid.add(actionMouseRight, 1, row - 1);
        
        // Add key bindings grid to content box
        contentBox.getChildren().addAll(keyBindingsLabel, keyBindingsGrid);
        
        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        tab.setContent(scrollPane);
        return tab;
    }
    
    /**
     * Tạo Tab "Tutorial" chứa hướng dẫn chơi game
     */
    private Tab createTutorialTab() {
        Tab tab = new Tab("Tutorial");
        tab.setClosable(false);
        
        // Use Label instead of TextArea to fix white text on white background issue
        Label tutorialLabel = new Label();
        tutorialLabel.setWrapText(true);
        tutorialLabel.setFont(Font.font(SettingsMenuConfig.SETTINGS_MENU_FONT_FAMILY, 14));
        tutorialLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        tutorialLabel.setText(
            "WELCOME TO FARM SIMULATION!\n\n" +
            "Goal: Manage your farm, grow crops, raise animals, and complete daily quests to become the richest farmer!\n\n" +
            "Farming:\n" +
            "1. Use the Hoe to till soil.\n" +
            "2. Plant Seeds on tilled soil.\n" +
            "3. Water crops daily with the Watering Can.\n" +
            "4. Harvest crops when fully grown.\n\n" +
            "Animals:\n" +
            "- Buy animals from the Shop (B).\n" +
            "- Feed them Super Feed to keep them happy.\n" +
            "- Harvest products (Milk, Wool, Eggs) when ready.\n\n" +
            "Tips:\n" +
            "- Watch your Stamina! Eat food or drink Energy Drinks to recover.\n" +
            "- Check the Quest Board (J) daily for extra rewards.\n" +
            "- Use Reroll in the shop to find rare items."
        );
        
        // VBox container for proper padding
        VBox tutorialContainer = new VBox(10);
        tutorialContainer.setPadding(new Insets(20));
        tutorialContainer.setAlignment(Pos.TOP_LEFT);
        tutorialContainer.getChildren().add(tutorialLabel);
        
        ScrollPane scrollPane = new ScrollPane(tutorialContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        tab.setContent(scrollPane);
        return tab;
    }

    /**
     * Cập nhật styles cho tất cả tabs theo minimal "Text + Underline" look
     * Active tab: White text với underline (border-bottom), transparent background
     * Inactive tabs: Light gray text (#aaaaaa), transparent background, no border
     * Sử dụng aggressive CSS để loại bỏ tất cả default backgrounds
     */
    private void updateTabStyles() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        
        // Active Tab Style: White text với underline (border-bottom), transparent background
        // Aggressive CSS để loại bỏ default backgrounds và borders
        String activeTabStyle = 
            "-fx-background-color: transparent; " +
            "-fx-background-insets: 0; " +
            "-fx-background-radius: 0; " +
            "-fx-text-base-color: white; " +
            "-fx-font-weight: bold; " +
            "-fx-border-color: transparent transparent white transparent; " +
            "-fx-border-width: 2; " +
            "-fx-focus-color: transparent; " +
            "-fx-faint-focus-color: transparent;";
        
        // Inactive Tab Style: Light gray text (#aaaaaa), transparent background, no border
        // Aggressive CSS để loại bỏ default backgrounds
        String inactiveTabStyle = 
            "-fx-background-color: transparent; " +
            "-fx-background-insets: 0; " +
            "-fx-background-radius: 0; " +
            "-fx-text-base-color: #aaaaaa; " +
            "-fx-font-weight: normal; " +
            "-fx-border-width: 0; " +
            "-fx-focus-color: transparent; " +
            "-fx-faint-focus-color: transparent;";
        
        // Áp dụng style cho từng tab
        for (Tab tab : tabPane.getTabs()) {
            if (tab == selectedTab) {
                tab.setStyle(activeTabStyle);
            } else {
                tab.setStyle(inactiveTabStyle);
            }
        }
    }

    public void updatePlayerInfo(String playerName, int playerLevel) {
        nameLabel.setText("Player: " + playerName);
        levelLabel.setText("Level: " + playerLevel);
    }

    public void show() {
        // Load brightness hiện tại từ HudView khi mở menu
        if (gameManager != null && gameManager.getMainGameView() != null && gameManager.getMainGameView().getHudView() != null) {
            HudView hudView = gameManager.getMainGameView().getHudView();
            brightness = hudView.getBrightness();
            if (brightnessSlider != null) {
                brightnessSlider.setValue(brightness);
            }
        }
        
        // Load volume hiện tại từ AudioManager khi mở menu
        if (gameManager != null && gameManager.getAudioManager() != null) {
            masterVolume = gameManager.getAudioManager().getCurrentVolume();
            if (masterVolumeSlider != null) {
                masterVolumeSlider.setValue(masterVolume);
            }
        }
        
        setVisible(true);
        // Đảm bảo Settings Menu luôn ở trên cùng khi mở
        this.toFront();
    }

    public void hide() {
        setVisible(false);
    }
    
    /**
     * Set GameManager reference (được gọi sau khi MainGameView được set gameManager)
     * @param gameManager GameManager instance
     */
    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }
}
