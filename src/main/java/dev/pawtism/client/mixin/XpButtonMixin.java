package dev.pawtism.client.mixin;

import dev.pawtism.client.ui.XpTheme;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractButton.class)
public abstract class XpButtonMixin extends AbstractWidget.WithInactiveMessage {
    protected XpButtonMixin(int x, int y, int width, int height, Component message) { super(x, y, width, height, message); }
    @Inject(method = "extractDefaultSprite", at = @At("HEAD"), cancellable = true)
    private void pawtism$button(GuiGraphicsExtractor g, CallbackInfo ci) {
        if (!XpTheme.enabled()) return;
        XpTheme.button(g, getX(), getY(), getWidth(), getHeight(), isHovered(), isFocused(), active);
        ci.cancel();
    }
    @Inject(method = "extractDefaultLabel", at = @At("HEAD"), cancellable = true)
    private void pawtism$label(ActiveTextCollector output, CallbackInfo ci) {
        if (!XpTheme.enabled()) return;
        extractScrollingStringOverContents(output, getMessage().copy().withStyle(s -> s.withColor(active ? 0x202638 : 0x83838a)), 2);
        ci.cancel();
    }
}
