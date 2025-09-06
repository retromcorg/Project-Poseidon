package org.bukkit.inventory;

public enum InventoryType {
    CHEST(27, "Chest"),
    LARGE_CHEST(54, "Large chest"),
    DISPENSER(9, "Dispenser"),
    FURNACE(3, "Furnace"),
    WORKBENCH(10, "Crafting"),
    CRAFTING(5, "Crafting"),
    PLAYER(36, "Player"),
    CUSTOM(27, "Chest");

    private final int size;
    private final String title;

    InventoryType(int defaultSize, String defaultTitle) {
        size = defaultSize;
        title = defaultTitle;
    }

    public int getDefaultSize() {
        return size;
    }

    public String getDefaultTitle() {
        return title;
    }
}
