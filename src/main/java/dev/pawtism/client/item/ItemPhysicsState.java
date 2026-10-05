package dev.pawtism.client.item;

public interface ItemPhysicsState {
    boolean pawtism$isOnGround();
    float pawtism$groundYaw();
    void pawtism$setItemPose(boolean grounded, float yaw);
}
