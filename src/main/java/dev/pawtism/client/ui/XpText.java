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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

/** Keeps native text, links and scrolling readable on the light menu surfaces. */
public final class XpText {
    private static int tooltipDepth;
    private XpText() {}

    public static void enterTooltip() { tooltipDepth++; }
    public static void leaveTooltip() { tooltipDepth = Math.max(0, tooltipDepth - 1); }

    public static boolean lightSurface(int y) {
        if (!XpTheme.enabled() || tooltipDepth != 0 || y < 19) return false;
        Screen screen = Minecraft.getInstance().gui.screen();
        return screen != null && !screen.isInGameUi()
            && (!(screen instanceof GuiBase) || screen instanceof PawtismScreen)
            && !(screen instanceof XpTitleScreen) && !(screen instanceof PauseScreen)
            && !(screen instanceof HudEditorScreen) && !(screen instanceof ChatScreen);
    }

    public static boolean pale(int color) {
        int r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255;
        return r >= 185 && g >= 185 && b >= 185 && Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b)) <= 30;
    }

    public static FormattedCharSequence decorate(FormattedCharSequence text, int y) {
        if (!lightSurface(y)) return text;
        return sink -> text.accept((index, style, codePoint) -> sink.accept(index, darkStyle(style), codePoint));
    }

    private static Style darkStyle(Style style) {
        return style.getColor() == null || pale(style.getColor().getValue()) ? style.withColor(0x202638) : style;
    }

    public static ActiveTextCollector wrap(ActiveTextCollector original) {
        if (!XpTheme.enabled()) return original;
        return new ActiveTextCollector() {
            @Override public Parameters defaultParameters() { return original.defaultParameters(); }
            @Override public void defaultParameters(Parameters parameters) { original.defaultParameters(parameters); }
            @Override public void accept(TextAlignment alignment, int x, int y, Parameters parameters, FormattedCharSequence text) {
                original.accept(alignment, x, y, parameters, decorate(text, y));
            }
            @Override public void acceptScrolling(Component message, int centerX, int left, int right, int top, int bottom, Parameters parameters) {
                // The native helper calls this collector again, keeping styled runs and the same clipping rules.
                defaultScrollingHelper(message, centerX, left, right, top, bottom,
                    Minecraft.getInstance().font.width(message), 9, parameters);
            }
        };
    }
}
