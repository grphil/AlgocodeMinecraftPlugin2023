package ru.algocode.exam2022;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Random;

class Utils {
    static Random random;

    static {
        Utils.random = new Random();
    }

    static String getDeltaString(int delta) {
        if (delta > 0) {
            return ChatColor.GREEN + "+" + delta + ChatColor.RESET;
        } else {
            return ChatColor.RED + String.valueOf(delta) + ChatColor.RESET;
        }
    }

    static void sendTitle(Player player, String message) {
        player.sendTitle(message, "", 15, 25, 15);
    }
}
