package org.bukkit.craftbukkit.inventory;

import net.minecraft.server.CraftingRecipe;
import net.minecraft.server.IInventory;
import net.minecraft.server.InventoryCrafting;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.Arrays;

public class CraftInventoryCrafting extends CraftInventory implements CraftingInventory {

    private IInventory resultInventory;

    public CraftInventoryCrafting(InventoryCrafting inventory, IInventory resultInventory) {
        super(inventory);
        this.resultInventory = resultInventory;
    }

    public IInventory getResultInventory() {
        return resultInventory;
    }

    public IInventory getMatrixInventory() {
        return inventory;
    }

    @Override
    public int getSize() {
        return inventory.getSize() + resultInventory.getSize();
    }

    @Override
    public void setContents(ItemStack[] items) {
        int resultLen = resultInventory.getContents().length;
        int len = inventory.getContents().length + resultLen;
        if (len != items.length) {
            throw new IllegalArgumentException("Invalid inventory size; expected " + len + " and got " + items.length);
        }
        setContents(Arrays.copyOfRange(items, 1, items.length), items[0]);
    }

    public void setContents(ItemStack[] matrix, ItemStack result) {
        setMatrix(matrix);
        setResult(result);
    }

    @Override
    public CraftItemStack[] getContents() {
        CraftItemStack[] items = new CraftItemStack[getSize()];
        net.minecraft.server.ItemStack[] mcResultItems = getResultInventory().getContents();
        int i;
        for (i = 0; i < mcResultItems.length; i++ ) {
            items[i] = new CraftItemStack(mcResultItems[i]);
        }
        net.minecraft.server.ItemStack[] mcItems = getMatrixInventory().getContents();
        for (int j = 0; j < mcItems.length; j++) {
            items[i + j] = new CraftItemStack(mcItems[j]);
        }
        return items;
    }

    @Override
    public CraftItemStack getItem(int index) {
        if (index < getResultInventory().getSize()) {
            net.minecraft.server.ItemStack item = getResultInventory().getItem(index);
            return new CraftItemStack(item);
        } else {
            net.minecraft.server.ItemStack item = getMatrixInventory().getItem(index - getResultInventory().getSize());
            return new CraftItemStack(item);
        }
    }

    @Override
    public void setItem(int index, ItemStack item) {
        if (index < getResultInventory().getSize()) {
            getResultInventory().setItem(index, (item == null ? null : new net.minecraft.server.ItemStack(item.getTypeId(), item.getAmount(), item.getDurability())));
        } else {
            getMatrixInventory().setItem((index - getResultInventory().getSize()), (item == null ? null : new net.minecraft.server.ItemStack(item.getTypeId(), item.getAmount(), item.getDurability())));
        }
    }

    public ItemStack getResult() {
        return new CraftItemStack(resultInventory.getItem(0));
    }

    public ItemStack[] getMatrix() {
        CraftItemStack[] items = new CraftItemStack[getSize()];
        net.minecraft.server.ItemStack[] matrix = inventory.getContents();
        for (int i = 0; i < matrix.length; i++ ) {
            items[i] = new CraftItemStack(matrix[i]);
        }
        return items;
    }

    public void setResult(ItemStack item) {
        net.minecraft.server.ItemStack[] contents = resultInventory.getContents();
        if (item == null || item.getTypeId() <= 0) {
            contents[0] = null;
        } else {
            contents[0] = new net.minecraft.server.ItemStack(item.getTypeId(), item.getAmount(), item.getDurability());
        }
    }

    public void setMatrix(ItemStack[] contents) {
        if (inventory.getContents().length > contents.length) {
            throw new IllegalArgumentException("Invalid inventory size; expected " + inventory.getContents().length + " or less");
        }
        net.minecraft.server.ItemStack[] items = inventory.getContents();
        for (int i = 0; i < items.length; i++) {
            if (i < contents.length) {
                ItemStack item = contents[i];
                if (item == null || item.getTypeId() <= 0) {
                    items[i] = null;
                } else {
                    items[i] = new net.minecraft.server.ItemStack(item.getTypeId(), item.getAmount(), item.getDurability());
                }
            } else {
                items[i] = null;
            }
        }
    }

    public Recipe getRecipe() {
        CraftingRecipe recipe = ((InventoryCrafting) getInventory()).currentRecipe;
        return recipe == null ? null : recipe.toBukkitRecipe();
    }
}
