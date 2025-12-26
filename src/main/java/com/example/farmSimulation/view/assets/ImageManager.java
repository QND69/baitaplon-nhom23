package com.example.farmSimulation.view.assets;

import com.example.farmSimulation.config.*;
import com.example.farmSimulation.model.*;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

// Tải và quản lý tất cả tài nguyên
public class ImageManager {
    // Cache là một cơ chế lưu trữ tạm thời dữ liệu đã tính toán hoặc đã tải về
    // để sử dụng lại sau này, nhằm tăng tốc độ truy xuất và giảm tài nguyên tiêu thụ.

    // Cache cho các ảnh <key, value> (key là đường dẫn)
    private final Map<String, Image> textureCache = new HashMap<>();

    // Cache đặc biệt cho Tile (tối ưu cho Enum)
    private final Map<Tile, Image> tileTextureMap = new EnumMap<>(Tile.class);

    // Cache cho các sprite đã được cắt
    private final Map<String, Image> spriteCache = new HashMap<>();

    // Cache cho icon của Item (để dùng cho HUD và Shop)
    private final Map<ItemType, Image> itemIconCache = new EnumMap<>(ItemType.class);

    // Cache riêng cho Icon trạng thái (Status)
    private final Map<CropStatusIndicator, Image> statusIconCache = new EnumMap<>(CropStatusIndicator.class);

    // Cache cho GUI icons (Settings, Shop, Money, Weather, etc.)
    private final Map<String, Image> guiIconCache = new HashMap<>();

    /**
     * Tải TOÀN BỘ tài nguyên game vào bộ nhớ.
     * Sẽ được gọi 1 lần duy nhất khi game bắt đầu.
     */
    public void loadAssets() {
        // Tải Player
        getTexture(AssetPaths.PLAYER_SHEET);
        getTexture(AssetPaths.PLAYER_ACTIONS_SHEET);

        // Tải GUI
        getTexture(AssetPaths.LOGO);
        Image itemsSheet = getTexture(AssetPaths.ITEMS_SHEET); // Load items sheet
        getTexture(AssetPaths.ANIMAL_ITEM_SHEET); // Load animal item sheet
        getTexture(AssetPaths.ICON_BG); // Tải nền icon

        // Tải Tile Textures
        Image grass = getTexture(AssetPaths.GRASS);
        Image soil = getTexture(AssetPaths.SOIL);
        Image water = getTexture(AssetPaths.WATER);
        Image soilWet = getTexture(AssetPaths.SOIL_WET); // Tải đất ướt

        // Tải các tài nguyên trồng trọt
        getTexture(AssetPaths.FERTILIZER_OVERLAY); // Tải phân bón
        getTexture(AssetPaths.CROP_SHEET); // Tải ảnh crop

        // Tải các tài nguyên cây và hàng rào
        getTexture(AssetPaths.TREE_SHEET); // Tải ảnh cây tự nhiên
        getTexture(AssetPaths.FENCE_SHEET); // Tải ảnh hàng rào
        // TREE và FENCE không có texture cố định, sẽ được vẽ từ spritesheet

        // Tải các tài nguyên động vật
        loadAnimalTextures();

        // Liên kết Tile (Model) với Image (View)
        tileTextureMap.put(Tile.GRASS, grass);
        tileTextureMap.put(Tile.SOIL, soil);
        tileTextureMap.put(Tile.WATER, water);
        tileTextureMap.put(Tile.SOIL_WET, soilWet);

        // Cắt và cache các icon trạng thái từ Items Sheet
        loadStatusIcons(itemsSheet);

        // Tải và cache GUI icons
        Image guiIconSheet = getTexture(AssetPaths.GUI_ICONS);
        loadGuiIcons(guiIconSheet);
    }

