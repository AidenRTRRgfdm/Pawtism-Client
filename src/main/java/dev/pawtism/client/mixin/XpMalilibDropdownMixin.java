package dev.pawtism.client.mixin;

import dev.pawtism.client.PawtismScreen;
import dev.pawtism.client.ui.XpTheme;
import fi.dy.masa.malilib.gui.widgets.WidgetDropDownList;
import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Retains the config switcher's selection, filtering, scrolling and popup order. */
@Mixin(value = WidgetDropDownList.class, remap = false)
public abstract class XpMalilibDropdownMixin {
    @Shadow protected boolean isOpen;
    @Redirect(method = {"render", "renderOpen"}, at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/render/RenderUtils;drawOutlinedBox(Lfi/dy/masa/malilib/render/GuiContext;IIIIII)V"))
    private void pawtism$box(GuiContext context, int x, int y, int width, int height, int background, int outline) {
        RenderUtils.drawOutlinedBox(context, x, y, width, height,
            PawtismScreen.xpControls() ? 0xfffefefe : background, PawtismScreen.xpControls() ? 0xff7f9db9 : outline);
    }

    @Redirect(method = "renderOpen", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/render/RenderUtils;drawRect(Lfi/dy/masa/malilib/render/GuiContext;IIIII)V"))
    private void pawtism$row(GuiContext context, int x, int y, int width, int height, int color) {
        if (PawtismScreen.xpControls()) color = (color >>> 24) >= 80 ? 0xffd6e7fa : XpTheme.BEIGE;
        RenderUtils.drawRect(context, x, y, width, height, color);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/render/RenderUtils;drawTexturedRect(Lfi/dy/masa/malilib/render/GuiContext;Lnet/minecraft/resources/Identifier;IIIIII)V"))
    private void pawtism$arrow(GuiContext context, Identifier texture, int x, int y, int u, int v, int width, int height) {
        if (!PawtismScreen.xpControls()) { RenderUtils.drawTexturedRect(context, texture, x, y, u, v, width, height); return; }
        GuiGraphicsExtractor graphics = context.getGuiGraphics();
        int center = x + width / 2, top = y + height / 2 - 2;
        for (int row = 0; row < 4; row++) {
            int span = isOpen ? 1 + row * 2 : 7 - row * 2;
            graphics.fill(center - span / 2, top + row, center + span / 2 + 1, top + row + 1, XpTheme.DARK_TEXT);
        }
    }
}
