package dev.pawtism.client;

import dev.pawtism.client.hud.HudEditorScreen;
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
        addRenderableWidget(Button.builder(Component.literal("Module settings"), b -> minecraft.gui.setScreen(new PawtismScreen(this, ModuleCatalog.options(category))))
            .bounds(width - 145, height - 57, 133, 20).build());
        addRenderableWidget(Button.builder(Component.literal("HUD Editor"), b -> minecraft.gui.setScreen(new HudEditorScreen(this))).bounds(10, height - 29, 98, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Waypoints"), b -> minecraft.gui.setScreen(new WaypointScreen(this))).bounds(118, height - 29, 98, 20).build()).active = minecraft.player != null;
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose()).bounds(width - 80, height - 29, 68, 20).build());
        if (!query.isEmpty()) { setInitialFocus(search); search.setCursorPosition(query.length()); }
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, width, height, 0xf012101b);
        g.fill(0, 0, width, 35, 0xff22192c);
        g.text(font, "PAWTISM", 12, 10, PawtismConfig.ACCENT.getIntegerValue(), true);
        g.text(font, "26.2 · " + ModuleCatalog.modules("All", "").size() + " original modules", 80, 12, 0xffb8b8c6, true);
        int perPage = columns * rows;
        for (int index = page * perPage; index < Math.min(modules.size(), (page + 1) * perPage); index++) {
            ModuleCatalog.Module module = modules.get(index);
            int local = index - page * perPage, x = left + (local % columns) * cardWidth, y = 73 + (local / columns) * 55;
            g.fill(x, y, x + cardWidth - 5, y + 50, 0xff25202e);
            g.fill(x, y, x + 2, y + 50, module.option().getBooleanValue() ? PawtismConfig.ACCENT.getIntegerValue() : 0xff61536d);
            g.text(font, font.plainSubstrByWidth(module.option().getConfigGuiDisplayName(), cardWidth - 16), x + 7, y + 7, 0xfff2f2f7, true);
            g.text(font, module.category(), x + 7, y + 31, 0xffa39bae, false);
        }
        if (modules.isEmpty()) g.text(font, "No modules match your search.", left, 80, 0xffb8b8c6, true);
        g.text(font, (page + 1) + " / " + Math.max(1, (modules.size() + perPage - 1) / perPage) + " · " + modules.size() + " modules", left + 68, height - 51, 0xffb8b8c6, true);
        super.extractRenderState(g, mouseX, mouseY, delta);
    }
    @Override public void onClose() { PawtismConfig.INSTANCE.save(); minecraft.gui.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}
