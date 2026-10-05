package dev.pawtism.client.ui;

import dev.pawtism.client.PawtismConfig;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Luna-style window chrome and an original landscape made entirely from colored rectangles. */
public final class XpTheme {
    public static final int BLUE = 0xFF245EDB;
    public static final int BEIGE = 0xFFECE9D8;
    public static final int DARK_TEXT = 0xFF202020;
    public static final int MUTED_TEXT = 0xFF77746B;
    public static final int TITLE_HEIGHT = 23;
    public static final int TASKBAR_HEIGHT = 24;
    public static final int START_WIDTH = 66;
    private static final int BLUE_EDGE = 0xFF003C74;
    private static final int SILVER = 0xFFACA899;
    private static final int GOLD = 0xFFFFC73C;
    private static final int MENU_TITLE_HEIGHT = 16;
    private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static long clockMinute = Long.MIN_VALUE;
    private static String clockText = "";

    private XpTheme() {}

    public static boolean enabled() { return PawtismConfig.XP_THEME.getBooleanValue(); }
    public static boolean dark() { return enabled() && PawtismConfig.XP_DARK_MODE.getBooleanValue(); }
    public static int color(int lightColor, int darkColor) { return XpPalette.select(dark(), lightColor, darkColor); }
    public static int body() { return color(XpPalette.LIGHT_BODY, XpPalette.DARK_BODY); }
    public static int text() { return color(XpPalette.LIGHT_TEXT, XpPalette.DARK_TEXT); }
    public static int mutedText() { return color(XpPalette.LIGHT_MUTED, XpPalette.DARK_MUTED); }
    public static int content() { return color(XpPalette.LIGHT_CONTENT, XpPalette.DARK_CONTENT); }

    /** A bounded number of horizontal spans, independent of the physical window resolution. */
    public static void desktop(GuiGraphicsExtractor g, Font font, int width, int height) {
        if (width <= 0 || height <= 0) return;
        g.fillGradient(0, 0, width, height, color(0xFF3D92DF, 0xFF101A36), color(0xFFD1EAF7, 0xFF354765));
        cloud(g, width * 0.18, height * 0.17, width * 0.17, Math.max(12, height * 0.065));
        cloud(g, width * 0.67, height * 0.12, width * 0.15, Math.max(10, height * 0.055));
        g.fill(0, (int)(height * 0.76), width, height, color(0xFF31841C, 0xFF122D24));
        hill(g, width, height, width * 0.76, height * 1.03,
            width * 0.95, height * 0.55, color(0xFF86BD39, 0xFF385C3B), color(0xFF438F22, 0xFF1C3D2C));
        hill(g, width, height, width * 0.08, height * 1.26,
            width * 1.09, height * 0.69, color(0xFF70B62E, 0xFF2C553A), color(0xFF28751C, 0xFF102C24));
    }

    private static void cloud(GuiGraphicsExtractor g, double x, double y, double width, double height) {
        ellipse(g, x, y + height * 0.15, width * 0.50, height * 0.34, color(0xFFE7F1F7, 0xFF4E607A), 9);
        ellipse(g, x - width * 0.22, y, width * 0.28, height * 0.44, color(0xFFF9FCFE, 0xFF64758C), 9);
        ellipse(g, x + width * 0.11, y - height * 0.19, width * 0.29, height * 0.54, color(0xFFFFFFFF, 0xFF75869D), 9);
    }

    private static void ellipse(GuiGraphicsExtractor g, double cx, double cy, double rx, double ry, int color, int bands) {
        if (rx <= 0 || ry <= 0) return;
        int top = (int)Math.floor(cy - ry), bottom = (int)Math.ceil(cy + ry);
        int count = Math.max(1, Math.min(bands, bottom - top));
        for (int i = 0; i < count; i++) {
            int y0 = top + (bottom - top) * i / count;
            int y1 = top + (bottom - top) * (i + 1) / count;
            double dy = ((y0 + y1) * 0.5 - cy) / ry;
            double span = rx * Math.sqrt(Math.max(0, 1 - dy * dy));
            int x0 = (int)Math.floor(cx - span), x1 = (int)Math.ceil(cx + span);
            if (x1 > x0 && y1 > y0) g.fill(x0, y0, x1, y1, color);
        }
    }

