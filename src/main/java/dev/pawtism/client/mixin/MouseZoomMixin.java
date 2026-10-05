package dev.pawtism.client.mixin;

import dev.pawtism.client.HudRenderer;
import dev.pawtism.client.PawtismConfig;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(MouseHandler.class)
public abstract class MouseZoomMixin {
    @ModifyArgs(method = "turnPlayer", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    private void pawtism$zoomSensitivity(Args args) {
        if (HudRenderer.isZoomActive()) {
            double factor = Math.max(1, PawtismConfig.ZOOM_FACTOR.getIntegerValue());
            args.set(0, (double) args.get(0) / factor);
            args.set(1, (double) args.get(1) / factor);
        }
    }
}
