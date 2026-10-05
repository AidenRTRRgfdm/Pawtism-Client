package dev.pawtism.client.hud;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import java.util.List;

/** Original optional widgets; every module can be moved by Pawtism's HUD editor. */
public final class HudOptions {
    public static final ConfigBoolean COORDINATES = option("coordinatesHud", "Coordinates", true,
        "Shows your position and dimension.");
    public static final ConfigBoolean COMPASS = option("compassHud", "Direction / compass", false,
        "Shows your facing direction and compass heading.");
    public static final ConfigBoolean BIOME = option("biomeHud", "Biome", false,
        "Shows the biome at your current position.");
    public static final ConfigBoolean SPEED = option("speedHud", "Movement speed", false,
        "Shows horizontal travel speed in blocks per second. Teleports are excluded.");
    public static final ConfigBoolean POTIONS = option("potionsHud", "Potion timers", false,
        "Lists active effects, their strength and remaining time.");
    public static final ConfigBoolean HELD_ITEM = option("heldItemHud", "Held item / durability", true,
        "Shows your held item's name and remaining durability.");
    public static final ConfigBoolean INVENTORY = option("inventoryHud", "Inventory preview", false,
        "Shows the 27 inventory slots above your hotbar.");
    public static final ConfigBoolean CLOCK = option("clockHud", "Local clock", false,
        "Shows your computer's local time.");
    public static final ConfigBoolean SESSION = option("sessionHud", "Session timer", false,
        "Shows time in the current connection. Resets after disconnecting.");
    public static final ConfigBoolean MEMORY = option("memoryHud", "Memory usage", false,
        "Shows the Java memory used by Minecraft, compared with its memory limit.");
    public static final ConfigBoolean PLAYERS = option("playersHud", "Online players", false,
        "Counts the player list entries sent by the server.");
    public static final ConfigBoolean LIGHT = option("lightHud", "Light level", false,
        "Shows block light and sky light at your current position.");
    public static final ConfigBoolean DAY = option("dayHud", "Game day", false,
        "Shows the overworld's day counter and time.");
    public static final ConfigBoolean PACKS = option("packsHud", "Resource packs", false,
        "Lists active resource packs.");
    public static final ConfigBoolean ITEM_COUNTER = option("itemCounterHud", "Arrow / totem counter", false,
        "Counts arrows and totems in your inventory and offhand.");
    public static final ConfigBoolean FPS_GRAPH = option("frameGraphHud", "FPS / frame graph", false,
        "Shows frame timing over the last 90 rendered frames. Tall bars indicate slower frames.");

    public static final List<IConfigBase> OPTIONS = List.of(COORDINATES, COMPASS, BIOME, SPEED, POTIONS,
        HELD_ITEM, INVENTORY, CLOCK, SESSION, MEMORY, PLAYERS, LIGHT, DAY, PACKS, ITEM_COUNTER, FPS_GRAPH);

    private HudOptions() {}

    private static ConfigBoolean option(String id, String title, boolean enabled, String description) {
        ConfigBoolean option = new ConfigBoolean(id, enabled, description);
        option.setPrettyName(title);
        option.setTranslatedName(title);
        return option;
    }
}
