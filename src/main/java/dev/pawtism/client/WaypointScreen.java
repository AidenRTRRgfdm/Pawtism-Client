package dev.pawtism.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

/** A local waypoint editor: current position or user-entered coordinates. */
public final class WaypointScreen extends Screen {
    private final Screen parent;
    private EditBox name, x, y, z, color;
    private Waypoint editing;
    private int page;
    private int rows;
    private int left;
    private int panelWidth;
    private String message = "";

    public WaypointScreen(Screen parent) {
        super(Component.literal("Pawtism — Waypoints"));
        this.parent = parent;
    }

    @Override protected void init() {
        panelWidth = Math.min(480, width - 24);
        left = (width - panelWidth) / 2;
        rows = Math.max(1, (height - 224) / 24);
        String oldName = name == null ? "" : name.getValue();
        String oldX = x == null ? coord(minecraft.player == null ? 0 : minecraft.player.getX()) : x.getValue();
        String oldY = y == null ? coord(minecraft.player == null ? 0 : minecraft.player.getY()) : y.getValue();
        String oldZ = z == null ? coord(minecraft.player == null ? 0 : minecraft.player.getZ()) : z.getValue();
        String oldColor = color == null ? String.format(Locale.ROOT, "%06X", PawtismConfig.ACCENT.getIntegerValue() & 0xffffff) : color.getValue();
        name = field(left, 46, panelWidth, "Waypoint name", oldName, 48);
        name.setHint(Component.literal("Name this location"));
        int coordWidth = Math.max(40, (panelWidth - 104) / 3);
        x = field(left, 78, coordWidth, "X coordinate", oldX, 18);
        y = field(left + coordWidth + 6, 78, coordWidth, "Y coordinate", oldY, 18);
        z = field(left + (coordWidth + 6) * 2, 78, coordWidth, "Z coordinate", oldZ, 18);
        color = field(left + panelWidth - 86, 78, 86, "Color RRGGBB", oldColor, 9);
        int half = (panelWidth - 6) / 2;
        addRenderableWidget(Button.builder(Component.literal(editing == null ? "Save waypoint" : "Save changes"), b -> saveEditor())
                .bounds(left, 108, half, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Use current position"), b -> useCurrent())
                .bounds(left + half + 6, 108, half, 20).build());

        List<Waypoint> points = WaypointManager.points();
        int pages = Math.max(1, (points.size() + rows - 1) / rows);
        page = Math.max(0, Math.min(page, pages - 1));
        for (int i = page * rows; i < Math.min(points.size(), (page + 1) * rows); i++) {
            Waypoint point = points.get(i);
            int rowY = 153 + (i - page * rows) * 24;
            addRenderableWidget(Button.builder(Component.literal(point.visible() ? "Hide" : "Show"), b -> {
                WaypointManager.replace(point.withVisibility(!point.visible())); rebuildWidgets();
            }).bounds(left + panelWidth - 152, rowY, 46, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Edit"), b -> edit(point))
                    .bounds(left + panelWidth - 102, rowY, 46, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Delete"), b -> {
                WaypointManager.remove(point.id()); if (editing != null && editing.id().equals(point.id())) editing = null;
                rebuildWidgets();
            }).bounds(left + panelWidth - 52, rowY, 52, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("<"), b -> { page--; rebuildWidgets(); })
                .bounds(left, height - 51, 30, 20).build()).active = page > 0;
        addRenderableWidget(Button.builder(Component.literal(">"), b -> { page++; rebuildWidgets(); })
                .bounds(left + 36, height - 51, 30, 20).build()).active = page + 1 < pages;
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(left + panelWidth - 76, height - 51, 76, 20).build());
        setInitialFocus(name);
    }

    private EditBox field(int px, int py, int w, String narration, String value, int maxLength) {
        EditBox field = addRenderableWidget(new EditBox(font, px, py, w, 20, Component.literal(narration)));
        field.setMaxLength(maxLength);
        field.setValue(value);
        return field;
    }

    private void saveEditor() {
        try {
            String hex = color.getValue().strip().replace("#", "");
            if (!hex.matches("(?i)[0-9a-f]{6}")) throw new IllegalArgumentException("Color must be six hexadecimal digits, e.g. F5A9CA");
            int rgb = 0xff000000 | Integer.parseInt(hex, 16);
            double px = Double.parseDouble(x.getValue().strip());
            double py = Double.parseDouble(y.getValue().strip());
            double pz = Double.parseDouble(z.getValue().strip());
            Waypoint point = editing == null
                    ? Waypoint.create(name.getValue(), WaypointManager.dimension(), px, py, pz, rgb)
                    : new Waypoint(editing.id(), name.getValue(), editing.dimension(), px, py, pz, rgb, editing.visible());
            if (editing == null) WaypointManager.add(point); else WaypointManager.replace(point);
            message = WaypointManager.error().isEmpty() ? "Saved " + point.name() : WaypointManager.error();
            editing = null;
            name.setValue("");
            rebuildWidgets();
        } catch (NumberFormatException invalid) {
            message = "Enter valid numbers for X, Y and Z.";
        } catch (IllegalArgumentException invalid) {
            message = invalid.getMessage();
        }
    }

    private void edit(Waypoint point) {
        editing = point;
        name.setValue(point.name());
        x.setValue(coord(point.x())); y.setValue(coord(point.y())); z.setValue(coord(point.z()));
        color.setValue(String.format(Locale.ROOT, "%06X", point.color() & 0xffffff));
        message = "Editing " + point.name() + " in " + point.dimension();
        rebuildWidgets();
    }

    private void useCurrent() {
        if (minecraft.player == null) return;
        editing = null;
        x.setValue(coord(minecraft.player.getX()));
        y.setValue(coord(minecraft.player.getY()));
        z.setValue(coord(minecraft.player.getZ()));
        message = "Current position in " + WaypointManager.dimension();
        rebuildWidgets();
    }

    private static String coord(double value) { return String.format(Locale.ROOT, "%.1f", value); }

    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0xe012101b);
        graphics.fill(left - 8, 22, left + panelWidth + 8, height - 24, 0xdc1e1828);
        graphics.centeredText(font, title, width / 2, 28, PawtismConfig.ACCENT.getIntegerValue());
        graphics.text(font, "X", x.getX(), 68, 0xffbcb6c6);
        graphics.text(font, "Y", y.getX(), 68, 0xffbcb6c6);
        graphics.text(font, "Z", z.getX(), 68, 0xffbcb6c6);
        graphics.text(font, "Color", color.getX(), 68, 0xffbcb6c6);
        graphics.text(font, "Saved locations · " + WaypointManager.dimension().replace("minecraft:", ""), left, 138, 0xffbcb6c6);
        List<Waypoint> points = WaypointManager.points();
        for (int i = page * rows; i < Math.min(points.size(), (page + 1) * rows); i++) {
            Waypoint p = points.get(i);
            int rowY = 153 + (i - page * rows) * 24;
            String display = font.plainSubstrByWidth(p.name(), Math.max(30, panelWidth - 162));
            graphics.text(font, display, left, rowY + 1, p.visible() ? p.color() : 0xff827b8c);
            graphics.text(font, font.plainSubstrByWidth(p.dimension().replace("minecraft:", "") + " · " + coord(p.x()) + ", " + coord(p.y()) + ", " + coord(p.z()),
                    Math.max(30, panelWidth - 162)), left, rowY + 11, 0xffa39bae);
        }
        if (points.isEmpty()) graphics.text(font, "No locations yet. Save your current position above.", left, 160, 0xffa39bae);
        String status = message.isEmpty() ? WaypointManager.error() : message;
        if (!status.isEmpty()) graphics.text(font, font.plainSubstrByWidth(status, panelWidth), left, height - 68, 0xffffd394);
        graphics.text(font, "Page " + (page + 1) + " · B toggles markers", left + 75, height - 45, 0xffbcb6c6);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override public void onClose() { minecraft.gui.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}
