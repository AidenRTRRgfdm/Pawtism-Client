package dev.pawtism.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigColor;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PawtismConfig implements IConfigHandler {
    public static final Logger LOGGER = LoggerFactory.getLogger("Pawtism");
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Path DIRECTORY = FabricLoader.getInstance().getConfigDir().resolve("pawtism");
    private static final Path FILE = DIRECTORY.resolve("client.json");
    public static final ConfigBoolean ZOOM = bool("zoom", "Zoom", true, "Hold Z to zoom; remap the key in Minecraft controls.");
    public static final ConfigInteger ZOOM_FACTOR = integer("zoomFactor", "Zoom magnification", 4, 2, 16, "Objects appear this many times larger while zooming.");
    public static final ConfigBoolean TOGGLE_SPRINT = bool("toggleSprint", "Toggle sprint", true, "Use Minecraft's built-in toggle sprint. No automatic walking or sprint packets.");
    public static final ConfigBoolean HUD = bool("hud", "Status HUD", true, "Pawtism, FPS, ping, server address and sprint state.");
    public static final ConfigBoolean ARMOR_HUD = bool("armorHud", "Armor HUD", true, "Equipped armor and remaining durability.");
    public static final ConfigBoolean CROSSHAIR = bool("crosshair", "Custom crosshair", true, "A small configurable crosshair replaces the vanilla crosshair.");
    public static final ConfigInteger CROSSHAIR_SIZE = integer("crosshairSize", "Crosshair size", 5, 2, 16, "Length of the crosshair arms in GUI pixels.");
    public static final ConfigColor ACCENT = new ConfigColor("accent", "#FFF5A9CA", "Accent color for the HUD and crosshair.");
    public static final ConfigBoolean WAYPOINTS = bool("waypoints", "Waypoints", true, "N opens saved locations; B shows or hides markers. Saved separately per world/server.");
    public static final ConfigBoolean SKIN_3D = bool("skin3d", "3D skin layers", true, "Extruded outer skin pixels. Falls back to vanilla skin layers while a skin is loading.");
    public static final ConfigBoolean ITEM_PHYSICS = bool("itemPhysics", "Flat dropped items", true, "Visual item rotation only. Dropped items lie flat on the ground.");
    public static final ConfigBoolean SATURATION = bool("saturation", "Saturation display", true, "Food values and saturation. Multiplayer saturation is an estimate without server synchronization.");
    public static final ConfigBoolean SHULKER_TOOLTIP = bool("shulkerTooltip", "Shulker box preview", true, "Shows an inventory grid in the shulker box tooltip.");
    public static final ConfigBoolean PERFORMANCE_MODE = bool("performanceMode", "Performance preset", false, "Optional reversible video preset: fewer particles, no clouds or entity shadows, capped view distance. Restores previous settings when disabled.");
    public static final List<IConfigBase> CORE_OPTIONS = List.of(ZOOM, ZOOM_FACTOR, TOGGLE_SPRINT, HUD, ARMOR_HUD, CROSSHAIR, CROSSHAIR_SIZE, ACCENT, WAYPOINTS, SKIN_3D, ITEM_PHYSICS, SATURATION, SHULKER_TOOLTIP, PERFORMANCE_MODE);
    public static final List<IConfigBase> OPTIONS = java.util.stream.Stream.of(CORE_OPTIONS,
        dev.pawtism.client.hud.HudOptions.OPTIONS, dev.pawtism.client.combat.CombatOptions.OPTIONS,
        dev.pawtism.client.qol.QolOptions.OPTIONS).flatMap(List::stream).toList();
    public static final PawtismConfig INSTANCE = new PawtismConfig();
    private static JsonObject extras = new JsonObject();

    private static ConfigBoolean bool(String key, String title, boolean value, String comment) {
        ConfigBoolean result = new ConfigBoolean(key, value, comment);
        result.setPrettyName(title);
        result.setTranslatedName(title);
        return result;
    }
    private static ConfigInteger integer(String key, String title, int value, int min, int max, String comment) {
        ConfigInteger result = new ConfigInteger(key, value, min, max, comment);
        result.setPrettyName(title);
        result.setTranslatedName(title);
        return result;
    }
    public static void init() {
        ACCENT.setPrettyName("Accent color");
        ACCENT.setTranslatedName("Accent color");
        ConfigManager.getInstance().registerConfigHandler("pawtism", INSTANCE);
        INSTANCE.load();
    }
    public static JsonObject extras() { return extras; }
    @Override public void load() {
        if (!Files.isRegularFile(FILE)) return;
        try {
            JsonObject root = GSON.fromJson(Files.readString(FILE), JsonObject.class);
            if (root == null) return;
            ConfigUtils.readConfigBase(root, "features", OPTIONS);
            if (root.has("state") && root.get("state").isJsonObject()) extras = root.getAsJsonObject("state");
        } catch (Exception error) {
            LOGGER.error("Cannot load Pawtism configuration; using defaults without replacing the original", error);
            try { Files.copy(FILE, DIRECTORY.resolve("client.invalid-" + System.currentTimeMillis() + ".json")); }
            catch (IOException ignored) { }
        }
    }
    @Override public void save() {
        JsonObject root = new JsonObject();
        ConfigUtils.writeConfigBase(root, "features", OPTIONS);
        root.add("state", extras);
        try { writeJson(FILE, root); }
        catch (IOException error) { LOGGER.error("Cannot save Pawtism configuration", error); }
    }
    public static void writeJson(Path file, Object value) throws IOException {
        Files.createDirectories(file.getParent());
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(temporary, GSON.toJson(value), StandardCharsets.UTF_8);
        try { Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
        catch (java.nio.file.AtomicMoveNotSupportedException unsupported) { Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING); }
    }
}
