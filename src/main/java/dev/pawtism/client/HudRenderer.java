package dev.pawtism.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import dev.pawtism.client.hud.HudWidgets;
import dev.pawtism.client.combat.CombatOptions;
import dev.pawtism.client.combat.CombatModules;

/** Pawtism's own HUD. This class reads client information and never sends gameplay packets. */
public final class HudRenderer {
    private static final EquipmentSlot[] ARMOR = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final int TEXT = 0xFFF2F2F7;
    private static final int MUTED = 0xFFB8B8C6;
    private static boolean registered;

    private HudRenderer() {}

    public static void init() {
        if (!registered) {
            HudWidgets.register("status", "Status / sprint / saturation", 4, 4, 232, 103,
                () -> PawtismConfig.HUD.getBooleanValue() || PawtismConfig.TOGGLE_SPRINT.getBooleanValue() || PawtismConfig.SATURATION.getBooleanValue(),
                HudRenderer::renderStatus);
            HudWidgets.register("armor", "Armor HUD", -76, -86, 72, 80, PawtismConfig.ARMOR_HUD::getBooleanValue,
                (g, x, y) -> { Minecraft mc = Minecraft.getInstance(); if (mc.player != null && !mc.player.isSpectator()) renderArmor(g, mc, x, y); });
            registered = true;
        }
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        renderStatus(graphics, 4, 4);
    }

    private static void renderStatus(GuiGraphicsExtractor graphics, int px, int py) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) {
            return;
        }

        List<String> lines = new ArrayList<>();
        if (PawtismConfig.HUD.getBooleanValue()) {
            lines.add("Pawtism Client");
            PlayerInfo info = client.getConnection() == null ? null
                : client.getConnection().getPlayerInfo(client.player.getUUID());
            String ping = client.isLocalServer() ? "Local" : info == null ? "? ms" : info.getLatency() + " ms";
            lines.add(client.getFps() + " FPS  |  " + ping);
            ServerData server = client.getCurrentServer();
            lines.add(server == null ? "Singleplayer" : server.ip);
        }
        if (PawtismConfig.TOGGLE_SPRINT.getBooleanValue()) {
            boolean active = client.player.isSprinting() || client.options.keySprint.isDown();
            String mode = client.options.toggleSprint().get() ? "Toggled" : "Held";
            lines.add("Sprint: " + (active ? "ON" : "OFF") + " (" + mode + ")");
        }
        if (PawtismConfig.SATURATION.getBooleanValue() && !client.player.isSpectator()
            && !client.player.getAbilities().instabuild) {
            float saturation = client.player.getFoodData().getSaturationLevel();
            if (client.getSingleplayerServer() != null) {
                ServerPlayer serverPlayer = client.getSingleplayerServer().getPlayerList()
                    .getPlayer(client.player.getUUID());
                if (serverPlayer != null) {
                    saturation = serverPlayer.getFoodData().getSaturationLevel();
                }
            }
            // Multiplayer's last server snapshot can become stale between health/food updates.
            // Read the integrated server in singleplayer; never mutate either player's food state.
            String label = client.isLocalServer() ? "Saturation: " : "Saturation estimate: ";
            lines.add(label + decimal(saturation));
            FoodProperties food = heldFood(client.player.getMainHandItem());
            if (food == null) {
                food = heldFood(client.player.getOffhandItem());
            }
            if (food != null) {
                lines.add("Held food: +" + food.nutrition() + " hunger / +" + decimal(food.saturation()) + " sat.");
            }
        }

        if (!lines.isEmpty()) {
            int width = 0;
            for (String line : lines) {
                width = Math.max(width, client.font.width(line));
            }
            int right = Math.min(graphics.guiWidth() - 4, px + Math.min(232, width + 12));
            int bottom = py + 8 + lines.size() * 12;
            graphics.fill(px, py, right, bottom, 0xA014141D);
            graphics.fill(px, py, px + 2, bottom, PawtismConfig.ACCENT.getIntegerValue());
            graphics.enableScissor(px + 4, py, right - 3, bottom);
            int y = py + 6;
            for (int i = 0; i < lines.size(); i++) {
                int color = i == 0 && PawtismConfig.HUD.getBooleanValue()
                    ? PawtismConfig.ACCENT.getIntegerValue() : TEXT;
                graphics.text(client.font, lines.get(i), px + 6, y, color, true);
                y += 12;
            }
            graphics.disableScissor();
        }

    }

    private static void renderArmor(GuiGraphicsExtractor graphics, Minecraft client, int x, int y) {
        for (EquipmentSlot slot : ARMOR) {
            ItemStack stack = client.player.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            graphics.fill(x, y, x + 72, y + 19, 0xA014141D);
            graphics.item(stack, x + 2, y + 1);
            graphics.itemDecorations(client.font, stack, x + 2, y + 1);
            if (stack.isDamageableItem()) {
                int remaining = Math.max(0, stack.getMaxDamage() - stack.getDamageValue());
                int percent = remaining * 100 / Math.max(1, stack.getMaxDamage());
                int color = percent < 25 ? 0xFFFF7575 : percent < 50 ? 0xFFFFCF75 : TEXT;
                graphics.text(client.font, percent + "%", x + 23, y + 5, color, true);
            } else {
                graphics.text(client.font, "--", x + 23, y + 5, MUTED, true);
            }
            y += 20;
        }
    }

    private static FoodProperties heldFood(ItemStack stack) {
        return stack.get(DataComponents.FOOD);
    }

    private static String decimal(float value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    public static boolean isZoomActive() {
        Minecraft client = Minecraft.getInstance();
        return PawtismConfig.ZOOM.getBooleanValue() && PawtismClient.zoomHeld
            && client.player != null && client.gui.screen() == null;
    }

    public static void renderCrosshair(GuiGraphicsExtractor graphics) {
        int x = graphics.guiWidth() / 2;
        int y = graphics.guiHeight() / 2;
        int size = Math.max(2, Math.min(24, PawtismConfig.CROSSHAIR_SIZE.getIntegerValue()));
        int color = CombatModules.crosshairColor(PawtismConfig.ACCENT.getIntegerValue());
        int gap = CombatOptions.CROSSHAIR_GAP.getIntegerValue();
        // A one-pixel outline keeps the custom color legible against snow and other bright surfaces.
        for (int sign : new int[]{-1, 1}) {
            int near = sign < 0 ? x - gap - size : x + gap + 1;
            int far = near + size;
            graphics.fill(near - 1, y - 1, far + 1, y + 2, 0xBF000000);
            graphics.fill(near, y, far, y + 1, color);
            near = sign < 0 ? y - gap - size : y + gap + 1;
            far = near + size;
            graphics.fill(x - 1, near - 1, x + 2, far + 1, 0xBF000000);
            graphics.fill(x, near, x + 1, far, color);
        }
        if (CombatOptions.CROSSHAIR_DOT.getBooleanValue()) {
            graphics.fill(x - 1, y - 1, x + 2, y + 2, 0xBF000000);
            graphics.fill(x, y, x + 1, y + 1, color);
        }
    }
}
