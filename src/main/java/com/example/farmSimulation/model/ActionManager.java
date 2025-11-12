package com.example.farmSimulation.model;

import com.example.farmSimulation.view.MainGameView;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ActionManager {
    private final List<TimedTileAction> pendingActions;  // Thêm danh sách hành động chờ
    private boolean mapNeedsUpdate = false;

    public ActionManager() {
        this.pendingActions = new ArrayList<>();
    }

    public void addPendingAction(TimedTileAction action) {
        pendingActions.add(action);
    }

    /**
     * Hàm này được gọi 60 LẦN/GIÂY.
     * Nhiệm vụ: Lặp qua tất cả hành động chờ, "tick" chúng,
     * và thực thi những hành động đã hết giờ.
     */
    public void updateTimedActions(WorldMap worldMap, MainGameView mainGameView, double worldOffsetX, double worldOffsetY) {
        // Dùng Iterator để chúng ta có thể XÓA phần tử khỏi List pendingActions một cách an toàn
        Iterator<TimedTileAction> iterator = pendingActions.iterator();

        while (iterator.hasNext()) {
            TimedTileAction action = iterator.next();

            // Gọi tick(). Nếu nó trả về "true" (hết giờ)
            if (action.tick()) {
                // THỰC THI HÀNH ĐỘNG: Thay đổi Model
                worldMap.setTileType(action.getCol(), action.getRow(), action.getNewType());

                // Báo cho View biết cần vẽ lại bản đồ
                this.mapNeedsUpdate = true;

                // Xóa hành động này khỏi hàng đợi
                iterator.remove();
            }
        }

        // Update map nếu cần
        if (this.mapNeedsUpdate) {
            mainGameView.updateMap(worldOffsetX, worldOffsetY, true);
            this.mapNeedsUpdate = false;
        }
    }
}