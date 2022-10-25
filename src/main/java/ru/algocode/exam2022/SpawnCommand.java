package ru.algocode.exam2022;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {
    private SpawnManager spawnManager;

    SpawnCommand(SpawnManager spawnManager) {
        this.spawnManager = spawnManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
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
