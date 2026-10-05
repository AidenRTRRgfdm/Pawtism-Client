package dev.pawtism.client.mixin;

import dev.pawtism.client.ui.XpTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSelectionList.class)
public abstract class XpListMixin {
    @Inject(method = "extractListBackground", at = @At("HEAD"), cancellable = true)
    private void pawtism$listBackground(GuiGraphicsExtractor g, CallbackInfo ci) {
        if (!XpTheme.enabled()) return;
        AbstractSelectionList<?> list = (AbstractSelectionList<?>)(Object)this;
        int x = list.getX(), y = list.getY(), right = list.getRight(), bottom = list.getBottom();
        g.fill(x, y, right, bottom, 0xFFF8F7ED);
        g.fill(x, y, right, y + 1, 0xFFACA899);
        g.fill(x, y + 1, x + 1, bottom, 0xFFACA899);
        g.fill(x, bottom - 1, right, bottom, 0xFFFFFFFF);
        g.fill(right - 1, y + 1, right, bottom - 1, 0xFFFFFFFF);
        ci.cancel();
    }

    @Inject(method = "extractListSeparators", at = @At("HEAD"), cancellable = true)
    private void pawtism$listSeparators(GuiGraphicsExtractor g, CallbackInfo ci) {
        if (!XpTheme.enabled()) return;
        AbstractSelectionList<?> list = (AbstractSelectionList<?>)(Object)this;
        g.fill(list.getX(), list.getY() - 2, list.getRight(), list.getY() - 1, 0xFFACA899);
        g.fill(list.getX(), list.getY() - 1, list.getRight(), list.getY(), 0xFFFFFFFF);
        g.fill(list.getX(), list.getBottom(), list.getRight(), list.getBottom() + 1, 0xFFFFFFFF);
        g.fill(list.getX(), list.getBottom() + 1, list.getRight(), list.getBottom() + 2, 0xFFACA899);
        ci.cancel();
    }

    @Redirect(method = "extractSelection", at = @At(value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V"))
    private void pawtism$selectionEdge(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, int color) {
        if (!XpTheme.enabled()) { g.fill(x0, y0, x1, y1, color); return; }
        boolean focused = ((AbstractSelectionList<?>)(Object)this).isFocused();
        g.fill(x0, y0, x1, y1, focused ? 0xFF316AC5 : 0xFF90A6C2);
    }

    @Redirect(method = "extractSelection", at = @At(value = "INVOKE", ordinal = 1,
        target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V"))
    private void pawtism$selectionBody(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, int color) {
        if (!XpTheme.enabled()) { g.fill(x0, y0, x1, y1, color); return; }
        boolean focused = ((AbstractSelectionList<?>)(Object)this).isFocused();
        g.fillGradient(x0, y0, x1, y1, focused ? 0xFFE7F0FF : 0xFFF3F5F7, focused ? 0xFFCDDEFA : 0xFFE2E8EF);
    }
}
