package dev.pawtism.client.qol;

import dev.pawtism.client.PawtismConfig;
import dev.pawtism.client.Waypoint;
import dev.pawtism.client.WaypointManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.ShulkerBoxBlock;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public final class QolModules {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final DoublePressGate DROP_GATE = new DoublePressGate();
    private static final String DEATH_ID_PREFIX = "pawtism-death-";
    private static ItemStack armedStack = ItemStack.EMPTY;
    private static int armedSlot = -1;
    private static boolean armedWholeStack;
    private static boolean dropKeyReleased = true;
    private static LocalPlayer trackedPlayer;
    private static boolean previouslyDead;
    private static Boolean previousSneakMode;
    private static ServerData lastServer;
    private static DeathLocation lastDeath;
    private static boolean initialized;

    private QolModules() {}

    public static void init() {
        if (initialized) return;
        initialized = true;
        ClientTickEvents.END_CLIENT_TICK.register(QolModules::tick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(QolModules::restoreSneak);
    }

    private static void tick(Minecraft mc) {
        if (QolOptions.TOGGLE_SNEAK.getBooleanValue()) {
            if (previousSneakMode == null) previousSneakMode = mc.options.toggleCrouch().get();
            mc.options.toggleCrouch().set(true);
        } else if (previousSneakMode != null) restoreSneak(mc);

        LocalPlayer player = mc.player;
        if (trackedPlayer != player) {
            trackedPlayer = player;
            previouslyDead = false;
            DROP_GATE.clear();
            armedStack = ItemStack.EMPTY;
            if (player == null) lastDeath = null;
        }
        if (player == null || mc.level == null) return;
        if (!armedStack.isEmpty() && (mc.gui.screen() != null
                || player.getInventory().getSelectedSlot() != armedSlot || !ItemStack.matches(player.getMainHandItem(), armedStack))) {
            DROP_GATE.clear();
            armedStack = ItemStack.EMPTY;
        }
        if (mc.getCurrentServer() != null) rememberServer(mc.getCurrentServer());
        boolean dead = player.isDeadOrDying();
        if (dead && !previouslyDead) onDeath(mc, player);
        previouslyDead = dead;
    }

    private static void restoreSneak(Minecraft mc) {
        if (previousSneakMode == null) return;
        mc.options.toggleCrouch().set(previousSneakMode);
        previousSneakMode = null;
        mc.options.save();
    }

    private static void onDeath(Minecraft mc, LocalPlayer player) {
        lastDeath = new DeathLocation(WaypointManager.dimension(), player.getX(), player.getY(), player.getZ(), LocalTime.now().format(TIME));
        if (QolOptions.DEATH_INFO.getBooleanValue())
            mc.gui.hud.getChat().addClientSystemMessage(Component.literal("[Pawtism] Death at " + lastDeath.coordinates()
                    + " · " + lastDeath.dimension().replace("minecraft:", "")).withStyle(ChatFormatting.LIGHT_PURPLE));
        if (!QolOptions.AUTO_DEATH_WAYPOINTS.getBooleanValue()) return;
        try {
            List<Waypoint> deaths = WaypointManager.points().stream().filter(p -> p.id().startsWith(DEATH_ID_PREFIX)).toList();
            int keep = Math.max(1, Math.min(16, QolOptions.DEATH_LIMIT.getIntegerValue()));
            for (int i = 0; i <= deaths.size() - keep; i++) WaypointManager.remove(deaths.get(i).id());
            WaypointManager.add(new Waypoint(DEATH_ID_PREFIX + UUID.randomUUID(), "Death " + lastDeath.time(), lastDeath.dimension(),
                    lastDeath.x(), lastDeath.y(), lastDeath.z(), 0xffff8399, true));
        } catch (IllegalArgumentException error) {
            PawtismConfig.LOGGER.warn("Could not add automatic death waypoint: {}", error.getMessage());
        }
    }

    public static String lastDeathText() {
        return lastDeath == null ? "Death: none" : "Death " + lastDeath.coordinates() + " · " + lastDeath.time();
    }
    public static DeathLocation lastDeath() { return lastDeath; }

    public static Component timestamp(Component content) {
        if (!QolOptions.CHAT_TIMESTAMPS.getBooleanValue()) return content;
        return Component.empty().append(Component.literal("[" + LocalTime.now().format(TIME) + "] ").withStyle(ChatFormatting.DARK_GRAY))
                .append(content);
    }

    /** Called only for the hotbar drop key; inventory UI handling is deliberately untouched. */
    public static boolean allowHeldDrop(LocalPlayer player, boolean wholeStack) {
        Minecraft mc = Minecraft.getInstance();
        ItemStack stack = player.getMainHandItem();
        if (!QolOptions.DROP_PROTECTION.getBooleanValue() || mc.gui.screen() != null || !protectedItem(stack)) {
            DROP_GATE.clear(); armedStack = ItemStack.EMPTY; return true;
        }
        int slot = player.getInventory().getSelectedSlot();
        boolean same = slot == armedSlot && wholeStack == armedWholeStack && ItemStack.matches(stack, armedStack);
        // GLFW key repeats are not a second deliberate press.
        if (same && !dropKeyReleased) return false;
        long window = Math.max(1, Math.min(5, QolOptions.DROP_CONFIRM_SECONDS.getIntegerValue())) * 1_000_000_000L;
        if (DROP_GATE.confirm(same, System.nanoTime(), window)) {
            armedStack = ItemStack.EMPTY;
            return true;
        }
        armedStack = stack.copy(); armedSlot = slot; armedWholeStack = wholeStack;
        dropKeyReleased = false;
        player.sendOverlayMessage(Component.literal("Drop protected: press again within " + QolOptions.DROP_CONFIRM_SECONDS.getIntegerValue()
                + "s to drop " + stack.getHoverName().getString()));
        return false;
    }

    public static void noteDropKeyRelease() { dropKeyReleased = true; }

    private static boolean protectedItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (QolOptions.PROTECT_DURABLE.getBooleanValue() && stack.isDamageableItem()) return true;
        if (QolOptions.PROTECT_ENCHANTED.getBooleanValue() && stack.isEnchanted()) return true;
        if (!QolOptions.PROTECT_VALUABLE.getBooleanValue()) return false;
        Rarity rarity = stack.getRarity();
        return rarity == Rarity.RARE || rarity == Rarity.EPIC
                || stack.getItem() == Items.DIAMOND || stack.getItem() == Items.EMERALD
                || stack.getItem() == Items.NETHERITE_INGOT || stack.getItem() == Items.NETHERITE_SCRAP
                || (stack.getItem() instanceof BlockItem block && block.getBlock() instanceof ShulkerBoxBlock);
    }

    public static void rememberServer(ServerData source) {
        if (source == null) return;
        if (lastServer != null && source.ip.equals(lastServer.ip) && source.name.equals(lastServer.name)) {
            lastServer.copyFrom(source);
            return;
        }
        lastServer = new ServerData(source.name, source.ip, source.type());
        lastServer.copyFrom(source);
    }

    public static Button reconnectButton(Screen disconnected, Screen parent) {
        if (!QolOptions.RECONNECT.getBooleanValue() || lastServer == null || !Minecraft.getInstance().allowsMultiplayer()) return null;
        ServerData snapshot = new ServerData(lastServer.name, lastServer.ip, lastServer.type());
        snapshot.copyFrom(lastServer);
        return Button.builder(Component.literal("Reconnect"), b -> Minecraft.getInstance().gui.setScreen(
                new ManualReconnectScreen(disconnected, parent, snapshot, QolOptions.RECONNECT_DELAY.getIntegerValue())))
                .width(200).build();
    }

    public record DeathLocation(String dimension, double x, double y, double z, String time) {
        public String coordinates() { return Math.round(x) + ", " + Math.round(y) + ", " + Math.round(z); }
    }
}
