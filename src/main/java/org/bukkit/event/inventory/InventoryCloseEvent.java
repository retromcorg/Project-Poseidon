package org.bukkit.event.inventory;

import org.bukkit.inventory.InventoryView;

/**
 * Called when a player closes their current inventory
 */
public class InventoryCloseEvent extends InventoryEvent {

    public InventoryCloseEvent(InventoryView transaction) {
        super(Type.INVENTORY_CLOSE, transaction);
    }
}
