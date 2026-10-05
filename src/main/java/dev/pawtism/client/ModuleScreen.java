package dev.pawtism.client;

import dev.pawtism.client.hud.HudEditorScreen;
import dev.pawtism.client.ui.XpTheme;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Custom browser for original modules; detailed values are edited through MaLiLib. */
public final class ModuleScreen extends Screen {
    private final Screen parent;
    private String category = "All", query = "";
    private int page, left, columns, rows, cardWidth;
    private List<ModuleCatalog.Module> modules = List.of();
    public ModuleScreen(Screen parent) { super(Component.literal("Pawtism Client")); this.parent = parent; }
    @Override protected void init() {
        left = 118;
        columns = width >= 600 ? 3 : 2;
        cardWidth = Math.max(95, (width - left - 18) / columns);
        rows = Math.max(1, (height - 134) / 55);
        EditBox search = addRenderableWidget(new EditBox(font, left, 43, width - left - 12, 20, Component.literal("Search modules")));
        search.setMaxLength(100); search.setValue(query); search.setHint(Component.literal("Search modules…"));
        search.setResponder(value -> { if (!query.equals(value)) { query = value; page = 0; rebuildWidgets(); } });
        int cy = 44;
        for (String group : ModuleCatalog.CATEGORIES) {
            Button button = addRenderableWidget(Button.builder(Component.literal(group), b -> { category = group; page = 0; rebuildWidgets(); })
                .bounds(10, cy, 98, 21).build());
            button.active = !group.equals(category); cy += 27;
        }
        modules = ModuleCatalog.modules(category, query);
        int perPage = columns * rows;
        int pages = Math.max(1, (modules.size() + perPage - 1) / perPage);
        page = Math.max(0, Math.min(page, pages - 1));
        for (int index = page * perPage; index < Math.min(modules.size(), (page + 1) * perPage); index++) {
            ModuleCatalog.Module module = modules.get(index);
            int local = index - page * perPage, x = left + (local % columns) * cardWidth, y = 73 + (local / columns) * 55;
            Button toggle = addRenderableWidget(Button.builder(Component.literal(module.option().getBooleanValue() ? "ON" : "OFF"), b -> {
                module.option().setBooleanValue(!module.option().getBooleanValue()); PawtismConfig.INSTANCE.save();
                b.setMessage(Component.literal(module.option().getBooleanValue() ? "ON" : "OFF"));
            }).bounds(x + cardWidth - 47, y + 27, 39, 18).build());
            toggle.setTooltip(Tooltip.create(Component.literal(module.option().getComment())));
        }
        addRenderableWidget(Button.builder(Component.literal("<"), b -> { page--; rebuildWidgets(); }).bounds(left, height - 57, 26, 20).build()).active = page > 0;
        addRenderableWidget(Button.builder(Component.literal(">"), b -> { page++; rebuildWidgets(); }).bounds(left + 32, height - 57, 26, 20).build()).active = page + 1 < pages;
        int settingsWidth = XpTheme.enabled() ? Math.min(133, Math.max(80, width - left - 76)) : 133;
        addRenderableWidget(Button.builder(Component.literal("Module settings"), b -> minecraft.gui.setScreen(new PawtismScreen(this, ModuleCatalog.options(category))))
            .bounds(width - settingsWidth - 12, height - 57, settingsWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("HUD Editor"), b -> minecraft.gui.setScreen(new HudEditorScreen(this))).bounds(10, height - 29, 98, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Waypoints"), b -> minecraft.gui.setScreen(new WaypointScreen(this))).bounds(118, height - 29, 98, 20).build()).active = minecraft.player != null;
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose()).bounds(width - 80, height - 29, 68, 20).build());
        if (!query.isEmpty()) { setInitialFocus(search); search.setCursorPosition(query.length()); }
    }
    @Override public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        if (!XpTheme.enabled()) super.extractBackground(g, mouseX, mouseY, delta);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        boolean xp = XpTheme.enabled();
        if (xp) {
            if (minecraft.level == null) XpTheme.desktop(g, font, width, height);
            else g.fill(0, 0, width, height, XpTheme.color(0x58000000, 0x7808101d));
            XpTheme.frame(g, font, 5, 3, width - 10, height - 6, "Pawtism Client: Modules", false);
            g.text(font, "26.2 · " + ModuleCatalog.modules("All", "").size() + " original modules", 12, 29, XpTheme.mutedText(), false);
            XpTheme.panel(g, 8, 41, 102, Math.max(22, height - 76));
            XpTheme.panel(g, left - 4, 69, width - left - 8, Math.max(54, height - 139));
        } else {
            g.fill(0, 0, width, height, 0xf012101b);
            g.fill(0, 0, width, 35, 0xff22192c);
            g.text(font, "PAWTISM", 12, 10, PawtismConfig.ACCENT.getIntegerValue(), true);
            g.text(font, "26.2 · " + ModuleCatalog.modules("All", "").size() + " original modules", 80, 12, 0xffb8b8c6, true);
        }
        int perPage = columns * rows;
        for (int index = page * perPage; index < Math.min(modules.size(), (page + 1) * perPage); index++) {
            ModuleCatalog.Module module = modules.get(index);
            int local = index - page * perPage, x = left + (local % columns) * cardWidth, y = 73 + (local / columns) * 55;
            if (xp) {
                XpTheme.panel(g, x, y, cardWidth - 5, 50);
                g.fill(x + 1, y + 1, x + cardWidth - 6, y + 49, XpTheme.content());
                g.fill(x + 1, y + 1, x + 3, y + 49, module.option().getBooleanValue()
                    ? XpTheme.color(XpTheme.BLUE, 0xff6da7ed) : XpTheme.color(0xffaca899, 0xff566171));
            } else {
                g.fill(x, y, x + cardWidth - 5, y + 50, 0xff25202e);
                g.fill(x, y, x + 2, y + 50, module.option().getBooleanValue() ? PawtismConfig.ACCENT.getIntegerValue() : 0xff61536d);
            }
            g.text(font, font.plainSubstrByWidth(module.option().getConfigGuiDisplayName(), cardWidth - 16), x + 7, y + 7,
                xp ? XpTheme.text() : 0xfff2f2f7, !xp);
            g.text(font, module.category(), x + 7, y + 31, xp ? XpTheme.mutedText() : 0xffa39bae, false);
        }
        if (modules.isEmpty()) g.text(font, "No modules match your search.", left, 80, xp ? XpTheme.mutedText() : 0xffb8b8c6, !xp);
        boolean compactFooter = xp && width < 440;
        g.text(font, (page + 1) + " / " + Math.max(1, (modules.size() + perPage - 1) / perPage) + " · " + modules.size() + " modules",
            compactFooter ? left : left + 68, height - (compactFooter ? 71 : 51), xp ? XpTheme.mutedText() : 0xffb8b8c6, !xp);
        super.extractRenderState(g, mouseX, mouseY, delta);
    }
    @Override public void onClose() { PawtismConfig.INSTANCE.save(); minecraft.gui.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}
