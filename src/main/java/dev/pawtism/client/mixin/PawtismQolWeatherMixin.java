package dev.pawtism.client.mixin;
import dev.pawtism.client.qol.QolOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class PawtismQolWeatherMixin {
    @Inject(method = {"getRainLevel(F)F", "getThunderLevel(F)F"}, at = @At("HEAD"), cancellable = true)
    private void pawtism$clientWeather(float delta, CallbackInfoReturnable<Float> cir) {
        // Integrated/server levels are deliberately left untouched.
        if ((Object) this instanceof ClientLevel && QolOptions.CLEAR_WEATHER.getBooleanValue()) cir.setReturnValue(0.0f);
    }
}
