package com.example.farmSimulation.model;

import com.example.farmSimulation.config.GameLogicConfig;
import com.example.farmSimulation.config.PlayerSpriteConfig;
import com.example.farmSimulation.view.PlayerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý logic tương tác dựa trên quy tắc (rule-based).
 * Lớp này sẽ thay thế cho logic if-else phức tạp trong GameManager.
 */
public class InteractionManager {

    private final List<InteractionRule> rules;
    private final ActionManager actionManager; // Để thêm hành động chờ

    public InteractionManager(ActionManager actionManager) {
        this.actionManager = actionManager;
        this.rules = new ArrayList<>();
        initializeRules();
    }

    /**
     * Nơi định nghĩa tất cả "Luật chơi" của game.
     * Rất dễ dàng thêm/xóa/sửa luật mới tại đây.
     */
    private void initializeRules() {
        // --- Định nghĩa các luật chơi ---

        // Cuốc đất (Dùng CUỐC trên CỎ)
        rules.add(new InteractionRule(
                Tool.HOE,
                Tile.GRASS,
                Tile.SOIL, // Biến thành ĐẤT
                PlayerView.PlayerState.HOE,
                (long) GameLogicConfig.HOE_REPETITIONS * GameLogicConfig.HOE_DURATION_PER_REPETITION_MS
        ));

        // Tưới nước (Dùng BÌNH TƯỚI trên ĐẤT)
        // TODO: Cần thêm Tile.SOIL_WET vào enum Tile
        rules.add(new InteractionRule(
                Tool.WATERING_CAN,
                Tile.SOIL,
                Tile.WATER, // (đổi thành SOIL_WET nếu có)
                PlayerView.PlayerState.WATER,
                (long) GameLogicConfig.WATERING_CAN_REPETITIONS * GameLogicConfig.WATERING_CAN_DURATION_PER_REPETITION_MS
        ));

        // 3. (Ví dụ) Dùng CUỐC CHIM trên CỎ (sẽ fail, vì cuốc chim ko dùng đc trên cỏ)
        /*
        rules.add(new InteractionRule(
                Tool.PICKAXE,
                Tile.GRASS,
                null, // Không đổi tile
                PlayerView.PlayerState.ATTACK, // Tạm dùng anim attack
                (long) GameLogicConfig.PICKAXE_REPETITIONS * GameLogicConfig.PICKAXE_DURATION_PER_REPETITION_MS
        ));
        */
    }

    /**
     * Xử lý một hành động tương tác tại một ô cụ thể.
     *
     * @param mainPlayer Người chơi thực hiện
     * @param playerView View của người chơi (để chạy animation)
     * @param worldMap   Bản đồ (để lấy loại ô)
     * @param col        Tọa độ ô
     * @param row        Tọa độ ô
     * @return true nếu tìm thấy và thực thi một quy tắc, false nếu không.
     */
    public boolean processInteraction(Player mainPlayer, PlayerView playerView, WorldMap worldMap, int col, int row) {
        Tool currentTool = mainPlayer.getCurrentTool();
        Tile currentTile = worldMap.getTileType(col, row);

        // Tìm quy tắc phù hợp (tool khớp VÀ tile khớp)
        for (InteractionRule rule : rules) {
            if (rule.requiredTool() == currentTool && rule.currentTileType() == currentTile) {
                // Đã tìm thấy quy tắc! Thực thi nó.

                // Ra lệnh cho PlayerView chạy animation
                mainPlayer.setState(rule.playerActionState());
                playerView.setState(mainPlayer.getState(), mainPlayer.getDirection());

                // Tính toán thời gian chờ (delay)
                // (Chuyển đổi từ Ms sang Frames Game)
                long animDurationMs = rule.actionDurationMs();
                int delayInFrames = (int) (animDurationMs / (GameLogicConfig.SECONDS_PER_FRAME * 1000));

                // Thêm hành động "Hoàn thành" vào hàng đợi (ActionManager)
                // Hành động này sẽ đổi loại Tile và reset trạng thái Player
                actionManager.addPendingAction(
                        new TimedTileAction(col, row, rule.newTileType(), delayInFrames)
                );

                return true; // Đã xử lý thành công, dừng tìm kiếm
            }
        }

        return false; // Không tìm thấy quy tắc nào phù hợp
    }
}