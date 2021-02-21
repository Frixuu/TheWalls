package pl.grzegorz2047.thewalls.listeners;

import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pl.grzegorz2047.databaseapi.messages.MessageAPI;
import pl.grzegorz2047.thewalls.*;
import pl.grzegorz2047.thewalls.playerclass.ClassManager;
import pl.grzegorz2047.thewalls.scoreboard.ScoreboardAPI;

/**
 * Created by grzeg on 17.05.2016.
 */
public class CounterEnd implements Listener {

    private final TheWalls plugin;
    private final Server server;
    private final ScoreboardAPI scoreboardAPI;
    private final GameData gameData;
    private final MessageAPI messageManager;
    private final ClassManager classManager;

    public CounterEnd(TheWalls plugin, ClassManager classManager) {
        this.plugin = plugin;
        scoreboardAPI = plugin.getScoreboardAPI();
        gameData = plugin.getGameData();
        messageManager = plugin.getMessageManager();
        this.classManager = classManager;
        this.server = plugin.getServer();
    }

    @EventHandler
    public void onCounterEnd(CounterEndEvent e) {
        Counter.CounterStatus status = e.getStatus();
        switch (status) {
            case VOTED_COUNTING_TO_START:
                gameData.forceStartGame(scoreboardAPI, classManager);
                return;
            case COUNTINGTOSTART:
                gameData.startGame(scoreboardAPI, classManager);
                return;
            case COUNTINGTODROPWALLS:
                server.broadcastMessage("§7[§cWalls§7]Sciany opadly, bitwa rozpoczeta. Powodzenia!");
                gameData.startFight();
                return;
            case COUNTINGTODM:
                server.broadcastMessage("§7[§cWalls§7]Deatmatch zostal rozpoczety! Powodzenia.");
                gameData.startDeathMatch();
                return;
            case DEATHMATCH:
                gameData.restartGame("thewalls.nowinners");
                return;
        }
    }
}
