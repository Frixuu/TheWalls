package pl.grzegorz2047.thewalls;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import pl.grzegorz2047.thewalls.api.exception.IncorrectDataStringException;
import pl.grzegorz2047.thewalls.api.util.LocationUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by grzeg on 04.11.2016.
 */
public class GameLocationLoader {

    private final Map<GameData.GameTeam, Location> kitTeamSpawnLocations = new HashMap<>();
    private final Map<GameData.GameTeam, Location> teamSpawnLocations = new HashMap<>();
    private final Map<GameData.GameTeam, Location> dmTeamSpawnLocations = new HashMap<>();
    private final HashMap<String, String> settings;

    public GameLocationLoader(HashMap<String, String> settings, String worldName) {
        this.settings = settings;
        final var spawnPath = "thewalls.spawns.team.";
        final var logger = Bukkit.getLogger();
        for (int i = 1; i <= 4; i++) {
            try {
                final var team = GameData.GameTeam.fromNumber(i);
                final var dmSpawn = getTeamDmSpawn(i, worldName);
                logger.info("DM for team " + i + ": " + dmSpawn.toString());
                dmTeamSpawnLocations.put(team, dmSpawn);
                final var teamSpawn = getSpawn(worldName, settings.get(spawnPath + i));
                logger.info("Spawn for team " + i + ": " + teamSpawn.toString());
                teamSpawnLocations.put(team, teamSpawn);
                final var kitSpawn = getTeamKitSpawn(i, worldName);
                logger.info("Kit spawn for team " + i + ": " + kitSpawn.toString());
                kitTeamSpawnLocations.put(team, kitSpawn);
            } catch (IncorrectDataStringException e) {
                e.printStackTrace();
            }
        }
    }

    private Location getSpawn(String worldName, String locPath) throws IncorrectDataStringException {
        return LocationUtil.entityStringToLocation(worldName, locPath);
    }

    public Location getStartLocation(GameData.GameTeam team) {
        return teamSpawnLocations.get(team);
    }

    public Location getKitStartLocation(GameData.GameTeam team) {
        return kitTeamSpawnLocations.get(team);
    }

    private Location getTeamDmSpawn(int i, String worldName) throws IncorrectDataStringException {
        return getSpawn(worldName, settings.get("thewalls.spawns.dm.team." + i));
    }

    private Location getTeamKitSpawn(int i, String worldName) throws IncorrectDataStringException {
        return getSpawn(worldName, settings.get("thewalls.spawns.kitselect.team." + i));
    }

    public void teleportToDeathMatch(Player p, GameData.GameTeam assignedTeam) {
        p.teleport(dmTeamSpawnLocations.get(assignedTeam));
    }

    public void teleportPlayersOnDeathMatch(GameUsers gameUsers) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            GameUser gameUser = gameUsers.getGameUser(p.getName());
            if (gameUser.getAssignedTeam() == null) {
                p.teleport(dmTeamSpawnLocations.get(GameData.GameTeam.TEAM1));
                continue;
            }
            teleportToDeathMatch(p, gameUser.getAssignedTeam());
        }
    }


}