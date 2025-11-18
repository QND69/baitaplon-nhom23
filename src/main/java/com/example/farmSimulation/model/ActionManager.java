package com.example.farmSimulation.model;

import com.example.farmSimulation.view.MainGameView;
import com.example.farmSimulation.view.PlayerView;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Getter
@Setter
public class ActionManager {
    private final List<TimedTileAction> pendingActions;  // Thêm danh sách hành động chờ
    private boolean mapNeedsUpdate = false;

    // Thêm tham chiếu đến Model và View của Player
    private final Player mainPlayer;
    private final PlayerView playerView;

    public ActionManager(Player mainPlayer, PlayerView playerView) {
        this.pendingActions = new ArrayList<>();
        this.mainPlayer = mainPlayer;
        this.playerView = playerView;
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
            if (action.tick()) { // Cho phép action không đổi tile
                // THỰC THI HÀNH ĐỘNG: Thay đổi Model
                if (action.getNewTileData() != null) {
                    worldMap.setTileData(action.getCol(), action.getRow(), action.getNewTileData());
                    this.mapNeedsUpdate = true; // Báo cho View biết cần vẽ lại bản đồ
                }

                // Reset trạng thái Player về IDLE sau khi hành động xong
                // Chỉ reset nếu player VẪN đang trong trạng thái hành động đó
                PlayerView.PlayerState currentState = mainPlayer.getState();
                if (currentState == PlayerView.PlayerState.HOE ||
                        currentState == PlayerView.PlayerState.WATER ||
                        currentState == PlayerView.PlayerState.ATTACK ||
                        currentState == PlayerView.PlayerState.BUSY) {

                    mainPlayer.setState(PlayerView.PlayerState.IDLE);
                    playerView.setState(mainPlayer.getState(), mainPlayer.getDirection());
                }

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