package dev.pawtism.client.mixin;

import dev.pawtism.client.ui.XpText;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiGraphicsExtractor.class)
public abstract class XpTextMixin {
    @Inject(method = "text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)V", at = @At("HEAD"), cancellable = true)
    private void pawtism$text(Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow, CallbackInfo ci) {
        if (XpText.needsColor(color, y)) {
            ((GuiGraphicsExtractor) (Object) this).text(font, XpText.decorate(text, y), x, y, XpText.color(color, y), false);
            ci.cancel();
        }
    }
    @Inject(method = "textRendererForWidget", at = @At("RETURN"), cancellable = true)
    private void pawtism$widgetText(AbstractWidget owner, GuiGraphicsExtractor.HoveredTextEffects effects, CallbackInfoReturnable<ActiveTextCollector> cir) {
        cir.setReturnValue(XpText.wrap(cir.getReturnValue(), true));
    }
    @Inject(method = "textRenderer(Lnet/minecraft/client/gui/GuiGraphicsExtractor$HoveredTextEffects;Ljava/util/function/Consumer;)Lnet/minecraft/client/gui/ActiveTextCollector;", at = @At("RETURN"), cancellable = true)
    private void pawtism$styledText(GuiGraphicsExtractor.HoveredTextEffects effects, Consumer<Style> consumer, CallbackInfoReturnable<ActiveTextCollector> cir) {
        cir.setReturnValue(XpText.wrap(cir.getReturnValue()));
    }
    @Inject(method = "tooltip", at = @At("HEAD"))
    private void pawtism$tooltipStart(Font font, List<ClientTooltipComponent> lines, int x, int y, ClientTooltipPositioner positioner, Identifier style, CallbackInfo ci) {
        XpText.enterTooltip();
    }
    @Inject(method = "tooltip", at = @At("RETURN"))
    private void pawtism$tooltipEnd(Font font, List<ClientTooltipComponent> lines, int x, int y, ClientTooltipPositioner positioner, Identifier style, CallbackInfo ci) {
        XpText.leaveTooltip();
    }
}
