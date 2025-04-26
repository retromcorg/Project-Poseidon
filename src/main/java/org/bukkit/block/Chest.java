package org.bukkit.block;

import org.bukkit.inventory.InventoryHolder;

/**
 * Represents a chest.
 *
 * @author sk89q
 */
public interface Chest extends BlockState, ContainerBlock, InventoryHolder {} // Poseidon - Backport modern Inventory API
