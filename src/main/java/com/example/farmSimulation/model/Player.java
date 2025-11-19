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
    private double money;
    private int experience;
    private int level;
    private double stamina;

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

    // Constructor
    public Player() {
        this.tileX = GameLogicConfig.PLAYER_START_X;
        this.tileY = GameLogicConfig.PLAYER_START_Y;
        this.state = PlayerView.PlayerState.IDLE; // Trạng thái ban đầu
        this.direction = PlayerView.Direction.DOWN; // Hướng ban đầu

        // Khởi tạo hotbar
        this.hotbarItems = new ItemStack[HotbarConfig.HOTBAR_SLOT_COUNT];
        this.selectedHotbarSlot = 0;

        // Gán Item ban đầu (Số lượng tùy ý)
        // Tools: Stackable = false -> Quantity = 1
        this.hotbarItems[0] = new ItemStack(ItemType.HOE, 1);
        this.hotbarItems[1] = new ItemStack(ItemType.WATERING_CAN, 1);
        this.hotbarItems[2] = new ItemStack(ItemType.PICKAXE, 1);
        this.hotbarItems[3] = new ItemStack(ItemType.SHOVEL, 1);

        // Items: Stackable = true -> Quantity > 1
        this.hotbarItems[4] = new ItemStack(ItemType.SEEDS_STRAWBERRY, 2);
        this.hotbarItems[5] = new ItemStack(ItemType.SEEDS_CARROT, 5);
        this.hotbarItems[6] = new ItemStack(ItemType.FERTILIZER, 3);

        // Các ô khác là null (sẽ là Tool.HAND)
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
                    hotbarItems[slotIndex] = null; // Tool thường gãy thì mất
                    System.out.println("Tool broken!");
                }
            } else {
                // Item thường -> Giảm số lượng
                boolean stillHasItem = stack.remove(amount);
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
}