package com.dorohedoro.hole.worldgen.city;

/**
 * Chunk-independent procedural housing decisions. No templates, shared random
 * streams, chunk order, client state or mutable global caches.
 */
final class HousingPlan29 {
    private HousingPlan29() {}

    static boolean compact(Rect r, int storeys) {
        return storeys <= 2 && r.width() <= 16 && r.depth() <= 15;
    }

    /** Five rooflines; compact homes have no implausible giant antennas. */
    static int roofline(long seed, Rect r, int floors) {
        return CityHash.range(CityHash.of(seed ^ 0x2900A11L, r.centerX(), r.centerZ(), floors), 5);
    }

    /** Seven household identities, different on consecutive levels. */
    static int household(long seed, Rect r, int floor) {
        return CityHash.range(CityHash.of(seed ^ 0x2900B12L, r.centerX(), r.centerZ(), floor), 7);
    }

    /** Five facade typologies, varied independently of street block position. */
    static int frontage(long seed, Rect r, int floors) {
        return CityHash.range(CityHash.of(seed ^ 0x2900C13L, r.centerX(), r.centerZ(), floors), 5);
    }

    static int shutterSpacing(long seed, Rect r) {
        return 3 + CityHash.range(CityHash.of(seed ^ 0x2900D14L, r.centerX(), r.centerZ()), 2);
    }
}
