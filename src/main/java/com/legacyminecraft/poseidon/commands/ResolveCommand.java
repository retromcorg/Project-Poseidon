package com.legacyminecraft.poseidon.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.Map;

public class ResolveCommand extends Command {

    public ResolveCommand(String name) {
        super(name);
        this.description = "Find out what plugin a command belongs to";
        this.usageMessage = "/poseidon resolve <command>";
        this.setPermission("poseidon.command.resolve");
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!testPermission(sender)) return true;

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /poseidon resolve <command>");
            return true;
        }

        String targetCommand = args[0].toLowerCase();
        if (targetCommand.startsWith("/")) {
            targetCommand = targetCommand.substring(1);
        }

        // Try to find the command in the server's command map
        Command cmd = Bukkit.getServer().getPluginCommand(targetCommand);

        if (cmd != null && cmd instanceof PluginCommand) {
            displayPluginCommand(sender, (PluginCommand) cmd, targetCommand);
            return true;
        }

        // If not found as PluginCommand, search through all known commands
        try {
            SimpleCommandMap commandMap = getCommandMap();

            if (commandMap == null) {
                sender.sendMessage(ChatColor.RED + "Unable to access command map.");
                return true;
            }

            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);

            for (Map.Entry<String, Command> entry : knownCommands.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(targetCommand) ||
                        entry.getValue().getName().equalsIgnoreCase(targetCommand)) {

                    Command foundCmd = entry.getValue();

                    if (foundCmd instanceof PluginCommand) {
                        displayPluginCommand(sender, (PluginCommand) foundCmd, targetCommand);
                        return true;
                    } else {
                        displayServerCommand(sender, foundCmd, targetCommand);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Error while searching for command: " + e.getMessage());
            return true;
        }
        sender.sendMessage(ChatColor.RED + "Command '" + targetCommand + "' not found.");
        sender.sendMessage(ChatColor.YELLOW + "Note: The command might not be registered or might be case-sensitive.");
        return true;
    }

    private void displayPluginCommand(CommandSender sender, PluginCommand pluginCmd, String targetCommand) {
        Plugin plugin = pluginCmd.getPlugin();

        sender.sendMessage(ChatColor.GREEN + "Command: " + ChatColor.WHITE + "/" + targetCommand);
        sender.sendMessage(ChatColor.GREEN + "Plugin: " + ChatColor.WHITE + plugin.getDescription().getName());
        sender.sendMessage(ChatColor.GREEN + "Author: " + ChatColor.WHITE + plugin.getDescription().getAuthors());
        sender.sendMessage(ChatColor.GREEN + "Version: " + ChatColor.WHITE + plugin.getDescription().getVersion());

        if (pluginCmd.getAliases() != null && !pluginCmd.getAliases().isEmpty()) {
            sender.sendMessage(ChatColor.GREEN + "Aliases: " + ChatColor.WHITE +
                    String.join(", ", pluginCmd.getAliases()));
        }

        if (pluginCmd.getDescription() != null && !pluginCmd.getDescription().isEmpty()) {
            sender.sendMessage(ChatColor.GREEN + "Description: " + ChatColor.WHITE +
                    pluginCmd.getDescription());
        }
    }

    private void displayServerCommand(CommandSender sender, Command cmd, String targetCommand) {
        sender.sendMessage(ChatColor.GREEN + "Command: " + ChatColor.WHITE + "/" + targetCommand);
        sender.sendMessage(ChatColor.AQUA + "Source: " + ChatColor.WHITE + "Server (Poseidon/Bukkit)");

        if (!cmd.getAliases().isEmpty()) {
            sender.sendMessage(ChatColor.GREEN + "Aliases: " + ChatColor.WHITE +
                    String.join(", ", cmd.getAliases()));
        }

        if (cmd.getDescription() != null && !cmd.getDescription().isEmpty()) {
            sender.sendMessage(ChatColor.GREEN + "Description: " + ChatColor.WHITE +
                    cmd.getDescription());
        }

        if (cmd.getPermission() != null && !cmd.getPermission().isEmpty()) {
            sender.sendMessage(ChatColor.GREEN + "Permission: " + ChatColor.WHITE +
                    cmd.getPermission());
        }
    }

    @SuppressWarnings("CatchMayIgnoreException")
    private SimpleCommandMap getCommandMap() {
        try {
            if (Bukkit.getServer() instanceof CraftServer) {
                CraftServer craftServer = (CraftServer) Bukkit.getServer();
                Field commandMapField = CraftServer.class.getDeclaredField("commandMap");
                commandMapField.setAccessible(true);
                return (SimpleCommandMap) commandMapField.get(craftServer);
            }
        } catch (Exception e) {
        }
        return null;
    }
}