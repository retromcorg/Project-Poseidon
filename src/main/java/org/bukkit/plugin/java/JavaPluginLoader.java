package org.bukkit.plugin.java;

import com.legacyminecraft.poseidon.event.PoseidonCustomListener;
import org.bukkit.Server;
import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.packet.PacketListener;
import org.bukkit.event.packet.PacketReceivedEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.*;
import org.bukkit.event.vehicle.*;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.weather.WeatherListener;
import org.bukkit.event.world.*;
import org.bukkit.plugin.*;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Represents a Java plugin loader, allowing plugins in the form of .jar
 */
public class JavaPluginLoader implements PluginLoader
{
    private final Server server;
    protected final Pattern[] fileFilters = new Pattern[] { Pattern.compile("\\.jar$"), };
    protected final Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
    protected final Map<String, PluginClassLoader> loaders = new HashMap<String, PluginClassLoader>();

    public JavaPluginLoader(Server instance)
    {
        server = instance;
    }

    public Plugin loadPlugin(File file) throws InvalidPluginException, InvalidDescriptionException, UnknownDependencyException
    {
        return loadPlugin(file, false);
    }

    public Plugin loadPlugin(File file, boolean ignoreSoftDependencies) throws InvalidPluginException, InvalidDescriptionException, UnknownDependencyException
    {
        JavaPlugin result = null;
        PluginDescriptionFile description = null;

        if (!file.exists())
        {
            throw new InvalidPluginException(new FileNotFoundException(String.format("%s does not exist", file.getPath())));
        }
        try
        {
            JarFile jar = new JarFile(file);
            JarEntry entry = jar.getJarEntry("plugin.yml");

            if (entry == null)
            {
                throw new InvalidPluginException(new FileNotFoundException("Jar does not contain plugin.yml"));
            }

            InputStream stream = jar.getInputStream(entry);

            description = new PluginDescriptionFile(stream);

            stream.close();
            jar.close();
        } catch (IOException ex)
        {
            throw new InvalidPluginException(ex);
        } catch (YAMLException ex)
        {
            throw new InvalidPluginException(ex);
        }

        File dataFolder = new File(file.getParentFile(), description.getName());
        File oldDataFolder = getDataFolder(file);

        // Found old data folder
        if (dataFolder.equals(oldDataFolder))
        {
            // They are equal -- nothing needs to be done!
        } else if (dataFolder.isDirectory() && oldDataFolder.isDirectory())
        {
            server.getLogger().log(Level.INFO, String.format("While loading %s (%s) found old-data folder: %s next to the new one: %s", description.getName(), file, oldDataFolder, dataFolder));
        } else if (oldDataFolder.isDirectory() && !dataFolder.exists())
        {
            if (!oldDataFolder.renameTo(dataFolder))
            {
                throw new InvalidPluginException(new Exception("Unable to rename old data folder: '" + oldDataFolder + "' to: '" + dataFolder + "'"));
            }
            server.getLogger().log(Level.INFO, String.format("While loading %s (%s) renamed data folder: '%s' to '%s'", description.getName(), file, oldDataFolder, dataFolder));
        }

        if (dataFolder.exists() && !dataFolder.isDirectory())
        {
            throw new InvalidPluginException(new Exception(String.format("Projected datafolder: '%s' for %s (%s) exists and is not a directory", dataFolder, description.getName(), file)));
        }

        ArrayList<String> depend;

        try
        {
            depend = (ArrayList) description.getDepend();
            if (depend == null)
            {
                depend = new ArrayList<String>();
            }
        } catch (ClassCastException ex)
        {
            throw new InvalidPluginException(ex);
        }

        for (String pluginName : depend)
        {
            if (loaders == null)
            {
                throw new UnknownDependencyException(pluginName);
            }
            PluginClassLoader current = loaders.get(pluginName);

            if (current == null)
            {
                throw new UnknownDependencyException(pluginName);
            }
        }

        if (!ignoreSoftDependencies)
        {
            ArrayList<String> softDepend;

            try
            {
                softDepend = (ArrayList) description.getSoftDepend();
                if (softDepend == null)
                {
                    softDepend = new ArrayList<String>();
                }
            } catch (ClassCastException ex)
            {
                throw new InvalidPluginException(ex);
            }

            for (String pluginName : softDepend)
            {
                if (loaders == null)
                {
                    throw new UnknownSoftDependencyException(pluginName);
                }
                PluginClassLoader current = loaders.get(pluginName);

                if (current == null)
                {
                    throw new UnknownSoftDependencyException(pluginName);
                }
            }
        }

        PluginClassLoader loader = null;

        try
        {
            URL[] urls = new URL[1];

            urls[0] = file.toURI().toURL();
            loader = new PluginClassLoader(this, urls, getClass().getClassLoader());
            Class<?> jarClass = Class.forName(description.getMain(), true, loader);
            Class<? extends JavaPlugin> plugin = jarClass.asSubclass(JavaPlugin.class);

            Constructor<? extends JavaPlugin> constructor = plugin.getConstructor();

            result = constructor.newInstance();

            result.initialize(this, server, description, dataFolder, file, loader);
        } catch (Throwable ex)
        {
            throw new InvalidPluginException(ex);
        }

        loaders.put(description.getName(), (PluginClassLoader) loader);

        return (Plugin) result;
    }

