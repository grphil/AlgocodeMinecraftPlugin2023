package ru.algocode.exam2022;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class Exam2022 extends JavaPlugin implements Listener {

    private GameState game;
    private SpawnManager spawnManager;
    private HashSet<Location> updatedChests;

    private void initConfig() {
        getConfig();
        getConfig().addDefault("spreadsheet_id", "???");
        getConfig().addDefault("config_table_id", "???");
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public void onEnable() {
        initConfig();
        game = new GameState(this);

        for (Player player : getServer().getOnlinePlayers()) {
            game.InitPlayer(player);
        }

        (new BukkitRunnable() {
            @Override
            public void run() {
                game.Tick();
            }
        }).runTaskTimer(this, 20, 20);

        spawnManager = new SpawnManager(this);
        updatedChests = new HashSet<>();

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("addspawn").setExecutor(new SpawnCommand(spawnManager));
        getCommand("resetchests").setExecutor(new ResetChestsCommand(this));
        getCommand("syncconfig").setExecutor(new ReloadConfigCommand(this));
        getCommand("problemstatus").setExecutor(new EjudgeStatusCommand(this));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        game.InitPlayer(player);
        event.setJoinMessage(player.getDisplayName() + ChatColor.RESET + " присоединился!");
        player.sendMessage(ChatColor.GOLD + "Добро пожаловать на наш сервер экзамена");

        if (!player.hasPlayedBefore()) {
            Location loc = spawnManager.getRandomSpawnLocation();
            loc.getChunk().load();
            (new BukkitRunnable() {
                @Override
                public void run() {
                    player.teleport(loc);
                }
            }).runTaskLater(this, 1);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(player.getDisplayName() + " вышел!");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killed = event.getEntity();
        game.Died(killed);
        Player killer = killed.getKiller();
        if (killer != null) {
            game.Killed(killer);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        Player player = event.getPlayer();
        if (block.getType() == Material.CHEST && player.isOp()) {
            Inventory chestInventory = ((Chest) block.getState()).getBlockInventory();
            game.FillChest(chestInventory);
        }
        updatedChests.add(block.getLocation());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.BLAZE_ROD) {
            return;
        }

        item.setAmount(item.getAmount() - 1);
        player.sendMessage(ChatColor.RED + "Данные об игроках:");

        for (Player online : getServer().getOnlinePlayers()) {
            if (player == online || online.isOp()) {
                continue;
            }
            String message = online.getName() +
                    ": X=" + online.getLocation().getBlockX() +
                    ", Y=" + online.getLocation().getBlockY() +
                    ", Z=" + online.getLocation().getBlockZ();
            player.sendMessage(message);
        }
    }

    @EventHandler
    public void onInventoryOpenEvent(InventoryOpenEvent e){
        if (e.getInventory().getHolder() instanceof Chest) {
            Location loc = e.getInventory().getLocation();
            if (updatedChests.contains(loc)) {
                return;
            }
            updatedChests.add(loc);
            e.getInventory().clear();
            game.FillChest(e.getInventory());
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntityType() == EntityType.VILLAGER) {
            Villager villager = (Villager) event.getEntity();
            villager.setInvulnerable(true);
            villager.setAI(false);
            villager.setCustomName(ChatColor.GREEN + "Торговец");
            villager.setCustomNameVisible(true);
        }
    }
    
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() != EntityType.VILLAGER) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        game.OpenMerchant(player);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(spawnManager.getRandomSpawnLocation());
    }

    @EventHandler(priority= EventPriority.MONITOR)
    void onInventoryClick(InventoryClickEvent event) {
        game.BuyMerchant(event);
    }

    @EventHandler
    public void onPlayerEditBook(PlayerEditBookEvent event) {
        if (!event.isSigning()) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        String title = event.getNewBookMeta().getTitle();
        if (title == null) {
            return;
        }
        if (title.length() != 1) {
            return;
        }

        List<String> pages = event.getNewBookMeta().getPages();
        String code = String.join("\n", pages);
        this.game.SubmitProblem(player, title, code);
    }

    void ResetChests() {
        updatedChests.clear();
    }

    void ReloadConfig() {
        game.ReloadConfig();
    }

    void GetStatus(Player player) {
        game.GetStatus(player);
    }
}
