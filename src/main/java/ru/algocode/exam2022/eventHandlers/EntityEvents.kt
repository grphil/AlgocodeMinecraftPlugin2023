package ru.algocode.exam2022.eventHandlers

import org.bukkit.ChatColor
import org.bukkit.entity.EntityType
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntitySpawnEvent

class EntityEvents : Listener {
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

}