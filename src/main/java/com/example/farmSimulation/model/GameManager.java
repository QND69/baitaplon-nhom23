package com.example.farmSimulation.model;

import com.example.farmSimulation.controller.GameController;
import com.example.farmSimulation.view.MainGameView;
import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Getter
@Setter
// Class quản lý logic game
public class GameManager {
    private Player mainPlayer;
    private GameController gameController;
    private MainGameView mainGameView;

    private Pane worldPane;          // lấy từ MainGameView
    private WorldMap worldMap;       // lấy từ MainGameView
    private AnimationTimer gameLoop; // Khởi tạo gameLoop
    private double playerSpeed = 5.0;  // Tốc độ di chuyển (pixel mỗi frame)

    private double worldOffsetX = 0.0; // Tọa độ thế giới
    private double worldOffsetY = 0.0; // Tọa độ thế giới

    private boolean mapNeedsUpdate = false;

    private List<TimedTileAction> pendingActions; // Thêm danh sách hành động chờ

    public GameManager(Player player, GameController gameController, MainGameView mainGameView) {
        this.mainPlayer = player;
        this.gameController = gameController;
        this.mainGameView = mainGameView;
        this.pendingActions = new ArrayList<>(); // Khởi tạo danh sách hành động chờ
    }

    public void startGame() {
        // Lấy worldPane và worldMap từ View (gọi sau initUI)
        this.worldPane = mainGameView.getWorldPane();
        this.worldMap = mainGameView.getWorldMap();

        // *** Đặt vị trí khởi đầu của worldPane (camera) ***
        // Sao cho người chơi (ở giữa màn hình) nhìn vào tọa độ logic (tileX, tileY) của player
        // Vị trí worldPane = -Tọa độ logic player + (Nửa màn hình) - (Độ dài nhân vật)
        this.worldOffsetX = -mainPlayer.getTileX() + mainGameView.getSCREEN_WIDTH() / 2 - mainGameView.getPlayerView().getWidth() / 2;
        this.worldOffsetY = -mainPlayer.getTileY() + mainGameView.getSCREEN_HEIGHT() / 2 - mainGameView.getPlayerView().getHeight() / 2;

        // Gọi updateMap để vẽ map lần đầu
        mainGameView.updateMap(this.worldOffsetX, this.worldOffsetY);

        // Bắt đầu loop game
        this.gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) { // Hàm handle() này sẽ được gọi 60 lần mỗi giây
                updateGameLogic();
            }
        };
        gameLoop.start(); // Bắt đầu gọi hàm handle() 60 lần/giây
        System.out.println("Game Started!");
    }

    private void updateGameLogic() {
        // Tính toán hướng di chuyển (delta X, delta Y)
        double dx = 0;
        double dy = 0;

        // "Hỏi" GameController xem phím nào đang được nhấn
        if (gameController.isKeyPressed(KeyCode.W)) { // Di chuyển PLAYER đi LÊN
            dy += playerSpeed; // Di chuyển WORLD đi XUỐNG
        }
        if (gameController.isKeyPressed(KeyCode.S)) { // Di chuyển PLAYER đi XUỐNG
            dy -= playerSpeed; // Di chuyển WORLD đi LÊN
        }
        if (gameController.isKeyPressed(KeyCode.A)) { // Di chuyển PLAYER đi TRÁI
            dx += playerSpeed; // Di chuyển WORLD đi SANG PHẢI
        }
        if (gameController.isKeyPressed(KeyCode.D)) { // Di chuyển PLAYER đi PHẢI
            dx -= playerSpeed; // Di chuyển WORLD đi SANG TRÁI
        }

        // *** GỌI HÀM CẬP NHẬT HÀNG ĐỢI ***
        // (Luôn chạy 60 lần/giây)
        //updateTimedActions();

        // Cập nhật Map nếu di chuyển
        if (dx != 0 || dy != 0) {
            //worldPane.setLayoutX(worldPane.getLayoutX() + dx);
            //worldPane.setLayoutY(worldPane.getLayoutY() + dy);

            this.worldOffsetX += dx;
            this.worldOffsetY += dy;

            // Cập nhật tọa độ logic của Player
            mainPlayer.setTileX(mainPlayer.getTileX() - dx);
            mainPlayer.setTileY(mainPlayer.getTileY() - dy);

            // *** YÊU CẦU VIEW VẼ LẠI MAP DỰA TRÊN VỊ TRÍ MỚI ***
            // Truyền vào vị trí offset (dịch chuyển) của worldPane
            mainGameView.updateMap(this.worldOffsetX, this.worldOffsetY);
        }

        // Nếu không di chuyển, NHƯNG map cần update (do cuốc đất)
        /*else if (this.mapNeedsUpdate) {
            mainGameView.updateMap(worldPane.getLayoutX(), worldPane.getLayoutY());

            // Đặt lại cờ
            this.mapNeedsUpdate = false;
        }*/
        // Selector LUÔN chạy để theo chuột mượt mà
        mainGameView.updateSelector(
                gameController.getMouseX(),       // Lấy từ Controller
                gameController.getMouseY(),       // Lấy từ Controller
                this.worldOffsetX,           // Vị trí X của thế giới
                this.worldOffsetY           // Vị trí Y của thế giới
        );
    }

    /**
     * Xử lý logic khi người chơi TƯƠNG TÁC (CLICK) với một ô.
     * Nhiệm vụ của hàm này giờ là "Quyết định" và "Thêm hành động vào hàng đợi".
     */
    public void interactWithTile(int col, int row) {
        Tile currentType = worldMap.getTileType(col, row);

        // --- ÁP DỤNG "LUẬT CHƠI" ---

        // VÍ DỤ 1: Cuốc đất (Grass -> Soil)
        if (currentType == Tile.GRASS) {
            // Đặt độ trễ là 1 frame (hoặc 0 nếu muốn tức thì)
            int delayInFrames = 1;

            // Thêm hành động "Biến thành Đất" vào hàng đợi
            pendingActions.add(new TimedTileAction(col, row, Tile.SOIL, delayInFrames));

            System.out.println("Đã lên lịch cuốc ô (" + col + "," + row + ") thành Đất.");
        }

        // VÍ DỤ 2: Tưới nước (Soil -> Watered_Soil)
        // (Giả sử bạn đã thêm Tile.WATERED_SOIL vào Enum)
        /*
        else if (currentType == Tile.SOIL) {
            // Đặt độ trễ là 10 frames (khoảng 1/6 giây)
            int delayInFrames = 10;

            // Thêm hành động "Biến thành Đất Ẩm" vào hàng đợi
            pendingActions.add(new TimedTileAction(col, row, Tile.WATERED_SOIL, delayInFrames));

            System.out.println("Đã lên lịch tưới nước ô (" + col + "," + row + ")");
        }
        */
    }

    /**
     * Hàm này được gọi 60 LẦN/GIÂY.
     * Nhiệm vụ: Lặp qua tất cả hành động chờ, "tick" chúng,
     * và thực thi những hành động đã hết giờ.
     */
    private void updateTimedActions() {
        // Dùng Iterator để chúng ta có thể XÓA phần tử khỏi List một cách an toàn
        Iterator<TimedTileAction> iterator = pendingActions.iterator();

        while (iterator.hasNext()) {
            TimedTileAction action = iterator.next();

            // Gọi tick(). Nếu nó trả về "true" (hết giờ)
            if (action.tick()) {
                // 1. THỰC THI HÀNH ĐỘNG: Thay đổi Model
                worldMap.setTileType(action.getCol(), action.getRow(), action.getNewType());

                // 2. Báo cho View biết cần vẽ lại bản đồ
                this.mapNeedsUpdate = true;

                // 3. Xóa hành động này khỏi hàng đợi
                iterator.remove();

                // System.out.println("Hoàn thành: " + action.getNewType() + " tại " + action.getCol());
            }
        }
    }
}