package pl.grzegorz2047.thewalls.api.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import pl.grzegorz2047.thewalls.api.exception.IncorrectDataStringException;

/**
 * Created by Grzegorz2047. 23.09.2015.
 */
public class LocationUtil {

    public static Location entityStringToLocation(String world, String loc) throws IncorrectDataStringException {
        String[] split = loc.split(":");
        if (split.length != 5) {
            throw new IncorrectDataStringException("Podales niepoprawny ciag znakow: nie sklada sie z 5 czesci, tylko z " + split.length);
        }
        double x = Double.parseDouble(split[0]);
        double y = Double.parseDouble(split[1]);
        double z = Double.parseDouble(split[2]);
        float p = Float.parseFloat(split[3]);
        float yaw = Float.parseFloat(split[4]);
        return new Location(Bukkit.getWorld(world), x, y, z, p, yaw);
    }
}
