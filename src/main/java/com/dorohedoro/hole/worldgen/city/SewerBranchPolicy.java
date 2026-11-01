package com.dorohedoro.hole.worldgen.city;

/**
 * Pure deterministic sewer selection, independent of Minecraft for offline regression tests.
 *
 * <p>Existing worlds (missing sewer_cave_frequency) keep their original 1-in-11 cave policy.
 * Fresh worlds default to 30% cave approaches, 18% closed inspection bays, and 52% ordinary
 * sewer intersections. These are selection probabilities, not a guarantee that every carved
 * destination meets a pre-existing natural cave. Deep vanilla caves and ore remain unchanged.
 */
public final class SewerBranchPolicy {
    public static final int LEGACY_CAVE_FREQUENCY = 9;
    public static final int DEFAULT_NEW_WORLD_CAVE_FREQUENCY = 30;
    private static final int INSPECTION_FREQUENCY = 18;
    private static final long SALT = 0x5E4E4D2026L;

    private SewerBranchPolicy() {}

    /** Original selection for saves created before the frequency parameter existed. */
    public static int kind(long seed, int xIndex, int zIndex) {
        if (xIndex == 0 && zIndex == 0) return 0;
        return CityHash.range(CityHash.of(seed, SALT, xIndex, zIndex), 11);
    }

    /**
     * Returns 0 for cave-leading approaches, 1 or 2 for sealed inspection bays,
     * and 3 for normal sewer intersections in the new profile.
     *
     * <p>The old profile intentionally delegates to the exact historical selector so previously
     * generated sewer networks do not change where unexplored chunks meet saved ones.
     */
    public static int kind(long seed, int xIndex, int zIndex, int caveFrequency) {
        if (caveFrequency == LEGACY_CAVE_FREQUENCY) return kind(seed, xIndex, zIndex);
        if (xIndex == 0 && zIndex == 0) return 0;
        int roll = CityHash.range(CityHash.of(seed, SALT, xIndex, zIndex), 100);
        if (roll < caveFrequency) return 0;
        int inspectionRoll = roll - caveFrequency;
        if (inspectionRoll < INSPECTION_FREQUENCY / 2) return 1;
        if (inspectionRoll < INSPECTION_FREQUENCY) return 2;
        return 3;
    }

    public static boolean connectsToCave(long seed, int xIndex, int zIndex) {
        return kind(seed, xIndex, zIndex) == 0;
    }

    public static boolean connectsToCave(long seed, int xIndex, int zIndex, int caveFrequency) {
        return kind(seed, xIndex, zIndex, caveFrequency) == 0;
    }

    public static int direction(long seed, int xIndex, int zIndex) {
        return CityHash.range(CityHash.of(seed, SALT + 1, xIndex, zIndex), 2) == 0 ? 1 : -1;
    }
}
