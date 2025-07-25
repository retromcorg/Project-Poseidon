package org.bukkit;

import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a world, which may contain entities, chunks and blocks
 */
public interface World {

    /**
     * Gets the {@link Block} at the given coordinates
     *
     * @param x X-coordinate of the block
     * @param y Y-coordinate of the block
     * @param z Z-coordinate of the block
     * @return Block at the given coordinates
     * @see #getBlockTypeIdAt(int, int, int) Returns the current type ID of the block
     */
    public Block getBlockAt(int x, int y, int z);

    /**
     * Gets the {@link Block} at the given {@link Location}
     *
     * @param location Location of the block
     * @return Block at the given location
     * @see #getBlockTypeIdAt(org.bukkit.Location) Returns the current type ID of the block
     */
    public Block getBlockAt(Location location);

    /**
     * Gets the block type ID at the given coordinates
     *
     * @param x X-coordinate of the block
     * @param y Y-coordinate of the block
     * @param z Z-coordinate of the block
     * @return Type ID of the block at the given coordinates
     * @see #getBlockAt(int, int, int) Returns a live Block object at the given location
     */
    public int getBlockTypeIdAt(int x, int y, int z);

    /**
     * Gets the block type ID at the given {@link Location}
     *
     * @param location Location of the block
     * @return Type ID of the block at the given location
     * @see #getBlockAt(org.bukkit.Location) Returns a live Block object at the given location
     */
    public int getBlockTypeIdAt(Location location);

    /**
     * Gets the highest non-air coordinate at the given coordinates
     *
     * @param x X-coordinate of the blocks
     * @param z Z-coordinate of the blocks
     * @return Y-coordinate of the highest non-air block
     */
    public int getHighestBlockYAt(int x, int z);

    /**
     * Gets the highest non-air coordinate at the given {@link Location}
     *
     * @param location Location of the blocks
     * @return Y-coordinate of the highest non-air block
     */
    public int getHighestBlockYAt(Location location);

    /**
     * Gets the highest non-empty block at the given coordinates
     *
     * @param x X-coordinate of the block
     * @param z Z-coordinate of the block
     *
     * @return Highest non-empty block
     */
    public Block getHighestBlockAt(int x, int z);

    /**
     * Gets the highest non-empty block at the given coordinates
     *
     * @param location Coordinates to get the highest block
     *
     * @return Highest non-empty block
     */
    public Block getHighestBlockAt(Location location);

    /**
     * Gets the {@link Chunk} at the given coordinates
     *
     * @param x X-coordinate of the chunk
     * @param z Z-coordinate of the chunk
     * @return Chunk at the given coordinates
     */
    public Chunk getChunkAt(int x, int z);

    /**
     * Gets the {@link Chunk} at the given {@link Location}
     *
     * @param location Location of the chunk
     * @return Chunk at the given location
     */
    public Chunk getChunkAt(Location location);

    /**
     * Gets the {@link Chunk} that contains the given {@link Block}
     *
     * @param block Block to get the containing chunk from
     * @return The chunk that contains the given block
     */
    public Chunk getChunkAt(Block block);

    /**
     * Checks if the specified {@link Chunk} is loaded
     *
     * @param chunk The chunk to check
     * @return true if the chunk is loaded, otherwise false
     */
    public boolean isChunkLoaded(Chunk chunk);

    /**
     * Gets an array of all loaded {@link Chunk}s
     *
     * @return Chunk[] containing all loaded chunks
     */
    public Chunk[] getLoadedChunks();

    /**
     * Loads the specified {@link Chunk}
     *
     * @param chunk The chunk to load
     */
    public void loadChunk(Chunk chunk);

    /**
     * Checks if the {@link Chunk} at the specified coordinates is loaded
     *
     * @param x X-coordinate of the chunk
     * @param z Z-coordinate of the chunk
     * @return true if the chunk is loaded, otherwise false
     */
    public boolean isChunkLoaded(int x, int z);

    /**
     * Loads the {@link Chunk} at the specified coordinates
     *
     * If the chunk does not exist, it will be generated.
     * This method is analogous to {@link #loadChunk(int, int, boolean)} where generate is true.
     *
     * @param x X-coordinate of the chunk
     * @param z Z-coordinate of the chunk
     */
    public void loadChunk(int x, int z);

