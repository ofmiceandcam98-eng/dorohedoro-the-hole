package com.dorohedoro.hole.worldgen.city;

/**
 * Pass 46: coherent, neighborhood-scale residential rooflines in newly created
 * worlds. The old per-building floor selection was almost independent for
 * every parcel, making adjacent streets resemble a random-height checkerboard.
 *
 * A broad, smoothly interpolated seed field shifts the *existing*, size-aware
 * Housing25 floor count. District rules preserve lower derelict/market fabric
 * and occasional taller, occupied residential blocks. The floor count still
 * drives the existing fully furnished Tower.build loop: never add fake floors.
 * This class performs no world reads or block writes.
 */
final class NeighborhoodSkyline46 {
    private static final int CELL = 192;
    private static final long SALT = 0x4600C17A11L;
    private NeighborhoodSkyline46() {}

    private static double smooth(double x) {
        return x * x * (3.0 - 2.0 * x);
    }

    static double intensity(long seed, int x, int z) {
        int gx = Math.floorDiv(x, CELL);
        int gz = Math.floorDiv(z, CELL);
        double u = smooth(Math.floorMod(x, CELL) / (double) CELL);
        double v = smooth(Math.floorMod(z, CELL) / (double) CELL);
        double a = CityHash.unit(CityHash.of(seed, SALT, gx, gz));
        double b = CityHash.unit(CityHash.of(seed, SALT, gx + 1, gz));
        double c = CityHash.unit(CityHash.of(seed, SALT, gx, gz + 1));
        double d = CityHash.unit(CityHash.of(seed, SALT, gx + 1, gz + 1));
        return (a + (b - a) * u) * (1.0 - v) + (c + (d - c) * u) * v;
    }

    /** Return a real number of habitable storeys, never a decorative fake tower. */
    static int floors(long seed, District district, Rect footprint, long lotHash,
                      int existing, int maximum, boolean apartment) {
        if (maximum <= 1) return Math.max(1, maximum);
        // Skyline overbuilding would make the open outskirts and graveyards
        // look like more continuous downtown instead of distinct districts.
        if (district == District.OUTSKIRTS || district == District.GRAVEYARD) return existing;
        double city = intensity(seed, footprint.centerX(), footprint.centerZ());
        int neighborhoodShift = city >= 0.72 ? 2 : city >= 0.48 ? 1
                : city < 0.20 ? -1 : 0;
        // Market streets should stay predominantly low rise, but permit occupied
        // flats over shops; dereliction is a silhouette change, not empty towers.
        int districtShift = switch (district) {
            case DERELICT -> -1;
            case MARKET -> 0;
            case INDUSTRIAL -> -1;
            case WATERFRONT -> 0;
            case RESIDENTIAL -> 0;
            default -> 0;
        };
        int shift = neighborhoodShift + districtShift;
        // Individual variation keeps occupied buildings from becoming identical
        // but most variation now comes from the neighborhood, not isolated RNG.
        if (CityHash.range(CityHash.of(lotHash, SALT), 7) == 0) shift++;
        int area = footprint.area();
        int allowedGrowth = area >= 270 && footprint.width() >= 15 && footprint.depth() >= 14 ? 2 : 1;
        int low = apartment ? 2 : 1;
        return Math.max(Math.min(low, maximum), Math.min(maximum,
                Math.min(existing + allowedGrowth, existing + shift)));
    }
}
