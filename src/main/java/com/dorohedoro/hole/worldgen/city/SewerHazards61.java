package com.dorohedoro.hole.worldgen.city;

/**
 * Deterministic, optional sewer survival pressure for newly generated worlds.
 * This class contains no Minecraft dependencies so frequency and save-compatibility
 * can be regression-tested without a running NeoForge client.
 *
 * <p>Dark utility stretches promote ordinary hostile spawning, but players can
 * relight them with vanilla torches. Rare, optional inspection rooms contain a
 * breakable vanilla spawner for an existing sewer roach, not a technique lock.
 * Every ordinary sewer walkway remains continuous and every public entrance is
 * left intact; no poison, starvation modifier, or unbreakable obstacle is added.
 */
public final class SewerHazards61 {
    public static final int FIRST_VERSION = 44;
    private static final long DARK_SALT = 0x5E7E6112L;
    private static final long NEST_SALT = 0x5E7E6127L;
    private static final int LIGHT_CYCLE = 31;
    private static final int SECTION_LENGTH = 93;

    private SewerHazards61() {}

    /**
     * Lighting is unchanged on old saves; new worlds contain coherent stretches
     * of damaged/missing municipal lighting. Lit sections keep the old cadence.
     * Axis identity is the coordinate of the parent trunk, not a local chunk.
     */
    public static boolean lampAt(long seed, int axis, int along, int worldDetailVersion) {
        if (Math.floorMod(along, LIGHT_CYCLE) != 0) return false;
        if (worldDetailVersion < FIRST_VERSION) return true;
        int section = Math.floorDiv(along, SECTION_LENGTH);
        // 2/5 of sections have no powered lights; all others keep vanilla lanterns.
        return CityHash.range(CityHash.of(seed, DARK_SALT, axis, section), 5) >= 2;
    }

    /**
     * Existing 18-percent inspection-room policy is preserved. About one quarter
     * of these optional side rooms contains a roach nest, not the main artery.
     * Old saves never gain a nest on a previously generated corridor.
     */
    public static boolean infested(long seed, int xIndex, int zIndex,
                                   int branchKind, int worldDetailVersion) {
        return worldDetailVersion >= FIRST_VERSION
                && (branchKind == 1 || branchKind == 2)
                && CityHash.range(CityHash.of(seed, NEST_SALT, xIndex, zIndex), 4) == 0;
    }
}
