package net.minecraft.server;

// CraftBukkit start
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.block.BlockPlaceEvent;
// CraftBukkit end

public class ItemRedstone extends Item {

    public ItemRedstone(int i) {
        super(i);
    }

    public boolean a(ItemStack itemstack, EntityHuman entityhuman, World world, int i, int j, int k, int l) {
        int clickedX = i, clickedY = j, clickedZ = k; // CraftBukkit

        if (world.getTypeId(i, j, k) != Block.SNOW.id) {
            if (l == 0) {
                --j;
            }

            if (l == 1) {
                ++j;
            }

            if (l == 2) {
                --k;
            }

            if (l == 3) {
                ++k;
            }

            if (l == 4) {
                --i;
            }

            if (l == 5) {
                ++i;
            }

            if (!world.isEmpty(i, j, k)) {
                return false;
            }
        }

        if (Block.REDSTONE_WIRE.canPlace(world, i, j, k)) {
            CraftBlockState blockState = CraftBlockState.getBlockState(world, i, j, k); // CraftBukkit

            // Ignore physic at first when the player try to place the block
            world.suppressPhysics = true;
            try {
                world.setRawTypeId(i, j, k, Block.REDSTONE_WIRE.id); // CraftBukkit - We update after the event
            } finally {
                // Always set the physic backgit remote -v
                world.suppressPhysics = false;
            }

            // CraftBukkit start - redstone
            BlockPlaceEvent event = CraftEventFactory.callBlockPlaceEvent(world, entityhuman, blockState, clickedX, clickedY, clickedZ, Block.REDSTONE_WIRE);

            if (event.isCancelled() || !event.canBuild()) {
                // Ignore physic at first when the player try to place the block
                world.suppressPhysics = true;
                try {
                    event.getBlockPlaced().setTypeIdAndData(blockState.getTypeId(), blockState.getRawData(), false);
                } finally {
                    // Always set the physic back
                    world.suppressPhysics = false;
                }

                return false;
            }

            // world.update() only updates the 6 orthogonal neighbours, so it misses the diagonal redstone dust...
            Block.REDSTONE_WIRE.c(world, i, j, k); // Must take place after BlockPlaceEvent, we need to update all other blocks.
            world.notify(i, j, k); // c() doesn't notify the placed block itself
            // CraftBukkit end

            --itemstack.count; // CraftBukkit - ORDER MATTERS
        }

        return true;
    }comm
}
