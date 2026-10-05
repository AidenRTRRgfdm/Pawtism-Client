package dev.pawtism.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.pawtism.client.PawtismConfig;
import dev.pawtism.client.skin.VoxelPlayerModel;
import dev.pawtism.client.skin.ModelPartSnapshot;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class FirstPersonSkinLayersMixin {
    @Inject(method = "renderHand", at = @At("HEAD"))
    private void pawtism$handSkin(PoseStack pose, SubmitNodeCollector collector, int light,
                                Identifier texture, ModelPart arm, boolean sleeve, CallbackInfo ci) {
        var model = ((LivingEntityRenderer<?, ?, ?>) (Object) this).getModel();
        if (model instanceof VoxelPlayerModel voxel)
            voxel.pawtism$useSkin(texture, PawtismConfig.SKIN_3D.getBooleanValue());
    }

    @Redirect(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModelPart(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IILnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"))
    private void pawtism$snapshotHand(SubmitNodeCollector collector, ModelPart arm, PoseStack pose,
                                      RenderType type, int light, int overlay, TextureAtlasSprite sprite) {
        // World avatars and first-person arms share the renderer's model. A snapshot
        // prevents later world-model setup from changing a deferred arm's skin mesh.
        collector.submitModelPart(ModelPartSnapshot.copy(arm), pose, type, light, overlay, sprite);
    }
}
