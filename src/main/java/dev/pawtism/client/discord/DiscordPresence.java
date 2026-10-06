package dev.pawtism.client.discord;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

/** Native lifecycle bridge. Socket I/O and JSON work never run on Minecraft's thread. */
public final class DiscordPresence {
    private static final DiscordRpcService SERVICE = new DiscordRpcService(DiscordIpcConnector.system(),
        System::nanoTime, ProcessHandle.current().pid(), System.currentTimeMillis() / 1000L);
    private static boolean initialized;
    private DiscordPresence() {}

    public static void init() {
        if (initialized) return;
        initialized = true;
        ClientTickEvents.END_CLIENT_TICK.register(DiscordPresence::update);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> SERVICE.close());
    }

    private static void update(Minecraft client) {
        PresenceData.ScreenState state = client.level == null ? PresenceData.ScreenState.MAIN_MENU
            : client.isLocalServer() ? PresenceData.ScreenState.SINGLEPLAYER : PresenceData.ScreenState.MULTIPLAYER;
        SERVICE.submit(new PresenceData(DiscordOptions.RICH_PRESENCE.getBooleanValue(),
            DiscordOptions.APPLICATION_ID.getStringValue(), state));
    }

    public static DiscordStatus status() { return SERVICE.status(); }
    public static boolean workerRunning() { return SERVICE.workerAlive(); }
}
