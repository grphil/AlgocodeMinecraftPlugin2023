package ru.algocode.exam2022

import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import ru.algocode.exam2022.utils.ItemUtils
import ru.algocode.exam2022.utils.SheetsAPI
import ru.algocode.exam2022.utils.Stats

class Config internal constructor(plugin: JavaPlugin) {
    var chestItems: MutableList<ParametrizedItemStack>? = null
    var spawnItems: MutableList<ParametrizedItemStack>? = null
    var merchantMenu: ShopMenu? = null
    var maxTimeInForbiddenZone = 0
    var timeIncreaseForForbiddenZone = 0
    var penaltyForForbiddenZone = 0
    var problemsCount = 0
    var spawnerX = 0
    var spawnerY = 0
    var spawnerZ = 0
    var spawnerRadius = 0
    var timeUntilNextKill = 0
    var ejudgeContestId = 0
    var externalXmlPath: String? = null
    var statsMultiplier: Stats? = null
    private val config: SheetsAPI
    private var merchantItems: MutableList<ParametrizedItemStack>? = null

    init {
        config = SheetsAPI(plugin.config.getString("config_table_id")!!, plugin)
    }

    fun ReloadConfig(): Int {
        val conf = config["Gameconfig!B1:B15"]
        val newPlayersCount: Int = (conf!![0][0] as String?)!!.toInt()
        val problemsLastRow: Int = (conf[1][0] as String?)!!.toInt()
        val potionsLastRow: Int = (conf[2][0] as String?)!!.toInt()
        val itemsLastRow: Int = (conf[3][0] as String?)!!.toInt()
        problemsCount = (conf[4][0] as String?)!!.toInt()
        maxTimeInForbiddenZone = (conf[5][0] as String?)!!.toInt()
        timeIncreaseForForbiddenZone = (conf[6][0] as String?)!!.toInt()
        penaltyForForbiddenZone = (conf[7][0] as String?)!!.toInt()
        spawnerX = (conf[8][0] as String?)!!.toInt()
        spawnerY = (conf[9][0] as String?)!!.toInt()
        spawnerZ = (conf[10][0] as String?)!!.toInt()
        spawnerRadius = (conf[11][0] as String?)!!.toInt()
        plugin.borderApi.replaceBorder(
            spawnerX.toLong(),
            spawnerZ.toLong(),
            spawnerRadius.toLong()
        )
        timeUntilNextKill = (conf[12][0] as String?)!!.toInt()
        ejudgeContestId = (conf[13][0] as String?)!!.toInt()
        externalXmlPath = (conf[14][0] as String)
        val multipliers = config["Score!A2:M2"]
        statsMultiplier = Stats(multipliers!![0])
        chestItems = ArrayList()
        merchantItems = ArrayList()
        spawnItems = ArrayList()
        val potions = config["Potions!A2:F$potionsLastRow"]
        for (row in potions!!) {
            val potionName = (row[0] as String)
            val potionTime: Int = (row[1] as String?)!!.toInt()
            val potionAmplifier: Int = (row[2] as String?)!!.toInt()
            val potionChest: Int = (row[3] as String?)!!.toInt()
            val potionPrice: Int = (row[4] as String?)!!.toInt()
            val potionSpawn: Int = (row[5] as String?)!!.toInt()
            val potion = ItemUtils.CreatePotion(potionName, potionTime, potionAmplifier)
            if (potion == null) {
                println("No potion named $potionName")
                continue
            }
            addItem(potion, potionChest, potionPrice, potionSpawn, 1)
        }
        val items = config["Items!A2:E$itemsLastRow"]
        for (row in items!!) {
            val itemName = (row[0] as String)
            val itemChest: Int = (row[1] as String?)!!.toInt()
            val itemCount: Int = (row[2] as String?)!!.toInt()
            val itemPrice: Int = (row[3] as String?)!!.toInt()
            val itemSpawn: Int = (row[4] as String?)!!.toInt()
            val item = ItemUtils.CreateItem(itemName)
            if (item == null) {
                println("No item named $itemName")
                continue
            }
            addItem(item, itemChest, itemPrice, itemSpawn, itemCount)
        }
        val problems: List<List<Any?>>? = config["Problems!A2:K$problemsLastRow"]
        for (row in problems!!) {
            val problemLabel = (row[0] as String?)!!
            val problemChest: Int = (row[1] as String?)!!.toInt()
            val problemPrice: Int = (row[2] as String?)!!.toInt()
            val problemSpawn: Int = (row[3] as String?)!!.toInt()
            val problemStatement = (row[4] as String?)!!
            val problemInput = (row[5] as String?)!!
            val problemOutput = (row[6] as String?)!!
            val problemSample1Input = (row[7] as String?)!!
            val problemSample1Output = (row[8] as String?)!!
            var problemSample2Input = ""
            var problemSample2Output = ""
            if (row.size > 9) {
                problemSample2Input = (row[9] as String?)!!
                problemSample2Output = (row[10] as String?)!!
            }
            val book = ItemUtils.CreateProblemBook(
                problemLabel,
                problemStatement,
                problemInput,
                problemOutput,
                problemSample1Input,
                problemSample1Output,
                problemSample2Input,
                problemSample2Output
            )
            addItem(book, problemChest, problemPrice, problemSpawn, 1)
        }
        val needSize = (merchantItems!!.size + INVENTORY_ROW - 1) / INVENTORY_ROW * INVENTORY_ROW
        merchantMenu = ShopMenu("Торговец", needSize)
        var curPos = 0
        for (item in merchantItems!!) {
            merchantMenu!!.setOption(curPos++, item)
        }
        return newPlayersCount
    }

    fun GetSheetsApi(): SheetsAPI {
        return config
    }

    private fun addItem(item: ItemStack, chest: Int, price: Int, spawn: Int, count: Int) {
        if (chest > 0) {
            chestItems!!.add(ParametrizedItemStack(item.clone(), chest, count))
        }
        if (price > 0) {
            merchantItems!!.add(ParametrizedItemStack(item.clone(), price, count))
        }
        if (spawn > 0) {
            spawnItems!!.add(ParametrizedItemStack(item.clone(), spawn, count))
        }
    }

    companion object {
        private const val INVENTORY_ROW = 9
    }
}