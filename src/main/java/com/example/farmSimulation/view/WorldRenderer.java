package com.example.farmSimulation.view;

import com.example.farmSimulation.config.CropConfig;
import com.example.farmSimulation.config.HudConfig;
import com.example.farmSimulation.config.WorldConfig;
import com.example.farmSimulation.model.Tile;
import com.example.farmSimulation.model.TileData;
import com.example.farmSimulation.model.WorldMap;
import com.example.farmSimulation.view.assets.AssetManager;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import lombok.Getter;

@Getter
public class WorldRenderer {
    // Mảng 2D LƯU TRỮ các ImageView
    private final ImageView[][] baseTiles; // Lớp 1: Các ô hiển thị trên màn hình
    private final ImageView[][] overlayTiles; // Lớp 2: Phân bón
    private final ImageView[][] cropTiles;    // Lớp 3: Cây trồng
    private final ImageView[][] statusIconTiles; // Lớp 4: Icon báo hiệu
    private final ImageView[][] statusBackground; // Lớp 5: Mảng chứa nền mờ

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

        // Khởi tạo tất cả các lớp
        this.baseTiles = new ImageView[WorldConfig.NUM_ROWS_ON_SCREEN][WorldConfig.NUM_COLS_ON_SCREEN];
        this.overlayTiles = new ImageView[WorldConfig.NUM_ROWS_ON_SCREEN][WorldConfig.NUM_COLS_ON_SCREEN];
        this.cropTiles = new ImageView[WorldConfig.NUM_ROWS_ON_SCREEN][WorldConfig.NUM_COLS_ON_SCREEN];
        this.statusIconTiles = new ImageView[WorldConfig.NUM_ROWS_ON_SCREEN][WorldConfig.NUM_COLS_ON_SCREEN];
        this.statusBackground = new ImageView[WorldConfig.NUM_ROWS_ON_SCREEN][WorldConfig.NUM_COLS_ON_SCREEN];

        this.worldPane = new Pane();

        // Chèn các tile rỗng vào worldPane
        for (int r = 0; r < WorldConfig.NUM_ROWS_ON_SCREEN; r++) {
            for (int c = 0; c < WorldConfig.NUM_COLS_ON_SCREEN; c++) {
                // Gọi hàm helper để tạo ImageView
                baseTiles[r][c] = createTileView(c, r, 0, WorldConfig.TILE_SIZE, WorldConfig.TILE_SIZE);
                overlayTiles[r][c] = createTileView(c, r, 0, WorldConfig.TILE_SIZE, WorldConfig.TILE_SIZE);
                cropTiles[r][c] = createTileView(c, r, -CropConfig.CROP_Y_OFFSET, CropConfig.CROP_SPRITE_WIDTH, CropConfig.CROP_SPRITE_HEIGHT);

                // Status Background
                ImageView bg = createTileView(c, r, -HudConfig.ICON_Y_OFFSET, HudConfig.ICON_BG_SIZE, HudConfig.ICON_BG_SIZE);
                bg.setImage(assetManager.getIconBG());
                bg.setVisible(false);

                // Căn BG góc trái trên khớp với ô tile
                bg.setLayoutX(c * WorldConfig.TILE_SIZE);

                statusBackground[r][c] = bg;

                // Status Icon
                ImageView icon = createTileView(c, r, -HudConfig.ICON_Y_OFFSET, HudConfig.ICON_SIZE, HudConfig.ICON_SIZE);

                // Căn giữa Icon so với BG
                double iconOffset = (HudConfig.ICON_BG_SIZE - HudConfig.ICON_SIZE) / 2;
                icon.setLayoutX(c * WorldConfig.TILE_SIZE + iconOffset);

                // Offset Y: Dịch thêm HudConfig.ICON_PADDING_TOP để lọt vào bong bóng
                icon.setLayoutY(r * WorldConfig.TILE_SIZE - HudConfig.ICON_Y_OFFSET + HudConfig.ICON_PADDING_TOP);

                statusIconTiles[r][c] = icon;

                // Thêm vào worldPane theo ĐÚNG THỨ TỰ RENDER
                // Lớp dưới cùng được thêm vào trước
                worldPane.getChildren().addAll(
                        baseTiles[r][c],
                        overlayTiles[r][c],
                        cropTiles[r][c],
                        statusBackground[r][c], // Nền
                        statusIconTiles[r][c]    // Icon
                );
            }
        }

