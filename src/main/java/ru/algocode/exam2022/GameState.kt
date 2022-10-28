package ru.algocode.exam2022

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import ru.algocode.ejudge.EjudgeSession

class GameState internal constructor(private val plugin: JavaPlugin) {
    private val config: Config
    private val playersApi: SheetsAPI
    private val players: HashMap<String, Stats>
    private var playersCount: Int
    private val scoreboardObjective: Objective
    private var secondsCount: Int
    var xmlParser: ExternalXmlParser

    init {
        xmlParser = ExternalXmlParser(plugin)
        playersCount = 1
        secondsCount = 0
        playersApi = SheetsAPI(plugin.config.getString("spreadsheet_id"))
        config = Config(plugin)
        players = HashMap()
        ReloadConfig()
        try {
            xmlParser.UpdateStandings(config.ExternalXmlPath, players)
        } catch (e: Exception) {
            println("Failed to update standings")
        }
        reloadPlayers()
        tickPlayers()
        val manager = Bukkit.getScoreboardManager()
        val board = manager!!.newScoreboard
        scoreboardObjective = board.registerNewObjective("score", "dummy")
        scoreboardObjective.displaySlot = DisplaySlot.SIDEBAR
        scoreboardObjective.displayName = ChatColor.GOLD.toString() + "Таблица результатов"
    }

    fun Tick() {
        secondsCount++
        if (secondsCount % 60 == 0) {
            reloadPlayers()
        }
        if (secondsCount % 20 == 10) {
            try {
                xmlParser.UpdateStandings(config.ExternalXmlPath, players)
            } catch (e: Exception) {
                println("Failed to update standings")
            }
        }
        tickPlayers()
        spawnItems()
    }

    fun InitPlayer(player: Player) {
        player.scoreboard = scoreboardObjective.scoreboard!!
        updatePlayerInfo(player)
    }

    fun Died(player: Player) {
        val name = player.name
        if (players.containsKey(name)) {
            players[name]!!.Died()
        }
        updatePlayerInfo(player)
    }

    fun Killed(player: Player) {
        val name = player.name
        if (players.containsKey(name)) {
            val playerStats = players[name]
            if (playerStats!!.GetTimeUntilNextKill() > 0) {
                playerStats.CruelKilledSomeone(config.TimeUntilNextKill)
                player.sendMessage("Слишком частые убийства")
            } else {
                playerStats.KilledSomeone(config.TimeUntilNextKill)
            }
        }
        updatePlayerInfo(player)
    }

    fun FillChest(inventory: Inventory) {
        for (item in config.ChestItems) {
            if (item!!.Generated()) {
                val itemStack = item.GenerateItem()
                if (item != null) {
                    val pos = Utils.random.nextInt(inventory.size)
                    inventory.setItem(pos, itemStack)
                }
            }
        }
    }

    fun OpenMerchant(player: Player?) {
        config.MerchantMenu.Open(player)
    }

    fun BuyMerchant(event: InventoryClickEvent) {
        val itemStack = config.MerchantMenu.OnClick(event) ?: return
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

    fun ReloadConfig() {
        val newPlayersCount = config.ReloadConfig()
        for (row in playersApi["Gamestats!A" + (playersCount + 1) + ":M" + newPlayersCount]) {
            val player = Stats(row)
            players[player.GetLogin()] = player
        }
        for (row in playersApi["Problems!B" + (playersCount + 1) + ":" + ('B'.code + config.ProblemsCount).toChar() + newPlayersCount]) {
            val login = row[0] as String
            if (players.containsKey(login)) {
                players[login]!!.LoadProblems(row.subList(1, row.size))
                players[login]!!.RecalculateScore(config.StatsMultiplier)
            }
        }
        for (ejudgeAuth in config.GetSheetsApi()["Users!A" + (playersCount + 1) + ":E" + newPlayersCount]) {
            val login = ejudgeAuth[1] as String
            if (players.containsKey(login)) {
                players[login]!!.AddEjudgeAuth(ejudgeAuth)
            }
        }
        playersCount = newPlayersCount
    }

    fun SubmitProblem(player: Player, label: String, code: String) {
        val name = player.name
        if (players.containsKey(name)) {
            val playerStats = players[name]
            val t = Thread(object : Runnable {
                var login = playerStats!!.GetEjudgeLogin()
                var pasword = playerStats!!.GetEjudgePassword()
                var problem = label
                var source = code
                var contest = config.EjudgeContestId
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
        for (item in config.SpawnItems) {
            if (item!!.Generated()) {
                val itemStack = item.GenerateItem()
                if (item != null) {
                    val world = plugin.server.getWorld("world")
                    val x =
                        config.SpawnerX + Utils.random.nextInt(config.SpawnerRadius * 2 + 1) - config.SpawnerRadius - 1
                    val z =
                        config.SpawnerZ + Utils.random.nextInt(config.SpawnerRadius * 2 + 1) - config.SpawnerRadius - 1
                    val loc = Location(world, x.toDouble(), config.SpawnerY.toDouble(), z.toDouble())
                    loc.world!!.dropItemNaturally(loc, itemStack)
                }
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
                if (Math.abs(location.x - config.SpawnerX) <= config.SpawnerRadius * 2 && Math.abs(location.y - config.SpawnerY) <= config.SpawnerRadius * 2 && Math.abs(
                        location.z - config.SpawnerZ
                    ) <= config.SpawnerRadius * 2
                ) {
                    if (!playerStats.GetIsInForbiddenZone()) {
                        player.sendMessage(
                            "Вы в запретной зоне, покинте её в течении " + (config.MaxTimeInForbiddenZone - playerStats.GetTimeInForbiddenZone()) / config.TimeIncreaseForForbiddenZone +
                                    "секунд"
                        )
                    }
                    playerStats.InForbiddenZone(config.TimeIncreaseForForbiddenZone)
                    if (playerStats.GetTimeInForbiddenZone() >= config.MaxTimeInForbiddenZone) {
                        player.sendMessage(ChatColor.RED.toString() + "Слишком долго в запретной зоне, штраф " + config.PenaltyForForbiddenZone)
                        playerStats.TooLongInForbiddenZone(config.PenaltyForForbiddenZone)
                    }
                } else {
                    playerStats.InAllowedZone()
                }
                updatePlayerInfo(player)
            }
        }
    }

    private fun reloadPlayers() {
        val gamestats: MutableList<List<Any>> = ArrayList()
        val problems: MutableList<List<Any>> = ArrayList()
        var idx = 0
        for (row in playersApi["Gamestats!B2:J$playersCount"]) {
            gamestats.add(ArrayList())
            problems.add(ArrayList())
            val login = row[0] as String
            val bonus: Int = (row[8] as String).toInt()
            if (players.containsKey(login)) {
                players[login]!!.SetBonus(bonus)
                players[login]!!.RecalculateScore(config.StatsMultiplier)
                players[login]!!.ExportStats(gamestats[idx], problems[idx])
            }
            idx++
        }
        playersApi.update("Gamestats!C2:M$playersCount", gamestats)
        playersApi.update("Problems!C2:" + ('C'.code + config.ProblemsCount).toChar() + playersCount, problems)
    }

    private fun updatePlayerInfo(player: Player) {
        val name = player.name
        val displayName: String
        if (player.isOp) {
            displayName = ChatColor.RED.toString() + "Преподаватель " + ChatColor.GOLD + name + ChatColor.RESET
        } else if (players.containsKey(name)) {
            val playerStats = players[name]
            playerStats!!.RecalculateScore(config.StatsMultiplier)
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