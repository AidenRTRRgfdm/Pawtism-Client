package dev.pawtism.client.combat;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigColor;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import java.util.List;

public final class CombatOptions {
    public static final ConfigBoolean CPS = bool("cps", "Left / right CPS", true, "Physical mouse presses during gameplay, counted over the last one second.");
    public static final ConfigBoolean KEYSTROKES = bool("keystrokes", "Keystrokes", false, "WASD movement, mouse button and space indicators. Informational only.");
    public static final ConfigBoolean COMBO = bool("combo", "Confirmed combo counter", true, "Consecutive server-confirmed melee damage to the same target. Resets on received damage or after three seconds.");
    public static final ConfigBoolean HIT_DISTANCE = bool("hitDistance", "Confirmed hit distance", true, "Approximate client eye-to-hit distance for the last server-confirmed melee attempt. Latency can affect this value; attack range is unchanged.");
    public static final ConfigBoolean COOLDOWN = bool("attackCooldown", "Attack cooldown HUD", true, "Shows your own vanilla attack charge. Does not attack or change cooldowns.");
    public static final ConfigBoolean LOW_FIRE = bool("lowFire", "Lower fire overlay", false, "Moves the first-person fire overlay down. Fire damage and entity flames are unchanged.");
    public static final ConfigInteger FIRE_SHIFT = integer("fireShift", "Fire overlay offset", 15, 5, 75, "Amount to lower the first-person fire overlay; larger offsets move more of the flames below the screen.");
    public static final ConfigBoolean STABLE_HURT_CAMERA = bool("stableHurtCamera", "Stable hurt camera", false, "Suppresses only the camera tilt after taking damage. Hurt animation, sounds and damage remain.");
    public static final ConfigBoolean COMPACT_VIEW = bool("compactHeldItems", "Compact held items", false, "Cosmetic first-person item size; animations and interactions remain vanilla.");
    public static final ConfigInteger VIEW_SCALE = integer("heldItemScale", "Held item scale (%)", 80, 40, 100, "Scale of first-person held items when Compact held items is enabled.");
    public static final ConfigInteger CROSSHAIR_GAP = integer("crosshairGap", "Crosshair center gap", 1, 0, 12, "Empty GUI pixels between the center and each crosshair arm.");
    public static final ConfigBoolean CROSSHAIR_DOT = bool("crosshairDot", "Crosshair center dot", true, "Add a one-pixel dot to the custom crosshair.");
    public static final ConfigBoolean TARGET_COLOR = bool("crosshairTargetColor", "Crosshair target color", false, "Changes color only when the ordinary crosshair points at a visible living entity.");
    public static final ConfigColor TARGET_TINT = new ConfigColor("crosshairTargetTint", "#FFFF7575", "Custom crosshair color over a visible living entity.");
    public static final List<IConfigBase> OPTIONS = List.of(CPS, KEYSTROKES, COMBO, HIT_DISTANCE, COOLDOWN,
            LOW_FIRE, FIRE_SHIFT, STABLE_HURT_CAMERA, COMPACT_VIEW, VIEW_SCALE,
            CROSSHAIR_GAP, CROSSHAIR_DOT, TARGET_COLOR, TARGET_TINT);

    private CombatOptions() {}
    private static ConfigBoolean bool(String key, String title, boolean value, String comment) {
        ConfigBoolean option = new ConfigBoolean(key, value, comment);
        option.setPrettyName(title);
        option.setTranslatedName(title);
        return option;
    }
    private static ConfigInteger integer(String key, String title, int value, int min, int max, String comment) {
        ConfigInteger option = new ConfigInteger(key, value, min, max, comment);
        option.setPrettyName(title);
        option.setTranslatedName(title);
        return option;
    }
}
