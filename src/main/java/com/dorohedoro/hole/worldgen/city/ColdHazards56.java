package com.dorohedoro.hole.worldgen.city;

import static com.dorohedoro.hole.worldgen.city.CityPlan.GROUND_Y;
import static com.dorohedoro.hole.worldgen.city.CityPlan.STREET_Y;

import com.dorohedoro.hole.registry.ModBlocks;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * v56: the cold districts' pollution, in new worlds only.
 * <ul>
 *   <li><b>Ash-Snow drifts</b> pile against building walls along the pavement, one to three layers deep, the way wind
 *       banks snow into corners. Road centers stay clear. (Vanilla's own snow still settles everywhere else in snowy
 *       biomes, so the gray drifts stand out against the white.)</li>
 *   <li><b>Sludge-Ice floes</b> cover about half of each canal and river surface in irregular patches.</li>
 * </ul>
 * Cold means the optional frostbitten districts and the Snowfields region. Every choice is pure coordinate math, plus
 * the chunk's own blocks for the wall test, so chunks generate identically in any order.
 */
final class ColdHazards56 {
    private static final int CANAL_WATER_TOP = 61;
    private static final long SALT_FLOE = 0x5106E56L;
    private static final long SALT_DRIFT = 0xA54D2F7L;

    private ColdHazards56() {}

    static void paint(Canvas canvas, CityPlan plan) {
        if (!canvas.hasColdHazards56()) return;
        BlockState sludge = ModBlocks.SLUDGE_ICE.get().defaultBlockState();
        for (int x = canvas.minX; x <= canvas.maxX; x++) {
            for (int z = canvas.minZ; z <= canvas.maxZ; z++) {
                if (!cold(plan, x, z)) continue;
                // Floes: the patch cells of the v53 ground give ragged shapes 3 to 6 blocks across.
                if (canvas.get(x, CANAL_WATER_TOP, z).is(Blocks.WATER) && canvas.get(x, CANAL_WATER_TOP + 1, z).isAir()
                        && CityHash.range(CityHash.of(canvas.patchCell(x, CANAL_WATER_TOP, z), SALT_FLOE), 2) == 0) {
                    canvas.set(x, CANAL_WATER_TOP, z, sludge);
                }
                drift(canvas, x, z);
            }
        }
    }

    static boolean cold(CityPlan plan, int x, int z) {
        return plan.isColdDistrict(x, z) || plan.regionAt(x, z) == HoleRegions42.Region.SNOWFIELDS;
    }

    /** An Ash-Snow drift on pavement against a solid wall. Neighbors outside this chunk read as air, so never count. */
    private static void drift(Canvas canvas, int x, int z) {
        if (!canvas.get(x, GROUND_Y, z).isAir() && !canvas.get(x, GROUND_Y, z).is(Blocks.SNOW)) return;
        if (!paving(canvas.get(x, STREET_Y, z))) return;
        boolean wall = false;
        for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
            int nx = x + d[0], nz = z + d[1];
            if (!canvas.contains(nx, nz)) continue;
            BlockState side = canvas.get(nx, GROUND_Y, nz);
            if (side.isSolid() && canvas.get(nx, GROUND_Y + 1, nz).isSolid()) wall = true;
        }
        if (!wall) return;
        long h = canvas.hash(x, (int) SALT_DRIFT, z);
        if (CityHash.range(h, 4) == 0) return;
        int layers = 1 + CityHash.range(h >>> 8, 3);
        canvas.set(x, GROUND_Y, z, ModBlocks.ASH_SNOW.get().defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers));
    }

    private static boolean paving(BlockState floor) {
        return floor.is(Blocks.STONE_BRICKS) || floor.is(Blocks.COBBLESTONE) || floor.is(Blocks.GRAY_CONCRETE)
                || floor.is(Blocks.POLISHED_ANDESITE) || floor.is(Blocks.ANDESITE) || floor.is(Blocks.STONE)
                || floor.is(Blocks.SMOOTH_STONE) || floor.is(Blocks.GRAVEL) || floor.is(Blocks.DIRT)
                || floor.is(Blocks.COARSE_DIRT) || floor.is(Blocks.PACKED_MUD);
    }
}
