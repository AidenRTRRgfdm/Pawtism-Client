package dev.pawtism.client.combat;

import dev.pawtism.client.hud.HudWidgets;
import dev.pawtism.client.hud.HudColors;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Original, local informational/cosmetic PvP modules. No outgoing packets or input automation. */
public final class CombatModules {
    private static final SlidingClickCounter LEFT = new SlidingClickCounter(), RIGHT = new SlidingClickCounter();
    private static final ComboTracker COMBAT = new ComboTracker();
    private static Object level;
    private static boolean leftDown, rightDown, initialized;

    private CombatModules() {}

    public static void init() {
        if (initialized) return;
        initialized = true;
        CombatOptions.TARGET_TINT.setPrettyName("Crosshair target tint");
        CombatOptions.TARGET_TINT.setTranslatedName("Crosshair target tint");
        HudWidgets.register("cps", "Left / right CPS", -118, 84, 114, 30,
                CombatOptions.CPS::getBooleanValue, (g, x, y) -> {
                    long now = System.nanoTime();
                    HudWidgets.panel(g, x, y, 114, List.of("Mouse CPS", "L " + LEFT.count(now) + "  |  R " + RIGHT.count(now)));
                });
        HudWidgets.register("keystrokes", "Keystrokes", 4, 164, 90, 90,
                CombatOptions.KEYSTROKES::getBooleanValue, CombatModules::keys);
        HudWidgets.register("combat", "Confirmed combo / hit distance", -146, 118, 142, 30,
                () -> CombatOptions.COMBO.getBooleanValue() || CombatOptions.HIT_DISTANCE.getBooleanValue(), CombatModules::combatHud);
        HudWidgets.register("cooldown", "Attack cooldown", -218, -34, 138, 29,
                CombatOptions.COOLDOWN::getBooleanValue, CombatModules::cooldownHud);
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (level != mc.level) {
                level = mc.level; LEFT.clear(); RIGHT.clear(); COMBAT.clear(); leftDown = rightDown = false;
            }
            if (mc.gui.screen() != null || !mc.mouseHandler.isMouseGrabbed()) leftDown = rightDown = false;
        });
    }

    public static void mouseButton(long window, int button, int action) {
        Minecraft mc = Minecraft.getInstance();
        if (window != mc.getWindow().handle() || (button != 0 && button != 1)) return;
        boolean pressed = action == 1;
        if (button == 0) leftDown = pressed; else rightDown = pressed;
        if (!pressed || mc.player == null || mc.gui.screen() != null || !mc.mouseHandler.isMouseGrabbed()) return;
        if (button == 0) LEFT.click(System.nanoTime()); else RIGHT.click(System.nanoTime());
    }

    public static void attemptedAttack(Player player, Entity target) {
        Minecraft mc = Minecraft.getInstance();
        if (player != mc.player || !(target instanceof LivingEntity) || !target.isAlive()) return;
        Vec3 eye = player.getEyePosition();
        Vec3 position;
        if (mc.hitResult instanceof EntityHitResult hit && hit.getEntity() == target) {
            position = hit.getLocation();
        } else {
            var box = target.getBoundingBox();
            position = new Vec3(Math.clamp(eye.x, box.minX, box.maxX),
                    Math.clamp(eye.y, box.minY, box.maxY), Math.clamp(eye.z, box.minZ, box.maxZ));
        }
        COMBAT.attempted(target.getId(), eye.distanceTo(position), System.nanoTime());
    }

    public static void damageEvent(ClientboundDamageEventPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        int playerId = mc.player.getId();
        if (packet.entityId() == playerId) {
            COMBAT.receivedDamage();
        } else if (packet.sourceCauseId() == playerId && packet.sourceDirectId() == playerId) {
            // Direct source matching excludes arrows, pets, and another player's hit.
            COMBAT.confirmed(packet.entityId(), System.nanoTime());
        }
    }

    public static int crosshairColor(int normal) {
        Minecraft mc = Minecraft.getInstance();
        if (CombatOptions.TARGET_COLOR.getBooleanValue() && mc.player != null
                && mc.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity target
                && target.isAlive() && !target.isInvisibleTo(mc.player))
            return CombatOptions.TARGET_TINT.getIntegerValue();
        return normal;
    }

    public static int leftCps() { return LEFT.count(System.nanoTime()); }
    public static int rightCps() { return RIGHT.count(System.nanoTime()); }
    public static int combo() { return COMBAT.combo(System.nanoTime()); }
    public static double lastHitDistance() { return COMBAT.lastHitDistance(); }

    private static void combatHud(GuiGraphicsExtractor g, int x, int y) {
        List<String> lines = new ArrayList<>();
        if (CombatOptions.COMBO.getBooleanValue()) lines.add("Confirmed combo: " + COMBAT.combo(System.nanoTime()));
        if (CombatOptions.HIT_DISTANCE.getBooleanValue()) {
            double distance = COMBAT.lastHitDistance();
            lines.add(Double.isNaN(distance) ? "Hit dist: --" : String.format(Locale.ROOT, "Hit dist: ~%.2f m", distance));
        }
        HudWidgets.panel(g, x, y, 142, lines);
    }

    private static void cooldownHud(GuiGraphicsExtractor g, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        float strength = Math.clamp(mc.player.getAttackStrengthScale(0.5f), 0, 1);
        HudWidgets.panel(g, x, y, 138, List.of("Attack charge: " + Math.round(strength * 100) + "%"));
        g.fill(x + 5, y + 22, x + 133, y + 26, HudColors.surface(0x993d3547));
        g.fill(x + 5, y + 22, x + 5 + Math.round(strength * 128), y + 26,
                strength >= 0.99f ? HudColors.accent() : HudColors.secondary());
    }

    private static void keys(GuiGraphicsExtractor g, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        key(g, x + 31, y, 24, "W", mc.options.keyUp.isDown());
        key(g, x + 5, y + 23, 24, "A", mc.options.keyLeft.isDown());
        key(g, x + 31, y + 23, 24, "S", mc.options.keyDown.isDown());
        key(g, x + 57, y + 23, 24, "D", mc.options.keyRight.isDown());
        key(g, x + 5, y + 46, 37, "LMB", leftDown);
        key(g, x + 44, y + 46, 37, "RMB", rightDown);
        key(g, x + 5, y + 69, 76, "SPACE", mc.options.keyJump.isDown());
    }

    private static void key(GuiGraphicsExtractor g, int x, int y, int width, String label, boolean pressed) {
        Minecraft mc = Minecraft.getInstance();
        g.fill(x, y, x + width, y + 20, pressed ? HudColors.accent() : HudColors.background());
        g.text(mc.font, label, x + (width - mc.font.width(label)) / 2, y + 6,
                pressed ? HudColors.pressedText() : HudColors.text(), true);
    }
}
