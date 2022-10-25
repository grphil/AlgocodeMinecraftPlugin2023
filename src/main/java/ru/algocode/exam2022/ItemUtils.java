package ru.algocode.exam2022;

import com.google.common.collect.ImmutableList;

import java.util.*;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

class ItemUtils {
    private static final int maxPageLength = 160;

    static ItemStack CreatePotion(String name, int maxDuration, int amplifier) {
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta data = (PotionMeta) potion.getItemMeta();
        data.setColor(Color.PURPLE);
        int duration = 20 * (10 + Utils.random.nextInt(maxDuration));
        PotionEffectType type = PotionEffectType.getByName(name);
        if (type == null) {
            return null;
        }
        data.addCustomEffect(new PotionEffect(type, duration, amplifier), true);
        data.setDisplayName(ChatColor.GOLD + "Алгодопинг");
        potion.setItemMeta(data);
        return potion;
    }

    static ItemStack CreateItem(String name) {
        if (Objects.equals(name, "LOCATOR")) {
            ItemStack item = new ItemStack(Material.BLAZE_ROD);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.RED + "Локатор соперников");
            item.setItemMeta(meta);
            return item;
        } else {
            Material material = Material.getMaterial(name);
            if (material == null) {
                return null;
            }
            return new ItemStack(material);
        }
    }

    static ItemStack CreateProblemBook(
            String problemLabel,
            String statement,
            String input,
            String output,
            String sample1Input,
            String sample1Output,
            String sample2Input,
            String sample2Output
    ) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta data = (BookMeta) book.getItemMeta();
        data.setTitle(ChatColor.GREEN + "Задача " + problemLabel);
        data.setAuthor(ChatColor.RED + "grphil & peltorator");

        List<String> pages = new ArrayList<>();

        AddPages(pages, "Условие\n" + statement);
        AddPages(pages, "Входные данные\n" + input);
        AddPages(pages, "Выходные данные\n" + output);
        AddPages(pages, "Пример1. Ввод\n" + sample1Input);
        AddPages(pages, "Пример1. Вывод\n" + sample1Output);

        if (sample2Input.length() > 0) {
            AddPages(pages, "Пример2. Ввод\n" + sample2Input);
            AddPages(pages, "Пример2. Вывод\n" + sample2Output);
        }

        data.setPages(pages);
        book.setItemMeta(data);
        return book;
    }

    static void AddPages(List<String> pages, String text) {
        StringBuilder pageBuffer = new StringBuilder();
        for (String word : text.split(" ")) {
            if (pageBuffer.length() + 1 + word.length() > maxPageLength) {
                pages.add(pageBuffer.toString());
                pageBuffer = new StringBuilder();
            } else {
                pageBuffer.append(" ");
            }
            pageBuffer.append(word);
        }

        if (pageBuffer.length() > 0) {
            pages.add(pageBuffer.toString());
        }
    }
}
