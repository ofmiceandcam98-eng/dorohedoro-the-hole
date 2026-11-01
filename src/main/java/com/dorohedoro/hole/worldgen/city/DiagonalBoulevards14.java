package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import net.minecraft.world.level.block.Blocks;

/**
 * Broad diagonal through-routes that break the rectangular arterial grid in new worlds.
 * Their paths are analytic functions of x/z, so roads are seamless at chunk borders.
 * The superblock planner reserves lots that intersect the right-of-way. Surface
 * paving happens after lot construction and before the protected shaft entrances.
 */
final class DiagonalBoulevards14 {
    private static final int CLEAR_HEIGHT = 104;

    private DiagonalBoulevards14() {}

    static boolean isRoad(long seed, int x, int z) {
        return DiagonalGrid14.isRoad(seed, x, z);
    }

    static boolean intersects(long seed, Rect rect) {
        return DiagonalGrid14.intersects(seed, rect);
    }

    static void paint(Canvas c, CityPlan plan) {
        if (!c.hasIrregularCity()) return;
        for (int x = c.minX; x <= c.maxX; x++) for (int z = c.minZ; z <= c.maxZ; z++) {
            if (!isRoad(plan.seed, x, z)) continue;
            int i = plan.xIndex(x), j = plan.zIndex(z);
            if (i == -1 && j == -1) continue;
            CityPlan.LandmarkSite site = plan.landmarkSite(
                    Math.floorDiv(i, CityPlan.LANDMARK_REGION),
                    Math.floorDiv(j, CityPlan.LANDMARK_REGION));
            if (site.blockI() == i && site.blockJ() == j) continue;
            // This creates surface passage only; deep caves, sewers and natural ores remain.
            // The brick bridge deck is above the canal water, leaving the channel intact.
            c.fill(x, STREET_Y - 2, z, x, STREET_Y - 1, z, Blocks.STONE_BRICKS);
            c.set(x, STREET_Y, z, Blocks.GRAY_CONCRETE);
            c.fill(x, GROUND_Y, z, x, CLEAR_HEIGHT, z, Blocks.AIR);
            // Irregular centerline repairs give the diagonals visual wayfinding identity.
            if (Math.floorMod(x + z * 2, 19) <= 1) {
                c.set(x, STREET_Y, z, Blocks.LIGHT_GRAY_CONCRETE);
            }
        }
    }
}
