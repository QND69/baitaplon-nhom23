package com.example.farmSimulation.view;

import com.example.farmSimulation.config.GameLogicConfig;
import com.example.farmSimulation.config.PlayerSpriteConfig;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
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
            this(row, frameCount, PlayerSpriteConfig.ANIMATION_SPEED);
        }
    }

    // "BỘ NÃO" LƯU TRỮ ANIMATION
    // Cấu trúc: Map<Trạng thái, Map<Hướng, Dữ liệu Anim>>
    private final Map<PlayerState, Map<Direction, AnimData>> animationMap;

    // Map lưu trữ State nào dùng Sheet nào
    private final Map<PlayerState, Image> stateSheetMap;
    private final Image playerSheet;
    private final Image playerActionsSheet;

    // Thêm một Pane bọc ngoài
    private final Pane spriteContainer; // "Khung" base
    private final ImageView sprite;     // "Ảnh" player hoặc player_action

    // Kích thước đã scale (Base size)
    private final double baseWidth;
    private final double baseHeight;

    // --- Node Debug (phải là null-able) ---
    private Rectangle debugBoundingBox; // Hộp debug
    private Circle debugCenterDot;      // Chấm debug
    private Circle debugRangeCircle;    // Vòng tròn Range

    // Trạng thái mặc định
    private PlayerState currentState = PlayerState.IDLE;
    private Direction currentDirection = Direction.DOWN;

    // Trạng thái animation
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private long frameAccumulator = 0; // Tổng thời gian tích lũy

    // --- Hàm khởi tạo ---
    public PlayerView(Image playerSheet, Image playerActionsSheet) {
        this.playerSheet = playerSheet;
        this.playerActionsSheet = playerActionsSheet;

        // Khởi tạo sprite VÀ container
        this.spriteContainer = new Pane();
        this.sprite = new ImageView(playerSheet); // Bắt đầu bằng sheet mặc định
        this.spriteContainer.getChildren().add(this.sprite); // Bỏ sprite vào container

        this.animationMap = new EnumMap<>(PlayerState.class);
        this.stateSheetMap = new EnumMap<>(PlayerState.class);

        // Đọc dữ liệu từ file player.png
        initializeAnimationMap();

        // Cấu hình kích thước
        this.baseWidth = PlayerSpriteConfig.BASE_PLAYER_FRAME_WIDTH * PlayerSpriteConfig.BASE_PLAYER_FRAME_SCALE;
        this.baseHeight = PlayerSpriteConfig.BASE_PLAYER_FRAME_HEIGHT * PlayerSpriteConfig.BASE_PLAYER_FRAME_SCALE;

        // Đặt kích thước cho CONTAINER
        this.spriteContainer.setPrefSize(this.baseWidth, this.baseHeight);

        // Setting cho sprite player
        sprite.setSmooth(false); // Khử răng cưa
        sprite.setPreserveRatio(false); // Giữ tỉ lệ

        this.lastFrameTime = System.nanoTime(); // Dùng nanoTime cho chính xác

        // --- [SỬA] Khởi tạo Debug Nodes (CHỈ KHI DEBUG BẬT) ---
        if (PlayerSpriteConfig.DEBUG_PLAYER_BOUNDS) {
            this.debugBoundingBox = new Rectangle(this.baseWidth, this.baseHeight);
            this.debugBoundingBox.setFill(null);
            this.debugBoundingBox.setStroke(PlayerSpriteConfig.DEBUG_BOUNDING_BOX_COLOR);
            this.debugBoundingBox.setStrokeWidth(1.0);
            this.debugBoundingBox.setMouseTransparent(true); // Không cản click

            this.debugCenterDot = new Circle(2.0); // Bán kính 2px
            this.debugCenterDot.setFill(PlayerSpriteConfig.DEBUG_CENTER_DOT_COLOR);
            this.debugCenterDot.setMouseTransparent(true); // Không cản click

            // [MỚI] Khởi tạo vòng tròn range
            this.debugRangeCircle = new Circle(GameLogicConfig.PLAYER_INTERACTION_RANGE_PIXELS);
            this.debugRangeCircle.setFill(null); // Không tô nền
            this.debugRangeCircle.setStroke(PlayerSpriteConfig.DEBUG_RANGE_COLOR);
            this.debugRangeCircle.setStrokeWidth(1.0);
            this.debugRangeCircle.setMouseTransparent(true);

        } else {
            // Nếu debug TẮT, giữ chúng là null
            this.debugBoundingBox = null;
            this.debugCenterDot = null;
            this.debugRangeCircle = null; // [MỚI]
        }
        // --- Hết phần Debug ---

        updateViewport(); // Cập nhật viewport lần đầu
    }

    /**
     * (QUAN TRỌNG NHẤT) Nơi cung cấp thông tin cho PlayerView về các animation
     * KHI CÓ THÊM HÀNH ĐỘNG, CHỈ CẦN THÊM VÀO HÀM NÀY
     */
    private void initializeAnimationMap() {
        // Liên kết State với Sheet ảnh tương ứng
        stateSheetMap.put(PlayerState.IDLE, playerSheet);
        stateSheetMap.put(PlayerState.WALK, playerSheet);
        stateSheetMap.put(PlayerState.ATTACK, playerSheet);
        stateSheetMap.put(PlayerState.HOE, playerActionsSheet); // Dùng sheet hành động
        stateSheetMap.put(PlayerState.WATER, playerActionsSheet); // Dùng sheet hành động
        stateSheetMap.put(PlayerState.DEAD, playerSheet);

        // IDLE (Đứng yên) - Dùng playerSheet
        Map<Direction, AnimData> idleMap = new EnumMap<>(Direction.class);
        idleMap.put(Direction.DOWN, new AnimData(PlayerSpriteConfig.IDLE_DOWN_ROW, PlayerSpriteConfig.IDLE_FRAMES));
        idleMap.put(Direction.UP, new AnimData(PlayerSpriteConfig.IDLE_UP_ROW, PlayerSpriteConfig.IDLE_FRAMES));
        idleMap.put(Direction.RIGHT, new AnimData(PlayerSpriteConfig.IDLE_RIGHT_ROW, PlayerSpriteConfig.IDLE_FRAMES));
        idleMap.put(Direction.LEFT, new AnimData(PlayerSpriteConfig.IDLE_LEFT_ROW, PlayerSpriteConfig.IDLE_FRAMES));
        animationMap.put(PlayerState.IDLE, idleMap);

        // WALK (Di chuyển) - Dùng playerSheet
        Map<Direction, AnimData> walkMap = new EnumMap<>(Direction.class);
        walkMap.put(Direction.DOWN, new AnimData(PlayerSpriteConfig.WALK_DOWN_ROW, PlayerSpriteConfig.WALK_FRAMES));
        walkMap.put(Direction.UP, new AnimData(PlayerSpriteConfig.WALK_UP_ROW, PlayerSpriteConfig.WALK_FRAMES));
        walkMap.put(Direction.RIGHT, new AnimData(PlayerSpriteConfig.WALK_RIGHT_ROW, PlayerSpriteConfig.WALK_FRAMES));
        walkMap.put(Direction.LEFT, new AnimData(PlayerSpriteConfig.WALK_LEFT_ROW, PlayerSpriteConfig.WALK_FRAMES));
        animationMap.put(PlayerState.WALK, walkMap);

        // ATTACK (Tấn công) - Dùng playerSheet
        Map<Direction, AnimData> attackMap = new EnumMap<>(Direction.class);
        attackMap.put(Direction.DOWN, new AnimData(PlayerSpriteConfig.ATTACK_DOWN_ROW, PlayerSpriteConfig.ATTACK_FRAMES, PlayerSpriteConfig.ATTACK_SPEED));
        attackMap.put(Direction.UP, new AnimData(PlayerSpriteConfig.ATTACK_UP_ROW, PlayerSpriteConfig.ATTACK_FRAMES, PlayerSpriteConfig.ATTACK_SPEED));
        attackMap.put(Direction.RIGHT, new AnimData(PlayerSpriteConfig.ATTACK_RIGHT_ROW, PlayerSpriteConfig.ATTACK_FRAMES, PlayerSpriteConfig.ATTACK_SPEED));
        attackMap.put(Direction.LEFT, new AnimData(PlayerSpriteConfig.ATTACK_LEFT_ROW, PlayerSpriteConfig.ATTACK_FRAMES, PlayerSpriteConfig.ATTACK_SPEED));
        animationMap.put(PlayerState.ATTACK, attackMap);

        // HOE (Cuốc đất) - Dùng playerActionsSheet
        Map<Direction, AnimData> hoeMap = new EnumMap<>(Direction.class);
        hoeMap.put(Direction.DOWN, new AnimData(PlayerSpriteConfig.HOE_DOWN_ROW, PlayerSpriteConfig.HOE_FRAMES, PlayerSpriteConfig.HOE_SPEED));
        hoeMap.put(Direction.UP, new AnimData(PlayerSpriteConfig.HOE_UP_ROW, PlayerSpriteConfig.HOE_FRAMES, PlayerSpriteConfig.HOE_SPEED));
        hoeMap.put(Direction.RIGHT, new AnimData(PlayerSpriteConfig.HOE_RIGHT_ROW, PlayerSpriteConfig.HOE_FRAMES, PlayerSpriteConfig.HOE_SPEED));
        hoeMap.put(Direction.LEFT, new AnimData(PlayerSpriteConfig.HOE_LEFT_ROW, PlayerSpriteConfig.HOE_FRAMES, PlayerSpriteConfig.HOE_SPEED));
        animationMap.put(PlayerState.HOE, hoeMap);

        // WATER (Tưới nước) - Dùng playerActionsSheet
        Map<Direction, AnimData> waterMap = new EnumMap<>(Direction.class);
        waterMap.put(Direction.DOWN, new AnimData(PlayerSpriteConfig.WATER_DOWN_ROW, PlayerSpriteConfig.WATER_FRAMES, PlayerSpriteConfig.WATER_SPEED));
        waterMap.put(Direction.UP, new AnimData(PlayerSpriteConfig.WATER_UP_ROW, PlayerSpriteConfig.WATER_FRAMES, PlayerSpriteConfig.WATER_SPEED));
        waterMap.put(Direction.RIGHT, new AnimData(PlayerSpriteConfig.WATER_RIGHT_ROW, PlayerSpriteConfig.WATER_FRAMES, PlayerSpriteConfig.WATER_SPEED));
        waterMap.put(Direction.LEFT, new AnimData(PlayerSpriteConfig.WATER_LEFT_ROW, PlayerSpriteConfig.WATER_FRAMES, PlayerSpriteConfig.WATER_SPEED));
        animationMap.put(PlayerState.WATER, waterMap);

//        // DEAD (Ngất)
//        Map<Direction, AnimData> deadMap = new EnumMap<>(Direction.class);
//        deadMap.put(Direction.DOWN, new AnimData(GameConfig.DEAD_DOWN_ROW, GameConfig.DEAD_FRAMES, GameConfig.DEAD_SPEED));
//        deadMap.put(Direction.UP, new AnimData(GameConfig.DEAD_UP_ROW, GameConfig.DEAD_FRAMES, GameConfig.DEAD_SPEED));
//        deadMap.put(Direction.RIGHT, new AnimData(GameConfig.DEAD_RIGHT_ROW, GameConfig.DEAD_FRAMES, GameConfig.DEAD_SPEED));
//        deadMap.put(Direction.LEFT, new AnimData(GameConfig.DEAD_LEFT_ROW, GameConfig.DEAD_FRAMES, GameConfig.DEAD_SPEED));
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
            // Xử lý animation "one-shot" (HOE, WATER, ATTACK)
            // Chúng không lặp lại (loop)
            boolean isOneShot = (currentState == PlayerState.ATTACK ||
                    currentState == PlayerState.HOE ||
                    currentState == PlayerState.WATER);

            int nextFrame = currentFrame + 1; // Tăng frame

            if (isOneShot) { // Animation ko lặp
                if (nextFrame >= data.frameCount()) {
                    // Đến frame cuối
                    currentFrame = 0; // Về frame đầu
                    frameAccumulator = 0; // Dừng chạy
                } else {
                    currentFrame = nextFrame; // Chuyển frame
                    frameAccumulator -= data.speed();
                }
            } else { // Animation lặp lại (IDLE, WALK)
                currentFrame = nextFrame % data.frameCount(); // Modulo để quay vòng
                frameAccumulator -= data.speed();
            }

            frameChanged = true;
        }

        // Chỉ cập nhật viewport NẾU frame thay đổi
        if (frameChanged) {
            updateViewport();
        }
    }

    /**
     * Hàm này được GameManager "ra lệnh"
     *
     * @param newState     Trạng thái mới (IDLE, WALK,...)
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

        // Đảm bảo ImageView đang dùng đúng Sheet ảnh
        Image requiredSheet = stateSheetMap.getOrDefault(currentState, playerSheet);
        if (sprite.getImage() != requiredSheet) {
            sprite.setImage(requiredSheet);
        }

        double frameWidth;
        double frameHeight;

        if (requiredSheet == playerSheet) { // Đây là sheet player
            frameWidth = PlayerSpriteConfig.PLAYER_FRAME_WIDTH;
            frameHeight = PlayerSpriteConfig.PLAYER_FRAME_HEIGHT;

            sprite.setTranslateX(0.0);
            sprite.setTranslateY(0.0);

            // Áp dụng hằng số bù trừ
            sprite.setTranslateX(PlayerSpriteConfig.PLAYER_SPRITE_OFFSET_X);
            sprite.setTranslateY(PlayerSpriteConfig.PLAYER_SPRITE_OFFSET_Y);

            sprite.setScaleY(1.0); // Reset scale

        } else {
            frameWidth = PlayerSpriteConfig.ACTION_FRAME_WIDTH;
            frameHeight = PlayerSpriteConfig.ACTION_FRAME_HEIGHT;

            sprite.setTranslateX(0.0);
            sprite.setTranslateY(0.0);

            sprite.setScaleY(1.0); // Reset scale
        }

        // Xác định lật ảnh (flip)
        int flipFactor = (currentDirection == Direction.LEFT) ? -1 : 1;
        sprite.setScaleX(flipFactor);

        // Xác định vùng cắt
        double viewportX = currentFrame * frameWidth;
        double viewportY = data.row() * frameHeight;

        // Tính toán và set Viewport
        sprite.setViewport(new Rectangle2D(viewportX, viewportY,
                frameWidth,
                frameHeight));
    }
}