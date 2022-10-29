package ru.algocode.exam2022.CommandExecutors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.algocode.exam2022.SpawnManager;

public class SpawnCommand implements CommandExecutor {
    private final SpawnManager spawnManager;

    public SpawnCommand(SpawnManager spawnManager) {
        this.spawnManager = spawnManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (!player.isOp()) {
                return false;
            }
            int amount = this.spawnManager.addSpawn(player.getLocation());
            player.sendMessage("Спаун добавлен! Уже спаунов: " + amount);
            return true;
        }
        return false;
    }
}
