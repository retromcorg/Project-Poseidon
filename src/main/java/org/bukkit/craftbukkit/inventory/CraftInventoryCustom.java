package org.bukkit.craftbukkit.inventory;

import net.minecraft.server.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryType;

/**
 * Represents a custom inventory.
 * <br>
 * It is highly recommended to use this class to show inventories of all types to players.
 */
public class CraftInventoryCustom extends CraftInventory {

    public CraftInventoryCustom(InventoryHolder owner, InventoryType type) {
        super(createInventory(owner, type));
    }

    public CraftInventoryCustom(InventoryHolder owner, int size) {
        super(new MinecraftInventory(owner, size));
    }

    public CraftInventoryCustom(InventoryHolder owner, String title, int size) {
        super(new MinecraftInventory(owner, title, size));
    }

    public CraftInventoryCustom(InventoryHolder owner, String title, int size, int stackSize) {
        super(new MinecraftInventory(owner, title, size, stackSize));
    }

    private static IInventory createInventory(InventoryHolder owner, InventoryType type) {
        switch (type) {
            case LARGE_CHEST:
                return new InventoryLargeChest("Large chest", new TileEntityChest(), new TileEntityChest());
            case DISPENSER:
                return new TileEntityDispenser();
            case FURNACE:
                return new TileEntityFurnace();
            case WORKBENCH:
                CraftHumanEntity player = (CraftHumanEntity) owner;
                Location loc = player.getLocation();
                return new InventoryCrafting(new ContainerWorkbench(player.getHandle().inventory, player.getHandle().world, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), 3, 3);
            default:
                return new TileEntityChest();
        }
    }

    static class MinecraftInventory implements IInventory {
        private ItemStack[] items;
        private int stackSize;
        private String title;
        private InventoryType type;
        private InventoryHolder owner;

        public MinecraftInventory(InventoryHolder owner, int size) {
            this(owner, "Chest", size);
        }

        public MinecraftInventory(InventoryHolder owner, String title, int size) {
            this(owner, title, size, 64);
        }

        public MinecraftInventory(InventoryHolder owner, String title, int size, int stackSize) {
            this.items = new ItemStack[size];
            this.stackSize = stackSize;
            this.title = title;
            this.owner = owner;
            this.type = InventoryType.CUSTOM;
        }

        public int getSize() {
            return items.length;
        }

        public ItemStack getItem(int i) {
            return items[i];
        }

        public ItemStack splitStack(int i, int j) {
            if (this.items[i] != null) {
                ItemStack itemstack;

                if (this.items[i].count <= j) {
                    itemstack = this.items[i];
                    this.items[i] = null;
                    this.update();
                    return itemstack;
                } else {
                    itemstack = this.items[i].a(j);
                    if (this.items[i].count == 0) {
                        this.items[i] = null;
                    }

                    this.update();
                    return itemstack;
                }
            } else {
                return null;
            }
        }

        public void setItem(int i, ItemStack itemstack) {
            this.items[i] = itemstack;
            if (itemstack != null && itemstack.count > this.getMaxStackSize()) {
                itemstack.count = this.getMaxStackSize();
            }

            this.update();
        }

        public String getName() {
            return title;
        }

        public InventoryType getType() {
            return type;
        }

        public int getMaxStackSize() {
            return stackSize;
        }

        public void update() {}

        public boolean a_(EntityHuman entityhuman) {
            return true;
        }

        public ItemStack[] getContents() {
            return items;
        }

        public InventoryHolder getHolder() {
            return owner;
        }
    }
}
