package com.example.farmSimulation.model;

import com.example.farmSimulation.config.AnimalConfig;
import com.example.farmSimulation.config.AssetPaths;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * Định nghĩa các loại động vật trong game.
 * Chứa thông tin cố định như tên, asset path, kích thước, sản phẩm, v.v.
 */
public enum AnimalType {
    // Định nghĩa: Tên, Asset path, Kích thước sprite (để cắt ảnh), Hitbox width/height,
    // Tốc độ, Thức ăn, Sản phẩm, Thời gian sản phẩm, Thời gian lớn, Scale (để vẽ)
    CHICKEN("Chicken",
            AssetPaths.CHICKEN_SHEET,
            AnimalConfig.SPRITE_SIZE_CHICKEN,
            AnimalConfig.SMALL_ANIMAL_HITBOX_WIDTH,
            AnimalConfig.SMALL_ANIMAL_HITBOX_HEIGHT,
            AnimalConfig.BASE_MOVEMENT_SPEED,
            Arrays.asList(ItemType.SEEDS_STRAWBERRY, ItemType.SEEDS_DAIKON, ItemType.SEEDS_POTATO, ItemType.SEEDS_CARROT,
                    ItemType.SEEDS_WATERMELON, ItemType.SEEDS_TOMATO, ItemType.SEEDS_WHEAT, ItemType.SEEDS_CORN, ItemType.SUPER_FEED),
            ItemType.EGG,
            AnimalConfig.BASE_PRODUCTION_TIME_MS,
            0L,
            AnimalConfig.SCALE_CHICKEN), // [SỬA] Dùng scale riêng

    COW("Cow",
            AssetPaths.COW_SHEET, // 96x96
            AnimalConfig.SPRITE_SIZE_COW,
            AnimalConfig.LARGE_ANIMAL_HITBOX_WIDTH,
            AnimalConfig.LARGE_ANIMAL_HITBOX_HEIGHT,
            AnimalConfig.BASE_MOVEMENT_SPEED * 0.8, // Bò di chuyển chậm hơn
            Arrays.asList(ItemType.CORN, ItemType.WHEAT, ItemType.SUPER_FEED),
            ItemType.MILK,
            AnimalConfig.BASE_PRODUCTION_TIME_MS * 2L, // Sản xuất sữa lâu hơn
            0L,
            AnimalConfig.SCALE_COW), // [SỬA] Dùng scale riêng

    PIG("Pig",
            AssetPaths.PIG_SHEET, // 96x96
            AnimalConfig.SPRITE_SIZE_PIG,
            AnimalConfig.LARGE_ANIMAL_HITBOX_WIDTH,
            AnimalConfig.LARGE_ANIMAL_HITBOX_HEIGHT,
            AnimalConfig.BASE_MOVEMENT_SPEED * 0.9,
            Arrays.asList(ItemType.STRAWBERRY, ItemType.DAIKON, ItemType.POTATO, ItemType.CARROT,
                    ItemType.WATERMELON, ItemType.TOMATO, ItemType.CORN, ItemType.SUPER_FEED), // Lợn ăn tạp (tất cả sản phẩm trừ WHEAT)
            null, // Lợn không có sản phẩm
            AnimalConfig.BASE_PRODUCTION_TIME_MS,
            0L,
            AnimalConfig.SCALE_PIG), // [SỬA] Dùng scale riêng

    SHEEP("Sheep",
            AssetPaths.SHEEP_SHEET, // 64x64
            AnimalConfig.SPRITE_SIZE_SHEEP,
            AnimalConfig.LARGE_ANIMAL_HITBOX_WIDTH,
            AnimalConfig.LARGE_ANIMAL_HITBOX_HEIGHT,
            AnimalConfig.BASE_MOVEMENT_SPEED * 0.85,
            Arrays.asList(ItemType.CORN, ItemType.WHEAT, ItemType.SUPER_FEED),
            ItemType.WOOL,
            (long)(AnimalConfig.BASE_PRODUCTION_TIME_MS * 1.5), // Sản xuất len
            0L,
            AnimalConfig.SCALE_SHEEP), // [SỬA] Dùng scale riêng

