package dev.pawtism.client.hud;

import dev.pawtism.client.ui.XpTheme;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class HudEditorScreen extends Screen {
    private final Screen parent;
    private HudWidgets.Widget selected;
    private int offsetX, offsetY;
    private boolean dragging;
    public HudEditorScreen(Screen parent) { super(Component.literal("Pawtism — HUD Editor")); this.parent = parent; }
    @Override protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Reset layout"), b -> HudWidgets.reset()).bounds(width / 2 - 104, height - 27, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Save & Done"), b -> onClose()).bounds(width / 2 + 4, height - 27, 100, 20).build());
    }
    @Override public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {}
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        boolean xp = XpTheme.enabled();
        g.fill(0, 0, width, height, xp ? 0x30000000 : 0x5012101b);
        if (minecraft.player != null) HudWidgets.draw(g);
        for (HudWidgets.Widget widget : HudWidgets.widgets()) {
            if (!widget.enabled().getAsBoolean()) continue;
            HudWidgets.Rect r = HudWidgets.rect(widget, width, height);
            int color = widget == selected ? (xp ? 0xffffc73c : 0xfff5a9ca) : 0x99ffffff;
            g.fill(r.x(), r.y(), r.x() + r.width(), r.y() + 1, color);
            g.fill(r.x(), r.y() + r.height() - 1, r.x() + r.width(), r.y() + r.height(), color);
            g.fill(r.x(), r.y(), r.x() + 1, r.y() + r.height(), color);
            g.fill(r.x() + r.width() - 1, r.y(), r.x() + r.width(), r.y() + r.height(), color);
            if (r.contains(mouseX, mouseY)) g.text(font, widget.title(), r.x() + 3, Math.max(30, r.y() - 11),
                xp && widget != selected ? 0xffcde5ff : color, true);
        }
        if (xp) {
            int headerWidth = Math.min(420, width - 8), headerX = (width - headerWidth) / 2;
            g.fill(headerX, 3, headerX + headerWidth, 30, 0xff003c74);
            g.fillGradient(headerX + 1, 4, headerX + headerWidth - 1, 29, 0xff4d91f2, XpTheme.BLUE);
            g.fill(headerX + 2, 4, headerX + headerWidth - 2, 5, 0xff85b4f5);
            g.centeredText(font, "Drag panels · Arrow keys move selected panel", width / 2, 6, 0xffffffff);
            g.centeredText(font, "Enable more modules in the module browser", width / 2, 17, 0xffe5efff);
        } else {
            g.fill(width / 2 - 174, 3, width / 2 + 174, 28, 0xdd14141d);
            g.centeredText(font, "Drag panels · Arrow keys move selected panel", width / 2, 6, 0xfff5a9ca);
            g.centeredText(font, "Enable more modules in the module browser", width / 2, 17, 0xffb8b8c6);
        }
        super.extractRenderState(g, mouseX, mouseY, delta);
    }
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) return true;
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        List<HudWidgets.Widget> widgets = HudWidgets.widgets();
        for (int i = widgets.size() - 1; i >= 0; i--) {
            HudWidgets.Widget widget = widgets.get(i);
            HudWidgets.Rect r = HudWidgets.rect(widget, width, height);
            if (widget.enabled().getAsBoolean() && r.contains(event.x(), event.y())) {
                selected = widget; dragging = true; offsetX = (int)event.x() - r.x(); offsetY = (int)event.y() - r.y(); return true;
            }
        }
        selected = null; return false;
    }
    @Override public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (dragging && selected != null) { HudWidgets.move(selected, (int)event.x() - offsetX, (int)event.y() - offsetY, width, height); return true; }
        return super.mouseDragged(event, dx, dy);
    }
    @Override public boolean mouseReleased(MouseButtonEvent event) {
        if (dragging) { dragging = false; HudWidgets.save(); return true; }
        return super.mouseReleased(event);
    }
    @Override public boolean keyPressed(KeyEvent event) {
        if (selected != null) {
            int dx = event.key() == GLFW.GLFW_KEY_LEFT ? -1 : event.key() == GLFW.GLFW_KEY_RIGHT ? 1 : 0;
            int dy = event.key() == GLFW.GLFW_KEY_UP ? -1 : event.key() == GLFW.GLFW_KEY_DOWN ? 1 : 0;
            if (dx != 0 || dy != 0) { HudWidgets.Rect r = HudWidgets.rect(selected, width, height); HudWidgets.move(selected, r.x() + dx, r.y() + dy, width, height); return true; }
        }
        return super.keyPressed(event);
    }
    @Override public void onClose() { HudWidgets.save(); minecraft.gui.setScreen(parent); }
    @Override public void removed() { HudWidgets.save(); }
    @Override public boolean isPauseScreen() { return false; }
}
