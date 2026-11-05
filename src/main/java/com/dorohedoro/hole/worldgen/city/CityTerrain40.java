package com.dorohedoro.hole.worldgen.city;

/**
 * Pass 40: district-scale valleys and ridges. The old CityElevation profile is
 * immutable for existing saves. The v24+ profile includes a floodplain mask
 * around every municipal canal so its flowing water never becomes a series
 * of raised source blocks at chunk seams. Hashes use absolute coordinates.
 *
 * A city is NOT a natural watershed: this intentionally grades the paved
 * streets and the level building foundations but cannot by itself add a full
 * river network or ensure every large building entrance is step-free.
 */
public final class CityTerrain40 {
    private static final long BROAD = 0x40B011L;
    private static final long RIDGE = 0x40B012L;
    private CityTerrain40() {}

    private static double smooth(double t) {
        t = Math.max(0.0, Math.min(1.0, t));
        return t * t * (3.0 - 2.0 * t);
    }

    private static double field(long seed, long salt, int x, int z, int period, int amplitude) {
        int gx = Math.floorDiv(x, period), gz = Math.floorDiv(z, period);
        double u = smooth(Math.floorMod(x, period) / (double) period);
        double v = smooth(Math.floorMod(z, period) / (double) period);
        double a = CityHash.range(CityHash.of(seed, salt, gx, gz), amplitude + 1);
        double b = CityHash.range(CityHash.of(seed, salt, gx + 1, gz), amplitude + 1);
        double c = CityHash.range(CityHash.of(seed, salt, gx, gz + 1), amplitude + 1);
        double d = CityHash.range(CityHash.of(seed, salt, gx + 1, gz + 1), amplitude + 1);
        return (a + (b - a) * u) * (1.0 - v) + (c + (d - c) * u) * v;
    }

    private static int canalClearance(CityPlan plan, int x, int z) {
        int clearance = 512;
        for (Line line : plan.xLinesNear(x - 224, x + 224)) {
            if (line.kind() == Line.Kind.CANAL)
                clearance = Math.min(clearance, Math.abs(x - line.center()) - line.halfWidth());
        }
        for (Line line : plan.zLinesNear(z - 224, z + 224)) {
            if (line.kind() == Line.Kind.CANAL)
                clearance = Math.min(clearance, Math.abs(z - line.center()) - line.halfWidth());
        }
        return clearance;
    }

    /**
     * Pass 41: a building is a flat, walkable volume, but its ground-floor door
     * faces the SOUTH public frontage. Sampling the old geometric lot center
     * can leave the doorway several blocks above/below the street when the
     * city-wide v24 terrain creates hills. New saves use the grade of the
     * approach at the CENTER of the south lot edge instead. No road is
     * overwritten and there is no per-chunk mutable pathfinding state.
     *
     * This is a local improvement, NOT a guarantee that all rear service
     * entrances, side alleys, or bridges are accessible on every seed.
     */
    static int frontageOffset(CityPlan plan, Rect lot) {
        return offset(plan, lot.centerX(), lot.z1() + 1);
    }

    public static int offset(CityPlan plan, int x, int z) {
        if (!plan.settings.terrainRelief()) return 0;
        if (plan.settings.worldDetailVersion() < 24) return CityElevation.offset(plan.seed, x, z);
        int spawnDistance = Math.max(Math.abs(x), Math.abs(z));
        if (spawnDistance <= 80) return 0;
        // Reserve the full floodplain and quay apron at constant level, not just
        // the wet tiles. Streets rise over roughly 140 blocks away from its edge.
        int clearance = canalClearance(plan, x, z);
        double floodplain = smooth((clearance - 6.0) / 140.0);
        double spawn = smooth((spawnDistance - 80.0) / 140.0);
        if (floodplain <= 0.0 || spawn <= 0.0) return 0;
        double broad = field(plan.seed, BROAD, x, z, 300, 14);
        double ridge = field(plan.seed, RIDGE, x, z, 155, 5);
        // Keep all topography nonnegative: underground galleries and the safe
        // starting plaza use their existing reference heights.
        double cityGrade = broad + ridge + CityElevation.smoothOffset(plan.seed, x, z);
        // Pass 71 (v53): no regional escarpments. Stepping streets and lots up a 60-block mountain made
        // giant terraced stone pyramids, so new worlds keep only the gentle district-scale relief.
        if (plan.settings.worldDetailVersion() >= 26 && plan.settings.worldDetailVersion() < 53) {
            HoleRegions42.Region region = plan.regionAt(x, z);
            double transition = plan.terrainBlendAt(x, z) * floodplain * spawn;
            // A mountain is a broad rock escarpment outside the Hole, not a
            // random 1-block pillar. Roads, lot foundation, and sewer shafts
            // all sample the same absolute-coordinate grading function.
            if (region == HoleRegions42.Region.MOUNTAIN_RIM) {
                double ramp = field(plan.seed, 0x42A110L, x, z, 420, 36);
                double crests = field(plan.seed, 0x42A111L, x, z, 142, 17);
                cityGrade += (13.0 + ramp + crests) * transition;
            } else if (region == HoleRegions42.Region.IRON_BADLANDS) {
                cityGrade += field(plan.seed, 0x42BADA1L, x, z, 230, 15) * transition;
            } else if (region == HoleRegions42.Region.SNOWFIELDS) {
                cityGrade += field(plan.seed, 0x42F012L, x, z, 260, 9) * transition;
            }
        }
        int cap = plan.settings.worldDetailVersion() >= 53 ? 24 : plan.settings.worldDetailVersion() >= 26 ? 106 : 36;
        return Math.min(cap,
                (int) Math.round(cityGrade * floodplain * spawn));
    }
}
