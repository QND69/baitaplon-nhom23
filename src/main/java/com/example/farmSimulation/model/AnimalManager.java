package com.example.farmSimulation.model;

import com.example.farmSimulation.config.AnimalConfig;
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
    private long lastUpdateTimeMs = 0;
    
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
        
        // [TỐI ƯU] Chỉ update animals theo interval, không phải mỗi frame
        if (currentTimeMs - lastUpdateTimeMs < AnimalConfig.ANIMAL_UPDATE_INTERVAL_MS) {
            return false; // Chưa đến lúc update
        }
        lastUpdateTimeMs = currentTimeMs;
        
        // [SỬA LỖI ĐỘNG VẬT ĐỨNG IM]
        // Tính deltaTime MỘT LẦN cho toàn bộ danh sách, thay vì tính trong từng con.
        double deltaTime = 0.0;
        if (lastMovementUpdateTime > 0) {
            long deltaNanos = currentTime - lastMovementUpdateTime;
            deltaTime = deltaNanos / 1_000_000_000.0;
            if (deltaTime > 0.1) deltaTime = 0.1;
        }
        lastMovementUpdateTime = currentTime; // Cập nhật thời gian sau khi tính toán xong
        
        Iterator<Animal> iterator = animals.iterator();
        while (iterator.hasNext()) {
            Animal animal = iterator.next();
            
            // Xóa động vật đã chết
            if (animal.isDead()) {
                iterator.remove();
                needsRedraw = true;
                continue;
            }
            
            // --- PHẦN 1: Cập nhật Đói ---
            updateHunger(animal, currentTime);
            
            // --- PHẦN 2: Kiểm tra Chết đói ---
            if (checkStarvation(animal, currentTime)) {
                // [MỚI] Logic rơi thịt khi chết đói
                int meatAmount = animal.calculateMeatDrop();
                if (meatAmount > 0 && worldMap != null) {
                    ItemType meatType = animal.getMeatType();
                    if (meatType != null) {
                        // [FIX LỆCH TỌA ĐỘ THỊT CHO CHẾT ĐÓI]
                        // Tính tile dựa trên visual center (giữa thân) thay vì chân
                        double visualCenterY = animal.getY() - (animal.getType().getSpriteSize() / 2.0);
                        
                        int tileCol = (int) Math.floor(animal.getX() / WorldConfig.TILE_SIZE);
                        int tileRow = (int) Math.floor(visualCenterY / WorldConfig.TILE_SIZE);
                        
                        // Tính toán offset
                        double targetItemX = animal.getX() - (ItemSpriteConfig.ITEM_SPRITE_WIDTH / 2.0);
                        double targetItemY = visualCenterY - (ItemSpriteConfig.ITEM_SPRITE_HEIGHT / 2.0);
                        
                        double offsetX = targetItemX - (tileCol * WorldConfig.TILE_SIZE);
                        double offsetY = targetItemY - (tileRow * WorldConfig.TILE_SIZE);
                        
                        // Đặt thịt xuống đất
                        TileData tileData = worldMap.getTileData(tileCol, tileRow);
                        tileData.setGroundItem(meatType);
                        tileData.setGroundItemAmount(meatAmount);
                        // [MỚI] Set offset
                        tileData.setGroundItemOffsetX(offsetX);
                        tileData.setGroundItemOffsetY(offsetY);
                        
                        worldMap.setTileData(tileCol, tileRow, tileData);
                    }
                }

                animal.setDead(true);
                needsRedraw = true;
                continue;
            }
            
            // --- PHẦN 3: Sinh trưởng (Trứng -> Gà con -> Gà) ---
            if (updateGrowth(animal, currentTime)) {
                needsRedraw = true;
            }
            
            // --- PHẦN 4: Di chuyển (Random Walk) ---
            // Truyền deltaTime đã tính vào hàm
            if (updateMovement(animal, currentTime, deltaTime)) {
                needsRedraw = true;
            }
            
            // --- PHẦN 5: Sản phẩm (Sữa, Len, Trứng) ---
            if (updateProduction(animal, currentTime)) {
                needsRedraw = true;
            }
            
            // --- PHẦN 6: Cập nhật Tuổi ---
            animal.setAge((int) animal.getAgeInSeconds());
        }
        
        return needsRedraw;
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
     * [SỬA] Nhận deltaTime từ bên ngoài
     */
    private boolean updateMovement(Animal animal, long currentTime, double deltaTime) {
        // Trứng không di chuyển
        if (animal.getType() == AnimalType.EGG_ENTITY) {
            return false;
        }
        
        // Nếu deltaTime = 0 (frame đầu tiên), không di chuyển
        if (deltaTime <= 0) {
            return false;
        }
        
        // Kiểm tra xem có đến lúc đổi hướng không
        long timeSinceDirectionChange = (currentTime - animal.getLastDirectionChangeTime()) / 1_000_000;
        boolean shouldChangeDirection = timeSinceDirectionChange >= AnimalConfig.DIRECTION_CHANGE_INTERVAL_MS;
        
        // Xác suất di chuyển (tính theo frame, không phải theo thời gian)
        boolean shouldMove = random.nextDouble() < AnimalConfig.MOVEMENT_CHANCE || shouldChangeDirection;
        
        if (shouldMove) {
            // Đổi hướng ngẫu nhiên
            if (shouldChangeDirection) {
                animal.setDirection(random.nextInt(4)); // 0-3
                animal.setLastDirectionChangeTime(currentTime);
            }
            
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
                animal.setCurrentAction(Animal.Action.WALK);
                return true;
            } else {
                // Nếu không thể di chuyển, đổi hướng
                animal.setDirection(random.nextInt(4));
                animal.setLastDirectionChangeTime(currentTime);
                animal.setCurrentAction(Animal.Action.IDLE);
            }
        } else {
            animal.setCurrentAction(Animal.Action.IDLE);
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