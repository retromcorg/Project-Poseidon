package net.minecraft.server;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;

public class InventoryLargeChest implements IInventory {

    private String a;
    public IInventory b; // Poseidon - private -> public
    public IInventory c; // Poseidon - private -> public
    private List<HumanEntity> viewers = new ArrayList<HumanEntity>(); // Poseidon

    // CraftBukkit start
    public ItemStack[] getContents() {
        ItemStack[] result = new ItemStack[this.getSize()];
        for (int i = 0; i < result.length; i++) {
            result[i] = this.getItem(i);
        }
        return result;
    }
    // CraftBukkit end

    public InventoryLargeChest(String s, IInventory iinventory, IInventory iinventory1) {
        this.a = s;
        this.b = iinventory;
        this.c = iinventory1;
    }

    public int getSize() {
        return this.b.getSize() + this.c.getSize();
    }

    public String getName() {
        return this.a;
    }

    public ItemStack getItem(int i) {
        return i >= this.b.getSize() ? this.c.getItem(i - this.b.getSize()) : this.b.getItem(i);
    }

    public ItemStack splitStack(int i, int j) {
        return i >= this.b.getSize() ? this.c.splitStack(i - this.b.getSize(), j) : this.b.splitStack(i, j);
    }

    public void setItem(int i, ItemStack itemstack) {
        if (i >= this.b.getSize()) {
            this.c.setItem(i - this.b.getSize(), itemstack);
        } else {
            this.b.setItem(i, itemstack);
        }
    }

    public int getMaxStackSize() {
        return this.b.getMaxStackSize();
    }

    public void update() {
        this.b.update();
        this.c.update();
    }

    public boolean a_(EntityHuman entityhuman) {
        return this.b.a_(entityhuman) && this.c.a_(entityhuman);
    }

    // Poseidon start
    public List<HumanEntity> getViewers() {
        return viewers;
    }

    public void onOpen(HumanEntity player) {
        b.onOpen(player);
        c.onOpen(player);
        viewers.add(player);
    }

    public void onClose(HumanEntity player) {
        b.onClose(player);
        c.onClose(player);
        viewers.remove(player);
    }

    public InventoryHolder getOwner() {
        return null;
    }
    // Poseidon end
}
