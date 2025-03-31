package org.bukkit.inventory;

public interface DoubleChestInventory {

    /**
     * Returns the left half of this double chest.
     * @return The left side inventory
     */
    Inventory getLeftSide();

    /**
     * Returns the right side of this double chest.
     * @return The right side inventory
     */
    Inventory getRightSide();
}
