package org.bukkit.craftbukkit.inventory;

import net.minecraft.server.Container;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.*;

public class CraftInventoryView extends InventoryView {

    private CraftHumanEntity player;
    private CraftInventory inventory;
    private Container container;

    public CraftInventoryView(HumanEntity player, Inventory inventory, Container container) {
        this.player = (CraftHumanEntity) player;
        this.inventory = (CraftInventory) inventory;
        this.container = container;
    }

    public Inventory getTopInventory() {
        return inventory;
    }

    public Inventory getBottomInventory() {
        return player.getInventory();
    }

    public InventoryType getType() {
        return inventory.getType();
    }

    public HumanEntity getPlayer() {
        return player;
    }

    @Override
    public void setItem(int slot, org.bukkit.inventory.ItemStack item) {
        if (slot != -999) {
            container.b(slot).c(item == null ? null : new net.minecraft.server.ItemStack(item.getTypeId(), item.getAmount(), item.getDurability()));
        }
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot == -999) {
            return null;
        }
        return new CraftItemStack(container.b(slot).getItem());
    }

    public boolean isInTop(int rawSlot) {
        return rawSlot < inventory.getSize();
    }

    public Container getHandle() {
        return container;
    }

    public static SlotType getSlotType(InventoryView inventory, int slot) {
        SlotType type = SlotType.CONTAINER;
        if (slot < inventory.getTopInventory().getSize()) {
            switch(inventory.getType()) {
                case FURNACE:
                    if (slot == 2) {
                        type = SlotType.RESULT;
                    } else if(slot == 1) {
                        type = SlotType.FUEL;
                    }
                    break;
                case WORKBENCH:
                case CRAFTING:
                    if (slot == 0) {
                        type = SlotType.RESULT;
                    } else {
                        type = SlotType.CRAFTING;
                    }
                    break;
                default:
            }
        } else {
            if (slot == -999) {
                type = SlotType.OUTSIDE;
            } else if (inventory.getType() == InventoryType.CRAFTING && slot < 9) {
                type = SlotType.ARMOR;
            } else if (slot >= (inventory.countSlots() - 9)) {
                type = SlotType.QUICKBAR;
            }
        }
        return type;
    }
}
