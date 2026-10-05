package dev.pawtism.client.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.pawtism.client.ui.XpTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractScrollArea.class)
public abstract class XpScrollbarMixin {
    @Shadow @Final private AbstractScrollArea.ScrollbarSettings scrollbarSettings;
    @Shadow private boolean scrolling;
    @Shadow protected abstract boolean isOverScrollbar(double x, double y);
    @Unique private boolean pawtism$barHovered;

    @Inject(method = "extractScrollbar", at = @At("HEAD"))
    private void pawtism$scrollbarHover(GuiGraphicsExtractor g, int mouseX, int mouseY, CallbackInfo ci) {
        pawtism$barHovered = XpTheme.enabled() && isOverScrollbar(mouseX, mouseY);
    }

    @Redirect(method = "extractScrollbar", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    private void pawtism$scrollbar(GuiGraphicsExtractor g, RenderPipeline pipeline, Identifier sprite,
                                  int x, int y, int width, int height) {
        if (!XpTheme.enabled()) { g.blitSprite(pipeline, sprite, x, y, width, height); return; }
        if (sprite.equals(scrollbarSettings.backgroundSprite())) {
            g.fillGradient(x, y, x + width, y + height, 0xFFE8E5D8, 0xFFF4F2E9);
            g.fill(x, y, x + 1, y + height, 0xFFB8B5A7);
            g.fill(x + width - 1, y, x + width, y + height, 0xFFFFFFFF);
            return;
        }
        boolean disabled = sprite.equals(scrollbarSettings.disabledScrollerSprite());
        int edge = disabled ? 0xFFACA899 : pawtism$barHovered || scrolling ? 0xFF316AC5 : 0xFF7B9AC5;
        g.fill(x, y, x + width, y + height, edge);
        if (width > 2 && height > 2) {
            g.fillGradient(x + 1, y + 1, x + width - 1, y + height - 1,
                disabled ? 0xFFF0EDE1 : 0xFFEEF4FF, disabled ? 0xFFD8D4C6 : 0xFFC8D8EE);
            g.fill(x + 1, y + 1, x + width - 1, y + 2, 0xFFFFFFFF);
            if (width >= 5 && height >= 14) {
                int grip = disabled ? 0xFFAAA79B : 0xFF7591B9;
                for (int i = -1; i <= 1; i++) g.fill(x + 2, y + height / 2 + i * 3,
                    x + width - 2, y + height / 2 + i * 3 + 1, grip);
            }
        }
    }
}
