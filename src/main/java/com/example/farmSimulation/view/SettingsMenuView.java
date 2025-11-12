package com.example.farmSimulation.view;

import com.example.farmSimulation.config.GameConfig;
import com.example.farmSimulation.model.GameManager;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class SettingsMenuView extends VBox {
    private final GameManager gameManager;
    private final Label nameLabel;
    private final Label levelLabel;

    private boolean musicOn = GameConfig.DEFAULT_MUSIC_ON;
    private boolean soundOn = GameConfig.DEFAULT_SOUND_ON;
    private double musicVolume = GameConfig.DEFAULT_MUSIC_VOLUME;
    private double soundVolume = GameConfig.DEFAULT_SOUND_VOLUME;

    public SettingsMenuView(GameManager gameManager) {
        super(GameConfig.SETTINGS_MENU_SPACING);
        this.gameManager = gameManager;

        this.setStyle(GameConfig.SETTINGS_MENU_STYLE_CSS);
        this.setPrefSize(GameConfig.SETTINGS_MENU_WIDTH, GameConfig.SETTINGS_MENU_HEIGHT);
        this.setAlignment(Pos.CENTER);

        // Tiêu đề
        Label title = new Label(GameConfig.SETTINGS_MENU_TITLE);
        title.setTextFill(GameConfig.SETTINGS_MENU_FONT_COLOR);
        title.setFont(Font.font(GameConfig.SETTINGS_MENU_FONT_FAMILY, GameConfig.SETTINGS_MENU_TITLE_FONT_SIZE));

        // Thông tin người chơi
        VBox playerInfo = new VBox(GameConfig.SETTINGS_PLAYER_INFO_SPACING);
        playerInfo.setAlignment(Pos.CENTER);
        nameLabel = new Label("Player: ");
        levelLabel = new Label("Level: ");
        nameLabel.setTextFill(GameConfig.SETTINGS_MENU_FONT_COLOR);
        levelLabel.setTextFill(GameConfig.SETTINGS_MENU_FONT_COLOR);

        nameLabel.setFont(Font.font(GameConfig.SETTINGS_MENU_FONT_FAMILY, GameConfig.SETTINGS_MENU_BODY_FONT_SIZE));
        levelLabel.setFont(Font.font(GameConfig.SETTINGS_MENU_FONT_FAMILY, GameConfig.SETTINGS_MENU_BODY_FONT_SIZE));
        playerInfo.getChildren().addAll(nameLabel, levelLabel);

        // Nút Resume
        Button resume = new Button(GameConfig.SETTINGS_RESUME_BUTTON_TEXT);
        resume.setPrefWidth(GameConfig.SETTINGS_MENU_BUTTON_WIDTH);
        resume.setOnAction(e -> {
            if (this.gameManager != null) {
                // ⚠️ GỌI LOGIC TỪ MANAGER
                this.gameManager.toggleSettingsMenu();
            }
        });

        // Nút Save Game
        Button save = new Button(GameConfig.SETTINGS_SAVE_BUTTON_TEXT);
        save.setPrefWidth(GameConfig.SETTINGS_MENU_BUTTON_WIDTH);
        save.setOnAction(e -> System.out.println("Save game logic here"));

        // Nút Exit
        Button exit = new Button(GameConfig.SETTINGS_EXIT_BUTTON_TEXT);
        exit.setPrefWidth(GameConfig.SETTINGS_MENU_BUTTON_WIDTH);
        exit.setOnAction(e -> System.exit(0));

        // Toggle nhạc
        Button musicToggle = new Button(GameConfig.SETTINGS_MUSIC_BUTTON_TEXT_PREFIX + (musicOn ? GameConfig.SETTINGS_TEXT_ON : GameConfig.SETTINGS_TEXT_OFF));
        musicToggle.setPrefWidth(GameConfig.SETTINGS_MENU_BUTTON_WIDTH);
        musicToggle.setOnAction(e -> {
            musicOn = !musicOn;
            musicToggle.setText(GameConfig.SETTINGS_MUSIC_BUTTON_TEXT_PREFIX + (musicOn ? GameConfig.SETTINGS_TEXT_ON : GameConfig.SETTINGS_TEXT_OFF));
        });

        Slider musicSlider = new Slider(GameConfig.SLIDER_MIN_VALUE, GameConfig.SLIDER_MAX_VALUE, musicVolume);
        musicSlider.setPrefWidth(GameConfig.SETTINGS_MENU_BUTTON_WIDTH);
        musicSlider.valueProperty().addListener((obs, oldVal, newVal) -> musicVolume = newVal.doubleValue());

        // Toggle âm thanh
        Button soundToggle = new Button(GameConfig.SETTINGS_SOUND_BUTTON_TEXT_PREFIX + (soundOn ? GameConfig.SETTINGS_TEXT_ON : GameConfig.SETTINGS_TEXT_OFF));
        soundToggle.setPrefWidth(GameConfig.SETTINGS_MENU_BUTTON_WIDTH);
        soundToggle.setOnAction(e -> {
            soundOn = !soundOn;
            soundToggle.setText(GameConfig.SETTINGS_SOUND_BUTTON_TEXT_PREFIX + (soundOn ? GameConfig.SETTINGS_TEXT_ON : GameConfig.SETTINGS_TEXT_OFF));
        });

        Slider soundSlider = new Slider(GameConfig.SLIDER_MIN_VALUE, GameConfig.SLIDER_MAX_VALUE, soundVolume);
        soundSlider.setPrefWidth(GameConfig.SETTINGS_MENU_BUTTON_WIDTH);
        soundSlider.valueProperty().addListener((obs, oldVal, newVal) -> soundVolume = newVal.doubleValue());

        this.getChildren().addAll(
                title,
                playerInfo,
                resume,
                save,
                exit,
                musicToggle,
                musicSlider,
                soundToggle,
                soundSlider
        );

        // Đặt menu giữa màn hình
        this.setLayoutX(GameConfig.SCREEN_WIDTH / 2 - GameConfig.SETTINGS_MENU_WIDTH / 2);
        this.setLayoutY(GameConfig.SCREEN_HEIGHT / 2 - GameConfig.SETTINGS_MENU_HEIGHT / 2);

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