    /**
     * Cắt icon từ ITEMS_SHEET để làm icon trạng thái
     */
    private void loadStatusIcons(Image itemsSheet) {
        if (itemsSheet == null) return;
        PixelReader reader = itemsSheet.getPixelReader(); // PixelReader là đối tượng đọc pixel của Image

        // Cắt Icon Bình Nước (Watering Can)
        WritableImage waterIcon = new WritableImage(reader,
                (int) (ItemSpriteConfig.ITEM_WATERING_CAN_COL * ItemSpriteConfig.ITEM_SPRITE_WIDTH), 0,
                (int) ItemSpriteConfig.ITEM_SPRITE_WIDTH, (int) ItemSpriteConfig.ITEM_SPRITE_HEIGHT);
        statusIconCache.put(CropStatusIndicator.NEEDS_WATER, waterIcon);

        // Cắt Icon Phân bón (Fertilizer)
        WritableImage fertilizerIcon = new WritableImage(reader,
                (int) (ItemSpriteConfig.ITEM_FERTILISER_COL * ItemSpriteConfig.ITEM_SPRITE_WIDTH), 0,
                (int) ItemSpriteConfig.ITEM_SPRITE_WIDTH, (int) ItemSpriteConfig.ITEM_SPRITE_HEIGHT);
        statusIconCache.put(CropStatusIndicator.NEEDS_FERTILIZER, fertilizerIcon);

        // Tạo Icon Kép (Nước + Phân bón)
        WritableImage combinedIcon = new WritableImage(reader,
                (int) (ItemSpriteConfig.ITEM_WATERING_CAN_COL * ItemSpriteConfig.ITEM_SPRITE_WIDTH), 0,
                (int) (ItemSpriteConfig.ITEM_SPRITE_WIDTH * 2), (int) ItemSpriteConfig.ITEM_SPRITE_HEIGHT);
        statusIconCache.put(CropStatusIndicator.NEED_WATER_AND_FERTILIZER, combinedIcon);

        // Icon Thu hoạch
        WritableImage harvestIcon = new WritableImage(reader,
                (int) (ItemSpriteConfig.ITEM_SCYTHE_COL * ItemSpriteConfig.ITEM_SPRITE_WIDTH), 0,
                (int) ItemSpriteConfig.ITEM_SPRITE_WIDTH, (int) ItemSpriteConfig.ITEM_SPRITE_HEIGHT);
        statusIconCache.put(CropStatusIndicator.READY_TO_HARVEST, harvestIcon);
    }

    /**
     * Cắt và cache các GUI icons từ GUI_icon_32x32.png
     */
    private void loadGuiIcons(Image guiIconSheet) {
        if (guiIconSheet == null) return;

        PixelReader reader = guiIconSheet.getPixelReader();
        double iconSize = HudConfig.GUI_ICON_SIZE;
        int row = 0;

        // Cắt Settings (Gear) icon
        WritableImage settingsIcon = new WritableImage(reader,
                (int) (HudConfig.GUI_ICON_SETTINGS_COL * iconSize), (int) (row * iconSize),
                (int) iconSize, (int) iconSize);
        guiIconCache.put("SETTINGS", settingsIcon);

        // Cắt Shop icon
        WritableImage shopIcon = new WritableImage(reader,
                (int) (HudConfig.GUI_ICON_SHOP_COL * iconSize), (int) (row * iconSize),
                (int) iconSize, (int) iconSize);
        guiIconCache.put("SHOP", shopIcon);

        // Cắt Money ($) icon
        WritableImage moneyIcon = new WritableImage(reader,
                (int) (HudConfig.GUI_ICON_MONEY_COL * iconSize), (int) (row * iconSize),
                (int) iconSize, (int) iconSize);
        guiIconCache.put("MONEY", moneyIcon);

        // Cắt Sunny weather icon
        WritableImage sunnyIcon = new WritableImage(reader,
                (int) (HudConfig.GUI_ICON_SUNNY_COL * iconSize), (int) (row * iconSize),
                (int) iconSize, (int) iconSize);
        guiIconCache.put("SUNNY", sunnyIcon);

        // Cắt Rain weather icon
        WritableImage rainIcon = new WritableImage(reader,
                (int) (HudConfig.GUI_ICON_RAIN_COL * iconSize), (int) (row * iconSize),
                (int) iconSize, (int) iconSize);
        guiIconCache.put("RAIN", rainIcon);

        // Cắt Energy Bar Empty (Lightning) icon
        WritableImage energyEmptyIcon = new WritableImage(reader,
                (int) (HudConfig.GUI_ICON_ENERGY_EMPTY_COL * iconSize), (int) (row * iconSize),
                (int) iconSize, (int) iconSize);
        guiIconCache.put("ENERGY_EMPTY", energyEmptyIcon);

        // Cắt Energy Bar Full (Lightning) icon
        WritableImage energyFullIcon = new WritableImage(reader,
                (int) (HudConfig.GUI_ICON_ENERGY_FULL_COL * iconSize), (int) (row * iconSize),
                (int) iconSize, (int) iconSize);
        guiIconCache.put("ENERGY_FULL", energyFullIcon);

        // Cắt Trash Can icon
        WritableImage trashIcon = new WritableImage(reader,
                (int) (HudConfig.GUI_ICON_TRASH_COL * iconSize), (int) (row * iconSize),
                (int) iconSize, (int) iconSize);
        guiIconCache.put("TRASH", trashIcon);

        // Cắt Quest (Scroll/Checklist) icon
        WritableImage questIcon = new WritableImage(reader,
                (int) (HudConfig.GUI_ICON_QUEST_COL * iconSize), (int) (row * iconSize),
                (int) iconSize, (int) iconSize);
        guiIconCache.put("QUEST", questIcon);

        // Cắt Stamina icon
        WritableImage staminaIcon = new WritableImage(reader,
                (int) (HudConfig.GUI_ICON_STAMINA_COL * iconSize), (int) (row * iconSize),
                (int) iconSize, (int) iconSize);
        guiIconCache.put("STAMINA", staminaIcon);

        // Cắt EXP icon
        WritableImage expIcon = new WritableImage(reader,
                (int) (HudConfig.GUI_ICON_EXP_COL * iconSize), (int) (row * iconSize),
                (int) iconSize, (int) iconSize);
        guiIconCache.put("EXP", expIcon);
    }

