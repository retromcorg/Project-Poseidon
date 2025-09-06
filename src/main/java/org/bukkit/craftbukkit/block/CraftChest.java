package org.bukkit.craftbukkit.block;

import net.minecraft.server.TileEntityChest;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest;
import org.bukkit.inventory.Inventory;

public class CraftChest extends CraftBlockState implements Chest {
    private final CraftWorld world;
    private final TileEntityChest chest;

    public CraftChest(final Block block) {
        super(block);

        world = (CraftWorld) block.getWorld();
        chest = (TileEntityChest) world.getTileEntityAt(getX(), getY(), getZ());
    }

    public Inventory getInventory() {
        // Poseidon start - Backport modern Inventory API
        int x = getX();
        int y = getY();
        int z = getZ();

        CraftInventory inventory = new CraftInventory(chest);
        if (world.getBlockTypeIdAt(x - 1, y, z) == Material.CHEST.getId()) {
            CraftInventory left = new CraftInventory((TileEntityChest) world.getHandle().getTileEntity(x - 1, y, z));
            inventory = new CraftInventoryDoubleChest(left, inventory);
        }
        if (world.getBlockTypeIdAt(x + 1, y, z) == Material.CHEST.getId()) {
            CraftInventory right = new CraftInventory((TileEntityChest) world.getHandle().getTileEntity(x + 1, y, z));
            inventory = new CraftInventoryDoubleChest(inventory, right);
        }
        if (world.getBlockTypeIdAt(x, y, z - 1) == Material.CHEST.getId()) {
            CraftInventory left = new CraftInventory((TileEntityChest) world.getHandle().getTileEntity(x, y, z - 1));
            inventory = new CraftInventoryDoubleChest(left, inventory);
        }
        if (world.getBlockTypeIdAt(x, y, z + 1) == Material.CHEST.getId()) {
            CraftInventory right = new CraftInventory((TileEntityChest) world.getHandle().getTileEntity(x, y, z + 1));
            inventory = new CraftInventoryDoubleChest(inventory, right);
        }
        return inventory;
        // Poseidon end
    }

    @Override
    public boolean update(boolean force) {
        boolean result = super.update(force);

        if (result) {
            chest.update();
        }

        return result;
    }
}
