package com.example.farmSimulation.model;

import com.example.farmSimulation.config.AnimalConfig;
import com.example.farmSimulation.config.GameLogicConfig; // [MỚI]
import com.example.farmSimulation.config.ItemSpriteConfig; // [MỚI]
import com.example.farmSimulation.config.WorldConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Class quản lý hệ thống động vật.
 * Xử lý AI, di chuyển, đói, sản phẩm, và sinh trưởng của động vật.
 */
public class AnimalManager {
    private final WorldMap worldMap;
    private final CollisionManager collisionManager;
    private final List<Animal> animals;
    private final Random random;

    // Thời gian update lần cuối (để sử dụng interval)
    private long lastLogicUpdateTimeMs = 0; // [SỬA] Đổi tên biến để rõ nghĩa hơn: Chỉ dùng cho Logic chậm

    // Thời gian update di chuyển lần cuối (nanoTime) - để tính deltaTime cho di chuyển
    private long lastMovementUpdateTime = 0;

    public AnimalManager(WorldMap worldMap, CollisionManager collisionManager) {
        this.worldMap = worldMap;
        this.collisionManager = collisionManager;
        this.animals = new ArrayList<>();
        this.random = new Random();
    }

    /**
     * Thêm động vật mới vào danh sách
     */
    public void addAnimal(Animal animal) {
        if (animal != null) {
            animals.add(animal);
        }
    }

    /**
     * Xóa động vật khỏi danh sách
     */
    public void removeAnimal(Animal animal) {
        animals.remove(animal);
    }

    /**
     * Lấy danh sách tất cả động vật
     */
    public List<Animal> getAnimals() {
        return new ArrayList<>(animals); // Trả về bản sao để tránh concurrent modification
    }

