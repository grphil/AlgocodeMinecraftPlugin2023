package ru.algocode.exam2022.commandExecutors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.algocode.exam2022.Exam2022;

public class EjudgeStatusCommand implements CommandExecutor {
    private final Exam2022 plugin;

    public EjudgeStatusCommand(Exam2022 plugin) {
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

