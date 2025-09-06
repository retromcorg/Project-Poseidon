package org.bukkit.craftbukkit.inventory;

import net.minecraft.server.InventoryLargeChest;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CraftInventoryDoubleChest extends CraftInventory implements DoubleChestInventory {

    private CraftInventory left, right;

    public CraftInventoryDoubleChest(CraftInventory left, CraftInventory right) {
        super(new InventoryLargeChest("Large chest", left.getInventory(), right.getInventory()));
        this.left = left;
        this.right = right;
    }

    public CraftInventoryDoubleChest(InventoryLargeChest largeChest) {
        super(largeChest);
        if (largeChest.b instanceof InventoryLargeChest) {
            left = new CraftInventoryDoubleChest((InventoryLargeChest)largeChest.b);
        } else {
            left = new CraftInventory(largeChest.b);
        }
        if (largeChest.c instanceof InventoryLargeChest) {
            right = new CraftInventoryDoubleChest((InventoryLargeChest)largeChest.c);
        } else {
            right = new CraftInventory(largeChest.c);
        }
    }

    public Inventory getLeftSide() {
        return left;
    }

    public Inventory getRightSide() {
        return right;
    }

    @Override
    public void setContents(ItemStack[] items) {
        if (getInventory().getContents().length != items.length) {
            throw new IllegalArgumentException("Invalid inventory size; expected " + getInventory().getContents().length + " and got " + items.length);
        }

        ItemStack[] leftItems = new ItemStack[left.getSize()];
        ItemStack[] rightItems = new ItemStack[right.getSize()];
        System.arraycopy(items, 0, leftItems, 0, left.getSize());
        left.setContents(leftItems);
        System.arraycopy(items, left.getSize(), rightItems, 0, right.getSize());
        right.setContents(rightItems);
    }
}
