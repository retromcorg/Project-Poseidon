package net.minecraft.server;

import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.entity.HumanEntity;

public class ContainerDispenser extends Container {

    public TileEntityDispenser a; // Poseidon - Backport modern Inventory API - private -> public
    // Poseidon start - Backport modern Inventory API
    private CraftInventoryView view = null;
    private InventoryPlayer player;
    // Poseidon end

    public ContainerDispenser(IInventory iinventory, TileEntityDispenser tileentitydispenser) {
        this.a = tileentitydispenser;
        this.player = (InventoryPlayer) iinventory; // Poseidon - Backport modern Inventory API

        int i;
        int j;

        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 3; ++j) {
                this.a(new Slot(tileentitydispenser, j + i * 3, 62 + j * 18, 17 + i * 18));
            }
        }

        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 9; ++j) {
                this.a(new Slot(iinventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (i = 0; i < 9; ++i) {
            this.a(new Slot(iinventory, i, 8 + i * 18, 142));
        }
    }

    public boolean b(EntityHuman entityhuman) {
        if (!this.checkReachable) return true; // Poseidon - Backport modern Inventory API
        return this.a.a_(entityhuman);
    }

    // Poseidon start - Backport modern Inventory API
    @Override
    public CraftInventoryView getBukkitView() {
        if (view != null) return view;
        CraftInventory inventory = new CraftInventory(a);
        view = new CraftInventoryView((HumanEntity) this.player.d.getBukkitEntity(), inventory, this);
        return view;
    }
    // Poseidon end
}
