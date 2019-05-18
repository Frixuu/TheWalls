package pl.grzegorz2047.thewalls;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitScheduler;
import pl.grzegorz2047.databaseapi.*;
import pl.grzegorz2047.databaseapi.messages.MessageAPI;
import pl.grzegorz2047.databaseapi.shop.Item;
import pl.grzegorz2047.databaseapi.shop.ShopAPI;
import pl.grzegorz2047.databaseapi.shop.Transaction;
import pl.grzegorz2047.thewalls.api.exception.IncorrectDataStringException;
import pl.grzegorz2047.thewalls.api.util.*;
import pl.grzegorz2047.thewalls.permissions.PermissionAttacher;
import pl.grzegorz2047.thewalls.playerclass.ClassManager;
import pl.grzegorz2047.thewalls.scoreboard.ScoreboardAPI;
import pl.grzegorz2047.thewalls.shop.Shop;

import java.util.*;

/**
 * Created by grzeg on 16.05.2016.
 */
public class GameData {
    private final TheWalls plugin;
    private final MoneyAPI moneyManager;
    private final DatabaseAPI playerManager;
    private final MessageAPI messageManager;
    private final HashMap<String, String> settings;
    private final Shop shopMenuManager;
    private final ScoreboardAPI scoreboardAPI;
    private final HashMap<String, GameUser> gameUsers = new HashMap<>();
    private final ShopAPI shopManager;
    private Counter counter;
    private HashMap<GameTeam, ArrayList<String>> teams = new HashMap<GameTeam, ArrayList<String>>();
    private HashMap<Location, String> protectedFurnaces = new HashMap<Location, String>();
    private ClassManager classManager;
    private int minPlayers;
    private int maxTeamSize;
    private WorldManagement worldManagement;
    private int moneyForKill;
    private int moneyForWin;
    private int multiplier;
    private int expForKill;
    private int expForWin;
    private StartGameLocationLoader startGameLocationLoader;
    private final StatsAPI statsManager;

    public GameData(TheWalls plugin) {
        this.plugin = plugin;
        this.counter = new Counter(plugin.getSettings());
        HashMap<String, String> settings = plugin.getSettings();
        this.minPlayers = Integer.parseInt(settings.get("thewalls.minplayers"));
        maxTeamSize = Integer.parseInt(settings.get("thewalls.maxteamsize"));
        moneyForKill = Integer.parseInt(settings.get("thewalls.moneyforkill"));
        multiplier = Integer.parseInt(settings.get("thewalls.multiplier"));
        moneyForWin = multiplier * Integer.parseInt(settings.get("thewalls.moneyforwin"));
        expForKill = Integer.parseInt(settings.get("thewalls.expforkill"));
        expForWin = Integer.parseInt(settings.get("thewalls.expforwin"));
        initializeArrays();
        this.worldManagement = new WorldManagement();
        //WorldManagement.loadWorld(getSettings().get("thewalls.map.path"));
        int numberOfMaps = Integer.parseInt(settings.get("thewalls.numberofmaps"));
        worldManagement.loadWorld(numberOfMaps);
        worldManagement.disableSaving();
        classManager = new ClassManager(plugin);
        startGameLocationLoader = new StartGameLocationLoader(plugin.getSettings()).loadSpawns(worldManagement);

        moneyManager = plugin.getMoneyManager();
        playerManager = plugin.getPlayerManager();
        messageManager = plugin.getMessageManager();
        this.settings = plugin.getSettings();
        shopMenuManager = plugin.getShopMenuManager();
        statsManager = plugin.getStatsManager();

        scoreboardAPI = plugin.getScoreboardAPI();
        shopManager = plugin.getShopManager();
    }

    private GameStatus status = GameStatus.WAITING;

