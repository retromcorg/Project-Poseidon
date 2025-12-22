package com.legacyminecraft.poseidon.uuid;

import com.legacyminecraft.poseidon.PoseidonConfig;
import com.legacyminecraft.poseidon.util.GetUUIDFetcher;
import com.legacyminecraft.poseidon.util.UUIDResult;
import com.projectposeidon.johnymuffin.LoginProcessHandler;
import net.minecraft.server.Packet1Login;
import org.bukkit.ChatColor;

import java.util.UUID;

import static com.legacyminecraft.poseidon.util.UUIDFetcher.getUUIDOf;
import static com.projectposeidon.johnymuffin.UUIDManager.generateOfflineUUID;

public class ThreadUUIDFetcher extends Thread {

    final Packet1Login loginPacket;
    //    final NetLoginHandler netLoginHandler;
    final LoginProcessHandler loginProcessHandler;
    final boolean useGetMethod;

    public ThreadUUIDFetcher(Packet1Login packet1Login, LoginProcessHandler loginProcessHandler, boolean useGetMethod) {
//        this.netLoginHandler = netloginhandler; // The login handler
        this.loginProcessHandler = loginProcessHandler;
        this.loginPacket = packet1Login; // The login packet
        this.useGetMethod = useGetMethod;

    }

    public void run() {
        if (PoseidonConfig.getInstance().getBoolean("settings.uuid-fetcher.always-use-graceful-uuids.enabled", false)) {
            System.out.println("[Poseidon] Skipping UUID fetching since always-use-graceful-uuids is enabled, " + loginPacket.name + " will use offline UUID.");
            UUID offlineUUID = generateOfflineUUID(loginPacket.name);
            loginProcessHandler.userUUIDReceived(offlineUUID, false);
            return;
        } else if (useGetMethod) {
            getMethod();
        } else {
            postMethod();
        }

    }

    public void getMethod() {
        UUIDResult uuidResult;
        GetUUIDFetcher.UUIDAndUsernameResult uuidAndUsernameResult = GetUUIDFetcher.getUUID(loginPacket.name);
        uuidResult = uuidAndUsernameResult.getUuidResult();

        if (uuidResult.getReturnType().equals(UUIDResult.ReturnType.ONLINE) && uuidAndUsernameResult.getReturnedUsername().equals(loginPacket.name)) {
            System.out.println("[Poseidon] Fetched UUID from Mojang for " + loginPacket.name + " using GET - " + uuidResult.getUuid().toString());
            loginProcessHandler.userUUIDReceived(uuidResult.getUuid(), true);
            return;
        } else if (uuidResult.getReturnType().equals(UUIDResult.ReturnType.ONLINE)) {
            if(PoseidonConfig.getInstance().getConfigBoolean("settings.uuid-fetcher.get.enforce-case-sensitivity.enabled")) {
                System.out.println("[Poseidon] Fetched UUID from Mojang for " + loginPacket.name + " using GET - " + uuidResult.getUuid().toString() + " however, the username returned was " + uuidAndUsernameResult.getReturnedUsername() + ". The user has been kicked as the server is configured to use case sensitive usernames");
                loginProcessHandler.cancelLoginProcess(ChatColor.RED + "Sorry, that username has invalid casing");
                return;
            } else {
                System.out.println("[Poseidon] Fetched UUID from Mojang for " + loginPacket.name + " - " + uuidResult.getUuid().toString());
                loginProcessHandler.userUUIDReceived(uuidResult.getUuid(), true);
                return;
            }
        } else if (uuidResult.getReturnType().equals(UUIDResult.ReturnType.OFFLINE)) {
            if ((boolean) PoseidonConfig.getInstance().getProperty("settings.uuid-fetcher.allow-graceful-uuids.value")) {
                System.out.println("[Poseidon] " + loginPacket.name + " does not have a Mojang UUID associated with their name");
                UUID offlineUUID = uuidResult.getUuid();
                loginProcessHandler.userUUIDReceived(offlineUUID, false);
                System.out.println("[Poseidon] Using Offline Based UUID for " + loginPacket.name + " - " + offlineUUID);
            } else {
                System.out.println("[Poseidon] " + loginPacket.name + " does not have a UUID with Mojang. Player has been kicked as graceful UUID is disabled");
                loginProcessHandler.cancelLoginProcess(ChatColor.RED + "Sorry, we only support premium accounts");
            }
            return;
        }
        System.out.println("[Poseidon] Failed to fetch UUID for " + loginPacket.name + " using GET method from Mojang.");
        System.out.println("[Poseidon] Mojang's API may be offline, your internet connection may be down, or something else may be wrong.");

        uuidResult.getException().printStackTrace();
        loginProcessHandler.cancelLoginProcess(ChatColor.RED + "Sorry, we can't connect to Mojang currently, please try again later");

    }


    public void postMethod() {
        UUID uuid;
        try {
            uuid = getUUIDOf(loginPacket.name);
            if (uuid == null) {
                if (PoseidonConfig.getInstance().getConfigBoolean("settings.uuid-fetcher.allow-graceful-uuids.value", true)) {
                    System.out.println("[Poseidon] " + loginPacket.name + " does not have a Mojang UUID associated with their name");
                    UUID offlineUUID = generateOfflineUUID(loginPacket.name);
                    loginProcessHandler.userUUIDReceived(offlineUUID, false);
                    System.out.println("[Poseidon] Using Offline Based UUID for " + loginPacket.name + " - " + offlineUUID);
                } else {
                    System.out.println("[Poseidon] " + loginPacket.name + " does not have a UUID with Mojang. Player has been kicked as graceful UUID is disabled");
                    loginProcessHandler.cancelLoginProcess(ChatColor.RED + "Sorry, we only support premium accounts");
                }
            } else {
                System.out.println("[Poseidon] Fetched UUID from Mojang for " + loginPacket.name + " using POST - " + uuid.toString());
                loginProcessHandler.userUUIDReceived(uuid, true);
            }
        } catch (Exception e) {
            System.out.println("[Poseidon] Mojang failed contact for user " + loginPacket.name + ":");

            System.out.println("[Poseidon] If this issue persists, please utilize the GET method. Mojang's API frequently has issues with POST requests.");
            System.out.println("[Poseidon] You can do this by changing settings.uuid-fetcher.method.value to GET in the config");

            e.printStackTrace();
            loginProcessHandler.cancelLoginProcess(ChatColor.RED + "Sorry, we can't connect to Mojang currently, please try again later");
        }

    }


}


