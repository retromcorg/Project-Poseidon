package com.legacyminecraft.poseidon;

import org.bukkit.util.config.Configuration;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;

public class PoseidonConfig extends Configuration {
    private static PoseidonConfig singleton;
    private final int configVersion = 5;
    private Integer[] treeBlacklistIDs;

    public Integer[] getTreeBlacklistIDs() {
        return treeBlacklistIDs;
    }

    private PoseidonConfig() {
        super(new File("poseidon.yml"));
        this.reload();
    }

    public void reload() {
        this.load();
        this.write();
        this.validation();
        this.save();
    }

    public void resetConfig() {
        // Delete all the config options
        for (String key : this.getKeys()) {
            this.removeProperty(key);
        }
        // Reload the config
        this.write();
    }

    private void validation() {
        //Confirm settings.uuid-fetcher.method.value is either POST or GET
        if (!this.getConfigString("settings.uuid-fetcher.method.value").equalsIgnoreCase("POST") && !this.getConfigString("settings.uuid-fetcher.method.value").equalsIgnoreCase("GET")) {
            System.out.println("[Poseidon] Config: settings.uuid-fetcher.method.value is not POST or GET. Changing to POST.");
            this.setProperty("settings.uuid-fetcher.method.value", "POST");
        }
    }

    private void write() {
        if (this.getString("config-version") == null || Integer.valueOf(this.getString("config-version")) < configVersion) {
            System.out.println("[Poseidon] Converting from config version " + (this.getString("config-version") == null ? "0" : this.getString("config-version")) + " to " + configVersion);
            convertToNewConfig();
            this.setProperty("config-version", configVersion);
        }
        //Main
        generateConfigOption("config-version", configVersion);
        //Setting
//        generateConfigOption("settings.allow-graceful-uuids", true);
        generateConfigOption("settings.delete-duplicate-uuids", false);
        generateConfigOption("settings.save-playerdata-by-uuid", true);
        // Log management and rotation
        generateConfigOption("settings.per-day-log-file.info", "This setting causes the server to create a new log file each day. This is useful for log rotation and log file management.");
        generateConfigOption("settings.per-day-log-file.enabled", false);
        generateConfigOption("settings.per-day-log-file.latest-log.info", "This setting causes the server to create a latest.log similar to modern Minecraft servers. This can be useful for certain control panels and log file management.");
        generateConfigOption("settings.per-day-log-file.latest-log.enabled", true);

        //generateConfigOption("settings.fetch-uuids-from", "https://api.mojang.com/profiles/minecraft");

        // UUID Fetcher Settings
        generateConfigOption("settings.uuid-fetcher.post.info", "This setting allows you to change the URL that the server fetches UUIDs from. This is useful if you have a custom UUID server or a proxy server that fetches UUIDs.");
        generateConfigOption("settings.uuid-fetcher.post.value", "https://api.minecraftservices.com/minecraft/profile/lookup/bulk/byname");

        generateConfigOption("settings.uuid-fetcher.get.info", "This setting allows you to change the URL that the server fetches UUIDs from. This is useful if you have a custom UUID server or a proxy server that fetches UUIDs.");
        generateConfigOption("settings.uuid-fetcher.get.value", "https://api.minecraftservices.com/minecraft/profile/lookup/name/{username}");
        generateConfigOption("settings.uuid-fetcher.get.enforce-case-sensitivity.enabled", true);
        generateConfigOption("settings.uuid-fetcher.get.enforce-case-sensitivity.info", "The Mojang API is case-insensitive by default meaning usernames with different casing will return the same UUID. This setting checks the case of the username to prevent this issue. If the name is invalid, the player will be kicked.");


        generateConfigOption("settings.uuid-fetcher.method.value", "POST");
        generateConfigOption("settings.uuid-fetcher.method.info", "This setting allows for POST or GET method for fetching UUIDs. This is useful if you have a custom UUID server, Mojang API is down, or a proxy server that fetches UUIDs.");

        generateConfigOption("settings.uuid-fetcher.allow-graceful-uuids.value", true);
        generateConfigOption("settings.uuid-fetcher.allow-graceful-uuids.info", "This setting means offline UUIDs are generated for players who don't have a Mojang UUID. This is useful for cracked or semi-cracked servers.");

        generateConfigOption("settings.remove-join-leave-debug", true);
        generateConfigOption("settings.enable-tpc-nodelay", false);

        //generateConfigOption("settings.use-get-for-uuids.enabled", true);
        //generateConfigOption("settings.use-get-for-uuids.info", "This setting causes the server to use the GET method for Username to UUID conversion. This is useful incase the POST method goes offline.");

        //generateConfigOption("settings.use-get-for-uuids.case-sensitive.enabled", true);
        //generateConfigOption("settings.use-get-for-uuids.case-sensitive.info", "This setting will verify sensitivity of the username as the GET method is case-insensitive.");

        generateConfigOption("settings.faster-packets.enabled", true);
        generateConfigOption("settings.faster-packets.info", "This setting increases the speed of packets, a fix from newer Minecraft versions.");

        generateConfigOption("settings.fix-drowning-push-down.enabled", true);
        generateConfigOption("settings.fix-drowning-push-down.info", "This setting fixes taking drowning damage pushing you down.");

        generateConfigOption("settings.player-knockback-fix.enabled", true);
        generateConfigOption("settings.player-knockback-fix.info", "This setting fixes reduced knockback for certain players on the server.");

        //Watchdog
        //generateConfigOption("settings.enable-watchdog", true);
        generateConfigOption("settings.watchdog.info", "Watchdog is a automatic hang detection system which can print stacktraces and kill the server automatically after a predefined interval.");
        generateConfigOption("settings.watchdog.enable", true);
        generateConfigOption("settings.watchdog.timeout.value", 120);
        generateConfigOption("settings.watchdog.timeout.info", "The number of seconds to kill the server process after no ticks occurring.");
        generateConfigOption("settings.watchdog.debug-timeout.enabled", false);
        generateConfigOption("settings.watchdog.debug-timeout.value", 30);
        generateConfigOption("settings.watchdog.debug-timeout.info", "debug-timeout can be used to print a stack trace at a lower interval then the main timeout allowing admins to locate blocking tasks that cause hangs over a certain duration. Only enable this if you have experienced temporary hangs/server freezes.");

        // Performance Monitoring
        generateConfigOption("settings.performance-monitoring.listener-reporting.info", "This setting will cause the server to record listener execution times.");
        generateConfigOption("settings.performance-monitoring.listener-reporting.enabled", true);
        generateConfigOption("settings.performance-monitoring.listener-reporting.print-statistics-on-shutdown.info", "Prints the listener statistics to the console on server shutdown.");
        generateConfigOption("settings.performance-monitoring.listener-reporting.print-statistics-on-shutdown.enabled", false);
        generateConfigOption("settings.performance-monitoring.listener-reporting.print-on-slow-listeners.info", "Print to console when a listener takes longer than the specified time in milliseconds. It isn't recommended to set this any lower then 10ms to prevent console spam.");
        generateConfigOption("settings.performance-monitoring.listener-reporting.print-on-slow-listeners.enabled", true);
        generateConfigOption("settings.performance-monitoring.listener-reporting.print-on-slow-listeners.value", 100); // Default to two Minecraft tick

        generateConfigOption("settings.performance-monitoring.task-reporting.info", "This setting will cause the server to record synchronous task execution times.");
        generateConfigOption("settings.performance-monitoring.task-reporting.enabled", true);
        generateConfigOption("settings.performance-monitoring.task-reporting.print-statistics-on-shutdown.info", "Prints the task statistics to the console on server shutdown.");
        generateConfigOption("settings.performance-monitoring.task-reporting.print-statistics-on-shutdown.enabled", false);
        generateConfigOption("settings.performance-monitoring.task-reporting.print-on-slow-tasks.info", "Print to console when a task takes longer than the specified time in milliseconds. It isn't recommended to set this any lower then 10ms to prevent console spam.");
        generateConfigOption("settings.performance-monitoring.task-reporting.print-on-slow-tasks.enabled", true);
        generateConfigOption("settings.performance-monitoring.task-reporting.print-on-slow-tasks.value", 100); // Default to two Minecraft tick

        //Packet Events
        generateConfigOption("settings.packet-events.enabled", false);
        generateConfigOption("settings.packet-events.info", "This setting causes the server to fire a Bukkit event for each packet received and sent to a player once they have finished the initial login process. This only needs to be enabled if you have a plugin that uses this specific feature.");
//        generateConfigOption("settings.bukkit-event.disabled-plugin-unregister.value", true);
//        generateConfigOption("settings.bukkit-event.disabled-plugin-unregister.info", "This setting will automatically unregister listeners from disabled plugins. This is useful if you have a plugin that can get disabled at runtime and you want to prevent errors to the disabled plugin.");
        generateConfigOption("settings.packet-spam-detection.enabled", true);
        generateConfigOption("settings.packet-spam-detection.info", "This setting causes the server to detect and kick malicious players who send too many packets in a short period of time. This is useful to prevent players from sending too many packets to the server to cause lag.");
        generateConfigOption("settings.packet-spam-detection.threshold", 10000);

        //Statistics
        generateConfigOption("settings.statistics.key", UUID.randomUUID().toString());
        generateConfigOption("settings.statistics.enabled", true);

        //World Settings
        generateConfigOption("world-settings.optimized-explosions", false);
        generateConfigOption("world-settings.send-explosion-velocity", true);
        generateConfigOption("world-settings.randomize-spawn", true);
        generateConfigOption("world-settings.teleport-to-highest-safe-block", true);
        generateConfigOption("world-settings.use-modern-fence-bounding-boxes", false);
        //TODO: Actually implement the tree growth functionality stuff
        generateConfigOption("world.settings.block-tree-growth.enabled", true);
        generateConfigOption("world.settings.block-tree-growth.list", "54,63,68");
        generateConfigOption("world.settings.block-tree-growth.info", "This setting allows for server owners to easily block trees growing from automatically destroying certain blocks. The list must be a string with numerical item ids separated by commas.");
        generateConfigOption("world.settings.block-pistons-pushing-furnaces.info", "This workaround prevents pistons from pushing furnaces which prevents a malicious server crash.");
        generateConfigOption("world.settings.block-pistons-pushing-furnaces.enabled", true);
        generateConfigOption("world.settings.pistons.transmutation-fix.enabled", true);
        generateConfigOption("world.settings.pistons.transmutation-fix.info", "This setting fixes block transmutation exploits.");
        generateConfigOption("world.settings.pistons.sand-gravel-duping-fix.enabled", true);
        generateConfigOption("world.settings.pistons.sand-gravel-duping-fix.info", "This setting fixes sand/gravel duplication exploits.");
        generateConfigOption("world.settings.pistons.other-fixes.enabled", true);
        generateConfigOption("world.settings.pistons.other-fixes.info", "This setting fixes various other piston exploits like creating illegal pistons, breaking bedrock and duplicating redstone torches.");
        generateConfigOption("world.settings.skeleton-shooting-sound-fix.info", "This setting fixes the sound of skeletons and players shooting not playing on clients.");
        generateConfigOption("world.settings.skeleton-shooting-sound-fix.enabled", true);
        generateConfigOption("world.settings.speed-hack-check.enable", true);
        generateConfigOption("world.settings.speed-hack-check.teleport", true);
        generateConfigOption("world.settings.speed-hack-check.distance", 100.0D);
        generateConfigOption("world.settings.speed-hack-check.info", "This setting allows you to configure the automatic speedhack detection.");
        //Mob Spawner Area Limit (8 chunks)
        generateConfigOption("world.settings.mob-spawner-area-limit.enable", true);
        generateConfigOption("world.settings.mob-spawner-area-limit.limit", 150);
        generateConfigOption("world.settings.mob-spawner-area-limit.chunk-radius", 8);
        generateConfigOption("world.settings.mob-spawner-area-limit.info",
                "This setting controls the maximum number of entities of a mob spawner type that can exist within the defined chunk radius around a mob spawner. If the number of entities exceeds this limit, the spawner will stop spawning additional entities of that type. This is useful to stop the extreme lag that can be caused by mob spawners.");


        //generateConfigOption("world-settings.eject-from-vehicle-on-teleport.enabled", true);
        //generateConfigOption("world-settings.eject-from-vehicle-on-teleport.info", "Eject the player from a boat or minecart before teleporting them preventing cross world coordinate exploits.");

        //Release2Beta Settings
        generateConfigOption("settings.release2beta.enable-ip-pass-through", false);
        generateConfigOption("settings.release2beta.proxy-ip", "127.0.0.1");

        //BungeeCord
        generateConfigOption("settings.bungeecord.bungee-mode.enable", false);
        generateConfigOption("settings.bungeecord.bungee-mode.kick-message", "You must connect through BungeeCord to join this server!");
        generateConfigOption("settings.bungeecord.bungee-mode.info", "Only allows connections via BungeeCord to join. Includes optional custom kick message for players not using BungeeCord.");

        //Modded Jar Support
        generateConfigOption("settings.support.modloader.enable", false);
        generateConfigOption("settings.support.modloader.info", "EXPERIMENTAL support for ModloaderMP.");

        //Offline Username Check
        generateConfigOption("settings.check-username-validity.enabled", true);
        generateConfigOption("settings.check-username-validity.info", "If enabled, verifies the validity of a usernames of cracked players.");
        generateConfigOption("settings.check-username-validity.regex", "[a-zA-Z0-9_?]*");
        generateConfigOption("settings.check-username-validity.max-length", 16);
        generateConfigOption("settings.check-username-validity.min-length", 3);
        generateConfigOption("emergency.debug.regenerate-corrupt-chunks.enable", false);
        generateConfigOption("emergency.debug.regenerate-corrupt-chunks.info", "This setting allows you to automatically regenerate corrupt chunks. This is useful after a ungraceful shutdown while a file is being written to or out of memory exception.");

        generateConfigOption("settings.update-checker.enabled", true);
        generateConfigOption("settings.update-checker.info", "This setting allows you to disable the update checker. This is useful if you have a custom build of Poseidon or don't want to be notified of updates.");
        generateConfigOption("settings.update-checker.notify-staff.enabled", true);
        generateConfigOption("settings.update-checker.notify-staff.info", "This setting notifies operators and players with the permission poseidon.update when a new version of Poseidon is available on join.");

        //Messages
        generateConfigOption("message.kick.banned", "You are banned from this server!");
        generateConfigOption("message.kick.ip-banned", "Your IP address is banned from this server!");
        generateConfigOption("message.kick.not-whitelisted", "You are not white-listed on this server!");
        generateConfigOption("message.kick.full", "The server is full!");
        generateConfigOption("message.kick.shutdown", "\u00A7cServer is shutting down, please rejoin later.");
        generateConfigOption("message.kick.already-online", "\u00A7cA player with your username or uuid is already online, try reconnecting in a minute.");
        generateConfigOption("message.player.join", "\u00A7e%player% joined the game.");
        generateConfigOption("message.player.leave", "\u00A7e%player% left the game.");
        generateConfigOption("message.update.available", "\u00A7dA newer version of Poseidon is available: %newversion%");

        //Optional Poseidon Commands
        generateConfigOption("command.info", "This section allows you to enable or disable optional Poseidon commands. This is useful if you have a plugin that conflicts with a Poseidon command.");
        generateConfigOption("command.tps.info", "Enables the /tps command to show the server's TPS for various intervals.");
        generateConfigOption("command.tps.enabled", true);

        //UberBukkit
        generateConfigOption("fix.optimize-sponges.enabled", true);
        generateConfigOption("fix.optimize-sponges.info", "Optimizes sponges by removing unnecessary block updates. This can also prevent some block duplication methods.");

        //Tree Leave Destroy Blacklist
        if (Boolean.valueOf(String.valueOf(getConfigOption("world.settings.block-tree-growth.enabled", true)))) {
            if (String.valueOf(this.getConfigOption("world.settings.block-tree-growth.list", "")).trim().isEmpty()) {
                //Empty Blacklist
            } else {
                String[] rawBlacklist = String.valueOf(this.getConfigOption("world.settings.block-tree-growth.list", "")).trim().split(",");
                int blackListCount = 0;
                for (String stringID : rawBlacklist) {
                    if (Pattern.compile("-?[0-9]+").matcher(stringID).matches()) {
                        blackListCount = blackListCount + 1;
                    } else {
                        System.out.println("The ID " + stringID + " for leaf decay blocker has been detected as invalid, and won't be used.");
                    }
                }
                //Loop a second time to get correct array length. I know this is horrible code, but it works and only runs on server startup.
                treeBlacklistIDs = new Integer[blackListCount];
                int i = 0;
                for (String stringID : rawBlacklist) {
                    if (Pattern.compile("-?[0-9]+").matcher(stringID).matches()) {
                        treeBlacklistIDs[i] = Integer.valueOf(stringID);
                        i = i + 1;
                    }
                }
                System.out.println("Leaf blocks can't replace the following block IDs: " + Arrays.toString(treeBlacklistIDs));
            }
        } else {
            treeBlacklistIDs = new Integer[0];
        }
    }


