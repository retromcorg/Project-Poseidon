package org.bukkit.event.inventory;

import org.bukkit.event.Cancellable;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SlotType;

/**
 * Called when a player clicks on an inventory slot in their inventory
 */
public class InventoryClickEvent extends InventoryEvent implements Cancellable {
    private Result result;
    private boolean rightClick;
    private boolean shiftClick;
    private SlotType slotType;
    private int slot;
    private int rawSlot;
    private ItemStack current;

    public InventoryClickEvent(InventoryView transaction, SlotType slotType, int rawSlot, boolean right, boolean shift) {
        super(Type.INVENTORY_CLICK, transaction);
        this.slotType = slotType;
        this.slot = transaction.convertSlot(rawSlot);
        this.rawSlot = rawSlot;
        this.rightClick = right;
        this.shiftClick = shift;
        this.result = Result.DEFAULT;
    }

    /**
     * The slot number that was clicked, ready for passing to {@link org.bukkit.inventory.Inventory#getItem(int)}. Note
     * that there may be two slots with the same slot number, since a view links two different inventories.
     * @return The slot number.
     */
    public int getSlot() {
        return slot;
    }

    public int getRawSlot() {
        return rawSlot;
    }

    /**
     * Get the type of slot that was clicked.
     * @return The slot type.
     */
    public SlotType getSlotType() {
        return slotType;
    }

    /**
     * @return True if the click is a right-click.
     */
    public boolean isRightClick() {
        return rightClick;
    }

    /**
     * @return True if the click is a left-click.
     */
    public boolean isLeftClick() {
        return !rightClick;
    }

    /**
     * Shift can be combined with right-click or left-click as a modifier.
     *
     * @return True if the click is a shift-click
     */
    public boolean isShiftClick() {
        return shiftClick;
    }

    /**
     * Returns the current item in the clicked slot.
     *
     * @return The slot item
     */
    public ItemStack getCurrentItem() {
        return slotType == SlotType.OUTSIDE ? current : getView().getItem(rawSlot);
    }

    /**
     * Set the current item in the slot.
     * @param item The new slot item.
     */
    public void setCurrentItem(ItemStack item) {
        if (slotType == SlotType.OUTSIDE) current = item;
        else getView().setItem(rawSlot, item);
    }

    /**
     * Get the current item on the cursor.
     * @return The cursor item
     */
    public ItemStack getCursor() {
        return getView().getCursor();
    }

    /**
     * Set the item on the cursor.
     * @param item The new cursor item.
     */
    public void setCursor(ItemStack item) {
        getView().setCursor(item);
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public boolean isCancelled() {
        return result == Result.DENY;
    }

    public void setCancelled(boolean cancel) {
        result = cancel ? Result.DENY : Result.ALLOW;
    }
}
