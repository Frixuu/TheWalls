package pl.grzegorz2047.thewalls.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scoreboard.Scoreboard;
import pl.grzegorz2047.databaseapi.MoneyAPI;
import pl.grzegorz2047.databaseapi.messages.MessageAPI;
import pl.grzegorz2047.databaseapi.shop.Item;
import pl.grzegorz2047.databaseapi.shop.ShopAPI;
import pl.grzegorz2047.databaseapi.shop.Transaction;
import pl.grzegorz2047.thewalls.GameData;
import pl.grzegorz2047.thewalls.GameUser;
import pl.grzegorz2047.thewalls.GameUsers;
import pl.grzegorz2047.thewalls.api.itemmenu.event.ChooseItemEvent;
import pl.grzegorz2047.thewalls.playerclass.ClassManager;
import pl.grzegorz2047.thewalls.scoreboard.ScoreboardAPI;
import pl.grzegorz2047.thewalls.shop.Shop;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by grzeg on 21.05.2016.
 */
public class ChooseItem implements Listener {

    private final MessageAPI messageManager;
    private final Shop shopMenuManager;
    private final ShopAPI shopManager;
    private final ScoreboardAPI scoreboardAPI;
    private final ClassManager classManager;
    private final GameUsers gameUsers;

    public ChooseItem(MessageAPI messageManager, Shop shopMenuManager, ScoreboardAPI scoreboardAPI, ShopAPI shopManager, ClassManager classManager, GameUsers gameUsers) {
        this.messageManager = messageManager;
        this.shopMenuManager = shopMenuManager;
        this.shopManager = shopManager;
        this.scoreboardAPI = scoreboardAPI;
        this.classManager = classManager;
        this.gameUsers = gameUsers;
    }

    @EventHandler
    public void onChoose(ChooseItemEvent e) {

        Player p = e.getPlayer();
        String playerName = p.getName();
        PlayerInventory playerInventory = p.getInventory();

        String title = e.getTitle();
        if (title == null) {
            return;
        }

        ItemStack clicked = e.getClicked();
        if (clicked == null) {
            return;
        }

        Material clickedType = clicked.getType();
        if (clickedType == null) {
            return;
        }

        switch (title) {
            case "Kits": {
                e.setCancelled(true);
                GameUser user = gameUsers.getGameUser(playerName);
                ClassManager.CLASS kit;
                ClassManager classManager = this.classManager;
                HashMap<String, ClassManager.CLASS> playerClasses = classManager.getPlayerClasses();

                switch (clickedType) {
                    case IRON_SWORD:
                        playerClasses.put(playerName, ClassManager.CLASS.WOJOWNIK);
                        kit = ClassManager.CLASS.WOJOWNIK;
                        break;
                    case IRON_PICKAXE:
                        playerClasses.put(playerName, ClassManager.CLASS.GORNIK);
                        kit = ClassManager.CLASS.GORNIK;
                        break;
                    case IRON_AXE:
                        playerClasses.put(playerName, ClassManager.CLASS.DRWAL);
                        kit = ClassManager.CLASS.DRWAL;
                        break;
                    case COOKED_PORKCHOP:
                        playerClasses.put(playerName, ClassManager.CLASS.KUCHARZ);
                        kit = ClassManager.CLASS.KUCHARZ;
                        break;
                    case POTION:
                        playerClasses.put(playerName, ClassManager.CLASS.ALCHEMIK);
                        kit = ClassManager.CLASS.ALCHEMIK;
                        break;
                    case BOW:
                        playerClasses.put(playerName, ClassManager.CLASS.LUCZNIK);
                        kit = ClassManager.CLASS.LUCZNIK;
                        break;
                    default:
                        return;
                }

                String userLanguage = user.getLanguage();
                p.sendMessage(messageManager.getMessage(userLanguage, "thewalls.msg.kitgiven").replace("{KIT}", kit.name()));
                p.closeInventory();
                break;
            }
            case "Main":
                e.setCancelled(true);
                if (clickedType.equals(Material.QUARTZ)) {
                    GameUser user = gameUsers.getGameUser(playerName);
                    shopMenuManager.openTempItems(p, user, messageManager);
                }
                if (clickedType.equals(Material.MAGMA_CREAM)) {
                    GameUser user = gameUsers.getGameUser(playerName);
                    shopMenuManager.openPermItems(p, user, messageManager);
                }
                return;
            case "Perm items": {
                GameUser user = gameUsers.getGameUser(playerName);
                e.setCancelled(true);

                int slot = e.getSlot();
                for (Map.Entry<Integer, Item> entry : shopMenuManager.getNormalItems().entrySet()) {
                    Item item = entry.getValue();
                    if (item.getSlot() == slot) {
                        String userLanguage = user.getLanguage();
                        for (Transaction t : user.getTransactions()) {
                            if (t.getItemid() == item.getItemid()) {
                                p.sendMessage(messageManager.getMessage(userLanguage, "shop.alreadybought"));
                                return;
                            }
                        }
                        if (user.getMoney() >= item.getPrice()) {
                            if (playerInventory.firstEmpty() != -1) {
                                shopManager.buyItem(playerName, String.valueOf(item.getItemid()), Instant.EPOCH.getEpochSecond());
                                user.changeMoney(-item.getPrice());
                                Scoreboard scoreboard = p.getScoreboard();
                                scoreboardAPI.updateIncreaseEntry(scoreboard, messageManager.getMessage(userLanguage, "thewalls.scoreboard.money"), -item.getPrice());
                                user.getTransactions().add(new Transaction(user.getUserid(), item.getItemid(), 0));
                                playerInventory.addItem(item.toItemStack());
                                p.sendMessage(messageManager.getMessage(userLanguage, "shop.success"));
                                return;
                            } else {
                                p.sendMessage(messageManager.getMessage(userLanguage, "shop.emptyinventoryfirst"));
                                return;
                            }
                        } else {
                            p.sendMessage(messageManager.getMessage(userLanguage, "shop.insufficientmoney"));
                            return;
                        }
                    }
                }
                break;
            }
            case "Temp items": {
                GameUser user = gameUsers.getGameUser(playerName);
                e.setCancelled(true);

                int slot = e.getSlot();
                for (Map.Entry<Integer, Item> entry : shopMenuManager.getTempItems().entrySet()) {
                    Item item = entry.getValue();
                    if (item.getSlot() == slot) {
                        String userLanguage = user.getLanguage();
                        List<Material> userBoughtTempItems = user.getBoughtTempItems();
                        if (userBoughtTempItems.contains(item.getMaterial())) {
                            p.sendMessage(messageManager.getMessage(userLanguage, "shop.alreadyboughtinthisround"));
                            return;
                        }
                        if (user.getMoney() >= item.getPrice()) {
                            if (playerInventory.firstEmpty() != -1) {
                                user.changeMoney(-item.getPrice());
                                userBoughtTempItems.add(item.getMaterial());
                                playerInventory.addItem(item.toItemStack());
                                Scoreboard scoreboard = p.getScoreboard();
                                scoreboardAPI.updateIncreaseEntry(scoreboard, messageManager.getMessage(userLanguage, "thewalls.scoreboard.money"), -item.getPrice());
                                p.sendMessage(messageManager.getMessage(userLanguage, "shop.success"));
                                return;
                            } else {
                                p.sendMessage(messageManager.getMessage(userLanguage, "shop.emptyinventoryfirst"));
                                return;
                            }
                        } else {
                            p.sendMessage(messageManager.getMessage(userLanguage, "shop.insufficientmoney"));
                            return;
                        }
                    }
                }
                break;
            }
        }
    }
}
