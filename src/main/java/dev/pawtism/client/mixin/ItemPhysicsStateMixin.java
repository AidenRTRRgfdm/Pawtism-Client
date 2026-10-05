package dev.pawtism.client.mixin;

import dev.pawtism.client.item.ItemPhysicsState;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemEntityRenderState.class)
public abstract class ItemPhysicsStateMixin implements ItemPhysicsState {
    @Unique private boolean pawtism$grounded;
    @Unique private float pawtism$yaw;
    @Override public boolean pawtism$isOnGround() { return pawtism$grounded; }
    @Override public float pawtism$groundYaw() { return pawtism$yaw; }
    @Override public void pawtism$setItemPose(boolean grounded, float yaw) {
        pawtism$grounded = grounded;
        pawtism$yaw = yaw;
    }
}
