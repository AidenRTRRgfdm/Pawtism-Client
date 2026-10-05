package dev.pawtism.client.skin;

/** Immutable ARGB snapshot; native image buffers are never retained by the mesh cache. */
public record SkinPixels(int width, int height, int[] argb) {
    public SkinPixels {
        if (width <= 0 || height <= 0 || argb.length != width * height) {
            throw new IllegalArgumentException("Invalid skin dimensions");
        }
        argb = argb.clone();
    }

    public boolean opaque(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height
                && (argb[y * width + x] >>> 24) >= 16;
    }
}
