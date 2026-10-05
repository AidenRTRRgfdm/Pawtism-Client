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
        g.fill(x0, y0, x1, y1, focused ? 0xFF316AC5 : 0xFF90A6C2);
    }

    @Redirect(method = "drawSelectionHighlight", at = @At(value = "INVOKE", ordinal = 1,
        target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V", remap = false))
    private void pawtism$modMenuSelectionBody(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, int color) {
        if (!XpTheme.enabled()) { g.fill(x0, y0, x1, y1, color); return; }
        boolean focused = ((AbstractSelectionList<?>)(Object)this).isFocused();
        g.fillGradient(x0, y0, x1, y1, focused ? 0xFFE7F0FF : 0xFFF3F5F7, focused ? 0xFFCDDEFA : 0xFFE2E8EF);
    }
}
