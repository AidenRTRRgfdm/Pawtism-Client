package dev.pawtism.client.mixin;

import dev.pawtism.client.ui.XpTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Mod Menu 20.0.3 draws selected rows through its own method instead of the native list helper. */
@Mixin(targets = "com.terraformersmc.modmenu.gui.widget.ModListWidget", remap = false)
public abstract class XpModMenuListMixin {
    @Redirect(method = "drawSelectionHighlight", at = @At(value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V", remap = false))
    private void pawtism$modMenuSelectionEdge(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, int color) {
        if (!XpTheme.enabled()) { g.fill(x0, y0, x1, y1, color); return; }
        boolean focused = ((AbstractSelectionList<?>)(Object)this).isFocused();
        g.fill(x0, y0, x1, y1, focused ? XpTheme.color(0xFF316AC5, 0xFF7FA8ED) : XpTheme.color(0xFF90A6C2, 0xFF4E617E));
    }

    @Redirect(method = "drawSelectionHighlight", at = @At(value = "INVOKE", ordinal = 1,
        target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V", remap = false))
    private void pawtism$modMenuSelectionBody(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, int color) {
        if (!XpTheme.enabled()) { g.fill(x0, y0, x1, y1, color); return; }
        boolean focused = ((AbstractSelectionList<?>)(Object)this).isFocused();
        g.fillGradient(x0, y0, x1, y1, focused ? XpTheme.color(0xFFE7F0FF, 0xFF304A70) : XpTheme.color(0xFFF3F5F7, 0xFF2C3444), focused ? XpTheme.color(0xFFCDDEFA, 0xFF243C5D) : XpTheme.color(0xFFE2E8EF, 0xFF242B37));
    }
}
