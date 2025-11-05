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
    private final static boolean DEBUG = false;
    private final static int WORLD_SIZE = 10000; // Độ rộng thế giới
    private final double SCREEN_WIDTH = 1280; // chiều ngang màn hình
    private final double SCREEN_HEIGHT = 720; // chiều dọc màn hình
    private final double TILE_SIZE = 60; // kích thước 1 ô (n x n)

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

        // Khởi tạo ô vuông selector
        this.tileSelector = new Rectangle(TILE_SIZE, TILE_SIZE);
        this.tileSelector.setFill(null); // Không tô nền
        this.tileSelector.setStroke(Color.BLACK); // Màu viền
        this.tileSelector.setStrokeWidth(1); // Độ dày viền
        this.tileSelector.setVisible(true); // Luôn hiển thị

        // Thêm worldPane vào root
        rootPane.getChildren().addAll(worldPane, tileSelector);


        if (DEBUG) {
            //---------------------------------------------------------------------------
            // 1. Lấy sprite nhân vật
            ImageView playerSprite = playerView.getSprite();

            // 2. Tạo Hitbox cho Player (chỉ tạo 1 lần)
            double hitboxWidth = playerSprite.getFitWidth();
            double hitboxHeight = playerSprite.getFitHeight();

            Rectangle playerHitbox = new Rectangle(hitboxWidth, hitboxHeight);
            playerHitbox.setStroke(Color.ORANGE); // Màu xanh lá
            playerHitbox.setStrokeWidth(2);
            playerHitbox.setFill(null);
            playerHitbox.setVisible(DEBUG); // Hiển thị hitbox nếu debug = true

            // 3. Gộp Player và Hitbox vào một container
            StackPane playerContainer = new StackPane();

            // Đặt container chứa nhân vật đứng yên ở giữa màn hình
            playerContainer.setLayoutX(SCREEN_WIDTH / 2 - playerView.getWidth() / 2);
            playerContainer.setLayoutY(SCREEN_HEIGHT / 2 - playerView.getHeight() / 2);
            playerContainer.getChildren().addAll(playerSprite, playerHitbox);
            rootPane.getChildren().add(playerContainer);
            //---------------------------------------------------------------------------
        } else {
            // Đặt nhân vật ĐỨNG YÊN ở giữa màn hình
            playerView.getSprite().setLayoutX(SCREEN_WIDTH / 2 - playerView.getWidth() / 2);
            playerView.getSprite().setLayoutY(SCREEN_HEIGHT / 2 - playerView.getHeight() / 2);

            rootPane.getChildren().add(playerView.getSprite()); // Thêm nhân vật vào "lớp trên" của root
        }


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

        // xóa map cũ
        worldPane.getChildren().clear();

        // *** TÍNH TOÁN VÙNG CAMERA NHÌN THẤY ***

        /* Tính tọa độ của camera (góc trên-trái màn hình) trong thế giới
        Tọa độ của worldPane (worldOffsetX và worldOffsetY) là tọa độ của (0,0) của thế giới so với màn hình
        => Tọa độ của màn hình so với thế giới là giá trị âm của nó */
        double cameraWorldX = -worldOffsetX;
        double cameraWorldY = -worldOffsetY;

        // Tính toán xem camera đang nhìn thấy các ô (tile) có chỉ số (row, col) nào
        // +2 để đảm bảo luôn vẽ đủ các ô ở rìa màn hình (buffer)
        int startCol = (int) Math.floor(cameraWorldX / TILE_SIZE);
        int startRow = (int) Math.floor(cameraWorldY / TILE_SIZE);
        int numCols = (int) (SCREEN_WIDTH / TILE_SIZE) + 2;
        int numRows = (int) (SCREEN_HEIGHT / TILE_SIZE) + 2;

        if (DEBUG) {
            // *** VẼ CÁC Ô (TILE) TRONG TẦM NHÌN ***
            for (int row = startRow; row < startRow + numRows; row++) {
                for (int col = startCol; col < startCol + numCols; col++) {

                    // "HỎI" MODEL: Ô (col, row) này là loại gì?
                    Tile type = worldMap.getTileType(col, row);

                    // CHỌN HÌNH ẢNH (TEXTURE) TƯƠNG ỨNG
                    Image textureToDraw;
                    switch (type) {
                        case SOIL:
                            textureToDraw = soilTexture;
                            break;
                        case WATER:
                            textureToDraw = waterTexture;
                            break;
                        case GRASS:
                        default: // Nếu là GRASS hoặc bất cứ thứ gì chưa định nghĩa, vẽ CỎ
                            textureToDraw = grassTexture;
                            break;
                    }

                    // TẠO VÀ VẼ Ô (TILE) ĐÓ
                    // (Kiểm tra null phòng trường hợp ảnh bị thiếu)
                    if (textureToDraw == null) continue;

                    ImageView tileView = new ImageView(textureToDraw);
                    tileView.setFitHeight(TILE_SIZE);
                    tileView.setFitWidth(TILE_SIZE);

                    // Tạo viền kiểm tra
                    Rectangle tileBorder = new Rectangle(TILE_SIZE, TILE_SIZE);
                    tileBorder.setStroke(Color.RED);
                    tileBorder.setStrokeWidth(1);
                    tileBorder.setFill(null);
                    tileBorder.setVisible(DEBUG); // Bật viền để debug

                    //Tạo Text để đánh số
                    String coordText = col + "," + row;
                    Text coordinateLabel = new Text(coordText);

                    // (Tùy chọn) Chỉnh style cho text để dễ đọc
                    coordinateLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12)); // Đặt font và kích thước
                    coordinateLabel.setFill(Color.BLACK);   // Màu chữ
                    coordinateLabel.setStroke(Color.WHITE); // Thêm viền đen cho chữ để nổi bật
                    coordinateLabel.setStrokeWidth(0.5);

                    // Hiển thị số nếu đang debug
                    coordinateLabel.setVisible(DEBUG);

                    // Tạo Hitbox Player
                    // Lấy kích thước của playerView (hoặc đặt kích thước hitbox cố định)
                    double hitboxWidth = playerView.getSprite().getFitWidth();  // Hoặc PLAYER_WIDTH
                    double hitboxHeight = playerView.getSprite().getFitHeight(); // Hoặc PLAYER_HEIGHT

                    StackPane tileStack = new StackPane();
                    tileStack.getChildren().addAll(tileView, tileBorder, coordinateLabel);

                    // *** QUAN TRỌNG: Đặt vị trí ô (tile) theo TỌA ĐỘ THẾ GIỚI ***
                    // (col * TILE_SIZE) là tọa độ X của ô này trong worldPane
                    tileStack.setLayoutX(col * TILE_SIZE);
                    tileStack.setLayoutY(row * TILE_SIZE);

                    worldPane.getChildren().add(tileStack);
                }
            }

        } else {
            // *** VẼ CÁC Ô (TILE) TRONG TẦM NHÌN ***
            for (int row = startRow; row < startRow + numRows; row++) {
                for (int col = startCol; col < startCol + numCols; col++) {

                    // "HỎI" MODEL: Ô (col, row) này là loại gì?
                    Tile tileType = worldMap.getTileType(col, row);

                    // Chọn texture tương ứng
                    Image textureToDraw;
                    switch (tileType) {
                        case SOIL:
                            textureToDraw = soilTexture;
                            break;
                        case WATER:
                            textureToDraw = waterTexture;
                            break;
                        case GRASS:
                        default: // Nếu là GRASS hoặc bất cứ thứ gì chưa định nghĩa, vẽ CỎ
                            textureToDraw = grassTexture;
                            break;
                    }

                    // TẠO VÀ VẼ Ô (TILE) ĐÓ
                    // (Kiểm tra null phòng trường hợp ảnh bị thiếu)
                    if (textureToDraw == null) continue;

                    ImageView tileView = new ImageView(textureToDraw);
                    tileView.setFitHeight(TILE_SIZE);
                    tileView.setFitWidth(TILE_SIZE);

                    // *** QUAN TRỌNG: Đặt vị trí ô (tile) theo TỌA ĐỘ THẾ GIỚI ***
                    // (col * TILE_SIZE) là tọa độ X của ô này trong worldPane
                    tileView.setLayoutX(col * TILE_SIZE);
                    tileView.setLayoutY(row * TILE_SIZE);
                    worldPane.getChildren().add(tileView);
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