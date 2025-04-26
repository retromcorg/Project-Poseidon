package net.minecraft.server;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;

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

    // Poseidon start - Backport modern Inventory API
    List<Player> getViewers();

    void onOpen(Player player);

    void onClose(Player player);

    InventoryHolder getOwner();
    // Poseidon end
}
