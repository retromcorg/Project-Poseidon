package org.bukkit.plugin;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;
import com.legacyminecraft.poseidon.Poseidon;
import com.legacyminecraft.poseidon.event.PoseidonCustomListener;
import com.legacyminecraft.poseidon.utility.PerformanceStatistic;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.FileUtil;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles all plugin management from the Server
 */
public final class SimplePluginManager implements PluginManager {
    private final Server server;
    private final Map<Pattern, PluginLoader> fileAssociations = new HashMap<Pattern, PluginLoader>();
    private final List<Plugin> plugins = new ArrayList<Plugin>();
    private final Map<String, Plugin> lookupNames = new HashMap<String, Plugin>();
    private final Map<Event.Type, SortedSet<RegisteredListener>> listeners = new EnumMap<Event.Type, SortedSet<RegisteredListener>>(Event.Type.class);
    private static File updateDirectory = null;
    private final SimpleCommandMap commandMap;
    private final Map<String, Permission> permissions = new HashMap<String, Permission>();
    private final Map<Boolean, Set<Permission>> defaultPerms = new LinkedHashMap<Boolean, Set<Permission>>();
    private final Map<String, Map<Permissible, Boolean>> permSubs = new HashMap<String, Map<Permissible, Boolean>>();
    private final Map<Boolean, Map<Permissible, Boolean>> defSubs = new HashMap<Boolean, Map<Permissible, Boolean>>();
    private final Comparator<RegisteredListener> comparer = new Comparator<RegisteredListener>() {
        public int compare(RegisteredListener i, RegisteredListener j) {
            int result = i.getPriority().compareTo(j.getPriority());

            if ((result == 0) && (i != j)) {
                result = 1;
            }

            return result;
        }
    };

    public SimplePluginManager(Server instance, SimpleCommandMap commandMap) {
        server = instance;
        this.commandMap = commandMap;

        defaultPerms.put(true, new HashSet<Permission>());
        defaultPerms.put(false, new HashSet<Permission>());

        // Project Poseidon Start
        this.listenerPerformanceEnabled = Poseidon.getServer().getConfig().getConfigBoolean("settings.performance-monitoring.listener-reporting.enabled");
        this.printOnSlowListener = Poseidon.getServer().getConfig().getConfigBoolean("settings.performance-monitoring.listener-reporting.print-on-slow-listeners.enabled");
        this.printOnSlowListenerThreshold = Poseidon.getServer().getConfig().getConfigInteger("settings.performance-monitoring.listener-reporting.print-on-slow-listeners.value");

        this.listenerPerformance = Poseidon.getServer().getListenerPerformance(); // Get the listener performance map from PoseidonServer for storing listener performance statistics
        // Project Poseidon End
    }

    // Project Poseidon Start

    private final boolean listenerPerformanceEnabled; // Project Poseidon
    private final Map<String, PerformanceStatistic> listenerPerformance; // Project Poseidon

    private final boolean printOnSlowListener;
    private final int printOnSlowListenerThreshold;

