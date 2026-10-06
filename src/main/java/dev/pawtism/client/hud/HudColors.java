package dev.pawtism.client.hud;

import dev.pawtism.client.PawtismConfig;

/** All Pawtism panels read the same existing accent and the two configurable HUD color roles. */
public final class HudColors {
    private static final int DEFAULT_TEXT = 0xFFF2F2F7;
    private static final int DEFAULT_BACKGROUND = 0xA014141D;
    private HudColors() {}

    public static int accent() { return PawtismConfig.ACCENT.getIntegerValue(); }
    public static int text() { return PawtismConfig.HUD_TEXT.getIntegerValue(); }
    public static int background() { return PawtismConfig.HUD_BACKGROUND.getIntegerValue(); }
    public static int secondary() { return text() == DEFAULT_TEXT ? 0xFFB8B8C6 : text(); }
    public static int surface(int original) { return HudColorMath.surface(background(), DEFAULT_BACKGROUND, original); }
    public static int pressedText() { return HudColorMath.contrastingText(accent(), 0xFF16131E, 0xFFF2F2F7); }
}
