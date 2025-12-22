package com.projectposeidon.johnymuffin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.legacyminecraft.poseidon.PoseidonConfig;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UUIDManager {
    private static UUIDManager singleton;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File cacheFile = new File("uuidcache.json");

    private final Map<String, List<UUIDEntry>> usernameCache = new ConcurrentHashMap<>();
    private final Map<UUID, List<UUIDEntry>> uuidCache = new ConcurrentHashMap<>();

    private List<UUIDEntry> uuidCacheList;

    private UUIDManager() {
        if (!cacheFile.exists()) {
            try (FileWriter writer = new FileWriter(cacheFile)) {
                System.out.println("[Poseidon] Generating uuidcache.json for Project Poseidon");
                uuidCacheList = new ArrayList<>();
                gson.toJson(uuidCacheList, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (FileReader reader = new FileReader(cacheFile)) {
            System.out.println("[Poseidon] Reading uuidcache.json for Project Poseidon");
            Type listType = new TypeToken<List<UUIDEntry>>() {}.getType();
            uuidCacheList = gson.fromJson(reader, listType);
            if (uuidCacheList == null) uuidCacheList = new ArrayList<>();
        } catch (Exception e) {
            System.out.println("[Poseidon] UUID cache corrupt or unreadable, resetting: " + e);
            uuidCacheList = new ArrayList<>();
            saveJsonArray();
        }

        rebuildCaches();
    }

    public static UUIDManager getInstance() {
        if (singleton == null) {
            singleton = new UUIDManager();
        }
        return singleton;
    }


    private void rebuildCaches() {
        usernameCache.clear();
        uuidCache.clear();

        for (UUIDEntry entry : uuidCacheList) {
            usernameCache.computeIfAbsent(entry.name.toLowerCase(), k -> new ArrayList<>()).add(entry);
            uuidCache.computeIfAbsent(entry.uuid, k -> new ArrayList<>()).add(entry);
        }
    }

    private void addToCaches(UUIDEntry entry) {
        usernameCache.computeIfAbsent(entry.name.toLowerCase(), k -> new ArrayList<>()).add(entry);
        uuidCache.computeIfAbsent(entry.uuid, k -> new ArrayList<>()).add(entry);
    }

    private void removeFromCaches(UUIDEntry entry) {
        List<UUIDEntry> userEntries = usernameCache.get(entry.name.toLowerCase());
        if (userEntries != null) userEntries.remove(entry);
        List<UUIDEntry> uuidEntries = uuidCache.get(entry.uuid);
        if (uuidEntries != null) uuidEntries.remove(entry);
    }

    public static UUID generateOfflineUUID(String username) {
        // TODO: Update to modern system: UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
        return UUID.nameUUIDFromBytes(username.getBytes());
    }

    public UUID getUUIDGraceful(String username) {
        UUID uuid = getUUIDFromUsername(username, true);
        return uuid != null ? uuid : generateOfflineUUID(username);
    }

    public void saveJsonArray() {
        try (FileWriter writer = new FileWriter(cacheFile)) {
            gson.toJson(uuidCacheList, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receivedUUID(String username, UUID uuid, Long expiry, boolean online) {
        // Check existing entry
        List<UUIDEntry> entries = usernameCache.getOrDefault(username.toLowerCase(), Collections.emptyList());
        for (UUIDEntry entry : entries) {
            if (entry.uuid.equals(uuid) && entry.onlineUUID == online) {
                entry.expiresOn = expiry;
                return;
            }
        }

        removeInstancesOfUsername(username);
        removeInstancesOfUUID(uuid);

        UUIDEntry newEntry = new UUIDEntry(username, uuid, expiry, online);
        uuidCacheList.add(newEntry);
        addToCaches(newEntry);
    }


    public UUID getUUIDFromUsername(String username) {
        List<UUIDEntry> entries = usernameCache.get(username.toLowerCase());
        if (entries != null && !entries.isEmpty()) {
            return entries.get(0).uuid;
        }
        return null;
    }

    public UUID getUUIDFromUsername(String username, boolean online) {
        List<UUIDEntry> entries = usernameCache.get(username.toLowerCase());
        if (entries != null) {
            for (UUIDEntry entry : entries) {
                if (entry.onlineUUID == online) {
                    return entry.uuid;
                }
            }
        }
        return null;
    }

    public UUID getUUIDFromUsername(String username, boolean online, long afterUnix) {
        List<UUIDEntry> entries = usernameCache.get(username.toLowerCase());
        if (entries != null) {
            for (UUIDEntry entry : entries) {
                if (entry.onlineUUID == online && entry.expiresOn > afterUnix) {
                    return entry.uuid;
                }
            }
        }
        return null;
    }

    public String getUsernameFromUUID(UUID uuid) {
        List<UUIDEntry> entries = uuidCache.get(uuid);
        if (entries == null || entries.isEmpty()) return null;

        UUIDEntry newest = entries.get(0);
        for (UUIDEntry entry : entries) {
            if (entry.expiresOn > newest.expiresOn) {
                newest = entry;
            }
        }
        return newest.name;
    }

    private void removeInstancesOfUsername(String username) {
        List<UUIDEntry> entries = usernameCache.get(username.toLowerCase());
        if (entries != null) {
            for (UUIDEntry entry : new ArrayList<>(entries)) {
                uuidCacheList.remove(entry);
                removeFromCaches(entry);
            }
        }
    }

    private void removeInstancesOfUUID(UUID uuid) {
        List<UUIDEntry> entries = uuidCache.get(uuid);
        if (entries == null) return;

        for (UUIDEntry entry : new ArrayList<>(entries)) {
            if ((boolean) PoseidonConfig.getInstance().getConfigOption("settings.delete-duplicate-uuids")) {
                uuidCacheList.remove(entry);
                removeFromCaches(entry);
            } else {
                entry.expiresOn = 1L; // mark as outdated
            }
        }
    }

    protected class UUIDEntry {
        public String name;
        public UUID uuid;
        public long expiresOn;
        public boolean onlineUUID;

        public UUIDEntry(String name, UUID uuid, long expiresOn, boolean onlineUUID) {
            this.name = name;
            this.uuid = uuid;
            this.expiresOn = expiresOn;
            this.onlineUUID = onlineUUID;
        }
    }
}
