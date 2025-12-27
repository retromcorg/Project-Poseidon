package org.bukkit.event.player;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Thrown when a client sends a BungeeCord message
 */
public class PlayerBungeeMessageEvent extends PlayerEvent {
    private byte[] data;

    public PlayerBungeeMessageEvent(final Player player, final byte[] data) {
        super(Type.PLAYER_BUNGEE_MESSAGE, player);
        this.data = data;
    }

    /**
     * Gets the data recieved
     *
     * @return Recieved data
     */
    public byte[] getData() {
        return data;
    }
}
