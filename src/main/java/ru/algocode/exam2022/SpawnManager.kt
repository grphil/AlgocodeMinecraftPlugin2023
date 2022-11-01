package ru.algocode.exam2022

import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class SpawnManager internal constructor(private val plugin: JavaPlugin) {
    var spawns: MutableList<Location?>? = null
    private var pointer = 0

    init {
        initializeSpawns()
    }

    private fun initializeSpawns() {
        spawns = ArrayList()
        for (loc in plugin.config.getStringList("spawns")) {
            val parts = loc.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val world = parts[0]
            val x = parts[1].toInt()
            val y = parts[2].toInt()
            val z = parts[3].toInt()
            spawns!!.add(Location(plugin.server.getWorld(world), x.toDouble(), y.toDouble(), z.toDouble()))
        }
        spawns!!.shuffle()
        pointer = 0
    }

    fun addSpawn(l: Location): Int {
        val loc = l.world!!.name + ":" + l.blockX + ":" + l.blockY + ":" + l.blockZ
        val cur = plugin.config.getStringList("spawns")
        cur.add(loc)
        plugin.config["spawns"] = cur
        plugin.saveConfig()
        initializeSpawns()
        return spawns!!.size
    }

    val randomSpawnLocation: Location?
        get() {
            if (spawns!!.isEmpty()) {
                return plugin.server.worlds[0].spawnLocation
            }
            if (pointer >= spawns!!.size) {
                pointer = 0
            }
            return spawns!![pointer++]
        }
}