package dev.pawtism.client.mixin;
import dev.pawtism.client.qol.QolModules;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatComponent.class)
public abstract class PawtismQolChatMixin {
    @ModifyVariable(method = "addMessage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Component pawtism$timestampIncoming(Component contents) {
        return QolModules.timestamp(contents);
    }
}