    // Project Poseidon Start
    private void notNull(Object object, String message) {
        if (object == null)
            throw new IllegalArgumentException(message);
    }
    @Override
    @NotNull
    public Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(@NotNull Listener listener, @NotNull final Plugin plugin) {
        notNull(plugin, "Plugin can not be null");
        notNull(listener, "Listener can not be null");

        Map<Class<? extends Event>, Set<RegisteredListener>> ret = new HashMap<>();
        Set<Method> methods;
        try {
            Method[] publicMethods = listener.getClass().getMethods();
            Method[] privateMethods = listener.getClass().getDeclaredMethods();
            methods = new HashSet<Method>(publicMethods.length + privateMethods.length, 1.0f);
            Collections.addAll(methods, publicMethods);
            Collections.addAll(methods, privateMethods);
        } catch (NoClassDefFoundError e) {
            if (listener instanceof PoseidonCustomListener) {
                plugin.getServer().getLogger().log(Level.WARNING, "The plugin " + plugin.getDescription().getName() + " has tried to register an unknown event. Please ensure the plugin containing the event is loaded before any plugins that listen.");
            } else {
                plugin.getServer().getLogger().severe("Plugin " + plugin.getDescription().getFullName() + " has failed to register events for " + listener.getClass() + " because " + e.getMessage() + " does not exist.");

            }
            return ret;
        }

        for (final Method method : methods) {
            final EventHandler eh = method.getAnnotation(EventHandler.class);
            if (eh == null)
                continue;
            if (method.isBridge() || method.isSynthetic())
                continue;
            final Class<?> checkClass;
            if (method.getParameterTypes().length != 1 || !Event.class.isAssignableFrom(checkClass = method.getParameterTypes()[0])) {
                plugin.getServer().getLogger().severe(plugin.getDescription().getFullName() + " attempted to register an invalid EventHandler method signature \"" + method.toGenericString() + "\" in " + listener.getClass());
                continue;
            }
            final Class<? extends Event> eventClass = checkClass.asSubclass(Event.class);
            method.setAccessible(true);
            Set<RegisteredListener> eventSet = ret.computeIfAbsent(eventClass, k -> new HashSet<>());

            for (Class<?> clazz = eventClass; Event.class.isAssignableFrom(clazz); clazz = clazz.getSuperclass()) {
                if (clazz.getAnnotation(Deprecated.class) != null) {
                    plugin.getServer().getLogger().log(
                            Level.WARNING,
                            String.format(
                                    "\"%s\" has registered a listener for %s on method \"%s\", but the event is Deprecated." +
                                            " \"%s\"; please notify the authors %s.",
                                    plugin.getDescription().getFullName(),
                                    clazz.getName(),
                                    method.toGenericString(),
                                    "Server performance will be affected",
                                    Arrays.toString(plugin.getDescription().getAuthors().toArray())),
                            new AuthorNagException(null));
                    break;
                }
            }

            final EventExecutor executor = (listener1, event) -> {
                try {
                    method.invoke(listener1, event);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            };
            eventSet.add(new RegisteredListener(listener, executor, eh.priority(), plugin, eh.ignoreCancelled()));
        }
        return ret;
    }
    // Project Poseidon End

    protected File getDataFolder(File file)
    {
        File dataFolder = null;

        String filename = file.getName();
        int index = file.getName().lastIndexOf(".");

        if (index != -1)
        {
            String name = filename.substring(0, index);

            dataFolder = new File(file.getParentFile(), name);
        } else
        {
            // This is if there is no extension, which should not happen
            // Using _ to prevent name collision

            dataFolder = new File(file.getParentFile(), filename + "_");
        }

        return dataFolder;
    }

    public Pattern[] getPluginFileFilters()
    {
        return fileFilters;
    }

    public Class<?> getClassByName(final String name)
    {
        Class<?> cachedClass = classes.get(name);

        if (cachedClass != null)
        {
            return cachedClass;
        } else
        {
            for (String current : loaders.keySet())
            {
                PluginClassLoader loader = loaders.get(current);

                try
                {
                    cachedClass = loader.findClass(name, false);
                } catch (ClassNotFoundException cnfe)
                {
                }
                if (cachedClass != null)
                {
                    return cachedClass;
                }
            }
        }
        return null;
    }

    public void setClass(final String name, final Class<?> clazz)
    {
        if (!classes.containsKey(name))
        {
            classes.put(name, clazz);
        }
    }

    public EventExecutor createExecutor(Event.Type type, Listener listener)
    {
        // TODO: remove multiple Listener type and hence casts

        switch (type)
        {
            // Poseidon events
            case PACKET_RECEIVED:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((PacketListener) listener).onPacketReceived((PacketReceivedEvent) event);
                    }
                };
                
            case INVENTORY_TRANSACTION:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((InventoryListener) listener).onInventoryTransaction((InventoryTransactionEvent) event);
                    }
                };
            
            // Player Events

            case PLAYER_JOIN:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((PlayerListener) listener).onPlayerJoin((PlayerJoinEvent) event);
                    }
                };

            case PLAYER_QUIT:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((PlayerListener) listener).onPlayerQuit((PlayerQuitEvent) event);
                    }
                };

            case PLAYER_RESPAWN:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((PlayerListener) listener).onPlayerRespawn((PlayerRespawnEvent) event);
                    }
                };
            // Project Poseidon Start
            case ITEM_DESPAWN:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((EntityListener) listener).onItemDespawn((ItemDespawnEvent) event);
                    }
                };
            // Project Poseidon End
            case PLAYER_KICK:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((PlayerListener) listener).onPlayerKick((PlayerKickEvent) event);
                    }
                };

            case PLAYER_COMMAND_PREPROCESS:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((PlayerListener) listener).onPlayerCommandPreprocess((PlayerCommandPreprocessEvent) event);
                    }
                };

            case PLAYER_CHAT:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((PlayerListener) listener).onPlayerChat((PlayerChatEvent) event);
                    }
                };

            case PLAYER_MOVE:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((PlayerListener) listener).onPlayerMove((PlayerMoveEvent) event);
                    }
                };

            case PLAYER_VELOCITY:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((PlayerListener) listener).onPlayerVelocity((PlayerVelocityEvent) event);
                    }
                };

            case PLAYER_TELEPORT:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((PlayerListener) listener).onPlayerTeleport((PlayerTeleportEvent) event);
                    }
                };

            case PLAYER_PORTAL:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((PlayerListener) listener).onPlayerPortal((PlayerPortalEvent) event);
                    }
                };

            case PLAYER_INTERACT:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((PlayerListener) listener).onPlayerInteract((PlayerInteractEvent) event);
                    }
                };

            case PLAYER_INTERACT_ENTITY:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((PlayerListener) listener).onPlayerInteractEntity((PlayerInteractEntityEvent) event);
                    }
                };

            case PLAYER_LOGIN:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((PlayerListener) listener).onPlayerLogin((PlayerLoginEvent) event);
                    }
                };

            case PLAYER_PRELOGIN:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((PlayerListener) listener).onPlayerPreLogin((PlayerPreLoginEvent) event);
                    }
                };

            case PLAYER_EGG_THROW:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((PlayerListener) listener).onPlayerEggThrow((PlayerEggThrowEvent) event);
                    }
                };

            case PLAYER_ANIMATION:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((PlayerListener) listener).onPlayerAnimation((PlayerAnimationEvent) event);
                    }
                };

            case PLAYER_ITEM_HELD:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((PlayerListener) listener).onItemHeldChange((PlayerItemHeldEvent) event);
                    }
                };

            case PLAYER_DROP_ITEM:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((PlayerListener) listener).onPlayerDropItem((PlayerDropItemEvent) event);
                    }
                };

            case PLAYER_PICKUP_ITEM:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((PlayerListener) listener).onPlayerPickupItem((PlayerPickupItemEvent) event);
                    }
                };

            case PLAYER_TOGGLE_SNEAK:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((PlayerListener) listener).onPlayerToggleSneak((PlayerToggleSneakEvent) event);
                    }
                };

            case PLAYER_BUCKET_EMPTY:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((PlayerListener) listener).onPlayerBucketEmpty((PlayerBucketEmptyEvent) event);
                    }
                };

            case PLAYER_BUCKET_FILL:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((PlayerListener) listener).onPlayerBucketFill((PlayerBucketFillEvent) event);
                    }
                };

            case PLAYER_BED_ENTER:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((PlayerListener) listener).onPlayerBedEnter((PlayerBedEnterEvent) event);
                    }
                };

            case PLAYER_BED_LEAVE:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((PlayerListener) listener).onPlayerBedLeave((PlayerBedLeaveEvent) event);
                    }
                };

            case PLAYER_FISH:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((PlayerListener) listener).onPlayerFish((PlayerFishEvent) event);
                    }
                };
            case PLAYER_ITEM_DAMAGE:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((PlayerListener) listener).onPlayerItemDamage((PlayerItemDamageEvent) event);
                    }
                };
            case PLAYER_CHANGED_WORLD:
                return new EventExecutor() {
                    public void execute(Listener listener, Event event) {
                        ((PlayerListener) listener).onPlayerChangedWorld((PlayerChangedWorldEvent) event);
                    }
                };

            // Block Events
            case BLOCK_PHYSICS:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((BlockListener) listener).onBlockPhysics((BlockPhysicsEvent) event);
                    }
                };

            case BLOCK_CANBUILD:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((BlockListener) listener).onBlockCanBuild((BlockCanBuildEvent) event);
                    }
                };

            case BLOCK_PLACE:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((BlockListener) listener).onBlockPlace((BlockPlaceEvent) event);
                    }
                };

            case BLOCK_DAMAGE:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((BlockListener) listener).onBlockDamage((BlockDamageEvent) event);
                    }
                };

            case BLOCK_FROMTO:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((BlockListener) listener).onBlockFromTo((BlockFromToEvent) event);
                    }
                };

            case LEAVES_DECAY:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((BlockListener) listener).onLeavesDecay((LeavesDecayEvent) event);
                    }
                };

            case SIGN_CHANGE:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((BlockListener) listener).onSignChange((SignChangeEvent) event);
                    }
                };

            case BLOCK_IGNITE:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((BlockListener) listener).onBlockIgnite((BlockIgniteEvent) event);
                    }
                };

            case REDSTONE_CHANGE:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((BlockListener) listener).onBlockRedstoneChange((BlockRedstoneEvent) event);
                    }
                };

            case BLOCK_BURN:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((BlockListener) listener).onBlockBurn((BlockBurnEvent) event);
                    }
                };

            case BLOCK_BREAK:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((BlockListener) listener).onBlockBreak((BlockBreakEvent) event);
                    }
                };

            case BLOCK_FORM:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((BlockListener) listener).onBlockForm((BlockFormEvent) event);
                    }
                };

            case BLOCK_SPREAD:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((BlockListener) listener).onBlockSpread((BlockSpreadEvent) event);
                    }
                };

            case BLOCK_FADE:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((BlockListener) listener).onBlockFade((BlockFadeEvent) event);
                    }
                };

            case BLOCK_DISPENSE:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((BlockListener) listener).onBlockDispense((BlockDispenseEvent) event);
                    }
                };

            case BLOCK_PISTON_RETRACT:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((BlockListener) listener).onBlockPistonRetract((BlockPistonRetractEvent) event);
                    }
                };

            case BLOCK_PISTON_EXTEND:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((BlockListener) listener).onBlockPistonExtend((BlockPistonExtendEvent) event);
                    }
                };

            // Server Events
            case PLUGIN_ENABLE:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((ServerListener) listener).onPluginEnable((PluginEnableEvent) event);
                    }
                };

            case PLUGIN_DISABLE:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((ServerListener) listener).onPluginDisable((PluginDisableEvent) event);
                    }
                };

            case SERVER_COMMAND:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((ServerListener) listener).onServerCommand((ServerCommandEvent) event);
                    }
                };

            case MAP_INITIALIZE:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((ServerListener) listener).onMapInitialize((MapInitializeEvent) event);
                    }
                };

            // World Events
            case CHUNK_LOAD:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((WorldListener) listener).onChunkLoad((ChunkLoadEvent) event);
                    }
                };

            case CHUNK_POPULATED:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((WorldListener) listener).onChunkPopulate((ChunkPopulateEvent) event);
                    }
                };

            case CHUNK_UNLOAD:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((WorldListener) listener).onChunkUnload((ChunkUnloadEvent) event);
                    }
                };

            case SPAWN_CHANGE:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((WorldListener) listener).onSpawnChange((SpawnChangeEvent) event);
                    }
                };

            case WORLD_SAVE:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((WorldListener) listener).onWorldSave((WorldSaveEvent) event);
                    }
                };

            case WORLD_INIT:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((WorldListener) listener).onWorldInit((WorldInitEvent) event);
                    }
                };

            case WORLD_LOAD:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((WorldListener) listener).onWorldLoad((WorldLoadEvent) event);
                    }
                };

            case WORLD_UNLOAD:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((WorldListener) listener).onWorldUnload((WorldUnloadEvent) event);
                    }
                };

            case PORTAL_CREATE:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((WorldListener) listener).onPortalCreate((PortalCreateEvent) event);
                    }
                };

            // Painting Events
            case PAINTING_PLACE:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((EntityListener) listener).onPaintingPlace((PaintingPlaceEvent) event);
                    }
                };

            case PAINTING_BREAK:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((EntityListener) listener).onPaintingBreak((PaintingBreakEvent) event);
                    }
                };

            // Entity Events
            case ENTITY_DAMAGE:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((EntityListener) listener).onEntityDamage((EntityDamageEvent) event);
                    }
                };
            // Project Poseidon Start
            case ENTITY_DAMAGE_BY_ENTITY:
                return new EventExecutor() {
                    @Override
                    public void execute(Listener listener, Event event) {
                        ((EntityListener) listener).onEntityDamageByEntity((EntityDamageByEntityEvent) event);
                    }
                };
            case ENTITY_DAMAGE_BY_BLOCK:
                return new EventExecutor() {
                    @Override
                    public void execute(Listener listener, Event event) {
                        ((EntityListener) listener).onEntityDamageByBlock((EntityDamageByBlockEvent) event);
                    }
                };
            // Project Poseidon End

            case ENTITY_DEATH:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((EntityListener) listener).onEntityDeath((EntityDeathEvent) event);
                    }
                };

            case ENTITY_COMBUST:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((EntityListener) listener).onEntityCombust((EntityCombustEvent) event);
                    }
                };

            case ENTITY_EXPLODE:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((EntityListener) listener).onEntityExplode((EntityExplodeEvent) event);
                    }
                };

            case EXPLOSION_PRIME:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((EntityListener) listener).onExplosionPrime((ExplosionPrimeEvent) event);
                    }
                };

            case ENTITY_TARGET:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((EntityListener) listener).onEntityTarget((EntityTargetEvent) event);
                    }
                };

            case ENTITY_INTERACT:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((EntityListener) listener).onEntityInteract((EntityInteractEvent) event);
                    }
                };

            case ENTITY_PORTAL_ENTER:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((EntityListener) listener).onEntityPortalEnter((EntityPortalEnterEvent) event);
                    }
                };

            case CREATURE_SPAWN:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((EntityListener) listener).onCreatureSpawn((CreatureSpawnEvent) event);
                    }
                };

            case ITEM_SPAWN:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((EntityListener) listener).onItemSpawn((ItemSpawnEvent) event);
                    }
                };

            case PIG_ZAP:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((EntityListener) listener).onPigZap((PigZapEvent) event);
                    }
                };

            case CREEPER_POWER:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((EntityListener) listener).onCreeperPower((CreeperPowerEvent) event);
                    }
                };

            case ENTITY_TAME:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((EntityListener) listener).onEntityTame((EntityTameEvent) event);
                    }
                };

            case ENTITY_REGAIN_HEALTH:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((EntityListener) listener).onEntityRegainHealth((EntityRegainHealthEvent) event);
                    }
                };

            case PROJECTILE_HIT:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((EntityListener) listener).onProjectileHit((ProjectileHitEvent) event);
                    }
                };

            // Vehicle Events
            case VEHICLE_CREATE:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((VehicleListener) listener).onVehicleCreate((VehicleCreateEvent) event);
                    }
                };

            case VEHICLE_DAMAGE:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((VehicleListener) listener).onVehicleDamage((VehicleDamageEvent) event);
                    }
                };

            case VEHICLE_DESTROY:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((VehicleListener) listener).onVehicleDestroy((VehicleDestroyEvent) event);
                    }
                };

            case VEHICLE_COLLISION_BLOCK:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((VehicleListener) listener).onVehicleBlockCollision((VehicleBlockCollisionEvent) event);
                    }
                };

            case VEHICLE_COLLISION_ENTITY:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((VehicleListener) listener).onVehicleEntityCollision((VehicleEntityCollisionEvent) event);
                    }
                };

            case VEHICLE_ENTER:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((VehicleListener) listener).onVehicleEnter((VehicleEnterEvent) event);
                    }
                };

            case VEHICLE_EXIT:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((VehicleListener) listener).onVehicleExit((VehicleExitEvent) event);
                    }
                };

            case VEHICLE_MOVE:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((VehicleListener) listener).onVehicleMove((VehicleMoveEvent) event);
                    }
                };

            case VEHICLE_UPDATE:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((VehicleListener) listener).onVehicleUpdate((VehicleUpdateEvent) event);
                    }
                };

            // Weather Events
            case WEATHER_CHANGE:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((WeatherListener) listener).onWeatherChange((WeatherChangeEvent) event);
                    }
                };

            case THUNDER_CHANGE:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((WeatherListener) listener).onThunderChange((ThunderChangeEvent) event);
                    }
                };

            case LIGHTNING_STRIKE:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((WeatherListener) listener).onLightningStrike((LightningStrikeEvent) event);
                    }
                };

            // Inventory Events
            case INVENTORY_OPEN:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((InventoryListener) listener).onInventoryOpen((InventoryOpenEvent) event);
                    }
                };

            case INVENTORY_CLOSE:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((InventoryListener) listener).onInventoryClose((InventoryCloseEvent) event);
                    }
                };

            case INVENTORY_CLICK:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((InventoryListener) listener).onInventoryClick((InventoryClickEvent) event);
                    }
                };

            case FURNACE_SMELT:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((InventoryListener) listener).onFurnaceSmelt((FurnaceSmeltEvent) event);
                    }
                };

            case FURNACE_BURN:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((InventoryListener) listener).onFurnaceBurn((FurnaceBurnEvent) event);
                    }
                };

            // Custom Events
            case CUSTOM_EVENT:
                return new EventExecutor()
                {
                    public void execute(Listener listener, Event event)
                    {
                        ((CustomEventListener) listener).onCustomEvent(event);
                    }
                };
        }

        throw new IllegalArgumentException("Event " + type + " is not supported");
    }

    public void enablePlugin(final Plugin plugin)
    {
        if (!(plugin instanceof JavaPlugin))
        {
            throw new IllegalArgumentException("Plugin is not associated with this PluginLoader");
        }

        if (!plugin.isEnabled())
        {
            JavaPlugin jPlugin = (JavaPlugin) plugin;

            String pluginName = jPlugin.getDescription().getName();

            if (!loaders.containsKey(pluginName))
            {
                loaders.put(pluginName, (PluginClassLoader) jPlugin.getClassLoader());
            }

            try
            {
                jPlugin.setEnabled(true);
            } catch (Throwable ex)
            {
                server.getLogger().log(Level.SEVERE, "Error occurred while enabling " + plugin.getDescription().getFullName() + " (Is it up to date?): " + ex.getMessage(), ex);
            }

            // Perhaps abort here, rather than continue going, but as it stands,
            // an abort is not possible the way it's currently written
            server.getPluginManager().callEvent(new PluginEnableEvent(plugin));
        }
    }

    public void disablePlugin(Plugin plugin)
    {
        if (!(plugin instanceof JavaPlugin))
        {
            throw new IllegalArgumentException("Plugin is not associated with this PluginLoader");
        }

        if (plugin.isEnabled())
        {
            JavaPlugin jPlugin = (JavaPlugin) plugin;
            ClassLoader cloader = jPlugin.getClassLoader();

            try
            {
                jPlugin.setEnabled(false);
            } catch (Throwable ex)
            {
                server.getLogger().log(Level.SEVERE, "Error occurred while disabling " + plugin.getDescription().getFullName() + " (Is it up to date?): " + ex.getMessage(), ex);
            }

            server.getPluginManager().callEvent(new PluginDisableEvent(plugin));

            loaders.remove(jPlugin.getDescription().getName());

            if (cloader instanceof PluginClassLoader)
            {
                PluginClassLoader loader = (PluginClassLoader) cloader;
                Set<String> names = loader.getClasses();

                for (String name : names)
                {
                    classes.remove(name);
                }
            }
        }
    }
}
