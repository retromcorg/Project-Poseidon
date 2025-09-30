package com.legacyminecraft.poseidon.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WhoAmISubCommand implements SubCommand {

    @Override
    public String getName() {
        return "whoami";
    }

    @Override
    public String getDescription() {
        return "Shows information about yourself";
    }

    @Override
    public String getUsage() {
        return "/poseidon whoami";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;

        sender.sendMessage(ChatColor.AQUA + "WhoAmI:");
        sender.sendMessage(ChatColor.GRAY + " - Name: " + ChatColor.YELLOW + player.getName());
        sender.sendMessage(ChatColor.GRAY + " - UUID: " + ChatColor.YELLOW + player.getUniqueId());
        sender.sendMessage(ChatColor.GRAY + " - Display Name: " + ChatColor.YELLOW + player.getDisplayName());
        sender.sendMessage(ChatColor.GRAY + " - World: " + ChatColor.YELLOW + player.getWorld().getName());
        sender.sendMessage(ChatColor.GRAY + " - Location: " + ChatColor.YELLOW
                + player.getLocation().getBlockX() + ", "
                + player.getLocation().getBlockY() + ", "
                + player.getLocation().getBlockZ());

        return true;
    }
}
