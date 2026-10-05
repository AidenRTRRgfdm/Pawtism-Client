package dev.pawtism.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.pawtism.client.PawtismConfig;
import dev.pawtism.client.item.ItemPhysicsState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.item.ItemEntity;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Purely cosmetic: never changes an item entity's movement, collision, or pickup. */
@Mixin(ItemEntityRenderer.class)
public abstract class ItemPhysicsMixin {
    @Unique private ItemEntityRenderState pawtism$currentItem;

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;F)V", at = @At("TAIL"))
    private void pawtism$extractPose(ItemEntity entity, ItemEntityRenderState state, float tickDelta, CallbackInfo ci) {
        // A stable orientation per entity replaces perpetual spinning on the ground.
        float yaw = (entity.getId() * 137.50776f) % 360f;
        ((ItemPhysicsState) state).pawtism$setItemPose(entity.onGround(), yaw);
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("HEAD"))
    private void pawtism$beginItem(ItemEntityRenderState state, PoseStack pose, SubmitNodeCollector collector,
                                 CameraRenderState camera, CallbackInfo ci) {
        pawtism$currentItem = state;
    }

    @Redirect(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void pawtism$restOnFloor(PoseStack pose, float x, float vanillaY, float z) {
        if (!pawtism$flat()) { pose.translate(x, vanillaY, z); return; }
        var box = pawtism$currentItem.item.getModelBoundingBox();
        double stackLift = box.getZsize() <= 0.0625
                ? Math.max(0, pawtism$currentItem.count - 1) * box.getZsize() * 0.75
                : pawtism$currentItem.count > 1 ? 0.15 : 0;
        pose.translate(x, (float) (box.maxZ + stackLift + 0.015), z);
    }

    @Redirect(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionfc;)V"))
    private void pawtism$flatRotation(PoseStack pose, Quaternionfc vanillaRotation) {
        if (!pawtism$flat()) { pose.mulPose(vanillaRotation); return; }
        float yaw = ((ItemPhysicsState) pawtism$currentItem).pawtism$groundYaw();
        pose.mulPose(new Quaternionf().rotationY((float) Math.toRadians(yaw)).rotateX((float) Math.PI / 2));
    }

    @Unique private boolean pawtism$flat() {
        return PawtismConfig.ITEM_PHYSICS.getBooleanValue() && pawtism$currentItem != null
                && ((ItemPhysicsState) pawtism$currentItem).pawtism$isOnGround();
    }
}
