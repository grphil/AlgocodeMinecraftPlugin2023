package ru.algocode.exam2022

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import ru.algocode.ejudge.EjudgeSession
import ru.algocode.exam2022.utils.ExternalXmlParser
import ru.algocode.exam2022.utils.SheetsAPI
import ru.algocode.exam2022.utils.Stats
import ru.algocode.exam2022.utils.Utils
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.max

class GameState internal constructor(private val plugin: JavaPlugin) {
    val config: Config
    private val playersApi: SheetsAPI
    private val players: ConcurrentHashMap<String, Stats>
    private val bossBars: ConcurrentHashMap<String, BossBar> = ConcurrentHashMap()
    private var playersCount: Int
    private val scoreboardObjective: Objective
    private var secondsCount: Int
    var xmlParser: ExternalXmlParser = ExternalXmlParser(plugin)

    init {
        playersCount = 1
        secondsCount = 0
        playersApi = SheetsAPI(plugin.config.getString("spreadsheet_id")!!, plugin)
        config = Config(plugin)
        players = ConcurrentHashMap()
        reloadConfig()
        try {
            xmlParser.UpdateStandings(config.externalXmlPath, players)
        } catch (e: Exception) {
            println("Failed to update standings")
        }
        reloadPlayers()
        tickPlayers()
        val manager = Bukkit.getScoreboardManager()
        val board = manager!!.newScoreboard
        scoreboardObjective =
            board.registerNewObjective(
                "score",
                "dummy",
                ChatColor.GOLD.toString() +
                        "Таблица результатов",
            )
        scoreboardObjective.displaySlot = DisplaySlot.SIDEBAR
    }

    fun tick() {
        secondsCount++
        if (secondsCount % 60 == 0) {
            reloadPlayers()
        }
        if (secondsCount % 20 == 10) {
            try {
                xmlParser.UpdateStandings(config.externalXmlPath, players)
            } catch (e: Exception) {
                println("Failed to update standings")
            }
        }
        tickPlayers()
        spawnItems()
    }

    fun initPlayer(player: Player) {
        player.scoreboard = scoreboardObjective.scoreboard!!
        updatePlayerInfo(player)
    }

    fun died(player: Player) {
        val name = player.name
        if (players.containsKey(name)) {
            players[name]!!.Died()
        }
        updatePlayerInfo(player)
    }

    fun killed(player: Player) {
        val name = player.name
        if (players.containsKey(name)) {
            val playerStats = players[name]
            if (playerStats!!.GetTimeUntilNextKill() > 0) {
                playerStats.CruelKilledSomeone(config.timeUntilNextKill)
                player.sendMessage("Слишком частые убийства")
            } else {
                playerStats.KilledSomeone(config.timeUntilNextKill)
            }
        }
        updatePlayerInfo(player)
    }

    fun fillChest(inventory: Inventory) {
        for (item in config.chestItems!!) {
            if (item.Generated()) {
                val itemStack = item.GenerateItem()
                val pos = Utils.random.nextInt(inventory.size)
                inventory.setItem(pos, itemStack)
            }
        }
    }

    fun openMerchant(player: Player?) {
        config.merchantMenu!!.Open(player)
    }

    fun buyMerchant(event: InventoryClickEvent) {
        val itemStack = config.merchantMenu!!.OnClick(event) ?: return
        val item = itemStack.GenerateItem()
        val meta = item.itemMeta
        meta!!.lore = emptyList()
        item.itemMeta = meta
        val player = event.whoClicked as Player
        if (player.isOp) {
            player.inventory.addItem(item)
            return
        }
        val name = player.name
        if (!players.containsKey(name)) {
            return
        }
        val stats = players[name]
        if (stats!!.GetScore() >= itemStack.GetParam()) {
            stats.Purchase(itemStack.GetParam())
            player.inventory.addItem(item)
            updatePlayerInfo(player)
        } else {
            Utils.sendTitle(player, ChatColor.RED.toString() + "Недостаточно денег!")
        }
    }

    fun reloadConfig() {
        val newPlayersCount = config.ReloadConfig()
        for (row in playersApi["Gamestats!A" + (playersCount + 1) + ":M" + newPlayersCount]!!) {
            val player = Stats(row)
            players[player.GetLogin()] = player
        }
        for (row in playersApi["Problems!B" + (playersCount + 1) + ":" + ('B'.code + config.problemsCount).toChar() + newPlayersCount]!!) {
            val login = row[0] as String
            if (players.containsKey(login)) {
                players[login]!!.LoadProblems(row.subList(1, row.size))
                players[login]!!.RecalculateScore(config.statsMultiplier)
            }
        }
        for (ejudgeAuth in config.GetSheetsApi()["Users!A" + (playersCount + 1) + ":E" + newPlayersCount]!!) {
            val login = ejudgeAuth[1] as String
            if (players.containsKey(login)) {
                players[login]!!.AddEjudgeAuth(ejudgeAuth)
            }
        }
        playersCount = newPlayersCount
    }

    fun submitProblem(player: Player, label: String, code: String) {
        val name = player.name
        if (players.containsKey(name)) {
            val playerStats = players[name]
            val t = Thread(object : Runnable {
                var login = playerStats!!.GetEjudgeLogin()
                var pasword = playerStats!!.GetEjudgePassword()
                var problem = label
                var source = code
                var contest = config.ejudgeContestId
                override fun run() {
                    val session = EjudgeSession(login, pasword, contest)
                    session.authenticate()
                    session.submit(problem, source)
                }
            })
            t.start()
        }
    }

    fun GetStatus(player: Player) {
        val name = player.name
        if (players.containsKey(name)) {
            val playerStats = players[name]
            player.sendMessage(playerStats!!.GetStatus())
        }
    }

