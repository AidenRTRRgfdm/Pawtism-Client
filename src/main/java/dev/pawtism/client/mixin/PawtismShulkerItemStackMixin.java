package dev.pawtism.client.mixin;

import dev.pawtism.client.ShulkerTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ItemStack.class)
public abstract class PawtismShulkerItemStackMixin {
    @Inject(method = "getTooltipImage", at = @At("RETURN"), cancellable = true)
    private void pawtism$preview(CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
        // Preserve an existing tooltip image supplied by vanilla or another mod.
        if (cir.getReturnValue().isPresent()) return;
        Optional<TooltipComponent> preview = ShulkerTooltip.imageFor((ItemStack) (Object) this);
        preview.ifPresent(value -> cir.setReturnValue(Optional.of(value)));
    }
}
