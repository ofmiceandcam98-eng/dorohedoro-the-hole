package com.dorohedoro.hole.worldgen.city;

/**
 * Macro-patterns inside each frontier biome. An entire province isn't uniformly
 * a city or uniformly wilderness: settlements, abandoned quarters and navigable
 * open ground alternate on a landscape scale. Decisions are world-seed/absolute-
 * coordinate based and stable at negative coordinates and chunk edges.
 */
public final class SettlementBands47 {
    public enum Form { WORKING, DERELICT, OPEN_COUNTRY }
    private static final long SALT = 0x47BE17L;
    private static final int CELL = 704;
    private SettlementBands47() {}

    private static double node(long seed, int x, int z) {
        return CityHash.unit(CityHash.of(seed, SALT, x, z));
    }

    private static double smooth(double v) { return v * v * (3.0 - 2.0 * v); }

    /** Continuous macrofield before the final land-use threshold. */
    public static double intensity(long seed, int x, int z) {
        int gx = Math.floorDiv(x, CELL), gz = Math.floorDiv(z, CELL);
        double u = smooth(Math.floorMod(x, CELL) / (double) CELL);
        double v = smooth(Math.floorMod(z, CELL) / (double) CELL);
        double a = node(seed, gx, gz) * (1-u) + node(seed, gx+1, gz)*u;
        double b = node(seed, gx, gz+1) * (1-u) + node(seed, gx+1, gz+1)*u;
        return a * (1-v) + b * v;
    }

    public static Form at(long seed, int x, int z) {
        double value = intensity(seed, x, z);
        if (value >= 0.61) return Form.WORKING;
        if (value >= 0.36) return Form.DERELICT;
        return Form.OPEN_COUNTRY;
    }

    /** Applicable only to outer-region lots, not downtown or saved v0..v30 worlds. */
    static LotType choose(HoleRegions42.Region region, Form form, float roll) {
        if (form == Form.OPEN_COUNTRY) {
            if (roll < .61f) return LotType.VACANT;
            if (roll < .83f) return LotType.RUBBLE;
            if (roll < .91f) return LotType.JUNKYARD;
            return switch (region) {
                case DEAD_TAIGA -> LotType.NURSERY;
                case SNOWFIELDS -> LotType.TENEMENT;
                case DUST_BASIN -> LotType.FRINGE_YARD;
                case IRON_BADLANDS -> LotType.WAREHOUSE;
                case MOUNTAIN_RIM -> LotType.GUARD_POST;
                case CITY -> LotType.VACANT;
            };
        }
        if (form == Form.DERELICT) {
            if (roll < .31f) return LotType.RUBBLE;
            if (roll < .49f) return LotType.VACANT;
            if (roll < .63f) return LotType.JUNKYARD;
            if (roll < .82f) return LotType.WAREHOUSE;
            return LotType.TENEMENT;
        }
        return switch (region) {
            case SNOWFIELDS -> roll < .27f ? LotType.TENEMENT : roll < .44f ? LotType.WAREHOUSE
                    : roll < .60f ? LotType.SHOP : roll < .73f ? LotType.GREENHOUSE : LotType.VACANT;
            case DEAD_TAIGA -> roll < .28f ? LotType.TENEMENT : roll < .47f ? LotType.SHOP
                    : roll < .65f ? LotType.NURSERY : roll < .76f ? LotType.GREENHOUSE : LotType.VACANT;
            case DUST_BASIN -> roll < .24f ? LotType.WAREHOUSE : roll < .42f ? LotType.SHOP
                    : roll < .57f ? LotType.TENEMENT : roll < .75f ? LotType.JUNKYARD : LotType.VACANT;
            case IRON_BADLANDS -> roll < .32f ? LotType.FACTORY : roll < .53f ? LotType.WAREHOUSE
                    : roll < .68f ? LotType.TENEMENT : roll < .80f ? LotType.SHOP : LotType.RUBBLE;
            case MOUNTAIN_RIM -> roll < .23f ? LotType.WAREHOUSE : roll < .41f ? LotType.GUARD_POST
                    : roll < .57f ? LotType.TENEMENT : roll < .71f ? LotType.SHOP : LotType.VACANT;
            case CITY -> LotType.VACANT;
        };
    }
}
