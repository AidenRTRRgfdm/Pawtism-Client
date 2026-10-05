package dev.pawtism.client.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.pawtism.client.HudRenderer;
import dev.pawtism.client.PawtismConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Hud.class)
public abstract class CrosshairMixin {
    // Replace just the crosshair sprite; vanilla visibility rules and the attack indicator remain intact.
    @Redirect(method = "extractCrosshair", at = @At(value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    private void pawtism$crosshair(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier sprite,
                                 int x, int y, int width, int height) {
        if (PawtismConfig.CROSSHAIR.getBooleanValue()) {
            HudRenderer.renderCrosshair(graphics);
        } else {
            graphics.blitSprite(pipeline, sprite, x, y, width, height);
        }
    }
}
