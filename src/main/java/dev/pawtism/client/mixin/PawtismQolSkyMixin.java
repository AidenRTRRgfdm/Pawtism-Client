package dev.pawtism.client.mixin;
import dev.pawtism.client.qol.QolOptions;
import dev.pawtism.client.qol.SkyClockMath;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public abstract class PawtismQolSkyMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void pawtism$cosmeticSky(ClientLevel level, float delta, Camera camera, SkyRenderState state, CallbackInfo ci) {
        if (!QolOptions.LOCAL_TIME.getBooleanValue() || state.skybox != DimensionType.Skybox.OVERWORLD) return;
        int ticks = QolOptions.SKY_TIME_TICKS.getIntegerValue();
        float angle = SkyClockMath.angleRadians(ticks);
        state.sunAngle = angle;
        state.moonAngle = angle;
        state.starAngle = angle;
        state.starBrightness = SkyClockMath.stars(ticks);
        state.skyColor = SkyClockMath.skyColor(ticks);
        state.sunriseAndSunsetColor = 0;
    }
}
