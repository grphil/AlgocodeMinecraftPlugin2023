package ru.algocode.exam2022;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

class ShopMenu implements Listener {
    private String name;
    private int size;
    private ItemStack[] optionItems;
    private int[] prices;

    public ShopMenu(String name, int size) {
        this.name = name;
        this.size = size;
        this.optionItems = new ItemStack[this.size];
        this.prices = new int[this.size];
    }

    public void setOption(int position, ParametrizedItemStack parametrizedItem) {
        this.prices[position] = parametrizedItem.GetParam();
        ItemStack item = parametrizedItem.GenerateItem();
        ItemMeta meta = item.getItemMeta();
        meta.setLore(Collections.singletonList(ChatColor.GREEN + "Цена: " + parametrizedItem.GetParam()));
        item.setItemMeta(meta);
        this.optionItems[position] = item;
    }

    void Open(Player player) {
        Inventory inventory = Bukkit.createInventory(player, this.size, this.name);
        for (int i = 0; i < this.optionItems.length; i++) {
            if (this.optionItems[i] != null) {
                inventory.setItem(i, this.optionItems[i]);
            }
        }
        player.openInventory(inventory);
    }

    ParametrizedItemStack OnClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(name)) {
            int slot = event.getRawSlot();
            if (slot >= this.size) {
                return null;
            }
            event.setCancelled(true);
            if (slot >= 0 && slot < this.size && this.optionItems[slot] != null) {
                return new ParametrizedItemStack(this.optionItems[slot], this.prices[slot], 1);
            }
        }
        return null;
    }
}