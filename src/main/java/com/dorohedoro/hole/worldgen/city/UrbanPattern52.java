package com.dorohedoro.hole.worldgen.city;

/**
 * Pass 52: continuous seeded urban fabric independent of climate/biome.
 * A snowfield can contain an occupied downtown and a familiar temperate
 * district can fade into empty countryside. This is entirely deterministic:
 * world seed and block position decide the pattern, not generation order.
 *
 * The close-in historic hub is dense enough to retain city gameplay. Outside
 * it, broad lived-in strips, derelict quarters, open countryside and rare
 * dense satellite neighborhoods appear without changing Minecraft cave, ore,
 * crafting, farming or hostile-spawn systems.
 */
public final class UrbanPattern52 {
    public enum Fabric { DENSE, INHABITED, DERELICT, OPEN_COUNTRY }
    private UrbanPattern52() {}

    private static double smooth(double t) {
        t = Math.max(0.0, Math.min(1.0, t));
        return t * t * (3.0 - 2.0 * t);
    }

    public static Fabric at(long seed, int x, int z) {
        // An irregular, inhabited center rather than another square/ring pattern.
        // Biome selection never sees this calculation: urbanization and climate
        // remain separate. Clamp the edge to keep the downtown starter intact.
        double distance = Math.hypot((double) x, (double) z);
        double coreEdge = 500.0 + 42.0 * Math.sin(x / 211.0)
                + 33.0 * Math.cos(z / 281.0) + 23.0 * Math.sin((x + z) / 153.0);
        if (distance <= coreEdge) return Fabric.DENSE;
        double macro = SettlementBands47.intensity(seed, x, z);
        double urbanBias = 0.33 * (1.0 - smooth((distance - coreEdge) / 1152.0));
        double intensity = macro + urbanBias;
        if (intensity >= 0.79) return Fabric.DENSE;
        if (intensity >= 0.59) return Fabric.INHABITED;
        if (intensity >= 0.39) return Fabric.DERELICT;
        return Fabric.OPEN_COUNTRY;
    }

    /** Used only for new worlds. The old urban-biome lot policy is unchanged. */
    static LotType choose(HoleRegions42.Region climate, Fabric fabric, float roll) {
        if (fabric == Fabric.DENSE) throw new IllegalArgumentException("dense lots use district generator");
        if (climate != HoleRegions42.Region.CITY) {
            SettlementBands47.Form oldForm = switch (fabric) {
                case INHABITED -> SettlementBands47.Form.WORKING;
                case DERELICT -> SettlementBands47.Form.DERELICT;
                case OPEN_COUNTRY -> SettlementBands47.Form.OPEN_COUNTRY;
                case DENSE -> throw new IllegalArgumentException("dense lots use district generator");
            };
            return SettlementBands47.choose(climate, oldForm, roll);
        }
        // CITY is an atmospheric biome, no longer synonymous with urbanization.
        // Its wilderness and hamlets still use the mod's original Minecraft
        // buildings/materials; no fake nonfunctional landmark shells.
        return switch (fabric) {
            case OPEN_COUNTRY -> roll < .63f ? LotType.VACANT : roll < .84f
                    ? LotType.RUBBLE : roll < .94f ? LotType.JUNKYARD : LotType.NURSERY;
            case DERELICT -> roll < .30f ? LotType.RUBBLE : roll < .49f
                    ? LotType.VACANT : roll < .69f ? LotType.WAREHOUSE
                    : roll < .84f ? LotType.JUNKYARD : LotType.TENEMENT;
            case INHABITED -> roll < .26f ? LotType.TENEMENT : roll < .47f
                    ? LotType.SHOP : roll < .61f ? LotType.WAREHOUSE
                    : roll < .74f ? LotType.JUNKYARD : roll < .85f
                    ? LotType.GREENHOUSE : LotType.VACANT;
            case DENSE -> throw new IllegalArgumentException("dense lots use district generator");
        };
    }
}
