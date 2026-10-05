package dev.pawtism.client.hud;

import dev.pawtism.client.PawtismConfig;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import net.minecraft.server.packs.repository.PackSource;

/** Original Lunar/Feather-style information widgets, backed by native Minecraft state. */
public final class ExtraHud {
    private static final int TEXT = 0xFFF2F2F7;
    private static final int BACKGROUND = 0xA014141D;
    private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final double[] FRAME_TIMES = new double[90];
    private static int frameCursor;
    private static int frameCount;
    private static boolean initialized;
    private static ClientPacketListener sessionConnection;
    private static ClientLevel previousLevel;
    private static long sessionStarted;
    private static double previousX;
    private static double previousZ;
    private static double horizontalSpeed;
    private static boolean havePosition;
    private static final Map<String, List<String>> RENDERED_LINES = new HashMap<>();
    private static int renderedInventoryItems;

    private ExtraHud() {}

    /** Last native-rendered labels, for diagnostics without recreating the display logic. */
    public static List<String> renderedLines(String id) { return RENDERED_LINES.getOrDefault(id, List.of()); }
    public static int renderedInventoryItems() { return renderedInventoryItems; }
    public static int frameSampleCount() { return frameCount; }

    private static void panel(String id, GuiGraphicsExtractor g, int x, int y, int width, List<String> lines) {
        RENDERED_LINES.put(id, List.copyOf(lines));
        HudWidgets.panel(g, x, y, width, lines);
    }

    public static void init() {
        if (initialized) return;
        initialized = true;
        ClientTickEvents.END_CLIENT_TICK.register(ExtraHud::tick);
        HudWidgets.register("coordinates", "Coordinates", 4, 112, 148, 46,
            HudOptions.COORDINATES::getBooleanValue, ExtraHud::coordinates);
        HudWidgets.register("compass", "Direction / compass", 4, 164, 148, 34,
            HudOptions.COMPASS::getBooleanValue, ExtraHud::compass);
        HudWidgets.register("biome", "Biome", 4, 204, 160, 34,
            HudOptions.BIOME::getBooleanValue, ExtraHud::biome);
        HudWidgets.register("speed", "Movement speed", 4, 244, 148, 34,
            HudOptions.SPEED::getBooleanValue, ExtraHud::speed);
        HudWidgets.register("potions", "Potion timers", -194, 8, 190, 70,
            HudOptions.POTIONS::getBooleanValue, ExtraHud::potions);
        HudWidgets.register("held_item", "Held item / durability", -194, -152, 190, 58,
            HudOptions.HELD_ITEM::getBooleanValue, ExtraHud::heldItem);
        HudWidgets.register("inventory", "Inventory preview", 4, -124, 170, 78,
            HudOptions.INVENTORY::getBooleanValue, ExtraHud::inventory);
        HudWidgets.register("clock", "Local clock", -154, 84, 150, 34,
            HudOptions.CLOCK::getBooleanValue, ExtraHud::clock);
        HudWidgets.register("session", "Session timer", -154, 124, 150, 34,
            HudOptions.SESSION::getBooleanValue, ExtraHud::session);
        HudWidgets.register("memory", "Memory usage", -174, 164, 170, 34,
            HudOptions.MEMORY::getBooleanValue, ExtraHud::memory);
        HudWidgets.register("players", "Online players", -154, 204, 150, 34,
            HudOptions.PLAYERS::getBooleanValue, ExtraHud::players);
        HudWidgets.register("light", "Light level", -164, 244, 160, 34,
            HudOptions.LIGHT::getBooleanValue, ExtraHud::light);
        HudWidgets.register("game_day", "Game day", 4, -168, 148, 34,
            HudOptions.DAY::getBooleanValue, ExtraHud::day);
        HudWidgets.register("packs", "Resource packs", 180, -124, 190, 70,
            HudOptions.PACKS::getBooleanValue, ExtraHud::packs);
        HudWidgets.register("item_counter", "Arrow / totem counter", 180, -164, 148, 34,
            HudOptions.ITEM_COUNTER::getBooleanValue, ExtraHud::itemCounter);
        HudWidgets.register("frame_graph", "FPS / frame graph", 180, 112, 148, 58,
            HudOptions.FPS_GRAPH::getBooleanValue, ExtraHud::frameGraph);
    }

    private static void tick(Minecraft client) {
        if (client.getConnection() != sessionConnection) {
            sessionConnection = client.getConnection();
            sessionStarted = sessionConnection == null ? 0 : System.nanoTime();
            frameCursor = frameCount = 0;
            RENDERED_LINES.clear();
            renderedInventoryItems = 0;
        }
        if (client.player == null || client.level == null) {
            havePosition = false;
            previousLevel = null;
            horizontalSpeed = 0;
            return;
        }
        double x = client.player.getX();
        double z = client.player.getZ();
        if (!client.isPaused() && havePosition && previousLevel == client.level) {
            double dx = x - previousX;
            double dz = z - previousZ;
            // A jump of more than 16 blocks in one client tick is a teleport, not travel speed.
            horizontalSpeed = dx * dx + dz * dz > 256 ? 0 : HudMath.horizontalSpeed(dx, dz, 0.05);
        } else {
            horizontalSpeed = 0;
        }
        previousX = x;
        previousZ = z;
        previousLevel = client.level;
        havePosition = true;
    }

