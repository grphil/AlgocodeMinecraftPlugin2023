package ru.algocode.exam2022;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class SpawnManager {
    private JavaPlugin plugin;
    List<Location> spawns;
    private int pointer;
    String team;

    SpawnManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.initializeSpawns();
    }

    private void initializeSpawns() {
        this.spawns = new ArrayList<>();
        for (String loc : this.plugin.getConfig().getStringList("spawns")) {
            String[] parts = loc.split(":");
            String world = parts[0];
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);
            this.spawns.add(new Location(this.plugin.getServer().getWorld(world), x, y, z));
        }
        Collections.shuffle(this.spawns);
        this.pointer = 0;
    }

    int addSpawn(Location l) {
        String loc = l.getWorld().getName() + ":" + l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ();
        List<String> cur = this.plugin.getConfig().getStringList("spawns");
        cur.add(loc);
        this.plugin.getConfig().set("spawns", cur);
        this.plugin.saveConfig();
        this.initializeSpawns();
        return this.spawns.size();
    }

    Location getRandomSpawnLocation() {
        if (this.spawns.isEmpty()) {
            return this.plugin.getServer().getWorlds().get(0).getSpawnLocation();
        }
        if (this.pointer >= this.spawns.size()) {
            this.pointer = 0;
        }
        return this.spawns.get(this.pointer++);
    }
}
