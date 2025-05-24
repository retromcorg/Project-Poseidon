# Contributing to Project Poseidon 🛠️⛏️

Thank you for interest in helping maintain Project Poseidon for Minecraft Beta 1.7.3! </br>
This guide entails Project Poseidon's contribution requirements along with some helpful information.

## Beta 1.7.3 Compatibility First

### Essential Requirements
- **Package Structure**: Preserve `net.minecraft.server` hierarchy
- **Protocol Preservation**: Maintain original packet IDs (0x17 protocol)
- **World Format**: Keep chunk data compatible with Beta 1.7.3 clients
- **Decompilation Integrity**: Match original CB1060 .class files

## Contribution Workflow

1. **Create/Find Issue** on [GitHub Issues](https://github.com/retromcorg/Project-Poseidon/issues)
2. **Fork** the repository
3. **Branch** with descriptive name (e.g., `Text-Wrap-Rework`)
4. **Code**
    - Preserve decompiled method/field names
    - Follow Minimal Diff Policy (see below)
5. **Test** with Beta 1.7.3 client
6. **Create a Pull Request** with an appropriate name and description. </br>
    - Note: Please keep all language professional in your pulls.

## Code Requirements

### Style Guidelines

- Match original CraftBukkit patterns.

````
    public class Packet10Flying extends Packet {
    public double x; // Preserve original field names
    public void a(DataInputStream stream) throws IOException { 
        // Original decompiled logic
        }
    }
````

- Indentation: 4 spaces (no tabs)

- Line Endings: LF only

- Imports: Group Bukkit imports under // CraftBukkit start comments

- Comments: Use Poseidon markers for changes:
````
  // Poseidon - Short description
  // Poseidon start - Short description
  // Poseidon end
````
````
    java

    // CraftBukkit start - reason
    newLogic();
    // CraftBukkit end
````
## Miscellaneous Guidelines
- When adding imports to a Minecraft class, any Java standard imports should be below all other imports

    ```
    // CraftBukkit start
    import java.io.UnsupportedEncodingException;
    import java.util.concurrent.ExecutionException;
    import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
    import java.util.logging.Level;
    import java.util.HashSet;

    import org.bukkit.Bukkit;
    import org.bukkit.Location;
    import org.bukkit.craftbukkit.CraftWorld;
    import org.bukkit.craftbukkit.inventory.CraftInventoryView;
    import org.bukkit.craftbukkit.inventory.CraftItemStack;
    import org.bukkit.craftbukkit.util.LazyPlayerSet;
    import org.bukkit.craftbukkit.util.Waitable;
    import org.bukkit.craftbukkit.entity.CraftPlayer;
    import org.bukkit.craftbukkit.event.CraftEventFactory;
    import org.bukkit.entity.Player;
    import org.bukkit.event.Event;
    import org.bukkit.event.block.Action;
    import org.bukkit.event.block.SignChangeEvent;
    import org.bukkit.event.player.AsyncPlayerChatEvent;
    // CraftBukkit end
  
    import java.io.ByteArrayInputStream;
    import java.io.DataInputStream;
    import java.io.IOException;
    import java.util.ArrayList;
    import java.util.Iterator;
    import java.util.Random;
    import java.util.concurrent.Callable;
  ```
- Do not remove unused imports if they are not marked by CraftBukkit comments.

### Commit Message Guidelines
- Keep it **under** 50 characters, you can go into detail in your Pull Request description.
- Reference issues with Fixes #123 or Addresses #456 <sub>if applicable</sub>
- Be specific.
- Keep is professional, no offensive language.
#### Examples
**Good:**</br>
````
    [Fix] Prevent chest duplication glitch. Fixes #45
    [Feature]TextWarp improvement
    Update Readme.md with new links
````
**Bad:**</br>
````
    fixed dupe bug
    improved performance of stupid watchdog system
````
### Development Environment requirements

[JDK 8](https://www.oracle.com/emea/java/technologies/javase/javase8-archive-downloads.html)

[Maven 3.6+](https://maven.apache.org/)

An IDE of your choice,
[IntelliJ IDEA Community Edition is recommended](https://www.jetbrains.com/idea/download/other.html).

### Build & Testing
- In the terminal of your choice run:

```mvn clean package```

- This will download all dependencies from our [maven repository](https://repository.johnymuffin.com/repository/maven-public/) and build a working jar in the `target` directory
  of your project.

### Legal Requirements Code Provenance
````
    Preserve original CraftBukkit (GPL v3) headers

    Maintain decompilation comments from mc-dev

    Never commit Mojang-proprietary code directly
````
## Licensing
- All contributions must be under GPL v3. File headers must include:

```
    /*
    * This file is part of Project Poseidon.
    * See LICENSE.md for licensing information.
    */
```

## Pull Request Format

### Title
- Provide a Brief summary. Fixes #Issue <sub>(if applicable)</sub> </br>
#### Examples: </br>
````
    [Fix] GetUUIDFetcher Doesn't Honor settings.fetch-uuids-from. Fixes #72 </br>
    [Feature] Backport vanish API. Fixes #18
````
### The Issue
- Describe the problem being solved
- Changes Made
- Fixes or improvements to existing code
#### Testing Performed
- Any and all tests preformed related to code additions
- Glitch reproduction tests <sub>(if applicable)</sub>
#### Related Issues<sub>(if applicable)</sub>
- Closes #123, Addresses #456

# Minimal Diff Policy
We enforce a **Minimal Diff Policy** to preserve compatibility while making targeted improvements. This means:
- Making the **smallest necessary changes** to code
- Prioritizing more surgical fixes over large refactors
- Preserving original code structure for future updates

### Why We Use It
- **Easier Updates**: Smaller diffs simplify merging upstream changes from CraftBukkit/Mojang code in the unlikely case it is needed.
- **Beta Accuracy**: Maintains original physics, protocols, and behavior expected in Beta 1.7.3.
- **Conflict Prevention**: Reduces merge headaches.
- **Legibility**: Clearly separates Poseidon changes from CraftBukkit CB1060 code using `// ProjectPoseidon` markers.

### Examples of minimal diff examples
Short-Circuit instead of removing code:
````
    java

    if (false && allowNether && player.enteredPortal) { // CraftBukkit - disable nether
    teleportToNether();
    }
````
Comment Out rather than delete:
````
    java

    // CraftBukkit start - old logic
    // removedCode();
    // CraftBukkit end
````
Preserve Fields even if unused.


## Testing Protocol
````
    Essential Checks

        Connect with Beta 1.7.3 client to either a localhost or server

        Verify no errors have occured in console on startup or when performing actions related to your code

        Validate any and all additions to code keep original functionality outside of any intentional fixes or additions
````
## Resources

Discord: https://discord.gg/FwKg676

Using Poseidon on your server: https://github.com/RhysB/Project-Poseidon/wiki/Implementing-Project-Poseidon-In-Production

Official Maven Repository: https://repository.johnymuffin.com/repository/maven-public/

Builds: https://github.com/retromcorg/Project-Poseidon/releases/
- Note: Please note, download the jar without original in the name, eg. `project-poseidon-1.1.8.jar`.
  </br>
  </br>

# Thanks for helping us keep the Beta 1.7.3 Minecraft community alive!