    private fun spawnItems() {
        for (item in config.spawnItems!!) {
            if (item.Generated()) {
                val itemStack = item.GenerateItem()
                val world = plugin.server.getWorld("world")
                val x =
                    config.spawnerX + Utils.random.nextInt(config.spawnerRadius * 2 + 1) - config.spawnerRadius - 1
                val z =
                    config.spawnerZ + Utils.random.nextInt(config.spawnerRadius * 2 + 1) - config.spawnerRadius - 1
                val loc = Location(world, x.toDouble(), config.spawnerY.toDouble(), z.toDouble())
                loc.world!!.dropItemNaturally(loc, itemStack)
            }
        }
    }

    private fun tickPlayers() {
        for (player in plugin.server.onlinePlayers) {
            val name = player.name
            if (players.containsKey(name)) {
                val playerStats = players[name]
                playerStats!!.Tick()
                if (secondsCount % 60 == 0) {
                    playerStats.IncInGame()
                }
                val location = player.location
                if (
                    abs(location.x - config.spawnerX) <= config.spawnerRadius &&
                    abs(location.z - config.spawnerZ) <= config.spawnerRadius
                ) {
                    if (!playerStats.GetIsInForbiddenZone()) {
                        bossBars[name]?.removeAll()
                        bossBars.remove(name)
                        bossBars[name] =
                            plugin.server.createBossBar("Запретная зона", BarColor.RED, BarStyle.SOLID).apply {
                                addPlayer(player)
                                progress = max(
                                    1.0 - playerStats.GetTimeInForbiddenZone() / config.maxTimeInForbiddenZone.toDouble(),
                                    0.0
                                )
                                isVisible = true
                            }
                    }
                    playerStats.InForbiddenZone(config.timeIncreaseForForbiddenZone)
                    if (playerStats.GetTimeInForbiddenZone() >= config.maxTimeInForbiddenZone) {
                        val scoreBefore = playerStats.GetScore()
                        playerStats.TooLongInForbiddenZone(config.penaltyForForbiddenZone)
                        bossBars[name]!!.run {
                            color = BarColor.YELLOW
                            setTitle(
                                "Запретная зона: штраф " +
                                        "${config.penaltyForForbiddenZone}, " +
                                        "$scoreBefore -> ${scoreBefore - config.penaltyForForbiddenZone}"
                            )
                        }
                    } else
                        bossBars[name]!!.run {
                            progress = max(
                                1.0 - playerStats.GetTimeInForbiddenZone() / config.maxTimeInForbiddenZone.toDouble(),
                                0.0
                            )
                            setTitle(
                                "Запретная зона: осталоcь " +
                                        "${
                                            (config.maxTimeInForbiddenZone - playerStats.GetTimeInForbiddenZone())
                                                    / config.timeIncreaseForForbiddenZone
                                        } сек. "
                            )
                        }
                } else {
                    playerStats.InAllowedZone()
                    val regenSucc = "Время в запретной зоне восстановлено"
                    if (playerStats.GetTimeInForbiddenZone() == 0 && bossBars[name]?.title == regenSucc) {
                        bossBars[name]?.removeAll()
                        bossBars.remove(name)
                    } else if (bossBars[name] != null) {
                        bossBars[name]!!.run {
                            if (playerStats.GetTimeInForbiddenZone() > 0) {
                                color = BarColor.GREEN
                                progress = max(
                                    1.0 -
                                            playerStats.GetTimeInForbiddenZone()
                                            / config.maxTimeInForbiddenZone.toDouble(),
                                    0.0
                                )
                                val left = "Вы покинули запретную зону"
                                val regen = "Восстановление:"

                                setTitle(
                                    if (title == left) {
                                        regen
                                    } else {
                                        if (title == regen || title.isEmpty()) {
                                            isVisible = false
                                            ""
                                        } else {
                                            left
                                        }
                                    }
                                )
                            } else {
                                isVisible = true
                                setTitle(regenSucc)
                            }
                        }
                    }
                }
                updatePlayerInfo(player)
            }
        }
    }

    private fun reloadPlayers() {
        val gamestats: MutableList<List<Any>> = ArrayList()
        val problems: MutableList<List<Any>> = ArrayList()
        var idx = 0
        for (row in playersApi["Gamestats!B2:J$playersCount"]!!) {
            gamestats.add(ArrayList())
            problems.add(ArrayList())
            val login = row[0] as String
            val bonus: Int = (row[8] as String).toInt()
            if (players.containsKey(login)) {
                players[login]!!.SetBonus(bonus)
                players[login]!!.RecalculateScore(config.statsMultiplier)
                players[login]!!.ExportStats(gamestats[idx], problems[idx])
            }
            idx++
        }
        playersApi.update("Gamestats!C2:M$playersCount", gamestats)
        playersApi.update("Problems!C2:" + ('C'.code + config.problemsCount).toChar() + playersCount, problems)
    }

    private fun updatePlayerInfo(player: Player) {
        val name = player.name
        val displayName: String
        if (player.isOp) {
            displayName = ChatColor.RED.toString() + "Преподаватель " + ChatColor.GOLD + name + ChatColor.RESET
        } else if (players.containsKey(name)) {
            val playerStats = players[name]
            playerStats!!.RecalculateScore(config.statsMultiplier)
            val score = playerStats.GetScore()
            displayName = "(" + score + ") " + ChatColor.GREEN + playerStats.GetName() + ChatColor.RESET
            scoreboardObjective.getScore(playerStats.GetName()).score = score
        } else {
            displayName = name
        }
        player.setDisplayName(displayName)
        player.setPlayerListName(displayName)
    }
}