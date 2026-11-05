package com.dorohedoro.hole.worldgen.city;

/**
 * Deterministic, low-density tree placement for NEW Hole worlds only (world detail v5).
 * Policy is independent of Minecraft classes so biome/lot frequency can be measured offline.
 * Percentages are per ELIGIBLE lot, not per whole city block, and most lots are built upon.
 */
public final class HoleTreePolicy {
    public enum Form { NONE, DEAD, STUNTED, CROOKED }

    private static final long SALT = 0x4A5E12FA6C1L;
    private HoleTreePolicy() {}

    public static Form select(long seed, long lotHash, District district, LotType type) {
        long h = CityHash.mix(seed ^ CityHash.mix(lotHash + SALT));
        int roll = CityHash.range(h, 1000);
        if (type == LotType.PARK) {
            // Municipal parks are neglected lots, not miniature healthy forests. At most
            // one tree can grow in a park, and most survivors are already dead.
            int living = switch (district) {
                case OUTSKIRTS -> 120;
                case RESIDENTIAL -> 55;
                case GRAVEYARD -> 35;
                case WATERFRONT -> 45;
                case DERELICT -> 25;
                case MARKET -> 15;
                case INDUSTRIAL -> 8;
            };
            int dead = switch (district) {
                case OUTSKIRTS -> 420;
                case RESIDENTIAL -> 390;
                case GRAVEYARD -> 540;
                case WATERFRONT -> 380;
                case DERELICT -> 475;
                case MARKET -> 260;
                case INDUSTRIAL -> 270;
            };
            if (roll < living) return (h & 3L) == 0 ? Form.CROOKED : Form.STUNTED;
            return roll < living + dead ? Form.DEAD : Form.NONE;
        }
        if (type == LotType.VACANT) {
            int living = switch (district) {
                case OUTSKIRTS -> 28;
                case DERELICT -> 15;
                case GRAVEYARD -> 12;
                case WATERFRONT -> 10;
                case RESIDENTIAL -> 7;
                case INDUSTRIAL -> 3;
                case MARKET -> 2;
            };
            int dead = switch (district) {
                case DERELICT -> 140;
                case OUTSKIRTS -> 125;
                case GRAVEYARD -> 100;
                case INDUSTRIAL -> 52;
                case WATERFRONT -> 75;
                case RESIDENTIAL -> 45;
                case MARKET -> 22;
            };
            if (roll < living) return (h & 3L) == 0 ? Form.CROOKED : Form.STUNTED;
            return roll < living + dead ? Form.DEAD : Form.NONE;
        }
        if (type == LotType.RUBBLE) {
            // No lush trees growing out of ruins. Rarely an old bark-stripped snag.
            if (roll < (district == District.OUTSKIRTS ? 40 : 18)) return Form.DEAD;
        }
        return Form.NONE;
    }
}
