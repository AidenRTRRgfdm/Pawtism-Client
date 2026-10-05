package dev.pawtism.client.mixin;

import dev.pawtism.client.ui.XpText;
import fi.dy.masa.malilib.render.RenderUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Library comment tooltips draw their own dark surface outside the native tooltip path. */
@Mixin(value = RenderUtils.class, remap = false)
public abstract class XpMalilibTooltipMixin {
    @Inject(method = "drawHoverText", at = @At("HEAD"))
    private static void pawtism$tooltipStart(CallbackInfo ci) { XpText.enterTooltip(); }

    @Inject(method = "drawHoverText", at = @At("RETURN"))
    private static void pawtism$tooltipEnd(CallbackInfo ci) { XpText.leaveTooltip(); }
}
