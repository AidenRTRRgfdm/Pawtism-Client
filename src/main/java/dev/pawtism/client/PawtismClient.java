package dev.pawtism.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class PawtismClient implements ClientModInitializer {
    public static boolean zoomHeld;
    public static final String VERSION = "1.2.0";
    private static Boolean originalSprintMode;
    @Override public void onInitializeClient() {
        PawtismConfig.init();
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("pawtism", "client"));
        KeyMapping settings = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.pawtism.settings", GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        KeyMapping zoom = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.pawtism.zoom", GLFW.GLFW_KEY_Z, category));
        KeyMapping waypoints = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.pawtism.waypoints", GLFW.GLFW_KEY_N, category));
        KeyMapping toggleWaypoints = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.pawtism.toggle_waypoints", GLFW.GLFW_KEY_B, category));
        HudRenderer.init();
        WaypointManager.init();
        ShulkerTooltip.init();
        dev.pawtism.client.hud.ExtraHud.init();
        dev.pawtism.client.combat.CombatModules.init();
        dev.pawtism.client.qol.QolModules.init();
        dev.pawtism.client.hud.HudWidgets.init();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            zoomHeld = client.player != null && client.gui.screen() == null && zoom.isDown();
            while (settings.consumeClick()) {
                if (client.gui.screen() == null) client.gui.setScreen(new ModuleScreen(null));
            }
            while (waypoints.consumeClick()) {
                if (client.player != null && client.gui.screen() == null) WaypointManager.openScreen();
            }
            while (toggleWaypoints.consumeClick()) {
                if (client.player != null && client.gui.screen() == null) {
                    PawtismConfig.WAYPOINTS.setBooleanValue(!PawtismConfig.WAYPOINTS.getBooleanValue());
                    PawtismConfig.INSTANCE.save();
                }
            }
            if (originalSprintMode == null) originalSprintMode = client.options.toggleSprint().get();
            client.options.toggleSprint().set(PawtismConfig.TOGGLE_SPRINT.getBooleanValue());
            PerformancePreset.update(client);
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            PerformancePreset.restoreOnExit(client);
            if (originalSprintMode != null) {
                client.options.toggleSprint().set(originalSprintMode);
                client.options.save();
            }
            PawtismConfig.INSTANCE.save();
        });
        PawtismConfig.LOGGER.info("Pawtism Client {} initialized for Minecraft 26.2", VERSION);
    }
}
