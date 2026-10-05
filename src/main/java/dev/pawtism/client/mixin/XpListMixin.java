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
        g.fill(x, y, right, bottom, XpTheme.color(0xFFF8F7ED, 0xFF181E2A));
        g.fill(x, y, right, y + 1, XpTheme.color(0xFFACA899, 0xFF101620));
        g.fill(x, y + 1, x + 1, bottom, XpTheme.color(0xFFACA899, 0xFF101620));
        g.fill(x, bottom - 1, right, bottom, XpTheme.color(0xFFFFFFFF, 0xFF52617A));
        g.fill(right - 1, y + 1, right, bottom - 1, XpTheme.color(0xFFFFFFFF, 0xFF52617A));
        ci.cancel();
    }

    @Inject(method = "extractListSeparators", at = @At("HEAD"), cancellable = true)
    private void pawtism$listSeparators(GuiGraphicsExtractor g, CallbackInfo ci) {
        if (!XpTheme.enabled()) return;
        AbstractSelectionList<?> list = (AbstractSelectionList<?>)(Object)this;
        g.fill(list.getX(), list.getY() - 2, list.getRight(), list.getY() - 1, XpTheme.color(0xFFACA899, 0xFF101620));
        g.fill(list.getX(), list.getY() - 1, list.getRight(), list.getY(), XpTheme.color(0xFFFFFFFF, 0xFF52617A));
        g.fill(list.getX(), list.getBottom(), list.getRight(), list.getBottom() + 1, XpTheme.color(0xFFFFFFFF, 0xFF52617A));
        g.fill(list.getX(), list.getBottom() + 1, list.getRight(), list.getBottom() + 2, XpTheme.color(0xFFACA899, 0xFF101620));
        ci.cancel();
    }

    @Redirect(method = "extractSelection", at = @At(value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V"))
    private void pawtism$selectionEdge(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, int color) {
        if (!XpTheme.enabled()) { g.fill(x0, y0, x1, y1, color); return; }
        boolean focused = ((AbstractSelectionList<?>)(Object)this).isFocused();
        g.fill(x0, y0, x1, y1, focused ? XpTheme.color(0xFF316AC5, 0xFF7FA8ED) : XpTheme.color(0xFF90A6C2, 0xFF4E617E));
    }

    @Redirect(method = "extractSelection", at = @At(value = "INVOKE", ordinal = 1,
        target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V"))
    private void pawtism$selectionBody(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, int color) {
        if (!XpTheme.enabled()) { g.fill(x0, y0, x1, y1, color); return; }
        boolean focused = ((AbstractSelectionList<?>)(Object)this).isFocused();
        g.fillGradient(x0, y0, x1, y1, focused ? XpTheme.color(0xFFE7F0FF, 0xFF304A70) : XpTheme.color(0xFFF3F5F7, 0xFF2C3444), focused ? XpTheme.color(0xFFCDDEFA, 0xFF243C5D) : XpTheme.color(0xFFE2E8EF, 0xFF242B37));
    }
}