    /**
     * Tìm động vật tại vị trí (để tương tác)
     */
    public Animal getAnimalAt(double worldX, double worldY, double range) {
        for (Animal animal : animals) {
            if (animal.isDead()) continue;

            double dx = animal.getX() - worldX;
            double dy = animal.getY() - worldY;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance <= range) {
                return animal;
            }
        }
        return null;
    }

    /**
     * Update logic động vật: AI, di chuyển, đói, sản phẩm, sinh trưởng
     */
    public boolean updateAnimals(long currentTime) {
        boolean needsRedraw = false;
        long currentTimeMs = currentTime / 1_000_000;

        // [QUAN TRỌNG] Tách Logic (chạy chậm) và Movement (chạy mỗi frame)
        // Kiểm tra xem đã đến lúc chạy logic nặng (AI, đói, lớn) chưa
        boolean shouldUpdateLogic = (currentTimeMs - lastLogicUpdateTimeMs >= AnimalConfig.ANIMAL_UPDATE_INTERVAL_MS);

        if (shouldUpdateLogic) {
            lastLogicUpdateTimeMs = currentTimeMs;
        }

        // [SỬA LỖI ĐỘNG VẬT ĐỨNG IM]
        // Tính deltaTime MỘT LẦN cho toàn bộ danh sách, chạy mỗi frame để mượt mà
        double deltaTime = 0.0;
        if (lastMovementUpdateTime > 0) {
            long deltaNanos = currentTime - lastMovementUpdateTime;
            deltaTime = deltaNanos / 1_000_000_000.0;
            // Cap deltaTime để tránh giật khi lag (max 0.1s)
            if (deltaTime > 0.1) deltaTime = 0.1;
        }
        lastMovementUpdateTime = currentTime; // Cập nhật thời gian sau khi tính toán xong

        // [MỚI] Dùng millis cho logic state timer
        long now = System.currentTimeMillis();

        Iterator<Animal> iterator = animals.iterator();
        while (iterator.hasNext()) {
            Animal animal = iterator.next();

            // Xóa động vật đã chết
            if (animal.isDead()) {
                iterator.remove();
                needsRedraw = true;
                continue;
            }

            // --- NHÓM 1: LOGIC NẶNG (Chỉ chạy khi đến lượt - Tiết kiệm CPU) ---
            if (shouldUpdateLogic) {
                // --- PHẦN 1: Cập nhật Đói ---
                updateHunger(animal, currentTime);

                // --- PHẦN 2: Kiểm tra Chết đói ---
                if (checkStarvation(animal, currentTime)) {
                    // Logic rơi thịt khi chết đói (giữ nguyên code cũ)
                    handleStarvationDrop(animal); // Refactor đoạn rơi thịt vào hàm riêng cho gọn
                    animal.setDead(true);
                    needsRedraw = true;
                    continue;
                }

                // --- PHẦN 3: Sinh trưởng (Trứng -> Gà con -> Gà) ---
                if (updateGrowth(animal, currentTime)) {
                    needsRedraw = true;
                }

                // --- PHẦN 5: Sản phẩm (Sữa, Len, Trứng) ---
                if (updateProduction(animal, currentTime)) {
                    needsRedraw = true;
                }

                // --- PHẦN 6: Cập nhật Tuổi ---
                animal.setAge((int) animal.getAgeInSeconds());
            }

            // --- NHÓM 2: MOVEMENT & AI DECISION (Chạy mỗi frame để mượt) ---
            // Cần chạy mỗi frame để:
            // 1. Cập nhật Timer AI (actionEndTime) chính xác
            // 2. Di chuyển pixel mượt mà theo deltaTime

            if (updateMovement(animal, now, deltaTime)) {
                needsRedraw = true;
            }
        }

        return needsRedraw;
    }

    /**
     * Xử lý logic rơi thịt khi chết đói (Tách ra từ hàm updateAnimals để gọn)
     */
    private void handleStarvationDrop(Animal animal) {
        int meatAmount = animal.calculateMeatDrop();
        if (meatAmount > 0 && worldMap != null) {
            ItemType meatType = animal.getMeatType();
            if (meatType != null) {
                // [FIX LỆCH TỌA ĐỘ THỊT CHO CHẾT ĐÓI]
                double visualCenterY = animal.getY() - (animal.getType().getSpriteSize() / 2.0);

                int idealTileCol = (int) Math.floor(animal.getX() / WorldConfig.TILE_SIZE);
                int idealTileRow = (int) Math.floor(visualCenterY / WorldConfig.TILE_SIZE);

                // Tính toán offset gốc
                double targetItemX = animal.getX() - (ItemSpriteConfig.ITEM_SPRITE_WIDTH / 2.0);
                double targetItemY = visualCenterY - (ItemSpriteConfig.ITEM_SPRITE_HEIGHT / 2.0);

                double originalOffsetX = targetItemX - (idealTileCol * WorldConfig.TILE_SIZE);
                double originalOffsetY = targetItemY - (idealTileRow * WorldConfig.TILE_SIZE);

                // [SỬA LỖI OVERWRITE] Tìm ô trống xung quanh để đặt thịt
                int searchRadius = GameLogicConfig.ITEM_DROP_SEARCH_RADIUS;
                int finalCol = -1;
                int finalRow = -1;
                boolean foundSpot = false;

                // 1. Kiểm tra ô lý tưởng trước
                TileData idealTile = worldMap.getTileData(idealTileCol, idealTileRow);
                if (idealTile.getGroundItem() == null) {
                    finalCol = idealTileCol;
                    finalRow = idealTileRow;
                    foundSpot = true;
                } else if (idealTile.getGroundItem() == meatType) {
                    finalCol = idealTileCol;
                    finalRow = idealTileRow;
                    foundSpot = true;
                } else {
                    // Ô lý tưởng đã có item khác -> Tìm xung quanh
                    for (int r = idealTileRow - searchRadius; r <= idealTileRow + searchRadius; r++) {
                        for (int c = idealTileCol - searchRadius; c <= idealTileCol + searchRadius; c++) {
                            if (r == idealTileRow && c == idealTileCol) continue;
                            TileData checkTile = worldMap.getTileData(c, r);
                            if (checkTile.getGroundItem() == null) {
                                finalCol = c;
                                finalRow = r;
                                foundSpot = true;
                                break;
                            }
                        }
                        if (foundSpot) break;
                    }
                }

                if (!foundSpot) {
                    finalCol = idealTileCol;
                    finalRow = idealTileRow;
                }

                // Đặt thịt xuống đất
                TileData finalTile = worldMap.getTileData(finalCol, finalRow);

                if (finalTile.getGroundItem() == meatType) {
                    finalTile.setGroundItemAmount(finalTile.getGroundItemAmount() + meatAmount);
                } else {
                    finalTile.setGroundItem(meatType);
                    finalTile.setGroundItemAmount(meatAmount);

                    if (finalCol == idealTileCol && finalRow == idealTileRow) {
                        finalTile.setGroundItemOffsetX(originalOffsetX);
                        finalTile.setGroundItemOffsetY(originalOffsetY);
                    } else {
                        finalTile.setDefaultItemOffset();
                        double scatter = GameLogicConfig.ITEM_DROP_SCATTER_RANGE;
                        double jitterX = (random.nextDouble() - 0.5) * scatter;
                        double jitterY = (random.nextDouble() - 0.5) * scatter;
                        finalTile.setGroundItemOffsetX(finalTile.getGroundItemOffsetX() + jitterX);
                        finalTile.setGroundItemOffsetY(finalTile.getGroundItemOffsetY() + jitterY);
                    }
                }

                worldMap.setTileData(finalCol, finalRow, finalTile);
            }
        }
    }

    /**
     * Cập nhật chỉ số đói
     */
    private void updateHunger(Animal animal, long currentTime) {
        long deltaTimeMs = (currentTime - animal.getLastHungerUpdateTime()) / 1_000_000;
        if (deltaTimeMs > 0) {
            // Giảm đói theo thời gian
            double hungerDecrease = (deltaTimeMs / 1000.0) * AnimalConfig.HUNGER_DECREASE_RATE;
            double newHunger = Math.max(0, animal.getHunger() - hungerDecrease);
            animal.setHunger(newHunger);
            animal.setLastHungerUpdateTime(currentTime);

            // Cập nhật thời gian bắt đầu đói
            if (animal.isHungry() && animal.getStarvationStartTime() == 0) {
                animal.setStarvationStartTime(currentTime);
            } else if (!animal.isHungry()) {
                animal.setStarvationStartTime(0); // Reset nếu không đói nữa
            }
        }
    }

    /**
     * Kiểm tra chết đói
     */
    private boolean checkStarvation(Animal animal, long currentTime) {
        if (animal.getStarvationStartTime() > 0) {
            long starvationDuration = (currentTime - animal.getStarvationStartTime()) / 1_000_000;
            if (starvationDuration >= AnimalConfig.STARVATION_TIME_MS) {
                return true; // Chết đói
            }
        }
        return false;
    }

    /**
     * Cập nhật sinh trưởng (Trứng -> Gà con -> Gà)
     */
    private boolean updateGrowth(Animal animal, long currentTime) {
        if (!animal.canGrow()) {
            return false;
        }

        long ageInSeconds = animal.getAgeInSeconds();
        long growthTimeSeconds = animal.getType().getGrowthTimeMs() / 1000;

        if (ageInSeconds >= growthTimeSeconds) {
            // Trưởng thành
            if (animal.getType() == AnimalType.EGG_ENTITY) {
                // Trứng nở thành gà con
                animal.setType(AnimalType.BABY_CHICKEN);
                animal.setSpawnTime(currentTime); // Reset spawn time
                return true;
            } else if (animal.getType() == AnimalType.BABY_CHICKEN) {
                // Gà con trưởng thành
                animal.setType(AnimalType.CHICKEN);
                animal.setSpawnTime(currentTime); // Reset spawn time
                return true;
            } else if (animal.getType() == AnimalType.BABY_COW) {
                // Bò con trưởng thành
                animal.setType(AnimalType.COW);
                animal.setSpawnTime(currentTime); // Reset spawn time
                return true;
            } else if (animal.getType() == AnimalType.BABY_PIG) {
                // Lợn con trưởng thành
                animal.setType(AnimalType.PIG);
                animal.setSpawnTime(currentTime); // Reset spawn time
                return true;
            } else if (animal.getType() == AnimalType.BABY_SHEEP) {
                // Cừu con trưởng thành
                animal.setType(AnimalType.SHEEP);
                animal.setSpawnTime(currentTime); // Reset spawn time
                return true;
            }
        }

        return false;
    }

    /**
     * Cập nhật di chuyển (Random Walk) - sử dụng deltaTime để độc lập với FPS
     * [SỬA] Nhận deltaTime từ bên ngoài và áp dụng Logic State Locking
     * [SỬA LẦN 2] Phân tách logic Gà (ít đi) và Thú thường (đi nhiều hơn)
     */
    private boolean updateMovement(Animal animal, long nowMs, double deltaTime) {
        // Trứng không di chuyển
        if (animal.getType() == AnimalType.EGG_ENTITY) {
            return false;
        }

        // Nếu deltaTime = 0 (frame đầu tiên), không di chuyển
        if (deltaTime <= 0) {
            return false;
        }

        // --- LOGIC DI CHUYỂN MỚI (State Locking) ---

        // Xác định loại động vật để lấy cấu hình phù hợp
        boolean isChickenType = (animal.getType() == AnimalType.CHICKEN || animal.getType() == AnimalType.BABY_CHICKEN);

        double walkChance;
        int minIdle, maxIdle;
        int minWalk, maxWalk;

        if (isChickenType) {
            // Cấu hình cho Gà (Giữ nguyên như cũ)
            walkChance = AnimalConfig.CHICKEN_WALK_CHANCE;
            minIdle = AnimalConfig.CHICKEN_MIN_IDLE_TIME_MS;
            maxIdle = AnimalConfig.CHICKEN_MAX_IDLE_TIME_MS;
            minWalk = AnimalConfig.CHICKEN_MIN_WALK_TIME_MS;
            maxWalk = AnimalConfig.CHICKEN_MAX_WALK_TIME_MS;
        } else {
            // Cấu hình cho Thú thường (Tăng hoạt động)
            walkChance = AnimalConfig.STANDARD_WALK_CHANCE;
            minIdle = AnimalConfig.STANDARD_MIN_IDLE_TIME_MS;
            maxIdle = AnimalConfig.STANDARD_MAX_IDLE_TIME_MS;
            minWalk = AnimalConfig.STANDARD_MIN_WALK_TIME_MS;
            maxWalk = AnimalConfig.STANDARD_MAX_WALK_TIME_MS;
        }

        // 1. Kiểm tra xem hành động hiện tại đã hết hạn chưa (AI Decision - Chạy mỗi frame để timer chính xác)
        if (nowMs >= animal.getActionEndTime()) {
            // Hết hạn -> Quyết định hành động mới
            double roll = random.nextDouble();

            if (roll < walkChance) {
                // -- CHUYỂN SANG ĐI BỘ --
                animal.setCurrentAction(Animal.Action.WALK);

                // Random hướng mới
                int newDir = random.nextInt(4);
                animal.setDirection(newDir);

                // Random thời gian đi
                long duration = minWalk + (long)(random.nextDouble() * (maxWalk - minWalk));
                animal.setActionEndTime(nowMs + duration);

            } else {
                // -- CHUYỂN SANG ĐỨNG YÊN --
                animal.setCurrentAction(Animal.Action.IDLE);

                // Random thời gian đứng
                long duration = minIdle + (long)(random.nextDouble() * (maxIdle - minIdle));
                animal.setActionEndTime(nowMs + duration);
            }
        }

        // 2. Thực hiện hành động hiện tại
        if (animal.getCurrentAction() == Animal.Action.WALK) {
            // Tính vị trí mới - nhân tốc độ với deltaTime để độc lập với FPS
            double speedPerSecond = animal.getType().getMovementSpeed(); // pixel/giây
            double movementDistance = speedPerSecond * deltaTime; // pixel/giây * giây = pixel
            double newX = animal.getX();
            double newY = animal.getY();

            // Di chuyển theo hướng
            switch (animal.getDirection()) {
                case 0: // Down
                    newY += movementDistance;
                    break;
                case 1: // Right
                    newX += movementDistance;
                    break;
                case 2: // Left
                    newX -= movementDistance;
                    break;
                case 3: // Up
                    newY -= movementDistance;
                    break;
            }

            // Kiểm tra collision trước khi di chuyển
            if (canAnimalMoveTo(animal, newX, newY)) {
                animal.setX(newX);
                animal.setY(newY);
                return true;
            } else {
                // Nếu bị chặn, dừng ngay lập tức và chuyển sang IDLE để chờ quyết định mới
                animal.setCurrentAction(Animal.Action.IDLE);
                // Reset timer để nó quyết định lại hành động mới ngay frame sau
                animal.setActionEndTime(nowMs);
            }
        }

        return false;
    }

    /**
     * Kiểm tra xem động vật có thể di chuyển đến vị trí mới không
     */
    private boolean canAnimalMoveTo(Animal animal, double newX, double newY) {
        // Kiểm tra collision với world (rào, cây, nước)
        double halfWidth = animal.getType().getHitboxWidth() / 2.0;
        double halfHeight = animal.getType().getHitboxHeight() / 2.0;

        // Kiểm tra 4 góc của hitbox
        if (!collisionManager.canPassThrough(newX - halfWidth, newY - halfHeight)) return false;
        if (!collisionManager.canPassThrough(newX + halfWidth, newY - halfHeight)) return false;
        if (!collisionManager.canPassThrough(newX - halfWidth, newY + halfHeight)) return false;
        if (!collisionManager.canPassThrough(newX + halfWidth, newY + halfHeight)) return false;

        // Kiểm tra tâm
        if (!collisionManager.canPassThrough(newX, newY)) return false;

        return true;
    }

    /**
     * Cập nhật sản phẩm (Sữa, Len, Trứng)
     */
    private boolean updateProduction(Animal animal, long currentTime) {
        if (!animal.canProduce() || animal.isHasProduct()) {
            return false; // Không thể tạo sản phẩm hoặc đã có sản phẩm
        }

        // Tính thời gian sản xuất (chậm hơn khi đói)
        long productionTime = animal.isHungry() ?
                AnimalConfig.HUNGRY_PRODUCTION_TIME_MS :
                animal.getType().getProductionTimeMs();

        // Khởi tạo timer nếu chưa có
        if (animal.getProductionTimer() == 0) {
            animal.setProductionTimer(currentTime);
        }

        // Kiểm tra xem đã đủ thời gian chưa
        long elapsed = (currentTime - animal.getProductionTimer()) / 1_000_000;
        if (elapsed >= productionTime) {
            animal.setHasProduct(true);
            animal.setProductionTimer(0); // Reset timer
            return true;
        }

        return false;
    }

    /**
     * Cho động vật ăn (reset đói)
     */
    public void feedAnimal(Animal animal) {
        if (animal != null && !animal.isDead()) {
            animal.setHunger(AnimalConfig.MAX_HUNGER);
            animal.setStarvationStartTime(0); // Reset thời gian đói
        }
    }

    /**
     * Thu hoạch sản phẩm (reset timer)
     */
    public void harvestProduct(Animal animal) {
        if (animal != null && animal.isHasProduct()) {
            animal.setHasProduct(false);
            animal.setProductionTimer(System.nanoTime()); // Bắt đầu timer mới
        }
    }

    /**
     * Giết động vật (trả về số lượng thịt)
     */
    public int killAnimal(Animal animal) {
        if (animal != null && !animal.isDead()) {
            animal.setDead(true);
            return animal.calculateMeatDrop();
        }
        return 0;
    }
}