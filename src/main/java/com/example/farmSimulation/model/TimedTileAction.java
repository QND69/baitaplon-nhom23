package com.example.farmSimulation.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TimedTileAction {
    private int col; // Tọa độ X
    private int row; // Tọa độ Y
    private Tile newType; // Loại Tile mới sẽ đổi thành
    private int framesRemaining; // Số frame còn lại

    public boolean tick() {
        this.framesRemaining--; // Đếm lùi
        return this.framesRemaining <= 0; // Trả về true nếu hết giờ
    }
}