    /**
     * Lấy GUI icon đã cache
     *
     * @param iconName Tên icon: "SETTINGS", "SHOP", "MONEY", "SUNNY", "RAIN", "ENERGY_EMPTY", "ENERGY_FULL", "TRASH", "QUEST", "STAMINA", "EXP"
     * @return Image của icon, null nếu không tìm thấy
     */
    public Image getGuiIcon(String iconName) {
        return guiIconCache.get(iconName);
    }

    /**
     * Lấy một ảnh từ cache. Nếu chưa có, nó sẽ tải và lưu lại.
     *
     * @param path Đường dẫn từ class AssetPaths
     */
    public Image getTexture(String path) {
        // computeIfAbsent là một method của Map (Java 8+).
        // computeIfAbsent: Nếu 'path' chưa có trong cache,
        // nó sẽ chạy hàm lambda (v -> new Image(...)) để tải ảnh,
        // sau đó tự động 'put' vào cache và return ảnh đó.
        // Cú pháp nếu ko dùng ->: V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)
        return textureCache.computeIfAbsent(path, p ->
                new Image(getClass().getResourceAsStream(p))
        );
    }

    /**
     * Lấy ảnh texture cho một loại Tile (GRASS, SOIL, etc.)
     * Siêu nhanh vì dùng EnumMap.
     */
    public Image getTileTexture(Tile tileType) {
        // Trả về ảnh mặc định (GRASS) nếu không tìm thấy
        return tileTextureMap.getOrDefault(tileType, tileTextureMap.get(Tile.GRASS));
    }

    /**
     * Lấy ảnh lớp phủ phân bón (đã cache)
     */
    public Image getFertilizerTexture() {
        return getTexture(AssetPaths.FERTILIZER_OVERLAY);
    }

    // Lấy ảnh nền Icon
    public Image getIconBG() {
        return getTexture(AssetPaths.ICON_BG);
    }

    // Lấy icon trạng thái đã cache
    public Image getStatusIcon(CropStatusIndicator status) {
        return statusIconCache.get(status);
    }

