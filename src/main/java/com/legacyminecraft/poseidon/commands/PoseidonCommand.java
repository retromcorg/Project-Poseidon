package com.legacyminecraft.poseidon.commands;

import com.legacyminecraft.poseidon.Poseidon;
import com.projectposeidon.api.PoseidonUUID;
import com.projectposeidon.api.UUIDType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;

public class PoseidonCommand extends Command {

    private final Properties versionProperties = new Properties();

    public PoseidonCommand(String name) {
        super(name);
        this.description = "Show data regarding the server's version of Project Poseidon";
        this.usageMessage = "/poseidon";
        this.setAliases(Arrays.asList("projectposeidon"));
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (args.length == 0) {
            String appName = Poseidon.getServer().getAppName();
            String releaseVersion = Poseidon.getServer().getReleaseVersion();
            String mavenVersion = Poseidon.getServer().getMavenVersion();
            String buildTimestamp = Poseidon.getServer().getBuildTimestamp();
            String gitCommit = Poseidon.getServer().getGitCommit();
            String buildType = Poseidon.getServer().getBuildType();

            // Shorten the git commit hash to 7 characters
            if (gitCommit.length() > 7) {
                gitCommit = gitCommit.substring(0, 7);
            }

            if ("Unknown".equals(releaseVersion)) {
                sender.sendMessage(ChatColor.RED + "Warning: version.properties not found. This is a local or unconfigured build.");
            } else {
                sender.sendMessage(ChatColor.GRAY + "This server is running " + ChatColor.AQUA + appName + ChatColor.GRAY + ":");
                sender.sendMessage(ChatColor.GRAY + " - Version: " + ChatColor.YELLOW + releaseVersion);
                sender.sendMessage(ChatColor.GRAY + " - Built at: " + ChatColor.YELLOW + buildTimestamp);
                sender.sendMessage(ChatColor.GRAY + " - Git SHA: " + ChatColor.YELLOW + gitCommit);

                if ("production".equalsIgnoreCase(buildType)) {
                    sender.sendMessage(ChatColor.GREEN + "This is a release build.");
                } else if ("pull_request".equalsIgnoreCase(buildType)) {
                    sender.sendMessage(ChatColor.BLUE + "This is a pull request build.");
                } else {
                    sender.sendMessage(ChatColor.GRAY + "This is a development build.");
                }
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("uuid")) {
                sender.sendMessage(ChatColor.GRAY + "Please specify a user /poseidon uuid (username)");
            } else {
                sender.sendMessage(ChatColor.GRAY + "Unknown sub command.");
            }
        } else {
            if (!args[0].equalsIgnoreCase("uuid")) {
                sender.sendMessage(ChatColor.GRAY + "Unknown sub command.");
            } else {
                UUID uuid = PoseidonUUID.getPlayerUUIDFromCache(args[1], true);
                if (uuid == null) {
                    uuid = PoseidonUUID.getPlayerUUIDFromCache(args[1], false);
                }

                if (uuid == null) {
                    sender.sendMessage(ChatColor.GRAY + "Unable to locate the UUID of the player called: " + ChatColor.WHITE + args[1] + ChatColor.GRAY + ". Please remember usernames are cap sensitive");
                } else {

                    String latestUsername = PoseidonUUID.getPlayerUsernameFromUUID(uuid);

                    sender.sendMessage(ChatColor.GRAY + "Username: " + latestUsername);
                    sender.sendMessage(ChatColor.GRAY + "UUID: " + uuid.toString());
                    UUIDType uuidType = PoseidonUUID.getPlayerUUIDCacheStatus(args[1]);
                    if (uuidType.equals(UUIDType.ONLINE)) {
                        sender.sendMessage(ChatColor.GRAY + "UUID Type: " + ChatColor.GREEN + "Online");
                    } else if (uuidType.equals(UUIDType.OFFLINE)) {
                        sender.sendMessage(ChatColor.GRAY + "UUID Type: " + ChatColor.RED + "Offline");
                    } else {
                        sender.sendMessage(ChatColor.GRAY + "UUID Type: " + ChatColor.DARK_RED + "UNKNOWN");
                    }
                }
            }
        }

        return true;

    }

}
