package com.example.farmSimulation.model;

import com.example.farmSimulation.config.*;

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
     * [SỬA] Thêm tham số Player để xử lý logic Follow Food
     */
    public boolean updateAnimals(long currentTime, Player player) {
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

        // [MỚI] List chứa các động vật mới sinh ra (trứng/con non) để add sau vòng lặp tránh lỗi ConcurrentModification
        List<Animal> newAnimals = new ArrayList<>();

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

                // --- PHẦN 4: Sinh sản (Breeding) [MỚI] ---
                // Chỉ kiểm tra cho động vật trưởng thành (không phải Baby, không phải Gà - Gà dùng logic trứng)
                // Và phải No bụng mới đẻ được
                if (!animal.isBaby() && animal.getType() != AnimalType.CHICKEN && animal.getHunger() >= AnimalConfig.MIN_HUNGER_FOR_BREEDING) {
                    // Cập nhật tìm kiếm bạn tình hoặc xử lý tiến trình sinh con (đã đứng cạnh nhau)
                    if (updateBreeding(animal, currentTime, newAnimals)) {
                        needsRedraw = true;
                    }
                } else {
                    // Nếu đói hoặc chưa lớn, reset các trạng thái breeding nếu có
                    animal.setBreedingPartner(null);
                    animal.setMatingStartTime(0);
                }

                // --- PHẦN 5: Sản phẩm (Sữa, Len, Trứng) ---
                // [SỬA] Truyền danh sách newAnimals để Gà đẻ trứng vào đó
                if (updateProduction(animal, currentTime, newAnimals)) {
                    needsRedraw = true;
                }

                // --- PHẦN 6: Cập nhật Tuổi ---
                animal.setAge((int) animal.getAgeInSeconds());
            }

            // --- NHÓM 2: MOVEMENT & AI DECISION (Chạy mỗi frame để mượt) ---
            // Cần chạy mỗi frame để:
            // 1. Cập nhật Timer AI (actionEndTime) chính xác
            // 2. Di chuyển pixel mượt mà theo deltaTime
            // [MỚI] Truyền Player vào để xử lý Follow Food
            if (updateMovement(animal, now, deltaTime, player)) {
                needsRedraw = true;
            }
        }

        // Thêm các động vật mới (trứng gà, con non) vào danh sách chính
        if (!newAnimals.isEmpty()) {
            animals.addAll(newAnimals);
            needsRedraw = true;
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
     * [MỚI] Thêm Logic Follow Food (nếu Player cầm thức ăn phù hợp)
     * [MỚI 2] ƯU TIÊN CAO NHẤT: Nếu đang có breedingPartner -> Đi tới partner
     */
    private boolean updateMovement(Animal animal, long nowMs, double deltaTime, Player player) {
        // Trứng không di chuyển
        if (animal.getType() == AnimalType.EGG_ENTITY) {
            return false;
        }

        // Nếu deltaTime = 0 (frame đầu tiên), không di chuyển
        if (deltaTime <= 0) {
            return false;
        }

        // --- ƯU TIÊN 1: LOGIC MATING (Đã đứng cạnh nhau, CHUẨN BỊ SINH) ---
        // [MỚI] Thêm logic di chuyển về vị trí thẳng hàng (Alignment)
        if (animal.getMatingStartTime() > 0) {
            Animal partner = animal.getBreedingPartner();
            if (partner != null && !partner.isDead()) {
                // Tính toán vị trí trung tâm giữa 2 con
                double dx = Math.abs(animal.getX() - partner.getX());
                double dy = Math.abs(animal.getY() - partner.getY());

                double targetX, targetY;
                int newDir;

                // [SỬA] Xác định hướng dàn hàng (Ngang hay Dọc) dựa trên đường nào ngắn hơn
                if (dx > dy) {
                    // Khoảng cách ngang xa hơn -> Dàn hàng NGANG (Horizontal)
                    // Y bằng nhau (giữa), X cách nhau OFFSET
                    targetY = (animal.getY() + partner.getY()) / 2.0;

                    double midX = (animal.getX() + partner.getX()) / 2.0;
                    boolean amILeft = animal.getX() <= partner.getX();

                    // Nếu tọa độ quá gần, dùng hashcode để phân định
                    if (Math.abs(animal.getX() - partner.getX()) < 1.0) {
                        amILeft = animal.hashCode() < partner.hashCode();
                    }

                    targetX = midX + (amILeft ? -AnimalConfig.BREEDING_ALIGNMENT_OFFSET : AnimalConfig.BREEDING_ALIGNMENT_OFFSET);
                    newDir = amILeft ? 1 : 2; // Right : Left (Nhìn vào nhau)

                } else {
                    // Khoảng cách dọc xa hơn (hoặc bằng) -> Dàn hàng DỌC (Vertical)
                    // X bằng nhau (giữa), Y cách nhau OFFSET
                    targetX = (animal.getX() + partner.getX()) / 2.0;

                    double midY = (animal.getY() + partner.getY()) / 2.0;
                    boolean amIUp = animal.getY() <= partner.getY();

                    // Nếu tọa độ quá gần, dùng hashcode để phân định
                    if (Math.abs(animal.getY() - partner.getY()) < 1.0) {
                        amIUp = animal.hashCode() < partner.hashCode();
                    }

                    targetY = midY + (amIUp ? -AnimalConfig.BREEDING_ALIGNMENT_OFFSET : AnimalConfig.BREEDING_ALIGNMENT_OFFSET);
                    newDir = amIUp ? 0 : 3; // Down : Up (Nhìn vào nhau)
                }

                // Di chuyển mượt mà (Lerp) về vị trí mục tiêu
                double lerpSpeed = AnimalConfig.BREEDING_ALIGNMENT_SPEED;
                double newX = animal.getX() + (targetX - animal.getX()) * lerpSpeed;
                double newY = animal.getY() + (targetY - animal.getY()) * lerpSpeed;

                // Cập nhật vị trí (bỏ qua collision vì đang mating)
                animal.setX(newX);
                animal.setY(newY);
                animal.setDirection(newDir);
            }

            // Đang mating thì trạng thái luôn là IDLE (để hiện tim bay nếu có animation)
            animal.setCurrentAction(Animal.Action.IDLE);
            return true; // Đã xử lý xong, không chạy logic random walk
        }

        // --- ƯU TIÊN 2: LOGIC APPROACHING PARTNER (Đi tìm bạn tình) ---
        if (animal.getBreedingPartner() != null) {
            Animal partner = animal.getBreedingPartner();

            // Kiểm tra tính hợp lệ của partner
            if (partner.isDead() || partner.isHungry()) {
                animal.setBreedingPartner(null); // Hủy kèo nếu partner chết hoặc đói
            } else {
                double dx = partner.getX() - animal.getX();
                double dy = partner.getY() - animal.getY();
                double distSq = dx*dx + dy*dy;

                // Nếu khoảng cách <= BREEDING_RANGE -> Bắt đầu Mating
                if (distSq <= AnimalConfig.BREEDING_RANGE * AnimalConfig.BREEDING_RANGE) {
                    // [SỬA] Đã tới gần -> Bắt đầu timer mating để chuyển sang pha Alignment
                    if (animal.getMatingStartTime() == 0) {
                        animal.setMatingStartTime(System.currentTimeMillis());
                    }
                    animal.setCurrentAction(Animal.Action.IDLE);
                    return true;
                }

                // Nếu chưa tới -> Đi tới
                animal.setCurrentAction(Animal.Action.WALK);
                animal.setActionEndTime(nowMs + 100); // Reset timer liên tục

                // Xác định hướng
                if (Math.abs(dx) > Math.abs(dy)) {
                    animal.setDirection(dx > 0 ? 1 : 2); // Right : Left
                } else {
                    animal.setDirection(dy > 0 ? 0 : 3); // Down : Up
                }

                // Di chuyển
                double speed = animal.getType().getMovementSpeed();
                double moveDist = speed * deltaTime;
                double dist = Math.sqrt(distSq);
                double moveX = (dx / dist) * moveDist;
                double moveY = (dy / dist) * moveDist;

                double nextX = animal.getX() + moveX;
                double nextY = animal.getY() + moveY;

                if (canAnimalMoveTo(animal, nextX, nextY)) {
                    animal.setX(nextX);
                    animal.setY(nextY);
                    return true;
                } else {
                    // Nếu bị kẹt đường -> Tạm hủy kèo để random walk thoát ra
                    // animal.setBreedingPartner(null);
                }
            }
        }

        // --- ƯU TIÊN 3: LOGIC FOLLOW PLAYER (Khi có thức ăn) ---
        boolean isAttracted = false;
        if (player != null && player.getCurrentItem() != null) {
            ItemType heldItem = player.getCurrentItem().getItemType();
            if (animal.getType().acceptsFood(heldItem)) {
                // Tính khoảng cách tới player
                // Dùng vị trí thực tế scale của player để chính xác hơn
                double pX = player.getTileX() + (PlayerSpriteConfig.BASE_PLAYER_FRAME_WIDTH / 2.0);
                double pY = player.getTileY() + PlayerSpriteConfig.BASE_PLAYER_FRAME_HEIGHT;

                double dx = pX - animal.getX();
                double dy = pY - animal.getY();
                double distSq = dx*dx + dy*dy;

                // Nếu trong tầm nhìn
                if (distSq < AnimalConfig.PLAYER_FOLLOW_DETECTION_RANGE * AnimalConfig.PLAYER_FOLLOW_DETECTION_RANGE) {
                    // Nếu chưa đến quá gần (stop distance)
                    if (distSq > AnimalConfig.PLAYER_FOLLOW_STOP_DISTANCE * AnimalConfig.PLAYER_FOLLOW_STOP_DISTANCE) {
                        isAttracted = true;

                        // Override hành động thành WALK
                        animal.setCurrentAction(Animal.Action.WALK);
                        animal.setActionEndTime(nowMs + 100); // Reset timer liên tục để giữ trạng thái

                        // Xác định hướng (đơn giản 4 hướng)
                        if (Math.abs(dx) > Math.abs(dy)) {
                            animal.setDirection(dx > 0 ? 1 : 2); // Right : Left
                        } else {
                            animal.setDirection(dy > 0 ? 0 : 3); // Down : Up
                        }

                        // Di chuyển trực tiếp về phía player
                        double speed = animal.getType().getMovementSpeed();
                        double moveDist = speed * deltaTime;

                        // Normalize vector
                        double dist = Math.sqrt(distSq);
                        double moveX = (dx / dist) * moveDist;
                        double moveY = (dy / dist) * moveDist;

                        double nextX = animal.getX() + moveX;
                        double nextY = animal.getY() + moveY;

                        if (canAnimalMoveTo(animal, nextX, nextY)) {
                            animal.setX(nextX);
                            animal.setY(nextY);
                            return true;
                        }
                    } else {
                        // Quá gần -> Đứng lại nhìn
                        animal.setCurrentAction(Animal.Action.IDLE);
                        // Quay mặt về phía player
                        if (Math.abs(dx) > Math.abs(dy)) {
                            animal.setDirection(dx > 0 ? 1 : 2);
                        } else {
                            animal.setDirection(dy > 0 ? 0 : 3);
                        }
                        return true;
                    }
                }
            }
        }

        // Nếu đang bị thu hút, bỏ qua logic random wander bên dưới
        if (isAttracted) return true;

        // --- ƯU TIÊN 4: LOGIC DI CHUYỂN NGẪU NHIÊN (CŨ) ---

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
    private boolean updateProduction(Animal animal, long currentTime, List<Animal> newAnimals) {
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
            // [SỬA] Gà tự đẻ trứng xuống đất (tạo EGG_ENTITY)
            if (animal.getType() == AnimalType.CHICKEN) {
                // Reset timer ngay lập tức
                animal.setProductionTimer(0);

                // Tạo một quả trứng tại vị trí của gà
                Animal egg = new Animal(AnimalType.EGG_ENTITY, animal.getX(), animal.getY());
                // Thêm vào list chờ add
                newAnimals.add(egg);

                // Không set hasProduct=true cho gà, vì sản phẩm đã rơi ra
                return true;
            } else {
                // Các con vật khác: Hiện icon sản phẩm
                animal.setHasProduct(true);
                animal.setProductionTimer(0); // Reset timer
                return true;
            }
        }

        return false;
    }

    // [MỚI] Cập nhật logic sinh sản (Breeding)
    private boolean updateBreeding(Animal animal, long currentTime, List<Animal> newAnimals) {
        // Kiểm tra Cooldown
        long timeSinceBreed = (currentTime - animal.getBreedingCooldownTimer()) / 1_000_000;
        // Nếu timer != 0 và chưa hết cooldown -> return
        if (animal.getBreedingCooldownTimer() > 0 && timeSinceBreed < AnimalConfig.BREEDING_COOLDOWN_MS) {
            return false;
        }

        // --- TRƯỜNG HỢP 1: ĐÃ CÓ ĐỐI TƯỢNG VÀ ĐANG "TÁN TỈNH" (MATING) ---
        if (animal.getMatingStartTime() > 0 && animal.getBreedingPartner() != null) {
            long matingDuration = System.currentTimeMillis() - animal.getMatingStartTime();

            // Nếu đã đủ thời gian "tán tỉnh" -> SINH CON
            if (matingDuration >= AnimalConfig.BREEDING_ANIMATION_DURATION_MS) {
                AnimalType babyType = animal.getType().getBabyType();
                if (babyType != null) {
                    double midX = (animal.getX() + animal.getBreedingPartner().getX()) / 2.0;
                    double midY = (animal.getY() + animal.getBreedingPartner().getY()) / 2.0;

                    // Tạo baby, constructor sẽ set age = 0
                    // [LƯU Ý] View sẽ gọi baby.getVisualScale() -> trả về 0.5 (nhờ Animal.java mới)
                    Animal baby = new Animal(babyType, midX, midY);
                    newAnimals.add(baby);

                    Animal partner = animal.getBreedingPartner();

                    // Reset cho cả 2
                    applyPostBreedingState(animal, currentTime);
                    applyPostBreedingState(partner, currentTime);

                    return true;
                }
            }
            return false; // Đang chờ sinh
        }

        // --- TRƯỜNG HỢP 2: CHƯA CÓ ĐỐI TƯỢNG, ĐI TÌM ---
        if (animal.getBreedingPartner() == null) {
            // Tìm bạn đời xung quanh trong tầm nhìn xa (BREEDING_DETECTION_RANGE)
            for (Animal partner : animals) {
                if (partner == animal) continue; // Không tự breed
                if (partner.isDead()) continue;

                // Phải cùng loại, trưởng thành, không đói, CHƯA CÓ ĐỐI TƯỢNG
                if (partner.getType() == animal.getType() &&
                        !partner.isBaby() &&
                        !partner.isHungry() &&
                        partner.getBreedingPartner() == null) {

                    // Kiểm tra partner có đang cooldown không
                    long partnerTimeSince = (currentTime - partner.getBreedingCooldownTimer()) / 1_000_000;
                    if (partner.getBreedingCooldownTimer() > 0 && partnerTimeSince < AnimalConfig.BREEDING_COOLDOWN_MS) {
                        continue;
                    }

                    // Kiểm tra khoảng cách
                    double distSq = Math.pow(animal.getX() - partner.getX(), 2) + Math.pow(animal.getY() - partner.getY(), 2);

                    // Nếu trong tầm nhìn -> CHỐT ĐƠN (Gán partner cho nhau)
                    if (distSq <= AnimalConfig.BREEDING_DETECTION_RANGE * AnimalConfig.BREEDING_DETECTION_RANGE) {
                        animal.setBreedingPartner(partner);
                        partner.setBreedingPartner(animal);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // [MỚI] Helper function để reset trạng thái sau khi sinh
    private void applyPostBreedingState(Animal animal, long currentTime) {
        animal.setBreedingCooldownTimer(currentTime); // Set cooldown

        double cost = AnimalConfig.BREEDING_HUNGER_COST;
        animal.setHunger(Math.max(0, animal.getHunger() - cost)); // Trừ đói
        animal.setStarvationStartTime(0); // Reset timer đói

        animal.setBreedingPartner(null); // Xóa partner
        animal.setMatingStartTime(0); // Reset timer mating
        animal.setCurrentAction(Animal.Action.IDLE); // Về trạng thái nghỉ
    }

    /**
     * Cho động vật ăn (reset đói)
     * [SỬA] Thêm logic hồi phục lượng nhất định thay vì luôn đầy
     */
    public void feedAnimal(Animal animal) {
        if (animal != null && !animal.isDead()) {
            double current = animal.getHunger();
            double recover = AnimalConfig.HUNGER_RECOVER_PER_FEED;
            animal.setHunger(Math.min(AnimalConfig.MAX_HUNGER, current + recover));

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