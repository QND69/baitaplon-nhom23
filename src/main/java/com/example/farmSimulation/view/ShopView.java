package com.example.farmSimulation.view;

import com.example.farmSimulation.config.ShopConfig;
import com.example.farmSimulation.model.ItemStack;
import com.example.farmSimulation.model.ItemType;
import com.example.farmSimulation.model.ShopManager;
import com.example.farmSimulation.model.ShopSlot;
import com.example.farmSimulation.view.assets.AssetManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * Shop UI - Supports Buying (Daily Stock) and Selling
 */
public class ShopView extends StackPane {
    private final ShopManager shopManager;
    private final AssetManager assetManager;
    private final GridPane itemGrid;
    private final Label messageLabel; // Label for messages
    private final Label moneyLabel; // Label for player's money
    private final VBox rerollButtonBox; // Reroll button (fixed position, not in grid)
    
    // Buy/Sell Mode
    private boolean isSellingMode = false; // false = Buy mode, true = Sell mode
    private Button buyTabButton;
    private Button sellTabButton;
    
    public ShopView(ShopManager shopManager, AssetManager assetManager) {
        this.shopManager = shopManager;
        this.assetManager = assetManager;
        this.itemGrid = new GridPane();
        this.messageLabel = new Label();
        this.moneyLabel = new Label();
        this.rerollButtonBox = new VBox(10);
        
        setupUI();
    }
    
    /**
     * Setup Shop UI
     */
    private void setupUI() {
        // Set fixed size for ShopView
        this.setPrefSize(ShopConfig.SHOP_WIDTH, ShopConfig.SHOP_HEIGHT);
        this.setMaxSize(ShopConfig.SHOP_WIDTH, ShopConfig.SHOP_HEIGHT);
        this.setMinSize(ShopConfig.SHOP_WIDTH, ShopConfig.SHOP_HEIGHT);
        
        // Load shop background image
        Image bgImage = new Image(getClass().getResourceAsStream(ShopConfig.SHOP_BG_PATH));
        ImageView bgView = new ImageView(bgImage);
        bgView.setFitWidth(ShopConfig.SHOP_WIDTH);
        bgView.setFitHeight(ShopConfig.SHOP_HEIGHT);
        bgView.setPreserveRatio(false); // Stretch to fill entire size
        
        // Create VBox for content
        VBox contentBox = new VBox(15);
        contentBox.setAlignment(Pos.TOP_CENTER);
        contentBox.setPadding(new Insets(ShopConfig.SHOP_PADDING));
        contentBox.setPrefSize(ShopConfig.SHOP_WIDTH, ShopConfig.SHOP_HEIGHT);
        
        // Create HBox for header (title + money display)
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPrefWidth(ShopConfig.SHOP_WIDTH - ShopConfig.SHOP_PADDING * 2);
        
        // Shop title
        Label titleLabel = new Label("SHOP");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setStyle("-fx-effect: dropshadow(one-pass-box, black, 2, 0, 0, 1);");
        
        // Money Display (Top-Right) - Very prominent
        moneyLabel.setFont(Font.font("Arial", FontWeight.BOLD, ShopConfig.MONEY_DISPLAY_FONT_SIZE));
        moneyLabel.setTextFill(ShopConfig.MONEY_DISPLAY_COLOR);
        moneyLabel.setStyle("-fx-effect: dropshadow(one-pass-box, black, 3, 0, 0, 2);");
        updateMoneyDisplay(); // Update money initially
        
        // [SỬA] Thêm margin cho moneyLabel để đẩy nó xuống thấp hơn (đừng dính sát mép trên)
        HBox.setMargin(moneyLabel, new Insets(15, 0, 0, 0)); // Top margin 15px

        // Use Region to push money label to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        headerBox.getChildren().addAll(titleLabel, spacer, moneyLabel);
        
        // Buy/Sell Tabs
        HBox tabBox = new HBox(10);
        tabBox.setAlignment(Pos.CENTER);
        
        buyTabButton = new Button(ShopConfig.TAB_BUY_TEXT);
        buyTabButton.setPrefSize(ShopConfig.TAB_BUTTON_WIDTH, ShopConfig.TAB_BUTTON_HEIGHT);
        buyTabButton.setFont(Font.font("Arial", FontWeight.BOLD, ShopConfig.TAB_BUTTON_FONT_SIZE));
        buyTabButton.setOnAction(e -> switchToBuyMode());
        
        sellTabButton = new Button(ShopConfig.TAB_SELL_TEXT);
        sellTabButton.setPrefSize(ShopConfig.TAB_BUTTON_WIDTH, ShopConfig.TAB_BUTTON_HEIGHT);
        sellTabButton.setFont(Font.font("Arial", FontWeight.BOLD, ShopConfig.TAB_BUTTON_FONT_SIZE));
        sellTabButton.setOnAction(e -> switchToSellMode());
        
        tabBox.getChildren().addAll(buyTabButton, sellTabButton);
        updateTabStyles(); // Update tab styles
        
        // Configure Grid
        itemGrid.setHgap(ShopConfig.SHOP_ITEM_SPACING);
        itemGrid.setVgap(ShopConfig.SHOP_ITEM_SPACING);
        itemGrid.setAlignment(Pos.CENTER);
        
        // Configure message label
        messageLabel.setFont(Font.font("Arial", ShopConfig.MESSAGE_FONT_SIZE));
        messageLabel.setVisible(false);
        messageLabel.setAlignment(Pos.CENTER);
        
        // Add components to contentBox
        contentBox.getChildren().addAll(headerBox, tabBox, itemGrid, messageLabel);
        
        // Create and position Reroll button at bottom-right (fixed position)
        // Add directly to StackPane, not in an overlay that blocks clicks
        rerollButtonBox.setAlignment(Pos.CENTER);
        rerollButtonBox.setPrefSize(ShopConfig.SHOP_ITEM_SLOT_SIZE, ShopConfig.SHOP_ITEM_SLOT_SIZE);
        rerollButtonBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE); // CRITICAL: Prevent StackPane from stretching/centering
        rerollButtonBox.setStyle("-fx-background-color: rgba(100, 50, 0, 0.7); -fx-border-color: orange; -fx-border-width: 3;");
        rerollButtonBox.setVisible(false); // Initially hidden, shown only in Buy mode
        
