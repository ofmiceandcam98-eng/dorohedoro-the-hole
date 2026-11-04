package com.dorohedoro.hole.worldgen.city;

/**
 * Coordinate-only broad urban relief. Heights are gentle terraces, not Minecraft mountain noise.
 * The center spawn quarter remains level so its plaza, safe spawn and starter shelters work.
 * The same absolute (seed,x,z) always produces the same vertical displacement.
 *
 * Gameplay extension: the manga does not specify the Hole's exact elevation map.
 */
public final class CityElevation {
    private static final int CELL = 320;
    private static final long SALT = 0x4E2D31A70L;

    private CityElevation() {}

    private static double smoother(double t) {
        return t * t * (3.0 - 2.0 * t);
    }

    private static int knot(long seed, int gx, int gz) {
        return CityHash.range(CityHash.of(seed, SALT ^ gx, gz), 12);
    }

    /** Unrounded legacy relief for blending new terrain without double-rounding steps.
     * Existing worlds continue using the unchanged integer offset() method. */
    static double smoothOffset(long seed, int x, int z) {
        double distance = Math.max(Math.abs(x), Math.abs(z));
        if (distance <= 80) return 0.0;
        int gx = Math.floorDiv(x, CELL);
        int gz = Math.floorDiv(z, CELL);
        double u = smoother(Math.floorMod(x, CELL) / (double) CELL);
        double v = smoother(Math.floorMod(z, CELL) / (double) CELL);
        double a = knot(seed, gx, gz) * (1.0 - u) + knot(seed, gx + 1, gz) * u;
        double b = knot(seed, gx, gz + 1) * (1.0 - u) + knot(seed, gx + 1, gz + 1) * u;
        double blend = Math.min(1.0, (distance - 80.0) / 128.0);
        return (a * (1.0 - v) + b * v) * smoother(blend);
    }

    /** Positive vertical offset, from 0 to about 11 blocks, with gradual district-scale gradients. */
    public static int offset(long seed, int x, int z) {
        // Hard guarantee the entire spawn quarter near (0,0) keeps its original elevation.
        double distance = Math.max(Math.abs(x), Math.abs(z));
        if (distance <= 80) return 0;
        int gx = Math.floorDiv(x, CELL);
        int gz = Math.floorDiv(z, CELL);
        double u = smoother(Math.floorMod(x, CELL) / (double) CELL);
        double v = smoother(Math.floorMod(z, CELL) / (double) CELL);
        double a = knot(seed, gx, gz) * (1.0 - u) + knot(seed, gx + 1, gz) * u;
        double b = knot(seed, gx, gz + 1) * (1.0 - u) + knot(seed, gx + 1, gz + 1) * u;
        double blend = Math.min(1.0, (distance - 80.0) / 128.0);
        return (int) Math.round((a * (1.0 - v) + b * v) * smoother(blend));
    }
}
