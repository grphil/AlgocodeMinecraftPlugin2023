package ru.algocode.exam2022.EventHandlers

import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import ru.algocode.exam2022.APlugin

class BlockEvents : Listener {
    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val block = event.blockPlaced
        val player = event.player
        if (block.type == Material.CHEST && player.isOp) {
            val chestInventory = (block.state as Chest).blockInventory
            APlugin.game!!.FillChest(chestInventory)
        }
        APlugin.updatedChests!!.add(block.location)
    }
}