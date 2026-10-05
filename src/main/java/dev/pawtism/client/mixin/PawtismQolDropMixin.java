package dev.pawtism.client.mixin;
import dev.pawtism.client.qol.QolModules;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class PawtismQolDropMixin {
    @Inject(method = "drop(Z)Z", at = @At("HEAD"), cancellable = true)
    private void pawtism$guardHeldDrop(boolean all, CallbackInfoReturnable<Boolean> cir) {
        if (!QolModules.allowHeldDrop((LocalPlayer) (Object) this, all)) cir.setReturnValue(false);
    }
}
