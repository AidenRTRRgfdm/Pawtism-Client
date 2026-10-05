package dev.pawtism.client.mixin;

import dev.pawtism.client.PawtismScreen;
import dev.pawtism.client.ui.XpTheme;
import fi.dy.masa.malilib.gui.LeftRight;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.interfaces.IGuiIcon;
import fi.dy.masa.malilib.render.GuiContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Changes library drawing only; its hover processing and action listeners still run normally. */
@Mixin(value = ButtonGeneric.class, remap = false)
public abstract class XpMalilibButtonMixin extends ButtonBase {
    @Shadow protected boolean renderDefaultBackground;
    @Shadow protected boolean textCentered;
    @Shadow protected LeftRight alignment;
    @Shadow @Final protected IGuiIcon icon;

    protected XpMalilibButtonMixin(int x, int y, int width, int height) { super(x, y, width, height); }

    @Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true)
    private void pawtism$background(GuiContext context, CallbackInfo ci) {
        if (!PawtismScreen.xpControls()) return;
        if (renderDefaultBackground) XpTheme.button(context.getGuiGraphics(), getX(), getY(), getWidth(), getHeight(), hovered, false, enabled);
        ci.cancel();
    }

    @Inject(method = "drawText", at = @At("HEAD"), cancellable = true)
    private void pawtism$text(GuiContext context, CallbackInfo ci) {
        if (!PawtismScreen.xpControls()) return;
        ci.cancel();
        if (displayString == null || displayString.isBlank()) return;
        String label = ChatFormatting.stripFormatting(displayString);
        if (label == null) return;
        int color = enabled ? XpTheme.DARK_TEXT : XpTheme.MUTED_TEXT;
        if (enabled && displayString.contains("§a")) color = 0xff28632f;
        else if (enabled && displayString.contains("§c")) color = 0xff9b3833;
        int textX = getX() + 6;
        if (textCentered) textX = getX() + (getWidth() - textRenderer.width(label)) / 2;
        else if (icon != null && alignment == LeftRight.LEFT) textX += icon.getWidth() + 2;
        GuiGraphicsExtractor graphics = context.getGuiGraphics();
        graphics.text(textRenderer, label, textX, getY() + (getHeight() - 8) / 2, color, false);
    }
}
