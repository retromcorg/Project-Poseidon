# Project-Poseidon
![](/img/banner.png)
## What's Project Poseidon?
**A CraftBukkit CB1060 fork for Beta 1.7.3 fixing bugs and adding basic features.<br>**

If your looking for Project Poseidon support on pre-1.7.3 versions, please check out [Project Poseidon Uberbukkit](https://github.com/Moresteck/Project-Poseidon-Uberbukkit) developed by Moresteck which supports earlier versions.<br>

Discord: https://discord.gg/FwKg676

## Features
This is a non-exhaustive list of features that Project Poseidon includes:

- **UUID Support:** Settings to enable UUID-based inventories alongside methods for plugins to transition to UUID-based systems similar to modern Minecraft server implementations.
- **Poseidon Watchdog Implementation:** An automatic utility for detecting server hangs, ending the server process, and providing diagnostics to fix the underlying issues.
- **Packet Events:** Adds packet send and receive events for advanced plugin development without needing Spout or to use Reflections that are incompatible with modern Java versions.
- **Inventory Block Duplication Fixes:** Includes fixes for chest, furnace, inventory block duplication, and Minecart duplication issues.
- **Cross-World Duplication Fix:** Fixes a cross-world duplication glitch.
- **Duplication Glitch Fix:** Fix a bug with the Bukkit chunk cache which allowed users to duplicate and get unobtainable items.
- **Server Crash Fix:** Addresses multiple server crash issues.
- **Connection Pauses:** A powerful feature allowing developers to retrieve information asynchronously before a player connects.
- **Event Handlers:** Modern event handlers backported to facilitating easier coding, plugin backporting, and early release plugin support.
- **Vanish API Backport:** Backports the modern vanish API for easy vanish plugin creation and fixes issues with hacking players seeing vanished players.
- **Logging Enhancements:** Adds an option for daily log file creation and logs player commands to the console, with exceptions for Authme, XAuth, and similar plugins.
- **TPS API and Command:** Adds a TPS command and API for plugins to access historical TPS data.
- **Server Shutdown Improvements:** Enhances server shutdown procedures to ensure world/player data and plugin data are properly saved.
- **Release2Beta Support:** Added support for Release2Beta, including IP Forwarding.
- **Tree Growth Blockage Settings:** Allows server owners to block tree growth from replacing certain blocks to prevent griefing.
- **TCP NoDelay Option:** Improves netcode performance for compatible clients.
- **Ragequit/Ragejoin Fixes:** Prevents ragequit and ragejoin connection/chat spam.
- **Plugin Hiding:** Allows plugins to be hidden from the server list via an attribute in `plugin.yml`.
- **Improve Console Debug:** Make multiple common errors less verbose in the console.
- **Event Handling Improvements:** Ensures primed TNT fires the correct event and backports the explosion event from modern Bukkit.
- **Spawn Location Options:** Provides options to disable spawn location randomization and teleportation to the highest safe block on join.
- **Configurable Mob Spawner Area Limit:** Allows server owners to set a mob-cap for mob spawners to prevent mob farms from causing extreme lag.



## Want to use Project Poseidon on your server?
Please read the following article before changing over to Project Poseidon: https://github.com/RhysB/Project-Poseidon/wiki/Implementing-Project-Poseidon-In-Production

# Download
You can download the latest builds at the [GitHub Releases](https://github.com/retromcorg/Project-Poseidon/releases/).

Historical builds can be found on the [Glass Launcher Jenkins](https://jenkins.glass-launcher.net/job/Project-Poseidon/).

Please note, download the artifact (JAR) without original in the name, eg. `project-poseidon-1.1.8.jar`.

## Maven Repository & Plugin Development

### Maven Repository
Project Poseidon releases are published to the Legacy Minecraft Maven repository:

- **Repository:** https://repository.johnymuffin.com/
- **Browse Poseidon artifacts:**  
  https://repository.johnymuffin.com/#browse/browse:maven-releases:com%2Flegacyminecraft%2Fposeidon%2Fposeidon-craftbukkit

This repository can be used to depend on Poseidon artifacts directly in your build tooling (e.g. Maven or Gradle) when developing plugins.

### Plugin Development Examples
Examples demonstrating how to develop plugins for Poseidon using **Maven** and **GitHub Actions** can be found here:

- **Poseidon Plugin Template:**  
  https://github.com/retromcorg/Poseidon-Plugin-Template

The template includes:
- A preconfigured Maven project
- GitHub Actions CI configuration
- Correct repository and dependency setup for Poseidon plugin development
- Example code including player listeners and a config system


## Licensing
CraftBukkit and Bukkit are licensed under GNU General Public License v3.0<br>
Any future commits to this repository will remain under the same GNU General Public License v3.0<br>
Libraries in the compiled .jar files distrusted may contain their own licenses.<br>
This project contains decompiled code that is copyrighted by Mojang AB typically under the `net.minecraft.server` package.<br>

## How To Setup - IntelliJ IDEA

1. Clone this project using Git or a desktop client.
2. Open IntelliJ and create a new project in the same directory as the Project Poseidon folder.

## Compiling

Compiling is done via maven. To compile a JAR, cd into the Project Poseidon directory and run the following command:

```
mvn clean package
```

You should now have a runnable JAR inside the /target folder!

## Regarding the DMCA of CraftBukkit in 2014
The contributor Wolverness who first contributed on CraftBukkit in February 2012 issued a DMCA against CraftBukkit and other major forks of CraftBukkit.
This project is based on the following commits:

* CraftBukkit: [54bcd1c1f36691a714234e5ca2f30a20b3ad2816](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/commits/54bcd1c1f36691a714234e5ca2f30a20b3ad2816) (SpigotMC)
* Bukkit: [3524fde5ffc387ef9e39f6ee7dae83ff4dbf8229](https://github.com/Bukkit/Bukkit/commit/3524fde5ffc387ef9e39f6ee7dae83ff4dbf8229) (GitHub)

The Bukkit and CraftBukkit commits that Project Poseidon is based on are before Wolverness started contributing.

If you were a contributor before these commits please feel free to contact me or open an issue asking for this repository to be taken down.

## MC-DEV
We include files from the mc-dev GitHub repository. This code is automatically generated using minecraft_server.jar and sourced from the Bukkit repositories.
* MC-DEV: [1a792ed860ebe2c6d4c40c52f3bc7b9e0789ca23](https://github.com/Bukkit/mc-dev/commit/1a792ed860ebe2c6d4c40c52f3bc7b9e0789ca23)

If Mojang Studios or someone on their behalf wants to have this repository removed due to including copyrighted Minecraft sources like bukkit/mc-dev, please contact me or make an issue.

## How to setup ModLoaderMP support (NOT WORKING) 
Please read the following: https://github.com/RhysB/Project-Poseidon/wiki/Adding-ModLoaderMP
