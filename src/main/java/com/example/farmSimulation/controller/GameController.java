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

            // Xử lý các phím hệ thống luôn hoạt động (ESC)
            handleSystemInput(event.getCode());

            // Block all other inputs when game is paused (Settings Menu is open)
            if (gameManager != null && gameManager.isPaused()) return;

            // Xử lý các phím chức năng trong game
            handleGameInput(event.getCode());
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
        scene.setOnMouseClicked(this::handleMouseClick);

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

    /**
     * [MỚI] Xử lý phím hệ thống (Luôn lắng nghe dù game đang pause hay không)
     */
    private void handleSystemInput(KeyCode code) {
        if (code == KeyCode.ESCAPE && gameManager != null) {
            gameManager.toggleSettingsMenu(); // Gọi hàm hiển thị/ẩn menu
        }
    }

    /**
     * [MỚI] Xử lý các phím chức năng game (Chỉ lắng nghe khi game không pause)
     */
    private void handleGameInput(KeyCode code) {
        if (gameManager == null) return;

        // Xử lý phím Q để ném item từ slot mà chuột đang trỏ vào
        if (code == KeyCode.Q) {
            int slotIndex = gameManager.getHotbarSlotFromMouse(mouseX, mouseY);
            if (slotIndex >= 0) {
                gameManager.dropItemFromHotbar(slotIndex);
            }
        }

        // [MỚI] Xử lý phím B để bật/tắt Shop
        if (code == KeyCode.B && mainGameView != null) {
            mainGameView.toggleShop();
        }

        // [MỚI] Xử lý phím M để test đổi thời tiết
        if (code == KeyCode.M) {
            gameManager.toggleWeather();
        }

        // [MỚI] Xử lý phím J để bật/tắt Quest Board
        if (code == KeyCode.J && mainGameView != null) {
            mainGameView.toggleQuestBoard();
        }

        // [MỚI] Cheat code: Bấm 'L' để thêm tiền
        if (code == KeyCode.L) {
            gameManager.getMainPlayer().addMoney(com.example.farmSimulation.config.GameLogicConfig.CHEAT_MONEY_AMOUNT);
        }

        // Xử lý phím số (1-9, 0) để đổi hotbar
        if (code.isDigitKey()) {
            int slot = getSlotFromDigit(code);
            if (slot != -1) {
                gameManager.changeHotbarSlot(slot);
            }
        }
    }

    /**
     * Chuyển đổi KeyCode phím số sang chỉ số Slot (0-9)
     */
    private int getSlotFromDigit(KeyCode code) {
        return switch (code) {
            case DIGIT1 -> 0;
            case DIGIT2 -> 1;
            case DIGIT3 -> 2;
            case DIGIT4 -> 3;
            case DIGIT5 -> 4;
            case DIGIT6 -> 5;
            case DIGIT7 -> 6;
            case DIGIT8 -> 7;
            case DIGIT9 -> 8;
            case DIGIT0 -> 9;
            default -> -1;
        };
    }

    // Hàm để GameManager (model) kiểm tra phím có đang nhấn không
    public boolean isKeyPressed(KeyCode key) {
        return activeKeys.contains(key);
    }

    public void handleMouseClick(MouseEvent event) {
        // Block all mouse interactions when game is paused (Settings Menu is open)
        if (gameManager == null || gameManager.isPaused()) return;

        // Xử lý click chuột phải (SECONDARY) để mở/đóng hàng rào hoặc ăn đồ
        if (event.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
            // First: Check if clicking on a Fence -> Toggle Fence
            int tileX = gameManager.getCurrentMouseTileX();
            int tileY = gameManager.getCurrentMouseTileY();

            // Check if there's a fence at this position
            if (gameManager.hasFenceAt(tileX, tileY)) {
                gameManager.toggleFence(tileX, tileY);
            } else {
                // Else: If holding an edible item -> Eat food
                gameManager.handlePlayerEating();
            }
            return;
        }

        // Xử lý click chuột trái (PRIMARY) cho các hành động khác
        if (event.getButton() != javafx.scene.input.MouseButton.PRIMARY) return;
        // GỌI HÀM LOGIC GAME (Ném hành động vào hàng đợi)
        gameManager.interactWithTile(
                gameManager.getCurrentMouseTileX(),
                gameManager.getCurrentMouseTileY()
        );
    }
}