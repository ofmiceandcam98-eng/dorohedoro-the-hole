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
    GRAVEYARD;

    public static final int CELL_SIZE = 128;

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
        DensityFunction.SinglePointContext center =
                new DensityFunction.SinglePointContext(cellX * CELL_SIZE + CELL_SIZE / 2, 0, cellZ * CELL_SIZE + CELL_SIZE / 2);
        return classify(sampler.temperature().compute(center), sampler.humidity().compute(center));
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
