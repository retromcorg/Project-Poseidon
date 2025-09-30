package com.legacyminecraft.poseidon.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Collection;

public class HelpSubCommand implements SubCommand {

    private final Collection<SubCommand> subCommands;

    public HelpSubCommand(Collection<SubCommand> subCommands) {
        this.subCommands = subCommands;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Shows all available Poseidon subcommands";
    }

    @Override
    public String getUsage() {
        return "/poseidon help";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage(ChatColor.AQUA + "Project Poseidon Commands:");
        for (SubCommand cmd : subCommands) {
            sender.sendMessage(ChatColor.YELLOW + cmd.getUsage() + ChatColor.GRAY + " - " + cmd.getDescription());
        }
        return true;
    }
}
