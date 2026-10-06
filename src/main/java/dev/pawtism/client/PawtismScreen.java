package dev.pawtism.client;

import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptions;
import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.util.StringUtils;
import dev.pawtism.client.ui.XpTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import java.util.List;

/** MaLiLib supplies the editable toggles, sliders, colors and reset buttons. */
public final class PawtismScreen extends GuiConfigsBase {
    public PawtismScreen(Screen parent) {
        this(parent, PawtismConfig.OPTIONS);
    }
    private final List<fi.dy.masa.malilib.config.IConfigBase> options;
    public PawtismScreen(Screen parent, List<fi.dy.masa.malilib.config.IConfigBase> options) {
        super(10, 42, "pawtism", parent, "Pawtism Client: Settings");
        this.options = options;
        // MaLiLib 0.29.6 accepts a parent argument but does not assign it in GuiConfigsBase.
        setParent(parent);
        setConfigWidth(180);
    }
    @Override public List<ConfigOptionWrapper> getConfigs() {
        return ConfigOptionWrapper.createFor(options);
    }
    @Override public void initGui() {
        int labelWidth = options.stream().mapToInt(option -> font.width(option.getConfigGuiDisplayName())).max().orElse(0);
        int resetWidth = font.width(StringUtils.translate("malilib.gui.button.reset.caps")) + 10;
        // Native rows reserve a label gap, reset gap, scrollbar and the window's outer padding.
        setConfigWidth(Math.min(180, Math.max(80, width - labelWidth - resetWidth - 48)));
        if (getListWidget() instanceof ResponsiveConfigList list) list.setValueColumnWidth(getConfigWidth());
        super.initGui();
    }
    @Override protected WidgetListConfigOptions createListWidget(int x, int y) {
        return new ResponsiveConfigList(x, y, getBrowserWidth(), getBrowserHeight(), getConfigWidth(), useKeybindSearch(), this);
    }
    public static boolean xpControls() {
        return XpTheme.enabled() && Minecraft.getInstance().gui.screen() instanceof PawtismScreen;
    }
    @Override protected void drawScreenBackground(GuiContext context, int mouseX, int mouseY) {
        boolean xp = XpTheme.enabled();
        if (modSwitchWidget != null) modSwitchWidget.setPosition(width - 155, xp ? 26 : 6);
        if (!xp) {
            super.drawScreenBackground(context, mouseX, mouseY);
            drawDiscordStatus(context);
            return;
        }
        GuiGraphicsExtractor graphics = context.getGuiGraphics();
        graphics.fill(0, 0, width, height, XpTheme.body());
        XpTheme.frame(graphics, font, 5, 3, width - 10, height - 6, "Pawtism Client: Settings", false);
        XpTheme.panel(graphics, 8, 40, width - 16, Math.max(24, height - 78));
        graphics.fill(9, 41, width - 9, Math.max(65, height - 39), XpTheme.content());
        drawDiscordStatus(context);
    }
    private void drawDiscordStatus(GuiContext context) {
        if (!options.contains(dev.pawtism.client.discord.DiscordOptions.RICH_PRESENCE)) return;
        String message = dev.pawtism.client.discord.DiscordPresence.status().message();
        context.getGuiGraphics().text(font, font.plainSubstrByWidth(message, Math.max(0, width - 24)),
            12, height - 25, XpTheme.enabled() ? XpTheme.text() : 0xFFF2F2F7, false);
    }
    @Override protected void drawTitle(GuiContext context, int mouseX, int mouseY, float delta) {
        if (!XpTheme.enabled()) super.drawTitle(context, mouseX, mouseY, delta);
    }
    @Override public void removed() {
        super.removed();
        PawtismConfig.INSTANCE.save();
    }
    private static final class ResponsiveConfigList extends WidgetListConfigOptions {
        private ResponsiveConfigList(int x, int y, int width, int height, int valueWidth, boolean keybindSearch, GuiConfigsBase parent) {
            super(x, y, width, height, valueWidth, 0.0F, keybindSearch, parent);
        }
        private void setValueColumnWidth(int valueWidth) { configWidth = valueWidth; }
    }
}