    /**
     * Lấy sprite của cây trồng và cache lại.
     */
    public Image getCropTexture(CropData cropData) {
        if (cropData == null) {
            return null;
        }

        // Kiểm tra nếu cây chết hoặc dữ liệu sai
        if (cropData.getGrowthStage() < 0) {
            return null;
        }

        // Tạo key cache
        String key = cropData.getType().name() + "_" + cropData.getGrowthStage();

        // Dùng computeIfAbsent để cắt 1 lần duy nhất
        return spriteCache.computeIfAbsent(key, k -> {
            Image cropSheet = getTexture(AssetPaths.CROP_SHEET);
            if (cropSheet == null) return null;

            PixelReader reader = cropSheet.getPixelReader();

            // Tính toán vị trí (x, y) để cắt
            double w = CropConfig.CROP_SPRITE_WIDTH;
            double h = CropConfig.CROP_SPRITE_HEIGHT;
            int x = (int) (cropData.getGrowthStage() * w);
            int y = (int) (cropData.getType().getSpriteRow() * h);

            if (x < 0 || y < 0 || x + w > cropSheet.getWidth() || y + h > cropSheet.getHeight()) return null;

            // Cắt và tạo ảnh mới
            return new WritableImage(reader, x, y, (int) w, (int) h);
        });
    }

    /**
     * Lấy icon hạt giống từ CROP_SHEET (Frame 0) để hiển thị ghost placement
     */
    public Image getSeedIcon(CropType type) {
        Image cropSheet = getTexture(AssetPaths.CROP_SHEET);
        if (cropSheet == null) return null;

        PixelReader reader = cropSheet.getPixelReader();

        // Lấy Frame 0 (Giai đoạn hạt giống)
        int x = (int) (CropConfig.CROP_SEED_FRAME_INDEX * CropConfig.CROP_SPRITE_WIDTH); // = 0
        // Lấy hàng tương ứng với loại cây
        int y = (int) (type.getSpriteRow() * CropConfig.CROP_SPRITE_HEIGHT);

        return new WritableImage(reader, x, y, (int) CropConfig.CROP_SPRITE_WIDTH, (int) CropConfig.CROP_SPRITE_HEIGHT);
    }

    /**
     * Lấy icon hạt giống cây tự nhiên từ TREE_SHEET (Stage 0) để hiển thị ghost placement
     */
    public Image getTreeSeedIcon() {
        Image treeSheet = getTexture(AssetPaths.TREE_SHEET);
        if (treeSheet == null) return null;

        PixelReader reader = treeSheet.getPixelReader();

        // Stage 0 (Hạt/Mầm) là frame đầu tiên
        int x = (int) (TreeConfig.TREE_SEED_STAGE * TreeConfig.TREE_SPRITE_WIDTH);
        int y = 0; // Hàng duy nhất trong tree sheet

        return new WritableImage(reader, x, y, (int) TreeConfig.TREE_SPRITE_WIDTH, (int) TreeConfig.TREE_SPRITE_HEIGHT);
    }

    /**
     * Lấy icon sản phẩm thu hoạch (Frame cuối cùng của cây)
     */
    public Image getHarvestIcon(CropType type) {
        Image cropSheet = getTexture(AssetPaths.CROP_SHEET);
        if (cropSheet == null) return null;

        PixelReader reader = cropSheet.getPixelReader();

        // Lấy Frame cuối cùng (MaxStages)
        int x = (int) (CropConfig.CROP_HARVEST_FRAME_INDEX * CropConfig.CROP_SPRITE_WIDTH);
        int y = (int) (type.getSpriteRow() * CropConfig.CROP_SPRITE_HEIGHT);

        return new WritableImage(reader, x, y, (int) CropConfig.CROP_SPRITE_WIDTH, (int) CropConfig.CROP_SPRITE_HEIGHT);
    }

    /**
     * Lưu trữ icon của item (dùng cho HUD)
     */
    public void cacheItemIcon(ItemType type, Image icon) {
        itemIconCache.put(type, icon);
    }

    /**
     * Lấy icon của bất kỳ loại item nào (Tools, Seeds, Animal Items, Products).
     * Hàm này tự động chọn sheet phù hợp và cắt ảnh nếu chưa có trong cache.
     */
    public Image getItemIcon(ItemType type) {
        if (type == null) return null;

        // Kiểm tra cache chính trước
        if (itemIconCache.containsKey(type)) {
            return itemIconCache.get(type);
        }

        // Nếu chưa có, tiến hành xử lý logic lấy icon
        Image icon = null;

        // Kiểm tra xem là Item vật nuôi (mua ở shop) hay Item thông thường
        if (isAnimalItem(type)) {
            icon = getClippedAnimalItemIcon(type);
        } else {
            icon = getClippedGeneralItemIcon(type);
        }

        // Cache lại và trả về
        if (icon != null) {
            itemIconCache.put(type, icon);
        }
        return icon;
    }

