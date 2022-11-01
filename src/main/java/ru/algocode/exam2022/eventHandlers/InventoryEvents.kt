package ru.algocode.exam2022.eventHandlers

import org.bukkit.block.Chest
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import ru.algocode.exam2022.plugin

class InventoryEvents : Listener {
    @EventHandler
    fun onInventoryOpenEvent(e: InventoryOpenEvent) {
        if (e.inventory.holder is Chest) {
            val loc = e.inventory.location
            if (plugin.updatedChests.contains(loc)) {
                return
            }
            plugin.updatedChests.add(loc)
            e.inventory.clear()
            plugin.game.fillChest(e.inventory)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onInventoryClick(event: InventoryClickEvent?) {
        plugin.game.buyMerchant(event!!)
    }
}