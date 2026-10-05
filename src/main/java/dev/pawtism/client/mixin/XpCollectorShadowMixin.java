package dev.pawtism.client.mixin;

import dev.pawtism.client.ui.XpText;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Only the scoped themed menu collectors flatten their text. */
@Mixin(GuiGraphicsExtractor.RenderingTextCollector.class)
public abstract class XpCollectorShadowMixin {
    @ModifyArg(method = "accept(Lnet/minecraft/client/gui/TextAlignment;IILnet/minecraft/client/gui/ActiveTextCollector$Parameters;Lnet/minecraft/util/FormattedCharSequence;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/state/gui/GuiTextRenderState;<init>(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;Lorg/joml/Matrix3x2fc;IIIIZZLnet/minecraft/client/gui/navigation/ScreenRectangle;)V"), index = 7)
    private boolean pawtism$flatText(boolean shadow) { return XpText.flatText() ? false : shadow; }
}
