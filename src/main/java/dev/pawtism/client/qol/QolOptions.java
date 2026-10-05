package dev.pawtism.client.qol;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import java.util.List;

/** Client-only comfort and cosmetic modules, configured through MaLiLib. */
public final class QolOptions {
    private QolOptions() {}
    public static final ConfigBoolean DEATH_INFO = bool("deathInfo", "Death location", true, "Shows your death coordinates locally in chat for recovery.");
    public static final ConfigBoolean AUTO_DEATH_WAYPOINTS = bool("autoDeathWaypoints", "Death waypoints", true, "Saves a waypoint at your own death location. Only automatic death markers are capped.");
    public static final ConfigInteger DEATH_LIMIT = number("deathWaypointLimit", "Keep death waypoints", 3, 1, 16, "Maximum saved automatic death markers per world/server.");
    public static final ConfigBoolean DROP_PROTECTION = bool("dropProtection", "Held item drop guard", true, "Press the drop key twice to drop protected items. Inventory screen actions keep their usual behavior.");
    public static final ConfigBoolean PROTECT_DURABLE = bool("protectDurable", "Guard tools and armor", true, "Protect held items with durability, including tools, weapons and armor.");
    public static final ConfigBoolean PROTECT_ENCHANTED = bool("protectEnchanted", "Guard enchanted items", true, "Protect held items that have enchantments.");
    public static final ConfigBoolean PROTECT_VALUABLE = bool("protectValuable", "Guard valuable items", true, "Protect rare/epic items, shulker boxes, diamonds, emeralds and netherite materials.");
    public static final ConfigInteger DROP_CONFIRM_SECONDS = number("dropConfirmSeconds", "Drop confirmation window", 2, 1, 5, "Seconds in which a second press confirms one drop. Changing items resets confirmation.");
    public static final ConfigBoolean RECONNECT = bool("reconnectButton", "Reconnect button", true, "Adds a manual reconnect button after disconnection. Starts one cancellable countdown and one connection attempt.");
    public static final ConfigInteger RECONNECT_DELAY = number("reconnectDelay", "Reconnect delay", 3, 0, 30, "Seconds to wait after clicking Reconnect. Cancel returns to the disconnect screen.");
    public static final ConfigBoolean CHAT_TIMESTAMPS = bool("chatTimestamps", "Chat timestamps", false, "Adds the local HH:mm time to new incoming chat lines. Message signatures and outgoing chat are preserved.");
    public static final ConfigBoolean TOGGLE_SNEAK = bool("toggleSneak", "Toggle sneak", false, "Uses Minecraft's native toggle sneak control. Disabling restores your previous crouch mode.");
    public static final ConfigBoolean LOCAL_TIME = bool("localSkyTime", "Cosmetic sky clock", false, "Pins the visible sun, moon and stars to the selected time. Terrain lighting and server time keep their real values.");
    public static final ConfigInteger SKY_TIME_TICKS = number("skyTimeTicks", "Sky time", 6000, 0, 23999, "0 sunrise, 6000 noon, 12000 sunset, 18000 midnight. Affects only the rendered overworld-style sky.");
    public static final ConfigBoolean CLEAR_WEATHER = bool("localClearWeather", "Local clear weather", false, "Hide local rain/snow and their ambient particles/sounds. Server weather and gameplay remain unchanged.");
    public static final ConfigBoolean HIDE_SCOREBOARD = bool("hideScoreboard", "Hide scoreboard", false, "Hide the sidebar scoreboard while keeping its received data intact.");
    public static final ConfigBoolean HIDE_BOSS_BARS = bool("hideBossBars", "Hide boss bars", false, "Hide boss bars; boss events continue to update normally.");
    public static final ConfigBoolean HIDE_TITLES = bool("hideTitles", "Hide screen titles", false, "Hide large title and subtitle overlays. Chat and action-bar messages remain visible.");
    public static final ConfigBoolean HIDE_TOASTS = bool("hideToasts", "Hide toast cards", false, "Hide toast graphics while their normal update/expiration continues. Toast sounds may still play.");
    public static final List<IConfigBase> OPTIONS = List.of(DEATH_INFO, AUTO_DEATH_WAYPOINTS, DEATH_LIMIT,
            DROP_PROTECTION, PROTECT_DURABLE, PROTECT_ENCHANTED, PROTECT_VALUABLE, DROP_CONFIRM_SECONDS,
            RECONNECT, RECONNECT_DELAY, CHAT_TIMESTAMPS, TOGGLE_SNEAK, LOCAL_TIME, SKY_TIME_TICKS,
            CLEAR_WEATHER, HIDE_SCOREBOARD, HIDE_BOSS_BARS, HIDE_TITLES, HIDE_TOASTS);
    private static ConfigBoolean bool(String key, String name, boolean value, String comment) {
        ConfigBoolean config = new ConfigBoolean(key, value, comment);
        config.setPrettyName(name);
        config.setTranslatedName(name);
        return config;
    }
    private static ConfigInteger number(String key, String name, int value, int minimum, int maximum, String comment) {
        ConfigInteger config = new ConfigInteger(key, value, minimum, maximum, comment);
        config.setPrettyName(name);
        config.setTranslatedName(name);
        return config;
    }
}