        // Position Reroll button at bottom-right using StackPane alignment and margin
        StackPane.setAlignment(rerollButtonBox, Pos.BOTTOM_RIGHT);
        
        // [SỬA] Tăng Bottom Margin lên 130px để đẩy Reroll Box lên cao hơn (giống padding item)
        // Insets(Top, Right, Bottom, Left)
        StackPane.setMargin(rerollButtonBox, new Insets(0, 80, 130, 0)); 
        
        // Add to StackPane (background at bottom, content in middle, reroll button on top)
        this.getChildren().addAll(bgView, contentBox, rerollButtonBox);
        
        // Hide shop initially
        this.setVisible(false);
        
        // Update item list (default is Buy mode)
        updateItemList();
    }
    
    /**
     * Switch to Buy mode
     */
    private void switchToBuyMode() {
        if (isSellingMode) {
            isSellingMode = false;
            updateTabStyles();
            updateItemList();
        }
    }
    
    /**
     * Switch to Sell mode
     */
    private void switchToSellMode() {
        if (!isSellingMode) {
            isSellingMode = true;
            updateTabStyles();
            updateItemList();
        }
    }
    
    /**
     * Update styles for Buy/Sell tabs
     */
    private void updateTabStyles() {
        if (isSellingMode) {
            // Sell mode active
            buyTabButton.setStyle(getTabButtonStyle(false));
            sellTabButton.setStyle(getTabButtonStyle(true));
        } else {
            // Buy mode active
            buyTabButton.setStyle(getTabButtonStyle(true));
            sellTabButton.setStyle(getTabButtonStyle(false));
        }
    }
    
    /**
     * Create style for tab button
     */
    private String getTabButtonStyle(boolean isActive) {
        Paint bgColor = isActive ? ShopConfig.TAB_ACTIVE_BG_COLOR : ShopConfig.TAB_INACTIVE_BG_COLOR;
        return String.format("-fx-background-color: #%02X%02X%02X; -fx-text-fill: white; -fx-font-size: %.0fpx; -fx-border-width: 2px; -fx-border-color: white;",
                (int)(((Color)bgColor).getRed() * 255),
                (int)(((Color)bgColor).getGreen() * 255),
                (int)(((Color)bgColor).getBlue() * 255),
                ShopConfig.TAB_BUTTON_FONT_SIZE);
    }
    
    /**
     * Update item list displayed in shop (Buy or Sell mode)
     */
    public void updateItemList() {
        itemGrid.getChildren().clear();
        
        int col = 0;
        int row = 0;
        
        if (!isSellingMode) {
            // Buy Mode: Display items from daily stock
            List<ShopSlot> dailyStock = shopManager.getCurrentDailyStock();
            for (int slotIndex = 0; slotIndex < dailyStock.size(); slotIndex++) {
                ShopSlot slot = dailyStock.get(slotIndex);
                VBox itemBox = createBuyItemBox(slot, slotIndex);
                itemGrid.add(itemBox, col, row);
                
                col++;
                if (col >= ShopConfig.SHOP_GRID_COLS) {
                    col = 0;
                    row++;
                }
            }
            
            // Reroll button is now positioned separately (not in grid) - only visible in Buy mode
            updateRerollButton(true); // Show reroll button in Buy mode
        } else {
            // Sell Mode: Display items from Player's Hotbar
            ItemStack[] hotbarItems = shopManager.getPlayer().getHotbarItems();
            for (int slotIndex = 0; slotIndex < hotbarItems.length; slotIndex++) {
                ItemStack stack = hotbarItems[slotIndex];
                if (stack != null && stack.getQuantity() > 0) {
                    ItemType itemType = stack.getItemType();
                    // Only show items with sellPrice > 0
                    if (itemType.getSellPrice() > 0) {
                        VBox itemBox = createSellItemBox(stack, slotIndex);
                        itemGrid.add(itemBox, col, row);
                        
                        col++;
                        if (col >= ShopConfig.SHOP_GRID_COLS) {
                            col = 0;
                            row++;
                        }
                    }
                }
            }
            
            // Hide reroll button in Sell mode
            updateRerollButton(false);
        }
    }
    
    /**
     * Update Reroll button visibility and content
     * @param visible true to show (Buy mode), false to hide (Sell mode)
     */
    private void updateRerollButton(boolean visible) {
        rerollButtonBox.setVisible(visible);
        rerollButtonBox.setManaged(visible);
        
        if (visible && rerollButtonBox.getChildren().isEmpty()) {
            // Create reroll button content by copying from createRerollButtonBox logic
            // Reroll icon - Use Money Icon from GUI icons
            ImageView rerollIcon = new ImageView();
            Image moneyIconImage = assetManager.getGuiIcon("MONEY");
            if (moneyIconImage != null) {
                rerollIcon.setImage(moneyIconImage);
            }
            rerollIcon.setFitWidth(48);
            rerollIcon.setFitHeight(48);
            rerollIcon.setPreserveRatio(true);
            
            // Reroll label
            Label rerollLabel = new Label("Reroll");
            rerollLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            rerollLabel.setTextFill(Color.WHITE);
            
            // Price label
            Label priceLabel = new Label("$" + ShopConfig.REROLL_PRICE);
            priceLabel.setFont(Font.font("Arial", FontWeight.BOLD, ShopConfig.PRICE_FONT_SIZE));
            priceLabel.setTextFill(Color.GOLD);
            
            // Reroll button
            Button rerollButton = new Button("Reroll");
            rerollButton.setPrefSize(ShopConfig.BUTTON_WIDTH, ShopConfig.BUTTON_HEIGHT);
            rerollButton.setStyle(getRerollButtonStyle());
            
            // Handle reroll button click
            rerollButton.setOnAction(e -> {
                String result = shopManager.rerollStock();
                if (result == null) {
                    showMessage("Shop stock refreshed!", ShopConfig.SUCCESS_TEXT_COLOR);
                    updateMoneyDisplay();
                    updateItemList(); // Refresh grid with new items
                } else {
                    showMessage(result, ShopConfig.ERROR_TEXT_COLOR);
                }
            });
            
            rerollButtonBox.getChildren().addAll(rerollIcon, rerollLabel, priceLabel, rerollButton);
        }
    }
    
    /**
     * Create VBox displaying a shop item in Buy mode (from daily stock)
     */
    private VBox createBuyItemBox(ShopSlot slot, int shopSlotIndex) {
        ItemType itemType = slot.getItemType();
        boolean isSoldOut = slot.isSoldOut();
        
        // Main container - StackPane to allow overlay of sale tag
        StackPane containerPane = new StackPane();
        containerPane.setPrefSize(ShopConfig.SHOP_ITEM_SLOT_SIZE, ShopConfig.SHOP_ITEM_SLOT_SIZE);
        containerPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4); -fx-border-color: gray; -fx-border-width: 2;"); // Reduced opacity to make icons pop out more
        
        // Content VBox - uniform padding for all items (no conditional padding)
        VBox itemBox = new VBox(8);
        itemBox.setAlignment(Pos.CENTER);
        itemBox.setPadding(new Insets(5)); // Uniform padding for all items
        itemBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE); // Prevent stretching to maintain uniform layout
        
        // Icon
        ImageView iconView = new ImageView();
        Image itemIcon = assetManager.getItemIcon(itemType);
        if (itemIcon != null) {
            iconView.setImage(itemIcon);
        }
        iconView.setFitWidth(ShopConfig.ITEM_ICON_SIZE);
        iconView.setFitHeight(ShopConfig.ITEM_ICON_SIZE);
        iconView.setPreserveRatio(true);
        
        // Item name
        Label nameLabel = new Label(itemType.getName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(ShopConfig.SHOP_ITEM_SLOT_SIZE - 10);
        nameLabel.setAlignment(Pos.CENTER);
        
        // Quantity label
        Label qtyLabel = new Label("Quantity: " + slot.getQuantity());
        qtyLabel.setFont(Font.font("Arial", 10));
        qtyLabel.setTextFill(Color.LIGHTGRAY);
        
        // Price display (with discount if on sale)
        HBox priceBox = createBuyPriceBox(slot);
        
        // Buy button
        Button buyButton = new Button(isSoldOut ? "SOLD OUT" : "Buy");
        buyButton.setPrefSize(ShopConfig.BUTTON_WIDTH, ShopConfig.BUTTON_HEIGHT);
        buyButton.setStyle(getButtonStyle());
        buyButton.setDisable(isSoldOut); // Disable if sold out
        
        // Dim the slot if sold out
        if (isSoldOut) {
            itemBox.setOpacity(0.5);
        }
        
        // Handle buy button click
        buyButton.setOnAction(e -> {
            String result = shopManager.buyItem(shopSlotIndex, 1);
            if (result == null) {
                showMessage("Bought " + itemType.getName() + "!", ShopConfig.SUCCESS_TEXT_COLOR);
                updateMoneyDisplay();
                updateItemList(); // Refresh to show updated quantity
                // Sync hotbar immediately after successful purchase
                if (shopManager.getPlayer().getMainGameView() != null) {
                    shopManager.getPlayer().getMainGameView().updateHotbar();
                }
            } else {
                // All error messages from buyItem should be displayed in red (error color)
                // Messages like "Inventory is full!", "Not enough money!", etc. are all errors
                showMessage(result, ShopConfig.ERROR_TEXT_COLOR);
            }
        });
        
        // Buy All button - calculate max amount player can buy
        Button buyAllButton = null;
        if (!isSoldOut) {
            int maxBuyable = calculateMaxBuyable(slot, shopManager.getPlayer().getMoney());
            if (maxBuyable > 1) {
                buyAllButton = new Button("Buy All");
                buyAllButton.setPrefSize(ShopConfig.BUTTON_WIDTH, ShopConfig.BUTTON_HEIGHT);
                buyAllButton.setStyle(getButtonStyle());
                
                buyAllButton.setOnAction(e -> {
                    String result = shopManager.buyItem(shopSlotIndex, maxBuyable);
                    if (result == null) {
                        showMessage("Bought " + maxBuyable + "x " + itemType.getName() + "!", ShopConfig.SUCCESS_TEXT_COLOR);
                        updateMoneyDisplay();
                        updateItemList();
                        if (shopManager.getPlayer().getMainGameView() != null) {
                            shopManager.getPlayer().getMainGameView().updateHotbar();
                        }
                    } else {
                        showMessage(result, ShopConfig.ERROR_TEXT_COLOR);
                    }
                });
            }
        }
        
        if (buyAllButton != null) {
            itemBox.getChildren().addAll(iconView, nameLabel, qtyLabel, priceBox, buyButton, buyAllButton);
        } else {
            itemBox.getChildren().addAll(iconView, nameLabel, qtyLabel, priceBox, buyButton);
        }
        
        // Add itemBox first to containerPane - center it
        StackPane.setAlignment(itemBox, Pos.CENTER);
        containerPane.getChildren().add(itemBox);
        
        // SALE tag as overlay at top-right corner (if on sale) - does not displace content
        if (slot.isOnSale() && !isSoldOut) {
            Label saleTag = new Label("SALE -" + (int)(slot.getDiscountRate() * 100) + "%");
            saleTag.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            saleTag.setTextFill(Color.WHITE); // White text
            saleTag.setStyle("-fx-background-color: rgba(0, 200, 0, 0.9); -fx-padding: 4px 8px;"); // Green background
            saleTag.setAlignment(Pos.CENTER);
            saleTag.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE); // Prevent stretching
            // Position at top-right corner as an overlay (does not affect itemBox layout)
            StackPane.setAlignment(saleTag, Pos.TOP_RIGHT);
            saleTag.setTranslateX(-5); // Small offset from right edge
            saleTag.setTranslateY(5); // Small offset from top edge
            containerPane.getChildren().add(saleTag); // Added after itemBox so it renders on top
        }
        
        // SOLD OUT overlay (if sold out)
        if (isSoldOut) {
            Label soldOutLabel = new Label("SOLD OUT");
            soldOutLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            soldOutLabel.setTextFill(Color.WHITE);
            soldOutLabel.setStyle("-fx-background-color: rgba(200, 0, 0, 0.9); -fx-padding: 5px;");
            containerPane.getChildren().add(soldOutLabel);
        }
        
        return new VBox(containerPane);
    }
    
    /**
     * Create price box for Buy mode (shows discounted price if on sale)
     */
    private HBox createBuyPriceBox(ShopSlot slot) {
        HBox priceBox = new HBox(5);
        priceBox.setAlignment(Pos.CENTER);
        priceBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6); -fx-background-radius: 10; -fx-padding: 2 8 2 8;"); // Styled background pill
        
        // Money icon from GUI icons (replaces Circle placeholder)
        ImageView moneyIconView = new ImageView();
        Image moneyIconImage = assetManager.getGuiIcon("MONEY");
        if (moneyIconImage != null) {
            moneyIconView.setImage(moneyIconImage);
        }
        moneyIconView.setFitWidth(ShopConfig.COIN_ICON_SIZE);
        moneyIconView.setFitHeight(ShopConfig.COIN_ICON_SIZE);
        moneyIconView.setPreserveRatio(true);
        
        // Price label (green if on sale, gold if normal)
        Label priceLabel = new Label(String.valueOf((int)slot.getPrice()));
        priceLabel.setFont(Font.font("Arial", FontWeight.BOLD, ShopConfig.PRICE_FONT_SIZE));
        
        if (slot.isOnSale()) {
            priceLabel.setTextFill(Color.GREEN); // Green for sale price
        } else {
            priceLabel.setTextFill(ShopConfig.PRICE_TEXT_COLOR); // Gold for normal price
        }
        priceLabel.setStyle("-fx-effect: dropshadow(one-pass-box, black, 1, 0, 0, 0);");
        
        priceBox.getChildren().addAll(moneyIconView, priceLabel);
        
        return priceBox;
    }
    
    /**
     * Create style for reroll button (different color to distinguish)
     */
    private String getRerollButtonStyle() {
        return String.format("-fx-background-color: #%02X%02X%02X; -fx-text-fill: white; -fx-font-size: %.0fpx; -fx-font-weight: bold;",
                255, 140, 0, // Orange color
                ShopConfig.BUTTON_FONT_SIZE);
    }
    
    /**
     * Create VBox displaying an item in Sell mode (from player inventory)
     */
    private VBox createSellItemBox(ItemStack stack, int slotIndex) {
        ItemType itemType = stack.getItemType();
        int quantity = stack.getQuantity();
        
        // Main container - StackPane to match Buy Tab structure
        StackPane containerPane = new StackPane();
        containerPane.setPrefSize(ShopConfig.SHOP_ITEM_SLOT_SIZE, ShopConfig.SHOP_ITEM_SLOT_SIZE);
        containerPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4); -fx-border-color: gray; -fx-border-width: 2;"); // Same style as Buy Tab
        
        // Content VBox - uniform padding for all items (same as Buy Tab)
        VBox itemBox = new VBox(8);
        itemBox.setAlignment(Pos.CENTER);
        itemBox.setPadding(new Insets(5)); // Uniform padding to prevent buttons from touching edges
        itemBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE); // Prevent stretching to maintain uniform layout
        
        // Icon
        ImageView iconView = new ImageView();
        Image itemIcon = assetManager.getItemIcon(itemType);
        if (itemIcon != null) {
            iconView.setImage(itemIcon);
        }
        iconView.setFitWidth(ShopConfig.ITEM_ICON_SIZE);
        iconView.setFitHeight(ShopConfig.ITEM_ICON_SIZE);
        iconView.setPreserveRatio(true);
        
        // Item name + quantity (ensure text wrapping and centering)
        Label nameLabel = new Label(itemType.getName() + " x" + quantity);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11)); // Match Buy Tab font size
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(ShopConfig.SHOP_ITEM_SLOT_SIZE - 10);
        nameLabel.setAlignment(Pos.CENTER);
        
        // Sell price with coin icon
        HBox priceBox = createPriceBox(itemType.getSellPrice());
        
        // Sell button
        Button sellButton = new Button("Sell");
        sellButton.setPrefSize(ShopConfig.BUTTON_WIDTH, ShopConfig.BUTTON_HEIGHT);
        sellButton.setStyle(getButtonStyle());
        
        // Handle sell button click
        sellButton.setOnAction(e -> {
            String result = shopManager.sellItem(slotIndex, 1);
            if (result == null) {
                showMessage("Sold " + itemType.getName() + "!", ShopConfig.SUCCESS_TEXT_COLOR);
                updateMoneyDisplay();
                updateItemList(); // Refresh grid to update quantity
                // Sync hotbar immediately after successful sale
                if (shopManager.getPlayer().getMainGameView() != null) {
                    shopManager.getPlayer().getMainGameView().updateHotbar();
                }
            } else {
                showMessage(result, ShopConfig.ERROR_TEXT_COLOR);
            }
        });
        
        // Sell All button
        Button sellAllButton = null;
        if (quantity > 1) {
            sellAllButton = new Button("Sell All");
            sellAllButton.setPrefSize(ShopConfig.BUTTON_WIDTH, ShopConfig.BUTTON_HEIGHT);
            sellAllButton.setStyle(getButtonStyle());
            
            sellAllButton.setOnAction(e -> {
                String result = shopManager.sellItem(slotIndex, quantity);
                if (result == null) {
                    showMessage("Sold " + quantity + "x " + itemType.getName() + "!", ShopConfig.SUCCESS_TEXT_COLOR);
                    updateMoneyDisplay();
                    updateItemList();
                    if (shopManager.getPlayer().getMainGameView() != null) {
                        shopManager.getPlayer().getMainGameView().updateHotbar();
                    }
                } else {
                    showMessage(result, ShopConfig.ERROR_TEXT_COLOR);
                }
            });
        }
        
        // Add components to itemBox (with proper spacing from VBox)
        if (sellAllButton != null) {
            itemBox.getChildren().addAll(iconView, nameLabel, priceBox, sellButton, sellAllButton);
        } else {
            itemBox.getChildren().addAll(iconView, nameLabel, priceBox, sellButton);
        }
        
        // Add itemBox to containerPane - center it
        StackPane.setAlignment(itemBox, Pos.CENTER);
        containerPane.getChildren().add(itemBox);
        
        // Return VBox wrapping StackPane (to match Buy Tab return structure)
        return new VBox(containerPane);
    }
    
    /**
     * Calculate maximum number of items player can buy based on money and stock
     * @param slot ShopSlot to buy from
     * @param playerMoney Player's current money
     * @return Maximum buyable quantity
     */
    private int calculateMaxBuyable(ShopSlot slot, double playerMoney) {
        int stock = slot.getQuantity();
        double price = slot.getPrice();
        if (price <= 0) return 0;
        
        int maxAffordable = (int)(playerMoney / price);
        return Math.min(stock, maxAffordable);
    }
    
    /**
     * Create style for button
     */
    private String getButtonStyle() {
        return String.format("-fx-background-color: #%02X%02X%02X; -fx-text-fill: white; -fx-font-size: %.0fpx;",
                (int)(((Color)ShopConfig.BUTTON_BG_COLOR).getRed() * 255),
                (int)(((Color)ShopConfig.BUTTON_BG_COLOR).getGreen() * 255),
                (int)(((Color)ShopConfig.BUTTON_BG_COLOR).getBlue() * 255),
                ShopConfig.BUTTON_FONT_SIZE);
    }
    
    /**
     * Create HBox displaying price with coin icon (for Sell mode)
     */
    private HBox createPriceBox(int price) {
        HBox priceBox = new HBox(5);
        priceBox.setAlignment(Pos.CENTER);
        priceBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6); -fx-background-radius: 10; -fx-padding: 2 8 2 8;"); // Styled background pill
        
        // Money icon from GUI icons (replaces Circle placeholder)
        ImageView moneyIconView = new ImageView();
        Image moneyIconImage = assetManager.getGuiIcon("MONEY");
        if (moneyIconImage != null) {
            moneyIconView.setImage(moneyIconImage);
        }
        moneyIconView.setFitWidth(ShopConfig.COIN_ICON_SIZE);
        moneyIconView.setFitHeight(ShopConfig.COIN_ICON_SIZE);
        moneyIconView.setPreserveRatio(true);
        
        // Price text
        Label priceLabel = new Label(String.valueOf(price));
        priceLabel.setFont(Font.font("Arial", FontWeight.BOLD, ShopConfig.PRICE_FONT_SIZE));
        priceLabel.setTextFill(ShopConfig.PRICE_TEXT_COLOR);
        priceLabel.setStyle("-fx-effect: dropshadow(one-pass-box, black, 1, 0, 0, 0);");
        
        priceBox.getChildren().addAll(moneyIconView, priceLabel);
        
        return priceBox;
    }
    
    /**
     * Update player's money display
     */
    public void updateMoneyDisplay() {
        double money = shopManager.getPlayer().getMoney();
        moneyLabel.setText("$" + (int)money);
    }
    
    /**
     * Show message
     */
    private void showMessage(String message, Paint color) {
        messageLabel.setText(message);
        messageLabel.setTextFill(color);
        messageLabel.setVisible(true);
        
        // Hide message after 3 seconds
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
            javafx.util.Duration.seconds(3)
        );
        pause.setOnFinished(e -> messageLabel.setVisible(false));
        pause.play();
    }
    
    /**
     * Show/hide shop
     */
    public void toggle() {
        boolean currentVisibility = this.isVisible();
        this.setVisible(!currentVisibility);
        if (!currentVisibility) {
            updateMoneyDisplay(); // Update money when opening shop
            updateItemList(); // Update item list when opening shop
            // Ensure shop is always on top when opened
            this.toFront();
        }
    }
    
    /**
     * Check if shop is currently visible
     */
    public boolean isShopVisible() {
        return this.isVisible();
    }
}