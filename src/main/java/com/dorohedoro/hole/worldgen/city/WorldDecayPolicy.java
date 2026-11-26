package com.dorohedoro.hole.worldgen.city;

/** Pure seeded decay profiles so abandoned districts vary without generation-order dependence. */
final class WorldDecayPolicy {
    private WorldDecayPolicy() {}

    /** 0 occupied, 1 weathered, 2 abandoned/boarded, 3 partially collapsed. */
    static int severity(long hash, District district) {
        int r = CityHash.range(CityHash.mix(hash ^ 0xDECAB1E2026L), 100);
        return switch (district) {
            case DERELICT -> r < 12 ? 0 : r < 35 ? 1 : r < 72 ? 2 : 3;
            case OUTSKIRTS -> r < 44 ? 0 : r < 71 ? 1 : r < 92 ? 2 : 3;
            case INDUSTRIAL -> r < 63 ? 0 : r < 85 ? 1 : r < 96 ? 2 : 3;
            case WATERFRONT -> r < 77 ? 0 : r < 94 ? 1 : 2;
            case GRAVEYARD -> r < 82 ? 0 : r < 96 ? 1 : 2;
            case RESIDENTIAL -> r < 89 ? 0 : r < 98 ? 1 : 2;
            case MARKET -> r < 96 ? 0 : 1;
        };
    }
}