    /**
     * Loads the {@link Chunk} at the specified coordinates
     *
     * @param x X-coordinate of the chunk
     * @param z Z-coordinate of the chunk
     * @param generate Whether or not to generate a chunk if it doesn't already exist
     * @return true if the chunk has loaded successfully, otherwise false
     */
    public boolean loadChunk(int x, int z, boolean generate);

    /**
     * Safely unloads and saves the {@link Chunk} at the specified coordinates
     *
     * This method is analogous to {@link #unloadChunk(int, int, boolean, boolean)} where safe and saveis true
     *
     * @param chunk the chunk to unload
     * @return true if the chunk has unloaded successfully, otherwise false
     */
    public boolean unloadChunk(Chunk chunk);

    /**
     * Safely unloads and saves the {@link Chunk} at the specified coordinates
     *
     * This method is analogous to {@link #unloadChunk(int, int, boolean, boolean)} where safe and saveis true
     *
     * @param x X-coordinate of the chunk
     * @param z Z-coordinate of the chunk
     * @return true if the chunk has unloaded successfully, otherwise false
     */
    public boolean unloadChunk(int x, int z);

    /**
     * Safely unloads and optionally saves the {@link Chunk} at the specified coordinates
     *
     * This method is analogous to {@link #unloadChunk(int, int, boolean, boolean)} where save is true
     *
     * @param x X-coordinate of the chunk
     * @param z Z-coordinate of the chunk
     * @param save Whether or not to save the chunk
     * @return true if the chunk has unloaded successfully, otherwise false
     */
    public boolean unloadChunk(int x, int z, boolean save);

    /**
     * Unloads and optionally saves the {@link Chunk} at the specified coordinates
     *
     * @param x X-coordinate of the chunk
     * @param z Z-coordinate of the chunk
     * @param save Controls whether the chunk is saved
     * @param safe Controls whether to unload the chunk when players are nearby
     * @return true if the chunk has unloaded successfully, otherwise false
     */
    public boolean unloadChunk(int x, int z, boolean save, boolean safe);

    /**
     * Safely queues the {@link Chunk} at the specified coordinates for unloading
     *
     * This method is analogous to {@link #unloadChunkRequest(int, int, boolean)} where safe is true
     *
     * @param x X-coordinate of the chunk
     * @param z Z-coordinate of the chunk
     * @return true is the queue attempt was successful, otherwise false
     */
    public boolean unloadChunkRequest(int x, int z);

    /**
     * Queues the {@link Chunk} at the specified coordinates for unloading
     *
     * @param x X-coordinate of the chunk
     * @param z Z-coordinate of the chunk
     * @param safe Controls whether to queue the chunk when players are nearby
     * @return Whether the chunk was actually queued
     */
    public boolean unloadChunkRequest(int x, int z, boolean safe);

    /**
     * Regenerates the {@link Chunk} at the specified coordinates
     *
     * @param x X-coordinate of the chunk
     * @param z Z-coordinate of the chunk
     * @return Whether the chunk was actually regenerated
     */
    public boolean regenerateChunk(int x, int z);

    /**
     * Resends the {@link Chunk} to all clients
     *
     * @param x X-coordinate of the chunk
     * @param z Z-coordinate of the chunk
     * @return Whether the chunk was actually refreshed
     */
    public boolean refreshChunk(int x, int z);

    /**
     * Drops an item at the specified {@link Location}
     *
     * @param location Location to drop the item
     * @param item ItemStack to drop
     * @return ItemDrop entity created as a result of this method
     */
    public Item dropItem(Location location, ItemStack item);

    /**
     * Drops an item at the specified {@link Location} with a random offset
     *
     * @param location Location to drop the item
     * @param item ItemStack to drop
     * @return ItemDrop entity created as a result of this method
     */
    public Item dropItemNaturally(Location location, ItemStack item);

    /**
     * Creates an {@link Arrow} entity at the given {@link Location}
     *
     * @param location Location to spawn the arrow
     * @param velocity Velocity to shoot the arrow in
     * @param speed Speed of the arrow. A recommend speed is 0.6
     * @param spread Spread of the arrow. A recommend spread is 12
     * @return Arrow entity spawned as a result of this method
     */
    public Arrow spawnArrow(Location location, Vector velocity, float speed, float spread);

    /**
     * Creates a tree at the given {@link Location}
     *
     * @param location Location to spawn the tree
     * @param type Type of the tree to create
     * @return true if the tree was created successfully, otherwise false
     */
    public boolean generateTree(Location location, TreeType type);

