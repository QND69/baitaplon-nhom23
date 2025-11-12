package com.example.farmSimulation.view;

import com.example.farmSimulation.config.AssetPaths;
import com.example.farmSimulation.config.GameConfig;
import com.example.farmSimulation.controller.GameController;
import com.example.farmSimulation.model.Tile;
import com.example.farmSimulation.model.WorldMap;
import com.example.farmSimulation.view.assets.AssetManager;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
@Getter
@Setter
public class MainGameView {
    private final ImageView[][] screenTiles; // Mảng 2D LƯU TRỮ các ImageView

    private final AssetManager assetManager; // Để lấy textures
    private final WorldMap worldMap;         // Để biết vẽ tile gì

    private Pane rootPane;    // Root pane
    private Pane worldPane;   // Pane "thế giới" chứa lưới, chỉ dùng để di chuyển cuộn mượt
    private Rectangle tileSelector; // Hình vuông chứa ô được chọn

    // Lưu lại vị trí render map lần cuối
    private int lastRenderedStartCol = -1;
    private int lastRenderedStartRow = -1;

    // --- Các thành phần cho UI tạm thời ---
    private Text temporaryText;       // Đối tượng Text để hiển thị thông báo tạm thời
    private SequentialTransition temporaryTextAnimation; // Animation cho text

    /**
     * Constructor (Hàm khởi tạo) nhận các thành phần nó cần
     * (Dependency Injection)
     */
    public MainGameView(AssetManager assetManager, WorldMap worldMap) {
        this.assetManager = assetManager;
        this.worldMap = worldMap;
        this.screenTiles = new ImageView[GameConfig.NUM_ROWS_ON_SCREEN][GameConfig.NUM_COLS_ON_SCREEN];
    }

