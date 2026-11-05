package com.dorohedoro.hole.worldgen.city;

import java.util.Arrays;

/** Run with plain Java 21: no Minecraft or NeoForge dependencies. */
public final class VerifyCityArchitecture {
    public static void main(String[] args) {
        long[] seeds = {1L, 2L, 42L, 99L, -100L, 20261031L, 8675309L, 12012312L, -987654321L, 0L};
        long[] facades = new long[CityArchitecture.FACADE_PROFILES];
        long[] roofs = new long[CityArchitecture.ROOF_PROFILES];
        long[] setbacks = new long[8];
        long[] neighborhoodTypes = new long[6];
        int tested = 0;
        int pairsDiffer = 0;
        for (long seed : seeds) {
            for (int gx = -55; gx <= 55; gx++) {
                for (int gz = -55; gz <= 55; gz++) {
                    int x = gx * 27 + 5;
                    int z = gz * 29 + 11;
                    long hash = CityHash.of(seed, 0xC011AB13L, gx, gz);
                    int n = CityArchitecture.neighborhood(seed, x, z);
                    int f = CityArchitecture.facade(seed, x, z, hash);
                    int r = CityArchitecture.roof(seed, x, z, hash);
                    int s = CityArchitecture.setback(seed, x, z, hash);
                    neighborhoodTypes[n]++;
                    facades[f]++;
                    roofs[r]++;
                    setbacks[s]++;
                    int next = CityArchitecture.facade(seed, x + 27, z, CityHash.of(seed, 0xC011AB13L, gx + 1, gz));
                    if (next != f) pairsDiffer++;
                    if (CityArchitecture.facade(seed, x, z, hash) != f
                            || CityArchitecture.roof(seed, x, z, hash) != r
                            || CityArchitecture.setback(seed, x, z, hash) != s)
                        throw new AssertionError("Non-deterministic site selection");
                    tested++;
                }
            }
            // Every point inside a 320x320 neighborhood uses one coherent identity.
            for (int gx = -3; gx <= 3; gx++) {
                for (int gz = -3; gz <= 3; gz++) {
                    int x = gx * CityArchitecture.NEIGHBORHOOD_SIZE;
                    int z = gz * CityArchitecture.NEIGHBORHOOD_SIZE;
                    int theme = CityArchitecture.neighborhood(seed, x, z);
                    if (CityArchitecture.neighborhood(seed, x + 319, z + 319) != theme)
                        throw new AssertionError("Neighborhood is not coherent");
                    if (CityArchitecture.neighborhood(seed, x + 15, z + 15) != theme)
                        throw new AssertionError("Neighborhood changed unexpectedly");
                }
            }
        }
        // All eight setback profiles fit inside valid lots and retain the footprint needed
        // for an unbroken stairwell, door, and central room.
        for (int profile = 0; profile < 8; profile++) {
            CityArchitecture.Margins m = CityArchitecture.margins(profile);
            for (int width = 18; width <= 48; width++) {
                for (int depth = 17; depth <= 48; depth++) {
                    int w = width - m.west() - m.east();
                    int d = depth - m.north() - m.south();
                    if (m.west() < 1 || m.east() < 1 || m.north() < 1 || m.south() < 1
                            || w < 11 || d < 10)
                        throw new AssertionError("Invalid footprint profile " + profile + " for " + width + "x" + depth);
                }
            }
        }
        for (long[] counts : new long[][]{facades, roofs, setbacks, neighborhoodTypes})
            for (long count : counts)
                if (count < 500) throw new AssertionError("Missing/rare profile: " + Arrays.toString(counts));
        double different = 100.0 * pairsDiffer / tested;
        if (different < 74.0 || different > 91.0)
            throw new AssertionError("Buildings still repeat their facades too often: " + different + "%");
        System.out.printf("Tested %,d sites, %d neighborhood coherence cases%n", tested, seeds.length * 49 * 2);
        System.out.printf("Adjacent lots with different facades: %.2f%%%n", different);
        System.out.println("Facade profiles: " + Arrays.toString(facades));
        System.out.println("Roof silhouettes: " + Arrays.toString(roofs));
        System.out.println("Setback profiles: " + Arrays.toString(setbacks));
        System.out.println("Neighborhood identities: " + Arrays.toString(neighborhoodTypes));
        System.out.println("Deterministic multiscale architecture regression passed.");
    }
}
