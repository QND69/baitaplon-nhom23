package com.example.farmSimulation.view;

import com.example.farmSimulation.controller.GameController;
import com.example.farmSimulation.model.Tile;
import com.example.farmSimulation.model.WorldMap;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MainGameView {
    private final static int WORLD_SIZE = 10000; // Độ rộng thế giới
    private final double SCREEN_WIDTH = 1280; // chiều ngang màn hình
    private final double SCREEN_HEIGHT = 720; // chiều dọc màn hình
    private final double TILE_SIZE = 60; // kích thước 1 ô (n x n)

    private final int NUM_COLS_ON_SCREEN = (int) (SCREEN_WIDTH / TILE_SIZE) + 2; // Số cột của mảng screenTile
    private final int NUM_ROWS_ON_SCREEN = (int) (SCREEN_HEIGHT / TILE_SIZE) + 2; // Số hàng của mảng screenTile
    private ImageView[][] screenTiles;// Mảng 2D LƯU TRỮ các ImageView để tái sử dụng

    private Pane rootPane;    // Root pane
    private Pane worldPane;   // Pane "thế giới" (chứa map)
    private WorldMap worldMap; // Tham chiếu tới WorldMap (Model), chứa thông tin về các ô
    private PlayerView playerView;
    private Rectangle tileSelector;

    private String grassPath = "/assets/images/world/grassDraft.png"; // URL của grass
    private Image grassTexture = new Image(getClass().getResourceAsStream(grassPath)); // texture của grass

    private String soilPath = "/assets/images/world/soilDraft.png"; // URL của soil
    private Image soilTexture = new Image(getClass().getResourceAsStream(soilPath)); //texture của soil

    private String waterPath = "/assets/images/world/waterDraft.png"; // URL của water
    private Image waterTexture = new Image(getClass().getResourceAsStream(waterPath)); // texture của water

    private Image logo = new Image("/assets/images/GUI/logo.png"); // logo game


    public void initUI(Stage primaryStage, GameController gameController) {
        System.out.println("Creating Map...");

        this.rootPane = new Pane();
        this.worldPane = new Pane();
        this.playerView = new PlayerView();
        this.worldMap = new WorldMap(WORLD_SIZE, WORLD_SIZE); // Khởi tạo đối tượng worldMap
        this.screenTiles = new ImageView[NUM_ROWS_ON_SCREEN][NUM_COLS_ON_SCREEN];

        // Chèn các tile rỗng vào worldPane
        for (int r = 0; r < NUM_ROWS_ON_SCREEN; r++) {
            for (int c = 0; c < NUM_COLS_ON_SCREEN; c++) {
                // Tạo ImageView
                ImageView tileView = new ImageView();

                tileView.setFitHeight(TILE_SIZE);
                tileView.setFitWidth(TILE_SIZE);

                // Đặt vị trí TƯƠNG ĐỐI BÊN TRONG worldPane
                // Vị trí này sẽ không bao giờ thay đổi nữa
                tileView.setLayoutX(c * TILE_SIZE);
                tileView.setLayoutY(r * TILE_SIZE);
                this.screenTiles[r][c] = tileView; // Lưu lại
                worldPane.getChildren().add(tileView);
            }
        }

        // Khởi tạo ô vuông selector
        this.tileSelector = new Rectangle(TILE_SIZE, TILE_SIZE);
        this.tileSelector.setFill(null); // Không tô nền
        this.tileSelector.setStroke(Color.BLACK); // Màu viền
        this.tileSelector.setStrokeWidth(1); // Độ dày viền
        this.tileSelector.setVisible(true); // Luôn hiển thị

        // Thêm worldPane và tileSelector vào root
        rootPane.getChildren().addAll(worldPane, tileSelector);

        // Đặt nhân vật ĐỨNG YÊN ở giữa màn hình
        playerView.getSprite().setLayoutX(SCREEN_WIDTH / 2 - playerView.getWidth() / 2);
        playerView.getSprite().setLayoutY(SCREEN_HEIGHT / 2 - playerView.getHeight() / 2);

        rootPane.getChildren().add(playerView.getSprite()); // Thêm nhân vật vào "lớp trên" của root


        Scene scene = new Scene(rootPane, SCREEN_WIDTH, SCREEN_HEIGHT, Color.GREENYELLOW); // khởi tạo obj scene

        gameController.setupInputListeners(scene); // Giao "Scene" cho "Controller" để nó bắt đầu lắng nghe phím

        primaryStage.getIcons().add(logo); // Logo game
        primaryStage.setTitle("Farm Simulation"); // Đặt tên game
        primaryStage.setFullScreen(false); // Toàn màn hình
        primaryStage.setResizable(false); // Kéo dãn cửa sổ

        primaryStage.setScene(scene); // Đặt scene vào primaryStage
        primaryStage.show(); // Hiển thị cửa sổ
    }

    // Hàm này được gọi nếu có di chuyển
    // Nhiệm vụ: Xóa map cũ, chỉ vẽ các ô (tile) mà camera thấy.
    public void updateMap(double worldOffsetX, double worldOffsetY) {
        if (this.worldMap == null) {
            return;
        }
        // *** TÍNH TOÁN VÙNG CAMERA NHÌN THẤY ***
        /* Tính tọa độ của camera (góc trên-trái màn hình) trong thế giới
        Tọa độ của worldPane (worldOffsetX và worldOffsetY) là tọa độ của (0,0) của thế giới so với màn hình
        => Tọa độ của màn hình so với thế giới là giá trị âm của nó */
        double cameraWorldX = -worldOffsetX;
        double cameraWorldY = -worldOffsetY;

        // Tính ô logic bắt đầu (số nguyên) mà camera nhìn thấy
        int startCol = (int) Math.floor(cameraWorldX / TILE_SIZE);
        int startRow = (int) Math.floor(cameraWorldY / TILE_SIZE);

        // Tính phần dư (pixel lẻ) để cuộn mượt
        // Đây là mấu chốt: worldPane chỉ di chuyển trong phạm vi 1 ô
        double pixelOffsetX = -(cameraWorldX - (startCol * TILE_SIZE));
        double pixelOffsetY = -(cameraWorldY - (startRow * TILE_SIZE));

        // Di chuyển TOÀN BỘ worldPane (chứa lưới) để tạo hiệu ứng mượt
        worldPane.setLayoutX(pixelOffsetX);
        worldPane.setLayoutY(pixelOffsetY);

        // 5. CẬP NHẬT HÌNH ẢNH (TEXTURE) cho các ô trong lưới
        for (int r = 0; r < NUM_ROWS_ON_SCREEN; r++) {
            for (int c = 0; c < NUM_COLS_ON_SCREEN; c++) {
                // Tính ô logic (thế giới) mà ô lưới (màn hình) này cần hiển thị
                int logicalCol = startCol + c;
                int logicalRow = startRow + r;

                // Lấy loại tile từ Model
                Tile type = worldMap.getTileType(logicalCol, logicalRow);
                Image textureToDraw;
                switch (type) {
                    case SOIL: textureToDraw = soilTexture; break;
                    case WATER: textureToDraw = waterTexture; break;
                    default: textureToDraw = grassTexture; break;
                }

                // Chỉ THAY ẢNH, không tạo mới
                if (textureToDraw != null) {
                    this.screenTiles[r][c].setImage(textureToDraw);
                }
            }
        }
    }

    //Hàm này được gọi 60 lần/giây bởi Game Loop.
    //Nhiệm vụ: Tính toán và di chuyển "ô chọn" (selector) để nó "bắt dính" (snap) vào ô (Tile) mà chuột đang trỏ vào.
    public void updateSelector(double mouseX, double mouseY, double worldOffsetX, double worldOffsetY) {
        // Kiểm tra tileSelector được khai báo chưa
        if (this.tileSelector == null) {
            return;
        }

        // Tọa độ logic của chuột trong thế giới
        double mouseWorldX = -worldOffsetX + mouseX;
        double mouseWorldY = -worldOffsetY + mouseY;

        // Tọa độ logic của ô mà chuột trỏ tới
        int tileSelectedX = (int) Math.floor(mouseWorldX / TILE_SIZE);
        int tileSelectedY = (int) Math.floor(mouseWorldY / TILE_SIZE);

        // Tọa độ thực của ô trên màn hình
        double tileSelectedOnScreenX = tileSelectedX * TILE_SIZE + worldOffsetX;
        double tileSelectedOnScreenY = tileSelectedY * TILE_SIZE + worldOffsetY;

        // Hiển thị ô được trỏ chuột
        this.tileSelector.setLayoutX(tileSelectedOnScreenX);
        this.tileSelector.setLayoutY(tileSelectedOnScreenY);
    }
}