    public boolean isStatus(GameStatus gameStatus) {
        return status.equals(gameStatus);
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public HashMap<GameTeam, ArrayList<String>> getTeams() {
        return teams;
    }

    public Counter getCounter() {
        return counter;
    }

    public int getMaxTeamSize() {
        return maxTeamSize;
    }

    public int getMoneyForKill() {
        return moneyForKill;
    }

    public int getMoneyForWin() {
        return moneyForWin;
    }

    public int getExpForKill() {
        return expForKill;
    }

    public int getExpForWin() {
        return expForWin;
    }

    public HashMap<Location, String> getProtectedFurnaces() {
        return protectedFurnaces;
    }

    public GameUser getGameUser(String username) {
        return gameUsers.get(username);
    }

    public ArrayList<String> getTeam(GameTeam team) {
        return teams.get(team);
    }

    public void removeFromTeam(String username, GameTeam assignedTeam) {
        teams.get(assignedTeam).remove(username);
    }

    public void addPlayerToTeam(String username, GameTeam team) {
        teams.get(team).add(username);
    }

    public int getTeamSize(GameTeam team) {
        return teams.get(team).size();
    }

    public String getPlayerProtectedFurnace(Location location) {
        return protectedFurnaces.get(location);
    }

    public GameUser addGameUser(String playerName) {
        moneyManager.insertPlayer(playerName);
        statsManager.insertPlayer(playerName);
        int money = moneyManager.getPlayer(playerName);
        //plugin.getPlayerManager().changePlayerExp(p.getName(), 100);
        SQLUser user = playerManager.getPlayer(playerName);
        StatsUser statsUser = statsManager.getPlayer(playerName);
        List<Transaction> transactions = shopManager.getPlayerItems(playerName);
        GameUser gameUser = new GameUser(user, statsUser, transactions, money);
        gameUsers.put(playerName, gameUser);
        return gameUser;
    }

    public boolean isArenaEmpty() {
        return gameUsers.size() == 0;
    }

    public String getLoadedWorldName() {
        return worldManagement.getLoadedWorld().getName();
    }

    public GameUser removePlayerFromGame(Player p) {
        return gameUsers.remove(p.getName());
    }

    public String getCurrentStatusLabel() {
        return status.toString();
    }

    public Set<Map.Entry<String, GameUser>> getArenaUsers() {
        return gameUsers.entrySet();
    }

    public int getNumberOfPlayers() {
        return gameUsers.size();
    }

    public enum GameTeam {
        TEAM1("§a"),
        TEAM2("§b"),
        TEAM3("§c"),
        TEAM4("§e");

        private String color = "§7";

        GameTeam(String teamColor) {
            this.color = teamColor;
        }

        public String getColor() {
            return color;
        }
    }

    public enum GameStatus {
        WAITING,
        INGAME,
        STARTING,
        RESTARTING
    }

    public void restartGame() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.kickPlayer("Arena przygotowuje sie do nowej gry!");
        }
        worldManagement.reloadLoadedWorld(settings.get("thewalls.numberofmaps"));
        startGameLocationLoader = new StartGameLocationLoader(settings).loadSpawns(worldManagement);
        initializeArrays();
        status = GameStatus.WAITING;
        this.counter.cancel();
        /*ArenaStatus.setStatus(ArenaStatus.Status.WAITING);
        ArenaStatus.setLore(
                "\n§7§l> §a1.7 - 1.10"
        );*/

    }

    private void initializeArrays() {
        for (GameTeam team : GameTeam.values()) {
            teams.put(team, new ArrayList<String>());
        }
    }

    public void checkToStart() {
        int numberOfPlayers = Bukkit.getOnlinePlayers().size();
        if (this.isStatus(GameStatus.WAITING)) {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            System.out.println("Godzina jest " + hour);
            if (hour < 13 || hour > 21) {
                this.minPlayers = Integer.parseInt(settings.get("thewalls.minplayersearly"));
            } else {
                this.minPlayers = Integer.parseInt(settings.get("thewalls.minplayers"));
            }
            if (numberOfPlayers >= this.minPlayers) {
                getCounter().start(Counter.CounterStatus.COUNTINGTOSTART);
                this.setStatus(GameStatus.STARTING);
                broadcastToAllPlayers("thewalls.countingstarted");
                //ArenaStatus.setStatus(//ArenaStatus.Status.STARTING);
            }
        } else if (this.isStatus(GameStatus.STARTING)) {
            if (numberOfPlayers < this.minPlayers) {
                this.setStatus(GameStatus.WAITING);
                broadcastToAllPlayers("thewalls.countingcancelled");
                this.counter.cancel();
            }
        }
    }

    private void broadcastToAllPlayers(String path) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            broadcastMessageToPlayer(p, path);
        }
    }

    private void broadcastMessageToPlayer(Player p, String path) {
        SQLUser user = gameUsers.get(p.getName());
        String message = messageManager.getMessage(user.getLanguage(), path);
        p.sendMessage(message);
    }


    public static String getTeamColor(GameTeam t) {
        if (t.equals(GameTeam.TEAM1)) {
            return "§a";
        }
        if (t.equals(GameTeam.TEAM2)) {
            return "§b";
        }
        if (t.equals(GameTeam.TEAM3)) {
            return "§c";
        }
        if (t.equals(GameTeam.TEAM4)) {
            return "§e";
        }
        return "§7";
    }

    public void startGame() {
        //ArenaStatus.setStatus(//ArenaStatus.Status.INGAME);
        this.getWorldManagement().setProtected(true);

        String expGiveMsgPL = messageManager.getMessage("PL", "thewalls.exp.giveinfo");
        String expGiveMsgEN = messageManager.getMessage("EN", "thewalls.exp.giveinfo");

        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerInventory userInventory = p.getInventory();
            userInventory.clear();
            String username = p.getName();
            GameUser user = gameUsers.get(username);
            String userRank = user.getRank();

            assignUnassigedToTeams(p, user);

            p.teleport(startGameLocationLoader.getStartLocation(user.getAssignedTeam()));
            if (!userRank.equals("Gracz")) {
                p.setLevel(5);
                String userLanguage = user.getLanguage();
                if (userLanguage.equals("PL")) {
                    p.sendMessage(expGiveMsgPL);
                } else if (userLanguage.equals("EN")) {
                    p.sendMessage(expGiveMsgEN);
                } else {
                    p.sendMessage(messageManager.getMessage(userLanguage, "thewalls.exp.giveinfo"));
                }
            }
            userInventory.addItem(new ItemStack(Material.LAPIS_ORE, 1));
            ItemStack netherStar = CreateItemUtil.createItem(Material.NETHER_STAR, "§6Sklep");
            userInventory.setItem(8, netherStar);
            scoreboardAPI.createIngameScoreboard(p, user);
            this.setStatus(GameStatus.INGAME);
            //ArenaStatus.setStatus(//ArenaStatus.Status.INGAME);
            givePermItems(p, user);
            classManager.givePlayerClass(p, user);
        }
        refreshScoreboardToAll();
        this.counter.start(Counter.CounterStatus.COUNTINGTODROPWALLS);
    }

    private void refreshScoreboardToAll() {
        for (Player pl : Bukkit.getOnlinePlayers()) {
            scoreboardAPI.refreshTags(pl);
        }
    }


    private void assignUnassigedToTeams(Player p, GameUser user) {
        GameTeam assignedTeam = user.getAssignedTeam();
        if (assignedTeam == null) {
            boolean found = false;
            if (Bukkit.getOnlinePlayers().size() >= 20) {
                for (Map.Entry<GameTeam, ArrayList<String>> entry : getTeams().entrySet()) {
                    if (entry.getValue().size() < this.maxTeamSize) {
                        user.setAssignedTeam(entry.getKey());
                        entry.getValue().add(p.getName());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    getTeams().get(GameTeam.TEAM4).add(p.getName());
                    user.setAssignedTeam(GameTeam.TEAM4);
                }
            } else {
                if (teams.get(GameTeam.TEAM1).size() < this.maxTeamSize) {
                    teams.get(GameTeam.TEAM1).add(p.getName());
                    user.setAssignedTeam(GameTeam.TEAM1);
                } else {
                    teams.get(GameTeam.TEAM2).add(p.getName());
                    user.setAssignedTeam(GameTeam.TEAM2);
                }
            }
            ColoringUtil.colorPlayerTab(p, assignedTeam.getColor());
        }
    }


    private void givePermItems(Player p, GameUser user) {
        for (Transaction t : user.getTransactions()) {
            Item perm = shopMenuManager.getNormalItems().get(t.getItemid());
            if (perm != null) {
                p.getInventory().addItem(perm.toItemStack());
            }
        }
        broadcastMessageToPlayer(p, "shop.givenpermitems");
    }

    public void startFight() {

        this.getWorldManagement().removeWalls();
        this.counter.start(Counter.CounterStatus.COUNTINGTODM);
        fixInvisiblePlayers();
    }

    private void fixInvisiblePlayers() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            for (Player pl : Bukkit.getOnlinePlayers()) {
                p.hidePlayer(pl);
                p.showPlayer(pl);
                pl.hidePlayer(p);
                pl.showPlayer(p);
            }
        }
    }

    public void startDeathMatch() {
        try {
            String worldName = this.getWorldManagement().getLoadedWorld().getName();
            Location dmloc1 = getTeamDmSpawn(worldName, 1);
            Location dmloc2 = getTeamDmSpawn(worldName, 2);
            Location dmloc3 = getTeamDmSpawn(worldName, 3);
            Location dmloc4 = getTeamDmSpawn(worldName, 4);

            for (Player p : Bukkit.getOnlinePlayers()) {
                HashMap<String, GameUser> gameUsers = this.gameUsers;
                GameUser user = gameUsers.get(p.getName());
                if (user.getAssignedTeam() == null) {
                    p.teleport(dmloc1);
                    continue;
                }
                teleportToDeathMatch(dmloc1, dmloc2, dmloc3, dmloc4, p, user);
            }
            this.setStatus(GameStatus.INGAME);
            //ArenaStatus.setStatus(//ArenaStatus.Status.INGAME);
        } catch (IncorrectDataStringException e) {
            e.printStackTrace();
        }
        this.counter.start(Counter.CounterStatus.DEATHMATCH);
    }

    private Location getTeamDmSpawn(String worldName, int i) throws IncorrectDataStringException {
        return LocationUtil.entityStringToLocation(worldName, settings.get("thewalls.spawns.dm.team." + i));
    }

    private void teleportToDeathMatch(Location dmloc1, Location dmloc2, Location dmloc3, Location dmloc4, Player p, GameUser user) {
        GameTeam assignedTeam = user.getAssignedTeam();
        if (assignedTeam.equals(GameTeam.TEAM1)) {
            p.teleport(dmloc1);
        } else if (assignedTeam.equals(GameTeam.TEAM2)) {
            p.teleport(dmloc2);
        } else if (assignedTeam.equals(GameTeam.TEAM3)) {
            p.teleport(dmloc3);
        } else if (assignedTeam.equals(GameTeam.TEAM4)) {
            p.teleport(dmloc4);
        }
    }

    public void makePlayerSpectator(GameUser gameUser, Player p, String worldName) {
        gameUser.setSpectator(true);
        p.setGameMode(GameMode.SPECTATOR);
        p.setAllowFlight(true);
        p.setFlying(true);
        PermissionAttacher.attachSpectatorPermissions(p, worldName);
        if (gameUser.getAssignedTeam() != null) {
            this.getTeams().get(gameUser.getAssignedTeam()).remove(p.getName());
            gameUser.setAssignedTeam(null);
        }
    }

    public HashMap<String, GameUser> getGameUsers() {
        return gameUsers;
    }


    public WorldManagement getWorldManagement() {
        return worldManagement;
    }

    public void checkWinners() {
        int alive = 0;
        GameTeam team = null;
        Set<Map.Entry<GameTeam, ArrayList<String>>> teams = this.getTeams().entrySet();
        for (Map.Entry<GameTeam, ArrayList<String>> iteratedTeamData : teams) {

            ArrayList<String> teamList = iteratedTeamData.getValue();
            if (teamList.size() > 0) {
                alive++;
                team = iteratedTeamData.getKey();
            }
        }
        BukkitScheduler scheduler = Bukkit.getScheduler();
        if (alive == 0) {
            scheduler.runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        //nikt nie wygral
                        BungeeUtil.changeServer(plugin, p, "Lobby1");
                    }
                    status = GameStatus.RESTARTING;
                }
            }, 20 * 3);

            scheduler.runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    restartGame();
                }
            }, 20 * 10);

        } else if (alive == 1) {
            final GameTeam finalTeam = team;
            int moveEndPlCooldown = 20 * 3;
            scheduler.runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    for (Map.Entry<String, GameUser> user : getGameUsers().entrySet()) {
                        Player p = Bukkit.getPlayer(user.getKey());
                        GameUser gameUser = user.getValue();
                        String teamName = finalTeam.name().toLowerCase();
                        p.sendMessage(messageManager.getMessage(gameUser.getLanguage(), "thewalls.game.win." + teamName));
                        giveMoneyToWinners(user.getValue(), user.getKey());
                        BungeeUtil.changeServer(plugin, p, "Lobby1");
                    }
                    status = GameStatus.RESTARTING;
                }
            }, moveEndPlCooldown);


            int restartCooldown = 20 * 6;
            scheduler.runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    restartGame();
                }
            }, restartCooldown);

        }
    }


    private void giveMoneyToWinners(GameUser user, String username) {
        if (user.getAssignedTeam() != null) {
            statsManager.increaseValueBy(username, "wins", 1);
            if (user.getRank().equals("Gracz")) {
                moneyManager.changePlayerMoney(username, Integer.parseInt(settings.get("thewalls.moneyforwin")));//TODO
            } else {
                moneyManager.changePlayerMoney(username, 2 * Integer.parseInt(settings.get("thewalls.moneyforwin")));//TODO
            }
            playerManager.changePlayerExp(username, getExpForWin());
        }
    }

    public ClassManager getClassManager() {
        return classManager;
    }


}