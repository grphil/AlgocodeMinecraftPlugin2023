package ru.algocode.exam2022.eventHandlers

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareAnvilEvent

class AnvilEvents : Listener {
    @EventHandler
    fun onPrepareAnvilEvent(event: PrepareAnvilEvent) {
        if (event.inventory.renameText == "SHUFFLE BALL") {
            event.inventory.repairCost = 228336
        }
    }
}