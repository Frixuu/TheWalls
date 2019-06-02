package pl.grzegorz2047.thewalls;

import org.bukkit.Bukkit;
import org.bukkit.World;
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
import pl.grzegorz2047.thewalls.commands.walls.WallsCommand;
import pl.grzegorz2047.thewalls.listeners.*;
import pl.grzegorz2047.thewalls.scoreboard.ScoreboardAPI;
import pl.grzegorz2047.thewalls.shop.Shop;
import pl.grzegorz2047.thewalls.threads.GeneralTask;

import java.util.HashMap;

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


    public ShopAPI getShopManager() {
        return shopManager;
    }

    @Override
    public void onEnable() {
        YmlFileHandler ymlFileHandler = new YmlFileHandler(this, "plugins/TheWalls", "config.yml");
        ymlFileHandler.load();
        FileConfiguration config = ymlFileHandler.getConfig();

        String dbUrl = config.getString("tw.url");
        int dbPort = config.getInt("tw.port");
        String dbName = config.getString("tw.dbName");
        String dbUser = config.getString("tw.user");
        String dbPassword = config.getString("tw.password");

        String theWallsStatsTableName = "TheWallsStats";
        String theWallsShopItemsTableName = "TheWallsShopItems";
        String theWallsShopItemsPurchasedTableName = "TheWallsShopItemsPurchased";
        String theWallsMoneyTableName = "TheWallsMoney";

        String loadedMotd = config.getString("motd");
        String minigameName = "TheWalls";

        moneyManager = new MoneyAPI(dbUrl, dbPort, dbName, dbUser, dbPassword);
        moneyManager.setMoneyTable(theWallsMoneyTableName);
        statsAPI = new StatsAPI(dbUrl, dbPort, dbName, dbUser, dbPassword, theWallsStatsTableName);
        playerManager = new DatabaseAPI(dbUrl, dbPort, dbName, dbUser, dbPassword);
        shopManager = new ShopAPI(dbUrl, dbPort, dbName, dbUser, dbPassword, theWallsShopItemsTableName, theWallsShopItemsPurchasedTableName);
        shopMenuManager = new Shop(shopManager);
        messageManager = new MessageAPI(dbUrl, dbPort, dbName, dbUser, dbPassword, minigameName);
        setSettings(playerManager.getSettings());
        Bukkit.getScheduler().runTaskTimer(this, new GeneralTask(this), 0, 20l);
        gameData = new GameData(this);
        scoreboardAPI = new ScoreboardAPI(messageManager, gameData);
        World loadedWorld = gameData.getWorldManagement().getLoadedWorld();
        registerListeners(Bukkit.getPluginManager(), loadedMotd, loadedWorld);
        this.getCommand("team").setExecutor(new TeamCommand("team", new String[]{"team", "druzyna", "t", "d"}, this));
        this.getCommand("wyjdz").setExecutor(new SurfaceCommand("wyjdz", new String[]{"wyjdz", "surface"}, this, gameData, messageManager));
        this.getCommand("walls").setExecutor(new WallsCommand("walls", new String[]{"walls", "thewalls"}, this));
        this.getCommand("tep").setExecutor(new TepCommand(this));
        //ArenaStatus.initStatus(Bukkit.getMaxPlayers());
        //ArenaStatus.setStatus(//ArenaStatus.Status.WAITING);
        //ArenaStatus.setPlayers(0);
        //ArenaStatus.setLore("§7§l> §a1.7 - 1.10");
        System.out.println("thewalls zostaly wlaczone!");


    }

    private void registerListeners(PluginManager pluginManager, String loadedMotd, World loadedWorld) {

        pluginManager.registerEvents(new PlayerJoin(gameData, loadedWorld), this);
        pluginManager.registerEvents(new PlayerQuit(this), this);
        pluginManager.registerEvents(new PlayerLogin(playerManager, gameData, messageManager), this);
        pluginManager.registerEvents(new EntityExplode(this), this);
        pluginManager.registerEvents(new PlayerChat(settings, gameData), this);
        pluginManager.registerEvents(new PlayerDead(gameData), this);
        pluginManager.registerEvents(new CounterEnd(this), this);

        pluginManager.registerEvents(new GeneralBlocking(gameData, messageManager), this);
        pluginManager.registerEvents(new BlockPlace(gameData, messageManager), this);
        pluginManager.registerEvents(new PlayersDamaging(gameData), this);
        pluginManager.registerEvents(new BlockBreak(gameData, messageManager), this);
        pluginManager.registerEvents(new Counting(scoreboardAPI), this);
        pluginManager.registerEvents(new InventoryClick(), this);
        pluginManager.registerEvents(new ChooseItem(messageManager, gameData, shopMenuManager, scoreboardAPI, moneyManager, this.getShopManager()), this);
        pluginManager.registerEvents(new PlayerInteract(gameData, messageManager, shopMenuManager), this);
        pluginManager.registerEvents(new ItemDrop(gameData), this);
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
