package pl.grzegorz2047.thewalls.playerclass;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.grzegorz2047.databaseapi.messages.MessageAPI;
import pl.grzegorz2047.thewalls.GameUser;
import pl.grzegorz2047.thewalls.TheWalls;
import pl.grzegorz2047.thewalls.api.playersclasses.CustomInventory;
import pl.grzegorz2047.thewalls.api.util.CreateItemUtil;
import pl.grzegorz2047.thewalls.api.util.ItemUtil;

import java.util.HashMap;

/**
 * Created by grzeg on 22.05.2016.
 */
public class ClassManager {

    private final TheWalls plugin;
    private final MessageAPI messageManager;

    public HashMap<CLASS, HashMap<String, CustomInventory>> getClassInventory() {
        return classInventory;
    }

    private HashMap<CLASS, HashMap<String, CustomInventory>> classInventory = new HashMap<CLASS, HashMap<String, CustomInventory>>();
    private Inventory classMenu;

    public enum CLASS {WOJOWNIK, DRWAL, GORNIK, LUCZNIK, KUCHARZ, ALCHEMIK}

    private HashMap<String, CLASS> classes = new HashMap<String, CLASS>();


    public ClassManager(TheWalls plugin) {
        this.plugin = plugin;
        for (CLASS c : CLASS.values()) {
            classInventory.put(c, new HashMap<String, CustomInventory>());
            classInventory.get(c).put("Gracz", new CustomInventory());
            classInventory.get(c).put("Vip", new CustomInventory());
        }
        addGornikItems();
        addWojownikItems();
        addLucznikItems();
        addDrwalItems();
        addKucharzItems();
        addAlchemikItems();
        classMenu = Bukkit.createInventory(null, 18, "Kits");
        createClassMenuItems();
        messageManager = plugin.getMessageManager();
    }

    private void createClassMenuItems() {
        ItemStack wojownik = new ItemStack(Material.IRON_SWORD, 1);
        ItemMeta wojownikim = wojownik.getItemMeta();
        wojownikim.setDisplayName("§7Wojownik");
        wojownik.setItemMeta(wojownikim);
        classMenu.setItem(1, wojownik);

        ItemStack drwal = new ItemStack(Material.IRON_AXE, 1);
        ItemMeta drwalim = wojownik.getItemMeta();
        drwalim.setDisplayName("§7Drwal");
        drwal.setItemMeta(drwalim);
        classMenu.setItem(3, drwal);

        ItemStack gornik = new ItemStack(Material.IRON_PICKAXE, 1);
        ItemMeta gornikim = wojownik.getItemMeta();
        gornikim.setDisplayName("§7Gornik");
        gornik.setItemMeta(gornikim);
        classMenu.setItem(5, gornik);

        ItemStack lucznik = new ItemStack(Material.BOW, 1);
        ItemMeta lucznikim = lucznik.getItemMeta();
        lucznikim.setDisplayName("§7Lucznik");
        lucznik.setItemMeta(lucznikim);
        classMenu.setItem(7, lucznik);

        ItemStack kucharz = new ItemStack(Material.GRILLED_PORK, 1);
        ItemMeta kucharzim = kucharz.getItemMeta();
        kucharzim.setDisplayName("§7Kucharz");
        kucharz.setItemMeta(kucharzim);
        classMenu.setItem(12, kucharz);

        ItemStack alchemik = new ItemStack(Material.POTION, 1);
        ItemMeta alchemikim = alchemik.getItemMeta();
        alchemikim.setDisplayName("§7Alchemik");
        alchemik.setItemMeta(alchemikim);
        classMenu.setItem(14, alchemik);
    }

    public Inventory getClassMenu() {
        return classMenu;
    }


    private void addGornikItems() {
        Inventory standardDiggerInventory = classInventory.get(CLASS.GORNIK).get("Gracz").getInventory();
        standardDiggerInventory.addItem(new ItemStack(Material.getMaterial(274), 1));
        standardDiggerInventory.addItem(new ItemStack(Material.getMaterial(263), 8));
        standardDiggerInventory.addItem(new ItemStack(Material.getMaterial(50), 8));
        standardDiggerInventory.addItem(new ItemStack(Material.getMaterial(364), 5));


        ItemStack pickaxe = new ItemStack(Material.getMaterial(257), 1);
        pickaxe.addEnchantment(Enchantment.DIG_SPEED, 1);
        Inventory vipDiggerInventory = classInventory.get(CLASS.GORNIK).get("Vip").getInventory();
        vipDiggerInventory.addItem(pickaxe);
        vipDiggerInventory.addItem(new ItemStack(Material.getMaterial(263), 16));
        vipDiggerInventory.addItem(new ItemStack(Material.getMaterial(50), 16));
        vipDiggerInventory.addItem(new ItemStack(Material.getMaterial(364), 10));


    }

