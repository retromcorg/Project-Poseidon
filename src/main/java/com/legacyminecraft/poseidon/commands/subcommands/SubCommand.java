package com.legacyminecraft.poseidon.commands.subcommands;

import org.bukkit.command.CommandSender;

public interface SubCommand {
    String getName();
    String getDescription();
    String getUsage();
    boolean execute(CommandSender sender, String[] args);
}
