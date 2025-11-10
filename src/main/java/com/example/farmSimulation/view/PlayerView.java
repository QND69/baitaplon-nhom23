package com.example.farmSimulation.view;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Getter;
import lombok.Setter;

import java.io.InputStream; // Import thêm để kiểm tra lỗi

@Getter
@Setter
public class PlayerView {
    // Tên file
    private String idlePath = "/assets/images/entities/player/idle.png";
    private String walkPath = "/assets/images/entities/player/walk.png";

    // Kích thước của MỘT frame (pixel)
    private final double FRAME_WIDTH = 32;
    private final double FRAME_HEIGHT = 32;

    // Số lượng frame (cột) trong spritesheet
    private final int IDLE_FRAME_COUNT = 4; // Idle.png có 4 frame
    private final int WALK_FRAME_COUNT = 6; // Walk.png có 6 frame

    private final double scale = 4.0; // Tỉ lệ phóng to

    private final long ANIMATION_SPEED = 120; // Thời gian 1 frame (ms), càng ít càng nhanh

    private Image idleSheet;
    private Image walkSheet;

    // *** [TỐI ƯU] Không cần 'walkSheetLeft' và 'flipImageHorizontally' nữa ***

    private ImageView sprite;

    // Kích thước đã scale (để MainGameView căn giữa)
    private double width;
    private double height;

    // Trạng thái animation
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private long frameAccumulator = 0; // Tổng thời gian tích lũy
    private boolean isMoving = false;

    // Enum để quản lý hướng
    private enum Direction {
        DOWN(0), // Hàng 0
        UP(1),   // Hàng 1
        LEFT(2), // Hàng 2 (dùng cho cả trái/phải)
        RIGHT(2); // Hàng 2

        private final int row;
        Direction(int row) {
            this.row = row;
        }
        public int getRow() {
            return row;
        }
    }
    private Direction currentDirection = Direction.DOWN; // Mặc định là xuống

    public PlayerView() {
        // Tải 2 ảnh spritesheet
        idleSheet = new Image(getClass().getResourceAsStream(idlePath));
        walkSheet = new Image(getClass().getResourceAsStream(walkPath));

        // Khởi tạo ImageView với ảnh mặc định là Idle
        this.sprite = new ImageView(idleSheet);

        // Tính toán kích thước hiển thị (đã scale)
        this.width = FRAME_WIDTH * scale;
        this.height = FRAME_HEIGHT * scale;
        sprite.setFitWidth(this.width);
        sprite.setFitHeight(this.height);
        sprite.setPreserveRatio(true); // Giữ tỉ lệ

        // Đặt viewport ban đầu (frame đầu tiên, hướng DOWN)
        sprite.setViewport(new Rectangle2D(0, 0, FRAME_WIDTH, FRAME_HEIGHT));

        this.lastFrameTime = System.nanoTime(); // Dùng nanoTime cho chính xác
    }
    /**
     * Hàm này sẽ được gọi 60 lần/giây bởi GameManager.
     */
    public void update(double dx, double dy) {
        updateMovement(dx, dy);      // Xác định di chuyển và hướng
        updateAnimation();           // Cập nhật frame animation dựa trên thời gian
        updateViewport();            // Cập nhật sprite và viewport
    }

    // --- Các hàm con để tách logic ---

    private void updateMovement(double dx, double dy) {
        boolean wasMoving = this.isMoving;
        this.isMoving = (dx != 0 || dy != 0);

        // Xác định hướng dựa trên delta (logic input của bạn)
        if (dy > 0) {
            this.currentDirection = Direction.UP;
        } else if (dy < 0) {
            this.currentDirection = Direction.DOWN;
        } else if (dx > 0) {
            this.currentDirection = Direction.LEFT;
        } else if (dx < 0) {
            this.currentDirection = Direction.RIGHT;
        }

        // Nếu trạng thái di chuyển thay đổi (đi -> dừng hoặc ngược lại)
        if (this.isMoving != wasMoving) {
            currentFrame = 0;
            frameAccumulator = 0;
            lastFrameTime = System.nanoTime();
        }
    }

    private void updateAnimation() {
        long now = System.nanoTime();
        long deltaMs = (now - lastFrameTime) / 1_000_000;
        lastFrameTime = now;
        frameAccumulator += deltaMs;

        int maxFrame = (isMoving) ? WALK_FRAME_COUNT : IDLE_FRAME_COUNT;

        // Bù frame nếu thời gian tích lũy > ANIMATION_SPEED
        while (frameAccumulator > ANIMATION_SPEED) {
            currentFrame = (currentFrame + 1) % maxFrame;
            frameAccumulator -= ANIMATION_SPEED;
        }
    }

    private void updateViewport() {
        // Chọn sheet dựa trên trạng thái (Idle / Walk)
        Image newSheet = isMoving ? walkSheet : idleSheet;

        // Chỉ set image nếu sheet thay đổi
        if (sprite.getImage() != newSheet) {
            sprite.setImage(newSheet);
        }

        // Xử lý lật ảnh (flip)
        if (currentDirection == Direction.LEFT) {
            sprite.setScaleX(-1); // Lật ảnh (hướng Phải -> Trái)
        } else {
            sprite.setScaleX(1); // Giữ nguyên (cho Phải, Lên, Xuống)
        }
        // Vì setScaleX áp dụng cho ImageView, nó sẽ lật BẤT KỲ ảnh nào

        // Tính toán và set Viewport
        double viewportX = currentFrame * FRAME_WIDTH;
        double viewportY = currentDirection.getRow() * FRAME_HEIGHT;
        sprite.setViewport(new Rectangle2D(viewportX, viewportY, FRAME_WIDTH, FRAME_HEIGHT));
    }
}