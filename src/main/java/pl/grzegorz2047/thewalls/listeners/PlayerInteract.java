package pl.grzegorz2047.thewalls.listeners;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import pl.grzegorz2047.databaseapi.messages.MessageAPI;
import pl.grzegorz2047.thewalls.Counter;
import pl.grzegorz2047.thewalls.GameData;
import pl.grzegorz2047.thewalls.GameUser;
import pl.grzegorz2047.thewalls.GameUsers;
import pl.grzegorz2047.thewalls.playerclass.ClassManager;
import pl.grzegorz2047.thewalls.scoreboard.ScoreboardAPI;
import pl.grzegorz2047.thewalls.shop.Shop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.bukkit.Material.OAK_SIGN;
import static org.bukkit.Material.SPRUCE_SIGN;
import static pl.grzegorz2047.thewalls.GameData.GameStatus.INGAME;

/**
 * Created by grzeg on 17.05.2016.
 */
public class PlayerInteract implements Listener {

    private static final List<Material> materialList = List.of(
        Material.ENDER_PEARL,
        Material.EXPERIENCE_BOTTLE,
        Material.GOLDEN_APPLE,
        Material.APPLE,
        Material.GOLD_INGOT,
        Material.IRON_INGOT,
        Material.OAK_WOOD);

    private final GameData gameData;
    private final MessageAPI messageManager;
    private final Counter counter;
    private final Shop shopMenuManager;
    private final ClassManager classManager;
    private final StorageProtection storageProtection;
    private GameUsers gameUsers;
    private ScoreboardAPI scoreboardAPI;
    private final Random r = new Random();
    private final Server server;
    private final Logger logger;

    public PlayerInteract(GameData gameData, MessageAPI messageManager, Shop shopMenuManager, ClassManager classManager, StorageProtection storageProtection, GameUsers gameUsers, ScoreboardAPI scoreboardAPI) {
        this.gameData = gameData;
        this.messageManager = messageManager;
        this.storageProtection = storageProtection;
        this.gameUsers = gameUsers;
        this.scoreboardAPI = scoreboardAPI;
        counter = this.gameData.getCounter();
        this.shopMenuManager = shopMenuManager;
        this.classManager = classManager;
        this.server = Bukkit.getServer();
        this.logger = gameData.getPlugin().getLogger();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getItemInHand();
        GameMode playerGameMode = player.getGameMode();
        Material itemInHandType = itemInHand.getType();
        String playerName = player.getName();
        Counter.CounterStatus counterStatus = counter.getStatus();
        if (itemInHand == null) {
            return;
        }
        if (playerGameMode.equals(GameMode.SPECTATOR)) {
            event.setCancelled(true);
            return;
        }

        // If not in game, handle the lobby items
        if (!gameData.isStatus(INGAME) && handleLobbyInteraction(event, player, itemInHandType)) {
            return;
        }

        if (handleIngameInteractions(event, player, itemInHandType, playerName, counterStatus)) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        if (!(clickedBlock.getState() instanceof Chest)) {
            return;
        }
        Chest state = (Chest) clickedBlock.getState();
        Location blockLocation = clickedBlock.getLocation();
        if (handleChestProtection(event, player, blockLocation)) return;

        handleRandomChestOpen(player, clickedBlock, state);
    }

    private boolean handleChestProtection(PlayerInteractEvent event, Player player, Location location) {
        boolean isChestOwner = storageProtection.isChestOwner(player, location);
        if (!isChestOwner) {
            event.setCancelled(true);
            return true;
        }
        return false;
    }

