package dev.pawtism.client.qol;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;

/** Exists only after an explicit button click. Leaving this screen cancels the attempt. */
public final class ManualReconnectScreen extends Screen {
    private final Screen cancelScreen;
    private final Screen connectionParent;
    private final ServerData server;
    private final long startedAt = System.nanoTime();
    private final long delayNanos;
    private boolean attempted;

    public ManualReconnectScreen(Screen cancelScreen, Screen connectionParent, ServerData server, int seconds) {
        super(Component.literal("Reconnect"));
        this.cancelScreen = cancelScreen;
        this.connectionParent = connectionParent;
        this.server = server;
        this.delayNanos = Math.max(0, Math.min(30, seconds)) * 1_000_000_000L;
    }

    @Override protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Cancel reconnect"), button -> onClose())
                .bounds(width / 2 - 100, height / 2 + 28, 200, 20).build());
    }

    @Override public void tick() {
        if (!attempted && minecraft.gui.screen() == this && System.nanoTime() - startedAt >= delayNanos) {
            attempted = true;
            ConnectScreen.startConnecting(connectionParent, minecraft, ServerAddress.parseString(server.ip), server, false, null);
        }
    }

    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        long remaining = Math.max(0, delayNanos - (System.nanoTime() - startedAt));
        int seconds = (int) ((remaining + 999_999_999L) / 1_000_000_000L);
        graphics.centeredText(font, "Reconnect in " + seconds + "s", width / 2, height / 2 - 18, 0xfff5a9ca);
        graphics.centeredText(font, server.name, width / 2, height / 2, 0xffd2c8dc);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override public void onClose() {
        attempted = true;
        minecraft.gui.setScreen(cancelScreen);
    }
    @Override public boolean isPauseScreen() { return false; }
}
