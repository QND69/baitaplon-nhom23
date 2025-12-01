package com.example.farmSimulation.config;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * Cấu hình cho hệ thống Quest (Nhiệm vụ hàng ngày)
 */
public class QuestConfig {
    
    // --- Số lượng quest mỗi ngày ---
    public static final int MAX_DAILY_QUESTS = 3;
    
    // --- Phần thưởng cơ bản ---
    public static final double BASE_REWARD_MONEY = 100.0; // Tiền thưởng cơ bản
    public static final double BASE_REWARD_XP = 20.0; // XP thưởng cơ bản
    
    // --- Kích thước cửa sổ Quest Board ---
    public static final double QUEST_BOARD_WIDTH = 600.0;
    public static final double QUEST_BOARD_HEIGHT = 400.0;
    
    // --- Cấu hình UI ---
    public static final double QUEST_BOARD_PADDING = 20.0;
    public static final double QUEST_ROW_HEIGHT = 100.0;
    public static final double QUEST_ROW_SPACING = 10.0;
    
    // --- Cấu hình màu sắc ---
    public static final Paint QUEST_BOARD_BG_COLOR = Color.rgb(40, 40, 40, 0.9);
    public static final Paint QUEST_TEXT_COLOR = Color.WHITE;
    public static final Paint QUEST_REWARD_COLOR = Color.GOLD;
    public static final Paint QUEST_PROGRESS_COLOR = Color.GREEN;
    
    // --- Cấu hình font ---
    public static final double QUEST_TITLE_FONT_SIZE = 18.0;
    public static final double QUEST_DESCRIPTION_FONT_SIZE = 14.0;
    public static final double QUEST_REWARD_FONT_SIZE = 14.0;
    
    // --- Cấu hình button ---
    public static final double CLAIM_BUTTON_WIDTH = 100.0;
    public static final double CLAIM_BUTTON_HEIGHT = 30.0;
    public static final Paint CLAIM_BUTTON_ENABLED_COLOR = Color.rgb(100, 200, 100);
    public static final Paint CLAIM_BUTTON_DISABLED_COLOR = Color.rgb(100, 100, 100);
    public static final Paint CLAIM_BUTTON_CLAIMED_COLOR = Color.rgb(150, 150, 150);
    
    // --- Cấu hình progress bar ---
    public static final double PROGRESS_BAR_WIDTH = 200.0;
    public static final double PROGRESS_BAR_HEIGHT = 20.0;
    
    private QuestConfig() {}
}