    /**
     * Kiểm tra xem ItemType có phải là loại vật nuôi để mua không
     */
    private boolean isAnimalItem(ItemType type) {
        return type == ItemType.ITEM_COW || type == ItemType.ITEM_CHICKEN ||
                type == ItemType.ITEM_SHEEP || type == ItemType.ITEM_PIG;
    }

    /**
     * Cắt icon từ animal_item_32x32.png
     */
    private Image getClippedAnimalItemIcon(ItemType itemType) {
        int col = -1;
        if (itemType == ItemType.ITEM_COW) col = ItemSpriteConfig.ANIMAL_ITEM_COW_COL;
        else if (itemType == ItemType.ITEM_CHICKEN) col = ItemSpriteConfig.ANIMAL_ITEM_CHICKEN_COL;
        else if (itemType == ItemType.ITEM_SHEEP) col = ItemSpriteConfig.ANIMAL_ITEM_SHEEP_COL;
        else if (itemType == ItemType.ITEM_PIG) col = ItemSpriteConfig.ANIMAL_ITEM_PIG_COL;

        if (col < 0) return null;

        Image sheet = getTexture(AssetPaths.ANIMAL_ITEM_SHEET);
        return clipItemFromSheet(sheet, col, 0);
    }

    /**
     * Cắt icon từ items_32x32.png dựa trên loại item
     */
    private Image getClippedGeneralItemIcon(ItemType type) {
        int col = -1;
        // Map các loại item sang cột tương ứng trong items_32x32.png
        // Lưu ý: Các nhãn case phải khớp hoàn toàn với tên hằng số trong ItemType.java
        switch (type) {
            case AXE: col = ItemSpriteConfig.ITEM_AXE_COL; break;
            case HOE: col = ItemSpriteConfig.ITEM_HOE_COL; break;
            case WATERING_CAN: col = ItemSpriteConfig.ITEM_WATERING_CAN_COL; break;
            case FERTILIZER: col = ItemSpriteConfig.ITEM_FERTILISER_COL; break;
            case PICKAXE: col = ItemSpriteConfig.ITEM_PICKAXE_COL; break;
            case SHOVEL: col = ItemSpriteConfig.ITEM_SHOVEL_COL; break;
            case SWORD: col = ItemSpriteConfig.ITEM_SWORD_COL; break;
            case SHEARS: col = ItemSpriteConfig.ITEM_SCISSORS_COL; break;
            case MILK_BUCKET: col = ItemSpriteConfig.ITEM_MILK_BUCKET_COL; break;
            case FULL_MILK_BUCKET: col = ItemSpriteConfig.ITEM_FULL_MILK_BUCKET_COL; break;
            case EGG: col = ItemSpriteConfig.ITEM_EGG_COL; break;
            case WOOD: col = ItemSpriteConfig.ITEM_WOOD_COL; break;
            case WOOL: col = ItemSpriteConfig.ITEM_WOOL_COL; break;
            case MEAT_CHICKEN: col = ItemSpriteConfig.ITEM_MEAT_CHICKEN_COL; break;
            case MEAT_COW: col = ItemSpriteConfig.ITEM_MEAT_COW_COL; break;
            case MEAT_PIG: col = ItemSpriteConfig.ITEM_MEAT_PIG_COL; break;
            case MEAT_SHEEP: col = ItemSpriteConfig.ITEM_MEAT_SHEEP_COL; break;
            case ENERGY_DRINK: col = ItemSpriteConfig.ITEM_ENERGY_DRINK_COL; break;
            case SUPER_FEED: col = ItemSpriteConfig.ITEM_SUPER_FEED_COL; break;

            // Xử lý icon túi hạt giống cho tất cả các loại SEEDS
            default: col = ItemSpriteConfig.ITEM_SEEDS_BAGS_COL; break;
        }

        if (col < 0) return null;

        Image sheet = getTexture(AssetPaths.ITEMS_SHEET);
        return clipItemFromSheet(sheet, col, 0);
    }

