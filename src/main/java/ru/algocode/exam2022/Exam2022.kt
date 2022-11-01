package ru.algocode.exam2022

import org.bukkit.Location
import org.bukkit.command.CommandExecutor
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.permissions.PermissionAttachment
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import ru.algocode.exam2022.commandExecutors.*
import ru.algocode.exam2022.eventHandlers.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

/**
 * Singleton object for plugin (я задолбался передавать plugin в каждую функцию).
 */
lateinit var plugin: Exam2022

class Exam2022 : JavaPlugin(), Listener {
    lateinit var game: GameState
    lateinit var spawnManager: SpawnManager
    lateinit var updatedChests: MutableSet<Location?>
    lateinit var borderApi: ChunkyBorderApi
    val perms = HashMap<UUID, PermissionAttachment>()
    private fun initConfig() {
        config.run {
            getString("spreadsheet_id")!!
            getString("config_table_id")!!
            options().copyDefaults(true)
            saveConfig()
        }
    }

    private lateinit var eventHandlers: List<Listener>
    private lateinit var commands: Map<String, CommandExecutor>

    override fun onEnable() {
        plugin = this
        initConfig()
        borderApi = ChunkyBorderApi()
        game = GameState(this)
        eventHandlers = listOf(
            PlayerEvents(),
            BlockEvents(),
            InventoryEvents(),
            EntityEvents(),
            AnvilEvents(),
        )
        for (player in server.onlinePlayers) {
            game.initPlayer(player)
        }
        object : BukkitRunnable() {
            override fun run() {
                game.tick()
            }
        }.runTaskTimer(this, 20, 20)
        spawnManager = SpawnManager(this)
        updatedChests = ConcurrentHashMap.newKeySet<Location>()
        registerEventHandlers()
        commands = mapOf(
            "addspawn" to SpawnCommand(spawnManager),
            "resetchests" to ResetChestsCommand(this),
            "syncconfig" to ReloadConfigCommand(this),
            "problemstatus" to EjudgeStatusCommand(this),
            "shuffle" to ShuffleCommand(),
        )
        registerCommands()
    }

    private fun registerCommands() {
        commands.forEach { (commandName, executor) ->
            getCommand(commandName)!!.setExecutor(executor)
        }
    }

    private fun registerEventHandlers() {
        eventHandlers.forEach { listener ->
            server.pluginManager.registerEvents(listener, this)
        }
    }

    fun resetChests() {
        updatedChests.clear()
    }

    fun reloadGameConfig() {
        game.reloadConfig()
    }

    fun getStatus(player: Player?) {
        game.GetStatus(player!!)
    }
}