    private void addWojownikItems() {
        ItemStack sword = new ItemStack(Material.getMaterial(267), 1);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        Inventory standardFighterInventory = classInventory.get(CLASS.WOJOWNIK).get("Gracz").getInventory();
        standardFighterInventory.addItem(sword);
        standardFighterInventory.addItem(new ItemStack(Material.getMaterial(364), 5));
        standardFighterInventory.addItem(new ItemStack(Material.getMaterial(322), 2));

        ItemStack vipsword = new ItemStack(Material.getMaterial(267), 1);
        vipsword.addEnchantment(Enchantment.DAMAGE_ALL, 2);
        Inventory vipFighterInventory = classInventory.get(CLASS.WOJOWNIK).get("Vip").getInventory();
        vipFighterInventory.addItem(vipsword);
        vipFighterInventory.addItem(new ItemStack(Material.getMaterial(364), 10));
        vipFighterInventory.addItem(new ItemStack(Material.getMaterial(322), 4));
    }

    private void addLucznikItems() {
        Inventory standardBowmanInventory = classInventory.get(CLASS.LUCZNIK).get("Gracz").getInventory();
        standardBowmanInventory.addItem(new ItemStack(Material.getMaterial(261), 1));
        standardBowmanInventory.addItem(new ItemStack(Material.getMaterial(262), 32));
        standardBowmanInventory.addItem(new ItemStack(Material.getMaterial(364), 5));

        ItemStack vipbow = new ItemStack(Material.getMaterial(261), 1);
        vipbow.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
        Inventory vipBowmanInventory = classInventory.get(CLASS.LUCZNIK).get("Vip").getInventory();
        vipBowmanInventory.addItem(vipbow);
        vipBowmanInventory.addItem(new ItemStack(Material.getMaterial(262), 64));
        vipBowmanInventory.addItem(new ItemStack(Material.getMaterial(364), 10));
        vipBowmanInventory.addItem(new ItemStack(Material.getMaterial(322), 3));
        vipBowmanInventory.addItem(new ItemStack(Material.getMaterial(298), 1));
        vipBowmanInventory.addItem(new ItemStack(Material.getMaterial(301), 1));

    }

    public void addDrwalItems() {
        Inventory standardLumberjackInventory = classInventory.get(CLASS.DRWAL).get("Gracz").getInventory();
        standardLumberjackInventory.addItem(new ItemStack(Material.getMaterial(275), 1));
        standardLumberjackInventory.addItem(new ItemStack(Material.getMaterial(17), 16));
        standardLumberjackInventory.addItem(new ItemStack(Material.getMaterial(364), 5));

        Inventory vipLumberjackInventory = classInventory.get(CLASS.DRWAL).get("Vip").getInventory();
        vipLumberjackInventory.addItem(new ItemStack(Material.getMaterial(258), 1));
        vipLumberjackInventory.addItem(new ItemStack(Material.getMaterial(17), 32));
        vipLumberjackInventory.addItem(new ItemStack(Material.getMaterial(65), 16));
        vipLumberjackInventory.addItem(new ItemStack(Material.getMaterial(364), 10));
    }

    public void addKucharzItems() {
        ItemStack sword = new ItemStack(Material.getMaterial(267), 1);
        sword.addEnchantment(Enchantment.KNOCKBACK, 1);
        Inventory standardFoodInventory = classInventory.get(CLASS.KUCHARZ).get("Gracz").getInventory();
        standardFoodInventory.addItem(sword);
        standardFoodInventory.addItem(new ItemStack(Material.getMaterial(354), 1));
        standardFoodInventory.addItem(new ItemStack(Material.getMaterial(297), 16));
        standardFoodInventory.addItem(new ItemStack(Material.getMaterial(363), 32));
        standardFoodInventory.addItem(new ItemStack(Material.getMaterial(322), 16));
        standardFoodInventory.addItem(new ItemStack(Material.getMaterial(61), 2));
        standardFoodInventory.addItem(new ItemStack(Material.getMaterial(263), 4));

        ItemStack vipsword = new ItemStack(Material.getMaterial(267), 1);
        sword.addEnchantment(Enchantment.KNOCKBACK, 2);
        Inventory vipFoodInventory = classInventory.get(CLASS.KUCHARZ).get("Vip").getInventory();
        vipFoodInventory.addItem(vipsword);
        vipFoodInventory.addItem(new ItemStack(Material.getMaterial(354), 1));
        vipFoodInventory.addItem(new ItemStack(Material.getMaterial(297), 16));
        vipFoodInventory.addItem(new ItemStack(Material.getMaterial(363), 32));
        vipFoodInventory.addItem(new ItemStack(Material.getMaterial(322), 16));
        vipFoodInventory.addItem(new ItemStack(Material.getMaterial(61), 2));
        vipFoodInventory.addItem(new ItemStack(Material.getMaterial(263), 4));
    }

