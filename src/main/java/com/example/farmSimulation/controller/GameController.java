package com.example.farmSimulation.controller;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

import java.util.HashSet;

public class GameController {

    // Dùng HashSet để lưu các phím đang được nhấn
    private HashSet<KeyCode> activeKeys = new HashSet<>();
    /* Đây là một tập hợp (set), nghĩa là mỗi phần tử không thể trùng nhau.
    KeyCode là kiểu dữ liệu đại diện cho các phím trên bàn phím (ví dụ: KeyCode.W, KeyCode.SPACE).
    Mục đích: lưu danh sách tất cả các phím hiện đang được nhấn.*/

    // Thiết lập listener cho Scene
    // View sẽ gọi hàm setupInputListeners này và "giao" Scene cho Controller
    public void setupInputListeners(Scene scene) {

        scene.setOnKeyPressed(event -> { // được gọi khi người chơi nhấn một phím.
            activeKeys.add(event.getCode());
            /* event.getCode() lấy mã phím nhấn, rồi thêm vào activeKeys.
            Vì là HashSet, nếu phím đang nhấn rồi thì thêm lại cũng không ảnh hưởng (không bị trùng). */
        });

        scene.setOnKeyReleased(event -> { // được gọi khi người chơi nhả phím.
            activeKeys.remove(event.getCode());
            /* Lấy mã phím và xóa khỏi activeKeys.
            Nhờ vậy, activeKeys luôn phản ánh tình trạng phím đang giữ. */
        });
    }

    // Các hàm để GameManager (model) kiểm tra phím có đang nhấn không
    public boolean isKeyPressed(KeyCode key) {
        return activeKeys.contains(key);
    }
}
