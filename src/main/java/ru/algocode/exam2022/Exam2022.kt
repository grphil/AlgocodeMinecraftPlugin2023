package ru.algocode.exam2022

import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.*
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import ru.algocode.exam2022.EventHandlers.PlayerEvents

class Exam2022 : JavaPlugin(), Listener {
    private var game: GameState? = null
    private var spawnManager: SpawnManager? = null
    private var updatedChests: HashSet<Location?>? = null
    private fun initConfig() {
        config
        config.getString("spreadsheet_id")!!
        config.getString("config_table_id")!!
        config.options().copyDefaults(true)
        saveConfig()
    }

    private lateinit var eventHandlers: List<Listener>

    override fun onEnable() {
        initConfig()
        game = GameState(this)
        eventHandlers = listOf<Listener>(
            PlayerEvents(game, spawnManager, this),
            this,
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
        getCommand("addspawn")!!.setExecutor(SpawnCommand(spawnManager))
        getCommand("resetchests")!!.setExecutor(ResetChestsCommand(this))
        getCommand("syncconfig")!!.setExecutor(ReloadConfigCommand(this))
        getCommand("problemstatus")!!.setExecutor(EjudgeStatusCommand(this))
    }

    private fun registerEventHandlers() {
        eventHandlers.forEach { listener ->
            server.pluginManager.registerEvents(listener, this)
        }
    }



    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val block = event.blockPlaced
        val player = event.player
        if (block.type == Material.CHEST && player.isOp) {
            val chestInventory = (block.state as Chest).blockInventory
            game!!.FillChest(chestInventory)
        }
        updatedChests!!.add(block.location)
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
        for (online in server.onlinePlayers) {
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
    fun onInventoryOpenEvent(e: InventoryOpenEvent) {
        if (e.inventory.holder is Chest) {
            val loc = e.inventory.location
            if (updatedChests!!.contains(loc)) {
                return
            }
            updatedChests!!.add(loc)
            e.inventory.clear()
            game!!.FillChest(e.inventory)
        }
    }

    @EventHandler
    fun onEntitySpawn(event: EntitySpawnEvent) {
        if (event.entityType == EntityType.VILLAGER) {
            val villager = event.entity as Villager
            villager.isInvulnerable = true
            villager.setAI(false)
            villager.customName = ChatColor.GREEN.toString() + "Торговец"
            villager.isCustomNameVisible = true
        }
    }

    @EventHandler
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        if (event.rightClicked.type != EntityType.VILLAGER) {
            return
        }
        event.isCancelled = true
        val player = event.player
        game!!.OpenMerchant(player)
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        event.respawnLocation = spawnManager!!.randomSpawnLocation!!
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onInventoryClick(event: InventoryClickEvent?) {
        game!!.BuyMerchant(event!!)
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
        game!!.SubmitProblem(player, title, code)
    }

    fun ResetChests() {
        updatedChests!!.clear()
    }

    fun ReloadConfig() {
        game!!.ReloadConfig()
    }

    fun GetStatus(player: Player?) {
        game!!.GetStatus(player!!)
    }
}