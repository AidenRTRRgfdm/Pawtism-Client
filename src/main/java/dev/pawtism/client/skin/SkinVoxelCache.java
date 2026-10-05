package dev.pawtism.client.skin;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Bounded cache: every skin is decoded once, never downloaded a second time. */
public final class SkinVoxelCache {
    private static final int MAX_SKINS = 48;
    private static final Map<Key, List<List<ModelPart.Cube>>> MESHES = new LinkedHashMap<>(16, 0.75f, true);
    private static final Map<Identifier, Long> RETRY_AT = new LinkedHashMap<>();

    private SkinVoxelCache() {}

    public static List<List<ModelPart.Cube>> get(Identifier texture, boolean slim,
                                                List<List<ModelPart.Cube>> originals) {
        Key key = new Key(texture, slim);
        List<List<ModelPart.Cube>> cached = MESHES.get(key);
        if (cached != null) return cached;
        long now = System.nanoTime();
        Long retryAt = RETRY_AT.get(texture);
        if (retryAt != null && now - retryAt < 0) return null;
        SkinPixels pixels = read(texture);
        if (pixels == null || pixels.width() != 64 || pixels.height() != 64) {
            RETRY_AT.put(texture, now + 5_000_000_000L);
            while (RETRY_AT.size() > MAX_SKINS * 2) RETRY_AT.remove(RETRY_AT.keySet().iterator().next());
            return null;
        }
        List<List<ModelPart.Cube>> meshes = new ArrayList<>(originals.size());
        for (List<ModelPart.Cube> part : originals) meshes.add(SkinVoxelMesh.build(part, pixels));
        List<List<ModelPart.Cube>> result = List.copyOf(meshes);
        MESHES.put(key, result);
        RETRY_AT.remove(texture);
        while (MESHES.size() > MAX_SKINS) MESHES.remove(MESHES.keySet().iterator().next());
        return result;
    }

    private static SkinPixels read(Identifier texture) {
        Minecraft client = Minecraft.getInstance();
        AbstractTexture loaded = client.getTextureManager().getTexture(texture);
        if (loaded instanceof DynamicTexture dynamic) {
            NativeImage image = dynamic.getPixels();
            if (image != null && !image.isClosed())
                return new SkinPixels(image.getWidth(), image.getHeight(), image.getPixels());
        }
        // Default bundled skins and resource-pack replacements use resource textures.
        var resource = client.getResourceManager().getResource(texture);
        if (resource.isPresent()) {
            try (InputStream stream = resource.get().open(); NativeImage image = NativeImage.read(stream)) {
                return new SkinPixels(image.getWidth(), image.getHeight(), image.getPixels());
            } catch (Exception ignored) {
                // Keep the ordinary outer skin layer visible if skin data is unavailable.
            }
        }
        return null;
    }

    public static void clear() {
        MESHES.clear();
        RETRY_AT.clear();
    }

    public static int cachedSkinCount() { return MESHES.size(); }

    public static int cachedVoxelCount() {
        int count = 0;
        for (List<List<ModelPart.Cube>> skin : MESHES.values())
            for (List<ModelPart.Cube> part : skin) count += part.size();
        return count;
    }

    private record Key(Identifier texture, boolean slim) {}
}