        // Khởi tạo ô vuông selector
        this.tileSelector = new Rectangle(WorldConfig.TILE_SIZE, WorldConfig.TILE_SIZE);
        this.tileSelector.setFill(null);                                    // Không tô nền
        this.tileSelector.setStroke(WorldConfig.SELECTOR_COLOR);             // Màu viền
        this.tileSelector.setStrokeWidth(WorldConfig.SELECTOR_STROKE_WIDTH); // Độ dày viền
        this.tileSelector.setVisible(true);                                 // Luôn hiển thị
    }

    /**
     * Hàm helper để tạo một ImageView cho một lớp
     */
    private ImageView createTileView(int c, int r, double yOffset, double width, double height) {
        ImageView tileView = new ImageView();
        tileView.setFitWidth(width);   // Set chiều rộng
        tileView.setFitHeight(height); // Set chiều cao
        tileView.setLayoutX(c * WorldConfig.TILE_SIZE);
        tileView.setLayoutY(r * WorldConfig.TILE_SIZE + yOffset);
        tileView.setSmooth(false);
        return tileView;
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

        // CẬP NHẬT HÌNH ẢNH (TEXTURE) cho TẤT CẢ CÁC LỚP
        for (int r = 0; r < WorldConfig.NUM_ROWS_ON_SCREEN; r++) {
            for (int c = 0; c < WorldConfig.NUM_COLS_ON_SCREEN; c++) {
                // Tính ô logic (thế giới) mà ô lưới (màn hình) này cần hiển thị
                int logicalCol = startCol + c;
                int logicalRow = startRow + r;

                // Lấy TOÀN BỘ TileData (Model)
                TileData data = worldMap.getTileData(logicalCol, logicalRow);

                // --- Lớp 1: Đất (Base) ---
                // Lấy loại tile từ Model
                Tile type = data.getBaseTileType();
                // Lấy ảnh từ AssetManager
                Image baseTexture = assetManager.getTileTexture(type);
                // Chỉ THAY ẢNH, không tạo mới
                this.baseTiles[r][c].setImage(baseTexture);

                // --- Lớp 2: Phân bón (Overlay) ---
                Image overlayTexture = data.isFertilized() ? assetManager.getFertilizerTexture() : null;
                this.overlayTiles[r][c].setImage(overlayTexture);

                // --- Lớp 3: Cây trồng (Crop) ---
                Image cropTexture = assetManager.getCropTexture(data.getCropData());
                this.cropTiles[r][c].setImage(cropTexture);

                // --- Lớp 3: Cây trồng (Crop) ---
                // Cập nhật Icon & Background
                Image statusIcon = assetManager.getStatusIcon(data.getStatusIndicator());

                // --- Lớp 4 & 5: Icon và Background ---
                // Xử lý icon kép (Size icon thay đổi, nhưng BG giữ nguyên)
                if (data.getStatusIndicator() == com.example.farmSimulation.model.CropStatusIndicator.NEED_WATER_AND_FERTILIZER) {
                    // Icon kép rộng gấp đôi
                    double doubleWidth = HudConfig.ICON_SIZE * 2;
                    this.statusIconTiles[r][c].setFitWidth(doubleWidth);

                    // Căn giữa lại
                    double iconOffset = (HudConfig.ICON_BG_SIZE - doubleWidth) / 2;
                    this.statusIconTiles[r][c].setLayoutX((startCol + c - startCol) * WorldConfig.TILE_SIZE + iconOffset);

                    // Background: GIỮ NGUYÊN, không resize
                } else {
                    // Reset về icon đơn
                    this.statusIconTiles[r][c].setFitWidth(HudConfig.ICON_SIZE);
                    double iconOffset = (HudConfig.ICON_BG_SIZE - HudConfig.ICON_SIZE) / 2;
                    this.statusIconTiles[r][c].setLayoutX((startCol + c - startCol) * WorldConfig.TILE_SIZE + iconOffset);
                }

                this.statusIconTiles[r][c].setImage(statusIcon);

                // Hiện BG nếu có icon
                this.statusBackground[r][c].setVisible(statusIcon != null);

                // Reset vị trí Background về mặc định (căn trái) để đảm bảo không bị lệch do logic cũ
                this.statusBackground[r][c].setLayoutX((startCol + c - startCol) * WorldConfig.TILE_SIZE);
                this.statusBackground[r][c].setFitWidth(HudConfig.ICON_BG_SIZE); // Đảm bảo luôn là 64
            }
        }
        // Ghi nhớ vị trí render lần cối
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