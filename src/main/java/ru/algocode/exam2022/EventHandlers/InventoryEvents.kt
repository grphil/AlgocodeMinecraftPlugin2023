package ru.algocode.exam2022.EventHandlers

import org.bukkit.block.Chest
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import ru.algocode.exam2022.APlugin

class InventoryEvents : Listener {
    @EventHandler
    fun onInventoryOpenEvent(e: InventoryOpenEvent) {
        if (e.inventory.holder is Chest) {
            val loc = e.inventory.location
            if (APlugin.updatedChests!!.contains(loc)) {
                return
            }
            APlugin.updatedChests!!.add(loc)
            e.inventory.clear()
            APlugin.game!!.FillChest(e.inventory)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onInventoryClick(event: InventoryClickEvent?) {
        APlugin.game!!.BuyMerchant(event!!)
    }
}