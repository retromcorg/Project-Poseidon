package net.minecraft.server;

import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.entity.HumanEntity;

public class ContainerChest extends Container {

    private IInventory a;
    private int b;
    // Poseidon start - Backport modern Inventory API
    private CraftInventoryView view = null;
    private InventoryPlayer player;
    // Poseidon end

    public ContainerChest(IInventory iinventory, IInventory iinventory1) {
        this.a = iinventory1;
        this.b = iinventory1.getSize() / 9;
        int i = (this.b - 4) * 18;
        this.player = (InventoryPlayer) iinventory; // Poseidon - Backport modern Inventory API

        int j;
        int k;

        for (j = 0; j < this.b; ++j) {
            for (k = 0; k < 9; ++k) {
                this.a(new Slot(iinventory1, k + j * 9, 8 + k * 18, 18 + j * 18));
            }
        }

        for (j = 0; j < 3; ++j) {
            for (k = 0; k < 9; ++k) {
                this.a(new Slot(iinventory, k + j * 9 + 9, 8 + k * 18, 103 + j * 18 + i));
            }
        }

        for (j = 0; j < 9; ++j) {
            this.a(new Slot(iinventory, j, 8 + j * 18, 161 + i));
        }
    }

    public boolean b(EntityHuman entityhuman) {
        if (!this.checkReachable) return true; // Poseidon - Backport modern Inventory API
        return this.a.a_(entityhuman);
    }

    public ItemStack a(int i) {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.e.get(i);

        if (slot != null && slot.b()) {
            ItemStack itemstack1 = slot.getItem();

            itemstack = itemstack1.cloneItemStack();
            if (i < this.b * 9) {
                this.a(itemstack1, this.b * 9, this.e.size(), true);
            } else {
                this.a(itemstack1, 0, this.b * 9, false);
            }

            if (itemstack1.count == 0) {
                slot.c((ItemStack) null);
            } else {
                slot.c();
            }
        }

        return itemstack;
    }

    // Poseidon start - Backport modern Inventory API
    @Override
    public CraftInventoryView getBukkitView() {
        if (view == null) {
            CraftInventory inventory;
            if (this.a instanceof InventoryLargeChest) {
                inventory = new CraftInventoryDoubleChest((InventoryLargeChest) this.a);
            } else {
                inventory = new CraftInventory(this.a);
            }
            view = new CraftInventoryView((HumanEntity) this.player.d.getBukkitEntity(), inventory, this);
        }
        return view;
    }
    // Poseidon end
}
