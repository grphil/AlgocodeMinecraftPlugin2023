package ru.algocode.exam2022

import org.bukkit.Location
import org.bukkit.command.CommandExecutor
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldEvent
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import ru.algocode.exam2022.commandExecutors.EjudgeStatusCommand
import ru.algocode.exam2022.commandExecutors.ReloadConfigCommand
import ru.algocode.exam2022.commandExecutors.ResetChestsCommand
import ru.algocode.exam2022.commandExecutors.SpawnCommand
import ru.algocode.exam2022.eventHandlers.BlockEvents
import ru.algocode.exam2022.eventHandlers.EntityEvents
import ru.algocode.exam2022.eventHandlers.InventoryEvents
import ru.algocode.exam2022.eventHandlers.PlayerEvents
import java.lang.Thread.sleep
import java.util.concurrent.ConcurrentHashMap

/**
 * Singleton object for plugin (я задолбался передавать plugin в каждую функцию).
 */
lateinit var plugin: Exam2022

class Exam2022 : JavaPlugin(), Listener {
    lateinit var game: GameState
    lateinit var spawnManager: SpawnManager
    lateinit var updatedChests: MutableSet<Location?>
    lateinit var borderApi: ChunkyBorderApi
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
        )
        for (player in server.onlinePlayers) {
            game.InitPlayer(player)
        }
        object : BukkitRunnable() {
            override fun run() {
                game.Tick()
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
        game.ReloadConfig()
    }

    fun getStatus(player: Player?) {
        game.GetStatus(player!!)
    }
}