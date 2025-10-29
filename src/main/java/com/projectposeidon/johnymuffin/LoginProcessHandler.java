package com.projectposeidon.johnymuffin;

import com.legacyminecraft.poseidon.PoseidonConfig;
import com.legacyminecraft.poseidon.PoseidonPlugin;
import com.legacyminecraft.poseidon.uuid.ThreadUUIDFetcher;
import net.minecraft.server.NetLoginHandler;
import net.minecraft.server.Packet1Login;
import net.minecraft.server.ThreadLoginVerifier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerConnectionInitializationEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.plugin.Plugin;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public class LoginProcessHandler {

    private NetLoginHandler netLoginHandler;
    private final Packet1Login packet1Login;
    private CraftServer server;
    private boolean onlineMode;
    private boolean loginCancelled = false;
    private LoginProcessHandler loginProcessHandler;
    private boolean loginSuccessful = false;
    private long startTime;

    private HashSet<ConnectionPause> connectionPauses = new HashSet<ConnectionPause>();

    private final String msgKickAlreadyOnline;

    public LoginProcessHandler(NetLoginHandler netloginhandler, Packet1Login packet1login, CraftServer server, boolean onlineMode) {
        this.loginProcessHandler = this;
        this.netLoginHandler = netloginhandler;
        this.packet1Login = packet1login;
        this.server = server;
        this.onlineMode = onlineMode;

        this.msgKickAlreadyOnline = PoseidonConfig.getInstance().getConfigString("message.kick.already-online");

        processAuthentication();

        long connectionStartTime = System.currentTimeMillis() / 1000L;
        runLoginTimer(connectionStartTime);

    }

    private void runLoginTimer(long connectionStartTime) {
        Bukkit.getScheduler().scheduleAsyncDelayedTask(new PoseidonPlugin(), () -> {
            int currentRunningTime = (int) (System.currentTimeMillis() / 1000L - connectionStartTime);
            if (!loginSuccessful && !loginCancelled) {
                //This if statement shouldn't be needed, but this is here just in case a players login fails but the appropriate variables aren't changed
                System.out.println("[Poseidon] The login process for " + packet1Login.name + " is still running. It has been running for " + currentRunningTime + " seconds. The following plugins are still currently pausing the login process: " + getConnectionPauseNames(true));

                //Cancel the login process if it has been running for more than 20 seconds.
                if (currentRunningTime >= 20) {
                    cancelLoginProcess("Login Process Handler Timeout");
                    System.out.println("[Poseidon] LoginProcessHandler for user " + packet1Login.name + " has failed to respond after 20 seconds. And future calls to this class will result in error");
                    System.out.println("[Poseidon] Plugin Pauses: " + getConnectionPauseNames(true));
                }

                if (currentRunningTime < 60) {
                    runLoginTimer(connectionStartTime);
                }
            }
        }, 20 * 5);
    }

    private void processAuthentication() {
        PlayerConnectionInitializationEvent event = new PlayerConnectionInitializationEvent(this.packet1Login.name, this.netLoginHandler.getSocket().getInetAddress(), loginProcessHandler);
        this.server.getPluginManager().callEvent(event);
        if (loginCancelled) {
            return;
        }

        if (onlineMode) {
            //Server is running online mode
            verifyMojangSession();
        } else {
            //Server is not running online mode
            getUserUUID();
        }
    }

    private void getUserUUID() {
        //UUID uuid = UUIDManager.getInstance().getUUIDFromUsername(packet1Login.name, true);
        long unixTime = (System.currentTimeMillis() / 1000L);
        UUID uuid = UUIDManager.getInstance().getUUIDFromUsername(packet1Login.name, true, unixTime);
        if (uuid == null) {
            boolean useGetMethod = PoseidonConfig.getInstance().getString("settings.uuid-fetcher.method.value", "POST").equalsIgnoreCase("GET");
            (new ThreadUUIDFetcher(packet1Login, this, useGetMethod)).start();
        } else {
            System.out.println("[Poseidon] Fetched UUID from Cache for " + packet1Login.name + " - " + uuid.toString());
            connectPlayer(uuid);
        }


    }

    public synchronized void userUUIDReceived(UUID uuid, boolean onlineMode) {
        if (!onlineMode) {
            if (Boolean.valueOf(String.valueOf(PoseidonConfig.getInstance().getConfigOption("settings.check-username-validity.enabled", true))) && !isUsernameValid()) {
                //Username is invalid, and is a cracked user
                return;
            }
        }

        // Connect the player based on their mode
        if(onlineMode) {
            // Handle saving the UUID for online mode user here
            long unixTime = (System.currentTimeMillis() / 1000L) + 1382400;
            UUIDManager.getInstance().receivedUUID(packet1Login.name, uuid, unixTime, onlineMode);

            connectPlayerPremium(uuid);
        } else {
            // Handle saving the UUID for cracked user inside connectPlayerCracked method as it may need to be modified

            connectPlayerCracked(uuid);
        }

    }

    public void connectPlayerPremium(UUID uuid) {
        connectPlayer(uuid);
    }

    public void connectPlayerCracked(UUID uuid) {
        // Append . to the start of the username if not already present and generate offline UUID based on that

        String username = this.packet1Login.name;

        if (Boolean.valueOf(String.valueOf(PoseidonConfig.getInstance().getConfigOption("settings.check-username-validity.enabled", true))) && !isUsernameValid()) {
            //Username is invalid, and is a cracked user
            return;
        }

        boolean prefixDot = PoseidonConfig.getInstance().getConfigBoolean("settings.cracked-username-prefix.enabled", false);

        if(prefixDot) {
            if (!username.startsWith(".")) {
                System.out.println("[Poseidon] Adding . prefix to cracked user " + username + "'s username");
                username = "." + username;
            }

            // Rename the player to have the . prefix
            this.packet1Login.name = username;
            netLoginHandler.updateUsername(username);
        }

        UUID offlineUUID = UUIDManager.generateOfflineUUID(username);
        // Add new UUID entry for cracked user
        long expiresOn = (System.currentTimeMillis() / 1000L) + 1382400;
        UUIDManager.getInstance().receivedUUID(username, offlineUUID, expiresOn, false);

        connectPlayer(offlineUUID);
    }

    //This function is only run if the user is cracked
    public boolean isUsernameValid() {
        String username = this.packet1Login.name;

        // Ensure the username length is valid
        int minimumLength = Integer.valueOf(String.valueOf(PoseidonConfig.getInstance().getConfigOption("settings.check-username-validity.min-length", 3)));
        int maximumLength = Integer.valueOf(String.valueOf(PoseidonConfig.getInstance().getConfigOption("settings.check-username-validity.max-length", 16)));

        if (username.length() < minimumLength) {
            cancelLoginProcess("Sorry, your username is too short. The minimum length is: " + minimumLength);
            return false;
        }

        // Remove the . prefix for validation if it exists and the server is configured to use it.
        boolean prefixDot = PoseidonConfig.getInstance().getConfigBoolean("settings.cracked-username-prefix.enabled", false);
        boolean dotAdded = false;
        if(prefixDot) {
            if (username.startsWith(".")) {
                username = username.substring(1);
                dotAdded = true;
            }
        }

        if (username.length() > maximumLength) {
            cancelLoginProcess("Sorry, your username is too long. The maximum length is: " + (dotAdded ? (maximumLength - 1) : maximumLength)); // Adjust max length if dot was automatically added
            return false;
        }

        if (username.isEmpty()) {
            cancelLoginProcess("Sorry, you don't have a username, messing with MC?????");
            return false;
        }

        String regex = String.valueOf(PoseidonConfig.getInstance().getConfigOption("settings.check-username-validity.regex", "[a-zA-Z0-9_?]*"));

        if (!username.matches(regex)) {
            cancelLoginProcess("Sorry, your username is invalid, allowed characters: " + regex);
            return false;
        }

        return true;
    }


    private void verifyMojangSession() {
        if (!loginSuccessful & !loginCancelled) {
            (new ThreadLoginVerifier(this, netLoginHandler, this.packet1Login, this.server)).start(); // CraftBukkit
        }
    }

    public synchronized void userMojangSessionVerified() {
        if (!loginSuccessful & !loginCancelled) {
            getUserUUID();
        }
    }

    private void connectPlayer(UUID uuid) {
        String username = packet1Login.name;
        //Check if a player with the same UUID or Username is already online which is mainly an issue in Offline Mode servers.
        for (Player p : server.getOnlinePlayers()) {
            if (p.getName().equalsIgnoreCase(username) || p.getUniqueId().equals(uuid)) {
                cancelLoginProcess(this.msgKickAlreadyOnline);
                System.out.println("[Poseidon] User " + username + " has been blocked from connecting as they share a username or UUID with a user who is already online called " + p.getName() +
                        "\nMost likely the user has changed their UUID or the server is running in offline mode and someone has attempted to connect with their name");
            }
        }


        if (!loginSuccessful && !loginCancelled) {

            //Bukkit Login Event Start
            if (this.netLoginHandler.getSocket() == null) {
                return;
            }


            PlayerPreLoginEvent event = new PlayerPreLoginEvent(this.packet1Login.name, ((InetSocketAddress) netLoginHandler.networkManager.getSocketAddress()).getAddress(), loginProcessHandler);
            this.server.getPluginManager().callEvent(event);
            if (event.getResult() != PlayerPreLoginEvent.Result.ALLOWED) {
                cancelLoginProcess(event.getKickMessage());
                return;
            }
            //Bukkit Login Event End
            if (isPlayerConnectionPaused()) {
                startTime = System.currentTimeMillis() / 1000L;
            } else {
                loginSuccessful = true;
                NetLoginHandler.a(netLoginHandler, packet1Login);
            }
        }
    }

    /**
     * Cancel a players login before join or login events
     */
    public void cancelLoginProcess(String s) {
        if (!loginCancelled && !loginSuccessful) {
            loginCancelled = true;
            netLoginHandler.disconnect(s);
        }
    }

    /**
     * Set a pause for your plugin
     * Connection pauses are for fetching data for a player before they MIGHT be allowed to join
     *
     * @param plugin              Instance of plugin
     * @param connectionPauseName Name of connection pause (Ensure no duplicates)
     * @return ConnectionPause Object, used to remove a connection pause
     */
    public ConnectionPause addConnectionInterrupt(Plugin plugin, String connectionPauseName) {
        final ConnectionPause connectionPause = new ConnectionPause(plugin.getDescription().getName(), connectionPauseName, loginProcessHandler);
        connectionPauses.add(connectionPause);
        return connectionPause;
    }

    @Deprecated
    public void removeConnectionPause(ConnectionPause connectionPause) {
        removeConnectionInterrupt(connectionPause);
    }

    /**
     * Remove a connection pause
     *
     * @param connectionPause ConnectionPause object
     */
    public void removeConnectionInterrupt(ConnectionPause connectionPause) {
        //Check if the connection pause is registered and active
        if (!connectionPauses.contains(connectionPause)) {
            System.out.println("[Poseidon] A plugin has tried to remove a connection pause from the player " + packet1Login.name + " called " + connectionPause.getConnectionPauseName() +
                    " from the plugin " + connectionPause.getPluginName() + ". Please contact the plugin author and get them to check their logic as this is a duplicate remove, or a pause for another player.");
            return;
        }
        //Handle the completion of the pause
        connectionPause.setActive(false);
        //If there are no more pauses, connect the player
        if (!isPlayerConnectionPaused()) {
            long endTime = System.currentTimeMillis() / 1000L;
            int timeTaken = (int) (endTime - startTime);

            //If a pause has cancelled the login, don't connect the player
            if (loginCancelled) {
                System.out.println("[Poseidon] Player " + loginProcessHandler.packet1Login.name + " was not allowed to join after being on hold for " + timeTaken + " seconds by the following plugins: " + getConnectionPauseNames(false));
                return;
            }

            this.setLoginSuccessful(true);
            System.out.println("[Poseidon] Player " + loginProcessHandler.packet1Login.name + " has been allowed to join after being on hold for " + timeTaken + " seconds by the following plugins: " + getConnectionPauseNames(false));
            NetLoginHandler.a(netLoginHandler, packet1Login);
        }
    }


    public ConnectionPause[] getActiveConnectionPauses() {
        HashSet<ConnectionPause> activePauses = new HashSet<>();
        for (ConnectionPause connectionPause : connectionPauses) {
            if (connectionPause.isActive()) {
                activePauses.add(connectionPause);
            }
        }
        return activePauses.toArray(new ConnectionPause[activePauses.size()]);
    }

    public String getConnectionPauseNames(boolean activeOnly) {

        StringBuilder pauseNames = new StringBuilder();
        for (ConnectionPause connectionPause : connectionPauses) {
            String pluginName = connectionPause.getPluginName();
            String pauseName = connectionPause.getConnectionPauseName();
            boolean isActive = connectionPause.isActive();
            int time = connectionPause.getRunningTime();
            if (activeOnly) {
                if (connectionPause.isActive()) {
                    pauseNames.append(pluginName).append(":").append(pauseName).append(":").append(isActive ? "Running" : "Complete").append(":").append(time).append("-Seconds, ");
                }
            } else {
                pauseNames.append(pluginName).append(":").append(pauseName).append(":").append(isActive ? "Running" : "Complete").append(":").append(time).append("-Seconds, ");
            }
        }
        return pauseNames.toString();
    }


    private ConnectionPause legacyConnectionPause;

    /**
     * Set a pause for your plugin
     * Connection pauses are for fetching data for a player before they MIGHT be allowed to join
     */
    @Deprecated
    public void addConnectionPause(Plugin plugin) throws Exception {
        System.out.println("[Poseidon] " + plugin.getDescription().getName() + " is using the deprecated connection pause system which will be removed in the future. Contact the plugin author to get an updated version.");
        legacyConnectionPause = addConnectionInterrupt(plugin, "Legacy-Connection-Pause");
    }

    /**
     * Remove a pause for your plugin
     */
    @Deprecated
    public void removeConnectionPause(Plugin plugin) {
        if (legacyConnectionPause != null) {
            removeConnectionInterrupt(legacyConnectionPause);
            return;
        }
        System.out.println("[Poseidon] " + plugin.getDescription().getName() + " Attempted to remove a legacy (deprecated) connection pause that was never added. Please contact the plugin author and get them to check their logic and update to the new connection pause system.");
    }

    /**
     * See if the players connection currently paused
     */
    public boolean isPlayerConnectionPaused() {
        return getActiveConnectionPauses().length > 0;
    }


    private void setLoginSuccessful(boolean loginSuccessful) {
        this.loginSuccessful = loginSuccessful;
    }
}
