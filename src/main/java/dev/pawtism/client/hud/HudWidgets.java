package dev.pawtism.client.hud;

import com.google.gson.JsonObject;
import dev.pawtism.client.PawtismConfig;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

/** One registry for all local HUD panels, with independent persistent positions. */
public final class HudWidgets {
    @FunctionalInterface public interface Renderer { void draw(GuiGraphicsExtractor graphics, int x, int y); }
    public record Widget(String id, String title, int defaultX, int defaultY, int width, int height,
                         BooleanSupplier enabled, Renderer renderer) {}
    public record Rect(int x, int y, int width, int height) {
        public boolean contains(double px, double py) { return px >= x && py >= y && px < x + width && py < y + height; }
    }
    private static final Map<String, Widget> WIDGETS = new LinkedHashMap<>();
    private static final Map<String, HudLayout.Position> POSITIONS = new LinkedHashMap<>();
    private HudWidgets() {}
    public static void register(String id, String title, int x, int y, int width, int height,
                                BooleanSupplier enabled, Renderer renderer) {
        if (WIDGETS.putIfAbsent(id, new Widget(id, title, x, y, width, height, enabled, renderer)) != null)
            throw new IllegalArgumentException("Duplicate HUD widget: " + id);
    }
    public static void init() {
        JsonObject state = PawtismConfig.extras();
        if (state.has("hudLayout") && state.get("hudLayout").isJsonObject()) {
            JsonObject saved = state.getAsJsonObject("hudLayout");
            for (String id : WIDGETS.keySet()) {
                try {
                    JsonObject pos = saved.getAsJsonObject(id);
                    if (pos != null && pos.get("x").isJsonPrimitive() && pos.get("x").getAsJsonPrimitive().isNumber()
                        && pos.get("y").isJsonPrimitive() && pos.get("y").getAsJsonPrimitive().isNumber())
                        POSITIONS.put(id, new HudLayout.Position(pos.get("x").getAsDouble(), pos.get("y").getAsDouble()));
                } catch (RuntimeException ignored) { /* A bad panel position falls back independently. */ }
            }
        }
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("pawtism", "modules"), (g, delta) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.level != null && mc.gui.screen() == null && !mc.gui.hud.isHidden()
                && !mc.getDebugOverlay().showDebugScreen()) draw(g);
        });
    }
    public static List<Widget> widgets() { return new ArrayList<>(WIDGETS.values()); }
    public static Rect rect(Widget widget, int width, int height) {
        HudLayout.Position pos = POSITIONS.get(widget.id());
        int x = pos == null ? (widget.defaultX() < 0 ? width + widget.defaultX() : widget.defaultX())
            : HudLayout.resolve(pos.x(), width, widget.width());
        int y = pos == null ? (widget.defaultY() < 0 ? height + widget.defaultY() : widget.defaultY())
            : HudLayout.resolve(pos.y(), height, widget.height());
        return new Rect(HudLayout.clamp(x, width, widget.width()), HudLayout.clamp(y, height, widget.height()), widget.width(), widget.height());
    }
    public static void move(Widget widget, int x, int y, int width, int height) {
        POSITIONS.put(widget.id(), HudLayout.normalized(x, y, width, height, widget.width(), widget.height()));
    }
    public static void reset() { POSITIONS.clear(); save(); }
    public static void save() {
        JsonObject saved = new JsonObject();
        POSITIONS.forEach((id, pos) -> {
            JsonObject entry = new JsonObject(); entry.addProperty("x", pos.x()); entry.addProperty("y", pos.y()); saved.add(id, entry);
        });
        PawtismConfig.extras().add("hudLayout", saved);
        PawtismConfig.INSTANCE.save();
    }
    public static void draw(GuiGraphicsExtractor g) {
        for (Widget widget : WIDGETS.values()) {
            if (widget.enabled().getAsBoolean()) {
                Rect rect = rect(widget, g.guiWidth(), g.guiHeight());
                widget.renderer().draw(g, rect.x(), rect.y());
            }
        }
    }
    public static void panel(GuiGraphicsExtractor g, int x, int y, int width, List<String> lines) {
        if (lines.isEmpty()) return;
        int height = lines.size() * 11 + 8;
        g.fill(x, y, x + width, y + height, HudColors.background());
        g.fill(x, y, x + 2, y + height, HudColors.accent());
        Minecraft mc = Minecraft.getInstance();
        g.enableScissor(x + 4, y, x + width - 3, y + height);
        for (int i = 0; i < lines.size(); i++) g.text(mc.font, lines.get(i), x + 6, y + 4 + i * 11, HudColors.text(), true);
        g.disableScissor();
    }
}
