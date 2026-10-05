package dev.pawtism.client.mixin;

import dev.pawtism.client.PawtismConfig;
import dev.pawtism.client.skin.SkinVoxelCache;
import dev.pawtism.client.skin.VoxelPlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerModel.class)
public abstract class PlayerSkinLayersMixin implements VoxelPlayerModel {
    @Shadow @Final private boolean slim;
    @Unique private ModelPart[] pawtism$parts;
    @Unique private List<List<ModelPart.Cube>> pawtism$originals;

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"))
    private void pawtism$skinVoxels(AvatarRenderState state, CallbackInfo ci) {
        boolean enabled = PawtismConfig.SKIN_3D.getBooleanValue() && state.distanceToCameraSq <= 256.0;
        pawtism$useSkin(state.skin == null ? null : state.skin.body().texturePath(), enabled);
    }

    @Override
    public void pawtism$useSkin(Identifier texture, boolean enabled) {
        if (pawtism$parts == null) {
            PlayerModel model = (PlayerModel) (Object) this;
            pawtism$parts = new ModelPart[]{model.hat, model.jacket, model.leftSleeve,
                    model.rightSleeve, model.leftPants, model.rightPants};
            List<List<ModelPart.Cube>> saved = new ArrayList<>(pawtism$parts.length);
            for (ModelPart part : pawtism$parts)
                saved.add(((ModelPartCubesAccessor) (Object) part).pawtism$getCubes());
            pawtism$originals = List.copyOf(saved);
        }
        List<List<ModelPart.Cube>> meshes = enabled && texture != null
                ? SkinVoxelCache.get(texture, slim, pawtism$originals) : null;
        List<List<ModelPart.Cube>> selection = meshes != null ? meshes : pawtism$originals;
        for (int i = 0; i < pawtism$parts.length; i++)
            ((ModelPartCubesAccessor) (Object) pawtism$parts[i]).pawtism$setCubes(selection.get(i));
        // Vanilla setupAnim retains each player's hat/jacket/sleeve visibility and pose.
    }
}