    BABY_COW("Baby Cow",
            AssetPaths.COW_SHEET, // [DÙNG CHUNG] sheet với bò lớn
            AnimalConfig.SPRITE_SIZE_COW, // [DÙNG KÍCH THƯỚC GỐC] 96 để cắt frame đúng
            AnimalConfig.SMALL_ANIMAL_HITBOX_WIDTH,
            AnimalConfig.SMALL_ANIMAL_HITBOX_HEIGHT,
            AnimalConfig.BASE_MOVEMENT_SPEED * 0.7, // Di chuyển chậm hơn
            Arrays.asList(ItemType.CORN, ItemType.WHEAT, ItemType.SUPER_FEED),
            null, // Bò con chưa cho sữa
            AnimalConfig.BASE_PRODUCTION_TIME_MS,
            AnimalConfig.BABY_ANIMAL_GROWTH_TIME_MS, // Thời gian trưởng thành
            AnimalConfig.SCALE_BABY_COW), // [SCALE] 0.5 để vẽ nhỏ lại

    BABY_PIG("Baby Pig",
            AssetPaths.PIG_SHEET, // [DÙNG CHUNG] sheet với lợn lớn
            AnimalConfig.SPRITE_SIZE_PIG, // [DÙNG KÍCH THƯỚC GỐC] 96
            AnimalConfig.SMALL_ANIMAL_HITBOX_WIDTH,
            AnimalConfig.SMALL_ANIMAL_HITBOX_HEIGHT,
            AnimalConfig.BASE_MOVEMENT_SPEED * 0.7, // Di chuyển chậm hơn
            Arrays.asList(ItemType.STRAWBERRY, ItemType.DAIKON, ItemType.POTATO, ItemType.CARROT,
                    ItemType.WATERMELON, ItemType.TOMATO, ItemType.CORN, ItemType.SUPER_FEED), // Lợn con ăn tạp (tất cả sản phẩm trừ WHEAT)
            null, // Lợn con không có sản phẩm
            AnimalConfig.BASE_PRODUCTION_TIME_MS,
            AnimalConfig.BABY_ANIMAL_GROWTH_TIME_MS, // Thời gian trưởng thành
            AnimalConfig.SCALE_BABY_PIG), // [SCALE] 0.5

    BABY_SHEEP("Baby Sheep",
            AssetPaths.SHEEP_SHEET, // [DÙNG CHUNG] sheet với cừu lớn
            AnimalConfig.SPRITE_SIZE_SHEEP, // [DÙNG KÍCH THƯỚC GỐC] 64
            AnimalConfig.SMALL_ANIMAL_HITBOX_WIDTH,
            AnimalConfig.SMALL_ANIMAL_HITBOX_HEIGHT,
            AnimalConfig.BASE_MOVEMENT_SPEED * 0.7, // Di chuyển chậm hơn
            Arrays.asList(ItemType.CORN, ItemType.WHEAT, ItemType.SUPER_FEED),
            null, // Cừu con chưa cho len
            AnimalConfig.BASE_PRODUCTION_TIME_MS,
            AnimalConfig.BABY_ANIMAL_GROWTH_TIME_MS, // Thời gian trưởng thành
            AnimalConfig.SCALE_BABY_SHEEP), // [SCALE] 0.5