    /**
     * Hàm tiện ích để cắt ảnh 32x32 từ một sheet
     */
    private Image clipItemFromSheet(Image sheet, int col, int row) {
        if (sheet == null) return null;
        PixelReader reader = sheet.getPixelReader();
        double w = ItemSpriteConfig.ITEM_SPRITE_WIDTH;
        double h = ItemSpriteConfig.ITEM_SPRITE_HEIGHT;

        int x = (int) (col * w);
        int y = (int) (row * h);

        if (x < 0 || y < 0 || x + w > sheet.getWidth() || y + h > sheet.getHeight()) return null;
        return new WritableImage(reader, x, y, (int) w, (int) h);
    }

    /**
     * Tải tất cả texture động vật
     */
    private void loadAnimalTextures() {
        // Tải spritesheet cho từng loại động vật
        for (AnimalType animalType : AnimalType.values()) {
            getTexture(animalType.getAssetPath());
        }
    }

    /**
     * Lấy sprite của động vật dựa trên type và direction
     * @param animalType Loại động vật
     * @param direction Hướng (0: Down, 1: Right, 2: Left, 3: Up)
     * @param action Hành động (IDLE, WALK, EAT)
     * @param frameIndex Chỉ số frame hoạt hình hiện tại (được tính theo FPS ở ngoài)
     * @return Image sprite của động vật
     */
    public Image getAnimalTexture(AnimalType animalType, int direction, Animal.Action action, int frameIndex) {
        if (animalType == null) return null;

        // Tạo key cache: type_direction_action_frame (thêm frame index để cache từng frame riêng)
        // Sử dụng frameIndex truyền vào thay vì hardcode 0
        String key = animalType.name() + "_" + direction + "_" + action.name() + "_" + frameIndex;

        return spriteCache.computeIfAbsent(key, k -> {
            Image animalSheet = getTexture(animalType.getAssetPath());
            if (animalSheet == null) return null;

            PixelReader reader = animalSheet.getPixelReader();
            double spriteSize = animalType.getSpriteSize();

            int row = 0;
            int col = 0;

            // Xử lý riêng cho EGG_ENTITY (trứng đặt dưới đất)
            if (animalType == AnimalType.EGG_ENTITY) {
                // EGG_ENTITY nằm ở hàng 4 (Idle Down theo logic Standard)
                row = AnimalConfig.STANDARD_ROW_IDLE_DOWN;
                // Lấy 1 trong 2 frame trứng (cột 4 và 5)
                // Sử dụng hằng số mới từ AnimalConfig
                int eggFrameStart = AnimalConfig.EGG_FRAME_START_INDEX;
                // Sử dụng random để chọn biến thể (lấy số nguyên ngẫu nhiên nhỏ hơn 2)
                col = eggFrameStart + ThreadLocalRandom.current().nextInt(2);
            }
            // Xử lý riêng cho CHICKEN
            else if (animalType == AnimalType.CHICKEN) {
                // Phân loại hướng thành 2 nhóm
                // Nhóm A (Trái/Xuống): direction = 2 (LEFT) hoặc 0 (DOWN)
                // Nhóm B (Phải/Trên): direction = 1 (RIGHT) hoặc 3 (UP)
                boolean isGroupA = (direction == 2 || direction == 0); // LEFT hoặc DOWN

                if (action == Animal.Action.IDLE) {
                    // IDLE: Nhóm A dùng Hàng 0 (8 frames), Nhóm B dùng Hàng 1 (8 frames)
                    // [SỬA] Sử dụng constant từ AnimalConfig
                    row = isGroupA ? AnimalConfig.CHICKEN_ROW_IDLE_LEFT : AnimalConfig.CHICKEN_ROW_IDLE_RIGHT;
                } else if (action == Animal.Action.WALK) {
                    // WALK: Nhóm A dùng Hàng 2 (4 frames), Nhóm B dùng Hàng 3 (4 frames)
                    // [SỬA] Sử dụng constant từ AnimalConfig
                    row = isGroupA ? AnimalConfig.CHICKEN_ROW_WALK_LEFT : AnimalConfig.CHICKEN_ROW_WALK_RIGHT;
                } else {
                    // EAT: Fallback về IDLE
                    row = isGroupA ? AnimalConfig.CHICKEN_ROW_IDLE_LEFT : AnimalConfig.CHICKEN_ROW_IDLE_RIGHT;
                }
                // Sử dụng frameIndex truyền vào
                col = frameIndex;
            }
            // Xử lý cho các động vật khác & Gà con (Standard layout)
            else {
                // Sử dụng switch với các constant từ AnimalConfig thay vì tính toán thủ công
                if (action == Animal.Action.WALK) {
                    // WALK: Dùng 4 hàng đầu (Row 0-3)
                    switch (direction) {
                        case 0: row = AnimalConfig.STANDARD_ROW_WALK_DOWN; break; // Down
                        case 3: row = AnimalConfig.STANDARD_ROW_WALK_UP; break;   // Up
                        case 2: row = AnimalConfig.STANDARD_ROW_WALK_LEFT; break; // Left
                        case 1: row = AnimalConfig.STANDARD_ROW_WALK_RIGHT; break;// Right
                        default: row = AnimalConfig.STANDARD_ROW_WALK_DOWN; break;
                    }
                } else { // IDLE or EAT
                    // IDLE: Dùng 4 hàng sau (Row 4-7)
                    switch (direction) {
                        case 0: row = AnimalConfig.STANDARD_ROW_IDLE_DOWN; break; // Down
                        case 3: row = AnimalConfig.STANDARD_ROW_IDLE_UP; break;   // Up
                        case 2: row = AnimalConfig.STANDARD_ROW_IDLE_LEFT; break; // Left
                        case 1: row = AnimalConfig.STANDARD_ROW_IDLE_RIGHT; break;// Right
                        default: row = AnimalConfig.STANDARD_ROW_IDLE_DOWN; break;
                    }
                }
                // Sử dụng frameIndex truyền vào
                col = frameIndex;
            }

            int x = (int) (col * spriteSize);
            int y = (int) (row * spriteSize);

            if (x < 0 || y < 0 || x + spriteSize > animalSheet.getWidth() || y + spriteSize > animalSheet.getHeight()) {
                // Nếu không đủ frame, dùng frame đầu tiên
                x = 0;
                // Giữ nguyên y (row) để không bị sai hướng
                y = (int) (row * spriteSize);
            }

            return new WritableImage(reader, x, y, (int) spriteSize, (int) spriteSize);
        });
    }

