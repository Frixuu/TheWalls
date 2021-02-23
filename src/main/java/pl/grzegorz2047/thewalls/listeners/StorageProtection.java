package pl.grzegorz2047.thewalls.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import pl.grzegorz2047.databaseapi.messages.MessageAPI;
import pl.grzegorz2047.thewalls.GameUser;
import pl.grzegorz2047.thewalls.GameUsers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StorageProtection {

    private final Map<Location, String> protectedFurnaces = new HashMap<>();
    private final GameUsers gameUsers;
    private final MessageAPI messageManager;

    public StorageProtection(GameUsers gameUsers, MessageAPI messageManager) {
        this.gameUsers = gameUsers;
        this.messageManager = messageManager;
    }

    public Optional<String> getPlayerProtectedFurnace(Location location) {
        return Optional.ofNullable(protectedFurnaces.get(location));
    }

    public boolean isChestOwner(Player player, Location location) {
        Optional<String> protectedFurnacePlayer = getPlayerProtectedFurnace(location);
        if (!protectedFurnacePlayer.isPresent()) {
            return true;
        }
        String playerName = player.getName();
        if (protectedFurnacePlayer.get().equals(playerName)) {
            player.sendMessage("§fTen §2Piec §fnalezy do §eCiebie.");
            return true;
        }
        player.sendMessage("§cTen Piec jest zablokowany magicznym zakleciem");
        return false;
    }

    public void removeFurnaceProtection(GameUser user, Location location) {
        protectedFurnaces.remove(location);
        user.decrementProtectedFurnaces();
    }

    public boolean isFurnaceOwner(String username, Location blockLocation) {
        Optional<String> playerProtectedFurnace = getPlayerProtectedFurnace(blockLocation);
        return playerProtectedFurnace.map(s -> s.equals(username)).orElse(true);
    }

    /**
     * Makes a specified user claim a furnace.
     * @return True if the furnace is now protected.
     */
    public boolean protectNewFurnace(Location blockLocation, String username, GameUser user) {
        if (exceedsNumberOfProtectedFurnaces(user)) {
            return false;
        }
        protectedFurnaces.put(blockLocation, username);
        user.incrementProtectedFurnaces();
        return true;
    }

    private boolean exceedsNumberOfProtectedFurnaces(GameUser user) {
        return user.getProtectedFurnaces() >= 3;
    }

    public boolean hasOwner(Location blockLocation) {
        return getPlayerProtectedFurnace(blockLocation).isPresent();
    }
}