    private static void coordinates(GuiGraphicsExtractor g, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        String position = "X " + fixed(mc.player.getX(), 1) + "  Y " + fixed(mc.player.getY(), 1);
        String dimension = HudMath.readableId(mc.level.dimension().identifier().getPath());
        panel("coordinates", g, x, y, 148, List.of("Coordinates", position,
            "Z " + fixed(mc.player.getZ(), 1) + " · " + dimension));
    }

    private static void compass(GuiGraphicsExtractor g, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        float yaw = mc.player.getYRot();
        panel("compass", g, x, y, 148, List.of("Direction", HudMath.direction(yaw) + " · " + HudMath.compassHeading(yaw) + "°"));
    }

    private static void biome(GuiGraphicsExtractor g, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        String name = mc.level.getBiome(mc.player.blockPosition()).unwrapKey()
            .map(key -> HudMath.readableId(key.identifier().getPath())).orElse("Unknown");
        panel("biome", g, x, y, 160, List.of("Biome", name));
    }

    private static void speed(GuiGraphicsExtractor g, int x, int y) {
        panel("speed", g, x, y, 148, List.of("Horizontal speed", fixed(horizontalSpeed, 2) + " m/s"));
    }

    private static void potions(GuiGraphicsExtractor g, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        List<MobEffectInstance> effects = mc.player.getActiveEffects().stream()
            .sorted(Comparator.comparing(effect -> effect.getEffect().value().getDisplayName().getString())).toList();
        List<String> lines = new ArrayList<>();
        lines.add("Effects" + (effects.size() > 4 ? " (" + effects.size() + ")" : ""));
        if (effects.isEmpty()) lines.add("None active");
        for (MobEffectInstance effect : effects.stream().limit(4).toList()) {
            String name = effect.getEffect().value().getDisplayName().getString();
            String strength = effect.getAmplifier() == 0 ? "" : " " + (effect.getAmplifier() + 1);
            String timer = effect.isInfiniteDuration() ? "∞" : HudMath.elapsed(Math.max(0, effect.getDuration()) / 20);
            lines.add(name + strength + " · " + timer);
        }
        panel("potions", g, x, y, 190, lines);
    }

    private static void heldItem(GuiGraphicsExtractor g, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        ItemStack stack = mc.player.getMainHandItem();
        List<String> lines = new ArrayList<>();
        lines.add("Held item");
        lines.add(stack.isEmpty() ? "Empty hand" : stack.getHoverName().getString());
        if (stack.isDamageableItem()) {
            int remaining = Math.max(0, stack.getMaxDamage() - stack.getDamageValue());
            lines.add("Durability " + remaining + " / " + stack.getMaxDamage());
        } else if (!stack.isEmpty()) {
            lines.add("Stack: " + stack.getCount());
        }
        ItemStack offhand = mc.player.getOffhandItem();
        if (!offhand.isEmpty()) lines.add("Offhand: " + offhand.getHoverName().getString());
        panel("held_item", g, x, y, 190, lines);
    }

    private static void inventory(GuiGraphicsExtractor g, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        g.fill(x, y, x + 170, y + 78, BACKGROUND);
        g.fill(x, y, x + 2, y + 78, PawtismConfig.ACCENT.getIntegerValue());
        g.text(mc.font, "Inventory", x + 6, y + 4, PawtismConfig.ACCENT.getIntegerValue(), true);
        renderedInventoryItems = 0;
        for (int slot = 0; slot < 27; slot++) {
            int sx = x + 4 + (slot % 9) * 18;
            int sy = y + 18 + (slot / 9) * 18;
            g.fill(sx, sy, sx + 17, sy + 17, 0x503A3345);
            ItemStack item = mc.player.getInventory().getItem(slot + 9);
            if (!item.isEmpty()) {
                renderedInventoryItems++;
                g.item(item, sx, sy);
                g.itemDecorations(mc.font, item, sx, sy);
            }
        }
    }

    private static void clock(GuiGraphicsExtractor g, int x, int y) {
        panel("clock", g, x, y, 150, List.of("Local time", LocalTime.now().format(CLOCK_FORMAT)));
    }

    private static void session(GuiGraphicsExtractor g, int x, int y) {
        long seconds = sessionStarted == 0 ? 0 : Math.max(0, System.nanoTime() - sessionStarted) / 1_000_000_000L;
        panel("session", g, x, y, 150, List.of("Connection time", HudMath.elapsed(seconds)));
    }

