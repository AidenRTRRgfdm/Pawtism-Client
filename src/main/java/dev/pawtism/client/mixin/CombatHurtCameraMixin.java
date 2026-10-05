package dev.pawtism.client.mixin;

import dev.pawtism.client.combat.CombatOptions;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CombatHurtCameraMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void pawtism$steadyHurtCamera(CameraRenderState state, float partialTick, CallbackInfo ci) {
        if (CombatOptions.STABLE_HURT_CAMERA.getBooleanValue()) state.entityRenderState.hurtTime = 0;
    }
}
