package com.dorohedoro.hole.worldgen.city;

import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * Optional cold industrial neighborhoods, derived from the SAME climate sampler as the city
 * biomes and street plan. This keeps snow patches aligned with their biome in every chunk.
 * Snow exists in Hole canon, but persistent frozen districts are a Minecraft adaptation.
 */
public final class ColdDistricts17 {
    public static final int SAFE_START_RADIUS_CELLS = 9;
    private ColdDistricts17() {}

    public static boolean isCold(Climate.Sampler climate, int x, int z) {
        int cx = District.cellOf(x);
        int cz = District.cellOf(z);
        if (Math.max(Math.abs(cx), Math.abs(cz)) <= SAFE_START_RADIUS_CELLS) return false;
        District district = District.ofCell(climate, cx, cz);
        if (district != District.INDUSTRIAL && district != District.OUTSKIRTS) return false;
        int centerX = cx * District.CELL_SIZE + District.CELL_SIZE / 2;
        int centerZ = cz * District.CELL_SIZE + District.CELL_SIZE / 2;
        // Macro climate makes patches locally coherent rather than random scattered snow pixels.
        DensityFunction.SinglePointContext p = new DensityFunction.SinglePointContext(centerX, 0, centerZ);
        return climate.temperature().compute(p) < -0.24
                && climate.humidity().compute(p) < -0.05;
    }
}
