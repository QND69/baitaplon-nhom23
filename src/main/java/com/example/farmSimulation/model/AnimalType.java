package com.example.farmSimulation.model;

import com.example.farmSimulation.config.AnimalConfig;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * Định nghĩa các loại động vật trong game.
 * Chứa thông tin cố định như tên, asset path, kích thước, sản phẩm, v.v.
 */
public enum AnimalType {
    // Định nghĩa: Tên, Asset path, Kích thước sprite, Hitbox width/height, Tốc độ, Thức ăn, Sản phẩm, Thời gian sản phẩm
    CHICKEN("Chicken", 
            "/assets/images/entities/animal/chicken_32x32.png",
            AnimalConfig.SMALL_ANIMAL_SIZE,
            AnimalConfig.SMALL_ANIMAL_HITBOX_WIDTH,
            AnimalConfig.SMALL_ANIMAL_HITBOX_HEIGHT,
            AnimalConfig.BASE_MOVEMENT_SPEED,
            Arrays.asList(ItemType.SEEDS_STRAWBERRY, ItemType.SEEDS_DAIKON, ItemType.SEEDS_POTATO, ItemType.SEEDS_CARROT, 
                         ItemType.SEEDS_WATERMELON, ItemType.SEEDS_TOMATO, ItemType.SEEDS_WHEAT, ItemType.SEEDS_CORN, ItemType.SUPER_FEED),
            ItemType.EGG,
            AnimalConfig.BASE_PRODUCTION_TIME_MS,
            0L), // Không có thời gian trưởng thành (đã trưởng thành)
    
    COW("Cow",
        "/assets/images/entities/animal/cow_64x64.png",
        AnimalConfig.LARGE_ANIMAL_SIZE,
        AnimalConfig.LARGE_ANIMAL_HITBOX_WIDTH,
        AnimalConfig.LARGE_ANIMAL_HITBOX_HEIGHT,
        AnimalConfig.BASE_MOVEMENT_SPEED * 0.8, // Bò di chuyển chậm hơn
        Arrays.asList(ItemType.CORN, ItemType.WHEAT, ItemType.SUPER_FEED),
        ItemType.MILK,
        AnimalConfig.BASE_PRODUCTION_TIME_MS * 2L, // Sản xuất sữa lâu hơn
        0L),
    
    PIG("Pig",
        "/assets/images/entities/animal/pig_64x64.png",
        AnimalConfig.LARGE_ANIMAL_SIZE,
        AnimalConfig.LARGE_ANIMAL_HITBOX_WIDTH,
        AnimalConfig.LARGE_ANIMAL_HITBOX_HEIGHT,
        AnimalConfig.BASE_MOVEMENT_SPEED * 0.9,
        Arrays.asList(ItemType.STRAWBERRY, ItemType.DAIKON, ItemType.POTATO, ItemType.CARROT, 
                      ItemType.WATERMELON, ItemType.TOMATO, ItemType.CORN, ItemType.SUPER_FEED), // Lợn ăn tạp (tất cả sản phẩm trừ WHEAT)
        null, // Lợn không có sản phẩm
        AnimalConfig.BASE_PRODUCTION_TIME_MS,
        0L),
    
    SHEEP("Sheep",
          "/assets/images/entities/animal/sheep_64x64.png",
          AnimalConfig.LARGE_ANIMAL_SIZE,
          AnimalConfig.LARGE_ANIMAL_HITBOX_WIDTH,
          AnimalConfig.LARGE_ANIMAL_HITBOX_HEIGHT,
          AnimalConfig.BASE_MOVEMENT_SPEED * 0.85,
          Arrays.asList(ItemType.CORN, ItemType.WHEAT, ItemType.SUPER_FEED),
          ItemType.WOOL,
          (long)(AnimalConfig.BASE_PRODUCTION_TIME_MS * 1.5), // Sản xuất len
          0L),
    
    BABY_COW("Baby Cow",
             "/assets/images/entities/animal/cow_32x32.png",
             AnimalConfig.SMALL_ANIMAL_SIZE,
             AnimalConfig.SMALL_ANIMAL_HITBOX_WIDTH,
             AnimalConfig.SMALL_ANIMAL_HITBOX_HEIGHT,
             AnimalConfig.BASE_MOVEMENT_SPEED * 0.7, // Di chuyển chậm hơn
            Arrays.asList(ItemType.CORN, ItemType.WHEAT, ItemType.SUPER_FEED),
             null, // Bò con chưa cho sữa
             AnimalConfig.BASE_PRODUCTION_TIME_MS,
             AnimalConfig.BABY_ANIMAL_GROWTH_TIME_MS), // Thời gian trưởng thành
    
