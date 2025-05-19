package org.bukkit.event.inventory;

import net.minecraft.server.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

/**
 * @deprecated This event is superseded by the modern Inventory API.
 * @see InventoryOpenEvent
 */
@Deprecated
public class ChestOpenedEvent extends Event implements Cancellable {
    private boolean cancelled;
    private Player player;
    private ItemStack[] contents;

    public ChestOpenedEvent(Player player, ItemStack[] contents) {
        super(Type.CHEST_OPENED);
        this.player = player;
        this.contents = contents;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack[] getContents() {
        return contents;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