    BABY_CHICKEN("Baby Chicken",
            AssetPaths.BABY_CHICKEN_EGG_SHEET, // [SỬA] Dùng sheet riêng cho Gà con (chung với trứng)
            AnimalConfig.SPRITE_SIZE_BABY_CHICKEN_EGG, // [SỬA] Size 32x32
            AnimalConfig.BABY_CHICKEN_HITBOX_WIDTH,
            AnimalConfig.BABY_CHICKEN_HITBOX_HEIGHT,
            AnimalConfig.BASE_MOVEMENT_SPEED * 0.7, // Di chuyển chậm hơn
            Arrays.asList(ItemType.SEEDS_STRAWBERRY, ItemType.SEEDS_DAIKON, ItemType.SEEDS_POTATO, ItemType.SEEDS_CARROT,
                    ItemType.SEEDS_WATERMELON, ItemType.SEEDS_TOMATO, ItemType.SEEDS_WHEAT, ItemType.SEEDS_CORN, ItemType.SUPER_FEED),
            null, // Gà con chưa đẻ trứng
            AnimalConfig.BASE_PRODUCTION_TIME_MS,
            AnimalConfig.BABY_CHICKEN_GROWTH_TIME_MS, // Thời gian trưởng thành
            AnimalConfig.SCALE_BABY_CHICKEN), // [SỬA] Scale 1.0 (vì ảnh gốc 32px nhỏ sẵn)

    EGG_ENTITY("Egg",
            AssetPaths.BABY_CHICKEN_EGG_SHEET, // Trứng vẫn dùng sheet riêng 32x32
            AnimalConfig.SPRITE_SIZE_BABY_CHICKEN_EGG, // [SỬA] Size 32x32 (dùng chung hằng số)
            AnimalConfig.BABY_CHICKEN_HITBOX_WIDTH,
            AnimalConfig.BABY_CHICKEN_HITBOX_HEIGHT,
            0.0, // Trứng không di chuyển
            Arrays.asList(), // Trứng không ăn
            null,
            0L,
            AnimalConfig.EGG_HATCH_TIME_MS, // Thời gian nở
            AnimalConfig.SCALE_EGG); // [SỬA] Dùng scale riêng

    @Getter private final String displayName;
    @Getter private final String assetPath;
    @Getter private final double spriteSize; // Kích thước frame gốc trong ảnh
    @Getter private final double hitboxWidth;
    @Getter private final double hitboxHeight;
    @Getter private final double movementSpeed;
    @Getter private final List<ItemType> acceptedFood;
    @Getter private final ItemType product; // Sản phẩm thu hoạch được (null nếu không có)
    @Getter private final long productionTimeMs; // Thời gian để tạo sản phẩm
    @Getter private final long growthTimeMs; // Thời gian trưởng thành (0 nếu đã trưởng thành)
    @Getter private final double scale; // Tỉ lệ scale hình ảnh khi vẽ

    AnimalType(String displayName, String assetPath, double spriteSize,
               double hitboxWidth, double hitboxHeight, double movementSpeed,
               List<ItemType> acceptedFood, ItemType product, long productionTimeMs, long growthTimeMs, double scale) {
        this.displayName = displayName;
        this.assetPath = assetPath;
        this.spriteSize = spriteSize;
        this.hitboxWidth = hitboxWidth;
        this.hitboxHeight = hitboxHeight;
        this.movementSpeed = movementSpeed;
        this.acceptedFood = acceptedFood;
        this.product = product;
        this.productionTimeMs = productionTimeMs;
        this.growthTimeMs = growthTimeMs;
        this.scale = scale;
    }

    /**
     * Kiểm tra xem loại thức ăn này có được chấp nhận không
     */
    public boolean acceptsFood(ItemType food) {
        return acceptedFood.contains(food);
    }

    /**
     * Kiểm tra xem động vật này có thể tạo sản phẩm không
     */
    public boolean canProduce() {
        return product != null;
    }

    /**
     * Kiểm tra xem động vật này có thể trưởng thành không
     */
    public boolean canGrow() {
        return growthTimeMs > 0;
    }

    // [MỚI] Helper để lấy loại con non tương ứng (cho việc Breeding)
    public AnimalType getBabyType() {
        switch (this) {
            case COW: return BABY_COW;
            case PIG: return BABY_PIG;
            case SHEEP: return BABY_SHEEP;
            case CHICKEN: return BABY_CHICKEN; // Hoặc EGG_ENTITY tùy logic
            default: return null;
        }
    }
}