package com.legacyminecraft.poseidon.level;

import java.util.HashMap;
import java.util.Map;

public enum ChunkCompressionType {
    NONE("none", 0),
    GZIP("gzip", 1),
    DEFLATE("deflate", 2),
    LZ4("lz4", 3),
    XZ("xz", 4);

    private static final Map<String, ChunkCompressionType> BY_NAME = new HashMap<>();
    private static final Map<Integer, ChunkCompressionType> BY_ID = new HashMap<>();

    static {
        for(ChunkCompressionType type : values()) {
            BY_NAME.put(type.name, type);
            BY_ID.put(type.id, type);
        }
    }

    private final String name;
    private final int id;

    ChunkCompressionType(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public static ChunkCompressionType fromName(String name) {
        return BY_NAME.get(name);
    }

    public static ChunkCompressionType fromId(int id) {
        return BY_ID.get(id);
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }
}
