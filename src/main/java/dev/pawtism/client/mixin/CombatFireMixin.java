package dev.pawtism.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.pawtism.client.combat.CombatOptions;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public abstract class CombatFireMixin {
    @Inject(method = "submitFire", at = @At("HEAD"))
    private static void pawtism$lowerFire(PoseStack pose, SubmitNodeCollector collector, TextureAtlasSprite sprite, CallbackInfo ci) {
        pose.pushPose();
        if (CombatOptions.LOW_FIRE.getBooleanValue())
            pose.translate(0, -CombatOptions.FIRE_SHIFT.getIntegerValue() / 100f, 0);
    }
    @Inject(method = "submitFire", at = @At("RETURN"))
    private static void pawtism$restoreFirePose(PoseStack pose, SubmitNodeCollector collector, TextureAtlasSprite sprite, CallbackInfo ci) {
        pose.popPose();
    }
}
