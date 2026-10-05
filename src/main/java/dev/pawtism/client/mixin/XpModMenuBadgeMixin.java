package dev.pawtism.client.mixin;

import dev.pawtism.client.ui.XpText;
import dev.pawtism.client.ui.XpTheme;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Keep Mod Menu's semantic badge colors and their light labels together on the original dark fills. */
@Mixin(targets = "com.terraformersmc.modmenu.util.DrawingUtil", remap = false)
public abstract class XpModMenuBadgeMixin {
    @Redirect(method = "drawBadge(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIILnet/minecraft/util/FormattedCharSequence;III)V",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)V",
            remap = false))
    private static void pawtism$badgeText(GuiGraphicsExtractor g, Font font, FormattedCharSequence text,
                                         int x, int y, int color, boolean shadow) {
        if (!XpTheme.enabled()) { g.text(font, text, x, y, color, shadow); return; }
        XpText.enterTooltip();
        try {
            g.text(font, text, x, y, color, shadow);
        } finally {
            XpText.leaveTooltip();
        }
    }
}
