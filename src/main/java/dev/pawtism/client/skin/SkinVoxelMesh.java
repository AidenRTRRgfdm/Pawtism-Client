package dev.pawtism.client.skin;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Original pixel-extrusion implementation using vanilla model UVs and animation.
 * Each nontransparent outer-layer pixel becomes a shallow cuboid. Only its outer
 * surface and edges bordering transparent pixels are emitted; inward faces and
 * sides shared with neighboring opaque pixels are omitted.
 */
public final class SkinVoxelMesh {
    private static final float DEPTH = 0.32f; // model pixels, or 1/50 of a block

    private SkinVoxelMesh() {}

    public static List<ModelPart.Cube> build(List<ModelPart.Cube> originals, SkinPixels skin) {
        List<ModelPart.Cube> mesh = new ArrayList<>();
        for (ModelPart.Cube cube : originals) {
            for (ModelPart.Polygon face : cube.polygons) {
                if (face != null) voxelize(face, skin, mesh);
            }
        }
        return List.copyOf(mesh);
    }

    private static void voxelize(ModelPart.Polygon face, SkinPixels skin, List<ModelPart.Cube> out) {
        float minU = Float.POSITIVE_INFINITY, minV = Float.POSITIVE_INFINITY;
        float maxU = Float.NEGATIVE_INFINITY, maxV = Float.NEGATIVE_INFINITY;
        for (ModelPart.Vertex vertex : face.vertices()) {
            minU = Math.min(minU, vertex.u()); maxU = Math.max(maxU, vertex.u());
            minV = Math.min(minV, vertex.v()); maxV = Math.max(maxV, vertex.v());
        }
        if (maxU - minU < 0.00001f || maxV - minV < 0.00001f) return;
        Vector3f origin = corner(face, minU, minV);
        Vector3f acrossU = corner(face, maxU, minV).sub(origin);
        Vector3f acrossV = corner(face, minU, maxV).sub(origin);
        Direction normal = direction(new Vector3f(face.normal()));
        Direction alongU = direction(acrossU), alongV = direction(acrossV);
        int firstX = Math.round(minU * skin.width()), lastX = Math.round(maxU * skin.width());
        int firstY = Math.round(minV * skin.height()), lastY = Math.round(maxV * skin.height());
        for (int y = firstY; y < lastY; y++) {
            for (int x = firstX; x < lastX; x++) {
                if (!skin.opaque(x, y)) continue;
                float u0 = (x / (float) skin.width() - minU) / (maxU - minU);
                float u1 = ((x + 1f) / skin.width() - minU) / (maxU - minU);
                float v0 = (y / (float) skin.height() - minV) / (maxV - minV);
                float v1 = ((y + 1f) / skin.height() - minV) / (maxV - minV);
                Vector3f p0 = at(origin, acrossU, acrossV, u0, v0);
                Vector3f p1 = at(origin, acrossU, acrossV, u1, v1);
                Vector3f low = new Vector3f(p0).min(p1), high = new Vector3f(p0).max(p1);
                Vector3f extrusion = new Vector3f(face.normal()).mul(DEPTH);
                low.min(new Vector3f(low).add(extrusion));
                high.max(new Vector3f(high).add(extrusion));
                EnumSet<Direction> visible = EnumSet.of(normal);
                if (x == firstX || !skin.opaque(x - 1, y)) visible.add(alongU.getOpposite());
                if (x + 1 == lastX || !skin.opaque(x + 1, y)) visible.add(alongU);
                if (y == firstY || !skin.opaque(x, y - 1)) visible.add(alongV.getOpposite());
                if (y + 1 == lastY || !skin.opaque(x, y + 1)) visible.add(alongV);
                ModelPart.Cube voxel = new ModelPart.Cube(0, 0, low.x, low.y, low.z,
                        high.x - low.x, high.y - low.y, high.z - low.z,
                        0, 0, 0, false, skin.width(), skin.height(), visible);
                // Use the center of this exact skin pixel on every exposed side.
                // This also prevents atlas colors from neighboring body parts bleeding in.
                float pixelU = (x + 0.5f) / skin.width(), pixelV = (y + 0.5f) / skin.height();
                for (ModelPart.Polygon polygon : voxel.polygons) {
                    ModelPart.Vertex[] vertices = polygon.vertices();
                    for (int i = 0; i < vertices.length; i++) vertices[i] = vertices[i].remap(pixelU, pixelV);
                }
                out.add(voxel);
            }
        }
    }

    private static Vector3f corner(ModelPart.Polygon face, float u, float v) {
        for (ModelPart.Vertex vertex : face.vertices()) {
            if (Math.abs(vertex.u() - u) < 0.00001f && Math.abs(vertex.v() - v) < 0.00001f)
                return new Vector3f(vertex.x(), vertex.y(), vertex.z());
        }
        throw new IllegalArgumentException("Nonrectangular skin face UVs");
    }

    private static Vector3f at(Vector3f origin, Vector3f u, Vector3f v, float s, float t) {
        return new Vector3f(origin).fma(s, u).fma(t, v);
    }

    private static Direction direction(Vector3f vector) {
        float x = Math.abs(vector.x), y = Math.abs(vector.y), z = Math.abs(vector.z);
        if (x >= y && x >= z) return vector.x > 0 ? Direction.EAST : Direction.WEST;
        if (y >= z) return vector.y > 0 ? Direction.UP : Direction.DOWN;
        return vector.z > 0 ? Direction.SOUTH : Direction.NORTH;
    }
}
