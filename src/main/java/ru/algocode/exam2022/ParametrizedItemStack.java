package ru.algocode.exam2022;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ParametrizedItemStack {
    private ItemStack item;

    private final int param;

    private final int maxCount;

    ParametrizedItemStack(ItemStack item, int param, int maxCount) {
        this.item = item;
        this.param = param;
        this.maxCount = maxCount;
    }


    boolean Generated() {
        return Utils.random.nextInt(1000) < this.param;
    }

    int GetParam() {
        return param;
    }

    ItemStack GenerateItem() {
        if (this.item == null) {
            return null;
        }
        item = new ItemStack(this.item);
        if (maxCount > 1) {
            item.setAmount(1 + Utils.random.nextInt(this.maxCount));
        }
        return item;
    }
}
