package com.legacyminecraft.poseidon.commands.subcommands;

import com.legacyminecraft.poseidon.Poseidon;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class VersionSubCommand implements SubCommand {

    @Override
    public String getName() {
        return "version";
    }

    @Override
    public String getDescription() {
        return "Shows the Project Poseidon build information";
    }

    @Override
    public String getUsage() {
        return "/poseidon version";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        String appName = Poseidon.getServer().getAppName();
        String releaseVersion = Poseidon.getServer().getReleaseVersion();
        String buildTimestamp = Poseidon.getServer().getBuildTimestamp();
        String gitCommit = Poseidon.getServer().getGitCommit();
        String buildType = Poseidon.getServer().getBuildType();

        if (gitCommit != null && gitCommit.length() > 7) {
            gitCommit = gitCommit.substring(0, 7);
        }

        if ("Unknown".equalsIgnoreCase(releaseVersion)) {
            sender.sendMessage(ChatColor.RED + "Warning: version.properties not found. This is a local or unconfigured build.");
            return true;
        }

        sender.sendMessage(ChatColor.GRAY + "This server is running " + ChatColor.AQUA + appName + ChatColor.GRAY + ":");
        sender.sendMessage(ChatColor.GRAY + " - Version: " + ChatColor.YELLOW + releaseVersion);
        sender.sendMessage(ChatColor.GRAY + " - Built at: " + ChatColor.YELLOW + buildTimestamp);
        sender.sendMessage(ChatColor.GRAY + " - Git SHA: " + ChatColor.YELLOW + gitCommit);

        switch (buildType.toLowerCase()) {
            case "production":
                sender.sendMessage(ChatColor.GREEN + "This is a release build.");
                break;
            case "pull_request":
                sender.sendMessage(ChatColor.BLUE + "This is a pull request build.");
                break;
            default:
                sender.sendMessage(ChatColor.GRAY + "This is a development build.");
                break;
        }
        return true;
    }
}
