package com.dorohedoro.hole.worldgen.city;

import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * City districts. A district is decided per {@value #CELL_SIZE}-block cell from the world's seeded climate noise, so the
 * biome source and the street planner reach the same answer without sharing any state.
 */
public enum District {
    RESIDENTIAL,
    MARKET,
    INDUSTRIAL,
    WATERFRONT,
    DERELICT,
    GRAVEYARD,
    OUTSKIRTS;

    public static final int CELL_SIZE = 128;

    /**
     * The climate noise is tuned for continents. Sampling it at a higher frequency brings districts down to
     * neighbourhood size, so walking a few blocks changes the character of the city.
     */
    private static final int MIX_SCALE = 7;

    public static int cellOf(int block) {
        return Math.floorDiv(block, CELL_SIZE);
    }

    /** The block column at {@code (x, z)}'s district. */
    public static District at(Climate.Sampler sampler, int x, int z) {
        return ofCell(sampler, cellOf(x), cellOf(z));
    }

    public static District ofCell(Climate.Sampler sampler, int cellX, int cellZ) {
        // The four cells around the origin form the starting quarter on every seed.
        if (cellX >= -1 && cellX <= 0 && cellZ >= -1 && cellZ <= 0) {
            return MARKET;
        }
        int centerX = cellX * CELL_SIZE + CELL_SIZE / 2;
        int centerZ = cellZ * CELL_SIZE + CELL_SIZE / 2;
        // The unscaled noise picks out broad zones where the city thins into scrubland and yards.
        if (Math.abs(sampler.temperature().compute(new DensityFunction.SinglePointContext(centerX, 0, centerZ))) > 0.42) {
            return OUTSKIRTS;
        }
        DensityFunction.SinglePointContext mixed = new DensityFunction.SinglePointContext(centerX * MIX_SCALE, 0, centerZ * MIX_SCALE);
        return classify(sampler.temperature().compute(mixed), sampler.humidity().compute(mixed));
    }

    static District classify(double temperature, double humidity) {
        if (humidity > 0.34 && temperature > 0.06) {
            return GRAVEYARD;
        }
        if (humidity < -0.26) {
            return temperature < 0.0 ? WATERFRONT : DERELICT;
        }
        if (temperature > 0.16) {
            return INDUSTRIAL;
        }
        if (temperature < -0.20) {
            return MARKET;
        }
        return RESIDENTIAL;
    }
}
