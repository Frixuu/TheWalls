package pl.grzegorz2047.thewalls;

public class RankUtils {
    public static String getPrefixFromRankName(String rankName) {
        switch (rankName) {
            case "Gracz":
                return "";
            case "Admin":
                return "§7[§4Admin§7]";
            default:
                return "";
        }
    }
}
