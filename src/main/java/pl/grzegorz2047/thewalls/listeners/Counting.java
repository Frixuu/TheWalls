package pl.grzegorz2047.thewalls.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pl.grzegorz2047.thewalls.Counter;
import pl.grzegorz2047.thewalls.CountingEvent;
import pl.grzegorz2047.thewalls.GameUsers;
import pl.grzegorz2047.thewalls.scoreboard.ScoreboardAPI;

/**
 * Created by grzeg on 17.05.2016.
 */
public class Counting implements Listener {

    private final ScoreboardAPI scoreboardAPI;
    private final Server server;
    private GameUsers gameUsers;

    public Counting(ScoreboardAPI scoreboardAPI, GameUsers gameUsers) {
        this.scoreboardAPI = scoreboardAPI;
        this.gameUsers = gameUsers;
        this.server = Bukkit.getServer();
    }

    @EventHandler
    public void onCount(CountingEvent e) {
        Counter.CounterStatus status = e.getStatus();
        /*ArenaStatus.setLore(
                "\n§7§l> Time: §a" + TimeUtil.formatHHMMSS(e.getCounter().getTime()) +
                        "\n§7§l> Status: §a" + status.toString() +
                        "\n§7§l> §6Jako VIP mozna obserwowac arene!" +
                        "\n§7§l> §a1.7 - 1.10"
        );*/
        int time = e.getCounter().getTime();
        if (status.equals(Counter.CounterStatus.COUNTINGTOSTART)) {
            if (time == 10) {
                server.broadcastMessage("§7[§cWalls§7] Pozostalo 10 sekund do rozpoczecia rozgrywki");
            }
            if (time > 0 && time < 11) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2, 1);
                }
            }
            if (time < 6) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage("§7[§cWalls§7] Pozostalo " + time + " sekund do rozpoczecia rozgrywki");
                }
            }
        }
        if (status.equals(Counter.CounterStatus.COUNTINGTODROPWALLS)) {
            if (time == 600) {
                server.broadcastMessage("§7[§cWalls§7] Pozostalo 10 minut do opadniecia scian!");
            }
            if (time == 300) {
                server.broadcastMessage("§7[§cWalls§7] Pozostalo 5 minut do opadniecia scian!");
            }
            if (time == 60) {
                server.broadcastMessage("§7[§cWalls§7] Pozostalo 60 sekund do opadniecia scian!");
            }
            if (time == 30) {
                server.broadcastMessage("§7[§cWalls§7] Pozostalo 30 sekund do opadniecia scian!");
            }
            if (time == 20) {
                server.broadcastMessage("§7[§cWalls§7] Pozostalo 20 sekund do opadniecia scian!");
            }
        }
        if (status.equals(Counter.CounterStatus.COUNTINGTODM)) {
            if (time == 300) {
                server.broadcastMessage("§7[§cWalls§7]Pozostalo 5 min. do Death Match'u");
            }
            if (time == 60) {
                server.broadcastMessage("§7[§cWalls§7]Pozostala 1 min. do Death Match'u");
            }
            if (time > 0 && time < 11) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 2, 1);
                }
            }
        }
        if (status.equals(Counter.CounterStatus.DEATHMATCH)) {
            if (time == 300) {
                //5 min
            }
            if (time > 0 && time < 11) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 2, 1);
                }
            }
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            scoreboardAPI.updateDisplayName(time, p, gameUsers.getNumberOfPlayers());
        }
    }

}