    private static void hill(GuiGraphicsExtractor g, int width, int height, double cx, double cy,
                             double rx, double ry, int topColor, int bottomColor) {
        int top = Math.max(0, (int)Math.floor(cy - ry));
        int bands = Math.max(1, Math.min(28, height - top));
        for (int i = 0; i < bands; i++) {
            int y0 = top + (height - top) * i / bands;
            int y1 = top + (height - top) * (i + 1) / bands;
            double dy = ((y0 + y1) * 0.5 - cy) / ry;
            double span = rx * Math.sqrt(Math.max(0, 1 - dy * dy));
            int x0 = Math.max(0, (int)Math.floor(cx - span));
            int x1 = Math.min(width, (int)Math.ceil(cx + span));
            if (x1 > x0 && y1 > y0) g.fill(x0, y0, x1, y1, mix(topColor, bottomColor, (double)i / bands));
        }
    }

    public static void frame(GuiGraphicsExtractor g, Font font, int x, int y, int width, int height,
                             String title, boolean controls) {
        window(g, font, x, y, width, height, title, controls, true, TITLE_HEIGHT);
    }

    /** Keeps vanilla screen coordinates intact; native headings at y=20 sit below the slim title bar. */
    public static void menuBackground(GuiGraphicsExtractor g, Font font, int width, int height,
                                      String title, boolean inWorld) {
        if (width <= 0 || height <= 0) return;
        if (inWorld) g.fill(0, 0, width, height, 0x38000000);
        else desktop(g, font, width, height);
        // No taskbar here: vanilla bottom-row buttons already occupy those coordinates.
        window(g, font, 5, 3, Math.max(0, width - 10), Math.max(0, height - 6),
            title, false, !inWorld, MENU_TITLE_HEIGHT);
    }

    private static void window(GuiGraphicsExtractor g, Font font, int x, int y, int width, int height,
                               String title, boolean controls, boolean opaqueBody, int titleHeight) {
        if (width < 12 || height < titleHeight + 5) return;
        int right = x + width, bottom = y + height;
        g.fill(right, y + 3, right + 2, bottom + 2, 0x40000000);
        g.fill(x + 3, bottom, right, bottom + 2, 0x40000000);
        outline(g, x, y, width, height, color(0xFF00138C, 0xFF0C142A));
        outline(g, x + 1, y + 1, width - 2, height - 2, color(0xFF5289EC, 0xFF5275AE));
        outline(g, x + 2, y + 2, width - 4, height - 4, color(BLUE, 0xFF254D8D));
        if (opaqueBody) {
            g.fill(x + 3, y + titleHeight, right - 3, bottom - 3, body());
            g.fill(x + 3, y + titleHeight, right - 3, y + titleHeight + 1, color(0xFFFFFFFF, 0xFF4C5C76));
            g.fill(x + 3, y + titleHeight + 1, x + 4, bottom - 3, color(0xFFFFFFFF, 0xFF4C5C76));
            g.fill(right - 4, y + titleHeight + 1, right - 3, bottom - 3, color(SILVER, 0xFF101827));
            g.fill(x + 4, bottom - 4, right - 3, bottom - 3, color(SILVER, 0xFF101827));
        }
        g.fillGradient(x + 2, y + 2, right - 2, y + titleHeight, color(0xFF4D91F2, 0xFF426BA6), color(BLUE, 0xFF22447E));
        g.fill(x + 4, y + 2, right - 4, y + 3, color(0xFF85B4F5, 0xFF7A9BC8));
        g.fill(x + 3, y + titleHeight - 1, right - 3, y + titleHeight, color(0xFF0E44A6, 0xFF142D54));
        int available = Math.max(0, width - 20 - (controls ? 57 : 0));
        String label = title == null ? "" : font.plainSubstrByWidth(title, available);
        g.text(font, label, x + 9, y + (titleHeight - font.lineHeight) / 2 + 1, 0xFFD5EAFF, true);
        if (controls && width >= 90 && titleHeight >= 21) {
            captionButton(g, right - 56, y + 4, 0);
            captionButton(g, right - 38, y + 4, 1);
            captionButton(g, right - 20, y + 4, 2);
        }
    }

