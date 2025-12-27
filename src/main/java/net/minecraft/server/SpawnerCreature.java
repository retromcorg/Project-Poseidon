package net.minecraft.server;

import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

// CraftBukkit

public final class SpawnerCreature {

    /**
     * Chunks around players that are eligible for natural spawns this tick.
     */
    private static final Set<ChunkCoordIntPair> candidateChunks = new HashSet<>();

    /**
     * Mob classes used when attempting to spawn a monster near a sleeping player.
     */
    private static final Class[] SLEEP_SPAWNER_MOBS = new Class[]{EntitySpider.class, EntityZombie.class, EntitySkeleton.class};

    public SpawnerCreature() {
    }

    public static int spawnEntities(World world, boolean allowHostiles, boolean allowPeaceful) {
        if (!allowHostiles && !allowPeaceful) {
            return 0;
        }

        candidateChunks.clear();

        // Build candidate chunk set around each player (square of radius 8 chunks)
        for (int p = 0; p < world.players.size(); ++p) {
            EntityHuman player = (EntityHuman) world.players.get(p);
            int playerChunkX = MathHelper.floor(player.locX / 16.0D);
            int playerChunkZ = MathHelper.floor(player.locZ / 16.0D);
            final int radius = 8;

            for (int dx = -radius; dx <= radius; ++dx) {
                for (int dz = -radius; dz <= radius; ++dz) {
                    candidateChunks.add(new ChunkCoordIntPair(playerChunkX + dx, playerChunkZ + dz));
                }
            }
        }

        int spawnedTotal = 0;
        ChunkCoordinates worldSpawn = world.getSpawn();
        EnumCreatureType[] creatureTypes = EnumCreatureType.values();

        for (EnumCreatureType type : creatureTypes) {

            // Skip if the type doesn't match peaceful/hostile toggles
            if ((type.isPeaceful() && !allowPeaceful) || (!type.isPeaceful() && !allowHostiles)) {
                continue;
            }

            // Skip if the global density cap is exceeded
            if (world.a(type.getBaseClass()) > type.getMaxCount() * candidateChunks.size() / 256) {
                continue;
            }

            Iterator<ChunkCoordIntPair> it = candidateChunks.iterator();

            // Walk all candidate chunks for this creature type
            chunksLoop:
            while (it.hasNext()) {
                ChunkCoordIntPair chunkPos = it.next();
                BiomeBase biome = world.getWorldChunkManager().a(chunkPos);
                List<BiomeMeta> spawnEntries = biome.a(type); // weighted list of mobs for the biome+type

                if (spawnEntries == null || spawnEntries.isEmpty()) {
                    continue;
                }

                // Pick BiomeMeta via weight
                int totalWeight = 0;
                for (BiomeMeta entry : spawnEntries) totalWeight += entry.b;

                int rnd = world.random.nextInt(totalWeight);
                BiomeMeta chosen = spawnEntries.get(0);
                for (BiomeMeta entry : spawnEntries) {
                    rnd -= entry.b;
                    if (rnd < 0) {
                        chosen = entry;
                        break;
                    }
                }

                // Pick a random position inside the chunk area
                ChunkPosition pos = pickRandomBlockInChunkArea(world, chunkPos.x * 16, chunkPos.z * 16);
                int baseX = pos.x;
                int baseY = pos.y;
                int baseZ = pos.z;

                if (!world.e(baseX, baseY, baseZ) && world.getMaterial(baseX, baseY, baseZ) == type.getSpawnMaterial()) {
                    int groupSpawned = 0;

                    // Up to 3 group attempts
                    for (int groupAttempt = 0; groupAttempt < 3; ++groupAttempt) {
                        int x = baseX;
                        int y = baseY;
                        int z = baseZ;
                        final byte spread = 6;

                        // Try up to 4 mobs per group, with small random offsets
                        for (int tries = 0; tries < 4; ++tries) {
                            x += world.random.nextInt(spread) - world.random.nextInt(spread);
                            y += world.random.nextInt(1) - world.random.nextInt(1);
                            z += world.random.nextInt(spread) - world.random.nextInt(spread);

                            if (canSpawnHere(type, world, x, y, z)) {
                                float fx = x + 0.5F;
                                float fy = y;
                                float fz = z + 0.5F;

                                // Keep spawns away from other players (24 block radius)
                                if (world.a(fx, fy, fz, 24.0D) == null) {
                                    float dx = fx - worldSpawn.x;
                                    float dy = fy - worldSpawn.y;
                                    float dz = fz - worldSpawn.z;
                                    float dist2 = dx * dx + dy * dy + dz * dz;

                                    // Don’t spawn too close to world spawn (>= 24 blocks -> 24^2 = 576)
                                    if (dist2 >= 576.0F) {
                                        EntityLiving mob;

                                        try {
                                            mob = (EntityLiving) chosen.a.getConstructor(World.class).newInstance(world);
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                            return spawnedTotal;
                                        }

                                        mob.setPositionRotation(fx, fy, fz, world.random.nextFloat() * 360.0F, 0.0F);

                                        // d() = canSpawn() check
                                        if (mob.d()) {
                                            ++groupSpawned;
                                            world.addEntity(mob, SpawnReason.NATURAL);
                                            applyPostSpawnExtras(mob, world, fx, fy, fz);

                                            // l() = getMaxGroup() clamp for this entity
                                            if (groupSpawned >= mob.l()) {
                                                // Move to next chunk if we filled a group here
                                                spawnedTotal += groupSpawned;
                                                continue chunksLoop;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    spawnedTotal += groupSpawned;
                }
            }

        }

        return spawnedTotal;
    }


    protected static ChunkPosition pickRandomBlockInChunkArea(World world, int i, int j) {
        int k = i + world.random.nextInt(16);
        int l = world.random.nextInt(128);
        int i1 = j + world.random.nextInt(16);

        return new ChunkPosition(k, l, i1);
    }

    /**
     * Block/material + headroom checks to determine if an entity type can spawn at (x,y,z).
     */
    private static boolean canSpawnHere(EnumCreatureType type, World world, int x, int y, int z) {
        if (type.getSpawnMaterial() == Material.WATER) {
            // Water creatures: must be in liquid, with air above
            return world.getMaterial(x, y, z).isLiquid() && !world.e(x, y + 1, z);
        } else {
            // Land creatures: solid block below, current block + head clear, and not liquid
            return world.e(x, y - 1, z)
                    && !world.e(x, y, z)
                    && !world.getMaterial(x, y, z).isLiquid()
                    && !world.e(x, y + 1, z);
        }
    }

    private static void applyPostSpawnExtras(EntityLiving entityliving, World world, float f, float f1, float f2) {
        if (entityliving instanceof EntitySpider && world.random.nextInt(100) == 0) {
            EntitySkeleton entityskeleton = new EntitySkeleton(world);

            entityskeleton.setPositionRotation((double) f, (double) f1, (double) f2, entityliving.yaw, 0.0F);
            // CraftBukkit - added a reason for spawning this creature
            world.addEntity(entityskeleton, SpawnReason.NATURAL);
            entityskeleton.mount(entityliving);
        } else if (entityliving instanceof EntitySheep) {
            ((EntitySheep) entityliving).setColor(EntitySheep.a(world.random));
        }
    }

    public static boolean spawnSleepThreats(World world, List<EntityHuman> listPlayers) {
        boolean anySpawned = false;
        Pathfinder pathfinder = new Pathfinder(world);

        for (Object o : listPlayers) {
            EntityHuman player = (EntityHuman) o;
            Class<?>[] candidates = SLEEP_SPAWNER_MOBS;

            if (candidates == null || candidates.length == 0) continue;

            boolean spawnedNearThisPlayer = false;

            // Up to 20 attempts per player
            for (int attempt = 0; attempt < 20 && !spawnedNearThisPlayer; ++attempt) {
                int x = MathHelper.floor(player.locX) + world.random.nextInt(32) - world.random.nextInt(32);
                int z = MathHelper.floor(player.locZ) + world.random.nextInt(32) - world.random.nextInt(32);
                int y = MathHelper.floor(player.locY) + world.random.nextInt(16) - world.random.nextInt(16);

                if (y < 1) y = 1;
                else if (y > 128) y = 128;

                // Find solid ground
                int groundY;
                for (groundY = y; groundY > 2 && !world.e(x, groundY - 1, z); --groundY) { /* descend */ }

                // Nudge upward until a valid spawn block is found (or give up)
                while (!canSpawnHere(EnumCreatureType.MONSTER, world, x, groundY, z) && groundY < y + 16 && groundY < 128) {
                    ++groundY;
                }

                if (groundY >= y + 16 || groundY >= 128) {
                    continue;
                }

                float fx = x + 0.5F;
                float fy = groundY;
                float fz = z + 0.5F;

                // Pick a random monster type from SLEEP_MONSTERS
                int pick = world.random.nextInt(candidates.length);
                EntityLiving mob;
                try {
                    mob = (EntityLiving) candidates[pick].getConstructor(World.class).newInstance(world);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return anySpawned;
                }

                mob.setPositionRotation(fx, fy, fz, world.random.nextFloat() * 360.0F, 0.0F);

                if (mob.d()) {
                    // Must be able to path to the player
                    PathEntity path = pathfinder.a(mob, player, 32.0F);
                    if (path != null && path.a > 1) {
                        PathPoint firstStep = path.c();
                        if (Math.abs(firstStep.a - player.locX) < 1.5D
                                && Math.abs(firstStep.c - player.locZ) < 1.5D
                                && Math.abs(firstStep.b - player.locY) < 1.5D) {

                            // Try bed-safe placement; fall back to current choice
                            ChunkCoordinates bed = BlockBed.f(world,
                                    MathHelper.floor(player.locX),
                                    MathHelper.floor(player.locY),
                                    MathHelper.floor(player.locZ), 1);

                            if (bed == null) bed = new ChunkCoordinates(x, groundY + 1, z);

                            mob.setPositionRotation(bed.x + 0.5F, bed.y, bed.z + 0.5F, 0.0F, 0.0F);
                            world.addEntity(mob, SpawnReason.BED);
                            applyPostSpawnExtras(mob, world, bed.x + 0.5F, bed.y, bed.z + 0.5F);

                            // Wake the player (vanilla flags)
                            player.a(true, false, false);
                            mob.Q(); // finalize spawn behaviors

                            anySpawned = true;
                            spawnedNearThisPlayer = true;
                        }
                    }
                }
            }
        }

        return anySpawned;
    }

    /* ------------------------------------------------------------
     * Compatibility
     * ------------------------------------------------------------ */

    // Old: protected static ChunkPosition a(World, int, int)
    protected static ChunkPosition a(World world, int i, int j) {
        return pickRandomBlockInChunkArea(world, i, j);
    }

    // Old: private static boolean a(EnumCreatureType, World, int, int, int)
    private static boolean a(EnumCreatureType type, World world, int x, int y, int z) {
        return canSpawnHere(type, world, x, y, z);
    }

    // Old: private static void a(EntityLiving, World, float, float, float)
    private static void a(EntityLiving entity, World world, float x, float y, float z) {
        applyPostSpawnExtras(entity, world, x, y, z);
    }

    // Old: public static boolean a(World, List)
    public static boolean a(World world, List list) {
        return spawnSleepThreats(world, list);

    }

}