package com.projectposeidon.johnymuffin;

import com.legacyminecraft.poseidon.uuid.PlayerUUIDManager;

import java.util.UUID;

/**
 * @deprecated see {@link PlayerUUIDManager}
 */
@Deprecated
public class UUIDManager {
    private static UUIDManager singleton;

    private UUIDManager() {
    }

    /**
     * @deprecated replace with {@link PlayerUUIDManager#getGracefulUUID(String)}
     */
    @Deprecated
    public UUID getUUIDGraceful(String username) {
        return PlayerUUIDManager.getGracefulUUID(username);
    }

    /**
     * @deprecated replace with {@link PlayerUUIDManager#generateOfflineUUID(String)}
     */
    @Deprecated
    public static UUID generateOfflineUUID(String username) {
        return PlayerUUIDManager.generateOfflineUUID(username);
    }

    /**
     * @deprecated replace with {@link PlayerUUIDManager#upsertPlayer(UUID, String, long, boolean)}
     */
    @Deprecated
    public void receivedUUID(String username, UUID uuid, Long expiry, boolean online) {
        PlayerUUIDManager.upsertPlayer(uuid, username, expiry, online);
    }

    /**
     * @deprecated replace with {@link PlayerUUIDManager#getCachedUUID(String)}
     */
    @Deprecated
    public UUID getUUIDFromUsername(String username) {
        return PlayerUUIDManager.getCachedUUID(username);
    }

    /**
     * @deprecated replace with {@link PlayerUUIDManager#getCachedUUID(String, boolean)}
     */
    @Deprecated
    public UUID getUUIDFromUsername(String username, boolean online) {
        return PlayerUUIDManager.getCachedUUID(username, online);
    }

    /**
     * @deprecated replace with {@link PlayerUUIDManager#getCachedUUID(String, boolean)}
     */
    @Deprecated
    public UUID getUUIDFromUsername(String username, boolean online, Long afterUnix) {
        UUID uuid = PlayerUUIDManager.getCachedUUID(username, online);
        Long expiry = PlayerUUIDManager.getExpiry(uuid);
        if (expiry == null || afterUnix >= expiry)
            return null;

        return uuid;
    }

    /**
     * @deprecated replace with {@link PlayerUUIDManager#getCachedUsername(UUID)}
     */
    @Deprecated
    public String getUsernameFromUUID(UUID uuid) {
        return PlayerUUIDManager.getCachedUsername(uuid);
    }

    public static UUIDManager getInstance() {
        if (UUIDManager.singleton == null) {
            UUIDManager.singleton = new UUIDManager();
        }
        return UUIDManager.singleton;
    }

}