    private void generateConfigOption(String key, Object defaultValue) {
        if (this.getProperty(key) == null) {
            this.setProperty(key, defaultValue);
        }
        final Object value = this.getProperty(key);
        this.removeProperty(key);
        this.setProperty(key, value);
    }

    //Getters Start
    public Object getConfigOption(String key) {
        return this.getProperty(key);
    }

    public Object getConfigOption(String key, Object defaultValue) {
        Object value = getConfigOption(key);
        if (value == null) {
            value = defaultValue;
        }
        return value;

    }

    public String getConfigString(String key) {
        return String.valueOf(getConfigOption(key));
    }

    public Integer getConfigInteger(String key) {
        return Integer.valueOf(getConfigString(key));
    }

    public Long getConfigLong(String key) {
        return Long.valueOf(getConfigString(key));
    }

    public Double getConfigDouble(String key) {
        return Double.valueOf(getConfigString(key));
    }

    public Boolean getConfigBoolean(String key) {
        return Boolean.valueOf(getConfigString(key));
    }

    public Boolean getConfigBoolean(String key, Boolean defaultValue) {
        Boolean value = getConfigBoolean(key);
        if (value == null) {
            System.out.println("[Poseidon] Config: " + key + " does not exist. Using default value: " + defaultValue);
            System.out.println("[Poseidon] Config: This is likely the result of an error. Please report this to the developer.");
            value = defaultValue;
        }
        return value;
    }