    /**
     * Creates a tree at the given {@link Location}
     *
     * @param loc Location to spawn the tree
     * @param type Type of the tree to create
     * @param delegate A class to call for each block changed as a result of this method
     * @return true if the tree was created successfully, otherwise false
     */
    public boolean generateTree(Location loc, TreeType type, BlockChangeDelegate delegate);

    /**
     * Creates a creature at the given {@link Location}
     *
     * @param loc The location to spawn the creature
     * @param type The creature to spawn
     * @return Resulting LivingEntity of this method, or null if it was unsuccessful
     */
    public LivingEntity spawnCreature(Location loc, CreatureType type);

    /**
     * Strikes lightning at the given {@link Location}
     *
     * @param loc The location to strike lightning
     * @return
     */
    public LightningStrike strikeLightning(Location loc);

    /**
     * Strikes lightning at the given {@link Location} without doing damage
     *
     * @param loc The location to strike lightning
     * @return
     */
    public LightningStrike strikeLightningEffect(Location loc);

    /**
     * Get a list of all entities in this World
     *
     * @return A List of all Entities currently residing in this world
     */
    public List<Entity> getEntities();

    /**
     * Get a list of all living entities in this World
     *
     * @return A List of all LivingEntities currently residing in this world
     */
    public List<LivingEntity> getLivingEntities();

    /**
     * Get a list of all players in this World
     *
     * @return A list of all Players currently residing in this world
     */
    public List<Player> getPlayers();

    /**
     * Gets the unique name of this world
     *
     * @return Name of this world
     */
    public String getName();

    /**
     * Gets the Unique ID of this world
     *
     * @return Unique ID of this world.
     */
    public UUID getUID();

    /**
     * Gets a semi-unique identifier for this world.
     *
     * While it is highly unlikely that this may be shared with another World,
     * it is not guaranteed to be unique
     *
     * @deprecated Replaced with {@link #getUID()}
     * @return Id of this world
     */
    @Deprecated
    public long getId();

    /**
     * Gets the default spawn {@link Location} of this world
     *
     * @return The spawn location of this world
     */
    public Location getSpawnLocation();

    /**
     * Sets the spawn location of the world
     *
     * @param x
     * @param y
     * @param z
     * @return True if it was successfully set.
     */
    public boolean setSpawnLocation(int x, int y, int z);

    // Poseidon start

    /**
     * Sets the spawn location of the world
     *
     * @param x
     * @param y
     * @param z
     * @param yaw
     * @param pitch
     * @return True if it was successfully set.
     */
    public boolean setSpawnLocation(int x, int y, int z, float yaw, float pitch);

    // Poseidon end

    /**
     * Gets the relative in-game time of this world.
     *
     * The relative time is analogous to hours * 1000
     *
     * @return The current relative time
     * @see #getFullTime() Returns an absolute time of this world
     */
    public long getTime();

    /**
     * Sets the relative in-game time on the server.
     *
     * The relative time is analogous to hours * 1000
     * <br /><br />
     * Note that setting the relative time below the current relative time will
     * actually move the clock forward a day. If you require to rewind time, please
     * see setFullTime
     *
     * @param time The new relative time to set the in-game time to (in hours*1000)
     * @see #setFullTime(long) Sets the absolute time of this world
     */
    public void setTime(long time);

    /**
     * Gets the full in-game time on this world
     *
     * @return The current absolute time
     * @see #getTime() Returns a relative time of this world
     */
    public long getFullTime();

    /**
     * Sets the in-game time on the server
     * <br /><br />
     * Note that this sets the full time of the world, which may cause adverse
     * effects such as breaking redstone clocks and any scheduled events
     *
     * @param time The new absolute time to set this world to
     * @see #setTime(long) Sets the relative time of this world
     */
    public void setFullTime(long time);

    /**
     * Returns whether the world has an ongoing storm.
     *
     * @return Whether there is an ongoing storm
     */
    public boolean hasStorm();

    /**
     * Set whether there is a storm. A duration will be set for the new
     * current conditions.
     *
     * @param hasStorm Whether there is rain and snow
     */
    public void setStorm(boolean hasStorm);

    /**
     * Get the remaining time in ticks of the current conditions.
     *
     * @return Time in ticks
     */
    public int getWeatherDuration();

    /**
     * Set the remaining time in ticks of the current conditions.
     *
     * @param duration Time in ticks
     */
    public void setWeatherDuration(int duration);

