package com.legacyminecraft.poseidon.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public class PlayerInvCommand extends Command {

    public PlayerInvCommand(String name) {
        super(name);
        this.description = "Manage Player Inventory";
        this.usageMessage = "/pinv <Player Name>";
        this.setPermission("poseidon.command.op.pinv");
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!testPermission(sender)) return true;

        Player p = sender.getServer().getPlayerExact(sender.getName());

        PlayerInventory inventory = null;
        if (args.length >= 1) {
            Player target = sender.getServer().getPlayerExact(args[0]);
            inventory = target.getInventory();
        } else  {
            sender.sendMessage(ChatColor.RED + "Warning: Please specify a player to open inventory");
            return false;
        }
        if (inventory != null) {
            p.openContainer(((CraftInventory) inventory).getInventory());
        }

        return true;
    }
}
