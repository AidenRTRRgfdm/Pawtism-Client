package dev.pawtism.client.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.pawtism.client.ui.XpTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EditBox.class)
public abstract class XpEditBoxMixin extends AbstractWidget {
    @Shadow private int textColor;
    @Shadow private int textColorUneditable;
    @Shadow private boolean textShadow;
    @Unique private int pawtism$oldColor, pawtism$oldInactive;
    @Unique private boolean pawtism$oldShadow, pawtism$restoring;
    protected XpEditBoxMixin(int x, int y, int w, int h, Component text) { super(x, y, w, h, text); }
    @Inject(method = "extractWidgetRenderState", at = @At("HEAD"))
    private void pawtism$readable(GuiGraphicsExtractor g, int mx, int my, float delta, CallbackInfo ci) {
        pawtism$restoring = XpTheme.enabled() && (((EditBox) (Object) this).isBordered()
            || !(net.minecraft.client.Minecraft.getInstance().gui.screen() instanceof net.minecraft.client.gui.screens.ChatScreen));
        if (!pawtism$restoring) return;
        pawtism$oldColor = textColor; pawtism$oldInactive = textColorUneditable; pawtism$oldShadow = textShadow;
        textColor = 0xff202638; textColorUneditable = 0xff77777e; textShadow = false;
    }
    @Inject(method = "extractWidgetRenderState", at = @At("RETURN"))
    private void pawtism$restore(GuiGraphicsExtractor g, int mx, int my, float delta, CallbackInfo ci) {
        if (!pawtism$restoring) return;
        textColor = pawtism$oldColor; textColorUneditable = pawtism$oldInactive; textShadow = pawtism$oldShadow; pawtism$restoring = false;
    }
    @Redirect(method = "extractWidgetRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    private void pawtism$field(GuiGraphicsExtractor g, RenderPipeline pipeline, Identifier sprite, int x, int y, int w, int h) {
        if (!XpTheme.enabled()) { g.blitSprite(pipeline, sprite, x, y, w, h); return; }
        g.fill(x, y, x + w, y + h, isFocused() ? 0xff316ac5 : 0xff7f9db9);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, active ? 0xffffffff : 0xffece9d8);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, 0xffd3d6dc);
    }
}
