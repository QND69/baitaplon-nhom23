package com.example.farmSimulation.view;

import com.example.farmSimulation.config.*;
import com.example.farmSimulation.config.SettingsMenuConfig;
import com.example.farmSimulation.model.GameManager;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
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

        // Nút Resume (span 2 cột) - Sửa để request focus
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
        this.add(resume, 0, currentRow++, 2, 1);
        GridPane.setHalignment(resume, HPos.CENTER);

        // Nút Save Game (span 2 cột)
        Button save = new Button(SettingsMenuConfig.SETTINGS_SAVE_BUTTON_TEXT);
        save.setPrefWidth(SettingsMenuConfig.SETTINGS_MENU_BUTTON_WIDTH);
        save.setOnAction(e -> System.out.println("Save game logic here"));
        this.add(save, 0, currentRow++, 2, 1);
        GridPane.setHalignment(save, HPos.CENTER);

        // Nút Exit (span 2 cột)
        Button exit = new Button(SettingsMenuConfig.SETTINGS_EXIT_BUTTON_TEXT);
        exit.setPrefWidth(SettingsMenuConfig.SETTINGS_MENU_BUTTON_WIDTH);
        exit.setOnAction(e -> System.exit(0));
        this.add(exit, 0, currentRow++, 2, 1);
        GridPane.setHalignment(exit, HPos.CENTER);

        // Master Volume Slider (đơn giản hóa audio settings)
        Label masterVolLabel = new Label(SettingsMenuConfig.SETTINGS_MASTER_VOLUME_LABEL);
        masterVolLabel.setTextFill(SettingsMenuConfig.SETTINGS_MENU_FONT_COLOR);
        masterVolLabel.setFont(Font.font(SettingsMenuConfig.SETTINGS_MENU_FONT_FAMILY, SettingsMenuConfig.SETTINGS_MENU_BODY_FONT_SIZE));
        this.add(masterVolLabel, 0, currentRow);

        masterVolumeSlider = new Slider(SettingsMenuConfig.SLIDER_MIN_VALUE, SettingsMenuConfig.SLIDER_MAX_VALUE, masterVolume);
        masterVolumeSlider.setPrefWidth(200);
        masterVolumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            masterVolume = newVal.doubleValue();
            // TODO: Áp dụng master volume vào audio system
        });
        this.add(masterVolumeSlider, 1, currentRow++);

        // Brightness Slider
        Label brightnessLabel = new Label("Brightness:");
        brightnessLabel.setTextFill(SettingsMenuConfig.SETTINGS_MENU_FONT_COLOR);
        brightnessLabel.setFont(Font.font(SettingsMenuConfig.SETTINGS_MENU_FONT_FAMILY, SettingsMenuConfig.SETTINGS_MENU_BODY_FONT_SIZE));
        this.add(brightnessLabel, 0, currentRow);

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
        this.add(brightnessSlider, 1, currentRow++);

        // Key Bindings (read-only, span 2 cột) - Thêm ScrollPane nếu cần
        Label keyBindingsLabel = new Label("Key Bindings:");
        keyBindingsLabel.setTextFill(SettingsMenuConfig.SETTINGS_MENU_FONT_COLOR);
        keyBindingsLabel.setFont(Font.font(SettingsMenuConfig.SETTINGS_MENU_FONT_FAMILY, 14));
        this.add(keyBindingsLabel, 0, currentRow++, 2, 1);
        GridPane.setHalignment(keyBindingsLabel, HPos.LEFT);

        // Tạo VBox chứa key bindings
        VBox keyBindingsBox = new VBox(5);
        keyBindingsBox.setAlignment(Pos.CENTER_LEFT);
        Label key1 = new Label("WASD: Move");
        Label key2 = new Label("B: Shop");
        Label key3 = new Label("Q: Drop Item");
        Label key4 = new Label("ESC: Pause/Settings");
        key1.setTextFill(SettingsMenuConfig.SETTINGS_MENU_FONT_COLOR);
        key2.setTextFill(SettingsMenuConfig.SETTINGS_MENU_FONT_COLOR);
        key3.setTextFill(SettingsMenuConfig.SETTINGS_MENU_FONT_COLOR);
        key4.setTextFill(SettingsMenuConfig.SETTINGS_MENU_FONT_COLOR);
        key1.setFont(Font.font(SettingsMenuConfig.SETTINGS_MENU_FONT_FAMILY, 12));
        key2.setFont(Font.font(SettingsMenuConfig.SETTINGS_MENU_FONT_FAMILY, 12));
        key3.setFont(Font.font(SettingsMenuConfig.SETTINGS_MENU_FONT_FAMILY, 12));
        key4.setFont(Font.font(SettingsMenuConfig.SETTINGS_MENU_FONT_FAMILY, 12));
        keyBindingsBox.getChildren().addAll(key1, key2, key3, key4);
        
        // Wrap trong ScrollPane để có thể scroll nếu danh sách dài
        ScrollPane keyBindingsScrollPane = new ScrollPane(keyBindingsBox);
        keyBindingsScrollPane.setFitToWidth(true);
        keyBindingsScrollPane.setPrefHeight(80); // Chiều cao cố định
        keyBindingsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        keyBindingsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        keyBindingsScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        this.add(keyBindingsScrollPane, 0, currentRow++, 2, 1);
        GridPane.setHalignment(keyBindingsScrollPane, HPos.LEFT);

        // Đặt menu giữa màn hình
        this.setLayoutX(WindowConfig.SCREEN_WIDTH / 2 - SettingsMenuConfig.SETTINGS_MENU_WIDTH_NEW / 2);
        this.setLayoutY(WindowConfig.SCREEN_HEIGHT / 2 - SettingsMenuConfig.SETTINGS_MENU_HEIGHT_NEW / 2);

        this.setVisible(false); // Ẩn ban đầu
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
