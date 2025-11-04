package com.example.farmSimulation.view;

import com.example.farmSimulation.controller.GameController;
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

    private final double SCREEN_WIDTH = 1280; // chiều ngang màn hình
    private final double SCREEN_HEIGHT = 720; // chiều dọc màn hình
    private final double TILE_SIZE = 50; // kích thước 1 ô (n x n)

    private Pane rootPane;    // Root pane
    private Pane worldPane;   // Pane "thế giới" (chứa map)

    private PlayerView playerView;


    private String grassPath = "/assets/images/world/grassDraft.png"; // URL của grass
    private Image grassTexture = new Image(getClass().getResourceAsStream(grassPath));// texture của grass

    private Image logo = new Image("/assets/images/GUI/logo.png"); // logo game

    public void initUI(Stage primaryStage, GameController gameController) {

        this.rootPane = new Pane();
        this.worldPane = new Pane();
        this.playerView = new PlayerView();

        //System.out.println("Đang vẽ map: " + numCols + " cột và " + numRows + " hàng");

        rootPane.getChildren().add(worldPane);

        drawMap(); // Vẽ map 1 lần duy nhất

        // Đặt nhân vật ĐỨNG YÊN ở giữa màn hình
        playerView.getSprite().setLayoutX(SCREEN_WIDTH / 2 - playerView.getWidth() / 2);
        playerView.getSprite().setLayoutY(SCREEN_HEIGHT / 2 - playerView.getHeight() / 2);

        // Thêm nhân vật vào "lớp trên"
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

    private void drawMap() {
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

                /*grassTile.setX(col * TILE_SIZE);
                grassTile.setY(row * TILE_SIZE);

                worldPane.getChildren().add(grassTile);*/
            }
        }
    }
}