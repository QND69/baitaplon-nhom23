package com.example.farmSimulation.view;

import com.example.farmSimulation.config.*;
import com.example.farmSimulation.config.SettingsMenuConfig;
import com.example.farmSimulation.model.GameManager;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

// TODO: Sửa lại toàn bộ hàm này
public class SettingsMenuView extends VBox {
    private final GameManager gameManager;
    private final Label nameLabel;
    private final Label levelLabel;
    private double hotbarScale = HotbarConfig.DEFAULT_HOTBAR_SCALE;

    private boolean musicOn = SettingsMenuConfig.DEFAULT_MUSIC_ON;
    private boolean soundOn = SettingsMenuConfig.DEFAULT_SOUND_ON;
    private double musicVolume = SettingsMenuConfig.DEFAULT_MUSIC_VOLUME;
    private double soundVolume = SettingsMenuConfig.DEFAULT_SOUND_VOLUME;

    public SettingsMenuView(GameManager gameManager) {
        super(SettingsMenuConfig.SETTINGS_MENU_SPACING);
        this.gameManager = gameManager;

        this.setStyle(SettingsMenuConfig.SETTINGS_MENU_STYLE_CSS);
        this.setPrefSize(SettingsMenuConfig.SETTINGS_MENU_WIDTH, SettingsMenuConfig.SETTINGS_MENU_HEIGHT);
        this.setAlignment(Pos.CENTER);

        // Tiêu đề
        Label title = new Label(SettingsMenuConfig.SETTINGS_MENU_TITLE);
        title.setTextFill(SettingsMenuConfig.SETTINGS_MENU_FONT_COLOR);
        title.setFont(Font.font(SettingsMenuConfig.SETTINGS_MENU_FONT_FAMILY, SettingsMenuConfig.SETTINGS_MENU_TITLE_FONT_SIZE));

        // Thông tin người chơi
        VBox playerInfo = new VBox(SettingsMenuConfig.SETTINGS_PLAYER_INFO_SPACING);
        playerInfo.setAlignment(Pos.CENTER);
        nameLabel = new Label("Player: ");
        levelLabel = new Label("Level: ");
        nameLabel.setTextFill(SettingsMenuConfig.SETTINGS_MENU_FONT_COLOR);
        levelLabel.setTextFill(SettingsMenuConfig.SETTINGS_MENU_FONT_COLOR);

        nameLabel.setFont(Font.font(SettingsMenuConfig.SETTINGS_MENU_FONT_FAMILY, SettingsMenuConfig.SETTINGS_MENU_BODY_FONT_SIZE));
        levelLabel.setFont(Font.font(SettingsMenuConfig.SETTINGS_MENU_FONT_FAMILY, SettingsMenuConfig.SETTINGS_MENU_BODY_FONT_SIZE));
        playerInfo.getChildren().addAll(nameLabel, levelLabel);

        // Nút Resume
        Button resume = new Button(SettingsMenuConfig.SETTINGS_RESUME_BUTTON_TEXT);
        resume.setPrefWidth(SettingsMenuConfig.SETTINGS_MENU_BUTTON_WIDTH);
        resume.setOnAction(e -> {
            if (this.gameManager != null) {
                // ⚠️ GỌI LOGIC TỪ MANAGER
                this.gameManager.toggleSettingsMenu();
            }
        });

        // Nút Save Game
        Button save = new Button(SettingsMenuConfig.SETTINGS_SAVE_BUTTON_TEXT);
        save.setPrefWidth(SettingsMenuConfig.SETTINGS_MENU_BUTTON_WIDTH);
        save.setOnAction(e -> System.out.println("Save game logic here"));

        // Nút Exit
        Button exit = new Button(SettingsMenuConfig.SETTINGS_EXIT_BUTTON_TEXT);
        exit.setPrefWidth(SettingsMenuConfig.SETTINGS_MENU_BUTTON_WIDTH);
        exit.setOnAction(e -> System.exit(0));

        // Toggle nhạc
        Button musicToggle = new Button(SettingsMenuConfig.SETTINGS_MUSIC_BUTTON_TEXT_PREFIX + (musicOn ? SettingsMenuConfig.SETTINGS_TEXT_ON : SettingsMenuConfig.SETTINGS_TEXT_OFF));
        musicToggle.setPrefWidth(SettingsMenuConfig.SETTINGS_MENU_BUTTON_WIDTH);
        musicToggle.setOnAction(e -> {
            musicOn = !musicOn;
            musicToggle.setText(SettingsMenuConfig.SETTINGS_MUSIC_BUTTON_TEXT_PREFIX + (musicOn ? SettingsMenuConfig.SETTINGS_TEXT_ON : SettingsMenuConfig.SETTINGS_TEXT_OFF));
        });

        Slider musicSlider = new Slider(SettingsMenuConfig.SLIDER_MIN_VALUE, SettingsMenuConfig.SLIDER_MAX_VALUE, musicVolume);
        musicSlider.setPrefWidth(SettingsMenuConfig.SETTINGS_MENU_BUTTON_WIDTH);
        musicSlider.valueProperty().addListener((obs, oldVal, newVal) -> musicVolume = newVal.doubleValue());

        // Toggle âm thanh
        Button soundToggle = new Button(SettingsMenuConfig.SETTINGS_SOUND_BUTTON_TEXT_PREFIX + (soundOn ? SettingsMenuConfig.SETTINGS_TEXT_ON : SettingsMenuConfig.SETTINGS_TEXT_OFF));
        soundToggle.setPrefWidth(SettingsMenuConfig.SETTINGS_MENU_BUTTON_WIDTH);
        soundToggle.setOnAction(e -> {
            soundOn = !soundOn;
            soundToggle.setText(SettingsMenuConfig.SETTINGS_SOUND_BUTTON_TEXT_PREFIX + (soundOn ? SettingsMenuConfig.SETTINGS_TEXT_ON : SettingsMenuConfig.SETTINGS_TEXT_OFF));
        });

        Slider soundSlider = new Slider(SettingsMenuConfig.SLIDER_MIN_VALUE, SettingsMenuConfig.SLIDER_MAX_VALUE, soundVolume);
        soundSlider.setPrefWidth(SettingsMenuConfig.SETTINGS_MENU_BUTTON_WIDTH);
        soundSlider.valueProperty().addListener((obs, oldVal, newVal) -> soundVolume = newVal.doubleValue());

        // Slider cho Hotbar Scale
        Label hotbarLabel = new Label("Hotbar Scale");
        hotbarLabel.setTextFill(SettingsMenuConfig.SETTINGS_MENU_FONT_COLOR);
        hotbarLabel.setFont(Font.font(SettingsMenuConfig.SETTINGS_MENU_FONT_FAMILY, SettingsMenuConfig.SETTINGS_MENU_BODY_FONT_SIZE));

        Slider hotbarScaleSlider = new Slider(0.2, 1.5, hotbarScale); // Min 20%, Max 150%
        hotbarScaleSlider.setPrefWidth(SettingsMenuConfig.SETTINGS_MENU_BUTTON_WIDTH);
        hotbarScaleSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            // [MỚI] Gọi thẳng đến HotbarView để update layout
            if (gameManager != null && gameManager.getMainGameView() != null && gameManager.getMainGameView().getHotbarView() != null) {
                gameManager.getMainGameView().getHotbarView().updateLayout(newVal.doubleValue());
            }
        });

        this.getChildren().addAll(
                title,
                playerInfo,
                resume,
                save,
                exit,
                musicToggle,
                musicSlider,
                soundToggle,
                soundSlider,
                hotbarLabel,
                hotbarScaleSlider
        );

        // Đặt menu giữa màn hình
        this.setLayoutX(WindowConfig.SCREEN_WIDTH / 2 - SettingsMenuConfig.SETTINGS_MENU_WIDTH / 2);
        this.setLayoutY(WindowConfig.SCREEN_HEIGHT / 2 - SettingsMenuConfig.SETTINGS_MENU_HEIGHT / 2);

        this.setVisible(false); // Ẩn ban đầu
    }

    public void updatePlayerInfo(String playerName, int playerLevel) {
        nameLabel.setText("Player: " + playerName);
        levelLabel.setText("Level: " + playerLevel);
    }

    public void show() {
        setVisible(true);
    }

    public void hide() {
        setVisible(false);
    }
}