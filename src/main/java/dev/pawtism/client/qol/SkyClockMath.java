package dev.pawtism.client.qol;

/** Visual clock math only; does not touch level clocks or light calculations. */
public final class SkyClockMath {
    private SkyClockMath() {}
    public static float angleRadians(int ticks) {
        double phase = Math.floorMod(ticks, 24000) / 24000.0 - 0.25;
        if (phase < 0) phase += 1;
        double eased = 1 - (Math.cos(phase * Math.PI) + 1) / 2;
        return (float) ((phase + (eased - phase) / 3) * Math.PI * 2);
    }
    public static int skyColor(int ticks) {
        double daylight = Math.max(0, Math.min(1, Math.cos(angleRadians(ticks)) * 2 + 0.5));
        int red = (int) Math.round(5 + 115 * daylight);
        int green = (int) Math.round(8 + 159 * daylight);
        int blue = (int) Math.round(18 + 237 * daylight);
        return 0xff000000 | red << 16 | green << 8 | blue;
    }
    public static float stars(int ticks) {
        double light = 1 - (Math.cos(angleRadians(ticks)) * 2 + 0.25);
        light = Math.max(0, Math.min(1, light));
        return (float) (light * light * 0.5);
    }
}
