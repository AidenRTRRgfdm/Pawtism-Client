package dev.pawtism.client.mixin;

import dev.pawtism.client.ui.XpTheme;
import dev.pawtism.client.ui.XpTitleScreen;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Gui.class)
public abstract class XpGuiMixin {
    @ModifyVariable(method = "setScreen", at = @At("LOAD"), argsOnly = true, ordinal = 0)
    private Screen pawtism$desktop(Screen screen) {
        if (XpTheme.enabled() && screen != null && screen.getClass() == TitleScreen.class) return new XpTitleScreen();
        if (!XpTheme.enabled() && screen instanceof XpTitleScreen) return new TitleScreen();
        return screen;
    }
}
