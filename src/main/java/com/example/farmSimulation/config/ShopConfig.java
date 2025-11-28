package com.example.farmSimulation.config;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * Cấu hình cho hệ thống Shop (Cửa hàng)
 */
public class ShopConfig {
    
    // --- Đường dẫn ảnh nền Shop ---
    public static final String SHOP_BG_PATH = "/assets/images/GUI/shop_bg.png";
    
    // --- Kích thước cửa sổ Shop ---
    // Tăng kích thước để shop chiếm gần hết màn hình (Screen: 1280x720)
    public static final double SHOP_WIDTH = 1100.0; // Chiều rộng cửa sổ shop (tăng từ 800)
    public static final double SHOP_HEIGHT = 650.0; // Chiều cao cửa sổ shop (tăng từ 600)
    
    // --- Cấu hình Grid (Lưới hiển thị vật phẩm) ---
    public static final int SHOP_GRID_COLS = 5; // Số cột trong lưới (tăng từ 4 để phù hợp với width lớn hơn)
    public static final int SHOP_GRID_ROWS = 5; // Số hàng trong lưới
    public static final double SHOP_ITEM_SLOT_SIZE = 120.0; // Kích thước mỗi ô vật phẩm
    public static final double SHOP_ITEM_SPACING = 20.0; // Khoảng cách giữa các ô
    
    // --- Cấu hình Nút bấm ---
    public static final double BUTTON_WIDTH = 100.0; // Chiều rộng nút
    public static final double BUTTON_HEIGHT = 30.0; // Chiều cao nút
    public static final Paint BUTTON_BG_COLOR = Color.rgb(100, 150, 200); // Màu nền nút
    public static final Paint BUTTON_TEXT_COLOR = Color.WHITE; // Màu chữ nút
    public static final double BUTTON_FONT_SIZE = 14.0; // Kích thước font nút
    
    // --- Cấu hình Text giá tiền ---
    public static final Paint PRICE_TEXT_COLOR = Color.GOLD; // Màu chữ giá tiền
    public static final double PRICE_FONT_SIZE = 16.0; // Kích thước font giá tiền
    public static final Paint PRICE_STROKE_COLOR = Color.BLACK; // Màu viền chữ giá
    public static final double PRICE_STROKE_WIDTH = 1.0; // Độ dày viền chữ
    
    // --- Cấu hình Icon Coin (placeholder) ---
    public static final double COIN_ICON_SIZE = 24.0; // Kích thước icon coin
    public static final Paint COIN_ICON_COLOR = Color.GOLD; // Màu icon coin
    
    // --- Cấu hình Padding và Margin ---
    public static final double SHOP_PADDING = 40.0; // Padding bên trong cửa sổ shop
    public static final double ITEM_ICON_SIZE = 64.0; // Kích thước icon vật phẩm trong shop
    
    // --- Cấu hình Text thông báo ---
    public static final Paint ERROR_TEXT_COLOR = Color.RED; // Màu chữ lỗi
    public static final Paint SUCCESS_TEXT_COLOR = Color.GREEN; // Màu chữ thành công
    public static final double MESSAGE_FONT_SIZE = 14.0; // Kích thước font thông báo
    
    // --- Cấu hình Buy/Sell Tabs ---
    public static final String TAB_BUY_TEXT = "BUY";
    public static final String TAB_SELL_TEXT = "SELL";
    public static final double TAB_BUTTON_WIDTH = 200.0; // Chiều rộng nút tab
    public static final double TAB_BUTTON_HEIGHT = 40.0; // Chiều cao nút tab
    public static final Paint TAB_ACTIVE_BG_COLOR = Color.rgb(80, 120, 160); // Màu nền tab đang active
    public static final Paint TAB_INACTIVE_BG_COLOR = Color.rgb(60, 60, 60); // Màu nền tab không active
    public static final double TAB_BUTTON_FONT_SIZE = 18.0; // Kích thước font tab
    
    // --- Daily Stock & Reroll Configuration ---
    public static final int REROLL_PRICE = 100; // Cost to reroll shop stock
    public static final int DAILY_SHOP_SLOTS = 10; // Number of items that appear per day
    public static final double DISCOUNT_CHANCE = 0.3; // 30% chance for an item to be discounted
    public static final int MAX_ITEM_QUANTITY = 10; // Maximum quantity per shop item
    public static final int MIN_ITEM_QUANTITY = 1; // Minimum quantity per shop item
    public static final double MAX_DISCOUNT_RATE = 0.3; // Maximum discount rate (30% off)
    
    // --- Cấu hình Money Display (trong Shop) ---
    public static final double MONEY_DISPLAY_FONT_SIZE = 28.0; // Kích thước font tiền (large và prominent)
    public static final Paint MONEY_DISPLAY_COLOR = Color.GOLD; // Màu chữ tiền (vàng)
    public static final double MONEY_DISPLAY_POS_X = SHOP_WIDTH - 250.0; // Vị trí X (từ phải)
    public static final double MONEY_DISPLAY_POS_Y = 20.0; // Vị trí Y (từ trên)
    
    private ShopConfig() {}
}


