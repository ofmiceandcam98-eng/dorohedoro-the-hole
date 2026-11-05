package com.dorohedoro.hole.worldgen.city;

import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * Pass 42: distant, broad landscape provinces around the inhabited Hole.
 * A province is NOT a vanilla forest/desert: it is a polluted industrial fringe
 * with useful wood, sand, clay and stone, connected to the existing city grid.
 *
 * The climate sampler is seeded by Minecraft. All decisions use absolute block
 * coordinates; no generation-order RNG or cross-chunk mutable state is used.
 * No new province appears within the starting 1,536-block city core.
 */
public final class HoleRegions42 {
    public enum Region { CITY, SNOWFIELDS, DEAD_TAIGA, DUST_BASIN, IRON_BADLANDS, MOUNTAIN_RIM }
    private static final int CELL = 512;
    public static final int CORE_RADIUS = 1536;
    private HoleRegions42() {}

    private static int warpedX(int x, int z) {
        return x + (int) Math.round(72.0 * Math.sin(z / 260.0));
    }
    private static int warpedZ(int x, int z) {
        return z + (int) Math.round(72.0 * Math.sin(x / 290.0));
    }

    public static Region at(Climate.Sampler climate, int x, int z) {
        if (Math.max(Math.abs(x), Math.abs(z)) < CORE_RADIUS) return Region.CITY;
        int wx = warpedX(x, z);
        int wz = warpedZ(x, z);
        int cellX = Math.floorDiv(wx, CELL);
        int cellZ = Math.floorDiv(wz, CELL);
        return regionInCell(climate, cellX, cellZ);
    }

    /**
     * Pass 52: climate may cover the inhabited city as well as its outskirts.
     * No fixed biome at the origin; the sampler itself carries the world's seed.
     * The urbanization decision is independent (UrbanPattern52).
     */
    public static Region atAnywhere(Climate.Sampler climate, int x, int z) {
        return regionInCellAnywhere(climate,
                Math.floorDiv(warpedX(x, z), CELL),
                Math.floorDiv(warpedZ(x, z), CELL));
    }


    /** Climate-only mountain relief, including at the origin in new worlds.
     * Prior versions' seed-independent relief hashing remains untouched. */
    private static Region regionInCellAnywhere(Climate.Sampler climate, int cx, int cz) {
        DensityFunction.SinglePointContext point = new DensityFunction.SinglePointContext(
                cx * CELL + CELL / 2, 0, cz * CELL + CELL / 2);
        double temperature = climate.temperature().compute(point);
        double humidity = climate.humidity().compute(point);
        if ((temperature < -0.43 && humidity < -0.24)
                || (temperature > 0.43 && humidity > 0.32)) return Region.MOUNTAIN_RIM;
        return regionInCell(climate, cx, cz);
    }

    /** Version 35 climate boundaries, with a reduced but nonzero urban relief.
     * Neighbor cells of the SAME climate have continuous ridges. */
    public static double terrainBlendAnywhere(Climate.Sampler climate, int x, int z) {
        final int width = 88;
        int wx = warpedX(x, z), wz = warpedZ(x, z);
        int cx = Math.floorDiv(wx, CELL), cz = Math.floorDiv(wz, CELL);
        int fx = Math.floorMod(wx, CELL), fz = Math.floorMod(wz, CELL);
        Region here = regionInCellAnywhere(climate, cx, cz);
        double border = 1.0;
        if (fx < width && regionInCellAnywhere(climate, cx - 1, cz) != here)
            border = Math.min(border, smooth(fx / (double) width));
        if (fx >= CELL - width && regionInCellAnywhere(climate, cx + 1, cz) != here)
            border = Math.min(border, smooth((CELL - 1 - fx) / (double) width));
        if (fz < width && regionInCellAnywhere(climate, cx, cz - 1) != here)
            border = Math.min(border, smooth(fz / (double) width));
        if (fz >= CELL - width && regionInCellAnywhere(climate, cx, cz + 1) != here)
            border = Math.min(border, smooth((CELL - 1 - fz) / (double) width));
        double distance = Math.hypot((double) x, (double) z);
        double relief = 0.16 + 0.84 * smooth((distance - 480.0) / 1250.0);
        return border * relief;
    }

    /**
     * Pass 49 / new worlds only: smooth the height at genuine province borders,
     * not at every 512-block sampler cell. Two adjacent cells with the same
     * region should form one continuous ridge instead of an artificial flat
     * trench every 512 blocks. Evaluate neighboring climate cells only within
     * the 88-block transition band to avoid multiplying worldgen sampler work.
     * Historical saves retain terrainBlend(int,int) unchanged.
     */
    public static double terrainBlend(Climate.Sampler climate, int x, int z) {
        final int width = 88;
        int wx = warpedX(x, z), wz = warpedZ(x, z);
        int cx = Math.floorDiv(wx, CELL), cz = Math.floorDiv(wz, CELL);
        int fx = Math.floorMod(wx, CELL), fz = Math.floorMod(wz, CELL);
        Region here = regionInCell(climate, cx, cz);
        double border = 1.0;
        if (fx < width && regionInCell(climate, cx - 1, cz) != here)
            border = Math.min(border, smooth(fx / (double) width));
        if (fx >= CELL - width && regionInCell(climate, cx + 1, cz) != here)
            border = Math.min(border, smooth((CELL - 1 - fx) / (double) width));
        if (fz < width && regionInCell(climate, cx, cz - 1) != here)
            border = Math.min(border, smooth(fz / (double) width));
        if (fz >= CELL - width && regionInCell(climate, cx, cz + 1) != here)
            border = Math.min(border, smooth((CELL - 1 - fz) / (double) width));
        double core = smooth((Math.max(Math.abs(x), Math.abs(z)) - CORE_RADIUS) / 192.0);
        return border * core;
    }

    private static Region regionInCell(Climate.Sampler climate, int cx, int cz) {
        DensityFunction.SinglePointContext point = new DensityFunction.SinglePointContext(
                cx * CELL + CELL / 2, 0, cz * CELL + CELL / 2);
        double temperature = climate.temperature().compute(point);
        double humidity = climate.humidity().compute(point);
        int relief = CityHash.range(CityHash.of(0x42A6E1L, cx, cz), 11);
        if (relief == 0 || (relief <= 2 && temperature < -0.18)) return Region.MOUNTAIN_RIM;
        if (temperature < -0.30) return Region.SNOWFIELDS;
        if (temperature < -0.02 && humidity >= -0.16) return Region.DEAD_TAIGA;
        if (temperature > 0.13 && humidity < -0.12) return Region.IRON_BADLANDS;
        if (humidity < -0.06 || temperature > 0.30) return Region.DUST_BASIN;
        return Region.CITY;
    }

    /** Avoid sharp cliffs at a province boundary and protect the central city. */
    public static double terrainBlend(int x, int z) {
        int wx = warpedX(x, z), wz = warpedZ(x, z);
        int edge = Math.min(Math.min(Math.floorMod(wx, CELL), CELL - 1 - Math.floorMod(wx, CELL)),
                Math.min(Math.floorMod(wz, CELL), CELL - 1 - Math.floorMod(wz, CELL)));
        double border = smooth(edge / 88.0);
        double core = smooth((Math.max(Math.abs(x), Math.abs(z)) - CORE_RADIUS) / 192.0);
        return border * core;
    }

    private static double smooth(double t) {
        t = Math.max(0.0, Math.min(1.0, t));
        return t * t * (3.0 - 2.0 * t);
    }
}
