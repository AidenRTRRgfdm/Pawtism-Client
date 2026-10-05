package dev.pawtism.client.mixin;

import dev.pawtism.client.combat.CombatModules;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class CombatDamageMixin {
    // TAIL runs after vanilla PacketUtils has moved processing onto the client thread.
    @Inject(method = "handleDamageEvent", at = @At("TAIL"))
    private void pawtism$confirmedDamage(ClientboundDamageEventPacket packet, CallbackInfo ci) {
        CombatModules.damageEvent(packet);
    }
}
