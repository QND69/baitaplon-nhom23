package com.example.farmSimulation.model;

import com.example.farmSimulation.view.PlayerView;

/**
 * Định nghĩa một quy tắc tương tác (Luật chơi) bằng record.
 *
 * @param requiredTool      Công cụ yêu cầu
 * @param currentTileType   Loại ô đất hiện tại
 * @param newTileType       Loại ô đất mới. Dùng 'null' nếu không thay đổi.
 * @param playerActionState Animation mà Player sẽ thực hiện
 * @param actionDurationMs  Tổng thời gian (ms) để hoàn thành hành động
 */
public record InteractionRule(
        Tool requiredTool,
        Tile currentTileType,
        Tile newTileType,
        PlayerView.PlayerState playerActionState,
        long actionDurationMs
) {
    /** Tính chất chính của class record
     * Immutable (bất biến): Các field final Không thể thay đổi sau khi tạo object
     * Không cần viết getter/setter, constructor, toString, equals, hashCode nên ko cần viết gì ở đây
     */
}