    /**
     * Returns whether there is thunder.
     *
     * @return Whether there is thunder
     */
    public boolean isThundering();

    /**
     * Set whether it is thundering.
     *
     * @param thundering Whether it is thundering
     */
    public void setThundering(boolean thundering);

    /**
     * Get the thundering duration.
     *
     * @return Duration in ticks
     */
    public int getThunderDuration();

    /**
     * Set the thundering duration.
     *
     * @param duration Duration in ticks
     */
    public void setThunderDuration(int duration);

    /**
     * Creates explosion at given coordinates with given power
     *
     * @param x
     * @param y
     * @param z
     * @param power The power of explosion, where 4F is TNT
     * @return false if explosion was canceled, otherwise true
     */
    public boolean createExplosion(double x, double y, double z, float power);

    /**
     * Creates explosion at given coordinates with given power and optionally setting
     * blocks on fire.
     *
     * @param x
     * @param y
     * @param z
     * @param power The power of explosion, where 4F is TNT
     * @param setFire Whether or not to set blocks on fire
     * @return false if explosion was canceled, otherwise true
     */
    public boolean createExplosion(double x, double y, double z, float power, boolean setFire);

    /**
     * Creates explosion at given coordinates with given power and DamageCause and optionally setting
     * blocks on fire.
     *
     * @param x
     * @param y
     * @param z
     * @param power The power of explosion, where 4F is TNT
     * @param setFire Whether or not to set blocks on fire
     * @param customDamageCause The DamageCause to use for the explosion
     * @return false if explosion was canceled, otherwise true
     */
    public boolean createExplosion(double x, double y, double z, float power, boolean setFire, EntityDamageEvent.DamageCause customDamageCause);

    /**
     * Creates explosion at given coordinates with given power
     *
     * @param loc
     * @param power The power of explosion, where 4F is TNT
     * @return false if explosion was canceled, otherwise true
     */
    public boolean createExplosion(Location loc, float power);

    /**
     * Creates explosion at given coordinates with given power and optionally setting
     * blocks on fire.
     *
     * @param loc
     * @param power The power of explosion, where 4F is TNT
     * @param setFire Whether or not to set blocks on fire
     * @return false if explosion was canceled, otherwise true
     */
    public boolean createExplosion(Location loc, float power, boolean setFire);

    /**
     * Creates explosion at given coordinates with given power and DamageCause and optionally setting
     * blocks on fire.
     *
     * @param loc
     * @param power The power of explosion, where 4F is TNT
     * @param setFire Whether or not to set blocks on fire
     * @param customDamageCause The DamageCause to use for the explosion
     * @return false if explosion was canceled, otherwise true
     */
    public boolean createExplosion(Location loc, float power, boolean setFire, EntityDamageEvent.DamageCause customDamageCause);

    /**
     * Gets the {@link Environment} type of this world
     *
     * @return This worlds Environment type
     */
    public Environment getEnvironment();

    /**
     * Gets the Seed for this world.
     *
     * @return This worlds Seed
     */
    public long getSeed();

    /**
     * Gets the current PVP setting for this world.
     * @return
     */
    public boolean getPVP();

    /**
     * Sets the PVP setting for this world.
     * @param pvp True/False whether PVP should be Enabled.
     */
    public void setPVP(boolean pvp);

    /**
     * Gets the chunk generator for this world
     *
     * @return ChunkGenerator associated with this world
     */
    public ChunkGenerator getGenerator();

    /**
     * Saves world to disk
     */
    public void save();

    /**
     * Gets a list of all applied {@link BlockPopulator}s for this World
     *
     * @return List containing any or none BlockPopulators
     */
    public List<BlockPopulator> getPopulators();

    /**
     * Spawn an entity of a specific class at the given {@link Location}
     *
     * @param location the {@link Location} to spawn the entity at
     * @param clazz the class of the {@link Entity} to spawn
     * @return an instance of the spawned {@link Entity}
     * @throws an {@link IllegalArgumentException} if either parameter is null or the {@link Entity} requested cannot be spawned
     */
    public <T extends Entity> T spawn(Location location, Class<T> clazz) throws IllegalArgumentException;

    /**
     * Plays an effect to all players within a default radius around a given location.
     *
     * @param location the {@link Location} around which players must be to hear the sound
     * @param effect the {@link Effect}
     * @param data a data bit needed for the RECORD_PLAY, SMOKE, and STEP_SOUND sounds
     */
    public void playEffect(Location location, Effect effect, int data);

