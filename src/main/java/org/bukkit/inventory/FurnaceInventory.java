package org.bukkit.inventory;

import org.bukkit.block.Furnace;

public interface FurnaceInventory extends Inventory {

    /**
     * Returns the current item in the result slot.
     * @return The item
     */
    ItemStack getResult();

    /**
     * Returns the current fuel.
     * @return The item
     */
    ItemStack getFuel();

    /**
     * Returns the item currently smelting.
     * @return The item
     */
    ItemStack getSmelting();

    /**
     * Set the current fuel.
     * @param stack The item
     */
    void setFuel(ItemStack stack);

    /**
     * Set the current item in the result slot.
     * @param stack The item
     */
    void setResult(ItemStack stack);

    /**
     * Set the item currently smelting.
     * @param stack The item
     */
    void setSmelting(ItemStack stack);

    Furnace getHolder();
}
