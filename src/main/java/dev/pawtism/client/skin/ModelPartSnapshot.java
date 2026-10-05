package dev.pawtism.client.skin;

import dev.pawtism.client.mixin.ModelPartCubesAccessor;
import net.minecraft.client.model.geom.ModelPart;

import java.util.HashMap;
import java.util.Map;

/** Snapshot poses/mesh references for deferred hand submissions, including sleeves. */
public final class ModelPartSnapshot {
    private ModelPartSnapshot() {}

    public static ModelPart copy(ModelPart source) {
        ModelPartCubesAccessor access = (ModelPartCubesAccessor) (Object) source;
        Map<String, ModelPart> children = new HashMap<>();
        access.pawtism$getChildren().forEach((name, child) -> children.put(name, copy(child)));
        ModelPart result = new ModelPart(access.pawtism$getCubes(), children);
        result.loadPose(source.storePose());
        result.visible = source.visible;
        result.skipDraw = source.skipDraw;
        return result;
    }
}
