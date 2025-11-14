package com.example.farmSimulation.model;

import com.example.farmSimulation.config.GameLogicConfig;
import com.example.farmSimulation.config.HotbarConfig;
import com.example.farmSimulation.view.PlayerView;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class Player {
    private String name;
    private double money;
    private int experience;
    private int level;
    private double stamina;

    // Tọa độ logic của người chơi trong thế giới
    private double tileX;
    private double tileY;

    // Lưu trữ trạng thái LOGIC (Model)
    // PlayerView sẽ lưu trạng thái VISUAL (View)
    private PlayerView.PlayerState state;
    private PlayerView.Direction direction;

    // --- Hotbar (Thanh công cụ) ---
    private Tool[] hotbarTools;
    private int selectedHotbarSlot;

    // Constructor
    public Player() {
        this.tileX = GameLogicConfig.PLAYER_START_X;
        this.tileY = GameLogicConfig.PLAYER_START_Y;
        this.state = PlayerView.PlayerState.IDLE; // Trạng thái ban đầu
        this.direction = PlayerView.Direction.DOWN; // Hướng ban đầu

        // Khởi tạo hotbar
        this.hotbarTools = new Tool[HotbarConfig.HOTBAR_SLOT_COUNT];
        this.selectedHotbarSlot = 0;
        // Gán công cụ vào hotbar (ví dụ)
        this.hotbarTools[0] = Tool.HOE;
        this.hotbarTools[1] = Tool.WATERING_CAN;
        this.hotbarTools[2] = Tool.PICKAXE;
        this.hotbarTools[3] = Tool.SHOVEL;
        // Các ô khác là null (sẽ là Tool.HAND)
    }

    /**
     * Lấy công cụ hiện tại đang cầm trên tay (dựa vào ô hotbar)
     */
    public Tool getCurrentTool() {
        Tool selectedTool = hotbarTools[selectedHotbarSlot];
        // Nếu ô đó trống (null), mặc định là dùng tay
        if (selectedTool == null) {
            return Tool.HAND;
        }
        return selectedTool;
    }
}