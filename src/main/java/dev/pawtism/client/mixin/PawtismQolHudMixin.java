package dev.pawtism.client.mixin;
import dev.pawtism.client.qol.QolOptions;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class PawtismQolHudMixin {
    @Inject(method = "extractScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void pawtism$sidebar(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) {
        if (QolOptions.HIDE_SCOREBOARD.getBooleanValue()) ci.cancel();
    }
    @Inject(method = "extractBossOverlay", at = @At("HEAD"), cancellable = true)
    private void pawtism$bossBars(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) {
        if (QolOptions.HIDE_BOSS_BARS.getBooleanValue()) ci.cancel();
    }
    @Inject(method = "extractTitle", at = @At("HEAD"), cancellable = true)
    private void pawtism$titles(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) {
        if (QolOptions.HIDE_TITLES.getBooleanValue()) ci.cancel();
    }
}