    /**
     * Plays an effect to all players within a given radius around a location.
     *
     * @param location the {@link Location} around which players must be to hear the effect
     * @param effect the {@link Effect}
     * @param data a data bit needed for the RECORD_PLAY, SMOKE, and STEP effects
     * @param radius the radius around the location
     */
    public void playEffect(Location location, Effect effect, int data, int radius);

    /**
     * Get empty chunk snapshot (equivalent to all air blocks), optionally including valid biome
     * data.  Used for representing an ungenerated chunk, or for fetching only biome data without loading a chunk.
     * @param x - chunk x coordinate
     * @param z - chunk z coordinate
     * @param includeBiome - if true, snapshot includes per-coordinate biome type
     * @param includeBiomeTempRain - if true, snapshot includes per-coordinate raw biome temperature and rainfall
     */
    public ChunkSnapshot getEmptyChunkSnapshot(int x, int z, boolean includeBiome, boolean includeBiomeTempRain);

    /**
     * Sets the spawn flags for this.
     *
     * @param allowMonsters - if true, monsters are allowed to spawn in this world.
     * @param allowAnimals - if true, animals are allowed to spawn in this world.
     */
    public void setSpawnFlags(boolean allowMonsters, boolean allowAnimals);

    /**
     * Gets whether animals can spawn in this world.
     *
     * @return whether animals can spawn in this world.
     */
    public boolean getAllowAnimals();

    /**
     * Gets whether monsters can spawn in this world.
     *
     * @return whether monsters can spawn in this world.
     */
    public boolean getAllowMonsters();

    /**
     * Gets the biome for the given block coordinates.
     *
     * It is safe to run this method when the block does not exist, it will not create the block.
     *
     * @param x X coordinate of the block
     * @param z Z coordinate of the block
     *
     * @return Biome of the requested block
     */
    public Biome getBiome(int x, int z);

    /**
     * Gets the temperature for the given block coordinates.
     *
     * It is safe to run this method when the block does not exist, it will not create the block.
     *
     * @param x X coordinate of the block
     * @param z Z coordinate of the block
     *
     * @return Temperature of the requested block
     */
    public double getTemperature(int x, int z);

    /**
     * Gets the humidity for the given block coordinates.
     *
     * It is safe to run this method when the block does not exist, it will not create the block.
     *
     * @param x X coordinate of the block
     * @param z Z coordinate of the block
     *
     * @return Humidity of the requested block
     */
    public double getHumidity(int x, int z);

    /**
     * Gets the maximum height of this world.
     *
     * If the max height is 100, there are only blocks from y=0 to y=99.
     *
     * @return Maximum height of the world
     */
    public int getMaxHeight();

    /**
     * Gets whether the world's spawn area should be kept loaded into memory or not.
     *
     * @return true if the world's spawn area will be kept loaded into memory.
     */
    public boolean getKeepSpawnInMemory();

    /**
    * Sets whether the world's spawn area should be kept loaded into memory or not.
    *
    * @param keepLoaded if true then the world's spawn area will be kept loaded into memory.
    */
    public void setKeepSpawnInMemory(boolean keepLoaded);

    /**
     * Gets whether or not the world will automatically save
     *
     * @return true if the world will automatically save, otherwise false
     */
    public boolean isAutoSave();

    /**
     * Sets whether or not the world will automatically save
     *
     * @param value true if the world should automatically save, otherwise false
     */
    public void setAutoSave(boolean value);

    /**
     * Represents various map environment types that a world may be
     */
    public enum Environment {
        /**
         * Represents the "normal"/"surface world" map
         */
        NORMAL(0),
        /**
         * Represents a nether based map ("hell")
         */
        NETHER(-1),
        /**
         * Represents a sky-lands based map ("heaven")
         */
        SKYLANDS(1);

        private final int id;
        private static final Map<Integer, Environment> lookup = new HashMap<Integer, Environment>();

        private Environment(int id) {
            this.id = id;
        }

        /**
         * Gets the dimension ID of this environment
         *
         * @return dimension ID
         */
        public int getId() {
            return id;
        }

        public static Environment getEnvironment(int id) {
            return lookup.get(id);
        }

        static {
            for (Environment env : values()) {
                lookup.put(env.getId(), env);
            }
        }
    }
}