    BABY_PIG("Baby Pig",
             "/assets/images/entities/animal/pig_32x32.png",
             AnimalConfig.SMALL_ANIMAL_SIZE,
             AnimalConfig.SMALL_ANIMAL_HITBOX_WIDTH,
             AnimalConfig.SMALL_ANIMAL_HITBOX_HEIGHT,
             AnimalConfig.BASE_MOVEMENT_SPEED * 0.7, // Di chuyển chậm hơn
             Arrays.asList(ItemType.STRAWBERRY, ItemType.DAIKON, ItemType.POTATO, ItemType.CARROT, 
                          ItemType.WATERMELON, ItemType.TOMATO, ItemType.CORN, ItemType.SUPER_FEED), // Lợn con ăn tạp (tất cả sản phẩm trừ WHEAT)
             null, // Lợn con không có sản phẩm
             AnimalConfig.BASE_PRODUCTION_TIME_MS,
             AnimalConfig.BABY_ANIMAL_GROWTH_TIME_MS), // Thời gian trưởng thành
    
    BABY_SHEEP("Baby Sheep",
               "/assets/images/entities/animal/sheep_32x32.png",
               AnimalConfig.SMALL_ANIMAL_SIZE,
               AnimalConfig.SMALL_ANIMAL_HITBOX_WIDTH,
               AnimalConfig.SMALL_ANIMAL_HITBOX_HEIGHT,
               AnimalConfig.BASE_MOVEMENT_SPEED * 0.7, // Di chuyển chậm hơn
               Arrays.asList(ItemType.CORN, ItemType.WHEAT, ItemType.SUPER_FEED),
               null, // Cừu con chưa cho len
               AnimalConfig.BASE_PRODUCTION_TIME_MS,
               AnimalConfig.BABY_ANIMAL_GROWTH_TIME_MS), // Thời gian trưởng thành
    
    BABY_CHICKEN("Baby Chicken",
                 "/assets/images/entities/animal/babychick&egg_16x16.png",
                 AnimalConfig.BABY_CHICKEN_SIZE, // Kích thước 16x16 từ file PNG
                 AnimalConfig.BABY_CHICKEN_HITBOX_WIDTH,
                 AnimalConfig.BABY_CHICKEN_HITBOX_HEIGHT,
                 AnimalConfig.BASE_MOVEMENT_SPEED * 0.7, // Di chuyển chậm hơn
                 Arrays.asList(ItemType.SEEDS_STRAWBERRY, ItemType.SEEDS_DAIKON, ItemType.SEEDS_POTATO, ItemType.SEEDS_CARROT, 
                              ItemType.SEEDS_WATERMELON, ItemType.SEEDS_TOMATO, ItemType.SEEDS_WHEAT, ItemType.SEEDS_CORN, ItemType.SUPER_FEED),
                 null, // Gà con chưa đẻ trứng
                 AnimalConfig.BASE_PRODUCTION_TIME_MS,
                 AnimalConfig.BABY_CHICKEN_GROWTH_TIME_MS), // Thời gian trưởng thành
    
    EGG_ENTITY("Egg",
               "/assets/images/entities/animal/babychick&egg_16x16.png",
               AnimalConfig.BABY_CHICKEN_SIZE, // Cùng kích thước với gà con (16x16) để nở đúng
               AnimalConfig.BABY_CHICKEN_HITBOX_WIDTH,
               AnimalConfig.BABY_CHICKEN_HITBOX_HEIGHT,
               0.0, // Trứng không di chuyển
               Arrays.asList(), // Trứng không ăn
               null,
               0L,
               AnimalConfig.EGG_HATCH_TIME_MS); // Thời gian nở

    @Getter private final String displayName;
    @Getter private final String assetPath;
    @Getter private final double spriteSize;
    @Getter private final double hitboxWidth;
    @Getter private final double hitboxHeight;
    @Getter private final double movementSpeed;
    @Getter private final List<ItemType> acceptedFood;
    @Getter private final ItemType product; // Sản phẩm thu hoạch được (null nếu không có)
    @Getter private final long productionTimeMs; // Thời gian để tạo sản phẩm
    @Getter private final long growthTimeMs; // Thời gian trưởng thành (0 nếu đã trưởng thành)

    AnimalType(String displayName, String assetPath, double spriteSize, 
               double hitboxWidth, double hitboxHeight, double movementSpeed,
               List<ItemType> acceptedFood, ItemType product, long productionTimeMs, long growthTimeMs) {
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
}

