package pl.grzegorz2047.thewalls.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import pl.grzegorz2047.databaseapi.DatabaseAPI;
import pl.grzegorz2047.databaseapi.SQLUser;
import pl.grzegorz2047.databaseapi.messages.MessageAPI;
import pl.grzegorz2047.thewalls.GameData;

/**
 * Created by grzeg on 16.05.2016.
 */
public class PlayerLogin implements Listener {

    private final DatabaseAPI playerManager;
    private final GameData gameData;
    private final MessageAPI messageManager;

    public PlayerLogin(DatabaseAPI playerManager, GameData gameData, MessageAPI messageManager) {
        this.playerManager = playerManager;
        this.gameData = gameData;
        this.messageManager = messageManager;
    }

    @EventHandler
    void onPlayerLogin(PlayerLoginEvent e) {
        Player p = e.getPlayer();

        playerManager.insertPlayer(p.getName(), "127.0.0.1");
        SQLUser user = playerManager.getPlayer(p.getName());
        PlayerLoginEvent.Result kickOtherResult = PlayerLoginEvent.Result.KICK_OTHER;

        String userLanguage = user.getLanguage();
        if (gameData.isStatus(GameData.GameStatus.RESTARTING)) {
            e.disallow(kickOtherResult, messageManager.getMessage(userLanguage, "thewalls.login.restarting"));
            return;
        }

        String userRank = user.getRank();
        if (!userRank.equals("Gracz") && !userRank.equals("Vip") && !userRank.equals("Youtube") && !userRank.equals("miniYT")) {
            e.allow();
            return;
        }

        if (userRank.equals("Gracz")) {
            if (gameData.isStatus(GameData.GameStatus.INGAME)) {
                e.disallow(kickOtherResult, messageManager.getMessage(userLanguage, "thewalls.login.notspectator"));
            } else {
                if ((Bukkit.getMaxPlayers() - Bukkit.getOnlinePlayers().size()) < 5) {
                    e.disallow(kickOtherResult, messageManager.getMessage(userLanguage, "thewalls.login.vipslots"));
                }
            }
        }
    }
}
