package dev.pawtism.client.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.pawtism.client.ui.XpTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractSliderButton.class)
public abstract class XpSliderMixin extends AbstractWidget.WithInactiveMessage {
    protected XpSliderMixin(int x, int y, int width, int height, Component message) { super(x, y, width, height, message); }
    @Redirect(method = "extractWidgetRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V"))
    private void pawtism$slider(GuiGraphicsExtractor g, RenderPipeline pipeline, Identifier sprite, int x, int y, int w, int h, int color) {
        if (!XpTheme.enabled()) { g.blitSprite(pipeline, sprite, x, y, w, h, color); return; }
        if (sprite.getPath().contains("handle")) XpTheme.button(g, x, y, w, h, isHovered(), isFocused(), active);
        else {
            XpTheme.panel(g, x, y, w, h);
            g.fill(x + 4, y + h / 2 - 1, x + w - 4, y + h / 2 + 2, 0xff8c929b);
            g.fill(x + 4, y + h / 2 + 2, x + w - 4, y + h / 2 + 3, 0xffffffff);
        }
    }
    @ModifyArg(method = "extractWidgetRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/AbstractSliderButton;extractScrollingStringOverContents(Lnet/minecraft/client/gui/ActiveTextCollector;Lnet/minecraft/network/chat/Component;I)V"), index = 1)
    private Component pawtism$label(Component label) {
        return XpTheme.enabled() ? label.copy().withStyle(s -> s.withColor(active ? 0x202638 : 0x83838a)) : label;
    }
}