    /**
     * initUI nhận Controller và PlayerSprite từ bên ngoài (từ class Game)
     */
    public void initUI(Stage primaryStage, GameController gameController, ImageView playerSprite) {
        //System.out.println("Creating Map...");

        this.rootPane = new Pane();
        this.worldPane = new Pane();

        // Chèn các tile rỗng vào worldPane
        for (int r = 0; r < GameConfig.NUM_ROWS_ON_SCREEN; r++) {
            for (int c = 0; c < GameConfig.NUM_COLS_ON_SCREEN; c++) {
                ImageView tileView = new ImageView();
                tileView.setFitHeight(GameConfig.TILE_SIZE);
                tileView.setFitWidth(GameConfig.TILE_SIZE);
                tileView.setLayoutX(c * GameConfig.TILE_SIZE);
                tileView.setLayoutY(r * GameConfig.TILE_SIZE);
                this.screenTiles[r][c] = tileView;
                worldPane.getChildren().add(tileView);
            }
        }

        // Khởi tạo ô vuông selector
        this.tileSelector = new Rectangle(GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
        this.tileSelector.setFill(null);                                    // Không tô nền
        this.tileSelector.setStroke(GameConfig.SELECTOR_COLOR);             // Màu viền
        this.tileSelector.setStrokeWidth(GameConfig.SELECTOR_STROKE_WIDTH); // Độ dày viền
        this.tileSelector.setVisible(true);                                 // Luôn hiển thị
        rootPane.getChildren().addAll(worldPane, tileSelector); // Thêm worldPane và tileSelector vào root

        // --- Khởi tạo Temporary Text ---
        temporaryText = new Text();
        temporaryText.setFont(GameConfig.TEMP_TEXT_FONT);
        temporaryText.setFill(GameConfig.TEMP_TEXT_COLOR);
        temporaryText.setFont(GameConfig.TEMP_TEXT_FONT);
        temporaryText.setStroke(GameConfig.TEMP_TEXT_STROKE_COLOR);
        temporaryText.setStrokeWidth(GameConfig.TEMP_TEXT_STROKE_WIDTH);
        temporaryText.setOpacity(0); // Ban đầu ẩn
        temporaryText.setManaged(false); // Không ảnh hưởng layout
        rootPane.getChildren().add(temporaryText);


        // Đặt nhân vật (nhận từ bên ngoài) vào giữa màn hình
        playerSprite.setLayoutX(GameConfig.SCREEN_WIDTH / 2 - playerSprite.getFitWidth() / 2);
        playerSprite.setLayoutY(GameConfig.SCREEN_HEIGHT / 2 - playerSprite.getFitHeight() / 2);
        rootPane.getChildren().add(playerSprite); // Thêm nhân vật vào root

        Scene scene = new Scene(rootPane, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT, GameConfig.BACKGROUND_COLOR);
        gameController.setupInputListeners(scene);

        primaryStage.getIcons().add(assetManager.getTexture(AssetPaths.LOGO)); // Lấy logo từ manager
        primaryStage.setTitle(GameConfig.GAME_TITLE);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Hàm này được gọi nếu có thay đổi về thế giới
    // Nhiệm vụ: Xóa map cũ, chỉ vẽ các ô (tile) mà camera thấy.
    public void updateMap(double worldOffsetX, double worldOffsetY, boolean forceRedraw) {
        // *** TÍNH TOÁN VÙNG CAMERA NHÌN THẤY ***
        /* Tính tọa độ của camera (góc trên-trái màn hình) trong thế giới
        Tọa độ của worldPane (worldOffsetX và worldOffsetY) là tọa độ của (0,0) của thế giới so với màn hình
        => Tọa độ của màn hình so với thế giới là giá trị âm của nó */
        double cameraWorldX = -worldOffsetX;
        double cameraWorldY = -worldOffsetY;

        // Tính ô logic bắt đầu (số nguyên) mà camera nhìn thấy
        int startCol = (int) Math.floor(cameraWorldX / GameConfig.TILE_SIZE);
        int startRow = (int) Math.floor(cameraWorldY / GameConfig.TILE_SIZE);

        // Tính phần dư (pixel lẻ) để cuộn mượt
        // Đây là mấu chốt: worldPane chỉ di chuyển trong phạm vi 1 ô
        double pixelOffsetX = -(cameraWorldX - (startCol * GameConfig.TILE_SIZE));
        double pixelOffsetY = -(cameraWorldY - (startRow * GameConfig.TILE_SIZE));

        // Di chuyển TOÀN BỘ worldPane (chứa lưới) để tạo hiệu ứng mượt
        worldPane.setLayoutX(pixelOffsetX);
        worldPane.setLayoutY(pixelOffsetY);

        // Kiểm tra xem có CẦN vẽ lại các ô hay không
        boolean needsTileUpdate = (startCol != lastRenderedStartCol ||
                startRow != lastRenderedStartRow ||
                forceRedraw);

        // Không cần vẽ lại, tiết kiệm rất nhiều CPU
        if (!needsTileUpdate) {
            return;
        }

        // CẬP NHẬT HÌNH ẢNH (TEXTURE) cho các ô trong lưới
        for (int r = 0; r < GameConfig.NUM_ROWS_ON_SCREEN; r++) {
            for (int c = 0; c < GameConfig.NUM_COLS_ON_SCREEN; c++) {
                // Tính ô logic (thế giới) mà ô lưới (màn hình) này cần hiển thị
                int logicalCol = startCol + c;
                int logicalRow = startRow + r;

                // Lấy loại tile từ Model
                Tile type = worldMap.getTileType(logicalCol, logicalRow);

                // Lấy ảnh từ AssetManager
                Image textureToDraw = assetManager.getTileTexture(type);

                // Chỉ THAY ẢNH, không tạo mới
                this.screenTiles[r][c].setImage(textureToDraw);
            }
        }
        // Ghi nhớ vị trí render lần cuối
        this.lastRenderedStartCol = startCol;
        this.lastRenderedStartRow = startRow;
    }

    // Hàm này được gọi 60 lần/giây bởi Game Loop.
    // Nhiệm vụ: Tính toán và di chuyển "ô chọn" (selector)
    // để nó "bắt dính" (snap) vào ô (Tile) mà chuột đang trỏ vào.
    public void updateSelector(int tileSelectedX, int tileSelectedY, double worldOffsetX, double worldOffsetY) {
        // Kiểm tra tileSelector được khai báo chưa
        if (this.tileSelector == null) {
            return;
        }
        // Tọa độ thực của ô trên màn hình
        double tileSelectedOnScreenX = tileSelectedX * GameConfig.TILE_SIZE + worldOffsetX;
        double tileSelectedOnScreenY = tileSelectedY * GameConfig.TILE_SIZE + worldOffsetY;

        // Hiển thị ô được trỏ chuột
        this.tileSelector.setLayoutX(tileSelectedOnScreenX);
        this.tileSelector.setLayoutY(tileSelectedOnScreenY);
    }

    /**
     * Hiển thị một đoạn text tạm thời trên đầu người chơi, sau đó mờ dần và biến mất.
     * @param message Nội dung text cần hiển thị.
     * @param playerScreenX Tọa độ X của người chơi trên màn hình.
     * @param playerScreenY Tọa độ Y của người chơi trên màn hình.
     */
    public void showTemporaryText(String message, double playerScreenX, double playerScreenY) {
        if (temporaryTextAnimation != null && temporaryTextAnimation.getStatus().equals(javafx.animation.Animation.Status.RUNNING)) {
            temporaryTextAnimation.stop(); // Dừng animation cũ nếu đang chạy
        }

        temporaryText.setText(message);
        temporaryText.setLayoutX(playerScreenX - temporaryText.getLayoutBounds().getWidth() / 2); // Căn giữa
        temporaryText.setLayoutY(playerScreenY + GameConfig.TEMP_TEXT_OFFSET_Y); // Trên đầu player
        temporaryText.setOpacity(1); // Hiển thị ngay lập tức

        FadeTransition fadeOut = new FadeTransition(Duration.millis(GameConfig.TEMP_TEXT_FADE_DURATION), temporaryText);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        PauseTransition pause = new PauseTransition(Duration.millis(GameConfig.TEMP_TEXT_DISPLAY_DURATION - GameConfig.TEMP_TEXT_FADE_DURATION));

        temporaryTextAnimation = new SequentialTransition(temporaryText, pause, fadeOut);
        temporaryTextAnimation.play();
    }
    private VBox settingsMenu;
    private boolean musicOn = true;
    private boolean soundOn = true;
    private double musicVolume = 0.5;
    private double soundVolume = 0.5;

    public void showSettingsMenu(String playerName, int playerLevel) {
        if (settingsMenu == null) {
            settingsMenu = new VBox(15);
            settingsMenu.setStyle(
                    "-fx-background-color: rgba(0,0,0,0.6);" +  // nền mờ
                            "-fx-padding: 30;" +
                            "-fx-border-radius: 10;" +
                            "-fx-background-radius: 10;"
            );
            settingsMenu.setPrefSize(400, 500);
            settingsMenu.setAlignment(Pos.CENTER);

            // Tiêu đề
            Label title = new Label("⚙ Game Menu");
            title.setTextFill(Color.WHITESMOKE);
            title.setFont(Font.font("Arial", 28));

            // Thông tin người chơi
            VBox playerInfo = new VBox(5);
            playerInfo.setAlignment(Pos.CENTER);
            Label nameLabel = new Label("Player: " + playerName);
            Label levelLabel = new Label("Level: " + playerLevel);
            nameLabel.setTextFill(Color.WHITESMOKE);
            levelLabel.setTextFill(Color.WHITESMOKE);
            nameLabel.setFont(Font.font(18));
            levelLabel.setFont(Font.font(18));
            playerInfo.getChildren().addAll(nameLabel, levelLabel);

            // Nút Resume
            Button resume = new Button("Resume");
            resume.setPrefWidth(200);
            resume.setOnAction(e -> hideSettingsMenu());


            // Nút Save Game
            Button save = new Button("Save Game");
            save.setPrefWidth(200);
            save.setOnAction(e -> System.out.println("Save game logic here"));

            // Nút Exit
            Button exit = new Button("Exit Game");
            exit.setPrefWidth(200);
            exit.setOnAction(e -> System.exit(0));

            // Toggle nhạc
            Button musicToggle = new Button("Music: ON");
            musicToggle.setPrefWidth(200);
            musicToggle.setOnAction(e -> {
                musicOn = !musicOn;
                musicToggle.setText("Music: " + (musicOn ? "ON" : "OFF"));
            });

            javafx.scene.control.Slider musicSlider = new javafx.scene.control.Slider(0, 1, musicVolume);
            musicSlider.setPrefWidth(200);
            musicSlider.valueProperty().addListener((obs, oldVal, newVal) -> musicVolume = newVal.doubleValue());

            // Toggle âm thanh
            Button soundToggle = new Button("Sound: ON");
            soundToggle.setPrefWidth(200);
            soundToggle.setOnAction(e -> {
                soundOn = !soundOn;
                soundToggle.setText("Sound: " + (soundOn ? "ON" : "OFF"));
            });

            javafx.scene.control.Slider soundSlider = new javafx.scene.control.Slider(0, 1, soundVolume);
            soundSlider.setPrefWidth(200);
            soundSlider.valueProperty().addListener((obs, oldVal, newVal) -> soundVolume = newVal.doubleValue());

            settingsMenu.getChildren().addAll(
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
            settingsMenu.setLayoutX(GameConfig.SCREEN_WIDTH / 2 - 200);
            settingsMenu.setLayoutY(GameConfig.SCREEN_HEIGHT / 2 - 250);

            rootPane.getChildren().add(settingsMenu);
        }

        settingsMenu.setVisible(true);
    }
    public void hideSettingsMenu() {
        if (settingsMenu != null) {
            settingsMenu.setVisible(false);
        }
    }
}