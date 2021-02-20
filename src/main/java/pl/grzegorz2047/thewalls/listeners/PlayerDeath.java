package pl.grzegorz2047.thewalls.listeners;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import pl.grzegorz2047.thewalls.GameData;
import pl.grzegorz2047.thewalls.GameData.GameStatus;

import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.CUSTOM;
import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.ENTITY_ATTACK;
import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.PROJECTILE;

/**
 * Created by grzeg on 16.05.2016.
 */
public class PlayerDeath implements Listener {

    private final GameData gameData;
    private final Server server;
    private final BukkitScheduler scheduler;
    private final Plugin plugin;

    public PlayerDeath(GameData gameData, Plugin plugin) {
        this.gameData = gameData;
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.scheduler = server.getScheduler();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setDeathMessage("");
        final Player killed = event.getEntity();
        final Player killer = killed.getKiller();

        if (gameData.isStatus(GameStatus.WAITING)) {
            gameData.handleWeirdDeath(killed);
        } else if (gameData.isStatus(GameStatus.INGAME)) {
            final String killedPlayerName = gameData.handleKilledPerson(killed);
            if (killer != null) {
                gameData.handleKiller(killer);
                messageDeath(event, killed, killer, killedPlayerName);
            } else {
                handleNoKiller(event, killed, killedPlayerName, killed.getLocation().getWorld());
            }
        }
    }

    private void messageDeath(PlayerDeathEvent e, Player killed, Player killer, String killedPlayerName) {
        EntityDamageEvent reason = killed.getLastDamageCause();
        ItemStack itemInHand = killer.getItemInHand();
        Material itemInHandType = itemInHand.getType();
        String itemInHandTypeName = itemInHandType.name();
        EntityDamageEvent.DamageCause damageCause = reason.getCause();
        String killerName = killer.getName();
        if (damageCause == ENTITY_ATTACK || damageCause == CUSTOM || damageCause == PROJECTILE) {
            e.setDeathMessage("§2✪ §a" + killerName + " §4✖ §c" + killedPlayerName + " §6§l⚔ §7" + itemInHandTypeName);
        }
    }

    private void handleNoKiller(PlayerDeathEvent event, Player killed, String killedPlayerName, World world) {
        final var cause = killed.getLastDamageCause();
        if (cause == null) {
            event.setDeathMessage("§r" + killedPlayerName + " zginal!");
        } else {
            switch (cause.getCause()) {
                case FIRE:
                case FIRE_TICK:
                    event.setDeathMessage("§r" + killedPlayerName + " sie zjaral!");
                    break;
                case LAVA:
                    event.setDeathMessage("§r" + killedPlayerName + " pomylil kolory i wskoczyl do lawy!");
                    break;
                default:
                    event.setDeathMessage("§r" + killedPlayerName + " zginal!");
                    break;
            }
        }

        scheduler.runTaskLater(plugin, () -> {
            killed.spigot().respawn();
            killed.teleport(world.getSpawnLocation());
            killed.setGameMode(GameMode.SPECTATOR);
        }, 1L);
    }
}