    //Getters End

    private void convertToNewConfig() {
        // 1- 2/3 Conversion
        convertToNewAddress("settings.statistics.enabled", "settings.enable-statistics");
        convertToNewAddress("settings.allow-graceful-uuids", "allowGracefulUUID");
        convertToNewAddress("settings.save-playerdata-by-uuid", "savePlayerdataByUUID");
        convertToNewAddress("settings.watchdog.enable", "settings.enable-watchdog");
        // 3-4 Conversion

        // Don't automatically enable the latest log file for servers that have the per-day-logfile setting enabled as this is a change in behavior
        if (this.getString("settings.per-day-logfile") != null && this.getConfigBoolean("settings.per-day-logfile")) {
            this.setProperty("settings.per-day-log-file.latest-log.enabled", false);
        }
        convertToNewAddress("settings.per-day-log-file.enabled", "settings.per-day-logfile");

        // 4-5 Conversion
        convertToNewAddress("settings.uuid-fetcher.post.value", "settings.fetch-uuids-from");
        if (this.getString("settings.uuid-fetcher.post.value", "https://api.minecraftservices.com/minecraft/profile/lookup/bulk/byname").equals("https://api.mojang.com/profiles/minecraft")) {
            System.out.println("[Poseidon] Config: settings.fetch-uuids-from is set to the default value (" + this.getString("settings.uuid-fetcher.post.value") + "). Changing to the new default value (https://api.minecraftservices.com/minecraft/profile/lookup/bulk/byname)");
            this.setProperty("settings.uuid-fetcher.post.value", "https://api.minecraftservices.com/minecraft/profile/lookup/bulk/byname");
        }

        boolean usePost = !this.getConfigBoolean("settings.use-get-for-uuids.enabled", false); // Is the server currently using POST?
        if (usePost) {
            this.setProperty("settings.uuid-fetcher.method.value", "POST");
        } else {
            this.setProperty("settings.uuid-fetcher.method.value", "GET");
        }

        removeDeprecatedConfig("settings.use-get-for-uuids.enabled", "settings.use-get-for-uuids.info");

        convertToNewAddress("settings.uuid-fetcher.allow-graceful-uuids.value", "settings.allow-graceful-uuids");
        convertToNewAddress("settings.uuid-fetcher.get.enforce-case-sensitivity.enabled", "settings.use-get-for-uuids.case-sensitive.enabled");
        removeDeprecatedConfig("settings.use-get-for-uuids.case-sensitive.info");


    }

    //Allow any number of string arguments to be passed to this method
    private boolean removeDeprecatedConfig(String... keys) {
        boolean removed = false;
        for (String key : keys) {
            if (this.getString(key) != null) {
                System.out.println("[Poseidon] Config: " + key + " is deprecated. Removing.");
                this.removeProperty(key);
                removed = true;
            }
        }
        return removed;
    }

    private boolean convertToNewAddress(String newKey, String oldKey) {
        if (this.getString(newKey) != null) {
            return false;
        }
        if (this.getString(oldKey) == null) {
            System.out.println("[Poseidon] Config: " + oldKey + " does not exist. Skipping conversion.");
            return false;
        }
        System.out.println("[Poseidon] Converting Config: " + oldKey + " to " + newKey);
        Object value = this.getProperty(oldKey);
        this.setProperty(newKey, value);
        this.removeProperty(oldKey);
        return true;

    }


    public synchronized static PoseidonConfig getInstance() {
        if (PoseidonConfig.singleton == null) {
            PoseidonConfig.singleton = new PoseidonConfig();
        }
        return PoseidonConfig.singleton;
    }

}