    @Override
    public void registerEvents(Listener listener, Plugin plugin) {
        if (!plugin.isEnabled()) {
            throw new IllegalPluginAccessException("Plugin attempted to register " + listener + " while not enabled");
        } else {
            for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry : plugin.getPluginLoader().createRegisteredListeners(listener, plugin).entrySet()) {
                Class<? extends Event> clazz = entry.getKey();
                Event.Type type = Event.Type.getTypeByName(clazz.getSimpleName().substring(0, clazz.getSimpleName().indexOf("Event")));
                if (type != null) {
                    getEventListeners(type).addAll(entry.getValue());
                } else {
                    //If listener implements PoseidonCustomListener, we can be sure it is probably a custom event.
                    if (listener instanceof PoseidonCustomListener) {
                        server.getLogger().log(Level.INFO, plugin.getDescription().getName() + " is utilizing event handlers to receive the custom event " + clazz.getSimpleName() + ". Please be aware this is a hacky beta feature.");
                        getEventListeners(Event.Type.CUSTOM_EVENT).addAll(entry.getValue());
                    } else {
                        String cName = clazz.getName();
                        server.getLogger().log(Level.SEVERE, String.format("Class %s failed to get Event.Type on @EventHandler", cName));
                    }
                }

            }

        }
    }
    // Project Poseidon End

    /**
     * Registers the specified plugin loader
     *
     * @param loader Class name of the PluginLoader to register
     * @throws IllegalArgumentException Thrown when the given Class is not a valid PluginLoader
     */
    public void registerInterface(Class<? extends PluginLoader> loader) throws IllegalArgumentException {
        PluginLoader instance;

        if (PluginLoader.class.isAssignableFrom(loader)) {
            Constructor<? extends PluginLoader> constructor;

            try {
                constructor = loader.getConstructor(Server.class);
                instance = constructor.newInstance(server);
            } catch (NoSuchMethodException ex) {
                String className = loader.getName();

                throw new IllegalArgumentException(String.format("Class %s does not have a public %s(Server) constructor", className, className), ex);
            } catch (Exception ex) {
                throw new IllegalArgumentException(String.format("Unexpected exception %s while attempting to construct a new instance of %s", ex.getClass().getName(), loader.getName()), ex);
            }
        } else {
            throw new IllegalArgumentException(String.format("Class %s does not implement interface PluginLoader", loader.getName()));
        }

        Pattern[] patterns = instance.getPluginFileFilters();

        synchronized (this) {
            for (Pattern pattern : patterns) {
                fileAssociations.put(pattern, instance);
            }
        }
    }

    /**
     * Loads the plugins contained within the specified directory
     *
     * @param directory Directory to check for plugins
     * @return A list of all plugins loaded
     */
    public Plugin[] loadPlugins(File directory) {
        List<Plugin> result = new ArrayList<Plugin>();
        File[] files = directory.listFiles();

        boolean allFailed = false;
        boolean finalPass = false;

        LinkedList<File> filesList = new LinkedList(Arrays.asList(files));

        if (!(server.getUpdateFolder().equals(""))) {
            updateDirectory = new File(directory, server.getUpdateFolder());
        }

        while (!allFailed || finalPass) {
            allFailed = true;
            Iterator<File> itr = filesList.iterator();

            while (itr.hasNext()) {
                File file = itr.next();
                Plugin plugin = null;

                try {
                    plugin = loadPlugin(file, finalPass);
                    itr.remove();
                } catch (UnknownDependencyException ex) {
                    if (finalPass) {
                        server.getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "' in folder '" + directory.getPath() + "': " + ex.getMessage(), ex);
                        itr.remove();
                    } else {
                        plugin = null;
                    }
                } catch (InvalidPluginException ex) {
                    server.getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "' in folder '" + directory.getPath() + "': ", ex.getCause());
                    itr.remove();
                } catch (InvalidDescriptionException ex) {
                    server.getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "' in folder '" + directory.getPath() + "': " + ex.getMessage(), ex);
                    itr.remove();
                }

                if (plugin != null) {
                    result.add(plugin);
                    allFailed = false;
                    finalPass = false;
                }
            }
            if (finalPass) {
                break;
            } else if (allFailed) {
                finalPass = true;
            }
        }

        return result.toArray(new Plugin[result.size()]);
    }

    /**
     * Loads the plugin in the specified file
     * <p>
     * File must be valid according to the current enabled Plugin interfaces
     *
     * @param file File containing the plugin to load
     * @return The Plugin loaded, or null if it was invalid
     * @throws InvalidPluginException      Thrown when the specified file is not a valid plugin
     * @throws InvalidDescriptionException Thrown when the specified file contains an invalid description
     */
    public synchronized Plugin loadPlugin(File file) throws InvalidPluginException, InvalidDescriptionException, UnknownDependencyException {
        return loadPlugin(file, true);
    }

    /**
     * Loads the plugin in the specified file
     * <p>
     * File must be valid according to the current enabled Plugin interfaces
     *
     * @param file                   File containing the plugin to load
     * @param ignoreSoftDependencies Loader will ignore soft dependencies if this flag is set to true
     * @return The Plugin loaded, or null if it was invalid
     * @throws InvalidPluginException      Thrown when the specified file is not a valid plugin
     * @throws InvalidDescriptionException Thrown when the specified file contains an invalid description
     */
    public synchronized Plugin loadPlugin(File file, boolean ignoreSoftDependencies) throws InvalidPluginException, InvalidDescriptionException, UnknownDependencyException {
        File updateFile = null;

        if (updateDirectory != null && updateDirectory.isDirectory() && (updateFile = new File(updateDirectory, file.getName())).isFile()) {
            if (FileUtil.copy(updateFile, file)) {
                server.getLogger().info("An updated file for \"" + file.getName() + "\" has been found in the update folder. Replacing the original file.");
                updateFile.delete();
            }
        }

        Set<Pattern> filters = fileAssociations.keySet();
        Plugin result = null;

        for (Pattern filter : filters) {
            String name = file.getName();
            Matcher match = filter.matcher(name);

            if (match.find()) {
                PluginLoader loader = fileAssociations.get(filter);

                result = loader.loadPlugin(file, ignoreSoftDependencies);
            }
        }

        if (result != null) {
            plugins.add(result);
            lookupNames.put(result.getDescription().getName(), result);
        }

        return result;
    }

    /**
     * Checks if the given plugin is loaded and returns it when applicable
     * <p>
     * Please note that the name of the plugin is case-sensitive
     *
     * @param name Name of the plugin to check
     * @return Plugin if it exists, otherwise null
     */
    public synchronized Plugin getPlugin(String name) {
        return lookupNames.get(name);
    }

    public synchronized Plugin[] getPlugins() {
        return plugins.toArray(new Plugin[0]);
    }

    /**
     * Checks if the given plugin is enabled or not
     * <p>
     * Please note that the name of the plugin is case-sensitive.
     *
     * @param name Name of the plugin to check
     * @return true if the plugin is enabled, otherwise false
     */
    public boolean isPluginEnabled(String name) {
        Plugin plugin = getPlugin(name);

        return isPluginEnabled(plugin);
    }

    /**
     * Checks if the given plugin is enabled or not
     *
     * @param plugin Plugin to check
     * @return true if the plugin is enabled, otherwise false
     */
    public boolean isPluginEnabled(Plugin plugin) {
        if ((plugin != null) && (plugins.contains(plugin))) {
            return plugin.isEnabled();
        } else {
            return false;
        }
    }

    public void enablePlugin(final Plugin plugin) {
        if (!plugin.isEnabled()) {
            List<Command> pluginCommands = PluginCommandYamlParser.parse(plugin);

            if (!pluginCommands.isEmpty()) {
                commandMap.registerAll(plugin.getDescription().getName(), pluginCommands);

                // Project Poseidon - Start - Hide commands
                for (Command c : pluginCommands) {
                    if (c.isHidden()) {
                        Poseidon.getServer().addHiddenCommand(c.getLabel());
                        Poseidon.getServer().addHiddenCommands(c.getAliases());
                    }
                }
                // Project Poseidon - End - Hide commands
            }

            try {
                plugin.getPluginLoader().enablePlugin(plugin);
            } catch (Throwable ex) {
                server.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while enabling " + plugin.getDescription().getFullName() + " (Is it up to date?): " + ex.getMessage(), ex);
            }
        }
    }

    public void disablePlugins() {
        for (Plugin plugin : getPlugins()) {
            disablePlugin(plugin);
        }
    }

    public void disablePlugin(final Plugin plugin) {
        if (plugin.isEnabled()) {
            try {
                plugin.getPluginLoader().disablePlugin(plugin);
            } catch (Throwable ex) {
                server.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while disabling " + plugin.getDescription().getFullName() + " (Is it up to date?): " + ex.getMessage(), ex);
            }

            try {
                server.getScheduler().cancelTasks(plugin);
            } catch (Throwable ex) {
                server.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while cancelling tasks for " + plugin.getDescription().getFullName() + " (Is it up to date?): " + ex.getMessage(), ex);
            }

            try {
                server.getServicesManager().unregisterAll(plugin);
            } catch (Throwable ex) {
                server.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while unregistering services for " + plugin.getDescription().getFullName() + " (Is it up to date?): " + ex.getMessage(), ex);
            }
        }
    }

    public void clearPlugins() {
        synchronized (this) {
            disablePlugins();
            plugins.clear();
            lookupNames.clear();
            listeners.clear();
            fileAssociations.clear();
            permissions.clear();
            defaultPerms.get(true).clear();
            defaultPerms.get(false).clear();
        }
    }

    /**
     * Calls a player related event with the given details and logs execution time, calling plugin, and listener class.
     *
     * @param event Event details
     */
    public synchronized void callEvent(Event event) {
        SortedSet<RegisteredListener> eventListeners = listeners.get(event.getType());

        if (eventListeners != null) {
            for (RegisteredListener registration : eventListeners) {
                long startTime = System.currentTimeMillis();  // Start timing before event call

                try {
                    registration.callEvent(event);  // Call the event

                    // Project Poseidon - Start - Listener Performance Reporting
                    if (listenerPerformanceEnabled) {
                        long duration = System.currentTimeMillis() - startTime;  // Calculate duration in milliseconds

                        String listenerKey = registration.getListener().getClass().getName() + ":" + event.getType().toString();

                        listenerPerformance.computeIfAbsent(listenerKey, k -> new PerformanceStatistic()).update(duration);

                        // If event took longer than the threshold, print the performance statistics for the listener
                        if (printOnSlowListener && duration > printOnSlowListenerThreshold) {
                            server.getLogger().log(Level.WARNING, String.format(
                                    "[Poseidon] Event %s in %s took %d milliseconds. Statistics: %s",
                                    event.getType(),
                                    listenerKey,
                                    duration,
                                    listenerPerformance.get(listenerKey).printStats()
                            ));
                        }
                    }
                    // Project Poseidon - End - Listener Performance Reporting
                } catch (AuthorNagException ex) {
                    Plugin plugin = registration.getPlugin();

                    if (plugin.isNaggable()) {
                        plugin.setNaggable(false);

                        String author = "<NoAuthorGiven>";

                        if (plugin.getDescription().getAuthors().size() > 0) {
                            author = plugin.getDescription().getAuthors().get(0);
                        }
                        server.getLogger().log(Level.SEVERE, String.format(
                                "Nag author: '%s' of '%s' about the following: %s",
                                author,
                                plugin.getDescription().getName(),
                                ex.getMessage()
                        ));
                    }
                } catch (Throwable ex) {
                    server.getLogger().log(Level.SEVERE, "Could not pass event " + event.getType() + " to " + registration.getPlugin().getDescription().getName(), ex);
                }
            }
        }
    }


    /**
     * Registers the given event to the specified listener
     *
     * @param type     EventType to register
     * @param listener PlayerListener to register
     * @param priority Priority of this event
     * @param plugin   Plugin to register
     */
    public void registerEvent(Event.Type type, Listener listener, Priority priority, Plugin plugin) {
        if (!plugin.isEnabled()) {
            throw new IllegalPluginAccessException("Plugin attempted to register " + type + " while not enabled");
        }

        getEventListeners(type).add(new RegisteredListener(listener, priority, plugin, type));
    }

    /**
     * Registers the given event to the specified listener using a directly passed EventExecutor
     *
     * @param type     EventType to register
     * @param listener PlayerListener to register
     * @param executor EventExecutor to register
     * @param priority Priority of this event
     * @param plugin   Plugin to register
     */
    public void registerEvent(Event.Type type, Listener listener, EventExecutor executor, Priority priority, Plugin plugin) {
        if (!plugin.isEnabled()) {
            throw new IllegalPluginAccessException("Plugin attempted to register " + type + " while not enabled");
        }

        getEventListeners(type).add(new RegisteredListener(listener, executor, priority, plugin));
    }

    /**
     * Returns a SortedSet of RegisteredListener for the specified event type creating a new queue if needed
     *
     * @param type EventType to lookup
     * @return SortedSet<RegisteredListener> the looked up or create queue matching the requested type
     */
    private SortedSet<RegisteredListener> getEventListeners(Event.Type type) {
        SortedSet<RegisteredListener> eventListeners = listeners.get(type);

        if (eventListeners != null) {
            return eventListeners;
        }

        eventListeners = new TreeSet<RegisteredListener>(comparer);
        listeners.put(type, eventListeners);
        return eventListeners;
    }

    public Permission getPermission(String name) {
        return permissions.get(name.toLowerCase());
    }

    public void addPermission(Permission perm) {
        String name = perm.getName().toLowerCase();

        if (permissions.containsKey(name)) {
            throw new IllegalArgumentException("The permission " + name + " is already defined!");
        }

        permissions.put(name, perm);
        calculatePermissionDefault(perm);
    }

    public Set<Permission> getDefaultPermissions(boolean op) {
        return ImmutableSet.copyOf(defaultPerms.get(op));
    }

    public void removePermission(Permission perm) {
        removePermission(perm.getName().toLowerCase());
    }

    public void removePermission(String name) {
        permissions.remove(name);
    }

    public void recalculatePermissionDefaults(Permission perm) {
        if (permissions.containsValue(perm)) {
            defaultPerms.get(true).remove(perm);
            defaultPerms.get(false).remove(perm);

            calculatePermissionDefault(perm);
        }
    }

    private void calculatePermissionDefault(Permission perm) {
        if ((perm.getDefault() == PermissionDefault.OP) || (perm.getDefault() == PermissionDefault.TRUE)) {
            defaultPerms.get(true).add(perm);
            dirtyPermissibles(true);
        }
        if ((perm.getDefault() == PermissionDefault.NOT_OP) || (perm.getDefault() == PermissionDefault.TRUE)) {
            defaultPerms.get(false).add(perm);
            dirtyPermissibles(false);
        }
    }

    private void dirtyPermissibles(boolean op) {
        Set<Permissible> permissibles = getDefaultPermSubscriptions(op);

        for (Permissible p : permissibles) {
            p.recalculatePermissions();
        }
    }

    public void subscribeToPermission(String permission, Permissible permissible) {
        String name = permission.toLowerCase();
        Map<Permissible, Boolean> map = permSubs.get(name);

        if (map == null) {
            map = new MapMaker().weakKeys().makeMap();
            permSubs.put(name, map);
        }

        map.put(permissible, true);
    }

    public void unsubscribeFromPermission(String permission, Permissible permissible) {
        String name = permission.toLowerCase();
        Map<Permissible, Boolean> map = permSubs.get(name);

        if (map != null) {
            map.remove(permissible);

            if (map.isEmpty()) {
                permSubs.remove(name);
            }
        }
    }

    public Set<Permissible> getPermissionSubscriptions(String permission) {
        String name = permission.toLowerCase();
        Map<Permissible, Boolean> map = permSubs.get(name);

        if (map == null) {
            return ImmutableSet.of();
        } else {
            return ImmutableSet.copyOf(map.keySet());
        }
    }

    public void subscribeToDefaultPerms(boolean op, Permissible permissible) {
        Map<Permissible, Boolean> map = defSubs.get(op);

        if (map == null) {
            map = new MapMaker().weakKeys().makeMap();
            defSubs.put(op, map);
        }

        map.put(permissible, true);
    }

    public void unsubscribeFromDefaultPerms(boolean op, Permissible permissible) {
        Map<Permissible, Boolean> map = defSubs.get(op);

        if (map != null) {
            map.remove(permissible);

            if (map.isEmpty()) {
                defSubs.remove(op);
            }
        }
    }

    public Set<Permissible> getDefaultPermSubscriptions(boolean op) {
        Map<Permissible, Boolean> map = defSubs.get(op);

        if (map == null) {
            return ImmutableSet.of();
        } else {
            return ImmutableSet.copyOf(map.keySet());
        }
    }

    public Set<Permission> getPermissions() {
        return new HashSet<Permission>(permissions.values());
    }
}
