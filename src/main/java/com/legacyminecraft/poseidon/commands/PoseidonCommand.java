package com.legacyminecraft.poseidon.commands;

import com.legacyminecraft.poseidon.commands.subcommands.HelpSubCommand;
import com.legacyminecraft.poseidon.commands.subcommands.SubCommand;
import com.legacyminecraft.poseidon.commands.subcommands.UptimeSubCommand;
import com.legacyminecraft.poseidon.commands.subcommands.VersionSubCommand;
import com.legacyminecraft.poseidon.commands.subcommands.WhoAmISubCommand;
import com.legacyminecraft.poseidon.commands.subcommands.UuidSubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;

public class PoseidonCommand extends Command {

    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public PoseidonCommand(String name, long startTime) {
        super(name);
        this.description = "Project Poseidon base command";
        this.usageMessage = "/poseidon [subcommand]";
        this.setAliases(Arrays.asList("projectposeidon"));

        registerSubCommand(new VersionSubCommand());
        registerSubCommand(new UuidSubCommand());
        registerSubCommand(new UptimeSubCommand(startTime));
        registerSubCommand(new WhoAmISubCommand());
        registerSubCommand(new HelpSubCommand(subCommands.values()));
    }

    private void registerSubCommand(SubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (args.length == 0) {
            return subCommands.get("version").execute(sender, args);
        }

        SubCommand subCommand = subCommands.get(args[0].toLowerCase());
        if (subCommand == null) {
            sender.sendMessage(ChatColor.RED + "Unknown sub command.");
            return true;
        }

        return subCommand.execute(sender, args);
    }
}
