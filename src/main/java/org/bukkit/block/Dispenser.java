package org.bukkit.block;

import org.bukkit.inventory.InventoryHolder;

/**
 * Represents a dispenser.
 *
 * @author sk89q
 */
public interface Dispenser extends BlockState, ContainerBlock, InventoryHolder { // Poseidon - Backport modern Inventory API

    /**
     * Attempts to dispense the contents of this block<br />
     * <br />
     * If the block is no longer a dispenser, this will return false
     *
     * @return true if successful, otherwise false
     */
    public boolean dispense();
}
