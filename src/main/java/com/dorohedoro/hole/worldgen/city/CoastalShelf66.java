package com.dorohedoro.hole.worldgen.city;

import net.minecraft.world.level.biome.Climate;

/** Pass 66: deterministic, gently graded dry shoreline surrounding true v47 oceans.
 * A climate-seeded geometric land-to-sea boundary; never depends on neighboring chunk state.
 * World Y and ocean depth are deliberately separate: there is no hidden freshwater flooding.
 */
public final class CoastalShelf66 {
    public static final int WIDTH = 16;
    public static final int BEACH_WIDTH = 7;
    private static final int[] DX = {-1, 0, 1, -1, 1, -1, 0, 1};
    private static final int[] DZ = {-1, -1, -1, 0, 0, 1, 1, 1};
    private CoastalShelf66() {}

    /** -1 = inland, 0 = wet ocean, 1..16 = walkable coastline, increasing toward inland. */
    public static int distanceAt(Climate.Sampler climate, int x, int z) {
        // OceanRegions65 makes all potential start positions inland. Fast path for the starter area.
        if (Math.max(Math.abs((long)x), Math.abs((long)z)) < OceanRegions65.LANDING_EXCLUSION - WIDTH) return -1;
        if (OceanRegions65.depthAt(climate, x, z) > 0) return 0;
        int nearest = WIDTH + 1;
        // First sample the outer radius to avoid expensive fine searches inland. Then
        // independently test midway distances so shallow, curved coves aren't missed.
        for (int i = 0; i < DX.length; i++) {
            int end = 0;
            for (int d = WIDTH; d >= 1; d /= 2) {
                if (OceanRegions65.depthAt(climate, x + DX[i] * d, z + DZ[i] * d) > 0) {
                    end = d;
                    break;
                }
            }
            if (end == 0) continue;
            for (int d = 1; d <= end && d < nearest; d++) {
                if (OceanRegions65.depthAt(climate, x + DX[i] * d, z + DZ[i] * d) > 0) {
                    nearest = d;
                    break;
                }
            }
            if (nearest == 1) break;
        }
        return nearest <= WIDTH ? nearest : -1;
    }

    /** Land at the shoreline is one block above seawater; further inland rises gently.
     * The upper part of the reserve is a work-free margin, not a newly invented building.
     */
    public static int drySurfaceY(int distance) {
        if (distance < 1 || distance > WIDTH) throw new IllegalArgumentException("shore distance must be 1..16");
        return OceanRegions65.SEA_SURFACE_Y + 1 + (distance - 1) / 4;
    }
}