    /**
     * Lấy sprite của cây tự nhiên dựa trên growth stage
     */
    public Image getTreeTexture(TreeData treeData) {
        if (treeData == null) {
            return null;
        }

        // Nếu đã bị chặt (stump), hiển thị frame 4
        if (treeData.getChopCount() > 0) {
            String key = "tree_stump";
            return spriteCache.computeIfAbsent(key, k -> {
                Image treeSheet = getTexture(AssetPaths.TREE_SHEET);
                if (treeSheet == null) return null;

                PixelReader reader = treeSheet.getPixelReader();
                double w = TreeConfig.TREE_SPRITE_WIDTH;
                double h = TreeConfig.TREE_SPRITE_HEIGHT;

                // Frame 4 là stump
                int x = (int) (TreeConfig.TREE_STUMP_FRAME_INDEX * w);
                int y = 0; // Chỉ có 1 hàng trong spritesheet

                if (x < 0 || y < 0 || x + w > treeSheet.getWidth() || y + h > treeSheet.getHeight()) return null;

                return new WritableImage(reader, x, y, (int) w, (int) h);
            });
        }

        // Nếu chưa bị chặt, hiển thị theo growth stage
        int stage = treeData.getGrowthStage();
        String key = "tree_" + stage;

        return spriteCache.computeIfAbsent(key, k -> {
            Image treeSheet = getTexture(AssetPaths.TREE_SHEET);
            if (treeSheet == null) return null;

            PixelReader reader = treeSheet.getPixelReader();
            double w = TreeConfig.TREE_SPRITE_WIDTH;
            double h = TreeConfig.TREE_SPRITE_HEIGHT;

            // Cây có 5 frame: 0 (seed/sprout), 1 (cây nhỏ), 2 (cây trung bình), 3 (cây trưởng thành), 4 (stump)
            // Cây phát triển từ stage 0 đến stage 3
            int x = (int) (stage * w);
            int y = 0; // Chỉ có 1 hàng trong spritesheet

            if (x < 0 || y < 0 || x + w > treeSheet.getWidth() || y + h > treeSheet.getHeight()) return null;

            return new WritableImage(reader, x, y, (int) w, (int) h);
        });
    }

