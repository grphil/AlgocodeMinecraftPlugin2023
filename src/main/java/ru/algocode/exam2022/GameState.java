package ru.algocode.exam2022;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import ru.algocode.ejudge.EjudgeSession;
import ru.algocode.ejudge.EjudgeSessionException;

import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.log;

class GameState {
    private JavaPlugin plugin;
    private Config config;

    private SheetsAPI playersApi;

    private HashMap<String, Stats> players;
    private int playersCount;

    private Objective scoreboardObjective;

    private int secondsCount;

    ExternalXmlParser xmlParser;

    GameState(JavaPlugin plugin) {
        this.plugin = plugin;
        xmlParser = new ExternalXmlParser(plugin);
        playersCount = 1;
        secondsCount = 0;
        playersApi = new SheetsAPI(plugin.getConfig().getString("spreadsheet_id"));
        config = new Config(plugin);
        players = new HashMap<>();
        ReloadConfig();

        try {
            xmlParser.UpdateStandings(config.ExternalXmlPath, players);
        } catch (Exception e) {
            System.out.println("Failed to update standings");
        }
        reloadPlayers();
        tickPlayers();

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        scoreboardObjective = board.registerNewObjective("score", "dummy");
        scoreboardObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        scoreboardObjective.setDisplayName(ChatColor.GOLD + "Таблица результатов");
    }

    void Tick() {
        secondsCount++;

        if (secondsCount % 60 == 0) {
            reloadPlayers();
        }

        if (secondsCount % 20 == 10) {
            try {
                xmlParser.UpdateStandings(config.ExternalXmlPath, players);
            } catch (Exception e) {
                System.out.println("Failed to update standings");
            }
        }

        tickPlayers();
        spawnItems();
    }

    void InitPlayer(Player player) {
        player.setScoreboard(scoreboardObjective.getScoreboard());
        updatePlayerInfo(player);
    }

    void Died(Player player) {
        String name = player.getName();
        if (players.containsKey(name)) {
            players.get(name).Died();
        }
        updatePlayerInfo(player);
    }

    void Killed(Player player) {
        String name = player.getName();
        if (players.containsKey(name)) {
            Stats playerStats = players.get(name);
            if (playerStats.GetTimeUntilNextKill() > 0) {
                playerStats.CruelKilledSomeone(config.TimeUntilNextKill);
                player.sendMessage("Слишком частые убийства");
            } else {
                playerStats.KilledSomeone(config.TimeUntilNextKill);
            }
        }
        updatePlayerInfo(player);
    }

    void FillChest(Inventory inventory) {
        for (ParametrizedItemStack item : config.ChestItems) {
            if (item.Generated()) {
                ItemStack itemStack = item.GenerateItem();
                if (item != null) {
                    int pos = Utils.random.nextInt(inventory.getSize());
                    inventory.setItem(pos, itemStack);
                }
            }
        }
    }

    void OpenMerchant(Player player) {
        config.MerchantMenu.Open(player);
    }

    void BuyMerchant(InventoryClickEvent event) {
        ParametrizedItemStack itemStack = config.MerchantMenu.OnClick(event);
        if (itemStack == null) {
            return;
        }

        ItemStack item = itemStack.GenerateItem();
        ItemMeta meta = item.getItemMeta();
        meta.setLore(Collections.emptyList());
        item.setItemMeta(meta);

        Player player = (Player) event.getWhoClicked();

        if (player.isOp()) {
            player.getInventory().addItem(item);
            return;
        }
        String name = player.getName();
        if (!players.containsKey(name)) {
            return;
        }

        Stats stats = players.get(name);
        if (stats.GetScore() >= itemStack.GetParam()) {
            stats.Purchase(itemStack.GetParam());
            player.getInventory().addItem(item);
            updatePlayerInfo(player);
        } else {
            Utils.sendTitle(player, ChatColor.RED + "Недостаточно денег!");
        }
    }

    void ReloadConfig() {
        int newPlayersCount = config.ReloadConfig();

        for (List<Object> row : playersApi.get("Gamestats!A" + (playersCount + 1) + ":M" + newPlayersCount)) {
            Stats player = new Stats(row);
            players.put(player.GetLogin(), player);
        }

       for (List<Object> row : playersApi.get("Problems!B" + (playersCount + 1) + ":" + (char)('B' + config.ProblemsCount) + newPlayersCount)) {
            String login = (String) row.get(0);
            if (players.containsKey(login)) {
                players.get(login).LoadProblems(row.subList(1, row.size()));
                players.get(login).RecalculateScore(config.StatsMultiplier);
            }
        }

        for (List<Object> ejudgeAuth : config.GetSheetsApi().get("Users!A" + (playersCount + 1) + ":E" + newPlayersCount)) {
            String login = (String) ejudgeAuth.get(1);
            if (players.containsKey(login)) {
                players.get(login).AddEjudgeAuth(ejudgeAuth);
            }
        }

        playersCount = newPlayersCount;
    }

