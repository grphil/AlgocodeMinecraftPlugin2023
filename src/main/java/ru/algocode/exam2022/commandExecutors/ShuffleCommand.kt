package ru.algocode.exam2022.commandExecutors

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import ru.algocode.exam2022.shufflePlayers
import ru.algocode.exam2022.plugin

class ShuffleCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.isOp) {
            try {
                shufflePlayers(plugin.server.worlds[if (args.isNotEmpty()) args[0].toInt() else 0])
            } catch (e: java.lang.IndexOutOfBoundsException) {
                sender.sendMessage("Index ${args[0]} is out bounds (there are ${plugin.server.worlds.size} worlds)")
                return false
            } catch (e: java.lang.NumberFormatException) {
                sender.sendMessage("${args.joinToString(separator = " ")} is not a correct id")
                return false
            }
            return true
        }
        return false
    }
}