package dev.pawtism.client.mixin;

import dev.pawtism.client.ui.XpTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TooltipRenderUtil.class)
public abstract class XpTooltipMixin {
    @Inject(method = "extractTooltipBackground", at = @At("HEAD"), cancellable = true)
    private static void pawtism$tooltip(GuiGraphicsExtractor g, int x, int y, int width, int height,
                                       Identifier style, CallbackInfo ci) {
        if (!XpTheme.enabled()) return;
        int left = x - TooltipRenderUtil.PADDING_LEFT, top = y - TooltipRenderUtil.PADDING_TOP;
        int right = x + width + TooltipRenderUtil.PADDING_RIGHT;
        int bottom = y + height + TooltipRenderUtil.PADDING_BOTTOM;
        g.fill(left + 2, top + 2, right + 2, bottom + 2, 0x66000000);
        g.fillGradient(left, top, right, bottom, 0xF51C2746, 0xF50E1629);
        g.fill(left, top, right, top + 1, 0xFFFFCF67);
        g.fill(left, bottom - 1, right, bottom, 0xFFD6A840);
        g.fill(left, top + 1, left + 1, bottom - 1, 0xFFFFCF67);
        g.fill(right - 1, top + 1, right, bottom - 1, 0xFFD6A840);
        g.fill(left + 1, top + 1, right - 1, top + 2, 0xFF627CAF);
        ci.cancel();
    }
}
