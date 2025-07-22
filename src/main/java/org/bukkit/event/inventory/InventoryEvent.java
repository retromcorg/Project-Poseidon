package org.bukkit.event.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

/**
 * Represents a player related inventory event
 */
public class InventoryEvent extends Event {
    protected InventoryView transaction;

    public InventoryEvent(final Type type, InventoryView transaction) {
        super(type);
        this.transaction = transaction;
    }

    /**
     * Returns the primary inventory involved in this transaction
     *
     * @return The upper inventory
     */
    public Inventory getInventory() {
        return transaction.getTopInventory();
    }

    /**
     * Returns the view object itself
     *
     * @return The inventory view
     */
    public InventoryView getView() {
        return transaction;
    }

    /**
     * Returns the player viewing the inventory
     *
     * @return The player
     */
    public Player getPlayer() {
        return (Player) transaction.getPlayer();
    }
}
