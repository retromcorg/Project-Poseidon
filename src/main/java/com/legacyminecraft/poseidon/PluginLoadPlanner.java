package com.legacyminecraft.poseidon;

import org.bukkit.Server;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PluginLoadPlanner {
    private final Server server;
    private final Set<Pattern> fileFilters;
    private final File updateDirectory;

    public PluginLoadPlanner(Server server, Set<Pattern> fileFilters, File updateDirectory) {
        this.server = server;
        this.fileFilters = fileFilters;
        this.updateDirectory = updateDirectory;
    }

    // Generate a load order for plugins in a given directory based on dependencies.
    public List<PlannedPlugin> plan(File directory, File[] files) {
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }

        // Normalize filesystem enumeration so plugin order is not platform-dependent.
        // Issue identified by RobertWesner
        Arrays.sort(files, (left, right) -> left.getName().compareToIgnoreCase(right.getName()));

        // Index plugin metadata up front so dependency decisions can be made before any plugin code runs.
        LinkedHashMap<String, PluginCandidate> candidates = new LinkedHashMap<String, PluginCandidate>();

        // Loop through files in the directory and parse plugin descriptions, skipping duplicates and invalid plugins.
        //TODO: We should figure out how we want to handle dupe plugins in Poseidon in the future. No reason exists for them and they just cause hard to trobleshoot issues
        for (File file : files) {
            PluginCandidate candidate = createCandidate(file);
            if (candidate == null) {
                continue;
            }

            PluginCandidate existing = candidates.get(candidate.name);
            if (existing != null) {
                server.getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "' in folder '" + directory.getPath() + "': duplicate plugin name '" + candidate.name + "' also found in '" + existing.file.getPath() + "'");
                continue;
            }

            candidates.put(candidate.name, candidate);
        }

        List<PlannedPlugin> plan = new ArrayList<PlannedPlugin>();


        // Create a deterministic load order
        Set<String> loadedNames = new LinkedHashSet<String>();
        LinkedHashSet<PluginCandidate> remaining = new LinkedHashSet<PluginCandidate>(candidates.values());

        // Iterate until no plugins remain or no progress can be made due to missing or circular dependencies.
        while (!remaining.isEmpty()) {
            List<PluginCandidate> ready = new ArrayList<PluginCandidate>();

            // First try to satisfy both hard dependencies
            for (PluginCandidate candidate : remaining) {
                if (hasMissingHardDependencies(candidate, candidates)) {
                    continue;
                }

                // Prefer loading after both hard and present soft dependencies when the graph allows it.
                if (dependenciesLoaded(candidate.getHardDependencies(), loadedNames)
                        && dependenciesLoaded(candidate.getPresentSoftDependencies(candidates), loadedNames)) {
                    ready.add(candidate);
                }
            }

            // If no plugin can satisfy every soft dependency, fall back to hard-dependency order.
            boolean relaxedSoftDependencies = false;
            if (ready.isEmpty()) {
                for (PluginCandidate candidate : remaining) {
                    if (hasMissingHardDependencies(candidate, candidates)) {
                        continue;
                    }

                    // This preserves startup progress when soft dependencies form cycles or long chains.
                    if (dependenciesLoaded(candidate.getHardDependencies(), loadedNames)) {
                        ready.add(candidate);
                    }
                }
                relaxedSoftDependencies = !ready.isEmpty();
            }

            if (ready.isEmpty()) {
                // Anything left here either references a missing hard dependency or is part of a cycle.
                break;
            }

            // Sort deterministically to ensure a stable load order
            Collections.sort(ready, (left, right) -> {
                int loadOrder = left.description.getLoad().compareTo(right.description.getLoad());
                if (loadOrder != 0) {
                    return loadOrder;
                }

                int nameOrder = left.name.compareToIgnoreCase(right.name);
                if (nameOrder != 0) {
                    return nameOrder;
                }

                return left.file.getName().compareToIgnoreCase(right.file.getName());
            });

            for (PluginCandidate candidate : ready) {
                // Tell the legacy loader to ignore soft dependencies only when the planner already relaxed them.
                boolean ignoreSoftDependencies = relaxedSoftDependencies || candidate.hasMissingSoftDependencies(candidates);
                plan.add(new PlannedPlugin(candidate.file, candidate.name, ignoreSoftDependencies));
                loadedNames.add(candidate.name);
                remaining.remove(candidate);
            }
        }

        // Print errors
        for (PluginCandidate candidate : remaining) {
            // If the candidate has missing hard dependencies, report them. Otherwise, report a circular or unresolved dependency chain.
            if (hasMissingHardDependencies(candidate, candidates)) {
                for (String dependency : candidate.getMissingHardDependencies(candidates)) {
                    server.getLogger().log(Level.SEVERE, "Could not load '" + candidate.file.getPath() + "' in folder '" + directory.getPath() + "': Unknown dependency " + dependency);
                }
            } else {
                server.getLogger().log(Level.SEVERE, "Could not load '" + candidate.file.getPath() + "' in folder '" + directory.getPath() + "': circular or unresolved dependency chain");
            }
        }

        return plan;
    }

    private PluginCandidate createCandidate(File file) {
        PluginDescriptionFile description;

        try {
            description = getPluginDescription(file);
        } catch (InvalidPluginException ex) {
            server.getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "': ", ex.getCause());
            return null;
        } catch (InvalidDescriptionException ex) {
            server.getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "': " + ex.getMessage(), ex);
            return null;
        }

        if (description == null) {
            // Non-plugin files in the directory are ignored by the registered file filters.
            return null;
        }

        return new PluginCandidate(file, description);
    }

    private PluginDescriptionFile getPluginDescription(File file) throws InvalidPluginException, InvalidDescriptionException {
        // Read plugin.yml first so ordering can be computed without instantiating plugin classes.
        File descriptionSource = getEffectivePluginFile(file); // If plugin has an update, read description from the update file instead as it might have new dependencies.

        for (Pattern filter : fileFilters) {
            Matcher match = filter.matcher(descriptionSource.getName());
            if (!match.find()) {
                continue;
            }

            JarFile jar = null;
            InputStream stream = null;
            try {
                jar = new JarFile(descriptionSource);
                JarEntry entry = jar.getJarEntry("plugin.yml");

                if (entry == null) {
                    throw new InvalidPluginException(new IOException("Jar does not contain plugin.yml"));
                }

                stream = jar.getInputStream(entry);
                return new PluginDescriptionFile(stream);
            } catch (IOException ex) {
                throw new InvalidPluginException(ex);
            } catch (YAMLException ex) {
                throw new InvalidPluginException(ex);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException ignored) {
                    }
                }
                if (jar != null) {
                    try {
                        jar.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }

        return null;
    }

    private File getEffectivePluginFile(File file) {
        if (updateDirectory == null || !updateDirectory.isDirectory()) {
            return file;
        }

        File updateFile = new File(updateDirectory, file.getName());
        if (updateFile.isFile()) {
            // Return the update file instead for processing
            return updateFile;
        }

        return file;
    }

    private boolean hasMissingHardDependencies(PluginCandidate candidate, Map<String, PluginCandidate> candidates) {
        return !candidate.getMissingHardDependencies(candidates).isEmpty();
    }

    private boolean dependenciesLoaded(Collection<String> dependencies, Set<String> loadedNames) {
        for (String dependency : dependencies) {
            if (!loadedNames.contains(dependency)) {
                return false;
            }
        }

        return true;
    }

    public static final class PlannedPlugin {
        public final File file;
        public final String name;
        public final boolean ignoreSoftDependencies;

        PlannedPlugin(File file, String name, boolean ignoreSoftDependencies) {
            this.file = file;
            this.name = name;
            this.ignoreSoftDependencies = ignoreSoftDependencies;
        }
    }

    private static final class PluginCandidate {
        private final File file;
        private final PluginDescriptionFile description;
        private final String name;
        private final List<String> hardDependencies;
        private final List<String> softDependencies;

        private PluginCandidate(File file, PluginDescriptionFile description) {
            this.file = file;
            this.description = description;
            this.name = description.getName();
            this.hardDependencies = copyDependencies(description.getDepend());
            this.softDependencies = copyDependencies(description.getSoftDepend());
        }

        private List<String> getHardDependencies() {
            return hardDependencies;
        }

        private List<String> getMissingHardDependencies(Map<String, PluginCandidate> candidates) {
            List<String> missing = new ArrayList<String>();
            for (String dependency : hardDependencies) {
                if (!candidates.containsKey(dependency)) {
                    missing.add(dependency);
                }
            }
            return missing;
        }

        private List<String> getPresentSoftDependencies(Map<String, PluginCandidate> candidates) {
            List<String> present = new ArrayList<String>();
            for (String dependency : softDependencies) {
                if (candidates.containsKey(dependency)) {
                    present.add(dependency);
                }
            }
            return present;
        }

        private boolean hasMissingSoftDependencies(Map<String, PluginCandidate> candidates) {
            // Missing soft dependencies should not block load, but present ones still influence ordering.
            return getPresentSoftDependencies(candidates).size() != softDependencies.size();
        }

        @SuppressWarnings("unchecked")
        private static List<String> copyDependencies(Object dependencies) {
            if (dependencies == null) {
                return Collections.emptyList();
            }

            return new ArrayList<>((Collection<String>) dependencies);
        }
    }
}
