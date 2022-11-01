@file:Suppress("DEPRECATION")

package ru.algocode.exam2022.utils

import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

object ItemUtils {
    private const val maxPageLength = 160
    fun CreatePotion(name: String?, maxDuration: Int, amplifier: Int): ItemStack? {
        val potion = ItemStack(Material.POTION)
        val data = (potion.itemMeta as PotionMeta)
        data.color = Color.PURPLE
        val duration = 20 * (10 + Utils.random.nextInt(maxDuration))
        val type = PotionEffectType.getByName(name!!) ?: return null
        data.addCustomEffect(PotionEffect(type, duration, amplifier), true)
        data.setDisplayName(ChatColor.GOLD.toString() + "Алгодопинг")
        potion.itemMeta = data
        return potion
    }

    fun CreateItem(name: String?): ItemStack? {
        if (name == "LOCATOR") {
            val item = ItemStack(Material.BLAZE_ROD)
            val meta = item.itemMeta!!
            meta.setDisplayName(ChatColor.RED.toString() + "Локатор соперников")
            item.itemMeta = meta
            return item
        } else if (name == "SHUFFLE_BALL") {
            return ItemStack(Material.SNOWBALL).apply {
                itemMeta = itemMeta.apply {
                    displayName(Component.text("SHUFFLE BALL"))
                }
            }
        } else {
            val material = Material.getMaterial(name!!) ?: return null
            return ItemStack(material)
        }
    }

    fun CreateProblemBook(
        problemLabel: String,
        statement: String,
        input: String,
        output: String,
        sample1Input: String,
        sample1Output: String,
        sample2Input: String,
        sample2Output: String
    ): ItemStack {
        val book = ItemStack(Material.WRITTEN_BOOK)
        val data = (book.itemMeta as BookMeta)
        data.title = ChatColor.GREEN.toString() + "Задача " + problemLabel
        data.author = ChatColor.RED.toString() + "grphil & peltorator"
        val pages: MutableList<String> = ArrayList()
        AddPages(pages, "Условие\n$statement")
        AddPages(pages, "Входные данные\n$input")
        AddPages(pages, "Выходные данные\n$output")
        AddPages(pages, "Пример1. Ввод\n$sample1Input")
        AddPages(pages, "Пример1. Вывод\n$sample1Output")
        if (sample2Input.length > 0) {
            AddPages(pages, "Пример2. Ввод\n$sample2Input")
            AddPages(pages, "Пример2. Вывод\n$sample2Output")
        }
        data.pages = pages
        book.itemMeta = data
        return book
    }

    fun AddPages(pages: MutableList<String>, text: String) {
        var pageBuffer = StringBuilder()
        for (word in text.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            if (pageBuffer.length + 1 + word.length > maxPageLength) {
                pages.add(pageBuffer.toString())
                pageBuffer = StringBuilder()
            } else {
                pageBuffer.append(" ")
            }
            pageBuffer.append(word)
        }
        if (pageBuffer.length > 0) {
            pages.add(pageBuffer.toString())
        }
    }
}