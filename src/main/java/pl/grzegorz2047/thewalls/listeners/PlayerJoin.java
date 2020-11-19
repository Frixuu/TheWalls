package pl.grzegorz2047.thewalls.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import pl.grzegorz2047.thewalls.BossBarExtension;
import pl.grzegorz2047.thewalls.GameData;

/**
 * Created by grzegorz2047 on 23.04.2016
 */
public class PlayerJoin implements Listener {

    private final GameData gameData;
    private final BossBarExtension bossBarExtension;

    public PlayerJoin(GameData gameData, BossBarExtension bossBarExtension) {
        this.gameData = gameData;
        this.bossBarExtension = bossBarExtension;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        e.setJoinMessage(null);
        bossBarExtension.addToBossBar(e.getPlayer());
        gameData.addPlayerToArena(e.getPlayer());
    }
}
