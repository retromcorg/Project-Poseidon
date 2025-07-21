package net.minecraft.server;

import org.bukkit.inventory.InventoryHolder;

public interface IInventory {

    int getSize();

    ItemStack getItem(int i);

    ItemStack splitStack(int i, int j);

    void setItem(int i, ItemStack itemstack);

    String getName();

    int getMaxStackSize();

    void update();

    boolean a_(EntityHuman entityhuman);

    ItemStack[] getContents(); // CraftBukkit

    InventoryHolder getHolder(); // Poseidon - Backport modern Inventory API
}
