package org.bukkit.event.inventory;

import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.SlotType;

public class CraftItemEvent extends InventoryClickEvent {

    private Recipe recipe;

    public CraftItemEvent(Recipe recipe, InventoryView transaction, SlotType slotType, int rawSlot, boolean right, boolean shift) {
        super(transaction, slotType, rawSlot, right, shift);
        this.recipe = recipe;
    }

    /**
     * @return A copy of the current recipe on the crafting matrix.
     */
    public Recipe getRecipe() {
        return recipe;
    }
}
