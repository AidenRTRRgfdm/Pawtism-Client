package dev.pawtism.client.mixin;

import dev.pawtism.client.combat.CombatModules;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class CombatMouseMixin {
    @Inject(method = "onButton", at = @At("HEAD"))
    private void pawtism$countPress(long window, MouseButtonInfo button, int action, CallbackInfo ci) {
        CombatModules.mouseButton(window, button.button(), action);
    }
}
