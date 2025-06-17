package com.legacyminecraft.poseidon.uuid;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.legacyminecraft.poseidon.PoseidonConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerUUIDManager {

    private static final String GET_URL = PoseidonConfig.getInstance().getString("settings.uuid-fetcher.get.value", "https://api.minecraftservices.com/minecraft/profile/lookup/name/{username}");
    private static final String POST_URL = PoseidonConfig.getInstance().getString("settings.uuid-fetcher.post.value", "https://api.minecraftservices.com/minecraft/profile/lookup/bulk/byname");

    private static final File cacheFile = new File("uuidcache.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final JsonObject root = new JsonObject();

    private static final HashMap<UUID, CacheEntry> lookupByUuid = new HashMap<>();
    private static final HashMap<String, CacheEntry> lookupByUsername = new HashMap<>();

    private PlayerUUIDManager() {
    }

    public static void initialize() {
        if (!cacheFile.exists()) {
            System.out.println("[Poseidon] Generating uuidcache.json");
            saveData();
        } else {
            try {
                System.out.println("[Poseidon] Reading uuidcache.json");
                parse();
            } catch (JsonParseException e) {
                System.out.println("[Poseidon] uuidcache.json is corrupt or unreadable, resetting");
                saveData();
            } catch (Exception e) {
                System.out.println("[Poseidon] Error reading uuidcache.json, switching to memory only cache");
                e.printStackTrace();
            }
        }
    }

    private static void parse() throws JsonParseException, IOException {
        JsonElement root = JsonParser.parseReader(new FileReader(cacheFile));

        if (root instanceof JsonObject) { // key-based format
            ((JsonObject) root).entrySet().forEach(entry -> {
                JsonObject object = (JsonObject) entry.getValue();
                UUID uuid = UUID.fromString(entry.getKey());
                String name = object.get("name").getAsString();
                long expiresOn = object.get("expiresOn").getAsLong();
                boolean onlineUUID = object.get("onlineUUID").getAsBoolean();
                upsertPlayer(uuid, name, expiresOn, onlineUUID);
            });
        } else if (root instanceof JsonArray) { // old array-based format
            ((JsonArray) root).forEach(element -> {
                JsonObject object = (JsonObject) element;
                UUID uuid = UUID.fromString(object.get("uuid").getAsString());
                String name = object.get("name").getAsString();
                long expiresOn = object.get("expiresOn").getAsLong();
                boolean onlineUUID = object.get("onlineUUID").getAsBoolean();

                // If multiple entries with this UUID exist, use the one with the latest expiry
                Long existingExpiry = getExpiry(uuid);
                if (existingExpiry != null && expiresOn < existingExpiry)
                    return;

                upsertPlayer(uuid, name, expiresOn, onlineUUID);
            });
        }
    }

    /**
     * Inserts a new player into the cache or updates an existing player.
     *
     * @param uuid the UUID
     * @param name the username
     * @param expiresOn the timestamp when the entry expires
     * @param onlineUUID if the UUID is an online UUID
     */
    public static void upsertPlayer(UUID uuid, String name, long expiresOn, boolean onlineUUID) {
        CacheEntry entry = new CacheEntry(uuid, name, expiresOn, onlineUUID);

        JsonObject object = new JsonObject();
        object.addProperty("name", name);
        object.addProperty("expiresOn", expiresOn);
        object.addProperty("onlineUUID", onlineUUID);
        root.add(uuid.toString(), object);

        lookupByUuid.put(uuid, entry);
        // If another entry with this username exists, use the one with the latest expiry
        CacheEntry existingEntry = lookupByUsername.get(name);
        if (existingEntry == null || expiresOn >= existingEntry.expiresOn)
            lookupByUsername.put(name, entry);
    }

    /**
     * Removes a player from the cache.
     *
     * @param uuid the UUID
     */
    public static void removePlayer(UUID uuid) {
        String username = getCachedUsername(uuid);
        lookupByUuid.remove(uuid);
        lookupByUsername.remove(username);
        root.remove(String.valueOf(uuid));
    }

    /**
     * Removes a player from the cache.
     *
     * @param username the username
     */
    public static void removePlayer(String username) {
        UUID uuid = getCachedUUID(username);
        lookupByUuid.remove(uuid);
        lookupByUsername.remove(username);
        root.remove(String.valueOf(uuid));
    }

    /**
     * Gets the graceful UUID of a player.
     *
     * @param username the username
     * @return an online UUID if it exists, otherwise an offline UUID
     */
    public static UUID getGracefulUUID(String username) {
        UUID uuid = getCachedUUID(username, true);
        return uuid != null ? uuid : generateOfflineUUID(username);
    }

    /**
     * Generates an offline UUID from a player's username.
     *
     * @param username the username
     * @return a name-based UUID generated from the username
     */
    public static UUID generateOfflineUUID(String username) {
        return UUID.nameUUIDFromBytes(username.getBytes());
    }

    /**
     * Gets the UUID associated with a username from the cache.
     *
     * @param username the username
     * @return the UUID associated with this username,
     *         or null if this username is not cached
     */
    public static UUID getCachedUUID(String username) {
        CacheEntry entry = lookupByUsername.get(username);
        return entry != null ? entry.uuid : null;
    }

    /**
     * Gets the UUID associated with a username from the cache.
     *
     * @param username the username
     * @param onlineUUID if the returned UUID should be an online or offline UUID
     * @return the UUID associated with this username,
     *         or null if this username is not cached
     */
    public static UUID getCachedUUID(String username, boolean onlineUUID) {
        CacheEntry entry = lookupByUsername.get(username);
        return entry != null && entry.onlineUUID == onlineUUID ? entry.uuid : null;
    }

    /**
     * Gets the latest username associated with a UUID from the cache.
     *
     * @param uuid the UUID
     * @return the username associated with this UUID,
     *         or null if this UUID is not cached
     */
    public static String getCachedUsername(UUID uuid) {
        CacheEntry entry = lookupByUuid.get(uuid);
        return entry != null ? entry.name : null;
    }

    /**
     * Gets the timestamp when the cache entry of a UUID expires.
     *
     * @param uuid the UUID
     * @return the timestamp of expiry, or null if this UUID is not cached
     */
    public static Long getExpiry(UUID uuid) {
        CacheEntry entry = lookupByUuid.get(uuid);
        return entry != null ? entry.expiresOn : null;
    }

    /**
     * Returns true if a UUID is an online UUID.
     *
     * @param uuid the UUID
     * @return true or false whether the UUID is an online UUID,
     *         or null if this UUID is not cached
     */
    public static Boolean isOnlineUUID(UUID uuid) {
        CacheEntry entry = lookupByUuid.get(uuid);
        return entry != null ? entry.onlineUUID : null;
    }

    /**
     * Returns true if a player is cached.
     *
     * @param uuid the UUID
     * @return true if this player is cached
     */
    public static boolean isPlayerCached(UUID uuid) {
        return lookupByUuid.containsKey(uuid);
    }

    /**
     * Returns true if a player is cached.
     *
     * @param username the username
     * @return true if this player is cached
     */
    public static boolean isPlayerCached(String username) {
        return lookupByUsername.containsKey(username);
    }

    /**
     * Returns all cached UUIDs.
     *
     * @return a list of all cached UUIDs
     */
    public static List<UUID> getCachedUUIDs() {
        return new ArrayList<>(lookupByUuid.keySet());
    }

    /**
     * Returns all cached usernames.
     *
     * @return a list of all cached usernames
     */
    public static List<String> getCachedUsernames() {
        return new ArrayList<>(lookupByUsername.keySet());
    }

    /**
     * Synchronously fetches the UUID for a username using the GET method.
     *
     * @param username the username
     * @param caseSensitive if the username should be checked for casing
     * @return the online UUID associated with this username,
     *         or null if it does not exist or case-sensitivity
     *         is enabled and the username has invalid casing
     * @throws IOException if an I/O error occurs
     */
    public static UUID fetchUUIDGet(String username, boolean caseSensitive) throws IOException {
        try {
            URL url = new URL(GET_URL.replace("{username}", username));
            InputStream stream = url.openStream();
            JsonObject object = (JsonObject) JsonParser.parseReader(new InputStreamReader(stream));
            if (caseSensitive && !object.get("name").getAsString().equals(username))
                return null;

            return toUUID(object.get("id").getAsString());
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * Synchronously fetches the UUID for a username using the POST method.
     *
     * @param username the username
     * @param caseSensitive if the username should be checked for casing
     * @return the online UUID associated with this username,
     *         or null if it does not exist or case-sensitivity
     *         is enabled and the username has invalid casing
     * @throws IOException if an I/O error occurs
     */
    public static UUID fetchUUIDPost(String username, boolean caseSensitive) throws IOException {
        URL url = new URL(POST_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        String body = "[\"" + username + "\"]";
        try (OutputStream stream = connection.getOutputStream()) {
            stream.write(body.getBytes());
            stream.flush();
        }

        JsonArray array = (JsonArray) JsonParser.parseReader(new InputStreamReader(connection.getInputStream()));
        if (array.isEmpty()) return null;
        JsonObject object = (JsonObject) array.get(0);
        if (caseSensitive && !object.get("name").getAsString().equals(username))
            return null;

        return toUUID(object.get("id").getAsString());
    }

    private static UUID toUUID(String id) {
        return UUID.fromString(
                id.substring(0, 8) + "-" +
                        id.substring(8, 12) + "-" +
                        id.substring(12, 16) + "-" +
                        id.substring(16, 20) + "-" +
                        id.substring(20, 32)
        );
    }

    public static void saveData() {
        System.out.println("[Poseidon] Saving uuidcache.json");
        try (FileWriter writer = new FileWriter(cacheFile)) {
            writer.write(gson.toJson(root));
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class CacheEntry {
        final UUID uuid;
        final String name;
        final long expiresOn;
        final boolean onlineUUID;

        CacheEntry(UUID uuid, String name, long expiresOn, boolean onlineUUID) {
            this.uuid = uuid;
            this.name = name;
            this.expiresOn = expiresOn;
            this.onlineUUID = onlineUUID;
        }
    }

}
