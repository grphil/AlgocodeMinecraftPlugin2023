package ru.algocode.exam2022

import org.bukkit.Location
import org.bukkit.command.CommandExecutor
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import ru.algocode.exam2022.EventHandlers.BlockEvents
import ru.algocode.exam2022.EventHandlers.EntityEvents
import ru.algocode.exam2022.EventHandlers.InventoryEvents
import ru.algocode.exam2022.EventHandlers.PlayerEvents

/**
 * Singleton object for plugin (я задолбался передавать plugin в каждую функцию).
 * Short for "Algocode Plugin"
 *
 * Initiated in [Exam2022.onEnable]
 */
lateinit var APlugin: Exam2022

class Exam2022 : JavaPlugin(), Listener {
    var game: GameState? = null
    var spawnManager: SpawnManager? = null
    var updatedChests: HashSet<Location?>? = null
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
        APlugin = this
        initConfig()
        game = GameState(this)
        eventHandlers = listOf<Listener>(
            PlayerEvents(),
            BlockEvents(),
            InventoryEvents(),
            EntityEvents(),
        )
        for (player in server.onlinePlayers) {
            game!!.InitPlayer(player)
        }
        object : BukkitRunnable() {
            override fun run() {
                game!!.Tick()
            }
        }.runTaskTimer(this, 20, 20)
        spawnManager = SpawnManager(this)
        updatedChests = HashSet()
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
        updatedChests!!.clear()
    }

    fun reloadGameConfig() {
        game!!.ReloadConfig()
    }

    fun getStatus(player: Player?) {
        game!!.GetStatus(player!!)
    }
}