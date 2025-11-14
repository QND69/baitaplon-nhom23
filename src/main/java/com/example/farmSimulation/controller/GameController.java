package com.example.farmSimulation.controller;

import com.example.farmSimulation.config.HotbarConfig;
import com.example.farmSimulation.model.GameManager;
import com.example.farmSimulation.view.MainGameView;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;

@Getter
@Setter
@NoArgsConstructor
public class GameController {
    private MainGameView mainGameView;
    private GameManager gameManager;

    // 2 biến lưu tọa độ chuột
    private double mouseX;
    private double mouseY;

    // Dùng HashSet để lưu các phím đang được nhấn
    private HashSet<KeyCode> activeKeys = new HashSet<>();
    /* Đây là một tập hợp (set), nghĩa là mỗi phần tử không thể trùng nhau.
    KeyCode là kiểu dữ liệu đại diện cho các phím trên bàn phím (ví dụ: KeyCode.W, KeyCode.SPACE).
    Mục đích: lưu danh sách tất cả các phím hiện đang được nhấn.*/

    // Thiết lập listener cho Scene
    // MainGameView sẽ gọi hàm setupInputListeners này và "giao" Scene cho Controller
    public void setupInputListeners(Scene scene) {

        // Lắng nghe sự kiện ẤN PHÍM
        scene.setOnKeyPressed(event -> { // được gọi khi người chơi nhấn một phím.
            activeKeys.add(event.getCode()); // Thêm phím được ấn vào activeKeys
            /* event.getCode() lấy mã phím nhấn, rồi thêm vào activeKeys.
            Vì là HashSet, nếu phím đang nhấn rồi thì thêm lại cũng không ảnh hưởng (không bị trùng). */
            //System.out.println("Các phím đang nhấn: " + activeKeys);
            // Thêm xử lý ESC
            if (event.getCode() == KeyCode.ESCAPE) {
                if (gameManager != null) {
                    gameManager.toggleSettingsMenu(); // Gọi hàm hiển thị/ẩn menu
                }
            }
            // Xử lý phím số (1-9, 0) để đổi hotbar
            if (event.getCode().isDigitKey()) {
                int slot = -1;
                if (event.getCode() == KeyCode.DIGIT1) slot = 0;
                else if (event.getCode() == KeyCode.DIGIT2) slot = 1;
                else if (event.getCode() == KeyCode.DIGIT3) slot = 2;
                else if (event.getCode() == KeyCode.DIGIT4) slot = 3;
                else if (event.getCode() == KeyCode.DIGIT5) slot = 4;
                else if (event.getCode() == KeyCode.DIGIT6) slot = 5;
                else if (event.getCode() == KeyCode.DIGIT7) slot = 6;
                else if (event.getCode() == KeyCode.DIGIT8) slot = 7;
                else if (event.getCode() == KeyCode.DIGIT9) slot = 8;
                else if (event.getCode() == KeyCode.DIGIT0) slot = 9;

                if (slot != -1 && gameManager != null) {
                    gameManager.changeHotbarSlot(slot);
                }
            }
        });

        scene.setOnKeyReleased(event -> { // được gọi khi người chơi nhả phím.
            activeKeys.remove(event.getCode()); // Lấy mã phím và xóa khỏi activeKeys.
            //System.out.println("Các phím đang nhấn: " + activeKeys);
        });

        // Lắng nghe sự kiện DI CHUYỂN CHUỘT
        scene.setOnMouseMoved(event -> {
            this.mouseX = event.getSceneX();
            this.mouseY = event.getSceneY();
        });

        // Lắng nghe click chuột
        scene.setOnMouseClicked(event -> {
            handleMouseClick(event);
        });

        // Lắng nghe cuộn chuột
        scene.setOnScroll(event -> {
            if (gameManager == null || gameManager.isPaused()) return; // Không cuộn khi đang pause

            int currentSlot = gameManager.getMainPlayer().getSelectedHotbarSlot();
            if (event.getDeltaY() < 0) { // Cuộn xuống -> slot tiếp theo
                currentSlot = (currentSlot + 1) % HotbarConfig.HOTBAR_SLOT_COUNT;
            } else if (event.getDeltaY() > 0) { // Cuộn lên -> slot trước đó
                currentSlot = (currentSlot - 1 + HotbarConfig.HOTBAR_SLOT_COUNT) % HotbarConfig.HOTBAR_SLOT_COUNT;
            }
            gameManager.changeHotbarSlot(currentSlot);
        });
    }

    // Hàm để GameManager (model) kiểm tra phím có đang nhấn không
    public boolean isKeyPressed(KeyCode key) {
        return activeKeys.contains(key);
    }

    public void handleMouseClick(MouseEvent event) {
        if (event.getButton() != javafx.scene.input.MouseButton.PRIMARY) return;
        // GỌI HÀM LOGIC GAME (Ném hành động vào hàng đợi)
        gameManager.interactWithTile(
                gameManager.getCurrentMouseTileX(),
                gameManager.getCurrentMouseTileY()
        );
    }
}
