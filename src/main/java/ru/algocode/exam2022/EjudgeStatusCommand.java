package ru.algocode.exam2022;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EjudgeStatusCommand implements CommandExecutor {
    private final Exam2022 plugin;

    EjudgeStatusCommand(Exam2022 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {

            plugin.getStatus(player);
            return true;
        }
        return false;
    }
}

