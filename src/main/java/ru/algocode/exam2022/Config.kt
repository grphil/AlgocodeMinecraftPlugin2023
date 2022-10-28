package ru.algocode.exam2022;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

public class Config {
    private static final int INVENTORY_ROW = 9;

    public List<ParametrizedItemStack> ChestItems;
    public List<ParametrizedItemStack> SpawnItems;

    public ShopMenu MerchantMenu;

    public int MaxTimeInForbiddenZone;
    public int TimeIncreaseForForbiddenZone;
    public int PenaltyForForbiddenZone;
    public int ProblemsCount;

    public int SpawnerX;
    public int SpawnerY;
    public int SpawnerZ;
    public int SpawnerRadius;
    public int TimeUntilNextKill;

    public int EjudgeContestId;
    public String ExternalXmlPath;

    public Stats StatsMultiplier;


    private SheetsAPI config;

    private List<ParametrizedItemStack> merchantItems;

    Config(JavaPlugin plugin) {
        config = new SheetsAPI(plugin.getConfig().getString("config_table_id"));
    }

    int ReloadConfig() {
        List<List<Object>> conf = config.get("Gameconfig!B1:B15");
        int newPlayersCount = Integer.parseInt((String) conf.get(0).get(0));

        int problemsLastRow = Integer.parseInt((String) conf.get(1).get(0));
        int potionsLastRow = Integer.parseInt((String) conf.get(2).get(0));
        int itemsLastRow = Integer.parseInt((String) conf.get(3).get(0));

        ProblemsCount = Integer.parseInt((String) conf.get(4).get(0));
        MaxTimeInForbiddenZone = Integer.parseInt((String) conf.get(5).get(0));
        TimeIncreaseForForbiddenZone = Integer.parseInt((String) conf.get(6).get(0));
        PenaltyForForbiddenZone = Integer.parseInt((String) conf.get(7).get(0));
        SpawnerX = Integer.parseInt((String) conf.get(8).get(0));
        SpawnerY = Integer.parseInt((String) conf.get(9).get(0));
        SpawnerZ = Integer.parseInt((String) conf.get(10).get(0));
        SpawnerRadius = Integer.parseInt((String) conf.get(11).get(0));
        TimeUntilNextKill = Integer.parseInt((String) conf.get(12).get(0));
        EjudgeContestId = Integer.parseInt((String) conf.get(13).get(0));
        ExternalXmlPath = (String) conf.get(14).get(0);

        List<List<Object>> multipliers = config.get("Score!A2:M2");

        StatsMultiplier = new Stats(multipliers.get(0));

        ChestItems = new ArrayList<>();
        merchantItems = new ArrayList<>();
        SpawnItems = new ArrayList<>();

        List<List<Object>> potions = config.get("Potions!A2:F" + potionsLastRow);
        for (List<Object> row : potions) {
            String potionName = (String) row.get(0);
            int potionTime = Integer.parseInt((String) row.get(1));
            int potionAmplifier = Integer.parseInt((String) row.get(2));
            int potionChest = Integer.parseInt((String) row.get(3));
            int potionPrice = Integer.parseInt((String) row.get(4));
            int potionSpawn = Integer.parseInt((String) row.get(5));

            ItemStack potion = ItemUtils.CreatePotion(potionName, potionTime, potionAmplifier);
            if (potion == null) {
                System.out.println("No potion named " + potionName);
                continue;
            }
            addItem(potion, potionChest, potionPrice, potionSpawn, 1);

        }

        List<List<Object>> items = config.get("Items!A2:E" + itemsLastRow);
        for (List<Object> row : items) {
            String itemName = (String) row.get(0);
            int itemChest = Integer.parseInt((String) row.get(1));
            int itemCount = Integer.parseInt((String) row.get(2));
            int itemPrice = Integer.parseInt((String) row.get(3));
            int itemSpawn = Integer.parseInt((String) row.get(4));

            ItemStack item = ItemUtils.CreateItem(itemName);
            if (item == null) {
                System.out.println("No item named " + itemName);
                continue;
            }

            addItem(item, itemChest, itemPrice, itemSpawn, itemCount);
        }

        List<List<Object>> problems = config.get("Problems!A2:K" + problemsLastRow);
        for (List<Object> row : problems) {
            String problemLabel = (String) row.get(0);
            int problemChest = Integer.parseInt((String) row.get(1));
            int problemPrice = Integer.parseInt((String) row.get(2));
            int problemSpawn = Integer.parseInt((String) row.get(3));

            String problemStatement = (String) row.get(4);
            String problemInput = (String) row.get(5);
            String problemOutput = (String) row.get(6);
            String problemSample1Input = (String) row.get(7);
            String problemSample1Output = (String) row.get(8);
            String problemSample2Input = "";
            String problemSample2Output = "";
            if (row.size() > 9) {
                problemSample2Input = (String) row.get(9);
                problemSample2Output = (String) row.get(10);
            }

            ItemStack book = ItemUtils.CreateProblemBook(
                    problemLabel,
                    problemStatement,
                    problemInput,
                    problemOutput,
                    problemSample1Input,
                    problemSample1Output,
                    problemSample2Input,
                    problemSample2Output
            );
            addItem(book, problemChest, problemPrice, problemSpawn, 1);
        }

        int needSize = (merchantItems.size() + INVENTORY_ROW - 1) / INVENTORY_ROW * INVENTORY_ROW;
        MerchantMenu = new ShopMenu("Торговец", needSize);
        int curPos = 0;
        for (ParametrizedItemStack item : merchantItems) {
            MerchantMenu.setOption(curPos++, item);
        }

        return newPlayersCount;
    }

    SheetsAPI GetSheetsApi() {
        return config;
    }

    private void addItem(ItemStack item, int chest, int price, int spawn, int count) {
        if (chest > 0) {
            ChestItems.add(new ParametrizedItemStack(item.clone(), chest, count));
        }

        if (price > 0) {
            merchantItems.add(new ParametrizedItemStack(item.clone(), price, count));
        }

        if (spawn > 0) {
            SpawnItems.add(new ParametrizedItemStack(item.clone(), spawn, count));
        }
    }
}