    public void addAlchemikItems() {
        classInventory.get(CLASS.ALCHEMIK).get("Gracz").getInventory().addItem(CreateItemUtil.createItem(Material.getMaterial(379), 1));
        classInventory.get(CLASS.ALCHEMIK).get("Gracz").getInventory().addItem(CreateItemUtil.createItem(Material.getMaterial(380), 1));
        classInventory.get(CLASS.ALCHEMIK).get("Gracz").getInventory().addItem(CreateItemUtil.createItem(Material.getMaterial(326), 1));
        classInventory.get(CLASS.ALCHEMIK).get("Gracz").getInventory().addItem(CreateItemUtil.createItem(Material.getMaterial(374), 6));
        classInventory.get(CLASS.ALCHEMIK).get("Gracz").getInventory().addItem(CreateItemUtil.createItem(Material.getMaterial(372), 3));
        classInventory.get(CLASS.ALCHEMIK).get("Gracz").getInventory().addItem(CreateItemUtil.createItem(Material.getMaterial(378), 1));
        classInventory.get(CLASS.ALCHEMIK).get("Gracz").getInventory().addItem(CreateItemUtil.createItem(Material.getMaterial(353), 1));
        classInventory.get(CLASS.ALCHEMIK).get("Gracz").getInventory().addItem(CreateItemUtil.createItem(Material.getMaterial(348), 1));
        classInventory.get(CLASS.ALCHEMIK).get("Gracz").getInventory().addItem(CreateItemUtil.createItem(Material.getMaterial(382), 1));
        classInventory.get(CLASS.ALCHEMIK).get("Gracz").getInventory().addItem(CreateItemUtil.createItem(Material.getMaterial(370), 1));
        classInventory.get(CLASS.ALCHEMIK).get("Gracz").getInventory().addItem(CreateItemUtil.createItem(Material.getMaterial(289), 3));
        classInventory.get(CLASS.ALCHEMIK).get("Gracz").getInventory().addItem(CreateItemUtil.createItem(Material.getMaterial(364), 12));

        classInventory.get(CLASS.ALCHEMIK).get("Vip").getInventory().addItem(CreateItemUtil.createItem(Material.getMaterial(379), 1));
        classInventory.get(CLASS.ALCHEMIK).get("Vip").getInventory().addItem(CreateItemUtil.createItem(Material.getMaterial(380), 1));
        classInventory.get(CLASS.ALCHEMIK).get("Vip").getInventory().addItem(CreateItemUtil.createItem(Material.getMaterial(326), 1));
        classInventory.get(CLASS.ALCHEMIK).get("Vip").getInventory().addItem(CreateItemUtil.createItem(Material.getMaterial(374), 6));
        classInventory.get(CLASS.ALCHEMIK).get("Vip").getInventory().addItem(CreateItemUtil.createItem(Material.getMaterial(372), 3));
        classInventory.get(CLASS.ALCHEMIK).get("Vip").getInventory().addItem(CreateItemUtil.createItem(Material.getMaterial(378), 1));
        classInventory.get(CLASS.ALCHEMIK).get("Vip").getInventory().addItem(CreateItemUtil.createItem(Material.getMaterial(353), 1));
        classInventory.get(CLASS.ALCHEMIK).get("Vip").getInventory().addItem(CreateItemUtil.createItem(Material.getMaterial(348), 1));
        classInventory.get(CLASS.ALCHEMIK).get("Vip").getInventory().addItem(CreateItemUtil.createItem(Material.getMaterial(382), 1));
        classInventory.get(CLASS.ALCHEMIK).get("Vip").getInventory().addItem(CreateItemUtil.createItem(Material.getMaterial(370), 1));
        classInventory.get(CLASS.ALCHEMIK).get("Vip").getInventory().addItem(CreateItemUtil.createItem(Material.getMaterial(289), 3));
        classInventory.get(CLASS.ALCHEMIK).get("Vip").getInventory().addItem(CreateItemUtil.createItem(Material.getMaterial(364), 12));

    }

    public HashMap<String, CLASS> getPlayerClasses() {
        return classes;
    }

    public void givePlayerClass(Player p, GameUser user) {
        ClassManager.CLASS playerClass = this.getPlayerClasses().get(p.getName());
        if (playerClass == null) {
            playerClass = ClassManager.CLASS.GORNIK;
        }
        String kittype = "Gracz";
        if (!user.getRank().equals("Gracz")) {
            kittype = "Vip";
        }
        for (ItemStack it : this.getClassInventory().get(playerClass).get(kittype).getInventory()) {
            if (it != null) {
                p.getInventory().addItem(it);
            }
        }
        p.sendMessage(messageManager.getMessage(user.getLanguage(), "thewalls.msg.classgiven").replace("{CLASS}", playerClass.name()));
    }

}
