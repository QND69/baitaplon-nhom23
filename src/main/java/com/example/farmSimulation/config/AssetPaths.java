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
    public static final String CROP_SHEET = "/assets/images/items/crops/full_crop_64x64.png";

    /** Spritesheet cho cây tự nhiên (tree2_64x96.png) */
    public static final String TREE_SHEET = "/assets/images/world/tree2_64x96.png";

    /** Spritesheet cho hàng rào (fences_64x64.png) */
    public static final String FENCE_SHEET = "/assets/images/world/fences_64x64.png";

    // GUI
    public static final String LOGO = "/assets/images/GUI/logo.png";
    public static final String GUI_ICONS = "/assets/images/GUI/GUI_icon_32x32.png"; // GUI icons sheet

    public static final String ITEMS_SHEET = "/assets/images/items/tools/items_32x32.png";

    /** Spritesheet cho icon vật nuôi (animal_item_32x32.png) */
    public static final String ANIMAL_ITEM_SHEET = "/assets/images/items/tools/animal_item_32x32.png";

    public static final String ICON_BG = "/assets/images/GUI/icon_white_bg3.png";

    // Audio
    public static final String BACKGROUND_MUSIC = "/assets/sounds/bg_music_2.mp3";

    // --- Animal Sheets (Cấu hình đúng theo tên file ảnh) ---

    // Chicken - 48x48
    public static final String CHICKEN_SHEET = "/assets/images/entities/animal/chicken_48x48.png";

    // Cow, Pig - 96x96
    public static final String COW_SHEET = "/assets/images/entities/animal/cow_96x96.png";
    public static final String PIG_SHEET = "/assets/images/entities/animal/pig_96x96.png";

    // Sheep - 64x64
    public static final String SHEEP_SHEET = "/assets/images/entities/animal/sheep_64x64.png";

    // Egg & Baby Chick (Dùng riêng cho trứng hoặc gà con nếu không scale từ gà lớn) - 32x32
    public static final String BABY_CHICKEN_EGG_SHEET = "/assets/images/entities/animal/babychick&egg_32x32.png";

    private AssetPaths() {
    }
}