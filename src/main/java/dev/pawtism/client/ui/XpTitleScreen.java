package dev.pawtism.client.ui;

import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import dev.pawtism.client.ModuleScreen;
import dev.pawtism.client.PawtismConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

/** A desktop presentation of the native title menu and its original actions. */
public final class XpTitleScreen extends TitleScreen {
    private XpDesktopLayout layout;
    private Button realmsButton;

    public XpTitleScreen() { super(false); }

    @Override protected void init() {
        super.init();
        layout = XpDesktopLayout.forScreen(width, height);
        realmsButton = null;
        Button launch = null, multiplayer = null, options = null, quit = null, testWorld = null;
        List<Button> primary = new ArrayList<>();
        List<AbstractWidget> tools = new ArrayList<>();
        List<AbstractWidget> extras = new ArrayList<>();
        for (GuiEventListener child : List.copyOf(children())) {
            if (!(child instanceof AbstractWidget widget)) continue;
            if (widget instanceof PlainTextButton) {
                widget.setPosition(Math.max(4, width - widget.getWidth() - 4), layout.creditsY());
            } else if (widget instanceof SpriteIconButton) {
                tools.add(widget);
            } else if (widget instanceof Button button) {
                if (hasMessage(button, "menu.singleplayer") || hasMessage(button, "menu.playdemo")) {
                    launch = button; primary.add(button);
                } else if (hasMessage(button, "menu.multiplayer")) {
                    multiplayer = button; primary.add(button);
                } else if (hasMessage(button, "menu.online")) {
                    realmsButton = button; primary.add(button);
                } else if (hasMessage(button, "menu.resetdemo")) {
                    primary.add(button);
                } else if (hasMessage(button, "menu.options")) {
                    options = button;
                } else if (hasMessage(button, "menu.quit")) {
                    quit = button;
                } else if (button.getMessage().getString().equals("TW")) {
                    testWorld = button;
                } else {
                    extras.add(widget);
                }
            } else {
                extras.add(widget);
            }
        }
        for (int i = 0; i < primary.size(); i++) place(primary.get(i), layout.primary(i));
        if (testWorld != null && launch != null) {
            launch.setWidth(Math.max(1, layout.contentWidth() - 24));
            testWorld.setRectangle(20, 20, layout.contentX() + layout.contentWidth() - 20, layout.primaryY());
        }
        int half = (layout.contentWidth() - 4) / 2;
        if (options != null) options.setRectangle(half, 20, layout.contentX(), layout.optionsY());
        if (quit != null) quit.setRectangle(half, 20, layout.contentX() + half + 4, layout.optionsY());
        int toolsWidth = tools.size() * 24;
        for (int i = 0; i < tools.size(); i++) tools.get(i).setRectangle(20, 20, layout.contentX() + i * 24, layout.toolsY());
        int extraWidth = Math.max(20, (layout.contentWidth() - toolsWidth) / Math.max(1, extras.size()));
        for (int i = 0; i < extras.size(); i++) {
            extras.get(i).setRectangle(Math.max(16, extraWidth - 4), 20,
                layout.contentX() + toolsWidth + i * extraWidth, layout.toolsY());
        }
        addRenderableWidget(Button.builder(Component.literal("Modules"), b -> openModules())
            .bounds(layout.contentX(), layout.modulesY(), half, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Classic menu"), b -> useClassicMenu())
            .bounds(layout.contentX() + half + 4, layout.modulesY(), half, 20)
            .tooltip(Tooltip.create(Component.literal("Turn off the desktop theme"))).build());
        if (width >= 300) {
            if (launch != null) addRenderableWidget(new DesktopShortcut(layout.launch(),
                minecraft.isDemo() ? "Play demo" : "Launch Minecraft", Icon.COMPUTER, launch, null));
            if (multiplayer != null) addRenderableWidget(new DesktopShortcut(layout.multiplayer(),
                "Multiplayer", Icon.NETWORK, multiplayer, null));
            addRenderableWidget(new DesktopShortcut(layout.modules(), "Modules", Icon.FOLDER, null, this::openModules));
        }
        addRenderableWidget(new StartButton(layout.start()));
    }

    private static boolean hasMessage(Button button, String key) {
        return button.getMessage().equals(Component.translatable(key));
    }

    private static void place(AbstractWidget widget, XpDesktopLayout.Rect rect) {
        widget.setRectangle(rect.width(), rect.height(), rect.x(), rect.y());
    }

    private void openModules() { minecraft.gui.setScreen(new ModuleScreen(this)); }

    private void useClassicMenu() {
        PawtismConfig.XP_THEME.setBooleanValue(false);
        PawtismConfig.INSTANCE.save();
        minecraft.gui.setScreen(new TitleScreen());
    }

    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        XpTheme.desktop(graphics, font, width, height);
        XpDesktopLayout.Rect window = layout.window();
        XpTheme.frame(graphics, font, window.x(), window.y(), window.width(), window.height(), "Pawtism Client", false);
        graphics.text(font, "Client 1.2.0 · Minecraft 26.2", layout.contentX(), window.y() + 29, 0xff29364c, false);
        XpTheme.taskbar(graphics, font, width, height);
        // TitleScreen adds its widgets as renderable children. Keep the same instances for input and narration.
        for (GuiEventListener child : children()) {
            if (child instanceof Renderable renderable) renderable.extractRenderState(graphics, mouseX, mouseY, delta);
        }
        extractRealmsNotifications(graphics, mouseX, mouseY, delta);
    }

