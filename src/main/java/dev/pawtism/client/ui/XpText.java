package dev.pawtism.client.ui;

import net.minecraft.client.Minecraft;
import dev.pawtism.client.hud.HudEditorScreen;
import dev.pawtism.client.PawtismScreen;
import fi.dy.masa.malilib.gui.GuiBase;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

/** Keeps native text, links and scrolling readable on themed menu surfaces. */
public final class XpText {
    private static int tooltipDepth;
    private static int flatDepth;
    private XpText() {}

    public static void enterTooltip() { tooltipDepth++; }
    public static void leaveTooltip() { tooltipDepth = Math.max(0, tooltipDepth - 1); }
    public static boolean flatText() { return XpTheme.enabled() && tooltipDepth == 0 && flatDepth > 0; }

    public static boolean lightSurface(int y) {
        return !XpTheme.dark() && themedSurface(y);
    }

    public static boolean themedSurface(int y) {
        if (!XpTheme.enabled() || tooltipDepth != 0) return false;
        Screen screen = Minecraft.getInstance().gui.screen();
        if (screen == null || screen instanceof XpTitleScreen || screen instanceof PauseScreen
            || screen instanceof HudEditorScreen || screen instanceof ChatScreen
            || screen instanceof GuiBase && !(screen instanceof PawtismScreen)) return false;
        // Container captions use local coordinates below the screen's translated pose.
        if (screen instanceof AbstractContainerScreen<?>) return XpTheme.dark();
        return !screen.isInGameUi() && y >= 19;
    }

    public static boolean pale(int color) { return XpPalette.pale(color); }

    public static boolean needsColor(int original, int y) {
        return themedSurface(y) && XpPalette.menuColor(original, XpTheme.dark()) != original;
    }

    public static int color(int original, int y) {
        return themedSurface(y) ? XpPalette.menuColor(original, XpTheme.dark()) : original;
    }

    public static FormattedCharSequence decorate(FormattedCharSequence text, int y) {
        if (!themedSurface(y)) return text;
        boolean dark = XpTheme.dark();
        return sink -> text.accept((index, style, codePoint) -> sink.accept(index, themedStyle(style, dark), codePoint));
    }

    private static Style themedStyle(Style style, boolean dark) {
        if (style.getColor() == null) {
            return style.withColor((dark ? XpPalette.DARK_TEXT : XpPalette.LIGHT_NATIVE_TEXT) & 0x00FFFFFF);
        }
        int original = style.getColor().getValue();
        int replacement = XpPalette.menuColor(original, dark);
        return replacement == original ? style : style.withColor(replacement & 0x00FFFFFF);
    }

    public static ActiveTextCollector wrap(ActiveTextCollector original) {
        return wrap(original, false);
    }

    public static ActiveTextCollector wrap(ActiveTextCollector original, boolean widget) {
        if (!XpTheme.enabled()) return original;
        return new ActiveTextCollector() {
            @Override public Parameters defaultParameters() { return original.defaultParameters(); }
            @Override public void defaultParameters(Parameters parameters) { original.defaultParameters(parameters); }
            @Override public void accept(TextAlignment alignment, int x, int y, Parameters parameters, FormattedCharSequence text) {
                Screen screen = Minecraft.getInstance().gui.screen();
                boolean flatten = XpTheme.enabled() && screen != null && !(screen instanceof ChatScreen)
                    && tooltipDepth == 0 && (widget || themedSurface(y));
                if (flatten) flatDepth++;
                try {
                    original.accept(alignment, x, y, parameters, decorate(text, y));
                } finally {
                    if (flatten) flatDepth--;
                }
            }
            @Override public void acceptScrolling(Component message, int centerX, int left, int right, int top, int bottom, Parameters parameters) {
                // The native helper calls this collector again, keeping styled runs and the same clipping rules.
                defaultScrollingHelper(message, centerX, left, right, top, bottom,
                    Minecraft.getInstance().font.width(message), 9, parameters);
            }
        };
    }
}
