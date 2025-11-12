package com.example.farmSimulation.view;

import com.example.farmSimulation.config.AssetPaths;
import com.example.farmSimulation.config.GameConfig;
import com.example.farmSimulation.controller.GameController;
import com.example.farmSimulation.model.Tile;
import com.example.farmSimulation.model.WorldMap;
import com.example.farmSimulation.view.assets.AssetManager;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

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

        // Thêm worldPane và tileSelector vào root
        rootPane.getChildren().addAll(worldPane, tileSelector);

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
}