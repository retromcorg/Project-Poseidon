package org.bukkit.inventory;

public enum SlotType {
    /**
     * A result slot in a furnace or crafting inventory.
     */
    RESULT,
    /**
     * A slot in the crafting matrix, or the input slot in a furnace inventory.
     */
    CRAFTING,
    /**
     * An armour slot in the player's inventory.
     */
    ARMOR,
    /**
     * A regular slot in the container or the player's inventory; anything not covered
     * by the other enum values.
     */
    CONTAINER,
    /**
     * A slot in the bottom row or quickbar.
     */
    QUICKBAR,
    /**
     * A pseudo-slot representing the area outside the inventory window.
     */
    OUTSIDE,
    /**
     * The fuel slot in a furnace inventory.
     */
    FUEL
}