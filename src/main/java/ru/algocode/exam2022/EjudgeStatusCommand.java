package ru.algocode.exam2022;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EjudgeStatusCommand implements CommandExecutor {
    private Exam2022 plugin;

    EjudgeStatusCommand(Exam2022 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            plugin.GetStatus(player);
            return true;
        }
        return false;
    }
}

