package com.example.farmSimulation.view;

import com.example.farmSimulation.config.WorldConfig;
import com.example.farmSimulation.model.Tile;
import com.example.farmSimulation.model.WorldMap;
import com.example.farmSimulation.view.assets.AssetManager;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import lombok.Getter;

@Getter
public class WorldRenderer {
    private final ImageView[][] screenTiles; // Mảng 2D LƯU TRỮ các ImageView
    private final AssetManager assetManager; // Để lấy textures
    private final WorldMap worldMap;         // Để biết vẽ tile gì

    private final Pane worldPane;   // Pane "thế giới" chứa lưới, chỉ dùng để di chuyển cuộn mượt
    private final Rectangle tileSelector; // Hình vuông chứa ô được chọn

    // Lưu lại vị trí render map lần cuối
    private int lastRenderedStartCol = -1;
    private int lastRenderedStartRow = -1;

    public WorldRenderer(AssetManager assetManager, WorldMap worldMap) {
        this.assetManager = assetManager;
        this.worldMap = worldMap;
        this.screenTiles = new ImageView[WorldConfig.NUM_ROWS_ON_SCREEN][WorldConfig.NUM_COLS_ON_SCREEN];
        this.worldPane = new Pane();

        // Chèn các tile rỗng vào worldPane
        for (int r = 0; r < WorldConfig.NUM_ROWS_ON_SCREEN; r++) {
            for (int c = 0; c < WorldConfig.NUM_COLS_ON_SCREEN; c++) {
                ImageView tileView = new ImageView();
                tileView.setFitHeight(WorldConfig.TILE_SIZE);
                tileView.setFitWidth(WorldConfig.TILE_SIZE);
                tileView.setLayoutX(c * WorldConfig.TILE_SIZE);
                tileView.setLayoutY(r * WorldConfig.TILE_SIZE);
                this.screenTiles[r][c] = tileView;
                worldPane.getChildren().add(tileView);
            }
        }

        // Khởi tạo ô vuông selector
        this.tileSelector = new Rectangle(WorldConfig.TILE_SIZE, WorldConfig.TILE_SIZE);
        this.tileSelector.setFill(null);                                    // Không tô nền
        this.tileSelector.setStroke(WorldConfig.SELECTOR_COLOR);             // Màu viền
        this.tileSelector.setStrokeWidth(WorldConfig.SELECTOR_STROKE_WIDTH); // Độ dày viền
        this.tileSelector.setVisible(true);                                 // Luôn hiển thị
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
        int startCol = (int) Math.floor(cameraWorldX / WorldConfig.TILE_SIZE);
        int startRow = (int) Math.floor(cameraWorldY / WorldConfig.TILE_SIZE);

        // Tính phần dư (pixel lẻ) để cuộn mượt
        // Đây là mấu chốt: worldPane chỉ di chuyển trong phạm vi 1 ô
        double pixelOffsetX = -(cameraWorldX - (startCol * WorldConfig.TILE_SIZE));
        double pixelOffsetY = -(cameraWorldY - (startRow * WorldConfig.TILE_SIZE));

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
        for (int r = 0; r < WorldConfig.NUM_ROWS_ON_SCREEN; r++) {
            for (int c = 0; c < WorldConfig.NUM_COLS_ON_SCREEN; c++) {
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
        double tileSelectedOnScreenX = tileSelectedX * WorldConfig.TILE_SIZE + worldOffsetX;
        double tileSelectedOnScreenY = tileSelectedY * WorldConfig.TILE_SIZE + worldOffsetY;

        // Hiển thị ô được trỏ chuột
        this.tileSelector.setLayoutX(tileSelectedOnScreenX);
        this.tileSelector.setLayoutY(tileSelectedOnScreenY);
    }
}