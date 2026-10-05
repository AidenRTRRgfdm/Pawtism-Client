package dev.pawtism.client.hud;

/** Positions use the available travel range so layouts also fit smaller windows. */
public final class HudLayout {
    private HudLayout() {}
    public record Position(double x, double y) {
        public Position {
            if (!Double.isFinite(x) || !Double.isFinite(y) || x < 0 || x > 1 || y < 0 || y > 1)
                throw new IllegalArgumentException("HUD position must be finite and between zero and one");
        }
    }
    public static int clamp(int value, int extent, int size) {
        return Math.max(0, Math.min(value, Math.max(0, extent - size)));
    }
    public static Position normalized(int x, int y, int width, int height, int itemWidth, int itemHeight) {
        return new Position(clamp(x, width, itemWidth) / (double)Math.max(1, width - itemWidth),
            clamp(y, height, itemHeight) / (double)Math.max(1, height - itemHeight));
    }
    public static int resolve(double fraction, int extent, int size) {
        return clamp((int)Math.round(fraction * Math.max(0, extent - size)), extent, size);
    }
}
