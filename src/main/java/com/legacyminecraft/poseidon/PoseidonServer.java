package com.legacyminecraft.poseidon;

import com.legacyminecraft.poseidon.utility.PerformanceStatistic;
import com.legacyminecraft.poseidon.utility.PoseidonVersionChecker;
import com.legacyminecraft.poseidon.watchdog.WatchDogThread;
import com.projectposeidon.johnymuffin.UUIDManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetServerHandler;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public final class PoseidonServer {
    private final MinecraftServer server;
    private final CraftServer craftServer;

    private final List<String> hiddenCommands = new ArrayList<>();
    private final Properties versionProperties = new Properties();

    private boolean serverInitialized = false;

    private PoseidonVersionChecker poseidonVersionChecker;
    private WatchDogThread watchDogThread;

    private Map<String, PerformanceStatistic> listenerPerformance = new HashMap<String, PerformanceStatistic>();

    private Map<String, PerformanceStatistic> taskPerformance = new HashMap<String, PerformanceStatistic>();

    private PoseidonConfig config;

    public PoseidonServer(MinecraftServer server, CraftServer craftServer) {
        this.server = server;
        this.craftServer = craftServer;
        this.config = PoseidonConfig.getInstance();

        loadVersionProperties();

        addHiddenCommands(Arrays.asList("login", "l", "register", "reg", "unregister", "changepassword", "changepw"));
    }

    private void loadVersionProperties() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("version.properties")) {
            if (inputStream != null) {
                versionProperties.load(inputStream);
            }
        } catch (IOException e) {
            getLogger().warning("Failed to load version.properties: " + e.getMessage());
        }
    }

    public void initializeServer() {
        if (serverInitialized) {
            throw new UnsupportedOperationException("Server already initialized");
        }

        getLogger().info("[Poseidon] Starting Project Poseidon Modules!");

        PoseidonConfig.getInstance();
        UUIDManager.getInstance();

        initializeUpdateChecker();

        //Start Watchdog
        watchDogThread = new WatchDogThread(Thread.currentThread());
        if (PoseidonConfig.getInstance().getBoolean("settings.enable-watchdog", true)) {
            getLogger().info("[Poseidon] Starting Watchdog to detect any server hangs!");
            watchDogThread.start();
            watchDogThread.tickUpdate();
        }

        serverInitialized = true;
        getLogger().info("[Poseidon] Finished loading Project Poseidon Modules!");
    }

    private void initializeUpdateChecker() {
        if (!PoseidonConfig.getInstance().getConfigBoolean("settings.update-checker.enabled", true)) {
            getLogger().info("[Poseidon] Version checker disabled. The server will not check for updates.");
            return;
        }

        String releaseVersion = getReleaseVersion();

        if (releaseVersion == null) {
            getLogger().warning("[Poseidon] Version checker is disabled as no version.properties file was found.");
            return;
        }

        if (!getBuildType().equalsIgnoreCase("production")) {
            getLogger().warning("[Poseidon] Version checker is disabled as this is a " + getBuildType() + " build. The updater will only check for updates on production builds.");
            return;
        }

        poseidonVersionChecker = new PoseidonVersionChecker(craftServer, releaseVersion);

        getLogger().info("[Poseidon] Version checker enabled. The server will check for updates every hour.");
        // Run the version checker in a separate thread every hour
        Bukkit.getScheduler().scheduleSyncRepeatingTask(new PoseidonPlugin(), new Runnable() {
            @Override
            public void run() {
                poseidonVersionChecker.fetchLatestVersion();
            }
        }, 0, 20 * 60 * 60);
    }

    public void shutdownServer() {
        if (!serverInitialized) {
//            throw new UnsupportedOperationException("Server not initialized");
            return;
        }
      
        getLogger().info("[Poseidon] Stopping Project Poseidon Modules!");

        UUIDManager.getInstance().saveJsonArray();

        if (watchDogThread != null) {
            getLogger().info("[Poseidon] Stopping Watchdog!");
            watchDogThread.interrupt();
        }

        serverInitialized = false;
        getLogger().info("[Poseidon] Finished unloading Project Poseidon Modules!");
    }

    public Logger getLogger() {
        return MinecraftServer.log;
    }

    public String getAppName() {
        return versionProperties.getProperty("app_name", "Unknown");
    }

    public String getReleaseVersion() {
        return versionProperties.getProperty("release_version", "Unknown");
    }

    public String getMavenVersion() {
        return versionProperties.getProperty("maven_version", "Unknown");
    }

    public String getBuildTimestamp() {
        return versionProperties.getProperty("build_timestamp", "Unknown");
    }

    public String getGitCommit() {
        return versionProperties.getProperty("git_commit", "Unknown");
    }

    public String getBuildType() {
        return versionProperties.getProperty("build_type", "Unknown");
    }

    public boolean isUpdateAvailable() {
        return poseidonVersionChecker != null && poseidonVersionChecker.isUpdateAvailable();
    }

    public String getNewestVersion() {
        return poseidonVersionChecker == null ? "Unknown" : poseidonVersionChecker.getLatestVersion();
    }

    public WatchDogThread getWatchDogThread() {
        return watchDogThread;
    }

    /**
     * Returns the current hide state of the command from param (Hide from console)
     *
     * @param cmdName Command name
     * @return True if the command from param is hidden and false otherwise
     */
    public boolean isCommandHidden(String cmdName) {
        return hiddenCommands.contains(cmdName.toLowerCase());
    }

    /**
     * Hides the command from param from being logged to server console
     *
     * @param cmd Command name
     */
    public void addHiddenCommand(String cmd) {
        cmd = cmd.toLowerCase();

        if (hiddenCommands.contains(cmd)) {
            Logger.getLogger(NetServerHandler.class.getName()).warning("List of Hidden commands already contains " + cmd);
            return;
        }

        hiddenCommands.add(cmd);
    }

    /**
     * Hides the commands from param from being logged to server console
     *
     * @param commands List of command names
     */
    public void addHiddenCommands(List<String> commands) {
        for (String cmd : commands) {
            addHiddenCommand(cmd);
        }
    }

    public Map<String, PerformanceStatistic> getListenerPerformance() {
        return listenerPerformance;
    }

    public Map<String, PerformanceStatistic> getTaskPerformance() {
        return taskPerformance;
    }

    // Generic method to sort any performance map
    public Map<String, PerformanceStatistic> getSortedPerformance(Map<String, PerformanceStatistic> unsortedMap) {
        return unsortedMap.entrySet()
                .stream()
                .sorted(Map.Entry.<String, PerformanceStatistic>comparingByValue(
                        Comparator.comparingLong(PerformanceStatistic::getAverageExecutionTime).reversed()
                ))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, // if same key, keep the first
                        LinkedHashMap::new
                ));
    }

    // Specific method to get sorted listener performance
    public Map<String, PerformanceStatistic> getSortedListenerPerformance() {
        return getSortedPerformance(getListenerPerformance());
    }

    // Specific method to get sorted task performance
    public Map<String, PerformanceStatistic> getSortedTaskPerformance() {
        return getSortedPerformance(getTaskPerformance());
    }

    public PoseidonConfig getConfig() {
        return config;
    }
}
