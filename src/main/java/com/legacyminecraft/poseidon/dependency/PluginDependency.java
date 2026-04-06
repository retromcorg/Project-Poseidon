package com.legacyminecraft.poseidon.dependency;

import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public final class PluginDependency {
    private final File file;
    private final String name;
    private final List<String> depends;

    private PluginDependency(File file, String name, List<String> depends) {
        this.file = file;
        this.name = name;
        this.depends = depends;
    }

    public static PluginDependency of(File file) throws InvalidPluginException {
        try (JarFile jar = new JarFile(file)) {
            JarEntry entry = jar.getJarEntry("plugin.yml");

            if (entry == null) {
                throw new FileNotFoundException("Jar does not contain plugin.yml");
            }

            InputStream stream = jar.getInputStream(entry);
            PluginDescriptionFile description = new PluginDescriptionFile(stream);

            stream.close();

            Object dependsRaw = description.getDepend();
            List<?> dependsList = dependsRaw instanceof List
                ? (ArrayList<?>) dependsRaw
                : Collections.emptyList();
            if (!dependsList.stream().allMatch(String.class::isInstance)) {
                throw new InvalidObjectException("Plugin 'depends' is not a list of strings");
            }

            List<String> depends = dependsList.stream()
                .map(String.class::cast)
                .collect(Collectors.toList());

            return new PluginDependency(file, description.getName(), depends);
        } catch (IOException | InvalidDescriptionException exception) {
            throw new InvalidPluginException(exception);
        }
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public List<String> getDepends() {
        return depends;
    }
}
