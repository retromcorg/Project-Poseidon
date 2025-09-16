package com.projectposeidon.api;

import com.projectposeidon.johnymuffin.UUIDManager;

import java.util.UUID;

public final class PoseidonUUID {

    private PoseidonUUID() {

    }

    /**
     * @param username Username of a player who has connected
     * @return A Mojang UUID if known, otherwise null
     */
    public static UUID getPlayerMojangUUID(String username) {
        return UUIDManager.getInstance().getUUIDFromUsername(username, true);
    }

    /**
     * @param username Username of a player who has connected
     * @return A Mojang UUID if known, otherwise a offline uuid
     */
    public static UUID getPlayerGracefulUUID(String username) {
        return UUIDManager.getInstance().getUUIDGraceful(username);
    }

    /**
     * Get a UUID of a player IF they have joined before.
     *
     * @param username   Username of a player who has connected
     * @param onlineUUID Search for online or offline UUIDs?
     * @return Returns a UUID if known in cache, otherwise null
     */
    public static UUID getPlayerUUIDFromCache(String username, boolean onlineUUID) {
        return UUIDManager.getInstance().getUUIDFromUsername(username, onlineUUID);
    }

    /**
     * @param username Username of a player
     * @return A offline UUID for a player
     */
    public static UUID getPlayerOfflineUUID(String username) {
        return UUIDManager.generateOfflineUUID(username);
    }

    /**
     * @param username Username of a player
     * @return A UUIDType enum.
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
     * @return A corresponding username if known, otherwise null
     */
    public static String getPlayerUsernameFromUUID(UUID uuid) {
        return UUIDManager.getInstance().getUsernameFromUUID(uuid);
    }


}
