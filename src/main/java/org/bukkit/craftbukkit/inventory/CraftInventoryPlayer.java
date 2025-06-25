package org.bukkit.craftbukkit.inventory;

import net.minecraft.server.InventoryPlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class CraftInventoryPlayer extends CraftInventory implements PlayerInventory {
    public CraftInventoryPlayer(net.minecraft.server.InventoryPlayer inventory) {
        super(inventory);
    }

    public InventoryPlayer getInventory() {
        return (InventoryPlayer) inventory;
    }

    public int getSize() {
        return super.getSize() - 4;
    }

    public ItemStack getItemInHand() {
        return new CraftItemStack(getInventory().getItemInHand());
    }

    public void setItemInHand(ItemStack stack) {
        setItem(getHeldItemSlot(), stack);
    }

    public int getHeldItemSlot() {
        return getInventory().itemInHandIndex;
    }

    public ItemStack getHelmet() {
        return getItem(getSize() + 3);
    }

    public ItemStack getChestplate() {
        return getItem(getSize() + 2);
    }

    public ItemStack getLeggings() {
        return getItem(getSize() + 1);
    }

    public ItemStack getBoots() {
        return getItem(getSize());
    }

    public void setHelmet(ItemStack helmet) {
        setItem(getSize() + 3, helmet);
    }

    public void setChestplate(ItemStack chestplate) {
        setItem(getSize() + 2, chestplate);
    }

    public void setLeggings(ItemStack leggings) {
        setItem(getSize() + 1, leggings);
    }

    public void setBoots(ItemStack boots) {
        setItem(getSize(), boots);
    }

    public ItemStack[] getArmorContents() {
        return new ItemStack[] { getBoots(), getLeggings(), getChestplate(), getHelmet() };
    }

    public void setArmorContents(ItemStack[] items) {
        int cnt = getSize();

        if (items == null) {
            items = new ItemStack[4];
        }
        for (ItemStack item : items) {
            if (item == null || item.getTypeId() == 0) {
                clear(cnt++);
            } else {
                setItem(cnt++, item);
            }
        }
    }

    // Poseidon start - Backport modern Inventory API
    @Override
    public HumanEntity getHolder() {
        return (HumanEntity) super.getHolder();
    }
    // Poseidon end
}
