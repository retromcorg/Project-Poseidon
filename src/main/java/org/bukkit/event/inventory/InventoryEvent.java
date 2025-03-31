package org.bukkit.event.inventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.util.List;

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
     * Returns the list of players viewing the primary (upper) inventory involved in this event
     *
     * @return A list of people viewing
     */
    public List<HumanEntity> getViewers() {
        return transaction.getTopInventory().getViewers();
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
    public HumanEntity getPlayer() {
        return transaction.getPlayer();
    }
}
