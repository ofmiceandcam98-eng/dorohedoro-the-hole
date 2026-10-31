package com.dorohedoro.hole.worldgen.city;

/**
 * Pure coordinate hashing. Everything the city generator decides comes from these functions, which is what keeps a
 * chunk's contents independent of the order chunks are generated in.
 */
public final class CityHash {
    private CityHash() {}

    public static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    public static long of(long seed, long a) {
        return mix(seed + 0x9E3779B97F4A7C15L + a * 0xC2B2AE3D27D4EB4FL);
    }

    public static long of(long seed, long a, long b) {
        return mix(of(seed, a) + b * 0x165667B19E3779F9L);
    }

    public static long of(long seed, long a, long b, long c) {
        return mix(of(seed, a, b) + c * 0x27D4EB2F165667C5L);
    }

    /** Uniform integer in {@code [0, bound)}. */
    public static int range(long hash, int bound) {
        return (int) Long.remainderUnsigned(hash, bound);
    }

    /** Uniform float in {@code [0, 1)}. */
    public static float unit(long hash) {
        return (hash >>> 40) * (1.0F / (1L << 24));
    }
}
