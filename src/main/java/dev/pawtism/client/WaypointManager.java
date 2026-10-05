package dev.pawtism.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

public final class WaypointManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("Pawtism/Waypoints");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<Waypoint> POINTS = new ArrayList<>();
    private static final int MAX_WAYPOINTS = 128;
    private static String worldKey;
    private static Path worldFile;
    private static String error = "";
    private static boolean initialized;

    private WaypointManager() {}

    public static void init() {
        if (initialized) return;
        initialized = true;
        ClientTickEvents.END_CLIENT_TICK.register(WaypointManager::updateWorld);
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("pawtism", "waypoints"), WaypointManager::render);
    }

    public static void openScreen() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        updateWorld(mc);
        mc.gui.setScreen(new WaypointScreen(mc.gui.screen()));
    }

    private static void updateWorld(Minecraft mc) {
        String next = currentWorldKey(mc);
        if (java.util.Objects.equals(next, worldKey)) return;
        worldKey = next;
        worldFile = null;
        POINTS.clear();
        error = "";
        if (next == null) return;
        try {
            String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(next.getBytes(StandardCharsets.UTF_8)));
            worldFile = FabricLoader.getInstance().getConfigDir().resolve("pawtism/waypoints").resolve(digest + ".json");
            load();
        } catch (IOException | NoSuchAlgorithmException | RuntimeException e) {
            LOGGER.warn("Could not load Pawtism waypoints; existing file is preserved", e);
            error = "Could not load waypoint file. Existing data is preserved.";
            // Never overwrite an unreadable file with an empty list.
            worldFile = null;
        }
    }

    private static String currentWorldKey(Minecraft mc) {
        if (mc.level == null || mc.player == null) return null;
        if (mc.getSingleplayerServer() != null)
            return "local:" + mc.getSingleplayerServer().getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
        if (mc.getCurrentServer() != null)
            return "server:" + mc.getCurrentServer().ip.toLowerCase(Locale.ROOT);
        if (mc.getConnection() != null)
            return "connection:" + mc.getConnection().getConnection().getRemoteAddress();
        return null;
    }

    private static void load() throws IOException {
        if (!Files.exists(worldFile)) return;
        if (Files.size(worldFile) > 262_144) throw new IOException("Waypoint file too large");
        POINTS.addAll(decodeSaved(Files.readString(worldFile, StandardCharsets.UTF_8)));
    }

    /** Validate the complete on-disk schema before accepting any waypoint. */
    static List<Waypoint> decodeSaved(String json) throws IOException {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonElement version = root.get("formatVersion");
            if (version == null || !version.isJsonPrimitive() || !version.getAsJsonPrimitive().isNumber()
                    || version.getAsBigDecimal().intValueExact() != 1 || !root.has("waypoints")
                    || !root.get("waypoints").isJsonArray()) throw new IOException("Invalid waypoint document");
            var entries = root.getAsJsonArray("waypoints");
            if (entries.size() > MAX_WAYPOINTS) throw new IOException("Too many waypoints");
            List<Waypoint> result = new ArrayList<>();
            java.util.Set<String> ids = new java.util.HashSet<>();
            for (JsonElement entry : entries) {
                if (!entry.isJsonObject()) throw new IOException("Invalid waypoint entry");
                JsonObject object = entry.getAsJsonObject();
                for (String key : List.of("id", "name", "dimension")) {
                    JsonElement field = object.get(key);
                    if (field == null || !field.isJsonPrimitive() || !field.getAsJsonPrimitive().isString())
                        throw new IOException("Missing waypoint " + key);
                }
                for (String key : List.of("x", "y", "z", "color")) {
                    JsonElement field = object.get(key);
                    if (field == null || !field.isJsonPrimitive() || !field.getAsJsonPrimitive().isNumber())
                        throw new IOException("Missing waypoint " + key);
                }
                JsonElement visible = object.get("visible");
                if (visible == null || !visible.isJsonPrimitive() || !visible.getAsJsonPrimitive().isBoolean())
                    throw new IOException("Missing waypoint visibility");
                // Reject fractional and overflowing colors instead of silently truncating them.
                object.get("color").getAsBigDecimal().intValueExact();
                Waypoint waypoint = GSON.fromJson(object, Waypoint.class);
                if (!ids.add(waypoint.id())) throw new IOException("Duplicate waypoint id");
                result.add(waypoint);
            }
            return List.copyOf(result);
        } catch (RuntimeException invalid) {
            throw new IOException("Invalid waypoint data", invalid);
        }
    }

    private static void save() {
        if (worldFile == null) {
            error = "Waypoint file unavailable. Changes are kept for this session only.";
            return;
        }
        Path tmp = null;
        try {
            Files.createDirectories(worldFile.getParent());
            tmp = Files.createTempFile(worldFile.getParent(), "waypoints-", ".tmp");
            Files.writeString(tmp, GSON.toJson(new SavedWaypoints(1, List.copyOf(POINTS))), StandardCharsets.UTF_8);
            try {
                Files.move(tmp, worldFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(tmp, worldFile, StandardCopyOption.REPLACE_EXISTING);
            }
            error = "";
        } catch (IOException e) {
            LOGGER.warn("Could not save Pawtism waypoints", e);
            error = "Could not save waypoints. Changes are kept for this session.";
        } finally {
            if (tmp != null) try { Files.deleteIfExists(tmp); } catch (IOException ignored) {}
        }
    }

    public static List<Waypoint> points() { return List.copyOf(POINTS); }
    public static String error() { return error; }
    public static String dimension() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level == null ? "minecraft:overworld" : mc.level.dimension().identifier().toString();
    }

    public static void add(Waypoint point) {
        if (POINTS.size() >= MAX_WAYPOINTS) throw new IllegalArgumentException("Maximum of 128 waypoints reached");
        POINTS.add(point);
        save();
    }

    public static void replace(Waypoint point) {
        for (int i = 0; i < POINTS.size(); i++) {
            if (POINTS.get(i).id().equals(point.id())) { POINTS.set(i, point); save(); return; }
        }
    }

    public static void remove(String id) {
        POINTS.removeIf(point -> point.id().equals(id));
        save();
    }

    /** Compass-style labels only: every location was explicitly entered by the player. */
    public static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (!PawtismConfig.WAYPOINTS.getBooleanValue() || mc.level == null || mc.player == null
                || mc.gui.hud.isHidden() || mc.gui.screen() != null) return;
        List<Waypoint> visible = POINTS.stream().filter(w -> w.visible() && w.dimension().equals(dimension()))
                .sorted(Comparator.comparingDouble(w -> w.distanceTo(mc.player.getX(), mc.player.getY(), mc.player.getZ())))
                .limit(6).toList();
        int row = 0;
        for (Waypoint point : visible) {
            double dx = point.x() - mc.player.getX();
            double dz = point.z() - mc.player.getZ();
            double bearing = Math.toDegrees(Math.atan2(-dx, dz));
            double relative = wrapDegrees(bearing - mc.player.getYRot());
            String arrow = relative < -65 ? "< " : relative > 65 ? "> " : "^ ";
            String label = mc.font.plainSubstrByWidth(arrow + point.name() + "  " + formatDistance(point.distanceTo(mc.player.getX(), mc.player.getY(), mc.player.getZ())), graphics.guiWidth() - 16);
            int textWidth = mc.font.width(label);
            int center = graphics.guiWidth() / 2;
            int targetX = center + (int) (Math.max(-1, Math.min(1, relative / 65)) * (center - 12));
            int x = Math.max(4, Math.min(graphics.guiWidth() - textWidth - 4, targetX - textWidth / 2));
            int y = 34 + row++ * 13;
            graphics.fill(x - 3, y - 2, x + textWidth + 3, y + 10, 0xa0161320);
            graphics.text(mc.font, label, x, y, point.color(), true);
        }
    }

    static double wrapDegrees(double angle) {
        double value = angle % 360;
        if (value >= 180) value -= 360;
        if (value < -180) value += 360;
        return value;
    }

    static String formatDistance(double distance) {
        return distance >= 1000 ? String.format(Locale.ROOT, "%.1f km", distance / 1000)
                : String.format(Locale.ROOT, "%.0f m", distance);
    }

    private record SavedWaypoints(int formatVersion, List<Waypoint> waypoints) {}
}
