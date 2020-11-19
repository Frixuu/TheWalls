package pl.grzegorz2047.thewalls.api.util;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Created by Grzegorz2047. 25.09.2015.
 */
public class ColoringUtil {

    public static void colorPlayerTab(Player player, String color) {
        try {
            player.setPlayerListName(color + player.getName());
        } catch (IllegalArgumentException e) {
            char[] origin = player.getName().toCharArray();
            char[] newNick = "123456789012".toCharArray();
            String nPo = "";
            if (origin.length > 12) {
                System.arraycopy(origin, 0, newNick, 0, 12);
                nPo = String.copyValueOf(newNick);
            }
            player.setPlayerListName(color + nPo);
        }
    }

    public static String colorText(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
