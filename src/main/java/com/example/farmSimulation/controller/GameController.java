package com.example.farmSimulation.controller;

import com.example.farmSimulation.view.MainGameView;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;

@Getter
@Setter
@NoArgsConstructor
public class GameController {

    private MainGameView mainGameView;

    // Dùng HashSet để lưu các phím đang được nhấn
    private HashSet<KeyCode> activeKeys = new HashSet<>();
    /* Đây là một tập hợp (set), nghĩa là mỗi phần tử không thể trùng nhau.
    KeyCode là kiểu dữ liệu đại diện cho các phím trên bàn phím (ví dụ: KeyCode.W, KeyCode.SPACE).
    Mục đích: lưu danh sách tất cả các phím hiện đang được nhấn.*/


    // Thiết lập listener cho Scene
    // MainGameView sẽ gọi hàm setupInputListeners này và "giao" Scene cho Controller
    public void setupInputListeners(Scene scene) {

        scene.setOnKeyPressed(event -> { // được gọi khi người chơi nhấn một phím.
            activeKeys.add(event.getCode()); // Thêm phím được ấn vào activeKeys
            /* event.getCode() lấy mã phím nhấn, rồi thêm vào activeKeys.
            Vì là HashSet, nếu phím đang nhấn rồi thì thêm lại cũng không ảnh hưởng (không bị trùng). */
            //System.out.println("Các phím đang nhấn: " + activeKeys);
        });

        scene.setOnKeyReleased(event -> { // được gọi khi người chơi nhả phím.
            activeKeys.remove(event.getCode()); // Lấy mã phím và xóa khỏi activeKeys.
            //System.out.println("Các phím đang nhấn: " + activeKeys);
        });
    }

    // Hàm để GameManager (model) kiểm tra phím có đang nhấn không
    public boolean isKeyPressed(KeyCode key) {
        return activeKeys.contains(key);
    }
}
