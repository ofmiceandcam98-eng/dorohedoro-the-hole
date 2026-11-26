package com.dorohedoro.hole.worldgen.city;

/**
 * Existing-save compatibility for urban aquatic ecology. No entities or blocks
 * are added: prevents unnecessary permanent fish in newly explored cities and
 * skips waterfront supply barrels that lack an actual foundation.
 */
final class CityEcologyPolicy32 {
    private CityEcologyPolicy32() {}

    /** v0-v18 retain their saved generation rules; v19+ uses vanilla fish despawning. */
    static boolean keepStarterFishPersistent(int worldDetailVersion) {
        return worldDetailVersion < 19;
    }

    /** v19+ waterfront supplies cannot float above missing quay masonry. */
    static boolean canPlaceCanalSupply(int worldDetailVersion, boolean hasFoundation) {
        return worldDetailVersion < 19 || hasFoundation;
    }
}
