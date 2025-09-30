package com.legacyminecraft.poseidon.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class UptimeSubCommand implements SubCommand {

    private final long startTime;

    public UptimeSubCommand(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public String getName() {
        return "uptime";
    }

    @Override
    public String getDescription() {
        return "Shows how long the server has been running";
    }

    @Override
    public String getUsage() {
        return "/poseidon uptime";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        long uptimeMillis = System.currentTimeMillis() - startTime;

        long seconds = uptimeMillis / 1000 % 60;
        long minutes = uptimeMillis / (1000 * 60) % 60;
        long hours = uptimeMillis / (1000 * 60 * 60) % 24;
        long days = uptimeMillis / (1000 * 60 * 60 * 24);

        StringBuilder uptime = new StringBuilder();
        if (days > 0) uptime.append(days).append("d ");
        if (hours > 0) uptime.append(hours).append("h ");
        if (minutes > 0) uptime.append(minutes).append("m ");
        uptime.append(seconds).append("s");

        sender.sendMessage(ChatColor.AQUA + "Server Uptime: " + ChatColor.YELLOW + uptime.toString().trim());
        return true;
    }
}