    private static void captionButton(GuiGraphicsExtractor g, int x, int y, int kind) {
        outline(g, x, y, 16, 16, color(0xFFFFFFFF, 0xFFBDD1ED));
        g.fillGradient(x + 1, y + 1, x + 15, y + 15,
            kind == 2 ? color(0xFFF38C71, 0xFFCA796B) : color(0xFF86B5F5, 0xFF6789BA),
            kind == 2 ? color(0xFFBD321D, 0xFF8D352A) : color(0xFF2860D1, 0xFF294F88));
        if (kind == 0) g.fill(x + 4, y + 11, x + 11, y + 13, 0xFFFFFFFF);
        else if (kind == 1) {
            outline(g, x + 4, y + 4, 8, 8, 0xFFFFFFFF);
            g.fill(x + 4, y + 5, x + 12, y + 6, 0xFFFFFFFF);
        } else {
            for (int i = 0; i < 6; i++) {
                g.fill(x + 5 + i, y + 5 + i, x + 6 + i, y + 6 + i, 0xFFFFFFFF);
                g.fill(x + 10 - i, y + 5 + i, x + 11 - i, y + 6 + i, 0xFFFFFFFF);
            }
        }
    }

    public static void button(GuiGraphicsExtractor g, int x, int y, int width, int height,
                              boolean hover, boolean focus, boolean active) {
        if (width < 3 || height < 3) return;
        int right = x + width, bottom = y + height;
        int border = active ? color(BLUE_EDGE, 0xFF8EA9CF) : color(SILVER, 0xFF526075);
        g.fillGradient(x + 1, y + 1, right - 1, bottom - 1,
            active ? color(0xFFFFFFFF, 0xFF364258) : color(0xFFF1EFE2, 0xFF2B3343),
            active ? color(0xFFE2E0D4, 0xFF263246) : body());
        // Leave the four extreme corner pixels clear for the characteristic small rounded edge.
        g.fill(x + 1, y, right - 1, y + 1, border);
        g.fill(x + 1, bottom - 1, right - 1, bottom, border);
        g.fill(x, y + 1, x + 1, bottom - 1, border);
        g.fill(right - 1, y + 1, right, bottom - 1, border);
        if (width > 6 && height > 6) {
            g.fill(x + 2, y + 1, right - 2, y + 2, color(0xFFFFFFFF, 0xFF566982));
            g.fill(x + 1, y + 2, x + 2, bottom - 2, color(0xFFFFFFFF, 0xFF566982));
            g.fill(x + 2, bottom - 2, right - 2, bottom - 1,
                active ? color(0xFFB9B6AA, 0xFF172031) : color(0xFFD6D2C2, 0xFF1A2231));
            g.fill(right - 2, y + 2, right - 1, bottom - 2,
                active ? color(0xFFC3C0B4, 0xFF1B2537) : color(0xFFD6D2C2, 0xFF1A2231));
            if (active && hover) outline(g, x + 2, y + 2, width - 4, height - 4, color(GOLD, 0xFFF4C76A));
            else if (active && focus) outline(g, x + 2, y + 2, width - 4, height - 4, color(0xFF7BA5E5, 0xFF92B6EF));
        }
    }

    public static void panel(GuiGraphicsExtractor g, int x, int y, int width, int height) {
        if (width < 3 || height < 3) return;
        g.fill(x, y, x + width, y + height, body());
        g.fill(x, y, x + width, y + 1, color(0xFFFFFFFF, 0xFF4C5C76));
        g.fill(x, y + 1, x + 1, y + height, color(0xFFFFFFFF, 0xFF4C5C76));
        g.fill(x, y + height - 1, x + width, y + height, color(SILVER, 0xFF101827));
        g.fill(x + width - 1, y + 1, x + width, y + height, color(SILVER, 0xFF101827));
    }

    public static void slot(GuiGraphicsExtractor g, int x, int y) {
        g.fill(x, y, x + 18, y + 18, color(0xFFC8C7BC, 0xFF172030));
        g.fill(x, y, x + 17, y + 1, color(0xFF77756C, 0xFF0B111C));
        g.fill(x, y, x + 1, y + 17, color(0xFF77756C, 0xFF0B111C));
        g.fill(x + 1, y + 1, x + 17, y + 2, color(0xFFAAA89D, 0xFF101827));
        g.fill(x + 1, y + 1, x + 2, y + 17, color(0xFFAAA89D, 0xFF101827));
        g.fill(x, y + 17, x + 18, y + 18, color(0xFFFFFFFF, 0xFF52637C));
        g.fill(x + 17, y, x + 18, y + 17, color(0xFFFFFFFF, 0xFF52637C));
    }

