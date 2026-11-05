package com.dorohedoro.hole.worldgen.city;

/**
 * Multiscale architectural identity, kept entirely independent of Minecraft APIs so the
 * generator can be regression-tested without the game runtime.
 *
 * <p>A neighborhood occupies roughly 320 blocks, each building has its own facade profile,
 * and its silhouette is derived independently. Buildings share a local visual vocabulary
 * without repeating the same shape. All selections are stateless and coordinate-seeded.
 */
final class CityArchitecture {
    private static final long NEIGHBORHOOD_SALT = 0xA7C41E13L;
    private static final long FACADE_SALT = 0xFACADE13L;
    private static final long ROOF_SALT = 0xBEEF2013L;
    private static final long FOOTPRINT_SALT = 0xF00D2013L;
    static final int NEIGHBORHOOD_SIZE = 320;
    static final int FACADE_PROFILES = 6;
    static final int ROOF_PROFILES = 5;

    private CityArchitecture() {}

    static int neighborhood(long seed, int x, int z) {
        return CityHash.range(CityHash.of(seed, NEIGHBORHOOD_SALT,
                Math.floorDiv(x, NEIGHBORHOOD_SIZE), Math.floorDiv(z, NEIGHBORHOOD_SIZE)), 6);
    }

    static int facade(long seed, int x, int z, long buildingHash) {
        int neighborhood = neighborhood(seed, x, z);
        // Mix site identity with the local construction culture; adjacent lots are not clones.
        return CityHash.range(CityHash.of(buildingHash, FACADE_SALT, neighborhood), FACADE_PROFILES);
    }

    static int roof(long seed, int x, int z, long buildingHash) {
        return CityHash.range(CityHash.of(buildingHash, ROOF_SALT,
                neighborhood(seed, x, z)), ROOF_PROFILES);
    }

    static int setback(long seed, int x, int z, long buildingHash) {
        return CityHash.range(CityHash.of(buildingHash, FOOTPRINT_SALT,
                neighborhood(seed, x, z)), 8);
    }

    /** Exact per-profile building setbacks (street-facing south); always nonnegative. */
    record Margins(int west, int east, int north, int south) {}

    static Margins margins(int kind) {
        return new Margins(
                switch (kind) { case 0, 4 -> 2; case 3 -> 3; default -> 1; },
                switch (kind) { case 1, 5 -> 3; case 6 -> 2; default -> 1; },
                switch (kind) { case 2, 5 -> 3; default -> 1; },
                switch (kind) { case 0, 3, 6 -> 3; case 1, 7 -> 2; default -> 1; });
    }

    static long styleHash(long seed, int x, int z, long buildingHash) {
        return CityHash.mix(buildingHash ^ (neighborhood(seed, x, z) * 0x9E3779B97F4A7C15L));
    }
}
