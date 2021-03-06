package pl.grzegorz2047.thewalls;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import pl.grzegorz2047.databaseapi.DatabaseAPI;
import pl.grzegorz2047.databaseapi.MoneyAPI;
import pl.grzegorz2047.databaseapi.StatsAPI;
import pl.grzegorz2047.databaseapi.messages.MessageAPI;
import pl.grzegorz2047.databaseapi.shop.ShopAPI;
import pl.grzegorz2047.thewalls.api.file.YmlFileHandler;
import pl.grzegorz2047.thewalls.commands.surface.SurfaceCommand;
import pl.grzegorz2047.thewalls.commands.team.TeamCommand;
import pl.grzegorz2047.thewalls.commands.tep.TepCommand;
import pl.grzegorz2047.thewalls.commands.vote.VoteCommand;
import pl.grzegorz2047.thewalls.commands.vote.Voter;
import pl.grzegorz2047.thewalls.commands.walls.WallsCommand;
import pl.grzegorz2047.thewalls.drop.BlockDrop;
import pl.grzegorz2047.thewalls.drop.Drop;
import pl.grzegorz2047.thewalls.drop.ExperienceDrop;
import pl.grzegorz2047.thewalls.drop.ItemDrop;
import pl.grzegorz2047.thewalls.listeners.*;
import pl.grzegorz2047.thewalls.playerclass.ClassManager;
import pl.grzegorz2047.thewalls.scoreboard.ScoreboardAPI;
import pl.grzegorz2047.thewalls.shop.Shop;
import pl.grzegorz2047.thewalls.threads.GeneralTask;

import java.util.*;

/**
 * Created by grzegorz2047 on 23.04.2016
 */
public class TheWalls extends JavaPlugin {

    private MoneyAPI moneyManager;
    private DatabaseAPI playerManager;
    private MessageAPI messageManager;
    private ScoreboardAPI scoreboardAPI;
    private StatsAPI statsAPI;
    private HashMap<String, String> settings = new HashMap<String, String>();
    private GameData gameData;
    private ShopAPI shopManager;
    private Shop shopMenuManager;
    private GameUsers gameUsers;
    private BossBarExtension bossBarExtension;
    private Voter voter;


    public ShopAPI getShopManager() {
        return shopManager;
    }

    @Override
    public void onEnable() {
        YmlFileHandler ymlFileHandler = new YmlFileHandler(this, "plugins/TheWalls", "config.yml");
        ymlFileHandler.load();
        FileConfiguration config = ymlFileHandler.getConfig();
        String loadedMotd = config.getString("motd");
        registerDbHandlers(config);
        Counter counter = new Counter(settings);
        voter = new Voter();
        String[] titles = {"Zapraszamy na ts.mc-walls.pl", "Wesprzyj nas na mc-walls.pl"};
        BossBarData bossBarData = new BossBarData(titles, new BarColor[]{BarColor.BLUE, BarColor.GREEN}, 60);
        BossBar bossBar = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SOLID);
        this.bossBarExtension = new BossBarExtension(bossBar, bossBarData);
        gameData = new GameData(this, counter, gameUsers, voter, this.bossBarExtension);

        Bukkit.getScheduler().runTaskTimer(this, new GeneralTask(gameData, counter, bossBarExtension), 0, 20l);

