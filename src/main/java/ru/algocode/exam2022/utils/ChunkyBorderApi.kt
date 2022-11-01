package ru.algocode.exam2022

import org.bukkit.Bukkit
import org.bukkit.ChatColor

/**
 * Автор плагина ChunkyBorder молодец, что сделал сервер-сайд визуализацию границ
 *
 * Но он слишком любил инкапсуляцию, и поставил private там, где это совсем блин было не нужно.
 *
 * Поэтому теперь, вместо того, чтобы использовать нормальное апи того плагина,
 * тупо подключив его как библиотеку, я буду вызывать команды из консоли сервера...
 */
class ChunkyBorderApi {
    private val sender = Bukkit.getConsoleSender()
    private fun execute(command: String) = Bukkit.dispatchCommand(sender, command)
    private val prf = "chunky"

    init {
        plugin.server.pluginManager.getPlugin("ChunkyBorder")
            ?: throw DependencyPluginNotLoaded("ChunkyBorder is required")
        sender.run {
            sendMessage(ChatColor.RED.toString() + "DO NOT TOUCH ANYTHING!")
            sendMessage(ChatColor.RED.toString() + "DO NOT USE CHUNKY COMMANDS!")
        }
    }

    fun world(world: String = "world") = execute("$prf world $world")
    fun shape(shape: String = "square") = execute("$prf shape $shape")
    fun center(x: Long, z: Long) = execute("$prf center $x $z")
    fun radius(r: Long) = execute("$prf radius $r")
    fun add() = execute("$prf border add")
    fun remove() = execute("$prf border remove")

    /**
     * adds CHUNKY border
     * @param x x of center of border
     * @param z z of center of border
     * @param r radius
     * @param world name of world to add the border to
     * @param shape shape of border, e.g. "circle", "square", etc
     */
    fun replaceBorder(
        x: Long,
        z: Long,
        r: Long,
        world: String = "world",
        shape: String = "square"
    ) {
        world(world)
        remove()
        shape(shape)
        center(x, z)
        radius(r)
        add()
    }


    /**
     * makes border bypassable to [player]
     */
    fun bypass(player: String) = execute("$prf border bypass $player")
}

class DependencyPluginNotLoaded(override val message: String) : Exception()