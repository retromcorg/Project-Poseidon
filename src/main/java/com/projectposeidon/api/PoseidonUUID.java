package com.projectposeidon.api;

import com.legacyminecraft.poseidon.uuid.PlayerUUIDManager;

import java.util.UUID;

public final class PoseidonUUID {

    private PoseidonUUID() {

    }

    /**
     * @param username Username of a player who has connected
     * @return A Mojang UUID if known, otherwise null
     */
    public static UUID getPlayerMojangUUID(String username) {
        return PlayerUUIDManager.getCachedUUID(username, true);
    }

    /**
     * @param username Username of a player who has connected
     * @return A Mojang UUID if known, otherwise a offline uuid
     */
    public static UUID getPlayerGracefulUUID(String username) {
        return PlayerUUIDManager.getGracefulUUID(username);
    }

    /**
     * Get a UUID of a player IF they have joined before.
     *
     * @param username   Username of a player who has connected
     * @param onlineUUID Search for online or offline UUIDs?
     * @return Returns a UUID if known in cache, otherwise null
     */
    public static UUID getPlayerUUIDFromCache(String username, boolean onlineUUID) {
        return PlayerUUIDManager.getCachedUUID(username, onlineUUID);
    }

    /**
     * @param username Username of a player
     * @return A offline UUID for a player
     */
    public static UUID getPlayerOfflineUUID(String username) {
        return PlayerUUIDManager.generateOfflineUUID(username);
    }

    //TODO: Maybe return an enum for if the UUID is an offline

    /**
     * @param username Username of a player
     * @return A boolean of if the player's UUID is known in the cache. Will return true if they have a graceful UUID.
     */
    public static UUIDType getPlayerUUIDCacheStatus(String username) {
        if (getPlayerUUIDFromCache(username, true) != null) {
            return UUIDType.ONLINE;
        }
        if (getPlayerUUIDFromCache(username, false) != null) {
            return UUIDType.OFFLINE;
        }
        return UUIDType.UNKNOWN;
    }

    /**
     * @param uuid UUID for a player
     * @return A corresponding UUID if known, otherwise null
     */
    public static String getPlayerUsernameFromUUID(UUID uuid) {
        return PlayerUUIDManager.getCachedUsername(uuid);
    }


}
