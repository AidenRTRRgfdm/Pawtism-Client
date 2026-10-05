package dev.pawtism.client.ui;

/** Color roles and neutral text rules shared by the two XP palettes. */
public final class XpPalette {
    public static final int LIGHT_BODY = 0xFFECE9D8;
    public static final int DARK_BODY = 0xFF20283A;
    public static final int LIGHT_TEXT = 0xFF202020;
    public static final int DARK_TEXT = 0xFFE8EDF5;
    public static final int LIGHT_MUTED = 0xFF77746B;
    public static final int DARK_MUTED = 0xFFA9B7CE;
    public static final int LIGHT_CONTENT = 0xFFF8F7ED;
    public static final int DARK_CONTENT = 0xFF151C2B;
    public static final int LIGHT_NATIVE_TEXT = 0xFF202638;

    private XpPalette() {}

    public static int select(boolean dark, int lightColor, int darkColor) {
        return dark ? darkColor : lightColor;
    }

    public static boolean pale(int color) {
        int red = color >>> 16 & 255, green = color >>> 8 & 255, blue = color & 255;
        return red >= 185 && green >= 185 && blue >= 185 && spread(red, green, blue) <= 30;
    }

    public static boolean nearBlack(int color) {
        int red = color >>> 16 & 255, green = color >>> 8 & 255, blue = color & 255;
        return red <= 120 && green <= 120 && blue <= 120 && spread(red, green, blue) <= 30;
    }

    /** Keeps opacity and semantic colors intact when changing a neutral menu label. */
    public static int menuColor(int original, boolean dark) {
        if (!(dark ? nearBlack(original) : pale(original))) return original;
        int replacement = dark ? DARK_TEXT : LIGHT_NATIVE_TEXT;
        return original & 0xFF000000 | replacement & 0x00FFFFFF;
    }

    private static int spread(int red, int green, int blue) {
        return Math.max(red, Math.max(green, blue)) - Math.min(red, Math.min(green, blue));
    }
}