        scoreboardAPI = new ScoreboardAPI(messageManager, gameData);
        Map<Material, BlockDrop> dropsMap = getBlockDrops(config);
        StorageProtection storageProtection = new StorageProtection(gameUsers, messageManager);
        registerListeners(Bukkit.getPluginManager(), loadedMotd, dropsMap, new ClassManager(this), storageProtection);
        registerCommands();
        //ArenaStatus.initStatus(Bukkit.getMaxPlayers());
        //ArenaStatus.setStatus(//ArenaStatus.Status.WAITING);
        //ArenaStatus.setPlayers(0);
        //ArenaStatus.setLore("§7§l> §a1.7 - 1.10");
        System.out.println("thewalls zostaly wlaczone!");


    }

    private void registerDbHandlers(FileConfiguration config) {
        String dbUrl = config.getString("tw.url");
        int dbPort = config.getInt("tw.port");
        String dbName = config.getString("tw.dbName");
        String dbUser = config.getString("tw.user");
        String dbPassword = config.getString("tw.password");

        String theWallsStatsTableName = "TheWallsStats";
        String theWallsShopItemsTableName = "TheWallsShopItems";
        String theWallsShopItemsPurchasedTableName = "TheWallsShopItemsPurchased";
        String theWallsMoneyTableName = "TheWallsMoney";


        String minigameName = "TheWalls";

        moneyManager = new MoneyAPI(dbUrl, dbPort, dbName, dbUser, dbPassword);
        moneyManager.setMoneyTable(theWallsMoneyTableName);
        statsAPI = new StatsAPI(dbUrl, dbPort, dbName, dbUser, dbPassword, theWallsStatsTableName);
        playerManager = new DatabaseAPI(dbUrl, dbPort, dbName, dbUser, dbPassword);
        shopManager = new ShopAPI(dbUrl, dbPort, dbName, dbUser, dbPassword, theWallsShopItemsTableName, theWallsShopItemsPurchasedTableName);
        shopMenuManager = new Shop(shopManager);
        messageManager = new MessageAPI(dbUrl, dbPort, dbName, dbUser, dbPassword, minigameName);
        setSettings(playerManager.getSettings());
        gameUsers = new GameUsers(moneyManager, playerManager, statsAPI, shopManager);
    }

    private void registerCommands() {
        this.getCommand("team").setExecutor(new TeamCommand("team", new String[]{"team", "druzyna", "t", "d"}, gameUsers, gameData, messageManager));
        this.getCommand("wyjdz").setExecutor(new SurfaceCommand("wyjdz", new String[]{"wyjdz", "surface"}, gameData, messageManager, gameUsers));
        this.getCommand("walls").setExecutor(new WallsCommand("walls", new String[]{"walls", "thewalls"}, gameUsers, gameData, messageManager));
        this.getCommand("vote").setExecutor(new VoteCommand("vote", new String[]{"v", "vote", "glosuj"}, gameData));
        this.getCommand("tep").setExecutor(new TepCommand(this, gameUsers));
    }

    private Map<Material, BlockDrop> getBlockDrops(FileConfiguration config) {
        Map<Material, BlockDrop> dropsMap = new HashMap<>();
        Map<String, Object> drops = config.getConfigurationSection("drops").getValues(true);
        for (Map.Entry<String, Object> next : drops.entrySet()) {
            String itemDropingStr = next.getKey();
            itemDropingStr = itemDropingStr.replace("drops.", "");
            List<Object> values = (List<Object>) next.getValue();
            Material materialItem = Material.valueOf(itemDropingStr);
            List<Drop> dropsList = new ArrayList<>();
            for (Object property : values) {
                Map<String, Object> value = (LinkedHashMap<String, Object>) property;
                Material materialDrop = Material.COBBLESTONE;
                List<String> tools = new ArrayList<>();
                String message = "";
                String additionalActions;
                int quantity = 1;
                boolean isExp = false;
                int chance = 0;
                for (Map.Entry<String, Object> props : value.entrySet()) {
                    String propertiesKey = props.getKey();
                    Object propsValue = props.getValue();
                    if (propertiesKey.equalsIgnoreCase("drop")) {
                        if (propsValue.equals("xp")) {
                            isExp = true;
                            continue;
                        }
                        if (propsValue.equals("default")) {
                            continue;
                        }
                        materialDrop = Material.valueOf(String.valueOf(propsValue));
                    } else if (propertiesKey.equalsIgnoreCase("quantity")) {
                        quantity = Integer.parseInt(String.valueOf(propsValue));
                    } else if (propertiesKey.equalsIgnoreCase("tool")) {
                        tools = (List<String>) propsValue;
                    } else if (propertiesKey.equalsIgnoreCase("chance")) {
                        chance = (int) propsValue;
                    } else if (propertiesKey.equalsIgnoreCase("message")) {
                        message = (String) propsValue;
                    }
                }
                int chanceVal = chance != 0 ? chance : 100;
                Drop drop = createDrop(materialDrop, tools, message, quantity, isExp, chanceVal);
                dropsList.add(drop);
            }
            dropsMap.put(materialItem, new BlockDrop(materialItem, dropsList));

        }
        return dropsMap;
    }

    private Drop createDrop(Material materialDrop, List<String> tools, String message, int quantity, boolean isExp, int chanceVal) {
        if (isExp) {
            return new ExperienceDrop(tools, quantity, chanceVal);
        } else {
            return new ItemDrop(materialDrop, tools, quantity, chanceVal, message);
        }
    }

    private void registerListeners(PluginManager pluginManager, String loadedMotd, Map<Material, BlockDrop> dropsMap, ClassManager classManager, StorageProtection storageProtection) {

        pluginManager.registerEvents(new PlayerJoin(gameData, this.bossBarExtension), this);
        pluginManager.registerEvents(new PlayerQuit(this, this.bossBarExtension, voter), this);
        pluginManager.registerEvents(new PlayerLogin(playerManager, gameData, messageManager), this);
        pluginManager.registerEvents(new EntityExplode(this, dropsMap), this);
        pluginManager.registerEvents(new PlayerChat(settings, gameData, gameUsers), this);
        pluginManager.registerEvents(new PlayerDead(gameData, this), this);
        pluginManager.registerEvents(new CounterEnd(this, classManager), this);

        pluginManager.registerEvents(new GeneralBlocking(gameData, messageManager, gameUsers), this);
        pluginManager.registerEvents(new BlockPlace(gameData, messageManager, gameUsers, storageProtection), this);
        pluginManager.registerEvents(new PlayersDamaging(gameData, gameUsers), this);
        pluginManager.registerEvents(new BlockBreak(gameData, messageManager, dropsMap, gameUsers, storageProtection), this);
        pluginManager.registerEvents(new Counting(scoreboardAPI, gameUsers), this);
        pluginManager.registerEvents(new InventoryClick(), this);
        pluginManager.registerEvents(new ChooseItem(messageManager, gameData, shopMenuManager, scoreboardAPI, this.getShopManager(), classManager, gameUsers), this);
        pluginManager.registerEvents(new PlayerInteract(gameData, messageManager, shopMenuManager, classManager, storageProtection, gameUsers, scoreboardAPI), this);
        pluginManager.registerEvents(new ItemDropWatcher(gameData), this);
        pluginManager.registerEvents(new ServerMotd(gameData, loadedMotd), this);

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    @Override
    public void onDisable() {
        System.out.println("thewalls is off!");
    }


    public MoneyAPI getMoneyManager() {
        return moneyManager;
    }

    public DatabaseAPI getPlayerManager() {
        return playerManager;
    }

    public MessageAPI getMessageManager() {
        return messageManager;
    }


    public HashMap<String, String> getSettings() {
        return settings;
    }

    public void setSettings(HashMap<String, String> settings) {
        this.settings = settings;
    }

    public ScoreboardAPI getScoreboardAPI() {
        return scoreboardAPI;
    }

    public GameData getGameData() {
        return gameData;
    }


    public StatsAPI getStatsManager() {
        return statsAPI;
    }

    public Shop getShopMenuManager() {
        return shopMenuManager;
    }


}
