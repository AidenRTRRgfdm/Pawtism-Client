package dev.pawtism.client.mixin;

import dev.pawtism.client.skin.SkinVoxelCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(TextureManager.class)
public abstract class TextureManagerSkinCacheMixin {
    @Inject(method = "reload", at = @At("RETURN"))
    private void pawtism$refreshSkinMeshes(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        // Resource preparation runs asynchronously. Invalidate after completion on
        // the game thread, so a replaced default skin cannot remain cached.
        cir.getReturnValue().whenComplete((unused, error) -> Minecraft.getInstance().execute(SkinVoxelCache::clear));
    }
}
