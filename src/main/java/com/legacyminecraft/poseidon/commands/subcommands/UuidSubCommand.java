package com.legacyminecraft.poseidon.commands.subcommands;

import com.projectposeidon.api.PoseidonUUID;
import com.projectposeidon.api.UUIDType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class UuidSubCommand implements SubCommand {

    @Override
    public String getName() {
        return "uuid";
    }

    @Override
    public String getDescription() {
        return "Looks up a player's UUID";
    }

    @Override
    public String getUsage() {
        return "/poseidon uuid <username>";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.GRAY + "Usage: " + getUsage());
            return true;
        }

        String username = args[1];
        UUID uuid = PoseidonUUID.getPlayerUUIDFromCache(username, true);

        if (uuid == null) {
            uuid = PoseidonUUID.getPlayerUUIDFromCache(username, false);
        }

        if (uuid == null) {
            sender.sendMessage(ChatColor.GRAY + "Unable to locate the UUID of " + ChatColor.WHITE + username);
            return true;
        }

        sender.sendMessage(ChatColor.GRAY + "Username: " + username);
        sender.sendMessage(ChatColor.GRAY + "UUID: " + uuid.toString());

        UUIDType uuidType = PoseidonUUID.getPlayerUUIDCacheStatus(username);
        switch (uuidType) {
            case ONLINE:
                sender.sendMessage(ChatColor.GRAY + "UUID Type: " + ChatColor.GREEN + "Online");
                break;
            case OFFLINE:
                sender.sendMessage(ChatColor.GRAY + "UUID Type: " + ChatColor.RED + "Offline");
                break;
            default:
                sender.sendMessage(ChatColor.GRAY + "UUID Type: " + ChatColor.DARK_RED + "UNKNOWN");
                break;
        }
        return true;
    }
}
