package dev.pawtism.client.hud;

import dev.pawtism.client.PawtismConfig;
import dev.pawtism.client.ui.XpText;
import dev.pawtism.client.ui.XpTheme;
import fi.dy.masa.malilib.config.options.ConfigColor;
import fi.dy.masa.malilib.gui.GuiColorEditorHSV;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Focused access to the same persisted color values used by every Pawtism HUD panel. */
public final class HudColorScreen extends Screen {
    private record Row(ConfigColor config, Button reset, int y) {}
    private final Screen parent;
    private final List<Row> rows = new ArrayList<>();
    private int left, contentWidth, pickerWidth;

    public HudColorScreen(Screen parent) {
        super(Component.literal("Pawtism: HUD Colors"));
        this.parent = parent;
    }

    public static void open(Screen parent) { Minecraft.getInstance().gui.setScreen(new HudColorScreen(parent)); }

    @Override protected void init() {
        rows.clear();
        contentWidth = Math.min(480, width - 24);
        left = (width - contentWidth) / 2;
        pickerWidth = contentWidth - 132;
        ConfigColor[] colors = {PawtismConfig.ACCENT, PawtismConfig.HUD_TEXT, PawtismConfig.HUD_BACKGROUND};
        for (int index = 0; index < colors.length; index++) {
            ConfigColor config = colors[index];
            int y = 58 + index * 28;
            Button picker = addRenderableWidget(Button.builder(Component.literal(config.getConfigGuiDisplayName()),
                button -> minecraft.gui.setScreen(new LiveColorPicker(config, this)))
                .bounds(left, y, pickerWidth, 20).build());
            picker.setTooltip(Tooltip.create(Component.literal(config.getComment())));
            Button reset = addRenderableWidget(Button.builder(Component.literal("Reset"), button -> {
                config.resetToDefault();
                PawtismConfig.INSTANCE.save();
            }).bounds(left + contentWidth - 44, y, 44, 20).build());
            reset.setTooltip(Tooltip.create(Component.literal("Reset " + config.getConfigGuiDisplayName().toLowerCase(java.util.Locale.ROOT))));
            rows.add(new Row(config, reset, y));
        }
        addRenderableWidget(Button.builder(Component.literal("Reset colors"), button -> {
            for (ConfigColor color : colors) color.resetToDefault();
            PawtismConfig.INSTANCE.save();
        }).bounds(width / 2 - 104, height - 27, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
            .bounds(width / 2 + 4, height - 27, 100, 20).build());
    }

    @Override public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        if (!XpTheme.enabled()) super.extractBackground(g, mouseX, mouseY, delta);
    }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        if (XpTheme.enabled()) {
            if (minecraft.level == null) XpTheme.desktop(g, font, width, height);
            else g.fill(0, 0, width, height, XpTheme.color(0x58000000, 0x7808101D));
            XpTheme.frame(g, font, 5, 3, width - 10, height - 6, "Pawtism: HUD Colors", false);
        } else {
            g.fill(0, 0, width, height, 0xF012101B);
            g.text(font, title, 12, 10, 0xFFF2F2F7, true);
        }
        int label = XpTheme.enabled() ? XpTheme.text() : 0xFFF2F2F7;
        int muted = XpTheme.enabled() ? XpTheme.mutedText() : 0xFFB8B8C6;
        g.text(font, "Changes apply immediately.", left, 31, label, false);
        g.text(font, "Accent also colors the crosshair.", left, 42, muted, false);
        for (Row row : rows) {
            row.reset().active = row.config().isModified();
            int swatchX = left + pickerWidth + 6;
            checkerboard(g, swatchX, row.y() + 2, 16, 16);
            g.fill(swatchX, row.y() + 2, swatchX + 16, row.y() + 18, row.config().getIntegerValue());
            g.text(font, row.config().getStringValue(), swatchX + 22, row.y() + 6, label, false);
        }
        g.text(font, "Live preview", left, 144, label, false);
        drawPreview(g, left, 156, contentWidth, true);
        super.extractRenderState(g, mouseX, mouseY, delta);
    }

    private static void drawPreview(GuiGraphicsExtractor g, int x, int y, int width, boolean crosshair) {
        int panelWidth = crosshair ? width - 64 : width;
        checkerboard(g, x, y, panelWidth, 41);
        XpText.enterTooltip();
        try {
            HudWidgets.panel(g, x, y, panelWidth, List.of("HUD text preview", "FPS · ping · sprint",
                crosshair ? "Colors update live" : "Esc to return · Changes saved"));
            if (crosshair) {
                int cx = x + width - 29, cy = y + 17;
                int color = HudColors.accent();
                for (int sign : new int[]{-1, 1}) {
                    int near = sign < 0 ? cx - 7 : cx + 3;
                    g.fill(near - 1, cy - 1, near + 6, cy + 2, 0xBF000000);
                    g.fill(near, cy, near + 5, cy + 1, color);
                    near = sign < 0 ? cy - 7 : cy + 3;
                    g.fill(cx - 1, near - 1, cx + 2, near + 6, 0xBF000000);
                    g.fill(cx, near, cx + 1, near + 5, color);
                }
                Minecraft mc = Minecraft.getInstance();
                g.centeredText(mc.font, "Crosshair", cx, y + 31, XpTheme.enabled() ? XpTheme.text() : 0xFFF2F2F7);
            }
        } finally {
            XpText.leaveTooltip();
        }
    }

    private static void checkerboard(GuiGraphicsExtractor g, int x, int y, int width, int height) {
        for (int row = 0; row < 3; row++) for (int column = 0; column < 8; column++) {
            int color = (row + column) % 2 == 0 ? XpTheme.color(0xFFE3E3E3, 0xFF445063)
                : XpTheme.color(0xFFB7B7B7, 0xFF303948);
            g.fill(x + width * column / 8, y + height * row / 3,
                x + width * (column + 1) / 8, y + height * (row + 1) / 3, color);
        }
    }

    @Override public void onClose() { PawtismConfig.INSTANCE.save(); minecraft.gui.setScreen(parent); }
    @Override public void removed() { PawtismConfig.INSTANCE.save(); }
    @Override public boolean isPauseScreen() { return false; }

    /** Native HSV/RGB/alpha controls with a HUD preview that follows the current picker value. */
    private static final class LiveColorPicker extends GuiColorEditorHSV {
        private LiveColorPicker(ConfigColor config, Screen parent) {
            super(config, null, parent);
            title = config.getConfigGuiDisplayName();
        }
        @Override public void initGui() {
            setPosition((width - dialogWidth) / 2, Math.max(3, (height - dialogHeight - 48) / 2));
            super.initGui();
        }
        @Override protected void drawScreenBackground(fi.dy.masa.malilib.render.GuiContext context, int mouseX, int mouseY) {
            // The native picker draws its parent first; hide those controls behind the active dialog.
            context.getGuiGraphics().fill(0, 0, width, height, XpTheme.enabled() ? XpTheme.body() : 0xFF12101B);
            super.drawScreenBackground(context, mouseX, mouseY);
        }
        @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
            if (config.getIntegerValue() != color) config.setIntegerValue(color);
            super.extractRenderState(g, mouseX, mouseY, delta);
            if (config.getIntegerValue() != color) config.setIntegerValue(color);
            drawPreview(g, dialogLeft, dialogTop + dialogHeight + 5, dialogWidth, false);
        }
        @Override public void removed() { super.removed(); PawtismConfig.INSTANCE.save(); }
        @Override public boolean isPauseScreen() { return false; }
    }
}
