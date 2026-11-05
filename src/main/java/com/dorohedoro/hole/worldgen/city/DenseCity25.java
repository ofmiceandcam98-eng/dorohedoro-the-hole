package com.dorohedoro.hole.worldgen.city;

/** Fresh-world-only procedural city density rules, never applied to old save profiles. */
final class DenseCity25 {
    private DenseCity25() {}

    static boolean isDenseDistrict(District district) {
        return switch (district) {
            case RESIDENTIAL, MARKET, INDUSTRIAL, WATERFRONT, DERELICT -> true;
            case GRAVEYARD, OUTSKIRTS -> false;
        };
    }

    static int maxLot(District district) {
        return switch (district) {
            case RESIDENTIAL -> 19;
            case MARKET -> 18;
            case INDUSTRIAL -> 31; // usable factory, warehouse and service footprints
            case WATERFRONT -> 22;
            case DERELICT -> 23;
            default -> 26;
        };
    }

    /** Three-block walkable alleys keep all local connections navigable. */
    static int localStreetWidth(int depth) { return 3; }
}
