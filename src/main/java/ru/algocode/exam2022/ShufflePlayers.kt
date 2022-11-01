@file:Suppress("DEPRECATION")

package ru.algocode.exam2022

import org.bukkit.World
import java.util.*
import java.util.logging.Level

fun shufflePlayers(world: World) {
    val registeredPlayers = mutableSetOf<UUID>()
    val playersLocations = world.players.filter { !it.isOp }.map {
        registeredPlayers.add(it.uniqueId)
        plugin.logger.log(Level.INFO, it.location.toString())
        Pair(it.location, it.displayName)
    }.shuffled().toMutableList()
    world.players.forEach {
        if (!it.isOp && it.uniqueId in registeredPlayers)
            it.run {
                sendTitle(
                    "Время шаффла!",
                    "Вы там, где был игрок ${playersLocations.last().second}",
                    10,
                    40,
                    20,
                )
                it.teleport(playersLocations.removeLast().first)
            }
    }
}