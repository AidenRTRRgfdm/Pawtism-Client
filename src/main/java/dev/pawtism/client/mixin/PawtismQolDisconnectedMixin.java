package dev.pawtism.client.mixin;
import dev.pawtism.client.qol.QolModules;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public abstract class PawtismQolDisconnectedMixin extends Screen {
    @Shadow @Final private Screen parent;
    @Shadow @Final private LinearLayout layout;
    protected PawtismQolDisconnectedMixin(Component title) { super(title); }
    @Inject(method = "init", at = @At("TAIL"))
    private void pawtism$reconnectButton(CallbackInfo ci) {
        Button button = QolModules.reconnectButton(this, parent);
        if (button == null) return;
        layout.addChild(button);
        addRenderableWidget(button);
        layout.arrangeElements();
        repositionElements();
    }
}
