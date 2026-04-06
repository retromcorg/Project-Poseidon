package com.legacyminecraft.poseidon.dependency;

import org.bukkit.plugin.InvalidPluginException;

import java.util.*;
import java.util.stream.Collectors;

public final class PluginDependencyResolver {
    public static List<PluginDependency> resolve(List<PluginDependency> items) throws InvalidPluginException {
        Map<String, PluginDependency> byName = items.stream()
            .collect(Collectors.toMap(PluginDependency::getName, i -> i));

        List<PluginDependency> result = new ArrayList<>();
        Set<String> checked = new HashSet<>();
        Set<String> checking = new HashSet<>();

        for (PluginDependency item : items) {
            check(item, byName, checked, checking, result);
        }

        return result;
    }

    private static void check(
        PluginDependency item,
        Map<String, PluginDependency> byName,
        Set<String> checked,
        Set<String> checking,
        List<PluginDependency> result
    ) throws InvalidPluginException {
        String name = item.getName();

        if (checked.contains(name)) return;
        if (!checking.add(name)) throwIllegalState("Circular dependency involving '" + name + "'.");

        for (String dependencyName : item.getDepends()) {
            PluginDependency dependency = byName.get(dependencyName);
            if (dependency == null) throwIllegalState("Missing dependency '" + dependencyName + "' for item '" + name + "'.");

            check(dependency, byName, checked, checking, result);
        }

        checking.remove(name);
        checked.add(name);
        result.add(item);
    }

    private static void throwIllegalState(String message) throws InvalidPluginException {
        throw new InvalidPluginException(new IllegalStateException(message));
    }
}