    private static void memory(GuiGraphicsExtractor g, int x, int y) {
        Runtime runtime = Runtime.getRuntime();
        long megabyte = 1024 * 1024;
        long used = (runtime.totalMemory() - runtime.freeMemory()) / megabyte;
        long max = runtime.maxMemory() / megabyte;
        panel("memory", g, x, y, 170, List.of("Minecraft memory", used + " / " + max + " MiB"));
    }

    private static void players(GuiGraphicsExtractor g, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        int count = mc.getConnection() == null ? 0 : mc.getConnection().getListedOnlinePlayers().size();
        panel("players", g, x, y, 150, List.of("Player list", count + " online"));
    }

    private static void light(GuiGraphicsExtractor g, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        var pos = mc.player.blockPosition();
        int block = mc.level.getBrightness(LightLayer.BLOCK, pos);
        int sky = mc.level.getBrightness(LightLayer.SKY, pos);
        panel("light", g, x, y, 160, List.of("Light level", "Block " + block + "  |  Sky " + sky));
    }

    private static void day(GuiGraphicsExtractor g, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        long ticks = mc.level.getOverworldClockTime();
        long day = Math.floorDiv(ticks, 24000) + 1;
        long minuteOfDay = Math.floorMod(ticks + 6000, 24000) * 1440 / 24000;
        String clock = String.format(java.util.Locale.ROOT, "%02d:%02d", minuteOfDay / 60, minuteOfDay % 60);
        panel("game_day", g, x, y, 148, List.of("World day " + day, clock + " · overworld"));
    }

    private static void packs(GuiGraphicsExtractor g, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        List<String> names = mc.getResourcePackRepository().getSelectedPacks().stream()
            .filter(pack -> !pack.getId().equals("vanilla") && (!pack.isRequired() || pack.getPackSource() == PackSource.SERVER))
            .map(pack -> pack.getTitle().getString()).toList();
        List<String> lines = new ArrayList<>();
        lines.add("Active resource packs");
        if (names.isEmpty()) lines.add("Default resources");
        else {
            lines.addAll(names.stream().limit(3).toList());
            if (names.size() > 3) lines.add("+ " + (names.size() - 3) + " more");
        }
        panel("packs", g, x, y, 190, lines);
    }

    private static void itemCounter(GuiGraphicsExtractor g, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        int arrows = 0;
        int totems = 0;
        for (int slot = 0; slot < mc.player.getInventory().getContainerSize(); slot++) {
            ItemStack item = mc.player.getInventory().getItem(slot);
            if (item.is(Items.ARROW) || item.is(Items.TIPPED_ARROW) || item.is(Items.SPECTRAL_ARROW)) arrows += item.getCount();
            if (item.is(Items.TOTEM_OF_UNDYING)) totems += item.getCount();
        }
        panel("item_counter", g, x, y, 148, List.of("Supplies", "Arrows " + arrows + "  |  Totems " + totems));
    }

    private static void frameGraph(GuiGraphicsExtractor g, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        long nanos = mc.getFrameTimeNs();
        double milliseconds = nanos <= 0 ? 0 : Math.min(250, nanos / 1_000_000.0);
        FRAME_TIMES[frameCursor] = milliseconds;
        frameCursor = (frameCursor + 1) % FRAME_TIMES.length;
        frameCount = Math.min(FRAME_TIMES.length, frameCount + 1);
        g.fill(x, y, x + 148, y + 58, BACKGROUND);
        g.fill(x, y, x + 2, y + 58, PawtismConfig.ACCENT.getIntegerValue());
        String label = mc.getFps() + " FPS · " + fixed(milliseconds, 1) + " ms";
        RENDERED_LINES.put("frame_graph", List.of(label));
        g.text(mc.font, label, x + 6, y + 4, TEXT, true);
        int left = x + 6;
        int bottom = y + 52;
        g.horizontalLine(left, x + 140, bottom, 0xFF65596E);
        for (int i = 0; i < frameCount; i++) {
            int index = (frameCursor - frameCount + i + FRAME_TIMES.length) % FRAME_TIMES.length;
            double duration = FRAME_TIMES[index];
            int height = (int) Math.max(1, Math.min(30, duration / 33.333 * 30));
            int px = left + i * 134 / FRAME_TIMES.length;
            int color = duration > 33.333 ? 0xFFFF7575 : duration > 16.667 ? 0xFFFFCF75 : PawtismConfig.ACCENT.getIntegerValue();
            g.fill(px, bottom - height, px + 1, bottom, color);
        }
    }

    private static String fixed(double value, int decimalPlaces) {
        return String.format(java.util.Locale.ROOT, "% ." + decimalPlaces + "f", value).strip();
    }
}
