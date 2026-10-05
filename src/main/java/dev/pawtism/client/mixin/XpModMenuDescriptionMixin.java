package dev.pawtism.client.mixin;

import dev.pawtism.client.ui.XpTheme;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Changes only Mod Menu's ordinary gray description color, leaving styled runs and links intact. */
@Mixin(targets = "com.terraformersmc.modmenu.gui.widget.DescriptionListWidget$DescriptionEntry", remap = false)
public abstract class XpModMenuDescriptionMixin {
    @ModifyArg(method = "extractContent", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)V",
        remap = false), index = 4)
    private int pawtism$descriptionColor(int color) {
        return XpTheme.enabled() && (color & 0xFFFFFF) == 0xAAAAAA ? (color & 0xFF000000) | XpTheme.color(0x555555, 0xA2ABBD) : color;
    }
}
