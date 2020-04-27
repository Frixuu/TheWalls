package pl.grzegorz2047.thewalls.api.util;

/**
 * Created by Grzegorz2047. 31.08.2015.
 */
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

/**
 * Created by neksi on 2015-01-22.
 */
public class ItemUtil {

     public static ItemStack setColor(Material material, int r, int g, int b){
        ItemStack itemstack = new ItemStack(material);
        LeatherArmorMeta lam = (LeatherArmorMeta)itemstack.getItemMeta();
        lam.setColor(Color.fromRGB(r,g,b));
        itemstack.setItemMeta(lam);

        return itemstack;
    }

    public static ItemStack setColor(ItemStack itemStack, int r, int g, int b){
        ItemStack itemstack = itemStack.clone();
        LeatherArmorMeta lam = (LeatherArmorMeta)itemstack.getItemMeta();
        lam.setColor(Color.fromRGB(r,g,b));
        itemstack.setItemMeta(lam);

        return itemstack;
    }
}