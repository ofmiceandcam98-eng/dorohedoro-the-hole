package com.dorohedoro.hole.worldgen.city;

import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * Keeps future custom deep-sewer excavations clear of the Deep Dark's ancient-city habitat.
 * The climate test is shared with HoleBiomeSource, preventing generator/biome drift.
 *
 * This is a conservative buffer against our own excavation, NOT a substitute for actual
 * structure-reference tests and verified Ancient City / Warden playtesting.
 */
public final class DeepDarkBuffer63 {
    private DeepDarkBuffer63() {}

    public static boolean matches(double humidity, double temperature) {
        return humidity < -0.22 && temperature < 0.0;
    }

    public static boolean climateAt(Climate.Sampler sampler, int x, int z) {
        // Match the fixed transformed coordinates used by the Hole's underground biome source.
        DensityFunction.SinglePointContext point = new DensityFunction.SinglePointContext(z + 4099, 0, -x + 7919);
        return matches(sampler.humidity().compute(point), sampler.temperature().compute(point));
    }

    /** Conservative 3x3 sampling across a 112x112 area surrounding the current chunk. */
    public static boolean nearDeepDark(Climate.Sampler sampler, int minX, int minZ) {
        int middleX = minX + 8, middleZ = minZ + 8;
        for (int dx = -48; dx <= 48; dx += 48) {
            for (int dz = -48; dz <= 48; dz += 48) {
                if (climateAt(sampler, middleX + dx, middleZ + dz)) return true;
            }
        }
        return false;
    }

    /** Only fresh v45 worlds receive the safety buffer; old save generation remains untouched. */
    public static boolean protects(int worldDetailVersion, int actualY, boolean nearDeepDark) {
        return worldDetailVersion >= 45 && actualY <= -24 && nearDeepDark;
    }
}
