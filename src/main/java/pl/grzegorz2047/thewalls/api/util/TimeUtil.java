package pl.grzegorz2047.thewalls.api.util;

import org.bukkit.ChatColor;

/**
 * Created by Grzegorz2047. 31.08.2015.
 */
public class TimeUtil {

    public static String formatHHMMSS(int secs) {
        return formatHHMMSS(secs, ChatColor.GRAY);
    }

    public static String formatHHMMSS(int secs, ChatColor timeColor) {
        if (secs < 3600) {
            int minutes = secs / 60,
                    seconds = secs % 60;

            return timeColor + (minutes < 10 ? "0" : "") + minutes + ":"
                    + (seconds < 10 ? "0" : "") + seconds;
        } else {
            int hours = secs / 3600,
                    divider = secs % 3600,
                    minutes = divider / 60,
                    seconds = divider % 60;

            return timeColor + (hours < 10 ? "0" : "") + hours + ":"
                    + (minutes < 10 ? "0" : "") + minutes + ":"
                    + (seconds < 10 ? "0" : "") + seconds;
        }
    }
}