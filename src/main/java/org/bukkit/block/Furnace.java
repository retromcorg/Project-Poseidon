package org.bukkit.block;

import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Represents a furnace.
 *
 * @author sk89q
 */
public interface Furnace extends BlockState, ContainerBlock, InventoryHolder { // Poseidon - Backport modern Inventory API

    /**
     * Get burn time.
     *
     * @return
     */
    public short getBurnTime();

    /**
     * Set burn time.
     *
     * @param burnTime
     */
    public void setBurnTime(short burnTime);

    /**
     * Get cook time.
     *
     * @return
     */
    public short getCookTime();

    /**
     * Set cook time.
     *
     * @param cookTime
     */
    public void setCookTime(short cookTime);

    public FurnaceInventory getInventory(); // Poseidon - Backport modern Inventory API
}
