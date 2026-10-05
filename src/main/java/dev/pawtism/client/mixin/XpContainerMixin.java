package dev.pawtism.client.mixin;

import dev.pawtism.client.ui.XpTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws each native active slot at its original coordinates before its icon and stack. */
@Mixin(AbstractContainerScreen.class)
public abstract class XpContainerMixin {
    @Inject(method = "extractSlot", at = @At("HEAD"))
    private void pawtism$xpSlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        if (XpTheme.enabled()) XpTheme.slot(graphics, slot.x - 1, slot.y - 1);
    }
}
