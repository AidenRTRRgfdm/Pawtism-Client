package dev.pawtism.client;

/** Projection math independent of Minecraft so the magnification contract can be tested directly. */
public final class ZoomMath {
    private ZoomMath() {}

    public static float fovForMagnification(float baseFov, int magnification) {
        float finiteFov = Float.isFinite(baseFov) ? baseFov : 70.0f;
        float boundedFov = Math.max(1.0f, Math.min(179.0f, finiteFov));
        int boundedMagnification = Math.max(1, magnification);
        return (float) Math.toDegrees(2.0 * Math.atan(
            Math.tan(Math.toRadians(boundedFov) / 2.0) / boundedMagnification));
    }
}
