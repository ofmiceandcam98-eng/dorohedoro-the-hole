package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Sparse accumulated snow at curb edges in the optional frostbitten district, not a blanket. */
final class WinterPavement17 {
    private WinterPavement17() {}
    static void paint(Canvas canvas, CityPlan plan) {
        if (!canvas.hasColdDistricts()) return;
        BlockState dusting = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1);
        for (int x=canvas.minX; x<=canvas.maxX; x++) {
            for (int z=canvas.minZ; z<=canvas.maxZ; z++) {
                if (!plan.isColdDistrict(x,z)) continue;
                // Keep the center of roads clear and do not build solid snow walls on paths.
                if (CityHash.range(canvas.hash(x, 417, z), 17) != 0) continue;
                if (!canvas.get(x, GROUND_Y, z).isAir()) continue;
                BlockState floor = canvas.get(x, STREET_Y, z);
                if (floor.is(Blocks.STONE_BRICKS) || floor.is(Blocks.COBBLESTONE)
                        || floor.is(Blocks.GRAY_CONCRETE) || floor.is(Blocks.POLISHED_ANDESITE)) {
                    canvas.set(x, GROUND_Y, z, dusting);
                }
            }
        }
    }
}
