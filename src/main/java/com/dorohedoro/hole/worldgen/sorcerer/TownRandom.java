package com.dorohedoro.hole.worldgen.sorcerer;

/**
 * SplitMix64. Town planning and building painting must make the same choices no matter which chunk is being painted,
 * so every decision is drawn from a generator re-created from a stored seed, never from the per-chunk worldgen random.
 */
public final class TownRandom {
    private long state;

    public TownRandom(long seed) {
        this.state = seed;
    }

    public static long mix(long a, long b) {
        long z = a * 0x9E3779B97F4A7C15L + b;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    /** A stable hash of a block column, for wear patterns that must agree across chunk borders. */
    public static int hash(long seed, int x, int z) {
        return (int) (mix(mix(seed, x), z) >>> 33);
    }

    public long nextLong() {
        long z = (state += 0x9E3779B97F4A7C15L);
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    public int nextInt(int bound) {
        return (int) Math.floorMod(nextLong(), (long) bound);
    }

    /** Inclusive range. */
    public int range(int min, int max) {
        return min + nextInt(max - min + 1);
    }

    public double nextDouble() {
        return (nextLong() >>> 11) * 0x1.0p-53;
    }

    public boolean chance(double p) {
        return nextDouble() < p;
    }

    public <T> T pick(T[] values) {
        return values[nextInt(values.length)];
    }
}