    void SubmitProblem(Player player, String label, String code) {
        String name = player.getName();
        if (players.containsKey(name)) {
            Stats playerStats = players.get(name);

            Thread t = new Thread(new Runnable() {
                String login = playerStats.GetEjudgeLogin();
                String pasword = playerStats.GetEjudgePassword();
                String problem = label;
                String source = code;
                int contest = config.EjudgeContestId;
                public void run() {
                    EjudgeSession session = new EjudgeSession(login, pasword, contest);
                    session.authenticate();
                    session.submit(problem, source);
                }
            });
            t.start();
        }
    }

    void GetStatus(Player player) {
        String name = player.getName();
        if (players.containsKey(name)) {
            Stats playerStats = players.get(name);
            player.sendMessage(playerStats.GetStatus());
        }
    }

    private void spawnItems() {
        for (ParametrizedItemStack item : config.SpawnItems) {
            if (item.Generated()) {
                ItemStack itemStack = item.GenerateItem();
                if (item != null) {
                    World world = plugin.getServer().getWorld("world");
                    int x = config.SpawnerX + Utils.random.nextInt(config.SpawnerRadius * 2 + 1) - config.SpawnerRadius - 1;
                    int z = config.SpawnerZ + Utils.random.nextInt(config.SpawnerRadius * 2 + 1) - config.SpawnerRadius - 1;
                    Location loc = new Location(world, x, config.SpawnerY, z);
                    loc.getWorld().dropItemNaturally(loc, itemStack);
                }
            }
        }
    }

    private void tickPlayers() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            String name = player.getName();
            if (players.containsKey(name)) {
                Stats playerStats = players.get(name);
                playerStats.Tick();
                if (secondsCount % 60 == 0) {
                    playerStats.IncInGame();
                }
                Location location = player.getLocation();
                if (
                        abs(location.getX() - config.SpawnerX) <= config.SpawnerRadius * 2 &&
                        abs(location.getY() - config.SpawnerY) <= config.SpawnerRadius * 2 &&
                        abs(location.getZ() - config.SpawnerZ) <= config.SpawnerRadius * 2
                ) {
                    if (!playerStats.GetIsInForbiddenZone()) {
                        player.sendMessage("Вы в запретной зоне, покинте её в течении " +
                                (config.MaxTimeInForbiddenZone - playerStats.GetTimeInForbiddenZone()) / config.TimeIncreaseForForbiddenZone +
                                "секунд"
                        );
                    }
                    playerStats.InForbiddenZone(config.TimeIncreaseForForbiddenZone);
                    if (playerStats.GetTimeInForbiddenZone() >= config.MaxTimeInForbiddenZone) {
                        player.sendMessage(ChatColor.RED + "Слишком долго в запретной зоне, штраф " + config.PenaltyForForbiddenZone);
                        playerStats.TooLongInForbiddenZone(config.PenaltyForForbiddenZone);
                    }
                } else {
                    playerStats.InAllowedZone();
                }
                updatePlayerInfo(player);
            }
        }
    }

    private void reloadPlayers() {
        List<List<Object>> gamestats = new ArrayList<>();
        List<List<Object>> problems = new ArrayList<>();

        int idx = 0;
        for (List<Object> row : playersApi.get("Gamestats!B2:J" + playersCount)) {
            gamestats.add(new ArrayList<>());
            problems.add(new ArrayList<>());
            String login = (String) row.get(0);
            int bonus = Integer.parseInt((String) row.get(8));

            if (players.containsKey(login)) {
                players.get(login).SetBonus(bonus);
                players.get(login).RecalculateScore(config.StatsMultiplier);
                players.get(login).ExportStats(gamestats.get(idx), problems.get(idx));
            }
            idx++;
        }

        playersApi.update("Gamestats!C2:M" + playersCount, gamestats);
        playersApi.update("Problems!C2:" + (char)('C' + config.ProblemsCount) + playersCount, problems);
    }

    private void updatePlayerInfo(Player player) {
        String name = player.getName();
        String displayName;
        if (player.isOp()) {
            displayName = ChatColor.RED + "Преподаватель " + ChatColor.GOLD + name + ChatColor.RESET;
        } else if (players.containsKey(name)) {
            Stats playerStats = players.get(name);
            playerStats.RecalculateScore(config.StatsMultiplier);
            int score = playerStats.GetScore();
            displayName = "(" + score + ") " + ChatColor.GREEN + playerStats.GetName() + ChatColor.RESET;
            scoreboardObjective.getScore(playerStats.GetName()).setScore(score);
        } else {
            displayName = name;
        }

        player.setDisplayName(displayName);
        player.setPlayerListName(displayName);
    }

}
