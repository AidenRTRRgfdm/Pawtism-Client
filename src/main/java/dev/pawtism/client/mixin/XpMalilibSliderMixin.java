package dev.pawtism.client.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.pawtism.client.PawtismScreen;
import dev.pawtism.client.ui.XpTheme;
import fi.dy.masa.malilib.gui.widgets.WidgetSlider;
import fi.dy.masa.malilib.render.GuiContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Keeps the native slider's value, drag handling and formatted display. */
@Mixin(value = WidgetSlider.class, remap = false)
public abstract class XpMalilibSliderMixin {
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/render/GuiContext;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    private void pawtism$slider(GuiContext context, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height) {
        if (!PawtismScreen.xpControls()) { context.blitSprite(pipeline, sprite, x, y, width, height); return; }
        GuiGraphicsExtractor graphics = context.getGuiGraphics();
        if (sprite.equals(WidgetSlider.BUTTON_DISABLE_TEXTURE)) {
            graphics.fill(x, y, x + width, y + height, XpTheme.BEIGE);
            graphics.fill(x + 2, y + height / 2 - 2, x + width - 2, y + height / 2 + 1, 0xff8c929b);
            graphics.fill(x + 2, y + height / 2 + 1, x + width - 2, y + height / 2 + 2, 0xffffffff);
        } else XpTheme.button(graphics, x, y, width, height, false, false, true);
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/gui/widgets/WidgetSlider;drawString(Lfi/dy/masa/malilib/render/GuiContext;IIILjava/lang/String;)V"), index = 3)
    private int pawtism$valueColor(int color) { return PawtismScreen.xpControls() ? XpTheme.DARK_TEXT : color; }
}
