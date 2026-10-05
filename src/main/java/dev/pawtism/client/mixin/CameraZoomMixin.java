package dev.pawtism.client.mixin;

import dev.pawtism.client.HudRenderer;
import dev.pawtism.client.PawtismConfig;
import dev.pawtism.client.ZoomMath;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraZoomMixin {
    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void pawtism$zoom(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (HudRenderer.isZoomActive()) {
            cir.setReturnValue(ZoomMath.fovForMagnification(cir.getReturnValueF(), PawtismConfig.ZOOM_FACTOR.getIntegerValue()));
        }
    }
}
