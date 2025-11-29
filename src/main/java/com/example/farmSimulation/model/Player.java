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
    private String gender; // Gender của người chơi (Male/Female)
    private double money;
    private double currentXP; // XP hiện tại
    private double xpToNextLevel; // XP cần để lên level tiếp theo
    private int level;
    private double currentStamina; // Stamina hiện tại
    private double maxStamina; // Stamina tối đa

    // Tọa độ logic của người chơi trong thế giới
    private double tileX;
    private double tileY;

    // Lưu trữ trạng thái LOGIC (Model)
    // PlayerView sẽ lưu trạng thái VISUAL (View)
    private PlayerView.PlayerState state;
    private PlayerView.Direction direction;

    // --- Hotbar (Thanh công cụ) ---
    private ItemStack[] hotbarItems;
    private int selectedHotbarSlot;
    
    // Tham chiếu đến MainGameView để hiển thị thông báo level up
    private com.example.farmSimulation.view.MainGameView mainGameView;
    
    // Time of death for death animation timing
    private long timeOfDeath = 0;

    // Constructor
    public Player() {
        this.tileX = GameLogicConfig.PLAYER_START_X;
        this.tileY = GameLogicConfig.PLAYER_START_Y;
        this.state = PlayerView.PlayerState.IDLE; // Trạng thái ban đầu
        this.direction = PlayerView.Direction.DOWN; // Hướng ban đầu
        this.money = GameLogicConfig.PLAYER_START_MONEY; // Khởi tạo tiền ban đầu
        
        // Khởi tạo Stamina & XP
        this.maxStamina = GameLogicConfig.PLAYER_MAX_STAMINA;
        this.currentStamina = GameLogicConfig.PLAYER_START_STAMINA;
        this.level = GameLogicConfig.PLAYER_START_LEVEL;
        this.currentXP = GameLogicConfig.PLAYER_START_XP;
        this.xpToNextLevel = GameLogicConfig.PLAYER_START_XP_TO_NEXT_LEVEL;

        // Khởi tạo hotbar
        this.hotbarItems = new ItemStack[HotbarConfig.HOTBAR_SLOT_COUNT];
        this.selectedHotbarSlot = 0;

        // Gán Item ban đầu (Số lượng tùy ý)
        this.hotbarItems[0] = new ItemStack(ItemType.AXE, 1);
        this.hotbarItems[1] = new ItemStack(ItemType.SWORD, 1);
        this.hotbarItems[2] = new ItemStack(ItemType.SHEARS, 1);
        this.hotbarItems[3] = new ItemStack(ItemType.EGG, 9);
        this.hotbarItems[4] = new ItemStack(ItemType.ITEM_COW, 2);
        this.hotbarItems[5] = new ItemStack(ItemType.ITEM_CHICKEN, 5);
        this.hotbarItems[6] = new ItemStack(ItemType.ITEM_PIG, 3);
        this.hotbarItems[7] = new ItemStack(ItemType.ITEM_SHEEP, 2);
        this.hotbarItems[8] = new ItemStack(ItemType.WOOD, 30);

        // Các ô khác là null (tay không)
    }

    /**
     * Lấy ItemStack hiện tại đang cầm
     */
    public ItemStack getCurrentItem() {
        return hotbarItems[selectedHotbarSlot];
    }

    // Hàm thêm vật phẩm vào túi (Dùng cho thu hoạch)
    public boolean addItem(ItemType type, int amount) {
        boolean addedAny = false;

        // Stack vào ô có sẵn
        for (ItemStack stack : hotbarItems) {
            if (stack != null && stack.getItemType() == type) {
                int remaining = stack.add(amount);
                if (remaining < amount) addedAny = true; // Đã thêm được ít nhất 1
                if (remaining == 0) return true;
                amount = remaining;
            }
        }

        // Tìm ô trống
        for (int i = 0; i < hotbarItems.length; i++) {
            if (hotbarItems[i] == null) {
                hotbarItems[i] = new ItemStack(type, amount);
                return true;
            }
        }

        return addedAny; // Trả về false nếu không thêm được gì (Inventory Full)
    }
    
    /**
     * Thêm item vào inventory với độ bền cụ thể (overload method)
     * @param type Loại item
     * @param amount Số lượng
     * @param durability Độ bền của item (nếu có). Nếu <= 0, sẽ dùng max durability
     * @return true nếu thêm thành công, false nếu không
     */
    public boolean addItem(ItemType type, int amount, int durability) {
        boolean addedAny = false;

        // Stack vào ô có sẵn (chỉ stack được nếu cùng loại và không có độ bền, hoặc cùng độ bền)
        for (ItemStack stack : hotbarItems) {
            if (stack != null && stack.getItemType() == type) {
                // Chỉ stack được nếu cả hai đều không có độ bền, hoặc cùng độ bền
                boolean canStack = !type.hasDurability() || 
                                  (stack.getCurrentDurability() == (durability > 0 ? durability : type.getMaxDurability()));
                
                if (canStack) {
                    int remaining = stack.add(amount);
                    if (remaining < amount) addedAny = true;
                    if (remaining == 0) return true;
                    amount = remaining;
                }
            }
        }

        // Tìm ô trống
        for (int i = 0; i < hotbarItems.length; i++) {
            if (hotbarItems[i] == null) {
                ItemStack newStack = new ItemStack(type, amount);
                // Set độ bền nếu item có độ bền
                if (type.hasDurability()) {
                    // Nếu durability > 0, dùng nó; nếu <= 0, dùng max durability (initial spawn case)
                    int finalDurability = (durability > 0) ? durability : type.getMaxDurability();
                    newStack.setCurrentDurability(finalDurability);
                }
                // Nếu item không có độ bền, ItemStack constructor đã set đúng rồi (max durability = 0)
                hotbarItems[i] = newStack;
                return true;
            }
        }

        return addedAny; // Trả về false nếu không thêm được gì (Inventory Full)
    }
    
    /**
     * Tính số lượng item có thể thêm vào inventory
     * @param type Loại item
     * @param amount Số lượng muốn thêm
     * @return Số lượng có thể thêm (0 đến amount)
     */
    public int calculateAddableAmount(ItemType type, int amount) {
        if (amount <= 0) return 0;
        
        int remainingToAdd = amount;
        int maxStackSize = type.getMaxStackSize();
        
        // Kiểm tra stack vào ô có sẵn
        for (ItemStack stack : hotbarItems) {
            if (stack != null && stack.getItemType() == type) {
                int spaceAvailable = maxStackSize - stack.getQuantity();
                int canAdd = Math.min(spaceAvailable, remainingToAdd);
                remainingToAdd -= canAdd;
                if (remainingToAdd <= 0) return amount; // Có thể thêm hết
            }
        }
        
        // Kiểm tra số ô trống
        int emptySlots = 0;
        for (ItemStack stack : hotbarItems) {
            if (stack == null) {
                emptySlots++;
            }
        }
        
        // Tính số lượng có thể thêm từ các ô trống
        int canAddFromEmptySlots = emptySlots * maxStackSize;
        int totalAddable = amount - remainingToAdd + Math.min(canAddFromEmptySlots, remainingToAdd);
        
        return Math.min(totalAddable, amount);
    }

    /**
     * Tiêu thụ item hoặc giảm độ bền tại slot chỉ định
     * Hàm này được gọi bởi ActionManager KHI HÀNH ĐỘNG KẾT THÚC
     */
    public void consumeItemAtSlot(int slotIndex, int amount) {
        if (slotIndex < 0 || slotIndex >= hotbarItems.length) return;
        ItemStack stack = hotbarItems[slotIndex];

        if (stack != null) {
            if (stack.getItemType().hasDurability()) {
                // Item có độ bền -> Giảm độ bền
                boolean broken = stack.decreaseDurability(amount); // amount thường là 1

                // Bình tưới nước: Hết độ bền (hết nước) -> KHÔNG MẤT, chỉ không dùng được
                if (broken && stack.getItemType() != ItemType.WATERING_CAN) {
                    hotbarItems[slotIndex] = null; // Item thường gãy thì mất
                    System.out.println("Item broken!");
                }
            } else {
                // Item thường -> Giảm số lượng
                stack.remove(amount);
                if (stack.isEmpty()) {
                    hotbarItems[slotIndex] = null;
                }
            }
        }
    }

    /**
     * Hoán đổi vị trí 2 item trong hotbar
     */
    public void swapHotbarItems(int indexA, int indexB) {
        if (indexA < 0 || indexA >= hotbarItems.length || indexB < 0 || indexB >= hotbarItems.length) {
            return;
        }
        ItemStack temp = hotbarItems[indexA];
        hotbarItems[indexA] = hotbarItems[indexB];
        hotbarItems[indexB] = temp;

        // Nếu slot đang chọn bị đổi đi, cần update lại logic chọn (nếu cần thiết)
        // Ở đây logic chọn dựa trên index (selectedHotbarSlot) nên không cần đổi index,
        // người chơi vẫn trỏ vào ô số đó, nhưng item bên trong đã khác.
    }
    
    /**
     * Thêm tiền cho người chơi
     * @param amount Số tiền cần thêm
     * @return true nếu thành công, false nếu amount < 0
     */
    public boolean addMoney(double amount) {
        if (amount < 0) {
            return false; // Không cho phép thêm số âm
        }
        this.money += amount;
        return true;
    }
    
    /**
     * Trừ tiền của người chơi (mua hàng)
     * @param amount Số tiền cần trừ
     * @return true nếu có đủ tiền và trừ thành công, false nếu không đủ tiền
     */
    public boolean spendMoney(double amount) {
        if (amount < 0) {
            return false; // Không cho phép trừ số âm
        }
        if (this.money < amount) {
            return false; // Không đủ tiền
        }
        this.money -= amount;
        return true;
    }
    
    // --- Stamina Methods ---
    
    /**
     * Giảm stamina
     * @param amount Lượng stamina cần giảm
     */
    public void reduceStamina(double amount) {
        this.currentStamina = Math.max(0, this.currentStamina - amount);
        
        // Kiểm tra Game Over nếu stamina <= 0
        if (this.currentStamina <= 0) {
            this.currentStamina = 0;
            this.state = PlayerView.PlayerState.DEAD;
            this.timeOfDeath = System.currentTimeMillis(); // Set time of death for animation timing
            // Hiển thị thông báo "You passed out!"
            if (mainGameView != null) {
                mainGameView.showTemporaryText("You passed out!", tileX, tileY);
            }
        }
    }
    
    /**
     * Hồi phục stamina
     * @param amount Lượng stamina cần hồi phục
     */
    public void recoverStamina(double amount) {
        this.currentStamina = Math.min(maxStamina, this.currentStamina + amount);
    }
    
    /**
     * Kiểm tra xem có đang bị penalty do stamina thấp không
     * Áp dụng penalty khi stamina <= 15% (khi thanh chuyển đỏ)
     * @return true nếu stamina <= threshold (15% của maxStamina)
     */
    public boolean hasStaminaPenalty() {
        // Tính phần trăm stamina và so sánh với 15%
        double percentage = maxStamina > 0 ? (currentStamina / maxStamina) : 0.0;
        return percentage <= 0.15; // Penalty khi stamina <= 15% (mức đỏ)
    }
    
    /**
     * Getter cho stamina hiện tại (tương thích với code cũ)
     */
    public double getStamina() {
        return currentStamina;
    }
    
    /**
     * Setter cho stamina hiện tại (tương thích với code cũ)
     */
    public void setStamina(double stamina) {
        this.currentStamina = Math.min(maxStamina, Math.max(0, stamina));
    }
    
    /**
     * Getter cho experience (tương thích với code cũ)
     */
    public int getExperience() {
        return (int) currentXP;
    }
    
    /**
     * Setter cho experience (tương thích với code cũ)
     */
    public void setExperience(int experience) {
        this.currentXP = experience;
    }
    
    // --- XP & Leveling Methods ---
    
    /**
     * Tăng XP cho người chơi
     * @param amount Lượng XP cần tăng
     */
    public void gainXP(double amount) {
        this.currentXP += amount;
        
        // Kiểm tra level up
        while (currentXP >= xpToNextLevel) {
            levelUp();
        }
    }
    
    /**
     * Lên level
     */
    private void levelUp() {
        this.level++;
        this.currentXP -= xpToNextLevel;
        
        // Tăng max stamina
        this.maxStamina += GameLogicConfig.STAMINA_INCREASE_PER_LEVEL;
        
        // Refill stamina
        this.currentStamina = maxStamina;
        
        // Tăng XP cần thiết cho level tiếp theo
        this.xpToNextLevel *= GameLogicConfig.XP_MULTIPLIER_PER_LEVEL;
        
        // Hiển thị thông báo "LEVEL UP!"
        if (mainGameView != null) {
            mainGameView.showTemporaryText("LEVEL UP! Level " + level, tileX, tileY);
        }
    }
    
    /**
     * Set MainGameView reference (để hiển thị thông báo)
     */
    public void setMainGameView(com.example.farmSimulation.view.MainGameView mainGameView) {
        this.mainGameView = mainGameView;
    }
    
    /**
     * Ăn item hiện tại đang cầm để hồi phục stamina
     * @return true nếu ăn thành công, false nếu không thể ăn (item không có staminaRestore hoặc stamina đã đầy)
     */
    public boolean eatCurrentItem() {
        ItemStack currentItem = getCurrentItem();
        if (currentItem == null) return false;
        
        ItemType itemType = currentItem.getItemType();
        
        // Kiểm tra xem item có thể ăn không (có staminaRestore > 0)
        if (itemType.getStaminaRestore() <= 0) {
            return false;
        }
        
        // Kiểm tra xem stamina có đầy không (không ăn nếu đã đầy)
        if (currentStamina >= maxStamina) {
            return false;
        }
        
        // Hồi phục stamina
        recoverStamina(itemType.getStaminaRestore());
        
        // Giảm số lượng item (tiêu thụ 1 item)
        currentItem.remove(1);
        
        // Xóa item nếu đã hết
        if (currentItem.isEmpty()) {
            hotbarItems[selectedHotbarSlot] = null;
        }
        
        // Set state thành BUSY (sẽ được xử lý bởi ActionManager)
        this.state = PlayerView.PlayerState.BUSY;
        
        return true;
    }
}