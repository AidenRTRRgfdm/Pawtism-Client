package dev.pawtism.client.hud;

import java.util.Locale;

/** Coordinate and time conversions shared by the original HUD widgets. */
public final class HudMath {
    private static final String[] DIRECTIONS = {
        "South", "Southwest", "West", "Northwest", "North", "Northeast", "East", "Southeast"
    };

    private HudMath() {}

    public static String direction(float minecraftYaw) {
        if (!Float.isFinite(minecraftYaw)) return "?";
        double angle = ((minecraftYaw % 360.0) + 360.0) % 360.0;
        int octant = (int) Math.floor((angle + 22.5) / 45.0) & 7;
        return DIRECTIONS[octant];
    }

    public static int compassHeading(float minecraftYaw) {
        if (!Float.isFinite(minecraftYaw)) return 0;
        return Math.floorMod((int) Math.round(((minecraftYaw % 360.0) + 180.0)), 360);
    }

    public static String elapsed(long seconds) {
        long nonnegative = Math.max(0, seconds);
        long hours = nonnegative / 3600;
        long minutes = nonnegative / 60 % 60;
        long remainder = nonnegative % 60;
        return hours == 0 ? String.format(Locale.ROOT, "%02d:%02d", minutes, remainder)
            : String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, remainder);
    }

    public static double horizontalSpeed(double deltaX, double deltaZ, double seconds) {
        if (!Double.isFinite(deltaX) || !Double.isFinite(deltaZ) || !Double.isFinite(seconds) || seconds <= 0) return 0;
        return Math.hypot(deltaX, deltaZ) / seconds;
    }

    public static String readableId(String id) {
        String[] words = id.replace('_', ' ').split(" ");
        StringBuilder output = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (!output.isEmpty()) output.append(' ');
            output.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return output.toString();
    }
}
