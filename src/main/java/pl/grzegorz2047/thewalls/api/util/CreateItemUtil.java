package pl.grzegorz2047.thewalls.api.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by grzeg on 10.12.2015.
 */
public class CreateItemUtil {

    public static ItemStack createItem(Material mat, String name) {
        return createItem(mat, 1, name);
    }

    public static ItemStack createItem(Material mat, int amount, String name) {
        ItemStack is = new ItemStack(mat, amount);
        ItemMeta md = is.getItemMeta();
        md.setDisplayName(name);
        is.setItemMeta(md);
        return is;
    }
}