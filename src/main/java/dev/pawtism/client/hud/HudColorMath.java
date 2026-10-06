package dev.pawtism.client.hud;

/** Derived surfaces keep their original shading while following the configured HUD background. */
public final class HudColorMath {
    private HudColorMath() {}

    public static int surface(int configured, int defaultBackground, int defaultSurface) {
        if (configured == defaultBackground) return defaultSurface;
        int defaultAlpha = defaultBackground >>> 24;
        int alpha = defaultAlpha == 0 ? configured >>> 24
            : clamp(Math.round((configured >>> 24) * (defaultSurface >>> 24) / (float) defaultAlpha));
        int red = relative(configured >>> 16, defaultBackground >>> 16, defaultSurface >>> 16);
        int green = relative(configured >>> 8, defaultBackground >>> 8, defaultSurface >>> 8);
        int blue = relative(configured, defaultBackground, defaultSurface);
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    public static int contrastingText(int surface, int darkLabel, int lightLabel) {
        return contrast(surface, darkLabel) >= contrast(surface, lightLabel) ? darkLabel : lightLabel;
    }

    private static int relative(int configured, int defaultBackground, int defaultSurface) {
        return clamp((configured & 255) + (defaultSurface & 255) - (defaultBackground & 255));
    }

    private static int clamp(int component) { return Math.max(0, Math.min(255, component)); }

    private static double contrast(int first, int second) {
        double a = luminance(first), b = luminance(second);
        return (Math.max(a, b) + 0.05) / (Math.min(a, b) + 0.05);
    }

    private static double luminance(int color) {
        return 0.2126 * channel(color >>> 16 & 255) + 0.7152 * channel(color >>> 8 & 255)
            + 0.0722 * channel(color & 255);
    }

    private static double channel(int value) {
        double srgb = value / 255.0;
        return srgb <= 0.04045 ? srgb / 12.92 : Math.pow((srgb + 0.055) / 1.055, 2.4);
    }
}
