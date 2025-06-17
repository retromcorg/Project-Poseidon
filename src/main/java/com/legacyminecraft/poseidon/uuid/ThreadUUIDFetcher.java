package com.legacyminecraft.poseidon.uuid;

import com.legacyminecraft.poseidon.PoseidonConfig;
import com.projectposeidon.johnymuffin.LoginProcessHandler;
import net.minecraft.server.Packet1Login;
import org.bukkit.ChatColor;

import java.io.IOException;
import java.util.UUID;

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
        if (useGetMethod) {
            getMethod();
        } else {
            postMethod();
        }

    }

    public void getMethod() {
        try {
            boolean caseSensitive = PoseidonConfig.getInstance().getConfigBoolean("settings.uuid-fetcher.enforce-case-sensitivity.enabled", false);
            UUID uuid = PlayerUUIDManager.fetchUUIDGet(loginPacket.name, caseSensitive);

            if (uuid != null) {
                System.out.println("[Poseidon] Fetched UUID from Mojang for " + loginPacket.name + " using GET - " + uuid);
                loginProcessHandler.userUUIDReceived(uuid, true);
            } else {
                if (PoseidonConfig.getInstance().getConfigBoolean("settings.uuid-fetcher.allow-graceful-uuids.value", true)) {
                    System.out.println("[Poseidon] " + loginPacket.name + " does not have a Mojang UUID associated with their name");
                    uuid = PlayerUUIDManager.generateOfflineUUID(loginPacket.name);
                    loginProcessHandler.userUUIDReceived(uuid, false);
                    System.out.println("[Poseidon] Using Offline Based UUID for " + loginPacket.name + " - " + uuid);
                } else {
                    System.out.println("[Poseidon] " + loginPacket.name + " does not have a UUID with Mojang. Player has been kicked as graceful UUID is disabled");
                    loginProcessHandler.cancelLoginProcess(ChatColor.RED + "Sorry, we only support premium accounts");
                }
            }
        } catch (IOException e) {
            System.out.println("[Poseidon] Failed to fetch UUID for " + loginPacket.name + " using GET method from Mojang.");
            System.out.println("[Poseidon] Mojang's API may be offline, your internet connection may be down, or something else may be wrong.");

            e.printStackTrace();
            loginProcessHandler.cancelLoginProcess(ChatColor.RED + "Sorry, we can't connect to Mojang currently, please try again later");
        }
    }

    public void postMethod() {
        try {
            boolean caseSensitive = PoseidonConfig.getInstance().getConfigBoolean("settings.uuid-fetcher.enforce-case-sensitivity.enabled", false);
            UUID uuid = PlayerUUIDManager.fetchUUIDPost(loginPacket.name, caseSensitive);

            if (uuid != null) {
                System.out.println("[Poseidon] Fetched UUID from Mojang for " + loginPacket.name + " using POST - " + uuid);
                loginProcessHandler.userUUIDReceived(uuid, true);
            } else {
                if (PoseidonConfig.getInstance().getConfigBoolean("settings.uuid-fetcher.allow-graceful-uuids.value", true)) {
                    System.out.println("[Poseidon] " + loginPacket.name + " does not have a Mojang UUID associated with their name");
                    UUID offlineUUID = PlayerUUIDManager.generateOfflineUUID(loginPacket.name);
                    loginProcessHandler.userUUIDReceived(offlineUUID, false);
                    System.out.println("[Poseidon] Using Offline Based UUID for " + loginPacket.name + " - " + offlineUUID);
                } else {
                    System.out.println("[Poseidon] " + loginPacket.name + " does not have a UUID with Mojang. Player has been kicked as graceful UUID is disabled");
                    loginProcessHandler.cancelLoginProcess(ChatColor.RED + "Sorry, we only support premium accounts");
                }
            }
        } catch (IOException e) {
            System.out.println("[Poseidon] Mojang failed contact for user " + loginPacket.name + ":");
            System.out.println("[Poseidon] If this issue persists, please utilize the GET method. Mojang's API frequently has issues with POST requests.");
            System.out.println("[Poseidon] You can do this by changing settings.uuid-fetcher.method.value to GET in the config");

            e.printStackTrace();
            loginProcessHandler.cancelLoginProcess(ChatColor.RED + "Sorry, we can't connect to Mojang currently, please try again later");
        }

    }


}


