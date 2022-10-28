package ru.algocode.exam2022.EventHandlers

import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import ru.algocode.exam2022.GameState
import ru.algocode.exam2022.SpawnManager

class PlayerEvents(val game: GameState?, val spawnManager: SpawnManager?, val plugin : Plugin) : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        game!!.InitPlayer(player)
        event.joinMessage = player.displayName + ChatColor.RESET + " присоединился!"
        player.sendMessage(ChatColor.GOLD.toString() + "Добро пожаловать на наш сервер экзамена")
        if (!player.hasPlayedBefore()) {
            val loc = spawnManager!!.randomSpawnLocation
            loc!!.chunk.load()
            object : BukkitRunnable() {
                override fun run() {
                    player.teleport(loc)
                }
            }.runTaskLater(plugin, 1)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        event.quitMessage = player.displayName + " вышел!"
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val killed = event.entity
        game!!.Died(killed)
        val killer = killed.killer
        if (killer != null) {
            game.Killed(killer)
        }
    }
}