    private void extractRealmsNotifications(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if (realmsButton == null || !((Object) this instanceof NativeNotificationsAccess access)) return;
        RealmsNotificationsScreen notifications = access.pawtism$realmsNotifications();
        if (notifications == null) return;
        // Vanilla anchors these status icons to its original Realms button coordinates.
        int dx = realmsButton.getX() + realmsButton.getWidth() - (width / 2 + 100);
        int dy = realmsButton.getY() - (height / 4 + 96);
        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(dx, dy);
            notifications.extractRenderState(graphics, mouseX - dx, mouseY - dy, delta);
        } finally {
            graphics.pose().popMatrix();
        }
    }

    /** Implemented by the title-screen accessor; native ticking and lifecycle stay in TitleScreen. */
    public interface NativeNotificationsAccess {
        RealmsNotificationsScreen pawtism$realmsNotifications();
    }

    private enum Icon { COMPUTER, NETWORK, FOLDER }

    private final class DesktopShortcut extends Button {
        private final Icon icon;
        private final Button target;
        private final Runnable action;
        private final String label;

        private DesktopShortcut(XpDesktopLayout.Rect rect, String label, Icon icon, Button target, Runnable action) {
            super(rect.x(), rect.y(), rect.width(), rect.height(), Component.literal(label), b -> {}, DEFAULT_NARRATION);
            this.label = label; this.icon = icon; this.target = target; this.action = action;
            active = target == null || target.isActive();
            if (!active) setTooltip(Tooltip.create(Component.translatable("title.multiplayer.disabled")));
        }

        @Override public void onPress(InputWithModifiers input) {
            if (target != null) { if (target.isActive()) target.onPress(input); }
            else if (action != null) action.run();
        }

        @Override protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            if (target != null) active = target.isActive();
            if (isHoveredOrFocused() && active) {
                graphics.fill(getX(), getY(), getX() + width, getY() + height, 0x60316fc9);
                outline(graphics, getX(), getY(), width, height, 0xffd8e8ff);
            }
            drawIcon(graphics, getX() + width / 2 - 12, getY() + 2, icon, active);
            int color = active ? 0xffffffff : 0xffcad5dc;
            if (label.equals("Launch Minecraft")) {
                graphics.centeredText(font, "Launch", getX() + width / 2, getY() + 29, color);
                graphics.centeredText(font, "Minecraft", getX() + width / 2, getY() + 39, color);
            } else graphics.centeredText(font, label, getX() + width / 2, getY() + 32, color);
        }
    }

    private final class StartButton extends Button {
        private StartButton(XpDesktopLayout.Rect rect) {
            super(rect.x(), rect.y(), rect.width(), rect.height(), Component.literal("start"), b -> openModules(), DEFAULT_NARRATION);
        }
        @Override protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            if (isHoveredOrFocused()) outline(graphics, getX() + 2, getY() + 2, width - 4, height - 4, 0xffe9ffd7);
        }
    }

    private static void outline(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + 1, color); graphics.fill(x, y + h - 1, x + w, y + h, color);
        graphics.fill(x, y, x + 1, y + h, color); graphics.fill(x + w - 1, y, x + w, y + h, color);
    }

    private static void drawIcon(GuiGraphicsExtractor graphics, int x, int y, Icon icon, boolean active) {
        int dark = active ? 0xff28465a : 0xff7c8991;
        if (icon == Icon.FOLDER) {
            graphics.fill(x + 2, y + 5, x + 12, y + 9, 0xffffe183);
            graphics.fill(x + 1, y + 8, x + 24, y + 24, 0xffb98323);
            graphics.fill(x + 2, y + 9, x + 23, y + 22, 0xffffd764);
            graphics.fill(x + 3, y + 10, x + 22, y + 12, 0xffffedaa);
            graphics.fill(x + 6, y + 15, x + 9, y + 18, 0xffb37d35);
            graphics.fill(x + 11, y + 15, x + 14, y + 18, 0xffb37d35);
            graphics.fill(x + 16, y + 15, x + 19, y + 18, 0xffb37d35);
        } else if (icon == Icon.NETWORK) {
            graphics.fill(x + 5, y + 3, x + 20, y + 19, dark);
            graphics.fill(x + 3, y + 6, x + 22, y + 16, dark);
            graphics.fill(x + 5, y + 5, x + 20, y + 17, 0xff74c5ef);
            graphics.fill(x + 11, y + 4, x + 13, y + 19, 0xffd8f2ff);
            graphics.fill(x + 5, y + 10, x + 20, y + 12, 0xffd8f2ff);
            graphics.fill(x + 11, y + 19, x + 13, y + 23, dark);
            graphics.fill(x + 2, y + 23, x + 23, y + 25, dark);
            graphics.fill(x + 2, y + 21, x + 5, y + 27, 0xffdde7ed);
            graphics.fill(x + 20, y + 21, x + 23, y + 27, 0xffdde7ed);
        } else {
            graphics.fill(x, y + 2, x + 25, y + 19, dark);
            graphics.fill(x + 2, y + 4, x + 23, y + 17, 0xffdce5e9);
            graphics.fill(x + 4, y + 6, x + 21, y + 15, 0xff59a9dd);
            graphics.fill(x + 4, y + 13, x + 21, y + 15, 0xff4eae5a);
            graphics.fill(x + 11, y + 19, x + 14, y + 23, dark);
            graphics.fill(x + 5, y + 23, x + 20, y + 25, 0xffcbd6dc);
        }
    }
}
