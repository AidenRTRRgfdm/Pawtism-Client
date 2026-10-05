package dev.pawtism.client;

import com.google.gson.JsonObject;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.server.level.ParticleStatus;

/** Deliberately changes rendering settings only, and retains the user's original values. */
public final class PerformancePreset {
    private static Boolean applied;
    private PerformancePreset() { }
    public static void restoreOnExit(Minecraft client) {
        boolean enabled = PawtismConfig.PERFORMANCE_MODE.getBooleanValue();
        if (!PawtismConfig.extras().has("performanceBackup")) return;
        PawtismConfig.PERFORMANCE_MODE.setBooleanValue(false);
        applied = null;
        update(client);
        PawtismConfig.PERFORMANCE_MODE.setBooleanValue(enabled);
        PawtismConfig.INSTANCE.save();
    }
    public static void update(Minecraft client) {
        boolean enabled = PawtismConfig.PERFORMANCE_MODE.getBooleanValue();
        if (applied != null && enabled == applied) return;
        applied = enabled;
        Options options = client.options;
        JsonObject state = PawtismConfig.extras();
        if (enabled) {
            if (state.has("performanceBackup")) {
                try { VideoBackup.read(state.getAsJsonObject("performanceBackup")); }
                catch (Exception error) {
                    PawtismConfig.LOGGER.error("Invalid video backup retained; performance preset was not applied", error);
                    PawtismConfig.PERFORMANCE_MODE.setBooleanValue(false);
                    PawtismConfig.INSTANCE.save();
                    return;
                }
            }
            if (!state.has("performanceBackup")) {
                JsonObject backup = new JsonObject();
                backup.addProperty("renderDistance", options.renderDistance().get());
                backup.addProperty("simulationDistance", options.simulationDistance().get());
                backup.addProperty("clouds", options.cloudStatus().get().name());
                backup.addProperty("particles", options.particles().get().name());
                backup.addProperty("entityShadows", options.entityShadows().get());
                state.add("performanceBackup", backup);
                PawtismConfig.INSTANCE.save();
            }
            options.renderDistance().set(Math.min(options.renderDistance().get(), 12));
            options.simulationDistance().set(Math.min(options.simulationDistance().get(), 8));
            options.cloudStatus().set(CloudStatus.OFF);
            options.particles().set(ParticleStatus.DECREASED);
            options.entityShadows().set(false);
            options.save();
        } else if (state.has("performanceBackup")) {
            try {
                // Validate the entire backup before changing even one setting.
                VideoBackup backup = VideoBackup.read(state.getAsJsonObject("performanceBackup"));
                options.renderDistance().set(backup.renderDistance());
                options.simulationDistance().set(backup.simulationDistance());
                options.cloudStatus().set(backup.clouds());
                options.particles().set(backup.particles());
                options.entityShadows().set(backup.shadows());
                state.remove("performanceBackup");
                options.save();
                PawtismConfig.INSTANCE.save();
            } catch (Exception error) {
                PawtismConfig.LOGGER.error("Cannot restore saved video options; backup retained", error);
            }
        }
    }
    private record VideoBackup(int renderDistance, int simulationDistance, CloudStatus clouds,
                               ParticleStatus particles, boolean shadows) {
        static VideoBackup read(JsonObject data) {
            int render = number(data, "renderDistance");
            int simulation = number(data, "simulationDistance");
            var clouds = data.getAsJsonPrimitive("clouds");
            var particles = data.getAsJsonPrimitive("particles");
            var shadows = data.getAsJsonPrimitive("entityShadows");
            if (!clouds.isString() || !particles.isString() || !shadows.isBoolean())
                throw new IllegalArgumentException("Invalid video backup types");
            return new VideoBackup(render, simulation, CloudStatus.valueOf(clouds.getAsString()),
                    ParticleStatus.valueOf(particles.getAsString()), shadows.getAsBoolean());
        }
        private static int number(JsonObject data, String key) {
            var value = data.getAsJsonPrimitive(key);
            if (!value.isNumber()) throw new IllegalArgumentException("Invalid " + key);
            int result = value.getAsBigDecimal().intValueExact();
            if (result < 1 || result > 512) throw new IllegalArgumentException("Invalid " + key);
            return result;
        }
    }
}
