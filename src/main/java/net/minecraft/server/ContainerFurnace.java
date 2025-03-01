package net.minecraft.server;

import org.bukkit.craftbukkit.inventory.CraftInventoryFurnace;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.entity.HumanEntity;

public class ContainerFurnace extends Container {

    private TileEntityFurnace a;
    private int b = 0;
    private int c = 0;
    private int h = 0;
    // Poseidon start
    private CraftInventoryView view = null;
    private InventoryPlayer player;
    // Poseidon end

    public ContainerFurnace(InventoryPlayer inventoryplayer, TileEntityFurnace tileentityfurnace) {
        this.a = tileentityfurnace;
        this.a(new Slot(tileentityfurnace, 0, 56, 17));
        this.a(new Slot(tileentityfurnace, 1, 56, 53));
        this.a(new SlotResult2(inventoryplayer.d, tileentityfurnace, 2, 116, 35));
        this.player = inventoryplayer;

        int i;

        for (i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.a(new Slot(inventoryplayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (i = 0; i < 9; ++i) {
            this.a(new Slot(inventoryplayer, i, 8 + i * 18, 142));
        }
    }

    public void a(ICrafting icrafting) {
        super.a(icrafting);
        icrafting.a(this, 0, this.a.cookTime);
        icrafting.a(this, 1, this.a.burnTime);
        icrafting.a(this, 2, this.a.ticksForCurrentFuel);
    }

    public void a() {
        super.a();

        for (int i = 0; i < this.listeners.size(); ++i) {
            ICrafting icrafting = (ICrafting) this.listeners.get(i);

            if (this.b != this.a.cookTime) {
                icrafting.a(this, 0, this.a.cookTime);
            }

            if (this.c != this.a.burnTime) {
                icrafting.a(this, 1, this.a.burnTime);
            }

            if (this.h != this.a.ticksForCurrentFuel) {
                icrafting.a(this, 2, this.a.ticksForCurrentFuel);
            }
        }

        this.b = this.a.cookTime;
        this.c = this.a.burnTime;
        this.h = this.a.ticksForCurrentFuel;
    }

    public boolean b(EntityHuman entityhuman) {
        if (!this.checkReachable) return true; // Poseidon
        return this.a.a_(entityhuman);
    }

    public ItemStack a(int i) {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.e.get(i);

        if (slot != null && slot.b()) {
            ItemStack itemstack1 = slot.getItem();

            itemstack = itemstack1.cloneItemStack();
            if (i == 2) {
                this.a(itemstack1, 3, 39, true);
            } else if (i >= 3 && i < 30) {
                this.a(itemstack1, 30, 39, false);
            } else if (i >= 30 && i < 39) {
                this.a(itemstack1, 3, 30, false);
            } else {
                this.a(itemstack1, 3, 39, false);
            }

            if (itemstack1.count == 0) {
                slot.c((ItemStack) null);
            } else {
                slot.c();
            }

            if (itemstack1.count == itemstack.count) {
                return null;
            }

            slot.a(itemstack1);
        }

        return itemstack;
    }

    // Poseidon
    @Override
    public CraftInventoryView getBukkitView() {
        if (view != null) return view;
        CraftInventoryFurnace inventory = new CraftInventoryFurnace(a);
        view = new CraftInventoryView((HumanEntity) this.player.d.getBukkitEntity(), inventory, this);
        return view;
    }
}
