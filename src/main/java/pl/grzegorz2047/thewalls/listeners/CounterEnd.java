package pl.grzegorz2047.thewalls.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pl.grzegorz2047.databaseapi.messages.MessageAPI;
import pl.grzegorz2047.thewalls.CounterEndEvent;
import pl.grzegorz2047.thewalls.GameData;
import pl.grzegorz2047.thewalls.TheWalls;
import pl.grzegorz2047.thewalls.playerclass.ClassManager;
import pl.grzegorz2047.thewalls.scoreboard.ScoreboardAPI;

/**
 * Created by grzeg on 17.05.2016.
 */
public class CounterEnd implements Listener {

    private final TheWalls plugin;
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
    }

    @EventHandler
    public void onCounterEnd(CounterEndEvent e) {
        switch (e.getStatus()) {
            case VOTED_COUNTING_TO_START:
                gameData.forceStartGame(scoreboardAPI, classManager);
                break;
            case COUNTINGTOSTART:
                gameData.startGame(scoreboardAPI, classManager);
                break;
            case COUNTINGTODROPWALLS:
                gameData.startFight();
                break;
            case COUNTINGTODM:
                gameData.startDeathMatch();
                break;
            case DEATHMATCH:
                gameData.restartGame("thewalls.nowinners");
                break;
            default:
                break;
        }
    }
}
