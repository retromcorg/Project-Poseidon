package net.minecraft.server;

public enum EnumCreatureType {

    MONSTER(IMonster.class, 70, Material.AIR, false),
    CREATURE(EntityAnimal.class, 15, Material.AIR, true),
    WATER_CREATURE(EntityWaterAnimal.class, 5, Material.WATER, true);
    private final Class baseClass;
    private final int maxCount;
    private final Material spawnMaterial;
    private final boolean isPeaceful;

    private static final EnumCreatureType[] h = new EnumCreatureType[]{MONSTER, CREATURE, WATER_CREATURE};

    EnumCreatureType(Class baseClass, int maxCount, Material spawnMaterial, boolean isPeaceful) {
        this.baseClass = baseClass;
        this.maxCount = maxCount;
        this.spawnMaterial = spawnMaterial;
        this.isPeaceful = isPeaceful;
    }

    public Class getBaseClass() {
        return this.baseClass;
    }

    public int getMaxCount() {
        return this.maxCount;
    }

    public Material getSpawnMaterial() {
        return this.spawnMaterial;
    }

    public boolean isPeaceful() {
        return this.isPeaceful;
    }
}
