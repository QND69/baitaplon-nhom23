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

    private Pane rootPane;    // Root pane
    private Pane worldPane;   // Pane "thế giới" (chứa map)

    private PlayerView playerView;

    private WorldMap worldMap; // Tham chiếu tới WorldMap (Model)

    private String grassPath = "/assets/images/world/grassDraft.png"; // URL của grass
    private Image grassTexture = new Image(getClass().getResourceAsStream(grassPath)); // texture của grass

    private String soilPath = "/assets/images/world/soilDraft.png"; // URL của soil
    private Image soilTexture = new Image(getClass().getResourceAsStream(soilPath)); //texture của soil

    private String waterPath = "/assets/images/world/waterDraft.png"; // URL của water
    private Image waterTexture = new Image(getClass().getResourceAsStream(waterPath)); // texture của water

    private Image logo = new Image("/assets/images/GUI/logo.png"); // logo game

    public void initUI(Stage primaryStage, GameController gameController) {

        this.rootPane = new Pane();
        this.worldPane = new Pane();
        this.playerView = new PlayerView();
        this.worldMap = new WorldMap(WORLD_SIZE, WORLD_SIZE); // khởi tạo đối tượng worldMap

        System.out.println("Creating Map...");

        // Thêm worldPane vào root
        rootPane.getChildren().add(worldPane);

        // Đặt nhân vật ĐỨNG YÊN ở giữa màn hình
        playerView.getSprite().setLayoutX(SCREEN_WIDTH / 2 - playerView.getWidth() / 2);
        playerView.getSprite().setLayoutY(SCREEN_HEIGHT / 2 - playerView.getHeight() / 2);

        // Thêm nhân vật vào "lớp trên" của root
        rootPane.getChildren().add(playerView.getSprite());

        primaryStage.getIcons().add(logo); // logo game

        primaryStage.setTitle("Farm Simulation"); // đặt tên game

        primaryStage.setFullScreen(false); // toàn màn hình

        primaryStage.setResizable(false); // kéo dãn cửa sổ


        Scene scene = new Scene(rootPane, SCREEN_WIDTH, SCREEN_HEIGHT, Color.GREENYELLOW); // khởi tạo obj scene

        gameController.setupInputListeners(scene); // Giao "Scene" cho "Controller" để nó bắt đầu lắng nghe phím

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Hàm này được gọi 60 lần/giây bởi GameManager (trong game loop)
    // Nhiệm vụ: Xóa map cũ, chỉ vẽ các ô (tile) mà camera thấy.
    public void updateMap(double worldOffsetX, double worldOffsetY){
        if(this.worldMap == null){
            return;
        }

        // xóa map cũ
        worldPane.getChildren().clear();

        // *** TÍNH TOÁN VÙNG CAMERA NHÌN THẤY ***

        /* Tính tọa độ của camera (góc trên-trái màn hình) trong thế giới
        Tọa độ của worldPane (worldOffsetX) là tọa độ của (0,0) thế giới so với màn hình
        => Tọa độ của màn hình so với thế giới là giá trị âm của nó */
        double cameraWorldX = -worldOffsetX;
        double cameraWorldY = -worldOffsetY;

        // Tính toán xem camera đang nhìn thấy các ô (tile) có chỉ số (row, col) nào
        // +2 để đảm bảo luôn vẽ đủ các ô ở rìa màn hình (buffer)
        int startCol = (int) Math.floor(cameraWorldX / TILE_SIZE);
        int startRow = (int) Math.floor(cameraWorldY / TILE_SIZE);
        int numCols = (int) (SCREEN_WIDTH / TILE_SIZE) + 2;
        int numRows = (int) (SCREEN_HEIGHT / TILE_SIZE) + 2;

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

                // *** QUAN TRỌNG: Đặt vị trí ô (tile) theo TỌA ĐỘ THẾ GIỚI ***
                // (col * TILE_SIZE) là tọa độ X của ô này trong worldPane
                tileView.setLayoutX(col * TILE_SIZE);
                tileView.setLayoutY(row * TILE_SIZE);
                worldPane.getChildren().add(tileView);

                // Tạo viền kiểm tra
                /*Rectangle tileBorder = new Rectangle(TILE_SIZE, TILE_SIZE);
                tileBorder.setStroke(Color.RED);
                tileBorder.setStrokeWidth(1);
                tileBorder.setFill(null);
                tileBorder.setVisible(false); // Vẫn bật viền để debug

                StackPane tileStack = new StackPane();
                tileStack.getChildren().addAll(tileView, tileBorder);

                tileStack.setLayoutX(col * TILE_SIZE);
                tileStack.setLayoutY(row * TILE_SIZE);

                worldPane.getChildren().add(tileStack);*/
            }
        }
    }

     // Hàm này chỉ gọi 1 lần đầu
    /*private void drawMap() {
        int numCols = (int) (SCREEN_WIDTH / TILE_SIZE) + 1; // Số cột ( +1 để không bị hở)
        int numRows = (int) (SCREEN_HEIGHT / TILE_SIZE) + 1; // Số hàng

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                ImageView grassTile = new ImageView(grassTexture);

                grassTile.setFitHeight(TILE_SIZE);
                grassTile.setFitWidth(TILE_SIZE);

                // Tạo viền kiểm tra
                Rectangle grassBorder = new Rectangle(TILE_SIZE, TILE_SIZE); // tạo hcn
                grassBorder.setStroke(Color.RED);  // màu viền
                grassBorder.setStrokeWidth(1);    // độ dày viền
                grassBorder.setFill(null);         // không tô nền

                // Chồng ImageView + border
                StackPane tileStack = new StackPane();
                tileStack.getChildren().addAll(grassTile, grassBorder);
                tileStack.setLayoutX(col * TILE_SIZE);
                tileStack.setLayoutY(row * TILE_SIZE);

                grassBorder.setVisible(true); // hiển thị viền ô grass
                worldPane.getChildren().add(tileStack);

                //grassTile.setX(col * TILE_SIZE);
                //grassTile.setY(row * TILE_SIZE);

                //worldPane.getChildren().add(grassTile);
            }
        }
    }*/
}