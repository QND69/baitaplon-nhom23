package com.example.farmSimulation.view;

import com.example.farmSimulation.config.AssetPaths;
import com.example.farmSimulation.config.GameConfig;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Getter;

import java.util.EnumMap;
import java.util.Map;

@Getter
public class PlayerView {

    // --- Định nghĩa trạng thái & hướng ---
    // Public để GameManager có thể "ra lệnh"
    public enum PlayerState {
        IDLE,
        WALK,
        ATTACK,
        HOE,
        WATER,
        DEAD
    }

    // Enum để quản lý hướng
    public enum Direction {
        DOWN,
        UP,
        RIGHT,
        LEFT
    }

    // --- Cấu trúc dữ liệu Animation ---
    // (private record) là 1 class siêu gọn để chứa dữ liệu
    private record AnimData(int row, int frameCount, long speed) {
        // Constructor phụ nếu muốn dùng tốc độ mặc định
        AnimData(int row, int frameCount) {
            this(row, frameCount, GameConfig.ANIMATION_SPEED);
        }
    }

    // "BỘ NÃO" LƯU TRỮ ANIMATION
    // Cấu trúc: Map<Trạng thái, Map<Hướng, Dữ liệu Anim>>
    private final Map<PlayerState, Map<Direction, AnimData>> animationMap;

    // --- Biến thành viên ---
    private final ImageView sprite;

    // Kích thước đã scale
    private final double width;
    private final double height;

    // Trạng thái mặc định
    private PlayerState currentState = PlayerState.IDLE;
    private Direction currentDirection = Direction.DOWN;

    // Trạng thái animation
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private long frameAccumulator = 0; // Tổng thời gian tích lũy

    // --- Hàm khởi tạo ---
    public PlayerView(Image playerSheet) {
        this.sprite = new ImageView(playerSheet);
        this.animationMap = new EnumMap<>(PlayerState.class);

        // Đọc dữ liệu từ file player.png
        initializeAnimationMap();

        // Cấu hình kích thước
        this.width = GameConfig.PLAYER_FRAME_WIDTH * GameConfig.PLAYER_SPRITE_SCALE;
        this.height = GameConfig.PLAYER_FRAME_HEIGHT * GameConfig.PLAYER_SPRITE_SCALE;

        sprite.setFitWidth(this.width);
        sprite.setFitHeight(this.height);
        sprite.setSmooth(false); // Khử răng cưa
        sprite.setPreserveRatio(true); // Giữ tỉ lệ

        this.lastFrameTime = System.nanoTime(); // Dùng nanoTime cho chính xác
        updateViewport(); // Cập nhật viewport lần đầu
    }

    /**
     * (QUAN TRỌNG NHẤT) Nơi cung cấp thông tin cho PlayerView về các animation
     * KHI CÓ THÊM HÀNH ĐỘNG, CHỈ CẦN THÊM VÀO HÀM NÀY
     */
    private void initializeAnimationMap() {
        // IDLE (Đứng yên)
        Map<Direction, AnimData> idleMap = new EnumMap<>(Direction.class);
        idleMap.put(Direction.DOWN,  new AnimData(0, 6));
        idleMap.put(Direction.UP,    new AnimData(2, 6));
        idleMap.put(Direction.RIGHT, new AnimData(1, 6));
        idleMap.put(Direction.LEFT,  new AnimData(1, 6));
        animationMap.put(PlayerState.IDLE, idleMap);

        // WALK (Di chuyển)
        Map<Direction, AnimData> walkMap = new EnumMap<>(Direction.class);
        walkMap.put(Direction.DOWN,  new AnimData(3, 6));
        walkMap.put(Direction.UP,    new AnimData(5, 6));
        walkMap.put(Direction.RIGHT, new AnimData(4, 6));
        walkMap.put(Direction.LEFT,  new AnimData(4, 6));
        animationMap.put(PlayerState.WALK, walkMap);

        // ATTACK (Tấn công)
        Map<Direction, AnimData> attackMap = new EnumMap<>(Direction.class);
        attackMap.put(Direction.DOWN,  new AnimData(6, 4, 100)); // Tốc độ nhanh hơn
        attackMap.put(Direction.UP,    new AnimData(8, 4, 100));
        attackMap.put(Direction.RIGHT, new AnimData(7, 4, 100));
        attackMap.put(Direction.LEFT,  new AnimData(7, 4, 100));
        animationMap.put(PlayerState.ATTACK, attackMap);


//        // HOE (Cuốc đất)
//        Map<Direction, AnimData> hoeMap = new EnumMap<>(Direction.class);
//        hoeMap.put(Direction.DOWN,  new AnimData(12, 3, 150));
//        hoeMap.put(Direction.UP,    new AnimData(13, 3, 150));
//        hoeMap.put(Direction.RIGHT, new AnimData(14, 3, 150));
//        hoeMap.put(Direction.LEFT,  new AnimData(15, 3, 150));
//        animationMap.put(PlayerState.HOE, hoeMap);
//
//        // DEAD (Ngất)
//        Map<Direction, AnimData> deadMap = new EnumMap<>(Direction.class);
//        deadMap.put(Direction.DOWN,  new AnimData(16, 2, 200));
//        deadMap.put(Direction.UP,    new AnimData(16, 2, 200)); // Dùng chung 1 hàng
//        deadMap.put(Direction.RIGHT, new AnimData(16, 2, 200));
//        deadMap.put(Direction.LEFT,  new AnimData(16, 2, 200));
//        animationMap.put(PlayerState.DEAD, deadMap);
    }

