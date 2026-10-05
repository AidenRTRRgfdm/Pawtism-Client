package dev.pawtism.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.pawtism.client.combat.CombatOptions;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class CombatHeldItemMixin {
    @Inject(method = "renderItem", at = @At("HEAD"))
    private void pawtism$compactItem(LivingEntity entity, ItemStack item, ItemDisplayContext type,
                                     PoseStack pose, SubmitNodeCollector collector, int light, CallbackInfo ci) {
        pose.pushPose();
        if (CombatOptions.COMPACT_VIEW.getBooleanValue()
                && (type == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || type == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)) {
            float scale = CombatOptions.VIEW_SCALE.getIntegerValue() / 100f;
            pose.scale(scale, scale, scale);
        }
    }
    @Inject(method = "renderItem", at = @At("RETURN"))
    private void pawtism$restoreHeldPose(LivingEntity entity, ItemStack item, ItemDisplayContext type,
                                        PoseStack pose, SubmitNodeCollector collector, int light, CallbackInfo ci) {
        pose.popPose();
    }
}
