package net.minecraft.server;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class InventoryCrafting implements IInventory {

    private ItemStack[] items;
    private int b;
    private Container c;

    // CraftBukkit start
    public ItemStack[] getContents() {
        return this.items;
    }
    // CraftBukkit end

    public InventoryCrafting(Container container, int i, int j) {
        int k = i * j;

        this.items = new ItemStack[k];
        this.c = container;
        this.b = i;
    }

    public int getSize() {
        return this.items.length;
    }

    public ItemStack getItem(int i) {
        return i >= this.getSize() ? null : this.items[i];
    }

    public ItemStack b(int i, int j) {
        if (i >= 0 && i < this.b) {
            int k = i + j * this.b;

            return this.getItem(k);
        } else {
            return null;
        }
    }

    public String getName() {
        return "Crafting";
    }

    public ItemStack splitStack(int i, int j) {
        if (this.items[i] != null) {
            ItemStack itemstack;

            if (this.items[i].count <= j) {
                itemstack = this.items[i];
                this.items[i] = null;
                this.c.a((IInventory) this);
                return itemstack;
            } else {
                itemstack = this.items[i].a(j);
                if (this.items[i].count == 0) {
                    this.items[i] = null;
                }

                this.c.a((IInventory) this);
                return itemstack;
            }
        } else {
            return null;
        }
    }

    public void setItem(int i, ItemStack itemstack) {
        this.items[i] = itemstack;
        this.c.a((IInventory) this);
    }

    public int getMaxStackSize() {
        return 64;
    }

    public void update() {}

    public boolean a_(EntityHuman entityhuman) {
        return true;
    }

    /**
     * Set the players 2x2 crafting grid.
     * This is not a perfect clone of the modern version.
     * This uses a static method instead with the player passed in.
     * 
     * @param grid The {@link ItemStack}[] to set the crafting grid to
     * @param player The {@link Player} to modify. This is not necessary in modern, but this is a quick addition
     */
    public static void setCraftingMatrix(final org.bukkit.inventory.ItemStack[] grid, final Player player) {
        final int slotCount = grid.length;
        if (slotCount > 4)
            throw new IllegalArgumentException("The number of items must be 4 or less");
        
        final EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        
        final NetServerHandler netServerHandler = entityPlayer.netServerHandler;
        final InventoryCrafting inventoryCrafting = ((ContainerPlayer) entityPlayer.defaultContainer).craftInventory;

        final ItemStack[] fullGrid = new ItemStack[4];
        for (int slot = 0; slot < slotCount; slot++) {
            fullGrid[slot] = new ItemStack(grid[slot]);
        }

        for(int slot = 0; slot < 4; slot++) {
            ItemStack item = fullGrid[slot];

            Packet103SetSlot packet = new Packet103SetSlot(0, slot, item);
            netServerHandler.sendPacket(packet);
    
            inventoryCrafting.setItem(slot, item);
        }
    }
}
