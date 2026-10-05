package dev.pawtism.client.mixin;
import dev.pawtism.client.qol.QolModules;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class PawtismQolDropKeyMixin {
    @Inject(method = "keyPress", at = @At("HEAD"))
    private void pawtism$dropRelease(long window, int action, KeyEvent event, CallbackInfo ci) {
        if (action == GLFW.GLFW_RELEASE && Minecraft.getInstance().options.keyDrop.matches(event))
            QolModules.noteDropKeyRelease();
    }
}
