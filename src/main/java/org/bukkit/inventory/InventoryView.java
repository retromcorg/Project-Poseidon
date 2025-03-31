package org.bukkit.inventory;

import org.bukkit.entity.HumanEntity;

/**
 * Represents a view linking two inventories and a single player
 * (whose inventory may or may not be one of the two)
 */
public abstract class InventoryView {

    public static final int OUTSIDE = -999;

    /**
     * Returns the upper inventory involved in this transaction
     * @return The inventory
     */
    public abstract Inventory getTopInventory();

    /**
     * Returns the lower inventory involved in this transaction
     * @return The inventory
     */
    public abstract Inventory getBottomInventory();

    /**
     * Returns the inventory type of the top inventory
     *
     * @return The inventory type
     */
    public abstract InventoryType getType();

    /**
     * Returns the viewing player
     *
     * @return The player
     */
    public abstract HumanEntity getPlayer();

    /**
     * Sets one item in this inventory view by its raw slot ID.
     * @param slot The ID as returned by InventoryClickEvent.getRawSlot()
     * @param item The new item to put in the slot, or null to clear it
     */
    public void setItem(int slot, ItemStack item) {
        if (slot != OUTSIDE) {
            if (slot < getTopInventory().getSize()) {
                getTopInventory().setItem(convertSlot(slot),item);
            } else {
                getBottomInventory().setItem(convertSlot(slot),item);
            }
        }
    }

    /**
     * Returns an item in this inventory view by its raw slot ID.
     *
     * @param slot The ID as returned by InventoryClickEvent.getRawSlot()
     * @return The item currently in the slot
     */
    public ItemStack getItem(int slot) {
        if (slot == OUTSIDE) {
            return null;
        }
        if (slot < getTopInventory().getSize()) {
            return getTopInventory().getItem(convertSlot(slot));
        } else {
            return getBottomInventory().getItem(convertSlot(slot));
        }
    }

    /**
     * Sets the item on the cursor of the viewing player.
     *
     * @param item The item to put on the cursor, or null to remove the item on their cursor
     */
    public final void setCursor(ItemStack item) {
        getPlayer().setItemOnCursor(item);
    }

    /**
     * Returns the item on the cursor of the viewing player.
     *
     * @return The item on the player's cursor, or null if they aren't holding one
     */
    public final ItemStack getCursor() {
        return getPlayer().getItemOnCursor();
    }

    /**
     * Converts a raw slot ID into its local slot ID into whichever of the two inventories
     * the slot points to. If the raw slot refers to the upper inventory, it will be returned
     * unchanged and thus be suitable for getTopInventory().getItem(); if it refers to the
     * lower inventory, the output will differ from the input and be suitable for
     * getBottomInventory().getItem().
     *
     * @param rawSlot The raw slot ID
     * @return The converted slot ID
     */
    public final int convertSlot(int rawSlot) {
        int numInTop = getTopInventory().getSize();
        if (rawSlot < numInTop) {
            return rawSlot;
        }
        int slot = rawSlot - numInTop;
        if (getType() == InventoryType.CRAFTING) {
            if(slot < 4) return 39 - slot;
            else slot -= 4;
        }
        if (slot >= 27) slot -= 27;
        else slot += 9;
        return slot;
    }

    /**
     * Check the total number of slots in this view, combining the upper and lower inventories.
     * Note though that it's possible for this to be greater than the sum of the two inventories
     * if for example some slots are not being used.
     * @return The total size
     */
    public final int countSlots() {
        return getTopInventory().getSize() + getBottomInventory().getSize();
    }

    /**
     * Closes the inventory view.
     */
    public final void close() {
        getPlayer().closeInventory();
    }
}
