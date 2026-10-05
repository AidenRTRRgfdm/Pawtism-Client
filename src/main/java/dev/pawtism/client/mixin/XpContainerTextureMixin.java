package dev.pawtism.client.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.pawtism.client.ui.XpTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces large vanilla container background quads while preserving small functional blits. */
@Mixin(GuiGraphicsExtractor.class)
public abstract class XpContainerTextureMixin {
    @Inject(method = "blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIIIIII)V",
            at = @At("HEAD"), cancellable = true)
    private void pawtism$xpContainerPanel(RenderPipeline pipeline, Identifier texture, int x, int y, float u, float v,
                                         int width, int height, int sourceWidth, int sourceHeight,
                                         int textureWidth, int textureHeight, int color, CallbackInfo ci) {
        if (!XpTheme.enabled() || !(Minecraft.getInstance().gui.screen() instanceof AbstractContainerScreen)
                || !texture.getNamespace().equals("minecraft") || !texture.getPath().startsWith("textures/gui/container/")
                || width < 80 || height < 40) return;
        GuiGraphicsExtractor graphics = (GuiGraphicsExtractor)(Object)this;
        XpTheme.panel(graphics, x, y, width, height);
        if (texture.getPath().endsWith("/furnace.png") || texture.getPath().endsWith("/blast_furnace.png")
                || texture.getPath().endsWith("/smoker.png")) {
            // This empty arrow was part of the replaced sheet. Native cooking progress is drawn over it next.
            graphics.fill(x + 79, y + 39, x + 96, y + 45, 0xffaba99d);
            for (int band = 0; band < 8; band++) {
                int inset = Math.abs(7 - band * 2);
                graphics.fill(x + 94, y + 34 + band * 2, x + 104 - inset, y + 36 + band * 2, 0xffaba99d);
            }
        }
        ci.cancel();
    }
}
