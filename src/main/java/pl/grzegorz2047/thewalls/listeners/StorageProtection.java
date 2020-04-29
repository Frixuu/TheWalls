package pl.grzegorz2047.thewalls.listeners;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import pl.grzegorz2047.databaseapi.messages.MessageAPI;
import pl.grzegorz2047.thewalls.GameUser;
import pl.grzegorz2047.thewalls.GameUsers;

import java.util.HashMap;
import java.util.Optional;

public class StorageProtection {

    HashMap<Location, String> protectedFurnaces = new HashMap<Location, String>();
    private GameUsers gameUsers;
    private final MessageAPI messageManager;

    public StorageProtection(GameUsers gameUsers, MessageAPI messageManager) {
        this.gameUsers = gameUsers;
        this.messageManager = messageManager;
    }

    public Optional<String> getPlayerProtectedFurnace(Location location) {
        return Optional.ofNullable(protectedFurnaces.get(location));
    }

    public boolean isChestOwner(Player player, String playerName, Block clickedBlock) {
        Optional<String> protectedFurnacePlayer = getPlayerProtectedFurnace(clickedBlock.getLocation());
        if (protectedFurnacePlayer.isPresent()) {
            if (!protectedFurnacePlayer.get().equals(playerName)) {
                GameUser user = gameUsers.getGameUser(playerName);
                String userLanguage = user.getLanguage();
                player.sendMessage(messageManager.getMessage(userLanguage, "thewalls.msg.someonesprotectedfurnace"));
                return false;
            } else {
                return true;
            }
        }
        return true;
    }

    public void removeFurnaceProtection(GameUser user, Location location) {
        protectedFurnaces.remove(location);
        user.setProtectedFurnaces(user.getProtectedFurnaces() - 1);
    }

    public boolean isFurnaceOwner(String username, Location blockLocation) {
        Optional<String> playerProtectedFurnace = getPlayerProtectedFurnace(blockLocation);
        return playerProtectedFurnace.map(s -> s.equals(username)).orElse(true);
    }

    public void protectNewFurnace(Location blockLocation, String username, GameUser user) {
        protectedFurnaces.put(blockLocation, username);
        user.setProtectedFurnaces(user.getProtectedFurnaces() + 1);
    }

    public boolean hasOwner(Location blockLocation) {
        return getPlayerProtectedFurnace(blockLocation).isPresent();
    }
}