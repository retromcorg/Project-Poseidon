package org.bukkit.entity;

import org.bukkit.Location;
import org.bukkit.inventory.*;
import org.bukkit.permissions.Permissible;

/**
 * Represents a human entity, such as an NPC or a player
 */
public interface HumanEntity extends LivingEntity, AnimalTamer, Permissible, InventoryHolder { // Poseidon - Backport modern Inventory API

    /**
     * Returns the name of this player
     *
     * @return Player name
     */
    public String getName();

    /**
     * Returns the player's inventory.
     *
     * @return The inventory of the player, this also contains the armor slots
     */
    public PlayerInventory getInventory();

    // Poseidon start - Backport modern Inventory API
    /**
     * Returns the inventory view the player is currently viewing. If they do not have
     * an inventory window open, it returns their internal crafting view.
     * @return The inventory view
     */
    public InventoryView getOpenInventory();

    /**
     * Opens an inventory window with the specified inventory on the top and the player's inventory
     * on the bottom.
     * @param inventory The inventory to open
     * @return The newly opened inventory view
     */
    public InventoryView openInventory(Inventory inventory);

    /**
     * Opens an empty workbench inventory window with the player's inventory on the bottom.
     * @param location The location to attach it to. If null, the player's location is used.
     * @param force If false, and there is no workbench block at the location, no inventory will be
     * opened and null will be returned.
     * @return The newly opened inventory view, or null if it could not be opened
     */
    public InventoryView openWorkbench(Location location, boolean force);

    /**
     * Opens an inventory window to the specified inventory view
     * @param inventory The view to open
     */
    public void openInventory(InventoryView inventory);

    /**
     * Force-closes the currently open inventory view for this player, if any.
     */
    public void closeInventory();
    // Poseidon end

    /**
     * Returns the ItemStack currently in your hand, can be empty.
     *
     * @return The ItemStack of the item you are currently holding.
     */
    public ItemStack getItemInHand();

    /**
     * Sets the item to the given ItemStack, this will replace whatever the
     * user was holding.
     *
     * @param item The ItemStack which will end up in the hand
     * @return
     */
    public void setItemInHand(ItemStack item);

    // Poseidon start - Backport modern Inventory API
    /**
     * Returns the ItemStack currently on your cursor, can be empty.
     * Will always be empty if the player currently has no open window.
     *
     * @return The ItemStack of the item you are currently moving around
     */
    public ItemStack getItemOnCursor();

    /**
     * Sets the item to the given ItemStack, this will replace whatever the
     * user was moving. Will always be empty if the player currently has no open window.
     *
     * @param item The ItemStack which will end up in the hand
     */
    public void setItemOnCursor(ItemStack item);
    // Poseidon end

    /**
     * Changes the item in hand to another of your 'action slots'.
     *
     * @param index The new index to use, only valid ones are 0-8.
     *
     public void selectItemInHand(int index);
     */

    /**
     * Returns whether this player is slumbering.
     *
     * @return slumber state
     */
    public boolean isSleeping();

    /**
     * Get the sleep ticks of the player. This value may be capped.
     *
     * @return slumber ticks
     */
    public int getSleepTicks();
}
