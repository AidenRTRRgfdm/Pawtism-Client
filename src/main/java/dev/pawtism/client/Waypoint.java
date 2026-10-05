package dev.pawtism.client;

import java.util.UUID;

/** A saved, user-created location. No entity tracking or teleport commands. */
public record Waypoint(String id, String name, String dimension, double x, double y, double z,
                       int color, boolean visible) {
    public Waypoint {
        if (id == null || id.isBlank() || id.length() > 64) throw new IllegalArgumentException("Invalid waypoint id");
        if (dimension == null || dimension.isBlank() || dimension.length() > 128) throw new IllegalArgumentException("Invalid dimension");
        if (name == null) throw new IllegalArgumentException("Waypoint name required");
        name = name.replaceAll("[\\p{Cntrl}\u00a7]", "").strip();
        if (name.isEmpty() || name.length() > 48) throw new IllegalArgumentException("Use a name from 1 to 48 characters");
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                || Math.abs(x) > 30_000_000 || Math.abs(z) > 30_000_000 || Math.abs(y) > 1_000_000)
            throw new IllegalArgumentException("Coordinates are outside the world range");
        color |= 0xff000000;
    }

    public static Waypoint create(String name, String dimension, double x, double y, double z, int color) {
        return new Waypoint(UUID.randomUUID().toString(), name, dimension, x, y, z, color, true);
    }

    public Waypoint withVisibility(boolean value) {
        return new Waypoint(id, name, dimension, x, y, z, color, value);
    }

    public double distanceTo(double px, double py, double pz) {
        return Math.sqrt((x - px) * (x - px) + (y - py) * (y - py) + (z - pz) * (z - pz));
    }
}
