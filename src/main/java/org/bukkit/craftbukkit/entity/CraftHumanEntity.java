
package org.bukkit.craftbukkit.entity;

import net.minecraft.server.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.Set;

//import org.bukkit.GameMode;

public class CraftHumanEntity extends CraftLivingEntity implements HumanEntity {
    private CraftInventoryPlayer inventory;
    protected final PermissibleBase perm = new PermissibleBase(this);
    private boolean op;

    public CraftHumanEntity(final CraftServer server, final EntityHuman entity) {
        super(server, entity);
        this.inventory = new CraftInventoryPlayer(entity.inventory);
    }

    public String getName() {
        return getHandle().name;
    }

    @Override
    public EntityHuman getHandle() {
        return (EntityHuman) entity;
    }

    public void setHandle(final EntityHuman entity) {
        super.setHandle((EntityHuman) entity);
        this.entity = entity;
        this.inventory = new CraftInventoryPlayer(entity.inventory);
    }

    public PlayerInventory getInventory() {
        return inventory;
    }

    // Poseidon start - Backport modern Inventory API
    public InventoryView getOpenInventory() {
        return getHandle().activeContainer.getBukkitView();
    }

    public InventoryView openInventory(Inventory inventory) {
        InventoryType type = inventory.getType();
        CraftInventory inv = (CraftInventory) inventory;

        switch (type) {
            case CHEST:
            case LARGE_CHEST:
            case CUSTOM:
                getHandle().a(inv.getInventory());
                break;
            case DISPENSER:
                getHandle().a((TileEntityDispenser) inv.getInventory());
                break;
            case FURNACE:
                getHandle().a((TileEntityFurnace) inv.getInventory());
                break;
            case WORKBENCH:
                Location loc = getLocation();
                getHandle().b(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                break;
            case PLAYER:
            case CRAFTING:
                throw new IllegalArgumentException("Can't open a " + type + " inventory!");
        }

        getHandle().activeContainer.checkReachable = false;
        return getHandle().activeContainer.getBukkitView();
    }

    public InventoryView openWorkbench(Location location, boolean force) {
        if (!force) {
            Block block = location.getBlock();
            if (block.getType() != Material.WORKBENCH) return null;
        }
        if (location == null) location = getLocation();
        getHandle().b(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        if (force) getHandle().activeContainer.checkReachable = false;

        return getHandle().activeContainer.getBukkitView();
    }

    public void openInventory(InventoryView inventory) {
        if (getHandle().activeContainer != getHandle().defaultContainer) {
            ((EntityPlayer)getHandle()).netServerHandler.a(new Packet101CloseWindow(getHandle().activeContainer.windowId));
        }

        EntityPlayer player = (EntityPlayer) getHandle();
        Container container = ((CraftInventoryView) inventory).getHandle();

        InventoryOpenEvent event = new InventoryOpenEvent(inventory);
        player.activeContainer.transferTo(container, this);
        server.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            container.transferTo(player.activeContainer, this);
            return;
        }

        player.netServerHandler.sendPacket(new Packet100OpenWindow(container.windowId, 1, "Crafting", 9));
        player.activeContainer = container;
        player.activeContainer.a((ICrafting) player);
    }

    public void closeInventory() {
        ((EntityPlayer) getHandle()).y();
    }
    // Poseidon end

    public ItemStack getItemInHand() {
        return getInventory().getItemInHand();
    }

    public void setItemInHand(ItemStack item) {
        getInventory().setItemInHand(item);
    }

    // Poseidon start - Backport modern Inventory API
    public ItemStack getItemOnCursor() {
        return new CraftItemStack(getHandle().inventory.j());
    }

    public void setItemOnCursor(ItemStack item) {
        getHandle().inventory.b(new net.minecraft.server.ItemStack(item.getTypeId(), item.getAmount(), item.getDurability()));
        ((EntityPlayer) getHandle()).z();
    }
    // Poseidon end

    @Override
    public String toString() {
        return "CraftHumanEntity{" + "id=" + getEntityId() + "name=" + getName() + '}';
    }

    public boolean isSleeping() {
        return getHandle().sleeping;
    }

    public int getSleepTicks() {
        return getHandle().sleepTicks;
    }

    public boolean isOp() {
        return op;
    }

    public boolean isPermissionSet(String name) {
        return perm.isPermissionSet(name);
    }

    public boolean isPermissionSet(Permission perm) {
        return this.perm.isPermissionSet(perm);
    }

    public boolean hasPermission(String name) {
        return perm.hasPermission(name);
    }

    public boolean hasPermission(Permission perm) {
        return this.perm.hasPermission(perm);
    }

    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        return perm.addAttachment(plugin, name, value);
    }

    public PermissionAttachment addAttachment(Plugin plugin) {
        return perm.addAttachment(plugin);
    }

    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        return perm.addAttachment(plugin, name, value, ticks);
    }

    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        return perm.addAttachment(plugin, ticks);
    }

    public void removeAttachment(PermissionAttachment attachment) {
        perm.removeAttachment(attachment);
    }

    public void recalculatePermissions() {
        perm.recalculatePermissions();
    }

    public void setOp(boolean value) {
        this.op = value;
        perm.recalculatePermissions();
    }

    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return perm.getEffectivePermissions();
    }

//    public GameMode getGameMode() {
//        return GameMode.SURVIVAL;
//    }
//
//    public void setGameMode(GameMode mode) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
}