    private boolean handleIngameInteractions(PlayerInteractEvent event, Player player, Material itemInHandType, String playerName, Counter.CounterStatus status) {
        GameUser user = gameUsers.getGameUser(playerName);
        String language = user.getLanguage();
        if (itemInHandType.equals(Material.NETHER_STAR)) {
            if (!gameData.isStatus(INGAME)) {
                player.sendMessage(messageManager.getMessage(language, "thewalls.shoponlyingame"));
                return true;
            }
            if (user.getAssignedTeam() == null) {
                return true;
            }
            shopMenuManager.openMainMenu(player, user, messageManager);
            return true;
        }
        if (itemInHandType.equals(Material.ENDER_PEARL)) {
            if (status.equals(Counter.CounterStatus.COUNTINGTODROPWALLS)) {
                player.sendMessage(messageManager.getMessage(language, "thewalls.msg.cantplacelava"));
                event.setCancelled(true);
                return true;
            }
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            try {
                final var block = event.getClickedBlock();
                Preconditions.checkNotNull(block);
                final var blockY = block.getLocation().getBlockY();
                final var material = block.getType();
                if (blockY > 100 && blockY < 110 && block.getState() instanceof Sign) {
                    logger.info("jest klikaned by " + player.getName());
                    final var sign = (Sign)(block.getState());
                    Preconditions.checkNotNull(sign);
                    final var signLines = sign.getLines();
                    final var lineSecond = signLines[1];
                    final var lineThird = signLines[2];
                    if (lineSecond.startsWith("§") && lineSecond.contains("ZESTAW") && lineThird.contains("§")) {
                        String tier;
                        switch (lineSecond.charAt(1)) {
                            case 'f':
                                tier = "Gracz";
                                break;
                            case '6':
                                tier = "Vip";
                                break;
                            case 'b':
                                tier = "SuperVip";
                                break;
                            default:
                                logger.warning("Not a recognized kit tier: "
                                    + String.join(";", signLines)
                                    + ", "
                                    + block.getLocation().toString());
                                return false;
                        }

                        final var rank = user.getRank();
                        if ((tier.equals("Vip") && rank.equals("Gracz")) ||
                            (tier.equals("SuperVip") && (rank.equals("Gracz") || rank.equals("Vip")))) {
                            player.sendMessage("§7[§cWalls§7] "
                                + "§fTwoja ranga nie pozwala na uzycie tego zestawu! "
                                + "Rangi dostepne na naszej stronie www.CraftGames.pl");
                            return true;
                        }

                        ClassManager.CLASS kit;
                        if (lineThird.contains("GORNIKA")) {
                            kit = ClassManager.CLASS.GORNIK;
                        } else if (lineThird.contains("DRWALA")) {
                            kit = ClassManager.CLASS.DRWAL;
                        } else if (lineThird.contains("LUCZNIKA")) {
                            kit = ClassManager.CLASS.LUCZNIK;
                        } else if (lineThird.contains("WOJOWNIKA")) {
                            kit = ClassManager.CLASS.WOJOWNIK;
                        } else if (lineThird.contains("ALCHEMIKA")) {
                            kit = ClassManager.CLASS.ALCHEMIK;
                        } else if (lineThird.contains("KUCHARZA")) {
                            kit = ClassManager.CLASS.KUCHARZ;
                        } else if (lineThird.contains("PIROMANA")) {
                            // TODO KURWAAAAAAA
                            kit = ClassManager.CLASS.DRWAL;
                        } else {
                            player.sendMessage("§cNie zdefiniowano kitu " + lineThird + "§c!");
                            logger.warning("Unknown kit type: " + lineThird);
                            return false;
                        }

                        classManager.givePlayerClass(player, user, kit, tier);
                        // TELEPORT TO THE BOTTOM
                        return true;
                    }
                }
            } catch (Throwable ex) {
                logger.warning(ex.toString());
            }
        }
        return false;
    }

    private boolean handleLobbyInteraction(PlayerInteractEvent event, Player player, Material itemInHandType) {
        switch (itemInHandType) {
            case GREEN_WOOL:
                Bukkit.dispatchCommand(player, "team 1");
                event.setCancelled(true);
                return true;
            case LIGHT_BLUE_WOOL:
                Bukkit.dispatchCommand(player, "team 2");
                event.setCancelled(true);
                return true;
            case RED_WOOL:
                Bukkit.dispatchCommand(player, "team 3");
                event.setCancelled(true);
                return true;
            case YELLOW_WOOL:
                Bukkit.dispatchCommand(player, "team 4");
                event.setCancelled(true);
                return true;
            case BOOK:
                player.openInventory(classManager.getClassMenu());
                event.setCancelled(true);
                return true;
            case FEATHER:
                changeLanguage(event, player);
                return true;
            default:
                return false;
        }
    }

    private void changeLanguage(PlayerInteractEvent event, Player player) {
        GameUser gameUser = gameUsers.getGameUser(player.getName());
        if (gameUser.getLanguage().equalsIgnoreCase("PL")) {
            gameUser.setLanguage("EN");
        } else {
            gameUser.setLanguage("PL");
        }
        //Jak bedzie trzeba, to albo feature off albo delay just in case maybe.. later :D
        scoreboardAPI.createWaitingScoreboard(player, gameUser);
        scoreboardAPI.updateDisplayName(0, player, gameUsers.getNumberOfPlayers());
        event.setCancelled(true);
    }


    private void handleRandomChestOpen(Player player, Block clickedBlock, Chest chest) {
        if (!chest.getInventory().contains(Material.BEDROCK)) {
            return;
        }
        List<ItemStack> chestContent = generateChestContent();
        final var playerInventory = player.getInventory();
        chestContent.forEach(playerInventory::addItem);
        player.updateInventory();
        chest.getInventory().clear();
        clickedBlock.setType(Material.AIR);

        // The lack of space is intentional
        player.sendMessage("§7[§cWalls§7]§aGratulacje, przedmioty ze skrzyni sa juz w twoim ekwipunku!");
    }

    private List<ItemStack> generateChestContent() {
        int pearl = 0;
        List<ItemStack> chestContent = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            //System.out.println("Losuje skrzynke!");
            //System.out.println("Test Destroy box");
            ItemStack itemstack1 = getRandomItemstack(pearl!= 0);
            if (itemstack1.getType().equals(Material.ENDER_PEARL)) {
                pearl++;
            }
            chestContent.add(itemstack1);
            //chest.getInventory().addItem(itemstack1);


        }
        return chestContent;
    }

    private ItemStack getRandomItemstack(boolean createEnderPearl) {
        int index = r.nextInt(materialList.size() - 1);
        int amount = r.nextInt(3);
        amount += 1;
        Material mat = materialList.get(index);
        if(mat.equals(Material.ENDER_PEARL) && !createEnderPearl) {
            mat = Material.COOKED_BEEF;
        }
        return new ItemStack(mat, amount);
     }


}
