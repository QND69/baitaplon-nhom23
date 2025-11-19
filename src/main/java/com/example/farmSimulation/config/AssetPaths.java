package com.example.farmSimulation.config;

// Chứa TOÀN BỘ đường dẫn đến tài nguyên (assets)
public final class AssetPaths {

    // Player
    public static final String PLAYER_SHEET = "/assets/images/entities/player/player_scaled_4x.png";
    public static final String PLAYER_ACTIONS_SHEET = "/assets/images/entities/player/player_action_merged.png";

    // World Tiles
    public static final String GRASS = "/assets/images/world/grass5.png";
    public static final String SOIL = "/assets/images/world/soil1.png";
    public static final String WATER = "/assets/images/world/water4.png";
    /** Ảnh đất đã tưới (sẫm màu hơn) */
    public static final String SOIL_WET = "/assets/images/world/soil_wet.png";

    /** Ảnh lớp phủ (overlay) phân bón */
    public static final String FERTILIZER_OVERLAY = "/assets/images/world/fertilizer_layer_2_scaled_2x.png";

    /** Spritesheet cho tất cả cây trồng (Dâu, Cà rốt...) */
    public static final String CROP_SHEET = "/assets/images/items/crops/seed7_scaled_4x.png";

    // GUI
    public static final String LOGO = "/assets/images/GUI/logo.png";

    public static final String TOOLS_SHEET = "/assets/images/items/tools/tools5.png";

    public static final String ICON_BG = "/assets/images/GUI/icon_white_bg3.png";

    private AssetPaths() {
    }
}