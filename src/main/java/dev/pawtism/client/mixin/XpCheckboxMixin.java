package dev.pawtism.client.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.pawtism.client.ui.XpTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Checkbox.class)
public abstract class XpCheckboxMixin {
    @Shadow private boolean selected;

    @Redirect(method = "extractContents", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V"))
    private void pawtism$checkbox(GuiGraphicsExtractor g, RenderPipeline pipeline, Identifier sprite,
                                 int x, int y, int width, int height, int tint) {
        if (!XpTheme.enabled()) {
            g.blitSprite(pipeline, sprite, x, y, width, height, tint);
            return;
        }
        Checkbox checkbox = (Checkbox)(Object)this;
        int alpha = tint & 0xFF000000;
        int edge = alpha | (checkbox.isFocused() ? 0x246DB3 : 0x77756C);
        g.fill(x, y, x + width, y + height, alpha | 0xECE9D8);
        g.fill(x + 2, y + 2, x + width - 2, y + height - 2,
            alpha | (checkbox.active ? 0xFFFFFF : 0xE8E5D7));
        g.fill(x, y, x + width, y + 1, edge);
        g.fill(x, y + 1, x + 1, y + height, edge);
        g.fill(x + 1, y + 1, x + width - 1, y + 2, alpha | 0xAAA89D);
        g.fill(x + 1, y + 2, x + 2, y + height - 1, alpha | 0xAAA89D);
        g.fill(x, y + height - 1, x + width, y + height, checkbox.isFocused() ? edge : alpha | 0xFFFFFF);
        g.fill(x + width - 1, y + 1, x + width, y + height - 1, checkbox.isFocused() ? edge : alpha | 0xFFFFFF);
        if (selected) {
            int check = alpha | (checkbox.active ? 0x23812B : 0x929085);
            for (int i = 0; i < 3; i++) g.fill(x + 4 + i, y + 8 + i, x + 6 + i, y + 10 + i, check);
            for (int i = 0; i < 6; i++) g.fill(x + 7 + i, y + 10 - i, x + 9 + i, y + 12 - i, check);
        }
    }
}
