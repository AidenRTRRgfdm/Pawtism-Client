package dev.pawtism.client.mixin;
import dev.pawtism.client.qol.QolOptions;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastManager.class)
public abstract class PawtismQolToastMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void pawtism$toastGraphics(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (QolOptions.HIDE_TOASTS.getBooleanValue()) ci.cancel();
    }
}
