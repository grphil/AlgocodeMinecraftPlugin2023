package ru.algocode.exam2022.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Random;

public class Utils {
    public static Random random;

    static {
        Utils.random = new Random();
    }

    public static String getDeltaString(int delta) {
        if (delta > 0) {
            return ChatColor.GREEN + "+" + delta + ChatColor.RESET;
        } else {
            return ChatColor.RED + String.valueOf(delta) + ChatColor.RESET;
        }
    }

    public static void sendTitle(Player player, String message) {
        player.sendTitle(message, "", 15, 25, 15);
    }
}
