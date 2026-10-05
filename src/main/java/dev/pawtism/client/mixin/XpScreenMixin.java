package dev.pawtism.client.mixin;

import dev.pawtism.client.ui.XpTheme;
import dev.pawtism.client.ui.XpTitleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class XpScreenMixin {
    @Shadow public int width;
    @Shadow public int height;
    @Shadow public abstract boolean isInGameUi();
    @Shadow public abstract Component getTitle();
    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void pawtism$background(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!XpTheme.enabled() || isInGameUi()) return;
        if ((Object) this instanceof XpTitleScreen) { ci.cancel(); return; }
        Minecraft client = Minecraft.getInstance();
        XpTheme.menuBackground(g, client.font, width, height, getTitle().getString(), client.level != null && (Object) this instanceof PauseScreen);
        client.gui.hud.extractDeferredSubtitles();
        ci.cancel();
    }
}
