package dev.pawtism.client.mixin;

import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(ModelPart.class)
public interface ModelPartCubesAccessor {
    @Accessor("cubes") List<ModelPart.Cube> pawtism$getCubes();
    @Mutable @Accessor("cubes") void pawtism$setCubes(List<ModelPart.Cube> cubes);
    @Accessor("children") Map<String, ModelPart> pawtism$getChildren();
}
