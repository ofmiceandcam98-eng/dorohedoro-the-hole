package com.dorohedoro.hole.worldgen.city;

/** Pure seed-stable diagonal boulevard geometry, independent of Minecraft. */
public final class DiagonalGrid14 {
    public static final int PERIOD = 660;
    public static final int ROAD_HALF_DIAGONAL = 8;
    public static final int PROTECTED_SPAWN_RADIUS = 380;
    private static final long POSITIVE_SALT = 0xC010714L;
    private static final long NEGATIVE_SALT = 0xA96F14L;
    private DiagonalGrid14() {}

    private static int intercept(long seed, long salt, int index) {
        return index * PERIOD + CityHash.range(CityHash.of(seed, salt, index), 101) - 50;
    }

    public static boolean isRoad(long seed, int x, int z) {
        if ((long)x * x + (long)z * z < (long)PROTECTED_SPAWN_RADIUS * PROTECTED_SPAWN_RADIUS)
            return false;
        return near(seed, POSITIVE_SALT, x + z, ROAD_HALF_DIAGONAL)
                || near(seed, NEGATIVE_SALT, x - z, ROAD_HALF_DIAGONAL);
    }

    private static boolean near(long seed, long salt, int coordinate, int band) {
        int guess = Math.floorDiv(coordinate, PERIOD);
        for (int k = guess - 1; k <= guess + 1; k++) {
            if (Math.abs((long)coordinate - intercept(seed, salt, k)) <= band) return true;
        }
        return false;
    }

    /** Reserve ANY lot touched by a diagonal outside the spawn core, including border-straddling lots. */
    public static boolean intersects(long seed, Rect rect) {
        long x = Math.max(Math.abs((long)rect.x0()), Math.abs((long)rect.x1()));
        long z = Math.max(Math.abs((long)rect.z0()), Math.abs((long)rect.z1()));
        if (x * x + z * z < (long)PROTECTED_SPAWN_RADIUS * PROTECTED_SPAWN_RADIUS)
            return false;
        return interval(seed, POSITIVE_SALT, rect.x0() + rect.z0(), rect.x1() + rect.z1())
                || interval(seed, NEGATIVE_SALT, rect.x0() - rect.z1(), rect.x1() - rect.z0());
    }

    private static boolean interval(long seed, long salt, int min, int max) {
        int k0 = Math.floorDiv(min, PERIOD) - 1;
        int k1 = Math.floorDiv(max, PERIOD) + 1;
        for (int k = k0; k <= k1; k++) {
            int line = intercept(seed, salt, k);
            if (line + ROAD_HALF_DIAGONAL >= min && line - ROAD_HALF_DIAGONAL <= max) return true;
        }
        return false;
    }
}