    public static void taskbar(GuiGraphicsExtractor g, Font font, int width, int height) {
        if (width <= 0 || height < TASKBAR_HEIGHT) return;
        int y = height - TASKBAR_HEIGHT;
        g.fillGradient(0, y, width, height, color(0xFF317CEB, 0xFF294C80), color(0xFF175BD3, 0xFF152C53));
        g.fill(0, y, width, y + 1, color(0xFF91B6F5, 0xFF688BB7));
        g.fill(0, y + 1, width, y + 2, color(0xFF4D91F2, 0xFF3F6598));
        int startWidth = Math.min(START_WIDTH, width);
        g.fillGradient(0, y, startWidth, height, color(0xFF70B85A, 0xFF42723F), color(0xFF278420, 0xFF214F2B));
        g.fill(0, y, startWidth - 2, y + 1, color(0xFF9AD07D, 0xFF84A976));
        g.fill(startWidth - 2, y + 1, startWidth, height, color(0xFF195E18, 0xFF153520));
        if (startWidth >= START_WIDTH) {
            // A small paw belongs to Pawtism, rather than a platform logo.
            ellipse(g, 12, y + 14, 5, 4, 0xFFFFFFFF, 5);
            ellipse(g, 6, y + 8, 2, 3, 0xFFFFFFFF, 3);
            ellipse(g, 11, y + 6, 2, 3, 0xFFFFFFFF, 3);
            ellipse(g, 16, y + 7, 2, 3, 0xFFFFFFFF, 3);
            ellipse(g, 20, y + 11, 2, 3, 0xFFFFFFFF, 3);
            g.text(font, "start", 27, y + 8, 0xFFFFFFFF, true);
        }
        if (width < 148) return;
        String clock = clock();
        int clockWidth = Math.max(62, font.width(clock) + 18);
        int clockX = width - clockWidth;
        int statusWidth = Math.min(174, Math.max(0, clockX - START_WIDTH - 12));
        if (statusWidth > 22) {
            int x = START_WIDTH + 6;
            g.fillGradient(x, y + 3, x + statusWidth, height - 3, color(0xFF4D8FEB, 0xFF416593), color(0xFF2264CD, 0xFF253F68));
            outline(g, x, y + 3, statusWidth, TASKBAR_HEIGHT - 6, color(0xFF94B7F3, 0xFF7594BB));
            g.text(font, font.plainSubstrByWidth("Pawtism Client", statusWidth - 12), x + 6, y + 8, 0xFFFFFFFF, false);
        }
        g.fillGradient(clockX, y + 2, width, height, color(0xFF21A2E9, 0xFF326C90), color(0xFF1385CA, 0xFF214B6B));
        g.fill(clockX, y + 2, clockX + 1, height, color(0xFF0E60B2, 0xFF102F4B));
        g.fill(clockX + 1, y + 2, clockX + 2, height, color(0xFF65C6F3, 0xFF699DBB));
        g.text(font, clock, clockX + (clockWidth - font.width(clock)) / 2, y + 8, 0xFFFFFFFF, false);
    }

    private static String clock() {
        long minute = System.currentTimeMillis() / 60_000L;
        if (minute != clockMinute) {
            clockMinute = minute;
            clockText = LocalTime.now().format(CLOCK_FORMAT);
        }
        return clockText;
    }

    private static void outline(GuiGraphicsExtractor g, int x, int y, int width, int height, int color) {
        if (width <= 1 || height <= 1) return;
        g.fill(x, y, x + width, y + 1, color);
        g.fill(x, y + height - 1, x + width, y + height, color);
        g.fill(x, y + 1, x + 1, y + height - 1, color);
        g.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    private static int mix(int a, int b, double progress) {
        int red = (int)(((a >>> 16) & 255) + (((b >>> 16) & 255) - ((a >>> 16) & 255)) * progress);
        int green = (int)(((a >>> 8) & 255) + (((b >>> 8) & 255) - ((a >>> 8) & 255)) * progress);
        int blue = (int)((a & 255) + ((b & 255) - (a & 255)) * progress);
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }
}