    /**
     * Hàm được gọi 60 lần/giây từ GameManager
     * Nhiệm vụ: CHỈ tính toán frame tiếp theo
     */
    public void updateAnimation() {
        // Lấy dữ liệu (row, frameCount, speed) từ "bộ não"
        AnimData data = getAnimationData();
        if (data == null) return;

        long now = System.nanoTime();
        long deltaMs = (now - lastFrameTime) / 1_000_000;
        lastFrameTime = now;
        frameAccumulator += deltaMs;

        // Bù frame nếu lag
        boolean frameChanged = false;
        while (frameAccumulator > data.speed()) {
            currentFrame = (currentFrame + 1) % data.frameCount(); // Tăng frame, modulo để quay vòng
            frameAccumulator -= data.speed();
            frameChanged = true;
        }

        // Chỉ cập nhật viewport NẾU frame thay đổi
        if (frameChanged) {
            updateViewport();
        }
    }

    /**
     * Hàm này được GameManager "ra lệnh"
     * @param newState Trạng thái mới (IDLE, WALK,...)
     * @param newDirection Hướng mới (UP, DOWN,...)
     */
    public void setState(PlayerState newState, Direction newDirection) {
        boolean stateChanged = (this.currentState != newState);
        boolean directionChanged = (this.currentDirection != newDirection);

        this.currentState = newState;
        this.currentDirection = newDirection;

        // Reset animation nếu trạng thái hoặc hướng thay đổi
        if (stateChanged || directionChanged) {
            currentFrame = 0;
            frameAccumulator = 0;
            lastFrameTime = System.nanoTime();
            updateViewport(); // Cập nhật hình ảnh ngay lập tức
        }
    }

    /**
     * Hàm nội bộ: Lấy data từ "bộ não"
     */
    private AnimData getAnimationData() {
        Map<Direction, AnimData> stateMap = animationMap.get(currentState);
        if (stateMap == null) return null; // Trạng thái chưa được định nghĩa

        AnimData data = stateMap.get(currentDirection);
        if (data == null) return null; // Hướng chưa được định nghĩa

        return data;
    }

    /**
     * Hàm nội bộ: Cập nhật hình ảnh (setViewport)
     */
    private void updateViewport() {
        AnimData data = getAnimationData();
        if (data == null) return; // An toàn

        double viewportX = currentFrame * GameConfig.PLAYER_FRAME_WIDTH;
        double viewportY = data.row() * GameConfig.PLAYER_FRAME_HEIGHT;

        // Xử lý lật ảnh (flip)
        if (currentDirection == Direction.LEFT) {
            sprite.setScaleX(-1); // Lật ảnh (hướng Phải -> Trái)
        } else {
            sprite.setScaleX(1); // Giữ nguyên (cho Phải, Lên, Xuống)
        }
        // Vì setScaleX áp dụng cho ImageView, nó sẽ lật BẤT KỲ ảnh nào

        // Tính toán và set Viewport
        sprite.setViewport(new Rectangle2D(viewportX, viewportY,
                GameConfig.PLAYER_FRAME_WIDTH,
                GameConfig.PLAYER_FRAME_HEIGHT));
    }
}