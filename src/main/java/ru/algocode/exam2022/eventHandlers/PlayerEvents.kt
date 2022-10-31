package ru.algocode.exam2022.eventHandlers

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import org.bukkit.scheduler.BukkitRunnable
import ru.algocode.exam2022.plugin

class PlayerEvents : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        plugin.game.InitPlayer(player)
        plugin.borderApi.bypass(player.name)
        event.joinMessage = player.displayName + ChatColor.RESET + " присоединился!"
        player.sendMessage(ChatColor.GOLD.toString() + "Добро пожаловать на наш сервер экзамена")
        if (!player.hasPlayedBefore()) {
            val loc = plugin.spawnManager.randomSpawnLocation
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
        plugin.game.Died(killed)
        val killer = killed.killer
        if (killer != null) {
            plugin.game.Killed(killer)
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) {
            return
        }
        val player = event.player
        val item = event.item
        if (item == null || item.type != Material.BLAZE_ROD) {
            return
        }
        item.amount = item.amount - 1
        player.sendMessage(ChatColor.RED.toString() + "Данные об игроках:")
        for (online in plugin.server.onlinePlayers) {
            if (player === online || online.isOp) {
                continue
            }
            val message = online.name +
                    ": X=" + online.location.blockX +
                    ", Y=" + online.location.blockY +
                    ", Z=" + online.location.blockZ
            player.sendMessage(message)
        }
    }

    @EventHandler
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        if (event.rightClicked.type != EntityType.VILLAGER) {
            return
        }
        event.isCancelled = true
        val player = event.player
        plugin.game.OpenMerchant(player)
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        event.respawnLocation = plugin.spawnManager.randomSpawnLocation!!
    }

    @EventHandler
    fun onPlayerEditBook(event: PlayerEditBookEvent) {
        if (!event.isSigning) {
            return
        }
        event.isCancelled = true
        val player = event.player
        val title = event.newBookMeta.title ?: return
        if (title.length != 1) {
            return
        }
        val pages = event.newBookMeta.pages
        val code = pages.joinToString(separator = "\n")
        plugin.game.SubmitProblem(player, title, code)
    }
}