    /**
     * Lấy sprite của hàng rào dựa trên pattern và trạng thái mở/đóng
     */
    public Image getFenceTexture(FenceData fenceData) {
        if (fenceData == null) return null;

        int width = (int) FenceConfig.FENCE_SPRITE_WIDTH;
        int height = (int) FenceConfig.FENCE_SPRITE_HEIGHT;

        // Nếu rào đang MỞ (Cổng) -> Lấy hình cái cọc đơn
        if (fenceData.isOpen()) {
            return spriteCache.computeIfAbsent("fence_open", k -> {
                Image fenceSheet = getTexture(AssetPaths.FENCE_SHEET);
                return new WritableImage(fenceSheet.getPixelReader(),
                        0, 3 * height, width, height); // Cột 0, Hàng 3
            });
        }

        // Nếu rào ĐÓNG -> Map theo Pattern 0-15
        /** Hệ thống này sử dụng một kỹ thuật trong lập trình game gọi là Bitmasking (Mặt nạ bit).
         * Mỗi ô hàng rào sẽ kiểm tra 4 hướng xung quanh nó: Trên (Up), Phải (Right), Dưới (Down), và Trái (Left).
         * Mỗi hướng có 2 trạng thái: Có kết nối (1) hoặc Không kết nối (0).
         * Với 4 hướng, ta có 2^4 = 16 sự kết hợp khác nhau.
         * Để tính toán ra con số pattern từ 0 đến 15, mỗi hướng được gán một "trọng số" (giá trị lũy thừa của 2):
         * Trên (Up): 1 (2^0); Phải (Right): 2 (2^1); Dưới (Down): 4 (2^2); Trái (Left): 8 (2^3)
         * Nói chung là chuyển số từ nhị phân về thập phân.
        */
        int pattern = fenceData.getTilePattern();
        String key = "fence_" + pattern;

        return spriteCache.computeIfAbsent(key, k -> {
            Image fenceSheet = getTexture(AssetPaths.FENCE_SHEET);
            PixelReader reader = fenceSheet.getPixelReader();

            int col, row;

            switch (pattern) {
                // --- NHÓM 1: CÁC ĐẦU MÚT & ĐƯỜNG THẲNG (CỘT 0 & HÀNG 0) ---
                case 0:  col = 0; row = 3; break; // Cọc đơn (Không nối gì)

                case 1:  col = 0; row = 2; break; // Chỉ nối Lên (Đầu dưới)
                case 4:  col = 0; row = 0; break; // Chỉ nối Xuống (Đầu trên)
                case 5:  col = 0; row = 1; break; // Dọc (Lên + Xuống)

                case 2:  col = 1; row = 0; break; // Chỉ nối Phải (Đầu trái)
                case 8:  col = 3; row = 0; break; // Chỉ nối Trái (Đầu phải)
                case 10: col = 2; row = 0; break; // Ngang (Trái + Phải)

                // --- NHÓM 2: CÁC GÓC (KHỐI 3x3 - BỐN GÓC) ---
                case 6:  col = 1; row = 1; break; // Góc Trên-Trái (Nối Phải+Xuống)
                case 12: col = 3; row = 1; break; // Góc Trên-Phải (Nối Trái+Xuống)
                case 3:  col = 1; row = 3; break; // Góc Dưới-Trái (Nối Phải+Lên)
                case 9:  col = 3; row = 3; break; // Góc Dưới-Phải (Nối Trái+Lên)

                // --- NHÓM 3: NGÃ BA (CHỮ T) ---
                case 7:  col = 1; row = 2; break; // T quay Phải (Lên+Xuống+Phải)
                case 11: col = 2; row = 3; break; // T quay Lên (Trái+Phải+Lên) - Úp ngược
                case 13: col = 3; row = 2; break; // T quay Trái (Lên+Xuống+Trái)
                case 14: col = 2; row = 1; break; // T quay Xuống (Trái+Phải+Xuống)

                // --- NHÓM 4: NGÃ TƯ (DẤU +) ---
                case 15: col = 2; row = 2; break; // Nối cả 4 hướng

                default: col = 0; row = 3; break; // Fallback về cọc đơn
            }

            // Cắt ảnh 64x64
            return new WritableImage(reader, col * width, row * height, width, height);
        });
    }
}