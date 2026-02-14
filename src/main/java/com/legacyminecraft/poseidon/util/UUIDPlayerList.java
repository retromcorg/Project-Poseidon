package com.legacyminecraft.poseidon.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Logger;

/**
 * Dual-indexed player list storing entries by both UUID and lowercase name.
 * Used for UUID-based ops, bans, and whitelist storage.
 */
public class UUIDPlayerList {

    private static final Logger logger = Logger.getLogger("Minecraft");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<PlayerListEntry>>() {}.getType();

    private final Map<UUID, PlayerListEntry> byUUID = new LinkedHashMap<>();
    private final Map<String, PlayerListEntry> byName = new LinkedHashMap<>();

    public static class PlayerListEntry {
        public UUID uuid;
        public String name;

        public PlayerListEntry() {}

        public PlayerListEntry(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }
    }

    public boolean containsUUID(UUID uuid) {
        return byUUID.containsKey(uuid);
    }

    public boolean containsName(String name) {
        return byName.containsKey(name.toLowerCase());
    }

    public void add(UUID uuid, String name) {
        PlayerListEntry entry = new PlayerListEntry(uuid, name);
        // Remove any existing entry with this UUID or name to avoid duplicates
        removeByUUID(uuid);
        removeByName(name);
        byUUID.put(uuid, entry);
        byName.put(name.toLowerCase(), entry);
    }

    public void removeByUUID(UUID uuid) {
        PlayerListEntry entry = byUUID.remove(uuid);
        if (entry != null) {
            byName.remove(entry.name.toLowerCase());
        }
    }

    public void removeByName(String name) {
        PlayerListEntry entry = byName.remove(name.toLowerCase());
        if (entry != null) {
            byUUID.remove(entry.uuid);
        }
    }

    public Set<String> getNames() {
        Set<String> names = new HashSet<>();
        for (PlayerListEntry entry : byUUID.values()) {
            names.add(entry.name.toLowerCase());
        }
        return names;
    }

    public Collection<PlayerListEntry> entries() {
        return Collections.unmodifiableCollection(byUUID.values());
    }

    public void clear() {
        byUUID.clear();
        byName.clear();
    }

    public boolean loadFromJson(File file) {
        if (!file.exists()) {
            return false;
        }
        try (Reader reader = new FileReader(file)) {
            List<PlayerListEntry> entries = gson.fromJson(reader, LIST_TYPE);
            if (entries != null) {
                clear();
                for (PlayerListEntry entry : entries) {
                    if (entry.uuid != null && entry.name != null) {
                        byUUID.put(entry.uuid, entry);
                        byName.put(entry.name.toLowerCase(), entry);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            logger.warning("Failed to load UUID player list from " + file.getName() + ": " + e);
            return false;
        }
    }

    public void saveToJson(File file) {
        try (Writer writer = new FileWriter(file)) {
            List<PlayerListEntry> entries = new ArrayList<>(byUUID.values());
            gson.toJson(entries, LIST_TYPE, writer);
        } catch (Exception e) {
            logger.warning("Failed to save UUID player list to " + file.getName() + ": " + e);
        }
    }

    public int size() {
        return byUUID.size();
    }
}
