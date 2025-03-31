package org.bukkit.event.inventory;

import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.Recipe;

public class PrepareItemCraftEvent extends InventoryEvent {

    private CraftingInventory matrix;

    public PrepareItemCraftEvent(CraftingInventory matrix, InventoryView transaction) {
        super(Type.PREPARE_ITEM_CRAFT, transaction);
        this.matrix = matrix;
    }

    /**
     * Get the recipe that has been formed.
     * @return The recipe being crafted.
     */
    public Recipe getRecipe() {
        return matrix.getRecipe();
    }

    /**
     * @return The crafting inventory on which the recipe was formed.
     */
    @Override
    public CraftingInventory getInventory() {
        return matrix;
    }
}
